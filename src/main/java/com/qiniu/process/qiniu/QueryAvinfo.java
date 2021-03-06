package com.qiniu.process.qiniu;

import com.qiniu.process.Base;
import com.qiniu.storage.Configuration;
import com.qiniu.util.*;

import java.io.IOException;
import java.util.Map;

public class QueryAvinfo extends Base<Map<String, String>> {

    private String protocol;
    private String domain;
    private String urlIndex;
    private Configuration configuration;
    private MediaManager mediaManager;

    public QueryAvinfo(Configuration configuration, String protocol, String domain, String urlIndex) throws IOException {
        super("avinfo", "", "", null);
        set(configuration, protocol, domain, urlIndex);
        this.mediaManager = new MediaManager(configuration.clone(), protocol);
    }

    public QueryAvinfo(Configuration configuration, String protocol, String domain, String urlIndex, String savePath,
                       int saveIndex) throws IOException {
        super("avinfo", "", "", null, savePath, saveIndex);
        set(configuration, protocol, domain, urlIndex);
        this.mediaManager = new MediaManager(configuration.clone(), protocol);
    }

    public QueryAvinfo(Configuration configuration, String protocol, String domain, String urlIndex, String savePath)
            throws IOException {
        this(configuration, protocol, domain, urlIndex, savePath, 0);
    }

    private void set(Configuration configuration, String protocol, String domain, String urlIndex) throws IOException {
        this.configuration = configuration;
        if (urlIndex == null || "".equals(urlIndex)) {
            this.urlIndex = "url";
            if (domain == null || "".equals(domain)) {
                throw new IOException("please set one of domain and url-index.");
            } else {
                this.protocol = protocol == null || !protocol.matches("(http|https)") ? "http" : protocol;
                RequestUtils.lookUpFirstIpFromHost(domain);
                this.domain = domain;
            }
        } else {
            this.urlIndex = urlIndex;
        }
    }

    @Override
    public QueryAvinfo clone() throws CloneNotSupportedException {
        QueryAvinfo queryAvinfo = (QueryAvinfo)super.clone();
        queryAvinfo.mediaManager = new MediaManager(configuration.clone(), protocol);
        return queryAvinfo;
    }

    @Override
    protected String resultInfo(Map<String, String> line) {
        String key = line.get("key");
        return key == null ? line.get(urlIndex) : String.join("\t", key, line.get(urlIndex));
    }

    @Override
    protected String singleResult(Map<String, String> line) throws Exception {
        String url = line.get(urlIndex);
        String key = line.get("key");
        if (url == null || "".equals(url)) {
            if (key == null) throw new IOException("key is not exists or empty in " + line);
            url = String.join("", protocol, "://", domain, "/", key.replace("\\?", "%3f"));
            line.put(urlIndex, url);
            return String.join("\t", key, url, JsonUtils.toJson(mediaManager.getAvinfoBody(url)));
        }
        return key == null ? String.join("\t", url, JsonUtils.toJson(mediaManager.getAvinfoBody(url))) :
                String.join("\t", key, url, JsonUtils.toJson(mediaManager.getAvinfoBody(url)));
    }

    @Override
    public void closeResource() {
        super.closeResource();
        protocol = null;
        domain = null;
        urlIndex = null;
        configuration = null;
        mediaManager = null;
    }
}
