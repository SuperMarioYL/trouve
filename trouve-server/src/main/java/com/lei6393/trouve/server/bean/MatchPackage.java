package com.lei6393.trouve.server.bean;

import javax.servlet.http.HttpServletRequest;

/**
 * @author leiyu
 * @date 2022/5/25 23:32
 */
public class MatchPackage {

    private String lookupPath;

    private HttpServletRequest request;

    public MatchPackage() {

    }

    public MatchPackage(HttpServletRequest request) {
        this.request = request;
    }

    public static MatchPackage create(HttpServletRequest request) {
        return new MatchPackage(request);
    }

    public String getLookupPath() {
        return lookupPath;
    }

    public void setLookupPath(String lookupPath) {
        this.lookupPath = lookupPath;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
