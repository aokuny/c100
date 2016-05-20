package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoShowCinsuredPage extends BasePage {
    private static Logger logger = LoggerFactory.getLogger(XubaoShowCitemCarPage.class);
    @Override
    public String doRequest(Request request) {
        String htmlCinsured = null;
        String urlCinsured = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCinsured = urlCinsured + StringBaseUtils.Map2GetParam(paraMap);
        Map mapCinsured = HttpsUtil.sendGet(urlCinsured,super.piccSessionId);
        htmlCinsured = mapCinsured.get("html").toString();
        return htmlCinsured;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        if(null!=html) {
            Map returnMap = new HashMap<>();
            Map lastResult = new HashMap<>();
            html = html.replaceAll("\r|\n|\t", "");
            Document doc = Jsoup.parse(html);
            String role="";//角色
            String type="";//类型
            String name="";//名称
            String companyType="";//公司性质
            String CredentislasNum="";//证件号码
            String IdCardType="";//证件类型
            String address="";//地址
            String email="";//邮箱
            String telNum="";//固定电话
            String mobilePhone="";//移动电话

            Elements trs = doc.getElementById("insertInsuredRow").select("tr");
            try {
                for (int i = 0; i < trs.size(); i++) {
                    if (trs.get(i).attributes().hasKey("id")) {
                        Elements tds = trs.get(i).select("td");
                        Map returnResult = new HashMap<>();
                        //初始化投被保人信息
                        returnResult.put("role","");
                        returnResult.put("type","");
                        returnResult.put("name","");
                        returnResult.put("companyType","");
                        returnResult.put("IdCardType","");
                        returnResult.put("CredentislasNum","");
                        returnResult.put("address","");
                        returnResult.put("email","");
                        returnResult.put("telNum","");
                        returnResult.put("mobilePhone","");
                        for (int j = 0; j < tds.size(); j++) {
                            Element td = tds.get(j);
                            if (j == 0) {
                                try {
                                    if (td.childNodeSize() == 1) {
                                        role = td.childNode(0).attributes().get("value");
                                        returnResult.put("role", role);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {//去除隐藏node
                                                role = td.childNode(k).attributes().get("value");
                                                role = role.replaceAll("\\s*", "");
                                                if (null != role && !role.equals("")) {
                                                    returnResult.put("role", role);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 role 字段失败】");
                                }
                            }
                            if (j == 1) {
                                try{
                                    if (td.childNodeSize() == 1) {
                                        type = td.childNode(0).attributes().get("value");
                                        returnResult.put("type", type);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                type = td.childNode(k).attributes().get("value");
                                                type = type.replaceAll("\\s*", "");
                                                if (null != type && !type.equals("")) {
                                                    returnResult.put("type", type);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 type 字段失败】");
                                }
                            }
                            if (j == 2) {
                                try {
                                    if (td.childNodeSize() == 1) {
                                        name = td.childNode(0).attributes().get("value");
                                        returnResult.put("name", name);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                name = td.childNode(k).attributes().get("value");
                                                name = name.replaceAll("\\s*", "");
                                                if (null != name && !name.equals("")) {
                                                    returnResult.put("name", name);
                                                    break;
                                                }

                                            }
                                        }
                                    }
                                }  catch (Exception e){
                                logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 name 字段失败】");
                            }
                            }
                            if (j == 3) {
                                try {
                                    if (td.childNodeSize() == 1) {
                                        companyType = td.childNode(0).attributes().get("value");
                                        returnResult.put("companyType", companyType);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                companyType = td.childNode(k).attributes().get("value");
                                                companyType = companyType.replaceAll("\\s*", "");
                                                if (null != companyType && !companyType.equals("")) {
                                                    returnResult.put("companyType", companyType);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 companyType 字段失败】");
                                }
                            }
                            if (j == 4) {
                                try{
                                    if (td.childNodeSize() == 1) {
                                        IdCardType = td.childNode(0).attributes().get("value");
                                        returnResult.put("IdCardType", IdCardType);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                IdCardType = td.childNode(k).attributes().get("value");
                                                IdCardType = IdCardType.replaceAll("\\s*", "");
                                                if (null != IdCardType && !IdCardType.equals("")) {
                                                    returnResult.put("IdCardType", IdCardType);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 IdCardType 字段失败】");
                                }
                            }
                            if (j == 5) {
                                try{
                                if (td.childNodeSize() == 1) {
                                    CredentislasNum = td.childNode(0).attributes().get("value");
                                    returnResult.put("CredentislasNum", CredentislasNum);
                                } else {
                                    for (int k = 0; k < td.childNodeSize(); k++) {
                                        if (!td.childNode(k).attributes().hasKey("type")) {
                                            CredentislasNum = td.childNode(k).attributes().get("value");
                                            CredentislasNum = CredentislasNum.replaceAll("\\s*", "");
                                            if (null != CredentislasNum && !CredentislasNum.equals("")) {
                                                returnResult.put("CredentislasNum", CredentislasNum);
                                                break;
                                            }
                                        }
                                    }
                                }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 CredentislasNum 字段失败】");
                                }
                            }
                            if (j == 6) {
                                try{
                                    if (td.childNodeSize() == 1) {
                                        address = td.childNode(0).attributes().get("value");
                                        returnResult.put("address", address);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                address = td.childNode(k).attributes().get("value");
                                                address = address.replaceAll("\\s*", "");
                                                if (null != address && !address.equals("")) {
                                                    returnResult.put("address", address);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 address 字段失败】");
                                }
                            }
                            if (j == 7) {
                                try{
                                    if (td.childNodeSize() == 1) {
                                        email = td.childNode(0).attributes().get("value");
                                        returnResult.put("email", email);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                email = td.childNode(k).attributes().get("value");
                                                email = email.replaceAll("\\s*", "");
                                                if (null != email && !email.equals("")) {
                                                    returnResult.put("email", email);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 email 字段失败】");
                                }
                            }
                            if (j == 8) {
                                try{
                                    if (td.childNodeSize() == 1) {
                                        telNum = td.childNode(0).attributes().get("value");
                                        returnResult.put("telNum", telNum);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                telNum = td.childNode(k).attributes().get("value");
                                                telNum = telNum.replaceAll("\\s*", "");
                                                if (null != telNum && !telNum.equals("")) {
                                                    returnResult.put("telNum", telNum);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 telNum 字段失败】");
                                }
                            }
                            if (j == 9) {
                                try {
                                    if (td.childNodeSize() == 1) {
                                        mobilePhone = td.childNode(0).attributes().get("value");
                                        returnResult.put("mobilePhone", mobilePhone);
                                    } else {
                                        for (int k = 0; k < td.childNodeSize(); k++) {
                                            if (!td.childNode(k).attributes().hasKey("type")) {
                                                mobilePhone = td.childNode(k).attributes().get("value");
                                                mobilePhone = mobilePhone.replaceAll("\\s*", "");
                                                if (null != mobilePhone && !mobilePhone.equals("")) {
                                                    returnResult.put("mobilePhone", mobilePhone);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人 mobilePhone 字段失败】");
                                }
                            }
                        }
                        lastResult.put(i, returnResult);
                    }
                }
            }catch (Exception e){
                logger.info("抓取机器人，【 PICC 在浏览表单页面中解析车辆的投保人/被保人失败】");
            }

            returnMap.put("nextParams", null);
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
