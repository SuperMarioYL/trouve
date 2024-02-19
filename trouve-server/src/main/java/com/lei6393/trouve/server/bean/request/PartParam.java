package com.lei6393.trouve.server.bean.request;

import org.springframework.http.HttpHeaders;

/**
 * @author leiyu
 * @date 2022/7/4 16:26
 */
public class PartParam {

    private HttpHeaders headers;

    private byte[] body;

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
