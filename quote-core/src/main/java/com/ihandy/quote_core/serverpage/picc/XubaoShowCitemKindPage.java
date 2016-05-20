package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoShowCitemKindPage extends BasePage {
    private static Logger logger = Logger.getLogger(XubaoShowCitemKindPage.class);
    @Override
    public String doRequest(Request request) {
        String htmlCitemKind = null;
        String urlCitemKind = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCitemKind = urlCitemKind +"?"+ StringBaseUtils.Map2GetParam(paraMap);
        Map mapClaimsMsg = HttpsUtil.sendGet(urlCitemKind,super.piccSessionId,"GB2312");
        htmlCitemKind = mapClaimsMsg.get("html").toString();
        return htmlCitemKind;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        if(!html.equals("")||null!=html) {
            Map returnMap = new HashMap<>();
            Map lastResult = new HashMap<>();
            returnMap.put("nextParams", null);
            File file=new File("d:/3.html");
            Document doc=null;
            try {
                doc = Jsoup.parse(html);
            }catch (Exception e){
                try{
                    doc = Jsoup.parse(file, "GB2312");
                }catch (Exception e1){}
            }
            //主险
            //初始化主险保险金额
            lastResult.put("CheSun",0);
            lastResult.put("DaoQiang",0);
            lastResult.put("SanZhe",0);
            lastResult.put("SiJi",0);
            lastResult.put("ChengKe",0);
            try {
                Elements itemKindMaintrs = doc.getElementById("itemKindMain").select("tbody").select("tr");
                for (int i = 0; i < itemKindMaintrs.size(); i++) {
                    Elements tds = itemKindMaintrs.get(i).select("td");
                    if (tds.size() == 32) {//主表中数据
                        String insureName = tds.get(1).child(1).attributes().get("value");
                        Double insureCost = Double.parseDouble(tds.get(4).childNode(1).childNode(1).attributes().get("value").toString());
                        if (insureName.equals("机动车损失保险")) {
                            // System.out.println("insureName =" + insureName + " insureCost = " + insureCost + "\n");
                            lastResult.put("CheSun", insureCost);
                        }
                        if (insureName.equals("盗抢险")) {
                            // System.out.println("insureName =" + insureName + " insureCost = " + insureCost + "\n");
                            lastResult.put("DaoQiang", insureCost);
                        }
                        if (insureName.equals("第三者责任保险")) {
                            // System.out.println("insureName =" + insureName + " insureCost = " + insureCost + "\n");
                            lastResult.put("SanZhe", insureCost);
                        }
                        if (insureName.equals("车上人员责任险（司机）")) {
                            // System.out.println("insureName =" + insureName + " insureCost = " + insureCost + "\n");
                            lastResult.put("SiJi", insureCost);
                        }
                        if (insureName.equals("车上人员责任险（乘客）")) {
                            //System.out.println("insureName =" + insureName + " insureCost = " + insureCost + "\n");
                            lastResult.put("ChengKe", insureCost);
                        }
                    }
                }
            }catch (Exception e){
                logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆险种主要险种失败】");
            }
            //附加险
            lastResult.put("Boli",0);
            lastResult.put("ZiRan",0);
            lastResult.put("HuaHen",0);
            lastResult.put("SheShui",0);
            lastResult.put("CheDeng",0);
            try{
                Elements itemKindSubtrs = doc.getElementById("itemKindSub").select("tr");
                for(int i = 0;i<itemKindSubtrs.size();i++) {
                    Elements tds = itemKindSubtrs.get(i).select("td");
                    if(tds.size()==27) {//主表中数据 玻璃险
                        String insureName = tds.get(1).child(0).attributes().get("value");
                        if (insureName.equals("玻璃单独破碎险")) {
                            try {
                                Element elementBoliSelect = tds.get(1).child(1).child(0);
                                if (elementBoliSelect.tagName().equals("select")) {
                                    Elements BoliElements = elementBoliSelect.getAllElements();
                                    for (int k = 0; k < BoliElements.size(); k++) {
                                        if (BoliElements.get(k).attributes().hasKey("selected")) {
                                            String boliValue = BoliElements.get(k).attributes().get("value");
                                            if (boliValue.equals("10") || boliValue.equals("11")) {//国产玻璃
                                                lastResult.put("Boli", 1);
                                                // System.out.println("insureName =" + insureName + " insureCost = " + 1 + "\n");
                                            } else if (boliValue.equals("20") || boliValue.equals("21")) {
                                                lastResult.put("Boli", 2);
                                                // System.out.println("insureName =" + insureName + " insureCost = " + 2 + "\n");
                                            }
                                        }
                                    }
                                }
                            } catch (Exception E) {
                                logger.info("抓取机器人，【 PICC 解析车辆险种玻璃险失败】");
                            }
                        }else if (insureName.equals("自燃损失险")) {
                            //Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                            lastResult.put("ZiRan", 1);
                        }else if(insureName.equals("车身划痕损失险")){
                            // Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                            lastResult.put("HuaHen",1);
                        }else if(insureName.equals("发动机特别损失险")){
                            //Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                            lastResult.put("SheShui",1);
                        }
                        //// TODO: 2016/5/18 1、车身划痕损失险  2、约定区域通行费用特约条款 3、法律费用特约条款 4、附加油污污染责任保险
                    }
                }
            }catch (Exception e){
                logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆险种附加险种失败】");
            }
            //不计免赔率初始化
            lastResult.put("BuJiMianCheSun",0);
            lastResult.put("BuJiMianSanZhe",0);
            lastResult.put("BuJiMianDaoQiang",0);
            lastResult.put("BuJiMianRenYuan",0);
            lastResult.put("BuJiMianFuJia",0);
            try{
                Elements itemKindSpecialtrs = doc.getElementById("itemKindSpecial").select("tbody").select("tr");
                for(int i = 0;i<itemKindSpecialtrs.size();i++) {
                    Elements tds = itemKindSpecialtrs.get(i).select("td");
                    if(tds.size()==27){//主表中数据
                        String insureName = tds.get(1).child(0).attributes().get("value");
                        if(insureName.equals("不计免赔率（车辆损失险）")){
                            lastResult.put("BuJiMianCheSun",1);
                            // System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                        }else if(insureName.equals("不计免赔率（三者险）")){
                            lastResult.put("BuJiMianSanZhe",1);
                            // System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                        }else if(insureName.equals("不计免赔率（机动车盗抢险）")){
                            lastResult.put("BuJiMianDaoQiang",1);
                            // System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                        }else if(insureName.contains("不计免赔率（车上人员责任险")){
                            lastResult.put("BuJiMianRenYuan",1);
                            // System.out.println(" BuJiMianRenYuan insureName =" +insureName +" insureCost = "+1+"\n");
                        }else{
                            lastResult.put("BuJiMianFuJia",1);
                            // System.out.println(" BuJiMianFuJia insureName =" +insureName +" insureCost = "+1+"\n");
                        }
                    }
                }
            }catch (Exception e){
                logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆险种不计免险种失败】");
            }

            returnMap.put("lastResult", lastResult);
            response.setResponseMap(returnMap);
            response.setReturnCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
        }else{
            response.setResponseMap(null);
            response.setReturnCode(SysConfigInfo.ERROR404);
            response.setErrMsg(SysConfigInfo.ERROR404MSG);
        }
        return response;
    }

    @Override
    public Response run(Request request) {
        String html = doRequest(request);
        Response response = getResponse(html);
        return response;
    }
}
