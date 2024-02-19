package com.lei6393.trouve.server.bean.response;

import com.lei6393.trouve.server.common.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

/**
 * @author yulei
 * @date 2022/5/25 19:53
 */
public class ResponseParam {

    /**
     * 请求是否成功
     */
    private Boolean successful;

    /**
     * http 状态码
     */
    private HttpStatus status;

    /**
     * response header
     */
    private MultiValueMap<String, String> headers;

    /**
     * response body
     */
    private byte[] body;

    /**
     * 请求耗时
     */
    private long duration = Constants.INVALID_DURATION;

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
