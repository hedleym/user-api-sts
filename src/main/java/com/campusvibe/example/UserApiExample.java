package com.campusvibe.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import com.amazonaws.auth.*;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.amazonaws.services.securitytoken.*;
import com.amazonaws.services.securitytoken.model.*;

public class UserApiExample {
    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF-8"));
    }

    static byte[] getSignatureKey(
            String key,
            String dateStamp,
            String regionName,
            String serviceName)
            throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF-8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }
    // "arn:aws:iam::{{otherAWSAccountID}}:role/{{otherAWSRoleName}}"

    public static void main(String[] args) {
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
        AWSSecurityTokenService awssts = AWSSecurityTokenServiceClientBuilder
            .standard()
            .build();
        AssumeRoleResult session_token_result = awssts
            .assumeRole(new AssumeRoleRequest()
                .withRoleArn("arn:aws:iam::628097307670:role/3rdageTestRole")
                .withRoleSessionName("mark").withExternalId("campusvibe")
                .withDurationSeconds(1000)
                .withRequestCredentialsProvider(
                    new PropertiesFileCredentialsProvider(
                        "creds.properties")));
        System.out.println(session_token_result.getAssumedRoleUser().getArn());
        System.out.println(String.format("Num headers: %s",
            session_token_result.getSdkHttpMetadata().getHttpHeaders().size()));
        for (Entry<String, String> item : session_token_result
            .getSdkHttpMetadata().getHttpHeaders().entrySet()) {
            System.out.println(
                String.format("H: %s %s", item.getKey(), item.getValue()));
        }
        session_token_result.getSdkHttpMetadata().getHttpHeaders().entrySet()
            .stream()
            .peek(p -> System.out
                .println(String.format("From request: %s %s", p.getKey(),
                    p.getValue())));
        System.out.println(session_token_result.getCredentials());
        try {
            TreeMap<String, String> qParms = new TreeMap<>();
            qParms.put("firstname", "demo");
            URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("ss2jjhgmj1.execute-api.us-east-1.amazonaws.com")
                .setPath("Prod/user")
                .setParameters(qParms.entrySet().stream()
                    .map(p -> new BasicNameValuePair(p.getKey(), p.getValue()))
                    .collect(Collectors.toList()))
                .build();
            System.out.println(uri.normalize().toString());
            // https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
            // https://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html
            // https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
            // https://docs.aws.amazon.com/general/latest/gr/sigv4-add-signature-to-request.html

            TreeMap<String, String> awsHeaders = new TreeMap<String, String>();
            awsHeaders.put("host",
                uri.getHost());
            awsHeaders.put("x-amz-security-token",
                session_token_result.getCredentials().getSessionToken());
            awsHeaders.put("x-api-key",
                "gFghdLyQ90aR9RuCG0UCU8OS5UoAuuSk4KL3gFYr");
            TreeMap<String, String> dummyheaders = new TreeMap<String, String>();
            AWSV4Auth aWSV4Auth = new AWSV4Auth.Builder(
                session_token_result.getCredentials().getAccessKeyId(),
                session_token_result.getCredentials().getSecretAccessKey())
                    .regionName("us-east-1")
                    .serviceName("execute-api")
                    .httpMethodName("GET") // GET, PUT, POST, DELETE, etc...
                    .canonicalURI(uri.getPath()) // end point
                    .queryParameters(qParms) // query parameters if any
                    .awsHeaders(dummyheaders) // aws header parameters
                    .payload(null) // payload if any
                    .debug() // turn on the debug mode
                    .build();
            getUsers(uri, awsHeaders, aWSV4Auth.getHeaders(),
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

    private static void getUsers(
            URI uri,
            Map<String, String> awsHeaders,
            Map<String, String> headers,
            String accessKeyId,
            String secretAccessKey)
            throws IOException,
            ClientProtocolException {
        HttpGet get = new HttpGet(uri);
        for (Entry<String, String> item : awsHeaders.entrySet()) {
            System.out.println(
                String.format("HTTP Client header %s %s", item.getKey(),
                    item.getValue()));
            get.addHeader(item.getKey(), item.getValue());
        }
        for (Entry<String, String> item : headers.entrySet()) {
            System.out.println(
                String.format("HTTP Client calc header %s %s", item.getKey(),
                    item.getValue()));
            // get.addHeader(item.getKey(), item.getValue());
        }
        for (Header item : Arrays.asList(get.getAllHeaders())) {
            System.out.println(
                String.format("PRE AUTH headers: %s %s", item.getName(),
                    item.getValue()));
        }
        System.out.println(get.getURI());
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("execute-api");
        signer.setRegionName("us-east-1");
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
            "execute-api", signer, new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKeyId, secretAccessKey)));
        CloseableHttpClient httpclient = HttpClients.custom()
            .addInterceptorLast(interceptor)
            .build();
        CloseableHttpResponse resp = httpclient.execute(get);
        HttpEntity ent = resp.getEntity();
        InputStream s = ent.getContent();
        String result = new BufferedReader(new InputStreamReader(s))
            .lines().collect(Collectors.joining("\n"));
        System.out.println(result);
    }
}
