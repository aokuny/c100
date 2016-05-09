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
                    if(f.getType().getName().equals("java.lang.String")){
                        urlMap.put(name, null );
                    }else if(f.getType().getName().equals("int")){
                        numMap.put(name, null);
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
                    map.put(s,numS);
                   // urlMap.remove(s);
                }
            }
        }

        return map;
    }
    public static void main(String[] args) {
        Map map  = getSelectedFields("RB_");
        logger.info(map+"");

    }


}
