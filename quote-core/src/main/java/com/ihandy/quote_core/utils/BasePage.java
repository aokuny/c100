package com.ihandy.quote_core.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;

/**
 * Created by fengwen on 2016/5/11.
 */
public abstract class BasePage  implements  IPage{
	
	private static Logger logger = Logger.getLogger(BasePage.class);
	
    public String piccSessionId;//picc登录session
    
    private Map<String, String> piccSessionIdMap = new HashMap<>();//人保登录缓存
    
    public BasePage(){
    	logger.info("抓取机器人，【初始化PICC登录session开始】");
    	//initPiccLogin();//初始化picc登录session
    	logger.info("抓取机器人，【初始化PICC登录session完成】");
    }
    
    /**
     * 初始化picc登录session(已测试)
     */
    public void initPiccLogin(){
    	piccSessionId = piccSessionIdMap.get("picc_sessionId");
    	//尝试sessionId是否可用
    	String urlString1 = SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/bindvalid/bjptBindValid.do";
		String param1="operatorCode=" + SysConfigInfo.PICC_USERNAME + "&checkOperaType=BJ_PT";
		String html = HttpsUtil.sendPost(urlString1, param1, piccSessionId).get("html");
		boolean f = false;
		if(!html.contains("302 Moved Temporarily")){//证明sessionId可用
			f = true;
		}
    	if(f){
    		logger.info("抓取机器人，【Picc sessionId有效】");
    	}else{//不可用的时候，重新获取保持会话sessionid
    		logger.info("抓取机器人，【Picc sessionId失效】");
    		Map<String, String> ticketMap = this.getTicket(SysConfigInfo.PICC_MAIN_URL + ":8888/casserver/login?service=http%3A%2F%2F10.134.136.48%3A80%2Fportal%2Findex.jsp", SysConfigInfo.PICC_USERNAME, SysConfigInfo.PICC_PWD1);
    		//访问解析出来的页面
    		Map<String, String> result1 = HttpsUtil.sendGet(ticketMap.get("ticket"), ticketMap.get("cookieValue"));
    		String url2 = SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/?calogin";
    		Map<String, String> result2 = HttpsUtil.sendGet(url2,  result1.get("cookieValue"));
    		String prpall = "prpall=" + SysConfigInfo.PICC_USERNAME;
    		String CASTGC = ticketMap.get("CASTGC");
    		String JSESSIONID = result1.get("cookieValue").replace("; path=/", "");
    		String BOCINS_prpall_Cookie = result2.get("cookieValue").replace("; path=/", "");
    		String a = prpall + "; " + CASTGC + "; " + JSESSIONID + "; " + BOCINS_prpall_Cookie;
    		String reUrl1 = SysConfigInfo.PICC_MAIN_URL + ":8888/casserver/login?service=http%3A%2F%2F10.134.136.48%3A8000%2Fprpall%2Findex.jsp%3Fcalogin";
    		Map<String, String> result3 = HttpsUtil.sendGetHttps(reUrl1,  a);
    		String ex = "<a href=\".*\">";
    		String reUrl2 = StringBaseUtils.getTextForMatcher(result3.get("html"), ex);
    		reUrl2 = reUrl2.replace("<a href=\"", "");
    		reUrl2 = reUrl2.replace("\">", "");
    		reUrl2 = reUrl2.replace("amp;", "");
    		String sessionId = result3.get("cookieValue").replace("; path=/", "") + "; " + BOCINS_prpall_Cookie;
    		HttpsUtil.sendGet(reUrl2, sessionId);
    		piccSessionId = sessionId;
        	piccSessionIdMap.put("picc_sessionId", piccSessionId);
        	logger.info("抓取机器人，【Picc sessionId重新获取成功】，piccSessionId：" + piccSessionId);
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

}
