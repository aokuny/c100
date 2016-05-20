package com.ihandy.quote_common.httpUtil;


import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBaseUtils {
	private static Logger logger = Logger.getLogger(StringBaseUtils.class);

	public static String getTextForMatcher(String resourceStr, String patternStr){
		Pattern patternOutput = Pattern.compile(patternStr);		
		Matcher mOutput = patternOutput.matcher(resourceStr);
		while(mOutput.find()){
			String outputStr = mOutput.group();
			return outputStr;
		}
		return null;
	}

	public  static String  Map2GetParam(Map map){
		String param = null;
		if(null!= map){
			Set<String> key = map.keySet();
			for (Iterator it = key.iterator(); it.hasNext();) {
				String keyName = (String) it.next();
				param = param + keyName + " = "+ map.get(keyName)+"&";
			}
			param =  param.substring(0,param.length()-1);//删除最后一个&符号
		}else{
			logger.info("Get请求参数为 null，【HTTPGET PARAM IS NULL】");
		}

		return param;
	}



	public static Map<String, Object> parseJSON2Map(String jsonStr){
		Map<String, Object> map = new HashMap<String, Object>();
		//最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for(Object k : json.keySet()){
			Object v = json.get(k);
			//如果内层还是数组的话，继续解析
			if(v instanceof JSONArray){
				List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
				Iterator<JSONObject> it = ((JSONArray)v).iterator();
				while(it.hasNext()){
					JSONObject json2 = it.next();
					list.add(parseJSON2Map(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}


}
