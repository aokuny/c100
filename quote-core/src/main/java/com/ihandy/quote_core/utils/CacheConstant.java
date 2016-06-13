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
	
	public static Map<String, Map<String, String>> proposalNoInfo = new HashMap<>();//存放车辆投保单号
	
	static{//测试数据
		Map<String, Object> map = new HashMap<>();
		map.put("reCiPolicyNo", "PDAT20151102T000182528");//上一年交强险投保单号
		map.put("reBiPolicyNo", "PDAT20151102T000182528");//上一年商业险投保单号
		map.put("ciEndDate", "2016-06-25");//上一年交强险结束日期
		map.put("biEndDate", "2016-06-25");//上一年商业险结束日期  
		map.put("identifyNumber", "320684198411040279");//车主身份证
		map.put("mobile", "13520030193");//车主手机号
		renewalInfo.put("京P55M11", map);
		Map<String, Object> map1 = new HashMap<>();
		map1.put("reCiPolicyNo", "PDZA20151102T000162603");//上一年交强险投保单号
		map1.put("reBiPolicyNo", "PDAT20151102T000158991");//上一年商业险投保单号
		map1.put("ciEndDate", "2016-07-14");//上一年交强险结束日期
		map1.put("biEndDate", "2016-07-14");//上一年商业险结束日期  
		map1.put("identifyNumber", "110107196401121237");//车主身份证
		map1.put("mobile", "13520030193");//车主手机号
		//renewalInfo.put("京MH0501", map1);
		Map<String, Object> map2 = new HashMap<>();
		map2.put("reCiPolicyNo", "PDZA201511010000566671");//上一年交强险投保单号
		map2.put("reBiPolicyNo", "PDAA201511010000504540");//上一年商业险投保单号
		map2.put("ciEndDate", "2016-06-30");//上一年交强险结束日期
		map2.put("biEndDate", "2016-06-30");//上一年商业险结束日期  
		map2.put("identifyNumber", "110105195312190436");//车主身份证
		map2.put("mobile", "13520030192");//车主手机号
		renewalInfo.put("京KL3491", map2);
		Map<String, Object> map3 = new HashMap<>();
		map3.put("reCiPolicyNo", "PDZA201611010000525920");//上一年交强险投保单号
		map3.put("reBiPolicyNo", "PDAA201511010000504540");//上一年商业险投保单号
		map3.put("ciEndDate", "2016-06-30");//上一年交强险结束日期
		map3.put("biEndDate", "2016-06-30");//上一年商业险结束日期  
		map3.put("identifyNumber", "110105195312190436");//车主身份证
		map3.put("mobile", "13520030192");//车主手机号
		renewalInfo.put("", map3);
	}
	
}
