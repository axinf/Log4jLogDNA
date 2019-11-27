package com.axinf.Log4jLogDNA;

import com.goebl.david.Webb;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Plugin(
        name = "LogDNAAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class LogDNAAppender extends AbstractAppender{

    private String hostname;
    private Webb http;

    private String appName;
    private boolean includeStacktrace;
    private boolean sendMDC;

    LogDNAAppender(String name, Layout<? extends Serializable> layout, String token, String appName,
                   boolean includeStacktrace, boolean sendMDC){
        super(name, null, layout, false, null);
        this.appName = appName;
        this.includeStacktrace = includeStacktrace;
        this.sendMDC = sendMDC;

        try{
            this.hostname = InetAddress.getLocalHost().getHostName();
        }catch(UnknownHostException e){
            this.hostname = "localhost";
        }

        var webb = Webb.create();
        webb.setBaseUri("https://logs.logdna.com/logs/ingest");
        webb.setDefaultHeader("apikey", token);
        webb.setDefaultHeader("User-Agent", "LogDna Log4j2 Appender");
        webb.setDefaultHeader("Accept", "application/json");
        webb.setDefaultHeader("Content-Type", "application/json");
        this.http = webb;
    }

    @PluginBuilderFactory
    public static LogDNAAppenderBuilder newBuilder(){
        return new LogDNAAppenderBuilder();
    }

    @Override
    public void append(LogEvent event){
        var layout = getLayout();
        var formattedMessage = new String(layout.toByteArray(event));

        var sb = new StringBuilder()
                .append(formattedMessage);

        if(event.getThrownProxy() != null && this.includeStacktrace){
            var throwableProxy = event.getThrownProxy();
            sb.append("\n\n").append(throwableProxy.getClass()).append(": ").append(throwableProxy.getMessage());

            var innerThrowableProxy = throwableProxy.getCauseProxy();
            while(innerThrowableProxy != null){
                sb.append("\n\t").append(innerThrowableProxy.getCauseStackTraceAsString(""));
                innerThrowableProxy = innerThrowableProxy.getCauseProxy();
            }
        }

        try{
            var payload = new JSONObject();
            var lines = new JSONArray();
            payload.put("lines", lines);

            var line = new JSONObject();
            line.put("timestamp", event.getInstant().getEpochMillisecond());
            line.put("level", event.getLevel().toString());
            line.put("app", this.appName);
            line.put("line", sb.toString());

            var meta = new JSONObject();
            meta.put("logger", event.getLoggerName());
            line.put("meta", meta);

            if(this.sendMDC && !event.getContextData().isEmpty()){
                for(var entry : event.getContextData().toMap().entrySet()){
                    meta.put(entry.getKey(), entry.getValue());
                }
            }

            lines.put(line);

            var response = http.post("?hostname=" + encode(this.hostname) +
                    "&now=" + encode(String.valueOf(System.currentTimeMillis())))
                    .body(payload)
                    .retry(3, true)
                    .asJsonObject();

            if(!response.isSuccess()){
                var msg = "Error posting to LogDNA: " + response.getStatusCode() + " ";

                try{
                    msg += response.getStatusLine();
                }catch(Exception ignored){}

                System.err.println(msg);
            }
        }catch(JSONException e){
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.err.println(sw.toString());
        }
    }

    private static String encode(String str){
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
}
