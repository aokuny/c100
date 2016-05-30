package com.ihandy.quote_core.serverpage.picc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.DateBaseUtils;
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
public class QuoteGetCarInfoPage extends BasePage {

	public QuoteGetCarInfoPage(int type) {
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
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		if(StringUtils.isNotBlank(html)){//返回结果不为空的时候
			Map<String, Object> returnMap = new HashMap<>();
			//解析车辆信息
			JSONObject obj = JSONObject.parseObject(html);
			JSONObject carInfoObj = obj.getJSONArray("data").getJSONObject(0);//获取车辆信息
			String engineNo = carInfoObj.getString("engineNo");//发动机号
			String vin = carInfoObj.getString("rackNo");//vin
			String enrollDateTimestamp = carInfoObj.getJSONObject("enrollDate").getString("time");//注册日期的时间戳
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date  enrollDate = new Date(Long.parseLong(enrollDateTimestamp));
			String enrollDateStr = sdf.format(enrollDate);
			String seatCount = carInfoObj.getString("seatCount");//座位数目
			String modelCodeAlias = carInfoObj.getString("brandName1") + carInfoObj.getString("modelCode");//modelCodeAlias
			int useYears = DateBaseUtils.yearBetweenRound(enrollDate, new Date());//使用年份
			returnMap.put("engineNo", engineNo);
			returnMap.put("vin", vin);
			returnMap.put("enrollDate", enrollDateStr);
			returnMap.put("seatCount", seatCount);
			returnMap.put("modelCodeAlias", modelCodeAlias);
			returnMap.put("useYears", useYears);
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
