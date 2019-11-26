package com.axinf.Log4jLogDNA;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Builder;

import java.io.Serializable;

public class LogDNAAppenderBuilder implements Builder<LogDNAAppender>{

    @PluginElement("Layout")
    private Layout<? extends Serializable> layout;

    @PluginBuilderAttribute
    private String name = "LogDNAAppender";

    @PluginBuilderAttribute(sensitive = true)
    @Required
    private String token;

    @PluginBuilderAttribute
    @Required
    private String appName;

    @PluginBuilderAttribute
    @Required
    private boolean includeStacktrace;

    @PluginBuilderAttribute
    @Required
    private boolean sendMDC;

    public LogDNAAppenderBuilder setName(String name){
        this.name = name;
        return this;
    }

    public LogDNAAppenderBuilder setLayout(Layout<? extends Serializable> layout){
        this.layout = layout;
        return this;
    }

    public LogDNAAppenderBuilder setToken(String token){
        this.token = token;
        return this;
    }

    public LogDNAAppenderBuilder setAppName(String appName){
        this.appName = appName;
        return this;
    }

    public LogDNAAppenderBuilder setIncludeStacktrace(boolean includeStacktrace){
        this.includeStacktrace = includeStacktrace;
        return this;
    }

    public LogDNAAppenderBuilder setSendMDC(boolean sendMDC){
        this.sendMDC = sendMDC;
        return this;
    }

    @Override
    public LogDNAAppender build(){
        return new LogDNAAppender(name, layout, token, appName, includeStacktrace, sendMDC);
    }
}