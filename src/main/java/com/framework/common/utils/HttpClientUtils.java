package com.framework.common.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: HttpClientUtils
 * @Description: HttpClient工具类(这里用一句话描述这个类的作用)
 * @date 2019/2/21 17:20
 */
public class HttpClientUtils {
    // 默认字符集
    private static String encoding = "utf-8";
    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);
    private static final int CONNECT_TIMEOUT = 4 * 1000;
    private static final int SOCKET_TIMEOUT = 5 * 1000;
    private static final int REQUEST_CONNECT_TIMEOUT = 3 * 1000;
    private static final int CONNECT_TOTAL = 200;
    private static final int CONNECT_ROUTE = 20;
    private static PoolingHttpClientConnectionManager connManager = null;
    private static CloseableHttpClient client = null;

    /**
     * 静态代码块配置连接池信息
     */
    static {

        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = createSSLConnSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf).register("https", sslsf).build();
        connManager = new PoolingHttpClientConnectionManager(registry);
        // 将最大连接数增加到200
        connManager.setMaxTotal(CONNECT_TOTAL);
        // 将每个路由基础的连接增加到20
        connManager.setDefaultMaxPerRoute(CONNECT_ROUTE);
        // 可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
        connManager.setValidateAfterInactivity(30000);
        // 设置socket超时时间
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build();
        connManager.setDefaultSocketConfig(socketConfig);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(REQUEST_CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            /**
             * 测出超时重试机制为了防止超时不生效而设置
             * 如果直接放回false,不重试
             * 这里会根据情况进行判断是否重试
             */
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return false;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// ssl握手异常
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (request instanceof HttpEntityEnclosingRequest) {
                    return false;
                }
                return false;
            }
        };
        client = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig)
                .setRetryHandler(httpRequestRetryHandler).build();
        if (connManager != null && connManager.getTotalStats() != null) {
            log.info("now client pool " + connManager.getTotalStats().toString());
        }

    }

    //SSL的socket工厂创建
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        // 创建TrustManager() 用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String authType) throws CertificateException {
                // TODO Auto-generated method stub
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String authType) throws CertificateException {
                // TODO Auto-generated method stub
            }
        };
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            sslContext.init(null, new TrustManager[]{(TrustManager) trustManager}, null);
            // 创建SSLSocketFactory , // 不校验域名 ,取代以前验证规则
            sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslsf;
    }


    public static String post(String postURL, Map<String, String> body) {
        PostMethod postMethod = new PostMethod(postURL);
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        try {
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            NameValuePair[] data = new NameValuePair[body.size()];
            int i = 0;
            for (String key : body.keySet()) {
                data[i] = new NameValuePair(key, body.get(key));
                i++;
            }
            postMethod.setRequestBody(data);
            HttpConnectionManagerParams params = connectionManager.getParams();
            // 设置连接超时时间(单位毫秒) 
            params.setConnectionTimeout(CONNECT_TIMEOUT);
            // 设置读数据超时时间(单位毫秒) 
            params.setSoTimeout(SOCKET_TIMEOUT);
            //设置最大连接数
            params.setDefaultMaxConnectionsPerHost(CONNECT_ROUTE);
            // 设置总连接数
            params.setMaxTotalConnections(CONNECT_TOTAL);
            HttpClient httpClient = new HttpClient(connectionManager);
            /*HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();
            // 设置连接超时时间(单位毫秒) 
            managerParams.setConnectionTimeout(CONNECT_TIMEOUT);
            // 设置读数据超时时间(单位毫秒) 
            managerParams.setSoTimeout(SO_TIMEOUT);*/
            int response = httpClient.executeMethod(postMethod);
            String result = postMethod.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
            //出现post error Broken pipe (Write failed)异常
            log.info("post error " + e.getMessage(), e);
            return null;
        } finally {
            postMethod.releaseConnection();
        }
    }


    /**
     * @param url      请求地址
     * @param headers  请求头
     * @param data     请求实体
     * @param encoding 字符集
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求)
     * @author
     * @date 2018年5月10日 下午4:36:17
     */
    public static String sendPost(String url, Map<String, String> headers, JSONObject data, String encoding) {
        log.info("进入post请求方法...");
        log.info("请求入参：URL= " + url);
        log.info("请求入参：headers=" + JSON.toJSONString(headers));
        //log.info("请求入参：data=" + JSON.toJSONString(data));
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost();
        // 设置返回对象
        CloseableHttpResponse response = null;
        try {
            // 设置请求地址
            httpPost.setURI(new URI(url));
            // 设置请求头
            if (!CollectionUtils.isEmpty(headers)) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpPost.setHeaders(allHeader);
            }
            // 设置实体
            httpPost.setEntity(new StringEntity(JSON.toJSONString(data)));
            // 发送请求,返回响应对象
            response = client.execute(httpPost);
            return parseData(response);

        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时,堆栈轨迹如下", cte);
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时,堆栈轨迹如下", ste);
        } catch (ClientProtocolException cpe) {
            // 该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
            log.error("请求通信[" + url + "]时协议异常,堆栈轨迹如下", cpe);
        } catch (ParseException pe) {
            log.error("请求通信[" + url + "]时解析异常,堆栈轨迹如下", pe);
        } catch (IOException ioe) {
            // 该异常通常是网络原因引起的,如HTTP服务器未启动等
            log.error("请求通信[" + url + "]时网络异常,堆栈轨迹如下", ioe);
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时偶遇异常,堆栈轨迹如下", e);
        } finally {
            try {
                if (null != response)
                    response.close();
            } catch (IOException e) {
                log.error("关闭closeableHttpResponse返回对象异常", e);
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return null;
    }

    /**
     * @param url  请求地址
     * @param data 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日 下午4:37:28
     */
    public static String sendPost(String url, JSONObject data) {
        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        return sendPost(url, headers, data, encoding);
    }

    /**
     * @param url    请求地址
     * @param params 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日 下午6:11:05
     */
    public static String sendPost(String url, Map<String, Object> params) {
        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return sendPost(url, headers, data, encoding);
    }

    /**
     * @param url     请求地址
     * @param headers 请求头
     * @param data    请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日 下午4:39:03
     */
    public static String sendPost(String url, Map<String, String> headers, JSONObject data) {
        return sendPost(url, headers, data, encoding);
    }

    /**
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description:(发送post请求，请求数据默认使用UTF-8编码)
     * @author weixs
     * @date 2018年5月10日 下午5:58:40
     */
    public static String sendPost(String url, Map<String, String> headers, Map<String, Object> params) {
        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return sendPost(url, headers, data, encoding);
    }


    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param encoding 编码
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     * @author weixs
     * @date 2018年5月14日 下午2:39:01
     */
    public static String sendGet(String url, Map<String,String> headers,Map<String, Object> params, String encoding) {
        log.info("进入get请求方法...");
        log.info("请求入参：URL= " + url);
        log.info("请求入参：headers=" + JSON.toJSONString(headers));
        log.info("请求入参：params=" + JSON.toJSONString(params));
        // 创建HttpGet
        HttpGet httpGet = new HttpGet();
        // 设置返回对象
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            // 设置请求头
            if (!CollectionUtils.isEmpty(headers)) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpGet.setHeaders(allHeader);
            }
            // 封装参数
            if (!CollectionUtils.isEmpty(params)) {
                for (String key : params.keySet()) {
                    builder.addParameter(key, params.get(key).toString());
                }
            }
            URI uri = builder.build();
            // 设置请求地址
            httpGet.setURI(uri);
            // 发送请求，返回响应对象
            response = client.execute(httpGet,HttpClientContext.create());
            return parseData(response);
        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时,堆栈轨迹如下", cte);
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时,堆栈轨迹如下", ste);
        } catch (ClientProtocolException cpe) {
            // 该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
            log.error("请求通信[" + url + "]时协议异常,堆栈轨迹如下", cpe);
        } catch (ParseException pe) {
            log.error("请求通信[" + url + "]时解析异常,堆栈轨迹如下", pe);
        } catch (IOException ioe) {
            // 该异常通常是网络原因引起的,如HTTP服务器未启动等
            log.error("请求通信[" + url + "]时网络异常,堆栈轨迹如下", ioe);
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时偶遇异常,堆栈轨迹如下", e);
        } finally {
            try {
                if (null != response)
                    response.close();
            } catch (IOException e) {
                log.error("关闭closeableHttpResponse返回对象异常", e);
            }
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }

        return null;
    }

    /**
     * 带头部的get请求
     * @param url
     * @param headers
     * @return
     */
    public static String sendGetHeaders(String url, Map<String, String> headers) {
        Map<String, Object> params = new HashMap<>();
        return sendGet(url, headers, params, encoding);
    }

    /**
     * @param url    请求地址
     * @param params 请求参数
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     * @author weixs
     * @date 2018年5月14日 下午2:32:39
     */
    public static String sendGet(String url, Map<String, Object> params) {
        Map<String, String> headers = new HashMap<>();
        return sendGet(url, headers, params, encoding);
    }

    /**
     * @param url 请求地址
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     * @author weixs
     * @date 2018年5月14日 下午2:33:45
     */
    public static String sendGet(String url) {
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        return sendGet(url, headers, params, encoding);
    }

    /**
     *
     * @param url 请求地址
     * @param headers
     * @param params
     * @return
     */
    public static String sendGet(String url, Map<String, String> headers, Map<String, Object> params) {

        return sendGet(url, headers, params, encoding);
    }

    /**
     * 解析response
     *
     * @param response
     * @return
     * @throws Exception
     */
    public static String parseData(CloseableHttpResponse response) throws Exception {
        // 获取响应状态
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            // 获取响应数据
            return EntityUtils.toString(response.getEntity(), encoding);
        } else {
            log.error("响应失败，状态码：" + status);
        }
        return null;
    }


    /**
     * @param url    请求地址
     * @param params 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日、 下午6:11:05
     */
    public static String sendPut(String url, String token, Map<String, Object> params) {

        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return sendPut(url, token, data);
    }

    /**
     * @param url    请求地址
     * @param params 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日、 下午6:11:05
     */
    public static String sendPut(String url, Map<String, String> headers, Map<String, Object> params) {

        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return sendPut(url, headers, data);
    }


    /**
     * 原生字符串发送put请求
     *
     * @param url
     * @param headers
     * @param headers
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String sendPut(String url, Map<String, String> headers, JSONObject data) {

        log.info("进入get请求方法...");
        log.info("请求入参：URL= " + url);
        //  log.info("请求入参：params=" + JSON.toJSONString(jsonStr));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpPut.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;
        try {
            httpPut.setURI(new URI(url));
            // 设置请求头
            if (headers != null) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpPut.setHeaders(allHeader);
            }
            httpPut.setEntity(new StringEntity(JSON.toJSONString(data)));
            httpResponse = httpClient.execute(httpPut);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 原生字符串发送put请求
     *
     * @param url
     * @param token
     * @param data
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String sendPut(String url, String token, JSONObject data) {

        log.info("进入get请求方法...");
        log.info("请求入参：URL= " + url);
        //  log.info("请求入参：params=" + JSON.toJSONString(jsonStr));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setHeader("DataEncoding", "UTF-8");
        httpPut.setHeader("token", token);

        CloseableHttpResponse httpResponse = null;
        try {
            httpPut.setEntity(new StringEntity(JSON.toJSONString(data)));
            httpResponse = httpClient.execute(httpPut);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String sendFormPost(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        return sendFormPost(url, headers, params);
    }


    public static String sendFormPost(String url, Map<String, String> headers, Map<String, String> params) {
        log.info("进入formPost请求方法...");
        log.info("请求入参：URL= " + url);
        //log.info("请求入参：params=" + JSON.toJSONString(params));
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost();
        // 设置返回对象
        CloseableHttpResponse response = null;
        if (CollectionUtils.isEmpty(headers)) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        try {
            // 设置请求地址
            httpPost.setURI(new URI(url));
            // 设置请求头
            if (headers != null) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpPost.setHeaders(allHeader);
            }
            // 设置表单参数
            List<BasicNameValuePair> formInfo = new ArrayList<>();
            params.forEach((key, value) -> formInfo.add(new BasicNameValuePair(key, value)));
            httpPost.setEntity(new UrlEncodedFormEntity(formInfo, encoding));
            // 发送请求,返回响应对象
            response = client.execute(httpPost, HttpClientContext.create());
            return parseData(response);

        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时,堆栈轨迹如下", cte);
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时,堆栈轨迹如下", ste);
        } catch (ClientProtocolException cpe) {
            // 该异常通常是协议错误导致:比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合HTTP协议要求等
            log.error("请求通信[" + url + "]时协议异常,堆栈轨迹如下", cpe);
        } catch (ParseException pe) {
            log.error("请求通信[" + url + "]时解析异常,堆栈轨迹如下", pe);
        } catch (IOException ioe) {
            // 该异常通常是网络原因引起的,如HTTP服务器未启动等
            log.error("请求通信[" + url + "]时网络异常,堆栈轨迹如下", ioe);
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时偶遇异常,堆栈轨迹如下", e);
        } finally {
            try {
                if (null != response)
                    response.close();
            } catch (IOException e) {
                log.error("关闭closeableHttpResponse返回对象异常", e);
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return null;
    }


    /**
     * @param url    请求地址
     * @param params 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     * @author weixs
     * @date 2018年5月10日 下午6:11:05
     */
    public static String sendDelete(String url, String token, Map<String, Object> params) {

        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return doDelete(url, token, data);
    }

    /**
     * 发送delete请求
     *
     * @param url
     * @param token
     * @param params
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String doDelete(String url, String token, Map<String, Object> params) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpDelete.setConfig(requestConfig);
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("DataEncoding", "UTF-8");
        httpDelete.setHeader("token", token);

        CloseableHttpResponse httpResponse = null;
        try {


            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            // 封装参数
            if (params != null) {
                for (String key : params.keySet()) {
                    builder.addParameter(key, params.get(key).toString());
                }
            }
            URI uri = builder.build();
            log.info("请求地址：" + uri);
            // 设置请求地址
            httpDelete.setURI(uri);


            httpResponse = httpClient.execute(httpDelete);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static void main(String[] args) {
        String s = HttpClientUtils.sendFormPost("http://192.168.1.60:5002/sysUser/getUsers", new HashMap<>());
        System.out.println(s);
    }
}
