package com.ihandy.quote_core.utils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 报价工具类：负责编码、计算
 * @author liming
 *
 */
public class QuoteCalculateUtils {
	
	private static Log logger = LogFactory.getLog(QuoteCalculateUtils.class);
	
	private static final int DEF_DIV_SCALE = 10;  
	
	/**
	 * 转码字符串
	 * @param str
	 * @return
	 */
	public static String encodeStr(String str){
		try {
			return URLEncoder.encode(str, "GBK");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 计算结果（四舍五入，保留两位有效数字）
	 * @param expr
	 * @return
	 */
	public static String calculationResult(String expr){
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("js");
			String result = engine.eval(expr).toString();
	        BigDecimal bd = new BigDecimal(Double.parseDouble(result));  
	        Double r = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	        return getPricePointTwo(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
     * DecimalFormat转换最简便 ,保留两位有数字
     */  
    public static Double m2(Double f) { 
    	if(f== 0){
			return 0D;
		}
    	DecimalFormat df = new DecimalFormat("######0.00");     
        return Double.parseDouble(df.format(f));
    }  
	
	/**
	 * 检查小数点后几位，如果不够两位补全
	 * @param r
	 * @return
	 */
	public static String getPricePointTwo(Double r){
		if(r == 0){
			return "0";
		}
		String rStr = r.toString();
		if(rStr.contains(".")){
        	String[] rStrs = rStr.split("\\.");
        	if(rStrs.length == 2){
        		if(rStrs[1].length() == 1){
        			rStr = rStr + "0";
        		}
        	}
        } else {
        	rStr = rStr + ".00";
        }
		return rStr;
	}
	
	/** 
     * 提供精确的加法运算。 
     *  
     * @param v1 
     *            被加数 
     * @param values 
     *            加数 
     * @return 多个参数的和 
     */  
    public static double add(double v1,double ...values){
    	BigDecimal b1 = new BigDecimal(v1);  
    	for(double value:values){
    		BigDecimal b = new BigDecimal(value);
    		b1 = new BigDecimal(b1.add(b).doubleValue());
    	}
    	return b1.doubleValue();
    }
	
	/** 
     * 提供精确的加法运算。 
     *  
     * @param v1 
     *            被加数 
     * @param v2 
     *            加数 
     * @return 两个参数的和 
     */  
  
    public static double add(double v1, double v2) {  
        BigDecimal b1 = new BigDecimal(Double.toString(v1));  
        BigDecimal b2 = new BigDecimal(Double.toString(v2));  
        return b1.add(b2).doubleValue();  
    }
    
    /** 
     * 提供精确的减法运算。 
     *  
     * @param v1 
     *            被减数 
     * @param v2 
     *            减数 
     * @return 两个参数的差 
     */  
  
    public static double sub(double v1, double v2) {  
        BigDecimal b1 = new BigDecimal(Double.toString(v1));  
        BigDecimal b2 = new BigDecimal(Double.toString(v2));  
        return b1.subtract(b2).doubleValue();  
    } 
    
	/**
	 * 计算折损价格
	 * 
	 * @param insuranceBeginDateString
	 *            起保时间
	 * @param firstRegisterDateString
	 *            首次登记日期
	 * @param insureAmount
	 *            新车购置价格
	 * @param seat
	 *            座位数
	 * @return
	 */
	public static Map<String, String> calculateAllAmount(Date insuranceBeginDate, Date firstRegisterDate, String insureAmount, int seat) {
		Map<String, String> resultMap = new HashMap<>();
		Double result = 0.0;

		Calendar cal = Calendar.getInstance();
		cal.setTime(insuranceBeginDate);
		int insuranceBeginDateYear = cal.get(Calendar.YEAR);// 获取年份
		int insuranceBeginDateMonth = (cal.get(Calendar.MONTH) + 1);// 获取月份
		int insuranceBeginDateDay = cal.get(Calendar.DATE);// 获取日

		cal.setTime(firstRegisterDate);
		int firstRegisterDateYear = cal.get(Calendar.YEAR);// 获取年份
		int firstRegisterDateMonth = (cal.get(Calendar.MONTH) + 1);// 获取月份
		int firstRegisterDateDay = cal.get(Calendar.DATE);// 获取日
		
		//计算天数
		long userDay = (insuranceBeginDate.getTime() - firstRegisterDate.getTime())/(1000*3600*24);  
		resultMap.put("userDay", String.valueOf(userDay));
		
		// 计算车辆使用月份
		int userMonth = (insuranceBeginDateYear - firstRegisterDateYear) * 12 + (insuranceBeginDateMonth - firstRegisterDateMonth);
		if (firstRegisterDateDay > insuranceBeginDateDay) {
			userMonth = userMonth - 1;
		}
		if (userMonth < 0) {
			userMonth = 0;
		}
		//设置使用月份
		resultMap.put("userMonth", String.valueOf(userMonth));
		if (userMonth > 0) {
			Double ra = 0.006;
			if (seat > 9) {
				ra = 0.009;
			}
			Double rate = (1 - (userMonth * ra));
			resultMap.put("rate", String.valueOf(rate));
			result = Double.parseDouble(insureAmount) * rate;
		} else {
			resultMap.put("rate", "1");
			result = Double.parseDouble(insureAmount);
		}
		resultMap.put("cost", String.valueOf(result));
		return resultMap;
	}
	
	/** 
	* * 两个Double数相乘 * 
	*  
	* @param v1 * 
	* @param v2 * 
	* @return Double 
	*/  
	public static Double mul(Double v1, Double v2) {  
	   BigDecimal b1 = new BigDecimal(v1.toString());  
	   BigDecimal b2 = new BigDecimal(v2.toString());  
	   return new Double(b1.multiply(b2).doubleValue());  
	}  
	  
	/** 
	* * 两个Double数相除 * 
	*  
	* @param v1 * 
	* @param v2 * 
	* @return Double 
	*/  
	public static Double div(Double v1, Double v2) {  
	   BigDecimal b1 = new BigDecimal(v1.toString());  
	   BigDecimal b2 = new BigDecimal(v2.toString());  
	   return new Double(b1.divide(b2, DEF_DIV_SCALE, BigDecimal.ROUND_HALF_UP)  
	     .doubleValue());  
	}  
	  
	/** 
	* * 两个Double数相除，并保留scale位小数 * 
	*  
	* @param v1 * 
	* @param v2 * 
	* @param scale * 
	* @return Double 
	*/  
	public static Double div(Double v1, Double v2, int scale) {  
	   if (scale < 0) {  
	    throw new IllegalArgumentException(  
	      "The scale must be a positive integer or zero");  
	   }  
	   BigDecimal b1 = new BigDecimal(v1.toString());  
	   BigDecimal b2 = new BigDecimal(v2.toString());  
	   return new Double(b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue());  
	}  
	
	/**
	 * 去除特殊字符
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		try {
			// 只允许字母和数字
			// String regEx = "[^a-zA-Z0-9]";
			// 清除掉所有特殊字符
			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？  ]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(str);
			return m.replaceAll("").trim();
		} catch (Exception e) {
			logger.error("字符处理失败【" + str + "】，" + e.getMessage());
		}
		return null;
	}
}
