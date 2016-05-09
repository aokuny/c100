package com.ihandy.quote_common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


import java.net.URLConnection;
import java.io.*;


/**
 * Created by fengwen on 2016/4/28.
 */
public class HttpUtils {

    private static Log log = LogFactory.getLog(HttpUtils.class);

    /**
     * 向指定 URL 发送 GET 方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数
     * @return 所代表远程资源的响应结果
     */
    public static Map<String, String>  sendGet(String url, String param) {
        Map<String, String> resultMap = new HashMap<String, String>();
        String result = "";
        try {
            // 向指定URL发送GET方法的请求
            String urlName = url;
            // 向指定URL发送POST方法的请求
            if(null!=param){
                urlName = urlName + "?" + param;
            }
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            //建立实际的连接
            conn.connect();
            result = convertStreamToString(conn.getInputStream());
            resultMap.put("result", result);
            String cookieValue = conn.getHeaderField("Set-Cookie");
            resultMap.put("JSESSIONID", cookieValue);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }
    /**
     * 向指定 URL 发送 Post 方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数
     * @return 所代表远程资源的响应结果
     */
    public static Map<String, String>  sendPost (String url, String param) {
        Map<String, String> resultMap = new HashMap<String, String>();
        PrintWriter out = null;
        String result = "";
        try {
            String urlName = url;
            // 向指定URL发送POST方法的请求
            if(null!=param){
                 urlName = urlName + "?" + param;
            }
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            result = convertStreamToString(conn.getInputStream());
            resultMap.put("result", result);
            String cookieValue = conn.getHeaderField("Set-Cookie");
            resultMap.put("JSESSIONID", cookieValue);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }
    /**
      ******************* 将输入流InputStream转换为String *******************
     */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



    public static void main(String[] args) throws Exception {


    }


    private static Log logger = LogFactory.getLog(HttpUtils.class);

    private static final int CONNECTION_TIMEOUT = 1000;// 连接超时

    private static final int READ_TIMEOUT = 5000;// 返回超时 1s

    private static final int SEND_HTTP_COUNT = 3;// 连接、读取、reset异常



    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param sessionId
     *            保持会话连接，
     * @return 所代表远程资源的响应结果
     */
    public static Map<String, String> sendPost(String url, String param, String sessionId, int readTimeOut, String encode, int count) {
        Map<String, String> resultMap = new HashMap<String, String>();
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {(TrustManager) new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpsURLConnection conn = (HttpsURLConnection) realUrl.openConnection();
            conn.setSSLSocketFactory(ssf);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            if (readTimeOut != 0) {
                conn.setReadTimeout(readTimeOut);
            } else {
                conn.setReadTimeout(READ_TIMEOUT);
            }
            // 设置通用的请求属性
            // conn.setRequestProperty("Host", "quote.zhonghe-bj.com:8085");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
            if (StringUtils.isBlank(encode)) {
                encode = "utf-8";
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encode);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            if (StringUtils.isNotBlank(sessionId)) {
                conn.setRequestProperty("Cookie", sessionId);
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), encode));
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encode));
            String cookieValue = conn.getHeaderField("Set-Cookie");
            resultMap.put("cookieValue", cookieValue);
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            resultMap.put("result", result);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg.contains("reset") || msg.contains("time")) {
                if (count < SEND_HTTP_COUNT) {
                    count = count + 1;
                    resultMap = sendPost(url, param, sessionId, readTimeOut, encode, count);
                    logger.info("【" + msg + "】，请求重新连接！url->【" + url + "】");
                }
            }
        } finally {
            // 使用finally块来关闭输出流、输入流
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return resultMap;
    }

}
