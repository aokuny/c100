package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 获取车辆信息：第二部分
 * @author liming
 *
 */
public class QuoteGetCarInfoOtherPage extends BasePage {

	public QuoteGetCarInfoOtherPage(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url  = request.getUrl();
		Map<String, String> paramMap = request.getRequestParam();
		String param = paramMap.get("param");
		return HttpsUtil.sendPost(url, param, super.piccSessionId, "utf-8").get("html");
	}

	@Override
	public Response getResponse(String html) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
			JSONObject obj = JSONObject.parseObject(html);
			JSONObject carInfObj = obj.getJSONArray("data").getJSONObject(0);
			String modelCode = carInfObj.getString("modelCode");//modelCode
			String purchasePrice = carInfObj.getString("replaceMentValue");//市场售价
			String brandName = carInfObj.getString("modelName");//品牌型号
			String wholeWeight = carInfObj.getString("wholeWeight");//吨位
			String owner = carInfObj.getString("owner");//车主姓名
			String pmQueryNo = carInfObj.getJSONObject("id").getString("pmQueryNo");
			returnMap.put("modelCode", modelCode);
			returnMap.put("purchasePrice", purchasePrice);
			returnMap.put("brandName", brandName);
			if(StringUtils.isBlank(wholeWeight)){
				returnMap.put("wholeWeight", "0.00");
			}else{
				returnMap.put("wholeWeight", wholeWeight);
			}
			returnMap.put("owner", owner);
			returnMap.put("pmQueryNo", pmQueryNo);
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
