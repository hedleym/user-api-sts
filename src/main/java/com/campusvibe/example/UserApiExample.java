package com.campusvibe.example;

import java.io.*;
import java.net.*;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.amazonaws.auth.*;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.amazonaws.services.securitytoken.*;
import com.amazonaws.services.securitytoken.model.*;

public class UserApiExample {

    static ClientConfig CONFIG;
    static {
        CONFIG = ClientConfig.load("aws-gw.properties");
    }

    // AWS Account Credential properties file containing accessKey/secretKey
    private static final String CREDENTIALS = "creds.properties";
    // Role session name, used for log tracing etc
    private static final String ROLESESSIONNAME = "myapisession";

    public static void main(String[] args) {
        initlogs();
        final AssumeRoleResult session_token_result = getststoken();
        System.out.println("\n\n======== User list demo");
        demoUserList(session_token_result);
        System.out.println("\n\n======== User query demo");
        demoUser(session_token_result, "user1@campusvibe.com");
    }

    private static void demoUser(
            AssumeRoleResult session_token_result,
            String loginid) {

        try {
            final InputStream in = buildAndExecuteGet(session_token_result,
                CONFIG.getPathUserList() + "/" + loginid,
                new TreeMap<String, String>());
            new BufferedReader(new InputStreamReader(in))
                .lines().forEach(p -> System.out.println(p));

            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void demoUserList(
            final AssumeRoleResult session_token_result) {
        try {
            TreeMap<String, String> qParms = new TreeMap<>();
            // Example query parameters:

            // qParms.put("loginid", "gemadmin@campusvibe.com");
            // qParms.put("roleadmin", "true");
            // qParms.put("roleorganizer", "true");
            // qParms.put("program", "School of Business");
            // qParms.put("firstname", "joe");
            // qParms.put("lastname", "demo");
            // qParms.put("emailaddress", "mark@3rdagesystems.com");
            // qParms.put("state", "active");
            qParms.put("usertype", "Staff");

            final InputStream in = buildAndExecuteGet(session_token_result,
                CONFIG.getPathUserList(), qParms);
            new BufferedReader(new InputStreamReader(in))
                .lines().forEach(p -> System.out.println(p));

            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static AssumeRoleResult getststoken() {
        AWSSecurityTokenService awssts = AWSSecurityTokenServiceClientBuilder
            .standard()
            .build();
        return awssts
            .assumeRole(new AssumeRoleRequest()
                .withRoleArn(CONFIG.getAssumedRole())
                .withRoleSessionName(ROLESESSIONNAME)
                .withExternalId(CONFIG.getExternalId())
                .withDurationSeconds(1000)
                .withRequestCredentialsProvider(
                    new PropertiesFileCredentialsProvider(
                        CREDENTIALS)));
    }

    private static void initlogs() {
        java.util.logging.Logger.getLogger("com.amazonaws")
            .setLevel(java.util.logging.Level.INFO);
        System.setProperty("org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
            "true");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.wire",
            "INFO");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn",
            "INFO");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.impl.client",
            "INFO");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.client",
            "INFO");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http",
            "INFO");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.com.amazonaws.auth",
            "INFO");
    }

    private static InputStream buildAndExecuteGet(
            AssumeRoleResult session_token_result,
            String path,
            TreeMap<String, String> qParms)
            throws IOException,
            ClientProtocolException {
        URI uri;
        try {
            uri = new URIBuilder()
                .setScheme(CONFIG.getProtocol())
                .setHost(CONFIG.getHost())
                .setPath(path)
                .setParameters(qParms.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList()))
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        TreeMap<String, String> awsHeaders = new TreeMap<String, String>();
        awsHeaders.put("host",
            uri.getHost());
        awsHeaders.put("x-amz-security-token",
            session_token_result.getCredentials().getSessionToken());
        awsHeaders.put("x-api-key",
            CONFIG.getApiKey());
        HttpGet get = new HttpGet(uri);
        for (Entry<String, String> item : awsHeaders.entrySet()) {
            get.addHeader(item.getKey(), item.getValue());
        }
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(CONFIG.getServiceName());
        signer.setRegionName(CONFIG.getRegion());
        HttpEntity ent = HttpClients
            .custom()
            .addInterceptorLast(new AWSRequestSigningApacheInterceptor(
                CONFIG.getServiceName(), signer,
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        session_token_result.getCredentials().getAccessKeyId(),
                        session_token_result.getCredentials()
                            .getSecretAccessKey()))))
            .build()
            .execute(get)
            .getEntity();
        return ent.getContent();
    }
}
