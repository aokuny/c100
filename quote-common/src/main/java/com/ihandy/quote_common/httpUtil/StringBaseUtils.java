package com.ihandy.quote_common.httpUtil;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
}
