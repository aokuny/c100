package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.CacheConstant;
import com.ihandy.quote_core.utils.QuoteCalculateUtils;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 报价请求
 * @author liming
 *
 */
public class QuotePage extends BasePage{
	
	private static Logger logger = Logger.getLogger(QuotePage.class);
	
	public QuotePage(int type) {
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
		//对交强险报价JSON进行缓存
		Map<String, String> quoteResultJsonMap = CacheConstant.quoteResultJsonInfo.get(carNo);
		if(quoteResultJsonMap == null){
			quoteResultJsonMap = new HashMap<>();
		}
		quoteResultJsonMap.put("syxJson", result.get("html"));
		CacheConstant.quoteResultJsonInfo.put(carNo, quoteResultJsonMap);
		return result.get("html");
	}

	@Override
	public Response getResponse(String html, Request request) {
		Response response = new Response();
		//TODO 报价结果需要进行缓存
		if(StringUtils.isNotBlank(html)){
			//返回的json数据中  data 中 prpCitemKinds 为报价信息 amount 为保额
			JSONObject jsonObj = JSON.parseObject(html);
			JSONArray dataArray = jsonObj.getJSONArray("data");
			JSONArray prpCitemKindsArray = dataArray.getJSONObject(0).getJSONArray("prpCitemKinds");
			Map<String, Map<String, Double>> returnMap = new HashMap<>();
			Double bujimianRenyuan = 0D;//不计免赔 司机+乘客
			Double bujimianFujia = 0D;//不计免赔 划痕+涉水
			for(int i=0; i<prpCitemKindsArray.size(); i++){
				//对象中premium 为报价结果，kindName 险种名称
				JSONObject obj = prpCitemKindsArray.getJSONObject(i);
				Double premium = QuoteCalculateUtils.m2(obj.getDouble("premium"));//报价结果
				Double amount = obj.getDouble("amount");//保额
				String kindName = obj.getString("kindName");//险种名称
				logger.info("PICC API 解析，结果：" + kindName + "：报价信息->" + premium + "，保额->" + amount);
				boolean f = true;
				switch (kindName) {
				case "不计免赔率（车上人员责任险（司机））":
					bujimianRenyuan = QuoteCalculateUtils.add(bujimianRenyuan, premium);
					f = false;
					break;
				case "不计免赔率（车上人员责任险（乘客））":
					bujimianRenyuan = QuoteCalculateUtils.add(bujimianRenyuan, premium);
					f = false;
					break;
				case "不计免赔率（车身划痕损失险）":
					bujimianFujia = QuoteCalculateUtils.add(bujimianFujia, premium);
					f = false;
					break;
				case "不计免赔率（发动机特别损失险）":
					bujimianFujia = QuoteCalculateUtils.add(bujimianFujia, premium);
					f = false;
					break;
				default:
					kindName = SysConfigInfo.insuranceNameMap.get(kindName);
					break;
				}
				if(f){
					Map<String, Double> premiumMap = new HashMap<>();
					premiumMap.put("premium", premium);
					premiumMap.put("amount", amount);
					returnMap.put(kindName, premiumMap);
				}
			}
			Map<String, Double> premiumMapFujia = new HashMap<>();
			premiumMapFujia.put("premium", QuoteCalculateUtils.m2(bujimianFujia));
			premiumMapFujia.put("amount", 0D);
			returnMap.put("BuJiMianFuJia", premiumMapFujia);
			Map<String, Double> premiumMapRenyuan = new HashMap<>();
			premiumMapRenyuan.put("premium", QuoteCalculateUtils.m2(bujimianRenyuan));
			premiumMapRenyuan.put("amount", 0D);
			returnMap.put("BuJiMianRenYuan", premiumMapRenyuan);
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
