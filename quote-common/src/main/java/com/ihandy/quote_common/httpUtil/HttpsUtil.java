package com.ihandy.quote_common.httpUtil;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

/**
 * 无视Https证书是否正确的Java Http Client
 * 
 * 
 * @author renbao99
 *
 * @create 20160509
 * @version 1.0
 */
public class HttpsUtil {
	
	static{
		//设置系统支持SSLv2Hello
		java.lang.System.setProperty( "jdk.tls.client.protocols", "SSLv3,SSLv2Hello");
	}

	/**
	 * 忽视证书HostName
	 */
	private static HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
		public boolean verify(String s, SSLSession sslsession) {
			return true;
		}
	};

	/**
	 * Ignore Certification
	 */
	private static TrustManager ignoreCertificationTrustManger = new X509TrustManager() {
		private X509Certificate[] certificates;
		@Override
		public void checkClientTrusted(X509Certificate certificates[], String authType) throws CertificateException {
			if (this.certificates == null) {
				this.certificates = certificates;
			}
		}
		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
			if (this.certificates == null) {
				this.certificates = ax509certificate;
			}
		}
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};
	
	/**
	 * 发送http请求
	 * @param urlString
	 * @param sessionId
	 * @return
	 */
	public static Map<String, String> sendGetHttps(String urlString, String sessionId) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
		HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
		HttpsURLConnection conn = null;
		InputStream reader = null;
		try {
			URL url = new URL(urlString);
			/*
			 * use ignore host name verifier
			 */
			conn = (HttpsURLConnection) url.openConnection();
			// Prepare SSL Context
			TrustManager[] tm = { ignoreCertificationTrustManger };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			conn.setSSLSocketFactory(ssf);
			if (StringUtils.isNotBlank(sessionId)) {
				conn.setRequestProperty("Cookie", sessionId);
			}
			reader = conn.getInputStream();
			byte[] bytes = new byte[512];
			int length = reader.read(bytes);
			do {
				buffer.write(bytes, 0, length);
				length = reader.read(bytes);
			} while (length > 0);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				conn.disconnect();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		String cookieValue = conn.getHeaderField("Set-Cookie");
		String html = new String(buffer.toByteArray());
		Map<String, String> result = new HashMap<>();
		result.put("cookieValue", cookieValue);
		result.put("html", html);
		return result;
	}
	
	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @param sessionId
	 *  			保持回话的sessionId
	 * @return 所代表远程资源的响应结果
	 */
	public static Map<String, String> sendPostHttps(String url, String param, String sessionId) {
		PrintWriter out = null;
		BufferedReader in = null;
		HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
		HttpsURLConnection conn = null;
		String resultStr = "";
		String cookieValue = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			conn = (HttpsURLConnection) realUrl.openConnection();
			TrustManager[] tm = { ignoreCertificationTrustManger };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			conn.setSSLSocketFactory(ssf);
			conn.setRequestProperty("Accept", "application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("Content-Length", "259");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			if (StringUtils.isNotBlank(sessionId)) {
				conn.setRequestProperty("Cookie", sessionId);
			}
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.getOutputStream().write(param.getBytes());
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
	        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        cookieValue = conn.getHeaderField("Set-Cookie");
	        String line;
	        while ((line = in.readLine()) != null) {
	        	resultStr += line;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 使用finally块来关闭输出流、输入流
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
				conn.disconnect();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		Map<String, String> result = new HashMap<>();
		result.put("cookieValue", cookieValue);
		result.put("html", resultStr);
		return result;
	}
	
	/**
     * 普通post请求
     * @param urlString
     * @param sessionId
     * @return
     */
    public static Map<String, String> sendPost(String urlString, String param, String sessionId){
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        URLConnection conn = null;
        String cookieValue = null;
        try {
        	URL url = new URL(urlString);
        	conn = url.openConnection();
        	conn.setRequestProperty("Accept", "application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("Content-Length", "259");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			if (StringUtils.isNotBlank(sessionId)) {
				conn.setRequestProperty("Cookie", sessionId);
			}
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.getOutputStream().write(param.getBytes());
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
            inputStream = conn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            cookieValue = conn.getHeaderField("Set-Cookie");
        } catch(Exception e){
        	e.printStackTrace();
        } finally {
        	try {
        		 if (reader != null) {
                     reader.close();
                 }
                 if (inputStreamReader != null) {
                     inputStreamReader.close();
                 }
                 if (inputStream != null) {
                     inputStream.close();
                 }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
        }
    	Map<String, String> result = new HashMap<>();
		result.put("cookieValue", cookieValue);
		result.put("html", resultBuffer.toString());
        return result;
    }
    
    /**
     * 普通get请求
     * @param urlString
     * @param sessionId
     * @return
     */
    public static Map<String, String> sendGet(String urlString, String sessionId){
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        URLConnection conn = null;
        String cookieValue = null;
        try {
        	URL url = new URL(urlString);
        	conn = url.openConnection();
            if (StringUtils.isNotBlank(sessionId)) {
            	conn.setRequestProperty("Cookie", sessionId);
    		}
            inputStream = conn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            cookieValue = conn.getHeaderField("Set-Cookie");
        } catch(Exception e){
        	e.printStackTrace();
        } finally {
        	try {
        		 if (reader != null) {
                     reader.close();
                 }
                 if (inputStreamReader != null) {
                     inputStreamReader.close();
                 }
                 if (inputStream != null) {
                     inputStream.close();
                 }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
        }
    	Map<String, String> result = new HashMap<>();
		result.put("cookieValue", cookieValue);
		result.put("html", resultBuffer.toString());
        return result;
    }
}