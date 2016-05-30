package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoSearchPage extends BasePage {
	
    public XubaoSearchPage(int type) {
		super(type);
	}

	private static Logger logger = LoggerFactory.getLogger(XubaoShowCitemCarPage.class);
    @Override
    public String doRequest(Request request) {
        String html= null;
        String url = request.getUrl();
        // url=url+"?pageSize=10&pageNo=1&prpCrenewalVo.licenseNo=%BE%A9P55M11&prpCrenewalVo.licenseType=02";
        // url=url+"?pageSize=10&pageNo=1";
        Map paraMap = request.getRequestParam();

        if(null!= paraMap){
            Set<String> key = paraMap.keySet();
            for (Iterator it = key.iterator(); it.hasNext();) {
                String keyName = (String) it.next();
                String keyValue = paraMap.get(keyName).toString();
                if(keyName.equals("prpCrenewalVo.licenseNo")){
                    try {
                        keyValue =  URLEncoder.encode(keyValue, "GBK");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    paraMap.put(keyName, keyValue);
                }
            }
        }

        String param = StringBaseUtils.Map2GetParam(paraMap);
        url = url+"?"+param;
        //  Map map = HttpsUtil.sendPost(url,param,super.piccSessionId);
        //  Map map = HttpsUtil.sendPost(url,param,"BOCINS_prpall_Cookie=pr2GX9vBshvQ0QyF157YJvTzH5JZJxzKwd5lHctTTdPXzWwnzjBp!-1432707418; JSESSIONID=4F19X8VTnvMP7cr5hHS1BJPVcmtkFJTLyQnQXV2K19HFCyg2d2vP!16969568");
        // Map map = HttpsUtil.sendPost(url,param,"BOCINS_prpall_Cookie=pr2GX9vBshvQ0QyF157YJvTzH5JZJxzKwd5lHctTTdPXzWwnzjBp!-1432707418; JSESSIONID=4F19X8VTnvMP7cr5hHS1BJPVcmtkFJTLyQnQXV2K19HFCyg2d2vP!16969568");
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");

        html = map.get("html").toString();
        return html;

    }

    /**************** 解析json 字符串（找出上年的保单）
     * {"totalRecords":4,
     *   "data":[
     *   {"frameNo":"LGXC16DF4A0169664","lastDamagedBI":0,"noDamYearsCI":null,"policyNo":"PDAT20141102T000135272","endDate":{"date":25,"day":4,"timezoneOffset":-480,"year":115,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1435161600000,"nanos":0},"lastDamagedCI":null,"noDamYearsBI":0,"riskCode":"DAT","licenseNo":"京P55M11","engineNo":"4LA4D8297","carKindCode":"客车"},
     *   {"frameNo":"LGXC16DF4A0169664","lastDamagedBI":2,"noDamYearsCI":null,"policyNo":"PDAT20151102T000182528","endDate":{"date":25,"day":6,"timezoneOffset":-480,"year":116,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1466784000000,"nanos":0},"lastDamagedCI":null,"noDamYearsBI":0,"riskCode":"DAT","licenseNo":"京P55M11","engineNo":"4LA4D8297","carKindCode":"客车"},
     *   {"frameNo":"LGXC16DF4A0169664","lastDamagedBI":null,"noDamYearsCI":0,"policyNo":"PDZA20151102T000186793","endDate":{"date":25,"day":6,"timezoneOffset":-480,"year":116,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1466784000000,"nanos":0},"lastDamagedCI":0,"noDamYearsBI":null,"riskCode":"DZA","licenseNo":"京P55M11","engineNo":"4LA4D8297","carKindCode":"客车"},
     *   {"frameNo":"LGXC16DF4A0169664","lastDamagedBI":null,"noDamYearsCI":1,"policyNo":"PDZA20141102T000140848","endDate":{"date":25,"day":4,"timezoneOffset":-480,"year":115,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1435161600000,"nanos":0},"lastDamagedCI":0,"noDamYearsBI":null,"riskCode":"DZA","licenseNo":"京P55M11","engineNo":"4LA4D8297","carKindCode":"客车"}
     *          ],
     *   "startIndex":1,
     *   "recordsReturned":10
     *   }
     * **************
     */
    @Override
    public Response getResponse(String html, Request request) {

        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            Map lastResultMap = new HashMap<>();
            Map map  = StringBaseUtils.parseJSON2Map(html);
            JSONArray jsonArray = JSONArray.fromObject(map);
            Map map1 = (Map)jsonArray.get(0);
            JSONArray jsonArray2 = (JSONArray)map1.get("data");
            if(null!=jsonArray2 && jsonArray2.size()>0){
                int maxSyYear = 0;
                int maxJqYear = 0;
                try {
                    for (int i = 0; i < jsonArray2.size(); i++) {
                        Map map2 = (Map) jsonArray2.get(i);
                        String policyNo = map2.get("policyNo").toString();
                        String riskCode = map2.get("riskCode").toString();
                        Map expireDate = (Map) map2.get("endDate");
                        int year = Integer.parseInt(policyNo.substring(4, 8));
                        String year1 =Integer.parseInt((Integer.parseInt(expireDate.get("year").toString()) + 1900)+"")+"";
                        String month1 = "";
                        String day1 = "";
                        int month = Integer.parseInt(expireDate.get("month").toString()) + 1;
                        if (month < 10) {
                            month1 = "0" + month;
                        } else {
                            month1 = month + "";
                        }
                        int day = Integer.parseInt(expireDate.get("date").toString());
                        if (day < 10) {
                            day1 = "0" + day;
                        } else {
                            day1 = day + "";
                        }
                        String expireDateStr = year1 + "-" + month1 + "-" + day1;
                        if (riskCode.equals("DAT") || riskCode.equals("DAA")) {
                            if (year > maxSyYear) {
                                nextParamsMap.put("bizNo", policyNo);//上次商业险保单号
                                lastResultMap.put("BusinessExpireDate", expireDateStr);//商业险到期日期
                                maxSyYear = year;
                            }
                        } else if (riskCode.equals("DZA")) {
                            if (year > maxJqYear) {
                                nextParamsMap.put("DZA", policyNo);//上次交强险保单号
                                lastResultMap.put("ForceExpireDate", expireDateStr);//交强险到期日期
                                maxJqYear = year;
                            }
                        }
                    }
                }
                catch(Exception e){
                    logger.info("抓取机器人，【 PICC 在查询保单表中解析保单号/商业险到期日期/交强险到期日期失败】");
                }
                //返回最终需要的车辆基本信息
                try {
                    Map map3 = (Map) jsonArray2.get(0);
                    lastResultMap.put("LicenseNo", map3.get("licenseNo"));//车牌号
                    nextParamsMap.put("LicenseNo", map3.get("licenseNo"));
                    lastResultMap.put("EngineNo", map3.get("engineNo"));//发动机号
                    lastResultMap.put("CarVin", map3.get("frameNo"));//车架号
                }
                catch (Exception e){
                    logger.info("抓取机器人，【 PICC 在查询保单表中解析车辆车牌号/发动机号/车架号失败】");
                }
            }
            returnMap.put("nextParams",nextParamsMap);
            returnMap.put("lastResult",lastResultMap);
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
        Response response = getResponse(html, request);
        return response;
    }
}
