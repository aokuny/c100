package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoSearchPage extends BasePage {

    @Override
    public String doRequest(Request request) {
        String html= null;
        String url = request.getUrl();
        url=url+"?pageSize=10&pageNo=1";
        Map paraMap = request.getRequestParam();
        String param = StringBaseUtils.Map2GetParam(paraMap);
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId);
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
    public Response getResponse(String html) {

        Response response = new Response();
        if(null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            Map lastResultMap = new HashMap<>();
            Map map  = new HashMap<>();
            map = StringBaseUtils.parseJSON2Map(html);
            JSONArray jsonArray = new JSONArray();
            jsonArray = JSONArray.fromObject(map);
            Map map1 = (Map)jsonArray.get(0);
            JSONArray jsonArray2 = (JSONArray)map1.get("data");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            Date date = new Date();
            int thisYear = Integer.parseInt( sdf.format(date));
            for(int i=0;i<jsonArray2.size();i++){
                Map map2 = (Map)jsonArray2.get(i);
                String riskCode = map2.get("riskCode").toString();
                String policyNo = map2.get("policyNo").toString();
                int year = Integer.parseInt(policyNo.substring(4,8));
                if(riskCode.equals("DAT") && year+1 == thisYear){
                    nextParamsMap.put("DAT",policyNo);//上年商业险保单号
                }
                else if(riskCode.equals("DZA") && year+1 == thisYear){
                    nextParamsMap.put("DZA",policyNo);//上年交强险保单号
                }else{}
                System.out.println("policyNo = "+policyNo+" riskCode = "+riskCode+"\n");
            }
            returnMap.put("nextParams",nextParamsMap);
            returnMap.put("lastResult",lastResultMap);
            response.setResponseMap(returnMap);
            response.setErrCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
        }else{
            response.setResponseMap(null);
            response.setErrCode(SysConfigInfo.ERROR404);
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
