package com.ihandy.quote_core.service.impl.picc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.serverpage.picc.QuoteJqxPage;
import com.ihandy.quote_core.serverpage.picc.QuotePage;
import com.ihandy.quote_core.utils.QuoteCalculateUtils;
import com.ihandy.quote_core.utils.SysConfigInfo;

/**
 * 人保报价线程
 * @author liming
 *
 */
public class QuoteThreadPicc extends Thread{
	
	private Map<String, String> quoteMap = new HashMap<>();//报价参数
	
	private static Logger logger = LoggerFactory.getLogger(QuoteThreadPicc.class);
	
	public QuoteThreadPicc() {
	}

	public QuoteThreadPicc(String name, Map<String, String> quoteMap) {
		super(name);
		this.quoteMap = quoteMap;
	}

	@Override
	public void run() {
		try {
			String LicenseNo = quoteMap.get("LicenseNo");
			Thread.sleep(2000);
			long startTime = System.currentTimeMillis();
			logger.info("人保  API接口，【报价开始】，车牌照：" + LicenseNo);
			JSONObject quoteResultJson = new JSONObject();//三部分信息：BusinessStatus 报价结果、StatusMessage 信息描述、Userinfo用户信息、Item 报价信息
			//封装Item信息
			JSONObject Item = new JSONObject();//报价结果的JSON对象
			Item.put("Source", quoteMap.get("IntentionCompany"));
			QuotePage quotePage = new QuotePage(1);
			//TODO request 封装
			Response quoteResponse = quotePage.run(null);
			if(quoteResponse.getReturnCode() == SysConfigInfo.ERROR404){
				//TODO 报价失败
				return;
			}else{
				Double total = 0D;
				Map<String, Map<String, Double>> quoteMap = quoteResponse.getResponseMap();
				for(String key : quoteMap.keySet()){
					Map<String, Double> map = quoteMap.get(key);
					JSONObject obj = new JSONObject();//每个险种的报价结果JSON对象
					obj.put("BaoE", map.get("amount"));
					obj.put("BaoFei", map.get("premium"));
					total = total + map.get("premium");
					Item.put(key, obj);
				}
				Item.put("BizTotal", QuoteCalculateUtils.m2(total));
			}
			//查看是否报价交强险
			if("1".equals(quoteMap.get("ForceTax"))){
				QuoteJqxPage quoteJqxPage = new QuoteJqxPage(1);
				//TODO request 封装
				Response quoteJqxResponse = quoteJqxPage.run(null);
				if(quoteJqxResponse.getReturnCode() == SysConfigInfo.ERROR404){
					//TODO 报价失败
					return;
				}else{
					Map<String, Double> quoteJqxMap = quoteJqxResponse.getResponseMap();
					Item.put("ForceTotal", quoteJqxMap.get("netPremium"));
					Item.put("TaxTotal", quoteJqxMap.get("taxTotal"));
				}
			}
			Item.put("QuoteStatus", "1");
			Item.put("QuoteResult", "报价成功");
			quoteResultJson.put("Item", Item);
			quoteResultJson.put("BusinessStatus", "1");
			quoteResultJson.put("StatusMessage", "报价成功");
			//封装Userinfo信息
			JSONObject Userinfo = new JSONObject();
			Userinfo.put("LinenseNo", LicenseNo);
			Userinfo.put("ForceExpireDate", "");
			Userinfo.put("BusinessExpireDate", "");
			Userinfo.put("BusinessStartDate", "");
			Userinfo.put("ForceStartDate", "");
			quoteResultJson.put("Userinfo", Userinfo);
			//TODO 报价结果放入缓存
			System.err.println("----------------------------" + quoteResultJson);
			logger.info("人保   API接口，【报价结束】，使用时间：" + ((System.currentTimeMillis() - startTime)/1000) + "S");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
