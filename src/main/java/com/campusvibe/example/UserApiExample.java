package com.campusvibe.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
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
    private static final String PATH_USERLIST = "Prod/user";
    private static final String PROTOCOL = "https";
    private static final String HOST = "ss2jjhgmj1.execute-api.us-east-1.amazonaws.com";
    private static final String ROLESESSIONNAME = "marksession";
    private static final String APIKEY = "gFghdLyQ90aR9RuCG0UCU8OS5UoAuuSk4KL3gFYr";
    private static final String ASSUMEDROLE = "arn:aws:iam::628097307670:role/3rdageTestRole";
    private static final String EXTERNALID = "campusvibe";
    private static final String REGION = "us-east-1";
    private static final String SERVICENAME = "execute-api";

    // "arn:aws:iam::{{otherAWSAccountID}}:role/{{otherAWSRoleName}}"

    public static void main(String[] args) {
        initlogs();
        AssumeRoleResult session_token_result = getststoken();
        try {
            TreeMap<String, String> qParms = new TreeMap<>();
            qParms.put("usertype", "Student");
            URI uri = new URIBuilder()
                .setScheme(PROTOCOL)
                .setHost(HOST)
                .setPath(PATH_USERLIST)
                .setParameters(qParms.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList()))
                .build();
            System.out.println(uri.normalize().toString());
            TreeMap<String, String> awsHeaders = new TreeMap<String, String>();
            awsHeaders.put("host",
                uri.getHost());
            awsHeaders.put("x-amz-security-token",
                session_token_result.getCredentials().getSessionToken());
            awsHeaders.put("x-api-key",
                APIKEY);
            getUsers(uri, awsHeaders,
                session_token_result.getCredentials().getAccessKeyId(),
                session_token_result.getCredentials().getSecretAccessKey());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static AssumeRoleResult getststoken() {
        AWSSecurityTokenService awssts = AWSSecurityTokenServiceClientBuilder
            .standard()
            .build();
        AssumeRoleResult session_token_result = awssts
            .assumeRole(new AssumeRoleRequest()
                .withRoleArn(ASSUMEDROLE)
                .withRoleSessionName(ROLESESSIONNAME).withExternalId(EXTERNALID)
                .withDurationSeconds(1000)
                .withRequestCredentialsProvider(
                    new PropertiesFileCredentialsProvider(
                        "creds.properties")));
        return session_token_result;
    }

    private static void initlogs() {
        java.util.logging.Logger.getLogger("com.amazonaws")
            .setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
            "true");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.wire",
            "DEBUG");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn",
            "DEBUG");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.impl.client",
            "DEBUG");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http.client",
            "DEBUG");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.http",
            "DEBUG");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.com.amazonaws.auth",
            "DEBUG");
    }

    private static void getUsers(
            URI uri,
            Map<String, String> awsHeaders,
            String accessKeyId,
            String secretAccessKey)
            throws IOException,
            ClientProtocolException {
        HttpGet get = new HttpGet(uri);
        for (Entry<String, String> item : awsHeaders.entrySet()) {
            get.addHeader(item.getKey(), item.getValue());
        }
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(SERVICENAME);
        signer.setRegionName(REGION);
        HttpEntity ent = HttpClients
            .custom()
            .addInterceptorLast(new AWSRequestSigningApacheInterceptor(
                SERVICENAME, signer, new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(accessKeyId, secretAccessKey))))
            .build()
            .execute(get)
            .getEntity();
        new BufferedReader(new InputStreamReader(ent.getContent()))
            .lines().forEach(p -> System.out.println(p));
    }
}
