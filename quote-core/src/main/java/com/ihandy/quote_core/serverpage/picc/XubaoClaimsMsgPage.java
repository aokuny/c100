package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoClaimsMsgPage extends BasePage {
    @Override
    public String doRequest(Request request) {
        String htmlClaimsMsg = null;
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        url = url+ StringBaseUtils.Map2GetParam(paraMap);
        Map mapClaimsMsg = HttpsUtil.sendGet(url,super.piccSessionId);
        htmlClaimsMsg = mapClaimsMsg.get("html").toString();
        return htmlClaimsMsg;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        if(null!=html){
            Map  returnMap  = new HashMap<>();
            Map lastResult = new HashMap<>();

            returnMap.put("nextParams",null);
            // PayCompanyName;//保险公司
            // PayAmount;//出险金额
            // EndCaseTime;//结案时间
            // LossTime;//出险时间
            Document doc = Jsoup.parse(html);
            Elements trs = doc.getElementById("insertUndwrtRow").select("tr");
            for(int i = 1;i<trs.size();i++){
                Elements tds = trs.get(i).select("td");
                Map lastResult1 = new HashMap<>();
                  /*0---->报案号---->RDAT201511000000094684
                    1---->立案号---->ADAT201511010761001142
                    2---->出险日期---->2015-06-14
                    3---->报案日期---->2015-06-14
                    4---->报案注销标志---->0
                    5---->立案日期---->2015-06-14
                    6---->立案注销标志---->0
                    7---->结案标志---->2015-06-14
                    8---->责任赔款---->2000.00
                    9---->总赔付金额---->2000.00
                    10---->地址---->石景山区西山奥园*/
                lastResult1.put("PayCompanyName",SysConfigInfo.PICC_NAME);
                lastResult1.put("PayAmount", tds.get(9).select("input").val());
                lastResult1.put("EndCaseTime", tds.get(7).select("input").val());
                lastResult1.put("LossTime", tds.get(2).select("input").val());
                lastResult.put(i,lastResult1);
            }
            returnMap.put("lastResult",lastResult);
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
