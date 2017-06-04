package com.ltsllc.mirandaClient;


import com.google.gson.Gson;
import com.ltsllc.miranda.PrivateKey;
import com.ltsllc.miranda.PublicKey;
import com.ltsllc.miranda.servlet.login.LoginHolder;
import com.ltsllc.miranda.servlet.login.LoginObject;
import com.ltsllc.miranda.servlet.login.LoginResultObject;
import com.ltsllc.miranda.user.User;
import com.ltsllc.miranda.util.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Clark on 6/2/2017.
 */
public class Session {
    private static Gson gson = new Gson();

    private String url;
    private User user;
    private HttpClient httpClient;
    private String sessionId;
    private PrivateKey privateKey;

    public Session(User user, PrivateKey privateKey, String url) {
        this.user = user;
        this.privateKey = privateKey;
        this.url = url;
        this.httpClient = HttpClients.createDefault();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public User getUser() {

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public <T> T getReply (HttpResponse httpResponse, Class<T> type) throws IOException {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;

        try {
            inputStream = httpResponse.getEntity().getContent();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int c = inputStream.read();
            while (c != -1) {
                byteArrayOutputStream.write(c);
                c = inputStream.read();
            }

            String json = new String(byteArrayOutputStream.toByteArray());

            return gson.fromJson(json, type);
        } finally {
            Utils.closeIgnoreExceptions(inputStream);
            Utils.closeIgnoreExceptions(inputStreamReader);
        }
    }

    public void connect () throws IOException, GeneralSecurityException {
        String url = getUrl() + "/servlets/login";

        url = "https://localhost/servlets/login";

        // org.apache.http.conn.ssl.SSLConnectionSocketFactory

        HostnameVerifier hostnameVerifier = new CustomHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);


        final SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .build();


        HttpPost post = new HttpPost(url);
        LoginObject loginObject = new LoginObject();
        loginObject.setName(getUser().getName());
        String json = gson.toJson(loginObject);
        StringEntity stringEntity = new StringEntity(json);
        post.setEntity(stringEntity);
        HttpResponse httpResponse = httpClient.execute(post);
        LoginResultObject loginResultObject = getReply(httpResponse, LoginResultObject.class);
        byte[] plainText = getPrivateKey().decrypt(loginResultObject.getSession().getBytes());
        this.sessionId = new String(plainText);
    }
}