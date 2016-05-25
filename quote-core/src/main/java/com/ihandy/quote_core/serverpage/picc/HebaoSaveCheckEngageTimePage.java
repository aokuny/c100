package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveCheckEngageTimePage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveCheckEngageTimePage.class);

    public HebaoSaveCheckEngageTimePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        String param ="startDateBi="+paraMap.get("biStartDate").toString()+"&startHourBi=0" +
                     "&startDateCi="+paraMap.get("ciStartDate").toString()+"&startHourCi=0" +
                     "&bizType="+paraMap.get("bizType").toString();
        url = url+"?"+param;
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        //解析{"msg":"0","totalRecords":0,"data":[]}
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                String msg =  map1.get("msg").toString();
                if(msg.equals("0")){
                    returnMap.put("nextParams",nextParamsMap);
                    response.setResponseMap(returnMap);
                    response.setReturnCode(SysConfigInfo.SUCCESS200);
                    response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
                }else{
                    logger.info("抓取机器人，【 PICC 核保保存1失败】");
                    response.setResponseMap(null);
                    response.setReturnCode(SysConfigInfo.ERROR404);
                    response.setErrMsg(SysConfigInfo.ERROR404MSG);
                }
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存1失败】");
                response.setResponseMap(null);
                response.setReturnCode(SysConfigInfo.ERROR404);
                response.setErrMsg(SysConfigInfo.ERROR404MSG);
            }
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
