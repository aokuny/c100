package com.ihandy.quote_common.httpUtil;


import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		String param = "";
		if(null!= map){
			Set<String> key = map.keySet();
			for (Iterator it = key.iterator(); it.hasNext();) {
				String keyName = (String) it.next();
				Object keyValue = map.get(keyName);
				if(keyValue instanceof Map){
					Map map2 = (Map) keyValue;
					Set<String> key2 = map2.keySet();
					for (Iterator it2 = key2.iterator(); it2.hasNext();) {
						String keyName2 = (String) it2.next();
						String keyValue2 = map2.get(keyName2).toString();
						param = param +(keyName+"."+keyName2) + "="+keyValue2+"&";
					}
				}else{
					keyValue = keyValue.toString();
					param = param + keyName + "="+keyValue+"&";
				}

			}
			if(!param.equals("")){
				param =  param.substring(0,param.length()-1);//删除最后一个&符号
			}

		}else{
			logger.info("Get请求参数为 null，【HTTPGET PARAM IS NULL】");
		}


		return param;
	}

	public  static String  Map2StringURLEncoder(Map map){
		String param = "";
		if(null!= map){
			Set<String> key = map.keySet();
			for (Iterator it = key.iterator(); it.hasNext();) {
				String keyName = (String) it.next();
				String keyValue = map.get(keyName).toString();
				try{
					keyName = java.net.URLEncoder.encode(keyName, "gbk");
					keyValue = java.net.URLEncoder.encode(keyValue, "gbk");
				}
				catch(Exception e){

				}
				param = param + keyName + "="+keyValue+"&";
			}
			if(!param.equals("")){
				param =  param.substring(0,param.length()-1);//删除最后一个&符号
			}

		}else{
			logger.info("Map ===null");
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
	public  static  double forDight(double num, int count){//保留count 位double
		double dight=0;
		dight = Math.round(num*Math.pow(10,count))/Math.pow(10,count);
		return dight;
	}
	public static String combineStringByRightOrder(String right,String error){
		String newParams ="";
		String[] paramArr1=right.split("&");
		String[] paramArr2=error.split("&");

		for(int j=0;j<paramArr1.length;j++){
			for(int i=0;i<paramArr2.length;i++){
				if(paramArr1[j].split("=")[0].equals(paramArr2[i].split("=")[0])){//
					try{
						newParams = newParams+paramArr2[i].split("=")[0]+"="+ paramArr2[i].split("=")[1]+"&"  ;
					}catch(Exception e){
						newParams = newParams+paramArr2[i].split("=")[0]+"=&" ;
					}
					break;
				}
			}//for end
		}
		newParams = newParams.substring(0,newParams.length()-1);
		return newParams;
	}

	public static void compareErrorStringLess(String right,String error){
		String[] paramArr1=right.split("&");
		String[] paramArr2=error.split("&");

		for(int i=0;i<paramArr1.length;i++){
			String rightStr = paramArr1[i].split("=")[0]+"=";
			try{
				rightStr = rightStr+paramArr1[i].split("=")[1];

			}catch(Exception e){

			}
			rightStr = rightStr.trim();
			if(error.contains(rightStr)){//包含
			}else{
				try{
					System.out.println("ErrorString  Less   "+paramArr1[i].split("=")[0] +" value="+paramArr1[i].split("=")[1]);
				}catch(Exception e){
					System.out.println("ErrorString  Less   "+paramArr1[i].split("=")[0] +" value=null");
				}
			}//else end
		}//for end
	}

	public static void compareErrorStringMore(String right,String error){
		String[] paramArr1=right.split("&");
		String[] paramArr2=error.split("&");
		// minus(paramArr1,paramArr2);
		for(int i=0;i<paramArr2.length;i++){
			String errorStr = paramArr2[i].split("=")[0]+"=";
			try{
				errorStr = errorStr+paramArr2[i].split("=")[1];
			}catch(Exception e){
			}
			errorStr = errorStr.trim();
			if(right.contains(errorStr)){//包含
			}else{
				try{
					System.out.println("ErrorString  More   "+paramArr2[i].split("=")[0] +" value="+paramArr1[i].split("=")[1]);
				}catch(Exception e){
					System.out.println("ErrorString  More   "+paramArr2[i].split("=")[0] +" value=null");
				}
			}//else end
		}//for end
	}

	//求两个数组的差集
	public static String[] minus(String[] arr1, String[] arr2) {
		LinkedList<String> list = new LinkedList<String>();
		LinkedList<String> history = new LinkedList<String>();
		String[] longerArr = arr1;
		String[] shorterArr = arr2;
		//找出较长的数组来减较短的数组
		if (arr1.length > arr2.length) {
			longerArr = arr2;
			shorterArr = arr1;
		}
		for (String str : longerArr) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		for (String str : shorterArr) {
			if (list.contains(str)) {
				history.add(str);
				list.remove(str);
			} else {
				if (!history.contains(str)) {
					list.add(str);
				}
			}
		}
		String[] result = {};
		return list.toArray(result);
	}

	//string 转两位小数double
	public static String  String2Double(String num){
		String returnNum="";
		try {
			if (!num.equals("0")) {
				String[] arr1 = num.split("\\.");
				returnNum = arr1[0] + "." + arr1[1].substring(0, 2);
			} else {
				returnNum = "0";
			}
		}catch (Exception e){
			returnNum = "0";
		}
		return returnNum;
	}


	public static int compareDate(String DATE1, String DATE2) {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				// System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				// System.out.println("dt1在dt2后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	public static String addParam(String startString,Map map){
		String param = startString;
		param = param+Map2GetParam(map);

		return param;
	}

	 public static String readFile(String filePath)
	{
           // 读取txt内容为字符串
		StringBuffer txtContent = new StringBuffer();
         // 每次读取的byte数
		byte[] b = new byte[8 * 1024];
		InputStream in = null;
		try
		{
           // 文件输入流
			in = new FileInputStream(filePath);
			while (in.read(b) != -1)
			{
// 字符串拼接
				txtContent.append(new String(b));
			}
// 关闭流
			in.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return txtContent.toString();
	}
}
