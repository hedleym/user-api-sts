package com.campusvibe.example;

import java.io.*;
import java.util.Properties;

class ClientConfig {
    private String assumedRole;
    private String externalId;
    private String pathUserList;
    private String protocol;
    private String host;
    private String apiKey;
    private String region;
    private String serviceName;

    private ClientConfig() {

    }

    private ClientConfig(String assumedRole, String externalId,
            String pathUserList, String protocol, String host,
            String apiKey, String region, String serviceName) {
        super();
        this.assumedRole = assumedRole;
        this.externalId = externalId;
        this.pathUserList = pathUserList;
        this.protocol = protocol;
        this.host = host;
        this.apiKey = apiKey;
        this.region = region;
        this.serviceName = serviceName;
    }

    static public ClientConfig load(String fileName) {

        try (InputStream input = new FileInputStream(fileName)) {
            Properties prop = new Properties();
            prop.load(input);
            String assumedRole = prop.getProperty("assumed_role");
            String externalId = prop.getProperty("external_id");
            String pathUserList = prop.getProperty("path_user_list");
            String protocol = prop.getProperty("protocol");
            String host = prop.getProperty("host");
            String apiKey = prop.getProperty("api_key");
            String region = prop.getProperty("region");
            String serviceName = prop.getProperty("service_name");
            return new ClientConfig(assumedRole, externalId, pathUserList,
                protocol, host, apiKey, region, serviceName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public String getAssumedRole() {
        return assumedRole;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getPathUserList() {
        return pathUserList;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRegion() {
        return region;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "ClientConfig [assumedRole=" + assumedRole + ", externalId="
                + externalId + ", pathUserList=" + pathUserList + ", protocol="
                + protocol + ", host=" + host + ", apiKey=" + apiKey
                + ", region=" + region + ", serviceName=" + serviceName + "]";
    }

}
