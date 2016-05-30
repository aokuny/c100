package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 获取车辆信息：第一部分
 * @author liming
 *
 */
public class QuoteBefore2Page extends BasePage {

	public QuoteBefore2Page(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url  = request.getUrl();
		Map<String, String> paramMap = request.getRequestParam();
		String operatorCode = paramMap.get("operatorCode");
		String comCode = paramMap.get("comCode");
		String agentCode = paramMap.get("agentCode");
		String usbkey = paramMap.get("usbkey");
		String param = "operatorCode=" + operatorCode + "&comCode=" + comCode + "&agentCode=" + agentCode + "&usbkey=" + usbkey + "&nowtime=Wed%20May%2018%2015%3A43%3A01%20UTC%2B0800%202016";;
		return HttpsUtil.sendPost(url, param, super.piccSessionId, "").get("html");
	}

	@Override
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
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
