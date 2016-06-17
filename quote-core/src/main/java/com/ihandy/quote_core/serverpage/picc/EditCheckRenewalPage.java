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
 * 查看续保信息是否可用
 * @author liming
 *
 */
public class EditCheckRenewalPage extends BasePage {

	public EditCheckRenewalPage(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url  = request.getUrl();
		return HttpsUtil.sendPost(url, null, super.piccSessionId, "").get("html");
	}

	@Override
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
			JSONObject obj = JSONObject.parseObject(html);
			JSONObject dataObj = obj.getJSONArray("data").getJSONObject(0);
			returnMap.put("proposalNo", dataObj.get("proposalNo"));
			returnMap.put("premium", dataObj.get("premium"));
			returnMap.put("riskCode", dataObj.get("riskCode"));
			returnMap.put("renewalFlag", dataObj.get("renewalFlag"));
			returnMap.put("nowComCode", dataObj.get("nowComCode"));
			returnMap.put("oldComCode", dataObj.get("oldComCode"));
			returnMap.put("othFlag", dataObj.get("othFlag"));
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
