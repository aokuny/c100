package com.ihandy.quote_core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存的常用类
 * @author liming
 *
 */
public class CacheConstant {
	
	public static Map<String, Map<String, Object>> uploadInsurInfo = new HashMap<>();//用户上传险种信息缓存
	
	public static Map<String, Map<String, Object>>  renewalInfo = new HashMap<>();//车辆的续保信息  
	
	public static Map<String, Map<String, Object>> quoteResultInfo = new HashMap<>();//报价结果信息缓存
	
	public static Map<String, Map<String, Object>> lastJqxEndDateInfo = new HashMap<>();//车辆上一年交强险结束日期缓存
	
	public static Map<String, Map<String, String>> quoteResultJsonInfo = new HashMap<>();//报价结果的JSON存放
	
	static{//测试数据
		Map<String, Object> map = new HashMap<>();
		map.put("reCiPolicyNo", "PDAT20151102T000182528");//上一年交强险投保单号
		map.put("reBiPolicyNo", "PDAT20151102T000182528");//上一年商业险投保单号
		map.put("ciEndDate", "2016-06-25");//上一年交强险结束日期
		map.put("biEndDate", "2016-06-25");//上一年商业险结束日期  
		map.put("identifyNumber", "320684198411040279");//车主身份证
		map.put("mobile", "13520030193");//车主手机号
		renewalInfo.put("京P55M11", map);
	}
	
}
