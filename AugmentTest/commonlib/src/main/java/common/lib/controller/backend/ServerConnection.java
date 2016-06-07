package common.lib.controller.backend;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import common.lib.helper.Logger;
import common.lib.helper.Utilities;

public class ServerConnection {
    public enum ResponseType {
        RESP_TYPE_BYTE_ARRAY,
        RESP_TYPE_STRING,
        RESP_TYPE_INPUST_STREAM
    }

    String serverUrl = "";
    DefaultHttpClient httpClient = null;


    // Default Constructor
    public ServerConnection() {
    }

    class HttpDelete2 extends HttpPost {
        public HttpDelete2(String url) {
            super(url);
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }

    }

    /*
     * Send Request to the server with the headers
     */
    public CTHttpResponse sendRequestToServer(String url, String methodType, final String contentType,
                                              final HashMap<String, String> additionalHeaders, final HttpParams params, final HttpEntity bodyEntity, final ResponseType responseType) {
        Logger.instance()
                .v(
                        "sendRequestToServer",
                        url + " " + ((bodyEntity != null) ? bodyEntity.toString() + "  " + bodyEntity.getContentLength() : "Null")
                                + "  ", true);


        CTHttpResponse serverResponse = new CTHttpResponse();

        // HttpURLConnection con = null;
        HttpResponse httpResponse = null;
        try {
            httpClient = getNewHttpClient();
            httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
                @Override
                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                    if (!Utilities.isNullString(contentType))
                        request.setHeader(HTTP.CONTENT_TYPE, contentType);

                    if (bodyEntity != null) {
                        request.setHeader(HTTP.CONTENT_LEN, String.valueOf(bodyEntity.getContentLength()));
                    }

                    if (additionalHeaders != null) {
                        Iterator<String> headersKeys = additionalHeaders.keySet().iterator();
                        while (headersKeys.hasNext()) {
                            String key = headersKeys.next();
                            request.setHeader(key, additionalHeaders.get(key));
                        }
                    }
                }
            });

            if (methodType.compareToIgnoreCase(HttpPost.METHOD_NAME) == 0) {
                HttpPost httpPost = new HttpPost(url);

                if (bodyEntity != null) {
                    httpPost.setEntity(bodyEntity);
                }
                httpResponse = httpClient.execute(httpPost);

            } else if (methodType.compareToIgnoreCase(HttpGet.METHOD_NAME) == 0) {
                HttpGet httpGet = new HttpGet(url);
                if(params != null)
                    httpGet.setParams(params);

                httpResponse = httpClient.execute(httpGet);

            } else if (methodType.compareToIgnoreCase(HttpPut.METHOD_NAME) == 0) {
                HttpPut httpPut = new HttpPut(url);

                if (bodyEntity != null) {
                    httpPut.setEntity(bodyEntity);
                }
                httpResponse = httpClient.execute(httpPut);

            } else if (methodType.compareToIgnoreCase(HttpDelete.METHOD_NAME) == 0) {
                HttpDelete2 httpPut = new HttpDelete2(url);

                if (bodyEntity != null) {
                    httpPut.setEntity(bodyEntity);
                }
                httpResponse = httpClient.execute(httpPut);
            }

            if (httpResponse != null) {
                org.apache.http.Header[] headers = httpResponse.getAllHeaders();
                if (headers != null)
                    for (org.apache.http.Header header : headers) {
                        System.out.println(header.getName() + " => " + header.getValue());
                        serverResponse.responseHeaders.put(header.getName(), header.getValue());
                    }
                serverResponse.statusMessage = httpResponse.getStatusLine().getReasonPhrase(); // CTApplication.getContext().getString(R.string.conn_problem);

                serverResponse.statusCode = httpResponse.getStatusLine().getStatusCode();
                // When Succeeded
                if (serverResponse.statusCode == HttpStatus.SC_OK ||
                        serverResponse.statusCode == HttpStatus.SC_CREATED ||
                        ( serverResponse.statusCode == 302 && params != null)) {
                    // Scanner scan1 = new Scanner(con.getInputStream());

                    // String line = IOUtils.toString(httpResponse.getEntity().getContent());
                    InputStream inputStream = httpResponse.getEntity().getContent();
                    if (inputStream != null && responseType == ResponseType.RESP_TYPE_STRING) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        if (reader != null) {
                            int length = (int) httpResponse.getEntity().getContentLength();

                            StringWriter total = null;
                            if (length > 0)
                                total = new StringWriter(length);
                            else
                                total = new StringWriter();

                            Logger.instance().v("ServerConn", ".. Start Reading Response.. " + httpResponse.getEntity().getContentLength(), false);

                            String line;
                            while ((line = reader.readLine()) != null) {
                                total.append(line + "\n");
                                line = null;
                            }
                            line = total.toString();
                            total = null;

                            serverResponse.response = line;
                            reader.close();
                            line = null;
                            reader = null;
                            inputStream = null;
                            // scan1.close();
                        }
                    } else if (responseType == ResponseType.RESP_TYPE_INPUST_STREAM) {
                        serverResponse.response = inputStream;
                    }
                }
                // when failed
                else {
                    // InputStream inputStream = con.getErrorStream();
                    Logger.instance()
                            .v("Not Succeded(!200)",
                                    httpResponse.getStatusLine().getStatusCode() + "   " + httpResponse.getStatusLine().getReasonPhrase(),
                                    true);
                    InputStream inputStream = httpResponse.getEntity().getContent();
                    if (inputStream != null) {
                        String respError = IOUtils.toString(inputStream);
                        if (!Utilities.isNullString(httpResponse.getStatusLine().getReasonPhrase()))
                            serverResponse.errorMessage = httpResponse.getStatusLine().getReasonPhrase();
                        else
                            serverResponse.errorMessage = respError;
                        Logger.instance().v("Error ->", respError, false);
                    }
                }
            }
        }
        // Catch if there
        catch (Exception e) {
            e.printStackTrace();
            serverResponse.error = e;
            try {
                serverResponse.statusCode = (httpResponse != null) ? httpResponse.getStatusLine().getStatusCode() : -1;

            } catch (Exception e1) {
                e1.printStackTrace();
                if (e1 instanceof org.apache.http.conn.ConnectTimeoutException) {
                    serverResponse.statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
                    // response[2] = CTApplication.getContext().getString(R.string.conn_time_out);
                } else {
                    serverResponse.statusCode = -1;
                    serverResponse.errorMessage = null;
                }
                Logger.instance().v("sendRequestToServer()--", e1, true);
            }

            serverResponse.response = null;
            if (e instanceof org.apache.http.conn.ConnectTimeoutException) {
                serverResponse.statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
                // response[2] = CTApplication.getContext().getString(R.string.conn_time_out);
            }
            Logger.instance().v("sendRequestToServer()", e, true);
        } finally {
            System.gc();
        }

        Logger.instance().v("Request End", serverResponse.statusCode + " " + serverResponse.response, false);
        return serverResponse;
    }


    public static DefaultHttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            org.apache.http.conn.ssl.SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 20000;
            HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 30000;
            HttpConnectionParams.setSoTimeout(params, timeoutSocket);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // HttpHost PROXY_HOST = new HttpHost("5.39.79.171 : 3128", 3128);
            DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);
            // httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, PROXY_HOST);
            return httpClient;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    /*
     * To Accept SSL Connections
     */
    static class MySSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]
                    {tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public void cancelConnection() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
