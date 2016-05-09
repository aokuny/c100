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
     public static String rb_domain ;

    /**首页面*/
     public  static String  RB_index;
     public  static int RB_index_pageNum;
    /**承保页面 */
     public static String  RB_calogin;
     public static int RB_calogin_pageNum;
     /**车商渠道专用出单页面 */
     public static String RB_quickProposal ;
     public static int RB_quickProposal_pageNum;
     /**续保对话框 */
     public static String  RB_editRenewalSearch ;
     public static int  RB_editRenewalSearch_pageNum;
     /**查看保单数据源 */
     public static String  RB_selectRenewal;
     public static int RB_selectRenewal_pageNum;
     /**浏览保单 */
     public static String  RB_browsePolicyNo;
     public static int RB_browsePolicyNo_pageNum;
     /**车辆信息 */
     public static String  RB_carTab;
     public static int RB_carTab_pageNum;
     /**关系人信息 */
     public static String  RB_insuredTab ;
     public static int RB_insuredTab_pageNum;
     /**保险责任 */
     public static String  RB_kindTab;
     public static int RB_kindTab_pageNum;
     /**查询理赔信息 */
     public static String  RB_queryClaimsMsg;
     public static int RB_queryClaimsMsg_pageNum;


    static {
        try {
            properties = new Properties();

            //FileInputStream fis = new FileInputStream(classpath:);// 属性文件输入流
            InputStream is = SysConfigInfo.class.getClassLoader().getResource("url.properties").openStream();
            properties.load(is);
            // 微信相关接口
            rb_domain = getString("rb.domain", "");

            RB_index = getString("rb.index", "");
            RB_index_pageNum =getInt("rb.index.pageNum",1);
            RB_calogin = getString("rb.calogin", "");
            RB_calogin_pageNum =getInt("rb.calogin.pageNum", 10);
            RB_quickProposal = getString("rb.quickProposal", "");
            RB_quickProposal_pageNum =getInt("rb.quickProposal.pageNum", 20);
            RB_editRenewalSearch = getString("rb.editRenewalSearch", "");
            RB_editRenewalSearch_pageNum = getInt("rb.editRenewalSearch.pageNum", 30);
            RB_selectRenewal = getString("rb.selectRenewal", "");
            RB_selectRenewal_pageNum = getInt("rb.selectRenewal.pageNum", 40);
            RB_browsePolicyNo = getString("rb.browsePolicyNo", "");
            RB_browsePolicyNo_pageNum = getInt("rb.browsePolicyNo.pageNum", 50);
            RB_carTab=getString("rb.carTab","");
            RB_carTab_pageNum = getInt("rb.carTab.pageNum", 60);
            RB_insuredTab = getString("rb.insuredTab", "");
            RB_insuredTab_pageNum =getInt("rb.insuredTab.pageNum", 70);
            RB_kindTab = getString("rb.kindTab", "");
            RB_kindTab_pageNum = getInt("rb.kindTab.pageNum", 80);
            RB_queryClaimsMsg = getString("rb.queryClaimsMsg", "");
            RB_queryClaimsMsg_pageNum = getInt("rb.queryClaimsMsg.pageNum", 90);

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

    public static Map getSelectedFields(String prex){
        Map map =new HashMap();
        Map urlMap = new HashMap();
        Map numMap = new HashMap();
        try {
            Class clazz = Class.forName("com.ihandy.quote_core.utils.SysConfigInfo");//根据类名获得其对应的Class对象 写上你想要的类名就是了 注意是全名 如果有包的话要加上 比如java.Lang.String
            Field[]  fields = clazz.getDeclaredFields();//根据Class对象获得属性 私有的也可以获得

            for(Field f : fields) {
                String name = f.getName();
                if(name.contains(prex)){
                    Object value =null;
                    Method[] methods = clazz.getMethods();
                    for(int i = 0; i < methods.length; i++){
                        Method method = methods[i];
                        if(method.getName().equals("get"+name)){
                            value = method.invoke(null);
                            break;
                        }
                    }
                    if(f.getType().getName().equals("java.lang.String")){
                        urlMap.put(name, value );

                    }else if(f.getType().getName().equals("int")){
                        numMap.put(name, value );
                    }else{}
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        Set<String> numKey = numMap.keySet();
        for (Iterator it = numKey.iterator(); it.hasNext();) {
            String numS = (String) it.next();
            Set<String> urlKey = urlMap.keySet();
            for (Iterator itUrlKey = urlKey.iterator(); itUrlKey.hasNext();) {
                String s = (String) itUrlKey.next();
                if(s.equals(numS.substring(0,numS.length()-8))){
                   // map.put(s,numS);
                    map.put(urlMap.get(s),numMap.get(numS));
                }
            }
        }

        return map;
    }
    public static void main(String[] args) {
        Map map  = getSelectedFields("RB_");
        logger.info(map+"");

    }

    public static String getRB_index() {
        return RB_index;
    }

    public static void setRB_index(String RB_index) {
        SysConfigInfo.RB_index = RB_index;
    }

    public static int getRB_index_pageNum() {
        return RB_index_pageNum;
    }

    public static void setRB_index_pageNum(int RB_index_pageNum) {
        SysConfigInfo.RB_index_pageNum = RB_index_pageNum;
    }

    public static String getRB_calogin() {
        return RB_calogin;
    }

    public static void setRB_calogin(String RB_calogin) {
        SysConfigInfo.RB_calogin = RB_calogin;
    }

    public static int getRB_calogin_pageNum() {
        return RB_calogin_pageNum;
    }

    public static void setRB_calogin_pageNum(int RB_calogin_pageNum) {
        SysConfigInfo.RB_calogin_pageNum = RB_calogin_pageNum;
    }

    public static String getRB_quickProposal() {
        return RB_quickProposal;
    }

    public static void setRB_quickProposal(String RB_quickProposal) {
        SysConfigInfo.RB_quickProposal = RB_quickProposal;
    }

    public static int getRB_quickProposal_pageNum() {
        return RB_quickProposal_pageNum;
    }

    public static void setRB_quickProposal_pageNum(int RB_quickProposal_pageNum) {
        SysConfigInfo.RB_quickProposal_pageNum = RB_quickProposal_pageNum;
    }

    public static String getRB_editRenewalSearch() {
        return RB_editRenewalSearch;
    }

    public static void setRB_editRenewalSearch(String RB_editRenewalSearch) {
        SysConfigInfo.RB_editRenewalSearch = RB_editRenewalSearch;
    }

    public static int getRB_editRenewalSearch_pageNum() {
        return RB_editRenewalSearch_pageNum;
    }

    public static void setRB_editRenewalSearch_pageNum(int RB_editRenewalSearch_pageNum) {
        SysConfigInfo.RB_editRenewalSearch_pageNum = RB_editRenewalSearch_pageNum;
    }

    public static String getRB_selectRenewal() {
        return RB_selectRenewal;
    }

    public static void setRB_selectRenewal(String RB_selectRenewal) {
        SysConfigInfo.RB_selectRenewal = RB_selectRenewal;
    }

    public static int getRB_selectRenewal_pageNum() {
        return RB_selectRenewal_pageNum;
    }

    public static void setRB_selectRenewal_pageNum(int RB_selectRenewal_pageNum) {
        SysConfigInfo.RB_selectRenewal_pageNum = RB_selectRenewal_pageNum;
    }

    public static String getRB_browsePolicyNo() {
        return RB_browsePolicyNo;
    }

    public static void setRB_browsePolicyNo(String RB_browsePolicyNo) {
        SysConfigInfo.RB_browsePolicyNo = RB_browsePolicyNo;
    }

    public static int getRB_browsePolicyNo_pageNum() {
        return RB_browsePolicyNo_pageNum;
    }

    public static void setRB_browsePolicyNo_pageNum(int RB_browsePolicyNo_pageNum) {
        SysConfigInfo.RB_browsePolicyNo_pageNum = RB_browsePolicyNo_pageNum;
    }

    public static String getRB_carTab() {
        return RB_carTab;
    }

    public static void setRB_carTab(String RB_carTab) {
        SysConfigInfo.RB_carTab = RB_carTab;
    }

    public static int getRB_carTab_pageNum() {
        return RB_carTab_pageNum;
    }

    public static void setRB_carTab_pageNum(int RB_carTab_pageNum) {
        SysConfigInfo.RB_carTab_pageNum = RB_carTab_pageNum;
    }

    public static String getRB_insuredTab() {
        return RB_insuredTab;
    }

    public static void setRB_insuredTab(String RB_insuredTab) {
        SysConfigInfo.RB_insuredTab = RB_insuredTab;
    }

    public static int getRB_insuredTab_pageNum() {
        return RB_insuredTab_pageNum;
    }

    public static void setRB_insuredTab_pageNum(int RB_insuredTab_pageNum) {
        SysConfigInfo.RB_insuredTab_pageNum = RB_insuredTab_pageNum;
    }

    public static String getRB_kindTab() {
        return RB_kindTab;
    }

    public static void setRB_kindTab(String RB_kindTab) {
        SysConfigInfo.RB_kindTab = RB_kindTab;
    }

    public static int getRB_kindTab_pageNum() {
        return RB_kindTab_pageNum;
    }

    public static void setRB_kindTab_pageNum(int RB_kindTab_pageNum) {
        SysConfigInfo.RB_kindTab_pageNum = RB_kindTab_pageNum;
    }

    public static String getRB_queryClaimsMsg() {
        return RB_queryClaimsMsg;
    }

    public static void setRB_queryClaimsMsg(String RB_queryClaimsMsg) {
        SysConfigInfo.RB_queryClaimsMsg = RB_queryClaimsMsg;
    }

    public static int getRB_queryClaimsMsg_pageNum() {
        return RB_queryClaimsMsg_pageNum;
    }

    public static void setRB_queryClaimsMsg_pageNum(int RB_queryClaimsMsg_pageNum) {
        SysConfigInfo.RB_queryClaimsMsg_pageNum = RB_queryClaimsMsg_pageNum;
    }
}
