package com.lei6393.trouve.core.data;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yulei
 * @date 2022/5/19 10:12
 */
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = 691905951343271376L;

    private String name;

    private Set<String> patterns;

    private Set<String> mediaTypes;

    private Set<NameValueExpression<String>> headers;

    private Set<NameValueExpression<String>> params;

    public static RequestInfo of(RequestMappingInfo mappingInfo) {
        RequestInfo info = new RequestInfo();
        info.setPatterns(mappingInfo.getPatternsCondition().getPatterns());

        info.setMediaTypes(mappingInfo.getConsumesCondition().getConsumableMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.toSet()));

        info.setHeaders(mappingInfo.getHeadersCondition().getExpressions());

        info.setParams(mappingInfo.getParamsCondition().getExpressions());

        info.setName(mappingInfo.getName());
        return info;
    }

    public RequestMappingInfo toMapping() {
        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(patterns.toArray(new String[]{}))
                .consumes(mediaTypes.toArray(new String[]{}))
                .build();
        return mappingInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<String> patterns) {
        this.patterns = patterns;
    }

    public Set<String> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(Set<String> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public Set<NameValueExpression<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<NameValueExpression<String>> headers) {
        this.headers = headers;
    }

    public Set<NameValueExpression<String>> getParams() {
        return params;
    }

    public void setParams(Set<NameValueExpression<String>> params) {
        this.params = params;
    }
}
