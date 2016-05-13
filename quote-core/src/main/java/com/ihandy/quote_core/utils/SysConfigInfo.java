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
    /** PICC登录URL */
    public static String PICC_LOGIN1_URL;
    /** PICC用户名 */
    public static String PICC_USERNAME;
    /** PICC密码1 */
    public static String PICC_PWD1;
    /** PICC密码2 */
    public static String PICC_PWD2;
    public static String PICC_LOGIN2_URL;
    public static String PICC_LOGIN3_URL;

    /**----------------------------- 返回错误信息 ----------------------------------*/
    public static Integer ERROR404;
    public static String ERROR404MSG;
    public static Integer SUCCESS200;
    public static String  SUCCESS200MSG;

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



            
            PICC_LOGIN1_URL = getString("picc_login1_url", "");
            PICC_USERNAME = getString("picc_username", "");
            PICC_PWD1 = getString("picc_pwd1", "");
            PICC_PWD2 = getString("picc_pwd2", "");
            PICC_LOGIN2_URL = getString("picc_login2_url","");
            PICC_LOGIN3_URL = getString("picc_login3_url","");

            //错误信息获取
            ERROR404 = getInt("picc_error404",404);
            ERROR404MSG =  getString("picc_error404msg","");
            SUCCESS200 = getInt("picc_error404",200);
            SUCCESS200MSG =  getString("picc_error404msg","");

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
