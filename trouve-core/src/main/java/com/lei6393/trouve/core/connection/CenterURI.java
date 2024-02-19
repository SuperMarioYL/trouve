package com.lei6393.trouve.core.connection;

/**
 * @author yulei
 * @date 2022/5/20 11:12
 */
public enum CenterURI {

    INSTANCE("/trouve/instance"),

    META("/trouve/meta"),

    ;
    private final String uri;

    CenterURI(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
