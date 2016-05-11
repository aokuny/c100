package com.ihandy.quote_common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBaseUtils {
	public static String getTextForMatcher(String resourceStr, String patternStr){
		Pattern patternOutput = Pattern.compile(patternStr);		
		Matcher mOutput = patternOutput.matcher(resourceStr);
		while(mOutput.find()){
			String outputStr = mOutput.group();
			return outputStr;
		}
		return null;
	}
}
