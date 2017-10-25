package com.jerry.http_client_util.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    private HttpClientUtil() {}

    //Character encoding
    public static final String CHARSET_UTF_8 = "utf-8";

    //The content type of HTTP
    public static final String CONTENT_TYPE_TEXT_HTML = "text/xml";

    public static final String CONTENT_TYPE_FORM_URL = "application/x-www-form-urlencoded";

    public static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";

    //The connection manager
    private static PoolingHttpClientConnectionManager pool;

    //The configuration of request
    private static RequestConfig requestConfig;

    private static DefaultHttpRequestRetryHandler requestRetryHandler;

    private static int MAX_CONNECTIONS = 200;
    private static int MAX_ROUTERS = 15;
    private static int SOCKET_TIMEOUT = 5000;
    private static int CONNECT_TIMEOUT = 5000;
    private static int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static int RETYR_COUNT = 3;

    public static void httpClientConfiguration() {
        try {

            pool = new PoolingHttpClientConnectionManager();

            //setting the maximal connection quantity
            pool.setMaxTotal(MAX_CONNECTIONS);

            //setting the maximal router quantity
            pool.setDefaultMaxPerRoute(MAX_ROUTERS);

            //setting the timeout parameters
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void httpClientConfiguration(PoolingHttpClientConnectionManager poolConfiguration,
                                               RequestConfig requestConfiguration) {

        pool = poolConfiguration;
        requestConfig = requestConfiguration;
    }

    private static SSLConnectionSocketFactory createSSLConn() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) {
                    return true;
                }
            }).build();

            sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }

    private static CloseableHttpClient getHttpClient() {
        requestRetryHandler = new DefaultHttpRequestRetryHandler(RETYR_COUNT, false);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(pool)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(requestRetryHandler)
                .build();
        return httpClient;
    }

    private static CloseableHttpClient getHttpsClient() {

        requestRetryHandler = new DefaultHttpRequestRetryHandler(RETYR_COUNT, false);
        CloseableHttpClient httpsClient = HttpClients.custom()
                .setSSLSocketFactory(createSSLConn())
                .setConnectionManager(pool)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(requestRetryHandler)
                .build();
        return httpsClient;
    }

    private static HttpEntity getDefaultMultipartFileEntity(String fileUrl) {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        File file = new File(fileUrl);
        builder.addBinaryBody("file", file);
        return builder.build();
    }

    private static HttpEntity getDefaultMultipartByteArrayEntity(byte[] byteData) {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", byteData);
        return builder.build();
    }



    public static void setRetryCount(int retryCount) {

        RETYR_COUNT = retryCount;
    }

    public static CloseableHttpResponse sendHttpGet(HttpGet httpGet) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            //create the default instance of httpClient
            httpClient = getHttpClient();

            //configuring the request information
            httpGet.setConfig(requestConfig);

            //executing the request
            response = httpClient.execute(httpGet);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpGet(String httpUrl) {

        HttpGet httpGet = new HttpGet(httpUrl);
        return sendHttpGet(httpGet);
    }

    public static CloseableHttpResponse sendHttpPost(HttpPost httpPost) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = getHttpClient();
            httpPost.setConfig(requestConfig);
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public static CloseableHttpResponse sendHttpPost(String httpUrl) {
        HttpPost httpPost = new HttpPost(httpUrl);
        return sendHttpPost(httpPost);
    }

    public static CloseableHttpResponse sendHttpPost(HttpPost httpPost, String paramsJson) {
        try {
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPost(httpPost);
    }

    public static CloseableHttpResponse sendHttpPost(String httpUrl, String paramsJson) {

        HttpPost httpPost = new HttpPost(httpUrl);
        return sendHttpPost(httpPost, paramsJson);
    }

    public static CloseableHttpResponse sendHttpPut(HttpPut httpPut) {

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = getHttpClient();
            httpPut.setConfig(requestConfig);
            response = httpClient.execute(httpPut);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public static CloseableHttpResponse sendHttpPut(String httpUrl) {

        HttpPut httpPut = new HttpPut(httpUrl);
        return sendHttpPut(httpPut);
    }

    public static CloseableHttpResponse sendHttpPut(HttpPut httpPut, String paramsJson) {

        try {
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpPut.setEntity(stringEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpPut(httpPut);
    }

    public static CloseableHttpResponse sendHttpPut(String httpUrl, String paramsJson) {

        HttpPut httpPut = new HttpPut(httpUrl);
        return sendHttpPut(httpPut, paramsJson);
    }

    public static CloseableHttpResponse sendHttpPostWithForm(HttpPost httpPost, Map<String, Object> params) {

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpPostWithForm(String url, Map<String, Object> params) {

        HttpPost httpPost = new HttpPost(url);
        return sendHttpPostWithForm(httpPost, params);
    }

    public static CloseableHttpResponse sendHttpPostWithFile(HttpPost httpPost, String fileUrl) {

        HttpEntity httpEntity = getDefaultMultipartFileEntity(fileUrl);
        httpPost.setEntity(httpEntity);
        return sendHttpPost(httpPost);
    }

    public static CloseableHttpResponse sendHttpPostWithFile(String uri, String fileUrl) {

        HttpPost httpPost = new HttpPost(uri);
        return sendHttpPostWithFile(httpPost, fileUrl);
    }

    public static CloseableHttpResponse sendHttpPostWithByteArray(HttpPost httpPost, byte[] byteData) {

        HttpEntity httpEntity = getDefaultMultipartByteArrayEntity(byteData);
        httpPost.setEntity(httpEntity);
        return sendHttpPost(httpPost);
    }

    public static CloseableHttpResponse sendHttpPostWithByteArray(String uri, byte[] byteData) {

        HttpPost httpPost = new HttpPost(uri);
        return sendHttpPostWithByteArray(httpPost, byteData);
    }

    public static CloseableHttpResponse sendHttpsGet(HttpGet httpGet) {

        CloseableHttpClient httpsClient = getHttpsClient();
        CloseableHttpResponse response = null;
        try {
            httpGet.setConfig(requestConfig);
            response = httpsClient.execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpsGet(String httpUrl) {

        HttpGet httpsGet = new HttpGet(httpUrl);
        return sendHttpsGet(httpsGet);
    }

    public static CloseableHttpResponse sendHttpsPost(HttpPost httpsPost) {

        CloseableHttpClient httpsClient = getHttpsClient();
        CloseableHttpResponse response = null;

        try {
            httpsPost.setConfig(requestConfig);
            response = httpsClient.execute(httpsPost);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpsPost(String httpUrl) {
        HttpPost httpsPost = new HttpPost(httpUrl);
        return sendHttpsPost(httpsPost);
    }

    public static CloseableHttpResponse sendHttpsPost(HttpPost httpsPost, String paramsJson) {
        try {
            httpsPost.setConfig(requestConfig);
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpsPost.setEntity(stringEntity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpsPost(httpsPost);
    }

    public static CloseableHttpResponse sendHttpsPost(String uri, String paramsJson) {

        HttpPost httpsPost = new HttpPost(uri);
        return sendHttpsPost(httpsPost, paramsJson);
    }

    public static CloseableHttpResponse sendHttpsPut(HttpPut httpsPut) {

        CloseableHttpClient httpsClient = getHttpsClient();
        CloseableHttpResponse response = null;

        try {
            httpsPut.setConfig(requestConfig);
            response = httpsClient.execute(httpsPut);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpsPut(String httpUrl) {

        HttpPut httpsPut = new HttpPut(httpUrl);
        return sendHttpsPut(httpsPut);
    }

    public static CloseableHttpResponse sendHttpsPut(HttpPut httpsPut, String paramsJson) {

        try {
            httpsPut.setConfig(requestConfig);
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(CONTENT_TYPE_JSON_URL);
                httpsPut.setEntity(stringEntity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendHttpsPut(httpsPut);
    }

    public static CloseableHttpResponse sendHttpsPut(String uri, String paramsJson) {

        HttpPut httpsPut = new HttpPut(uri);
        return sendHttpsPut(httpsPut, paramsJson);
    }

    public static CloseableHttpResponse sendHttpsPostWithForm(HttpPost httpsPost, Map<String, Object> params) {

        CloseableHttpClient httpsClient = getHttpsClient();
        CloseableHttpResponse response = null;
        try {
            httpsPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            httpsPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));
            response = httpsClient.execute(httpsPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CloseableHttpResponse sendHttpsPostWithForm(String uri, Map<String, Object> params) {

        HttpPost httpsPost = new HttpPost(uri);
        return sendHttpsPostWithForm(httpsPost, params);
    }

    public static CloseableHttpResponse sendHttpsPostWithFile(HttpPost httpsPost, String fileUrl) {

        HttpEntity httpEntity = getDefaultMultipartFileEntity(fileUrl);
        httpsPost.setEntity(httpEntity);
        return sendHttpsPost(httpsPost);
    }

    public static CloseableHttpResponse sendHttpsPostWithFile(String uri, String fileUrl) {

        HttpPost httpsPost = new HttpPost(uri);
        return sendHttpsPostWithFile(httpsPost, fileUrl);
    }

    public static CloseableHttpResponse sendHttpsPostWithByteArray(HttpPost httpsPost, byte[] byteData) {

        HttpEntity httpEntity = getDefaultMultipartByteArrayEntity(byteData);
        httpsPost.setEntity(httpEntity);
        return sendHttpsPost(httpsPost);
    }

    public static CloseableHttpResponse sendHttpsPostWithByteArray(String uri, byte[] byteData) {

        HttpPost httpsPost = new HttpPost(uri);
        return sendHttpsPostWithByteArray(httpsPost, byteData);
    }

    public static String getResponseEntity(CloseableHttpResponse response) {

        String responseContent = null;
        try {
            responseContent = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) {
            log.warn("no entity" + iae);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseContent;
    }

    public static int getResponseCode(CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

}
