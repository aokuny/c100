package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 根据用户名字查询用户名称
 * @author liming
 *
 */
public class QuoteGetUserInfoByNamePage extends BasePage {

	public QuoteGetUserInfoByNamePage(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url  = request.getUrl();
		Map<String, String> paramMap = request.getRequestParam();
		String userName = paramMap.get("name");
		String sessionId = super.piccSessionId;
		String prpall = "prpall=" + SysConfigInfo.PICC_USERNAME;
		String CASTGC = piccSessionIdMap.get("CASTGC");
		String urlo5 = "http://10.134.136.48:8000/prpall/custom/customAmountQueryP.do?_identifyType=01&_insuredName=朱佳佳&_identifyNumber=&_insuredCode=";
		HttpsUtil.sendGet(urlo5, sessionId, null);
		String urlo6 = "http://10.134.136.48:8300/cif/customperson/customPersonStartIntf.do";
		Map<String, String> map6 = HttpsUtil.sendGet(urlo6, sessionId, null);
		String sessionIdO1 =  sessionId + "; " +  map6.get("cookieValue").toString().replace("; path=/", "");
		String sessionIdO = prpall + "; " + CASTGC + "; " + sessionIdO1;
		String urlo1 = "http://10.134.136.48:8300/cif/customperson/customPersonStartIntf.do?customerCName=%D6%EC%BC%D1%BC%D1&identifyType=01&identifyNumber=&syscode=prpall";
		HttpsUtil.sendGet(urlo1, sessionIdO1, null);
		String urlo2 = "https://10.134.136.48:8888/casserver/login?service=http%3A%2F%2F10.134.136.48%3A8300%2Fcif%2Fcustomperson%2FcustomPersonStartIntf.do%3FcustomerCName%3D%25D6%25EC%25BC%25D1%25BC%25D1%26identifyType%3D01%26identifyNumber%3D%26syscode%3Dprpall";
		Map<String, String> map2 = HttpsUtil.sendGetHttps(urlo2,  sessionIdO);
		String ex = "<a href=\".*\">";
		String urlo3 = StringBaseUtils.getTextForMatcher(map2.get("html"), ex);
		urlo3 = urlo3.replace("<a href=\"", "");
		urlo3 = urlo3.replace("\">", "");
		urlo3 = urlo3.replace("amp;", "");
		urlo3 = urlo3.replace("&#37;D6&#37;EC&#37;BC&#37;D1&#37;BC&#37;D1", "%D6%EC%BC%D1%BC%D1");
		HttpsUtil.sendGet(urlo3,  sessionIdO1, null);
		String param4 = "returnxs=&comCode=11010286&syscode=prpall&clientURL=&prpDcustomerPerson.customerCName=" + userName + "&prpDcustomerPerson.customerFullEName=&prpDcustomerPerson.identifyType=01&prpDcustomerPerson.identifyNumber=&prpDcustomerPerson.customerCode=";
		return HttpsUtil.sendPost(url, param4, sessionIdO1, "utf-8").get("html");
	}

	@Override
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
			JSONObject obj = JSONObject.parseObject(html);
			JSONArray infoArray = obj.getJSONArray("data");
			String identifyNumber = "239005108902252314";
			String customMobile = "13520030193";
			if(infoArray.size() != 0){
				JSONObject info = infoArray.getJSONObject(0);
				customMobile  = info.getString("customMobile");
				identifyNumber  = info.getString("identifyNumber");
			}
			returnMap.put("identifyNumber", identifyNumber);
			returnMap.put("customMobile", customMobile);
			response.setResponseMap(returnMap);
            response.setReturnCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
		}else{
			response.setResponseMap(null);
			response.setReturnCode(SysConfigInfo.ERROR404);
	        response.setErrMsg(SysConfigInfo.ERROR404MSG);
		}
		return response;
	}

	@Override
	public Response run(Request request) {
		String html = this.doRequest(request);
		return this.getResponse(html, request);
	}

}
