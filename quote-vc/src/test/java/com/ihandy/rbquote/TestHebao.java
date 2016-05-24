package com.ihandy.rbquote;

import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-config.xml")
public class TestHebao {

    @Test
    public void testGetprpAnciInfo() {
        Map nextParamsMap = new HashMap<>();
        Map lastResultMap = new HashMap<>();
        String html = "{\n" +
                "     \"totalRecords\": 1,\n" +
                "     \"data\": [\n" +
                "     {\n" +
                "     \"discountRateBI\": 31.1499,\n" +
                "     \"origBusiType\": \"B\",\n" +
                "     \"averProfitRate\": 5,\n" +
                "     \"busiTypeCommBIUp\": 0,\n" +
                "     \"operSellExpensesAmountBI\": 378.6482,\n" +
                "     \"sellExpensesRateCIUp\": 4,\n" +
                "     \"discountRateCIUp\": 30,\n" +
                "     \"sellExpensesAmount\": 131.4184,\n" +
                "     \"discountRateBIAmount\": 1223.6584,\n" +
                "     \"operatePayRate\": 0.3829,\n" +
                "     \"actProCommRate\": 50,\n" +
                "     \"operateProfitRate\": 67.3486,\n" +
                "     \"operCommRateBIUp\": null,\n" +
                "     \"expProCommRateUp\": 56.15,\n" +
                "     \"profitRateBIUp\": 31.15,\n" +
                "     \"operateCommCI\": 0,\n" +
                "     \"standbyField1\": \"A险L险属于高赔付险种\",\n" +
                "     \"discountRateUpAmount\": 2439.145,\n" +
                "     \"sellExpensesCIUpAmount\": 23.2332,\n" +
                "     \"discountRateCIAmount\": 369.17,\n" +
                "     \"operSellExpensesRateBI\": 14,\n" +
                "     \"discountRateUp\": 50,\n" +
                "     \"sumPremium\": 3285.46,\n" +
                "     \"minNetSumPremium\": 1865.3557,\n" +
                "     \"breakEvenValue\": 0.01,\n" +
                "     \"operCommRate\": 0,\n" +
                "     \"operCommRateAmount\": 0,\n" +
                "     \"discountRateCI\": 38.86,\n" +
                "     \"busiRiskRate\": 81.4679,\n" +
                "     \"baseActBusiType\": null,\n" +
                "     \"anciIndiConfQueryVoList\": [],\n" +
                "     \"standPayRate\": 0.2579,\n" +
                "     \"busiTypeCommCIUp\": 0,\n" +
                "     \"busiBalanRate\": 76.9879,\n" +
                "     \"operSellExpensesRateCI\": 4,\n" +
                "     \"discountRateAmount\": 1592.83,\n" +
                "     \"operSellExpensesAmount\": 401.8814,\n" +
                "     \"sumPremiumCI\": 580.83,\n" +
                "     \"discountRate\": 32.6514,\n" +
                "     \"operSellExpensesRate\": 12.2321,\n" +
                "     \"sellExpensesBIUpAmount\": 378.6482,\n" +
                "     \"operateCommRateCI\": 0,\n" +
                "     \"strKindBusiTypeC\": \"\",\n" +
                "     \"businessCode\": null,\n" +
                "     \"strKindBusiTypeB\": \"\",\n" +
                "     \"strKindBusiTypeA\": \"050100 050500 050600 050701 050702 050231 050912 050921 050928 050929 \",\n" +
                "     \"busiStandardBalanRate\": 62.02,\n" +
                "     \"discountRateBIUpAmount\": 1223.6623,\n" +
                "     \"strKindBusiTypeE\": \"050200 050911 \",\n" +
                "     \"operCommRateCIUp\": 4,\n" +
                "     \"strKindBusiTypeD\": \"\",\n" +
                "     \"minNetSumPremiumBI\": 1504.5429,\n" +
                "     \"baseExpBusiType\": null,\n" +
                "     \"actBusiType\": \"A\",\n" +
                "     \"sellExpensesRate\": 4,\n" +
                "     \"sumPremiumBI\": 2704.63,\n" +
                "     \"operSellExpensesAmountCI\": 23.2332,\n" +
                "     \"operateCommRateBI\": 0,\n" +
                "     \"discountRateCIUpAmount\": 285,\n" +
                "     \"operateCommBI\": 0,\n" +
                "     \"actProCommRateUp\": null,\n" +
                "     \"averageRate\": 10.78,\n" +
                "     \"sellExpensesRateBIUp\": 14,\n" +
                "     \"minNetSumPremiumCI\": 741.4997,\n" +
                "     \"proCommRateBIUp\": 46.15,\n" +
                "     \"expBusiType\": \"A\"\n" +
                "     }\n" +
                "     ]\n" +
                "     }";
        Map returnPolicyNoMap = new HashMap<>();
        Map map = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map) jsonArray.get(0);
        JSONArray jsonArray2 = (JSONArray) map1.get("data");
        Map dataMap = (Map) jsonArray2.get(0);
        nextParamsMap.put("prpAnciInfo",dataMap);
       // nextParamsMap.put("num",66);
        String params = StringBaseUtils.Map2GetParam(nextParamsMap);
        System.out.println("params = "+params);
       /* nextParamsMap.put("prpAnciInfo.discountRateBI",dataMap.get("discountRateBI"));
        nextParamsMap.put("prpAnciInfo.origBusiType",dataMap.get("origBusiType"));
        nextParamsMap.put("prpAnciInfo.averProfitRate",dataMap.get("averProfitRate"));
        nextParamsMap.put("prpAnciInfo.busiTypeCommBIUp",dataMap.get("busiTypeCommBIUp"));
        nextParamsMap.put("prpAnciInfo.operSellExpensesAmountBI",dataMap.get("operSellExpensesAmountBI"));
        nextParamsMap.put("prpAnciInfo.sellExpensesRateCIUp",dataMap.get("sellExpensesRateCIUp"));
        nextParamsMap.put("prpAnciInfo.discountRateCIUp",dataMap.get("discountRateCIUp"));
        nextParamsMap.put("prpAnciInfo.sellExpensesAmount",dataMap.get("sellExpensesAmount"));
        nextParamsMap.put("prpAnciInfo.discountRateBIAmount",dataMap.get("discountRateBIAmount"));
        nextParamsMap.put("prpAnciInfo.operatePayRate",dataMap.get("operatePayRate"));
        nextParamsMap.put("prpAnciInfo.operCommRateBIUp",dataMap.get("operCommRateBIUp"));*/

    }

    @Test
    public void testSave1(){
        String html = "{\"msg\":\"0\",\"totalRecords\":0,\"data\":[]}";
        Map returnPolicyNoMap = new HashMap<>();
        Map map = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map) jsonArray.get(0);
        String msg =  map1.get("msg").toString();
        System.out.println("msg = "+msg);
    }

    @Test
    public void testSave2(){
        String html = "{\"msg\":\"SINGLE,,66692393-6,\",\"totalRecords\":0,\"data\":[]}";
        Map returnPolicyNoMap = new HashMap<>();
        Map map = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map) jsonArray.get(0);
        String msg =  map1.get("msg").toString();
        String[] msgArr = msg.split(",");
        System.out.println("msgArr = "+msgArr);
    }
}
