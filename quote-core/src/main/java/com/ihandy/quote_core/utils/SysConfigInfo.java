package com.ihandy.quote_core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by fengwen on 2016/5/8.
 */
public class SysConfigInfo {
    private static Logger logger = LoggerFactory.getLogger(SysConfigInfo.class);

    private static Properties properties;

    /** ====================================================人保begin====================*/
    /** 人保域名*/
     public static String PICC_DOMIAN ;

    /**首页面*/
     public  static String  PICC_INDEX;
    /**承保页面 */
     public static String  PICC_CALOGIN;
     /**车商渠道专用出单页面 */
     public static String PICC_QUICKPROPOSAL ;
     /**续保对话框 */
     public static String  PICC_EDITRENEWALSEARCH ;
     /**查看保单数据源 */
     public static String  PICC_SELECTRENEWAL;
     /**浏览保单 */
     public static String  PICC_BROWSEPOLICYNO;
     /**车辆信息 */
     public static String  PICC_CARTAB;
     /**关系人信息 */
     public static String  PICC_INSUREDTAB;
     /**保险责任 */
     public static String  PICC_KINDTAB;
     /**查询理赔信息 */
     public static String  PICC_QUERYCLAIMSMSG;

    
    /**----------------------------- 人保URL ----------------------------------*/
    /** PICC人保业务登录 */
    public static String PICC_MAIN_URL;
    /** PICC用户名 */
    public static String PICC_USERNAME;
    /** PICC密码1 */
    public static String PICC_PWD1;
    /** PICC密码2 */
    public static String PICC_PWD2;

    /**----------------------------- 返回错误信息 ----------------------------------*/
    public static Integer ERROR404;
    public static String ERROR404MSG;
    public static Integer SUCCESS200;
    public static String  SUCCESS200MSG;

    /**----------------------------- 返回保险公司信息 ----------------------------------*/
    public static String  PICC_NAME;
    
    /** 玻璃险值 */
    public static List<String> boliValueList = new ArrayList<String>();
    /** 不计免赔 */
    public static List<String> chooseValueList = new ArrayList<String>();
    /** 划痕 */
    public static List<String> huahenValueList = new ArrayList<String>();
    /** 司机 */
    public static List<String> renyuanValueList = new ArrayList<String>();
    /** 车损 */
    public static List<String> chesunValueList = new ArrayList<String>();
    /** 险种名称映射关系 */
    public static Map<String, String> insuranceNameMap = new HashMap<>();

    static {
        try {
            properties = new Properties();

            //FileInputStream fis = new FileInputStream(classpath:);// 属性文件输入流
            InputStream is = SysConfigInfo.class.getClassLoader().getResource("url.properties").openStream();
            properties.load(is);

            PICC_DOMIAN = getString("picc_domain", "");

            PICC_INDEX = getString("picc_index", "");

            PICC_CALOGIN = getString("picc_calogin", "");

            PICC_QUICKPROPOSAL = getString("picc_quickProposal", "");

            PICC_EDITRENEWALSEARCH = getString("picc_editRenewalSearch", "");

            PICC_SELECTRENEWAL = getString("picc_selectRenewal", "");

            PICC_BROWSEPOLICYNO = getString("picc_browsePolicyNo", "");

            PICC_CARTAB=getString("picc_carTab","");

            PICC_INSUREDTAB = getString("picc_insuredTab", "");

            PICC_KINDTAB = getString("picc_kindTab", "");

            PICC_QUERYCLAIMSMSG = getString("picc_queryClaimsMsg", "");

            PICC_USERNAME = getString("picc_username", "");
            PICC_PWD1 = getString("picc_pwd1", "");
            PICC_PWD2 = getString("picc_pwd2", "");
            PICC_MAIN_URL = getString("picc_main_url","");

            //错误信息获取
            ERROR404 = getInt("picc_error404",404);
            ERROR404MSG =  getString("picc_error404msg","");
            SUCCESS200 = getInt("picc_success200",200);
            SUCCESS200MSG =  getString("picc_success200msg","");

            //保险公司信息
            PICC_NAME =getString("picc_name","PICC");

            
            //险种信息验证
            boliValueList.add("0");
            boliValueList.add("1");
            boliValueList.add("2");
            chooseValueList.add("0");
            chooseValueList.add("1");
            huahenValueList.add("0");
            huahenValueList.add("2000");
            huahenValueList.add("5000");
            huahenValueList.add("10000");
            huahenValueList.add("20000");
            renyuanValueList.add("0");
            renyuanValueList.add("10000");
            renyuanValueList.add("20000");
            renyuanValueList.add("30000");
            renyuanValueList.add("40000");
            renyuanValueList.add("50000");
            renyuanValueList.add("100000");
            renyuanValueList.add("200000");
            chesunValueList.add("0");
            chesunValueList.add("50000");
            chesunValueList.add("100000");
            chesunValueList.add("150000");
            chesunValueList.add("200000");
            chesunValueList.add("300000");
            chesunValueList.add("500000");
            chesunValueList.add("1000000");
            chesunValueList.add("1500000");
            //PICC 险种map
            insuranceNameMap.put("机动车损失保险", "CheSun");
            insuranceNameMap.put("盗抢险", "DaoQiang");
            insuranceNameMap.put("第三者责任保险", "SanZhe");
            insuranceNameMap.put("车上人员责任险（司机）", "SiJi");
            insuranceNameMap.put("车上人员责任险（乘客）", "ChengKe");
            insuranceNameMap.put("车身划痕损失险条款", "HuaHen");
            insuranceNameMap.put("玻璃单独破碎险", "BoLi");
            insuranceNameMap.put("自燃损失险条款", "ZiRan");
            insuranceNameMap.put("发动机特别损失险条款", "SheShui");
            insuranceNameMap.put("不计免赔率（车辆损失险）", "BuJiMianCheSun");
            insuranceNameMap.put("不计免赔率（三者险）", "BuJiMianSanZhe");
            insuranceNameMap.put("不计免赔率（机动车盗抢险）", "BuJiMianDaoQiang");
            insuranceNameMap.put("不计免赔率（车身划痕损失险）", "BuJiMianFuJia");
            insuranceNameMap.put("不计免赔率（车上人员责任险（司机））", "BuJiMianRenYuan");
        } catch (Exception e) {
            logger.error("加载属性文件失败",e);
        }
    }

    private static String getString(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (null != value && !"".endsWith(value)) {
            return value.trim();
        } else {
            return defaultValue;
        }
    }

    private static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (null != value) {
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }



}
