package com.ihandy.quote_core.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by fengwen on 2016/5/11.
 */
public abstract class BasePage {

	private static Logger logger = Logger.getLogger(BasePage.class);

	public String piccSessionId;//picc登录session
	public String cookieValue;//axatp登录session

	protected static Map<String, String> piccSessionIdMap = new HashMap<>();//人保登录缓存
	protected static Map<String, String> axatpSessionIdMap = new HashMap<>();//人保登录缓存
	public  Map<String, String> axatpMap = new LinkedHashMap<>();//天平登录缓存

	public BasePage(int type){
		switch (type) {
			case 1://人保
				initPiccLogin();//初始化picc登录session
				break;
			case 2://天平
				initAxatpLogin();//初始化axatp登录session
				break;
			default:
				break;
		}
	}
  public void  initAxatpLogin(){
	  cookieValue = axatpSessionIdMap.get("cookieValue");
	  if(!StringUtils.isBlank(cookieValue)){

	  }else {
		  //获取session信息
		  String url_login = SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_LOGIN;
		  String cookieValue1 = HttpsUtil.sendGetForAxatp(url_login, null, "GBK").get("cookieValue");

		  //获取要邀请码标识
		  StringBuffer param_login = new StringBuffer();
		  param_login.append("memberName=" + SysConfigInfo.AXATP_USERNAME + "&");
		  param_login.append("flag=ajaxRecommendCode");
		  Map map1 = HttpsUtil.sendPost(url_login, param_login.toString(), cookieValue1, "GBK");
		  String login_two = map1.get("html").toString();


		  //获取验证码图片
		  String url_randCode = SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_PIC + "?type=login";
		  Map imgMap = HttpsUtil.getURLImgOutByte(url_randCode, cookieValue1, "GBK");
		  String html_getCode = HttpsUtil.uploadFile((byte[]) imgMap.get("byte"),
				  "http://192.168.4.117:8011/GetSeccode.aspx", "code." + imgMap.get("type"));
		  JSONObject returnObject = JSON.parseObject(login_two);
		  String agentCode = returnObject.get("agentCode").toString();
		  String checkRecommendCode = returnObject.get("checkRecommendCode").toString();
		  //用户登录
		  StringBuffer paramSb = new StringBuffer();
		  paramSb.append("memberName=" + SysConfigInfo.AXATP_USERNAME + "&");
		  paramSb.append("voucherNoArray=&");
		  paramSb.append("voucherNoArrayLogin=&");
		  paramSb.append("defaultAgentCode=1&");
		  paramSb.append("isVIP=false&");
		  paramSb.append("linkResource=&");
		  paramSb.append("flag=login&");
		  paramSb.append("memberName="+ SysConfigInfo.AXATP_USERNAME +"&");
		  paramSb.append("password=" + SysConfigInfo.AXATP_PWD + "&");
		  paramSb.append("showRecommendCode=1&");
		  paramSb.append("isAgent="+agentCode+"&");
		  paramSb.append("checkRecommendCode="+checkRecommendCode+"&");
		  paramSb.append("recommendCode=" + SysConfigInfo.AXATP_RECOMMENDCODE + "&");
		  paramSb.append("randomCode=" + html_getCode + "&");
		  String param = paramSb.toString();
		  param = param.substring(0, param.length() - 1);
		  System.out.println("cookieValue1 = "+cookieValue1);
		  Map map = HttpsUtil.sendPost(url_login, param.toString(), cookieValue1, "GBK");



		  String html_index = map.get("html").toString();
		  String cookieValue2= map.get("cookieValue").toString();
		  cookieValue = cookieValue1+ cookieValue2;
		  cookieValue =cookieValue.replace("Path=/;","");
		  cookieValue =cookieValue.replace("path=/;","");
		  cookieValue =cookieValue.replace("httponly;","");
		  cookieValue =cookieValue.replace("loginedName=;","");

		  axatpSessionIdMap.put("cookieValue", cookieValue);

		  if (!html_index.contains("你已经登录成功，你可以选择以下操作")) {
				  axatpSessionIdMap.put("cookieValue", "");
				  initAxatpLogin();
		  }
		  else{
			  Document doc = Jsoup.parse(html_index);
			  Element e = doc.getElementById("services_query");
			  Elements es = e.getElementsByClass("userGoTo");
			  es.get(0).select("a");
			  String href =es.get(0).select("a").get(0).attributes().get("href").toString();
			  String[] arrParam = href.split("\\?")[1].split("&");
			  for(int i=0;i<arrParam.length;i++){
				  try {
					  axatpMap.put(arrParam[i].split("=")[0], arrParam[i].split("=")[1]);
				  }catch (Exception e2){
					  axatpMap.put(arrParam[i].split("=")[0], "");
				  }
			  }
		  }
	  }

  }
	/**
	 * 初始化picc登录session(已测试)
	 */
	public void initPiccLogin(){
		piccSessionId = piccSessionIdMap.get("picc_sessionId");
		//尝试sessionId是否可用
		String urlString1 = "http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/bindvalid/bjptBindValid.do";
		String param1="operatorCode=" + SysConfigInfo.PICC_USERNAME + "&checkOperaType=BJ_PT";
		String html = HttpsUtil.sendPost(urlString1, param1, piccSessionId, "").get("html");
		boolean f = false;
		if(!html.contains("302 Moved Temporarily")){//证明sessionId可用
			f = true;
		}
		if(f){
			//logger.info("抓取机器人，【Picc sessionId有效】");
		}else{//不可用的时候，重新获取保持会话sessionid
			logger.info("抓取机器人，【Picc sessionId失效】");
			Map<String, String> ticketMap = this.getTicket("https://" + SysConfigInfo.PICC_MAIN_URL + ":8888/casserver/login?service=http%3A%2F%2F10.134.136.48%3A80%2Fportal%2Findex.jsp", SysConfigInfo.PICC_USERNAME, SysConfigInfo.PICC_PWD1);
			//访问解析出来的页面
			Map<String, String> result1 = HttpsUtil.sendGet(ticketMap.get("ticket"), ticketMap.get("cookieValue"), null);
			String url2 = "http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/?calogin";
			Map<String, String> result2 = HttpsUtil.sendGet(url2,  result1.get("cookieValue"), null);
			String prpall = "prpall=" + SysConfigInfo.PICC_USERNAME;
			String CASTGC = ticketMap.get("CASTGC");
			//String JSESSIONID = result1.get("cookieValue").replace("; path=/", "");
			String JSESSIONID = ticketMap.get("cookieValue");
			String BOCINS_prpall_Cookie = result2.get("cookieValue").replace("; path=/", "");
			String a = prpall + "; " + CASTGC + "; " + JSESSIONID + "; " + BOCINS_prpall_Cookie;
			String reUrl1 = "https://" + SysConfigInfo.PICC_MAIN_URL + ":8888/casserver/login?service=http%3A%2F%2F10.134.136.48%3A8000%2Fprpall%2Findex.jsp%3Fcalogin";
			Map<String, String> result3 = HttpsUtil.sendGetHttps(reUrl1,  a);
			String ex = "<a href=\".*\">";
			String reUrl2 = StringBaseUtils.getTextForMatcher(result3.get("html"), ex);
			reUrl2 = reUrl2.replace("<a href=\"", "");
			reUrl2 = reUrl2.replace("\">", "");
			reUrl2 = reUrl2.replace("amp;", "");
			//String sessionId = result3.get("cookieValue").replace("; path=/", "") + "; " + BOCINS_prpall_Cookie;
			String sessionId = ticketMap.get("cookieValue")+ "; " + BOCINS_prpall_Cookie;
			HttpsUtil.sendGet(reUrl2, sessionId, null);
			piccSessionId = sessionId;
			piccSessionIdMap.put("picc_sessionId", piccSessionId);
			piccSessionIdMap.put("CASTGC", CASTGC);
			logger.info("抓取机器人，【Picc sessionId获取成功】，piccSessionId：" + piccSessionId);
		}
	}

	/**
	 * 获取PICC登录需要的ticket信息(已测试)
	 */
	private  Map<String, String> getTicket(String url, String username, String password){
		Map<String, String> result = new HashMap<>();//返回的结果
		Map<String, String> result1 = HttpsUtil.sendGetHttps(url, null);
		String html = result1.get("html");
		String cookieValue = result1.get("cookieValue");
		cookieValue = cookieValue.replace("; path=/", "");
		result.put("cookieValue", cookieValue);
		//重新拼装，把session拼装进去
		String[] urlArray = url.split("[?]");
		String[] sessionArray = cookieValue.split("=");
		url = urlArray[0] + ";jsessionid=" + sessionArray[1] + "?" +urlArray[1];
		cookieValue = "prpall=" + username + "; " + cookieValue;
		//获取到ltl
		String ltEx = "<input type=\"hidden\" name=\"lt\" value=.* />";
		String lt = StringBaseUtils.getTextForMatcher(html, ltEx);
		lt = lt.substring(38, lt.length());
		lt = lt.substring(0, lt.length() - 4);
		logger.info("抓取机器人，【PICC，lt获取成功，lt：" + lt + "，cookieValue：" + cookieValue + "】");
		//登陆参数
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("_eventId", "submit");
		paramMap.put("button.x", "33");
		paramMap.put("button.y", "14");
		paramMap.put("errorKey", "null");
		paramMap.put("key", "yes");
		paramMap.put("loginMethod", "nameAndPwd");
		paramMap.put("lt", lt);
		paramMap.put("password", password);
		paramMap.put("pcguid", "");
		paramMap.put("PTAVersion", "");
		paramMap.put("rememberFlag", "0");
		paramMap.put("Signature", "");
		paramMap.put("toSign", "");
		paramMap.put("userMac", "");
		paramMap.put("username", username);
		StringBuffer paramSb = new StringBuffer();
		for(String mapKey : paramMap.keySet()){
			String value = paramMap.get(mapKey);
			paramSb.append(mapKey + "=" + value + "&");
		}
		String param = paramSb.toString();
		param = param.substring(0, param.length()-1);
		//登陆
		Map<String, String> result2 = HttpsUtil.sendPostHttps(url, param, cookieValue);
		//登陆完成
		String html1 = result2.get("html");
		if(html1.contains("忘记密码")){//证明密码错误，可能修改密码了
			logger.info("抓取机器人，【PICC密码修改】");
			return getTicket(url, username, SysConfigInfo.PICC_PWD2);
		}
		String ticketEx = "<a href=\".*\">";
		String ticket = StringBaseUtils.getTextForMatcher(html1, ticketEx);
		ticket = ticket.replace("<a href=\"", "");
		ticket = ticket.replace("\">", "");
		result.put("ticket", ticket);
		result.put("CASTGC", result2.get("cookieValue").replace("; path=/casserver", ""));
		logger.info("抓取机器人，【PICC获取ticket成功，使用密码：" + password + "，ticket：" + ticket + "】");
		return result;
	}

	/**
	 * 发送请求，返回html页面
	 * @param request
	 * @return
	 */
	public abstract String doRequest(Request request);

	/**
	 * 解析html页面，返回需要的数据
	 * @param html
	 * @return
	 */
	public abstract Response getResponse(String html, Request request);

	/**
	 * 结合doRequest、getResponse完成整体HTTP发送到解析流程
	 * @param request
	 * @return
	 */
	public abstract Response run(Request request);

}
