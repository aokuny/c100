package com.ihandy.quote_common.httpUtil;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	public static Map<String, String> sendPost(String urlString, String param, String sessionId,String enCode){
		if(StringUtils.isBlank(enCode)){
			enCode = "gb2312";
		}
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
			//conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			//conn.setRequestProperty("Content-Length", "357");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			//conn.setRequestProperty("Host", "10.134.136.48:8000");
			//conn.setRequestProperty("x-requested_with", "XMLHttpRequest");
			//conn.setRequestProperty("Accept-Charset", "gbk");
			//conn.setRequestProperty("Referer", "http://10.134.136.48:8000/prpall/business/editRenewalSearch.do?ticket=ST-39492-Rr7iFtJr6X4W5fdLgy36-cas");
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
			inputStreamReader = new InputStreamReader(inputStream,enCode);
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
	public static Map<String, String> sendGet(String urlString, String sessionId,String enCode){
		if(StringUtils.isBlank(enCode)){
			enCode = "gbk";
		}
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
			inputStreamReader = new InputStreamReader(inputStream,enCode);
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
	public static Map<String, String> sendGetForAxatp(String urlString, String sessionId,String enCode){
		if(StringUtils.isBlank(enCode)){
			enCode = "gbk";
		}
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader reader = null;
		StringBuffer resultBuffer = new StringBuffer();
		String tempLine = null;
		URLConnection conn = null;
		String cookieValue = "";
		try {
			URL url = new URL(urlString);
			conn = url.openConnection();
			if (StringUtils.isNoneBlank(sessionId)) {
				conn.setRequestProperty("Cookie", sessionId);
				cookieValue = sessionId;
			}else{
				Map <String,List<String>> headerFields = conn.getHeaderFields();
				for(Map.Entry<String,List<String>> entry:headerFields.entrySet()){
					if(("Set-Cookie").equals(entry.getKey())){
						List list=entry.getValue();
						for(int i=0;i<list.size();i++){
							cookieValue+=list.get(i)+";";
						}
					}
				}
			}

			inputStream = conn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream,enCode);
			reader = new BufferedReader(inputStreamReader);
			while ((tempLine = reader.readLine()) != null) {
				resultBuffer.append(tempLine);
			}


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
	 * 获取图片流
	 * @param urlString
	 * @param sessionId
	 * @return
	 */
	public static Map getURLImgOutByte(String urlString, String sessionId,String enCode){

		Map map =new HashMap();
		byte [] img=null;
		String type="";
		if(StringUtils.isBlank(enCode)){
			enCode = "gbk";
		}
		InputStream inputStream = null;
		StringBuffer resultBuffer = new StringBuffer();
		String tempLine = null;
		URLConnection conn = null;
		try {
			URL url = new URL(urlString);
			conn = url.openConnection();
			if (StringUtils.isNoneBlank(sessionId)) {
				conn.setRequestProperty("Cookie", sessionId);
			}
			type=conn.getHeaderField("Content-Type").split("/")[1];
			inputStream = conn.getInputStream();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while( (len=inputStream.read(buffer)) != -1 ){
				outStream.write(buffer, 0, len);
			}
			inputStream.close();
			img = outStream.toByteArray();

		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		map.put("byte",img);
		map.put("type",type);
		return map;
	}


	/**
	 * 图片流post上传
	 * @param b
	 * @param url
	 * @param name
	 * @return
	 */
	public static String uploadFile(byte[] b , String url , String name){
		String BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
		try {
			URL u= new URL(url);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			//设置头部
			huc.setReadTimeout(10*10000000);
			huc.setConnectTimeout(10*10000000);
			huc.setDoInput(true);
			huc.setDoOutput(true);
			huc.setUseCaches(false);
			huc.setRequestMethod("POST");
			huc.setRequestProperty("Charset", "utf-8");
			huc.setRequestProperty("Connection", "keep-alive");
			huc.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
			//设置头部结束

			DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
			//文件写入准备工作
			StringBuffer sb = new StringBuffer();
			sb.append("--"+BOUNDARY+"\r\n");
			sb.append("Content-Disposition: form-data; name=\""+name+"\"; filename=\"1.jpg\"\r\n");
			sb.append("Content-Type: application/octet-stream; charset=utf-8\r\n" );
			sb.append("\r\n");
			dos.write(sb.toString().getBytes());
			//文件写入准备工作结束

			//开始写入文件流
			dos.write(b, 0, b.length);
			//写入文件流结束

			//开始结尾工作
			sb.delete(0, sb.length());
			sb.append("\r\n--"+BOUNDARY+"--\r\n");
			dos.write(sb.toString().getBytes());
			//结束结尾工作

			//传参结束
			dos.flush();

			//获取返回值
			InputStream inStream = huc.getInputStream();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int responseLen = 0;
			while( (responseLen = inStream.read(buffer)) != -1 ){
				outStream.write(buffer, 0, responseLen);
			}
			inStream.close();
			byte[] data = outStream.toByteArray();
			return new String(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}