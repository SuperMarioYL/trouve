package com.lei6393.trouve.server.bean.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Map;

/**
 * @author leiyu
 * @date 2022/5/24 21:56
 */
public class RequestParam {

    private UrlParam urlParam;

    private MediaType mediaType;

    private HttpHeaders headers;

    private HttpMethod method;

    private boolean isMultiPart = false;

    private byte[] body;

    private Map<String, PartParam> parts;

    private int retryCount = 0;


    public UrlParam getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(UrlParam urlParam) {
        this.urlParam = urlParam;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isMultiPart() {
        return isMultiPart;
    }

    public void setMultiPart(boolean multiPart) {
        isMultiPart = multiPart;
    }

    public Map<String, PartParam> getParts() {
        return parts;
    }

    public void setParts(Map<String, PartParam> parts) {
        this.parts = parts;
    }
}
