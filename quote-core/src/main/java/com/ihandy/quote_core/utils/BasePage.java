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
    	initPiccLogin();//初始化picc登录session
    	logger.info("抓取机器人，【初始化PICC登录session完成】");
    }
    
    /**
     * 初始化picc登录session
     */
    public void initPiccLogin(){
    	//TODO 以下代码未测试
    	piccSessionId = piccSessionIdMap.get("picc_sessionId");
    	//TODO  尝试sessionId是否可用
    	boolean f = false;
    	if(f){
    	}else{//不可用的时候，重新获取保持会话sessionid
    		String ticket = this.getTicket(SysConfigInfo.PICC_LOGIN1_URL, SysConfigInfo.PICC_USERNAME, SysConfigInfo.PICC_PWD1);
        	Map<String, String> map1 = HttpsUtil.sendGet(ticket, null);
        	Map<String, String> map2 = HttpsUtil.sendGet(SysConfigInfo.PICC_LOGIN2_URL + SysConfigInfo.PICC_USERNAME, map1.get("cookieValue"));
        	Map<String, String> map3 = HttpsUtil.sendGet(SysConfigInfo.PICC_LOGIN3_URL, map2.get("cookieValue"));
        	piccSessionId = map3.get("cookieValue");
        	piccSessionIdMap.put("picc_sessionId", piccSessionId);
    	}
    }
    
    /**
     * 获取PICC登录需要的ticket信息(已测试)
     */
    private String getTicket(String url, String username, String password){
		Map<String, String> result1 = HttpsUtil.sendGetHttps(url, null);
		String html = result1.get("html");
		String cookieValue = result1.get("cookieValue");
		cookieValue = cookieValue.replace("; path=/", "");
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
		logger.info("抓取机器人，【PICC获取ticket成功，使用密码：" + password + "，ticket：" + ticket + "】");
		return ticket;
    }


}
