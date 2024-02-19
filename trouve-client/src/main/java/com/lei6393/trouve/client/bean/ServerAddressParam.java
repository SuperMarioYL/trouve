package com.lei6393.trouve.client.bean;

import com.lei6393.trouve.client.ServerAddress;

import java.net.URI;

/**
 * @author leiyu
 * @date 2022/6/7 11:36
 */
public class ServerAddressParam {

    private String schema;

    private String host;

    private int port = -1;

    public ServerAddressParam(String schema, String host, int port) {
        this.schema = schema;
        this.host = host;
        this.port = port;
    }

    public static ServerAddressParam of(String uriStr) {
        URI uri = URI.create(uriStr);
        return of(uri.getScheme(), uri.getHost(), uri.getPort());
    }

    public static ServerAddressParam of(ServerAddress address) {
        return of(address.schema(), address.host(), address.port());
    }

    public static ServerAddressParam of(String schema, String host, int port) {
        return new ServerAddressParam(schema, host, port);
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
