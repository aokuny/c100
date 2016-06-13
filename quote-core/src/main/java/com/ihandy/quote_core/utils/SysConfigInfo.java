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


    /**计算辅助核保 */
    public static String PICC_CALANCIINFO;
    /**保存核保1  */
    public static String  PICC_HEBAOSAVE1;
    /**保存核保2  */
    public static String  PICC_HEBAOSAVE2;
    /**保存核保3   */
    public static String  PICC_HEBAOSAVE3;
    /** 保存核保4  */
    public static String  PICC_HEBAOSAVE4;
    /** 保存核保5  */
    public static String  PICC_HEBAOSAVE5;
    /** 保存核保6   */
    public static String  PICC_HEBAOSAVE6;
    /**提交核保1   */
    public static String PICC_HEBAOCOMMIT1;
    /**提交核保2   */
    public static String PICC_HEBAOCOMMIT2;
    /** 查询核保   */
    public static String PICC_HEBAOSEARCH;
    /** 查询核保前   */
    public static String  PICC_HEBAOPREPARESEARCH;
    /** 查询核保意见   */
    public static String  PICC_HEBAOSEARCHUNDWRTMSG;


    /**----------------------------- 人保URL ----------------------------------*/
    /** PICC人保业务登录 */
    public static String PICC_MAIN_URL;
    /** PICC用户名 */
    public static String PICC_USERNAME;
    /** PICC密码1 */
    public static String PICC_PWD1;
    /** PICC密码2 */
    public static String PICC_PWD2;

    /** ====================================================人保end====================*/


    /** ==================================================== 天平start ====================*/

    /** AXATP业务登录 */
    public static String AXATP_DOMIAN;
    /** AXATP用户名 */
    public static String AXATP_USERNAME;
    /** AXATP密码 */
    public static String AXATP_PWD;
    /** AXATP推荐码 */
    public static String AXATP_RECOMMENDCODE;
    /** 天平系统登录 */
    public static String AXATP_LOGIN;
    /** 天平系统图片验证码 */
    public static String AXATP_PIC;
    /** 天平系统 */
    public static String AXATP_PRECISIONINDEX;
    public static String AXATP_TOPREPARATION;
    public static String AXATP_SAVEPRECISIONINDEXINFOFORPC;
    public static String AXATP_INITPRECISIONBASICINFO;

    public static String AXATP_CARBASEVEHICLEPRICEQUERY;
    public static String AXATP_CARQUERYWHERERBCODE;

    public static String AXATP_SAVEPRECISIONCARINFO;
    public static String AXATP_APPLYQUERY;
    public static String AXATP_HANDLEAPPLYQUERYRETURN;
    public static String AXATP_REINSUREVIEW;
    public static String AXATP_PKGPREMIUMCALC;

    public static String AXATP_SHOWBUSINESSPLANINFO;
    public static String AXATP_SHOWFORCEPLANINFO;

    public static  String AXATP_BUSINESSPREMIUMCALCULATER;
    public static  String AXATP_FORCEPREMIUMCALCULATER;





    /** ====================================================天平end ====================*/


    /**----------------------------- 返回错误信息 ----------------------------------*/
    public static Integer ERROR404;
    public static String ERROR404MSG;
    public static Integer SUCCESS200;
    public static String  SUCCESS200MSG;

    /**----------------------------- 返回保险公司信息 ----------------------------------*/
    public static String  PICC_NAME;
    
    /**------------------------------保险公司标识------------------------*/
    public static Integer PICC_FLAG;

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

            PICC_CALANCIINFO = getString("picc_calAnciInfo","");
            PICC_HEBAOSAVE1 = getString("picc_hebaosave1","");
            PICC_HEBAOSAVE2 = getString("picc_hebaosave2","");
            PICC_HEBAOSAVE3 = getString("picc_hebaosave3","");
            PICC_HEBAOSAVE4  = getString("picc_hebaosave4","");
            PICC_HEBAOSAVE5 = getString("picc_hebaosave5","");
            PICC_HEBAOSAVE6 = getString("picc_hebaosave6","");
            PICC_HEBAOCOMMIT1 = getString("picc_hebaocommit1","");
            PICC_HEBAOCOMMIT2 = getString("picc_hebaocommit2","");
            PICC_HEBAOSEARCH = getString("picc_hebaosearch","");
            PICC_HEBAOPREPARESEARCH = getString("picc_hebaopreparesearch","");
            PICC_HEBAOSEARCHUNDWRTMSG = getString("picc_hebaosearchundwrtmsg","");


            //安盛天平 start

             AXATP_DOMIAN = getString("axatp_domain","");
             AXATP_USERNAME = getString("axatp_username","");
             AXATP_PWD = getString("axatp_passwd","");
             AXATP_RECOMMENDCODE = getString("axatp_recommendcode","");
             AXATP_LOGIN = getString("axatp_login","");
             AXATP_PIC = getString("axatp_pic","");
             AXATP_PRECISIONINDEX  = getString("axatp_precisionindex","");
             AXATP_SAVEPRECISIONINDEXINFOFORPC = getString("axatp_savePrecisionIndexInfoForPc","");
             AXATP_SHOWBUSINESSPLANINFO = getString("axatp_showbusinessplaninfo","");
             AXATP_SHOWFORCEPLANINFO = getString("axatp_showforceplaninfo","");
             AXATP_INITPRECISIONBASICINFO = getString("axatp_initPrecisionBasicInfo","");
             AXATP_CARBASEVEHICLEPRICEQUERY = getString("axatp_carBasicVehiclePriceQuery","");
             AXATP_CARQUERYWHERERBCODE =getString("axatp_carQueryWhereRbCode","");
             AXATP_TOPREPARATION =getString("axatp_toPreparation","");
             AXATP_SAVEPRECISIONCARINFO = getString("axatp_savePrecisionCarInfo","");
             AXATP_APPLYQUERY = getString("axatp_applyQuery","");
             AXATP_HANDLEAPPLYQUERYRETURN = getString("axatp_handleApplyQueryReturn","");
             AXATP_REINSUREVIEW = getString("axatp_reInsureView","");
             AXATP_PKGPREMIUMCALC = getString("axatp_pkgPremiumCalc","");
             AXATP_BUSINESSPREMIUMCALCULATER = getString("axatp_businessPremiumCalculater","");
             AXATP_FORCEPREMIUMCALCULATER = getString("axatp_forcePremiumCalculater","");



            //安盛天平 end

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
            insuranceNameMap.put("玻璃单独破碎险条款", "BoLi");
            insuranceNameMap.put("自燃损失险条款", "ZiRan");
            insuranceNameMap.put("发动机特别损失险条款", "SheShui");
            insuranceNameMap.put("不计免赔率（车辆损失险）", "BuJiMianCheSun");
            insuranceNameMap.put("不计免赔率（车损险）", "BuJiMianCheSun");
            insuranceNameMap.put("不计免赔率（三者险）", "BuJiMianSanZhe");
            insuranceNameMap.put("不计免赔率（机动车盗抢险）", "BuJiMianDaoQiang");
            insuranceNameMap.put("不计免赔率（车身划痕损失险）", "BuJiMianFuJia");
            insuranceNameMap.put("不计免赔率（车上人员责任险（司机））", "BuJiMianRenYuan");
            //保险公司标识
            PICC_FLAG =  Integer.parseInt(getString("picc_flag","2"));
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
