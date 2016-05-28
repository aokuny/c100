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
 * 察看是否续保
 * @author liming
 *
 */
public class IsXuBaoPage extends BasePage {

	public IsXuBaoPage(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url  = request.getUrl();
		Map<String, String> paramMap = request.getRequestParam();
		String carNo = paramMap.get("carNo");
		try {
			carNo =  java.net.URLEncoder.encode(carNo,   "gb2312");
		} catch (Exception e) {
		}
		String param = "licenseNo=" + carNo + "&licenseFlag=1&licenseType=02";
		return HttpsUtil.sendPost(url, param, super.piccSessionId, "").get("html");
	}

	@Override
	public Response getResponse(String html) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
			JSONObject obj = JSONObject.parseObject(html);
			String proposalNo = obj.getJSONArray("data").getJSONObject(0).getString("proposalNo");
			returnMap.put("proposalNo", proposalNo);//保单号
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
		return this.getResponse(html);
	}

}
