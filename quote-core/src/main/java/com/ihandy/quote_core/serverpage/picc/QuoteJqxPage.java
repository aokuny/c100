package com.ihandy.quote_core.serverpage.picc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.CacheConstant;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 交强险请求
 * @author liming
 *
 */
public class QuoteJqxPage extends BasePage{
	
	private static Logger logger = Logger.getLogger(QuoteJqxPage.class);
	
	public QuoteJqxPage(int type) {
		super(type);
	}

	@Override
	public String doRequest(Request request) {
		String url = request.getUrl();
		Map<String, String> param = request.getRequestParam();
		String paramStr = param.get("param");
		String carNo = param.get("carNo");
		//发送http请求
		Map<String, String> result = HttpsUtil.sendPost(url, paramStr, super.piccSessionId, "utf-8");
		String html = result.get("html");
		if(html.contains("该车已经在其他公司投保了同类型的险种") || html.contains("该车已经在本公司投保了同类型的险种")){//解析出日期，并重新报价
			html = html.split("\"errMessage\":\"")[1].split("\"errorMessageVo\":")[0];
			String timeStr = html.split("\\[")[1].split("\\]")[0];
			String[] timeArray = timeStr.split("-");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String ciStartDateStr = "";
			String ciEndDateStr = "";
			try {
				Date ciEndDateOld = sdf.parse(timeArray[1]);//上一年交强险结束日期
				Date ciStartDate = DateUtils.addDays(ciEndDateOld, 1);//开始时间为上一年结束时间 +1 天
				Date ciEndDate = DateUtils.addYears(ciEndDateOld, 1);//结束时间为上一年结束时间 + 1年
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				ciStartDateStr = sdf.format(ciStartDate);
				ciEndDateStr = sdf.format(ciEndDate);
				//存入缓存
				 Map<String, Object> jqxDateInfo = CacheConstant.lastJqxEndDateInfo.get(carNo);
				 if(jqxDateInfo == null){
					 jqxDateInfo = new HashMap<>();
				 }
				 jqxDateInfo.put("ciStartDateStr", ciStartDateStr);
				 jqxDateInfo.put("ciEndDateStr", ciEndDateStr);
				 CacheConstant.lastJqxEndDateInfo.put(carNo, jqxDateInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//替换
			String[] paramArray = paramStr.split("&");
			for(int i=0; i<paramArray.length; i++){
				if(paramArray[i].contains("ciStartDate=")){
					paramStr = paramStr.replace(paramArray[i], "ciStartDate=" + ciStartDateStr);
					logger.info("替换成功：" + paramArray[i] + "->ciStartDate=" + ciStartDateStr);
				}
				if(paramArray[i].contains("ciEndDate=")){
					paramStr = paramStr.replace(paramArray[i], "ciEndDate=" + ciEndDateStr);
					logger.info("替换成功：" + paramArray[i] + "->ciEndDate=" + ciEndDateStr);
				}
				if(paramArray[i].contains("OLD_STARTDATE_CI=")){
					paramStr = paramStr.replace(paramArray[i], "OLD_STARTDATE_CI=" + ciStartDateStr);
					logger.info("替换成功：" + paramArray[i] + "->OLD_STARTDATE_CI=" + ciStartDateStr);
				}
				if(paramArray[i].contains("OLD_ENDDATE_CI=")){
					paramStr = paramStr.replace(paramArray[i], "OLD_ENDDATE_CI=" + ciEndDateStr);
					logger.info("替换成功：" + paramArray[i] + "->OLD_ENDDATE_CI=" + ciEndDateStr);
				}
				if(paramArray[i].contains("prpCmainCI.startDate=")){
					paramStr = paramStr.replace(paramArray[i], "prpCmainCI.startDate=" + ciStartDateStr);
					logger.info("替换成功：" + paramArray[i] + "->prpCmainCI.startDate=" + ciStartDateStr);
				}
				if(paramArray[i].contains("prpCmainCI.endDate=")){
					paramStr = paramStr.replace(paramArray[i], "prpCmainCI.endDate=" + ciEndDateStr);
					logger.info("替换成功：" + paramArray[i] + "->prpCmainCI.endDate=" + ciEndDateStr);
				}
			}
		}
		//重新发送请求
		html = HttpsUtil.sendPost(url, paramStr, super.piccSessionId, "utf-8").get("html");
		//对交强险报价JSON进行缓存
		Map<String, String> quoteResultJsonMap = CacheConstant.quoteResultJsonInfo.get(carNo);
		if(quoteResultJsonMap == null){
			quoteResultJsonMap = new HashMap<>();
		}
		quoteResultJsonMap.put("jqxJson", html);
		CacheConstant.quoteResultJsonInfo.put(carNo, quoteResultJsonMap);
		return html;
	}

	@Override
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		if(StringUtils.isNoneBlank(html)){
			// data ciInsureDemand netPremium 字段
			JSONObject jsonObj = JSONObject.parseObject(html);
			JSONArray dataArray = jsonObj.getJSONArray("data");
			String errMessage =  dataArray.getJSONObject(0).getString("errMessage");
			if(StringUtils.isNoneBlank(errMessage)){
				response.setResponseMap(null);
				response.setReturnCode(SysConfigInfo.ERROR404);
		        response.setErrMsg(errMessage);
		        return response;
			}
			JSONObject ciInsureDemandObj = dataArray.getJSONObject(0).getJSONObject("ciInsureDemand");
			Double netPremium = ciInsureDemandObj.getDouble("netPremium");
			Double taxTotal = ciInsureDemandObj.getDouble("taxTotal");
			logger.info("PICC API 解析，交强险：报价信息->" + netPremium);
			logger.info("PICC API 解析，车船税：报价信息->" + taxTotal);
			Map<String, Double> returnMap = new HashMap<>();
			returnMap.put("netPremium", netPremium);
			if(taxTotal == null){
				returnMap.put("taxTotal", 0D);
			}else{
				returnMap.put("taxTotal", taxTotal);
			}
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
		String html = null;
		try {
			html = this.doRequest(request);
		} catch (Exception e) {
			logger.error("PICC API 【HTTP请求出错】" + e.getMessage() + "，url：" + request.getUrl());
		}
		return this.getResponse(html, request);
	}
	
}
