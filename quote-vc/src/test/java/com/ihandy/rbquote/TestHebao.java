package com.ihandy.rbquote;

import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.bean.other.HebaoResponse;
import com.ihandy.quote_core.serverpage.picc.HebaoCalAnciInfoPage;
import com.ihandy.quote_core.service.IService;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by fengwen on 2016/5/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-init.xml")
public class TestHebao {
	
	private static Logger logger = LoggerFactory.getLogger(TestHebao.class);
    @Resource(name="RBServiceImpl")
    private IService irbService;
     @Test
    public  void  testHebao(){
    	Response response = new Response();
		//HebaoCalAnciInfoPage hebaoCalAnciInfoPage = new HebaoCalAnciInfoPage(1);
		//Request request = new Request();
		//Map preMap =(Map)resp.getResponseMap().get("nextParams");
		//request.setRequestParam(preMap);//
		//request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CALANCIINFO);// GET
		//Response responseHebaoCalAnciInfo = hebaoCalAnciInfoPage.run(request);
		
	   String code = 	irbService.commitHeBaoInfo(response);
	   System.out.println("code = "+code);
    }

	@Test
	public  void  searchHebao(){

		List<HebaoResponse> hebaoResponseList = 	irbService.getHebaoResponse("京55M11");
		System.out.println("\n");
		System.out.println("return hebaoResponse = "+hebaoResponseList);
	}
     /* @Test
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
        nextParamsMap.put("prpAnciInfo.discountRateBI",dataMap.get("discountRateBI"));
        nextParamsMap.put("prpAnciInfo.origBusiType",dataMap.get("origBusiType"));
        nextParamsMap.put("prpAnciInfo.averProfitRate",dataMap.get("averProfitRate"));
        nextParamsMap.put("prpAnciInfo.busiTypeCommBIUp",dataMap.get("busiTypeCommBIUp"));
        nextParamsMap.put("prpAnciInfo.operSellExpensesAmountBI",dataMap.get("operSellExpensesAmountBI"));
        nextParamsMap.put("prpAnciInfo.sellExpensesRateCIUp",dataMap.get("sellExpensesRateCIUp"));
        nextParamsMap.put("prpAnciInfo.discountRateCIUp",dataMap.get("discountRateCIUp"));
        nextParamsMap.put("prpAnciInfo.sellExpensesAmount",dataMap.get("sellExpensesAmount"));
        nextParamsMap.put("prpAnciInfo.discountRateBIAmount",dataMap.get("discountRateBIAmount"));
        nextParamsMap.put("prpAnciInfo.operatePayRate",dataMap.get("operatePayRate"));
        nextParamsMap.put("prpAnciInfo.operCommRateBIUp",dataMap.get("operCommRateBIUp"));

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

    @Test
    public void testSave3(){
        Map  returnMap  = new HashMap<>();
        Map nextParamsMap = new HashMap<>();
        String html = "{\"totalRecords\":1,\"data\":[{\"prpDdismantleDetails\":[{\"roleCode_uni\":\"\",\"flag\":\"DAA\",\"id\":{\"agreementNo\":\"RULE20130000000023071\",\"roleCode\":\"110021100065\",\"assignType\":\"1\",\"configCode\":\"PUB\"},\"prpDdismantle\":null,\"roleName\":\"北京众合四海保险代理有限公司\",\"costRate\":100,\"operateTimeForHis\":null,\"adjustFlag\":\"\",\"remark\":\"\",\"roleFlag\":\"1\",\"insertTimeForHis\":null,\"businessNature\":\"2\",\"roleAccount\":\"\"},{\"roleCode_uni\":\"\",\"flag\":\"DZA\",\"id\":{\"agreementNo\":\"RULE20130000000023072\",\"roleCode\":\"110021100065\",\"assignType\":\"1\",\"configCode\":\"PUB\"},\"prpDdismantle\":null,\"roleName\":\"北京众合四海保险代理有限公司\",\"costRate\":100,\"operateTimeForHis\":null,\"adjustFlag\":\"\",\"remark\":\"\",\"roleFlag\":\"1\",\"insertTimeForHis\":null,\"businessNature\":\"2\",\"roleAccount\":\"\"}],\"maxRateScm\":35,\"levelMaxRateCi\":null,\"levelMaxRate\":null,\"maxRateScmCi\":4,\"prpCsaless\":[{\"splitRate\":-2,\"agreementNo\":\"\",\"oriSplitNumber\":0,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"11\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"市公司费用率\",\"prpCsalesDatilss\":[]},{\"splitRate\":0,\"agreementNo\":\"\",\"oriSplitNumber\":0,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"12\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"县支公司费用率\",\"prpCsalesDatilss\":[]},{\"splitRate\":0,\"agreementNo\":\"\",\"oriSplitNumber\":0,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"13\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"团队费用率\",\"prpCsalesDatilss\":[]},{\"splitRate\":25,\"agreementNo\":\"\",\"oriSplitNumber\":108.7,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"15\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"渠道手续费率\",\"prpCsalesDatilss\":[]},{\"splitRate\":0,\"agreementNo\":\"\",\"oriSplitNumber\":0,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"14\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"销售端费用率\",\"prpCsalesDatilss\":[]},{\"splitRate\":0,\"agreementNo\":\"\",\"oriSplitNumber\":0,\"flag\":\"\",\"splitWay\":\"\",\"id\":{\"proposalNo\":\"\",\"salesCode\":\"01\",\"salesDetailCode\":\"16\"},\"upperRate\":0,\"totalRate\":0.23,\"operateTimeForHis\":null,\"totalRateMax\":0,\"riskCode\":\"DAA\",\"remark\":\"\",\"salesName\":\"销售费用\",\"splitFee\":0,\"insertTimeForHis\":null,\"prpCmain\":null,\"salesDetailName\":\"渠道维护费用率\",\"prpCsalesDatilss\":[]}],\"prpDpayForPolicies\":[{\"prpDagreement\":null,\"agentCode_uni\":\"\",\"customerGroupCode\":\"\",\"coinsDeduct\":\"\",\"auditFlag\":\"\",\"costType\":\"2\",\"flag\":\"\",\"id\":{\"agreementNo\":\"RULE20130000000023071\",\"seriseNo\":1,\"configCode\":\"PUB\"},\"kindFlag\":\"\",\"currency\":\"\",\"costRate\":25,\"costRateUpper\":35,\"operateTimeForHis\":null,\"adjustFlag\":\"0\",\"riskCode\":\"DAA\",\"costUpper\":0,\"remark\":\"\",\"upperFlag\":\"\",\"insertTimeForHis\":null},{\"prpDagreement\":null,\"agentCode_uni\":\"\",\"customerGroupCode\":\"\",\"coinsDeduct\":\"\",\"auditFlag\":\"\",\"costType\":\"2\",\"flag\":\"\",\"id\":{\"agreementNo\":\"RULE20130000000023072\",\"seriseNo\":2,\"configCode\":\"PUB\"},\"kindFlag\":\"\",\"currency\":\"\",\"costRate\":4,\"costRateUpper\":4,\"operateTimeForHis\":null,\"adjustFlag\":\"0\",\"riskCode\":\"DZA\",\"costUpper\":0,\"remark\":\"\",\"upperFlag\":\"\",\"insertTimeForHis\":null}]}]}";
        Map returnPolicyNoMap = new HashMap<>();
        Map map = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map) jsonArray.get(0);
        JSONArray data = (JSONArray) map1.get("data");
        Map dataMap = (Map) data.get(0);
        //1）组装prpDdismantleDetails
        JSONArray jsonArrayPrpDdismantleDetails =JSONArray.fromObject(dataMap.get("prpDdismantleDetails"));
        for(int i=0;i<jsonArrayPrpDdismantleDetails.size();i++){
            Map mapPrpDdismantleDetails = (Map)jsonArrayPrpDdismantleDetails.get(i);
            nextParamsMap.put("prpDdismantleDetails["+i+"].flag",mapPrpDdismantleDetails.get("flag"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].costRate",mapPrpDdismantleDetails.get("costRate"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].businessNature",mapPrpDdismantleDetails.get("businessNature"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].roleFlag",mapPrpDdismantleDetails.get("roleFlag"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].roleName",mapPrpDdismantleDetails.get("roleName"));
            JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpDdismantleDetails.get("id"));
            Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
            nextParamsMap.put("prpDdismantleDetails["+i+"].id.agreementNo", mapId.get("agreementNo"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].id.roleCode", mapId.get("roleCode"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].id.configCode",mapId.get("configCode"));
            nextParamsMap.put("prpDdismantleDetails["+i+"].id.assignType",mapId.get("assignType"));
            if(mapPrpDdismantleDetails.get("flag").toString().equals("DZA")){
                nextParamsMap.put("prpDdismantleDetails_["+i+"].flag",mapPrpDdismantleDetails.get("flag"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].costRate",mapPrpDdismantleDetails.get("costRate"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].businessNature",mapPrpDdismantleDetails.get("businessNature"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].roleFlag",mapPrpDdismantleDetails.get("roleFlag"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].roleName",mapPrpDdismantleDetails.get("roleName"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].id.agreementNo", mapId.get("agreementNo"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].id.roleCode", mapId.get("roleCode"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].id.configCode",mapId.get("configCode"));
                nextParamsMap.put("prpDdismantleDetails_["+i+"].id.assignType",mapId.get("assignType"));
            }
        }
        //2）组装prpCsaless(6)
        JSONArray jsonArrayPrpCsaless =JSONArray.fromObject(dataMap.get("prpCsaless"));
        for(int i=0;i<jsonArrayPrpCsaless.size();i++){
            Map mapPrpCsaless = (Map)jsonArrayPrpCsaless.get(i);
            nextParamsMap.put("prpCsaless["+i+"].agreementNo",mapPrpCsaless.get("agreementNo"));
            nextParamsMap.put("prpCsaless["+i+"].flag",mapPrpCsaless.get("flag"));
            nextParamsMap.put("prpCsaless["+i+"].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber"));
            nextParamsMap.put("prpCsaless["+i+"].remark",mapPrpCsaless.get("remark"));
            nextParamsMap.put("prpCsaless["+i+"].riskCode",mapPrpCsaless.get("riskCode"));
            nextParamsMap.put("prpCsaless["+i+"].salesDetailName",mapPrpCsaless.get("salesDetailName"));
            nextParamsMap.put("prpCsaless["+i+"].salesName",mapPrpCsaless.get("salesName"));
            nextParamsMap.put("prpCsaless["+i+"].splitFee",mapPrpCsaless.get("splitFee"));
            nextParamsMap.put("prpCsaless["+i+"].splitRate",mapPrpCsaless.get("splitRate"));
            nextParamsMap.put("prpCsaless["+i+"].splitWay",mapPrpCsaless.get("splitWay"));
            nextParamsMap.put("prpCsaless["+i+"].totalRate",mapPrpCsaless.get("totalRate"));
            nextParamsMap.put("prpCsaless["+i+"].totalRateMax",mapPrpCsaless.get("totalRateMax"));
            JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpCsaless.get("id"));
            Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
            nextParamsMap.put("prpCsaless["+i+"].id.proposalNo", mapId.get("proposalNo"));
            nextParamsMap.put("prpCsaless["+i+"].id.salesCode", mapId.get("salesCode"));
            nextParamsMap.put("prpCsaless["+i+"].id.salesDetailCode",mapId.get("salesDetailCode"));
            if(i==0){
                nextParamsMap.put("prpCsaless_["+i+"].agreementNo",mapPrpCsaless.get("agreementNo"));
                nextParamsMap.put("prpCsaless_["+i+"].flag",mapPrpCsaless.get("flag"));
                nextParamsMap.put("prpCsaless_["+i+"].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber"));
                nextParamsMap.put("prpCsaless_["+i+"].remark",mapPrpCsaless.get("remark"));
                nextParamsMap.put("prpCsaless_["+i+"].riskCode",mapPrpCsaless.get("riskCode"));
                nextParamsMap.put("prpCsaless_["+i+"].salesDetailName",mapPrpCsaless.get("salesDetailName"));
                nextParamsMap.put("prpCsaless_["+i+"].salesName",mapPrpCsaless.get("salesName"));
                nextParamsMap.put("prpCsaless_["+i+"].splitFee",mapPrpCsaless.get("splitFee"));
                nextParamsMap.put("prpCsaless_["+i+"].splitRate",mapPrpCsaless.get("splitRate"));
                nextParamsMap.put("prpCsaless_["+i+"].splitWay",mapPrpCsaless.get("splitWay"));
                nextParamsMap.put("prpCsaless_["+i+"].totalRate",mapPrpCsaless.get("totalRate"));
                nextParamsMap.put("prpCsaless_["+i+"].totalRateMax",mapPrpCsaless.get("totalRateMax"));
                nextParamsMap.put("prpCsaless_["+i+"].id.proposalNo", mapId.get("proposalNo"));
                nextParamsMap.put("prpCsaless_["+i+"].id.salesCode", mapId.get("salesCode"));
                nextParamsMap.put("prpCsaless_["+i+"].id.salesDetailCode",mapId.get("salesDetailCode"));
            }
        }
        //3)
        nextParamsMap.put("maxRateScmCi",dataMap.get("maxRateScmCi"));
        nextParamsMap.put("maxRateScm",dataMap.get("maxRateScm"));
        nextParamsMap.put("levelMaxRateCi",dataMap.get("levelMaxRateCi"));
        nextParamsMap.put("levelMaxRate",dataMap.get("levelMaxRate"));
        //4)组装prpDpayForPolicies // TODO: 2016/5/25
        System.out.println("data = "+nextParamsMap);
    }

    @Test
    public void testSave4(){

        Map nextParamsMap = new HashMap<>();
        String html = "{\"totalRecords\":3,\"data\":[{\"jfFlag\":\"1\",\"payReason\":\"R29\",\"delinquentFee\":580.83,\"flag\":\"\",\"payNo\":1,\"subsidyRate\":0,\"serialNo\":0,\"currency\":\"CNY,人民币\",\"planFee\":580.83,\"endorseNo\":\"\",\"payReasonName\":\"(强制)收保费\",\"isBICI\":\"CI\",\"netPremium\":547.95,\"planDate\":{\"date\":26,\"day\":0,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466870400000,\"nanos\":0},\"taxPremium\":32.88},{\"jfFlag\":\"1\",\"payReason\":\"RM9\",\"delinquentFee\":350,\"flag\":\"\",\"payNo\":1,\"subsidyRate\":0,\"serialNo\":0,\"currency\":\"CNY,人民币\",\"planFee\":350,\"endorseNo\":\"\",\"payReasonName\":\"代收车船税\",\"isBICI\":\"CShip\",\"netPremium\":null,\"planDate\":{\"date\":26,\"day\":0,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466870400000,\"nanos\":0},\"taxPremium\":null},{\"jfFlag\":\"1\",\"payReason\":\"R21\",\"delinquentFee\":3011.82,\"flag\":\"\",\"payNo\":1,\"subsidyRate\":0,\"serialNo\":0,\"currency\":\"CNY,人民币\",\"planFee\":3011.82,\"endorseNo\":\"\",\"payReasonName\":\"收保费\",\"isBICI\":\"BI\",\"netPremium\":2841.33,\"planDate\":{\"date\":26,\"day\":0,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466870400000,\"nanos\":0},\"taxPremium\":170.49}]}";

        Map map = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map) jsonArray.get(0);
        JSONArray data = (JSONArray) map1.get("data");
        //1）组装 prpCplanTemps
        for(int i=0;i<data.size();i++){
            prpCplanTemps_[0].currency	CNY	34
            prpCplanTemps_[0].delinquentFee	3011.82	43
            prpCplanTemps_[0].endorseNo		32
            prpCplanTemps_[0].flag		27
            prpCplanTemps_[0].isBICI	BI	31
            prpCplanTemps_[0].netPremium	2841.33	40
            prpCplanTemps_[0].payNo	1	29
            prpCplanTemps_[0].payReason	R21	35
            prpCplanTemps_[0].planDate	2016-6-26	40
            prpCplanTemps_[0].planFee	3011.82	37
            prpCplanTemps_[0].serialNo	0	32
            prpCplanTemps_[0].subsidyRate	0	35
            prpCplanTemps_[0].taxPremium	170.49	39
            Map mapPrpCplanTemps = (Map)data.get(i);
            nextParamsMap.put("prpCplanTemps["+i+"].currency",mapPrpCplanTemps.get("currency"));
            nextParamsMap.put("prpCplanTemps["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
            nextParamsMap.put("prpCplanTemps["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
            nextParamsMap.put("prpCplanTemps["+i+"].flag",mapPrpCplanTemps.get("flag"));
            nextParamsMap.put("prpCplanTemps["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
            nextParamsMap.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
            nextParamsMap.put("prpCplanTemps["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
            nextParamsMap.put("prpCplanTemps["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
            nextParamsMap.put("prpCplanTemps["+i+"].planDate",mapPrpCplanTemps.get("planDate"));
            nextParamsMap.put("prpCplanTemps["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
            nextParamsMap.put("prpCplanTemps["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
            nextParamsMap.put("prpCplanTemps["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));
            nextParamsMap.put("prpCplanTemps["+i+"].taxPremium", mapPrpCplanTemps.get("taxPremium"));
            if(mapPrpCplanTemps.get("isBICI").toString().equals("BI")){
                nextParamsMap.put("prpCplanTemps_["+i+"].currency",mapPrpCplanTemps.get("currency"));
                nextParamsMap.put("prpCplanTemps_["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
                nextParamsMap.put("prpCplanTemps_["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
                nextParamsMap.put("prpCplanTemps_["+i+"].flag",mapPrpCplanTemps.get("flag"));
                nextParamsMap.put("prpCplanTemps_["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
                nextParamsMap.put("prpCplanTemps_["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                nextParamsMap.put("prpCplanTemps_["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
                nextParamsMap.put("prpCplanTemps_["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
                nextParamsMap.put("prpCplanTemps_["+i+"].planDate",mapPrpCplanTemps.get("planDate"));
                nextParamsMap.put("prpCplanTemps_["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
                nextParamsMap.put("prpCplanTemps_["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
                nextParamsMap.put("prpCplanTemps_["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));
                nextParamsMap.put("prpCplanTemps_["+i+"].taxPremium", mapPrpCplanTemps.get("taxPremium"));
            }
        }

        System.out.println("data = "+nextParamsMap);
    }

    @Test
    public void testSearchHebao(){
        String html="{\"totalRecords\":2,\"data\":[{\"dmFlag\":\"0\",\"policyNo\":\"                      \",\"contractNo\":\"                      \",\"specialflag\":\"初始值\",\"underWriteEndDate\":{\"date\":25,\"day\":3,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464105600000,\"nanos\":0},\"licenseNo\":\"京P55M11\",\"checkStatus\":\"初始状态\",\"operateDate\":{\"date\":25,\"day\":3,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464105600000,\"nanos\":0},\"comCode\":\"11010286\",\"checkFlag\":\"初始值\",\"proposalNo\":\"TDAA201611010000955201\",\"underWriteFlag\":\"见费出单待缴费\",\"insuredName\":\"朱佳佳\",\"operatorCode\":\"020083    \",\"startDate\":{\"date\":26,\"day\":0,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466870400000,\"nanos\":0}},{\"dmFlag\":\"0\",\"policyNo\":\"                      \",\"contractNo\":\"                      \",\"specialflag\":\"初始值\",\"underWriteEndDate\":{\"date\":25,\"day\":3,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464105600000,\"nanos\":0},\"licenseNo\":\"京P55M11\",\"checkStatus\":\"初始状态\",\"operateDate\":{\"date\":25,\"day\":3,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464105600000,\"nanos\":0},\"comCode\":\"11010286\",\"checkFlag\":\"初始值\",\"proposalNo\":\"TDZA201611010000977639\",\"underWriteFlag\":\"见费出单待缴费\",\"insuredName\":\"朱佳佳\",\"operatorCode\":\"020083    \",\"startDate\":{\"date\":26,\"day\":0,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466870400000,\"nanos\":0}}],\"startIndex\":1,\"recordsReturned\":10}";
        Map  returnMap  = new HashMap<>();
        Map lastResultMap = new HashMap<>();

            Map map = new HashMap<>();
            map = StringBaseUtils.parseJSON2Map(html);
            JSONArray jsonArray = JSONArray.fromObject(map);
            Map map1 = (Map) jsonArray.get(0);
            JSONArray  jsonArrayData = (JSONArray) map1.get("data");
            for(int i=0;i<jsonArrayData.size();i++){
                   Map mapHebao = (Map)jsonArrayData.get(i);
                   String underWriteFlag =  mapHebao.get("underWriteFlag").toString();
                   String proposalNo = mapHebao.get("proposalNo").toString();
                    Map mapResult = new HashMap<>();
                    mapResult.put("underWriteFlag",underWriteFlag);
                    mapResult.put("proposalNo",proposalNo);
                    lastResultMap.put(i,mapResult);
            }
            returnMap.put("lastResult",lastResultMap);
    }
    
    public void test1(){
    	
    	HebaoResponse response = irbService.getHebaoResponse("京P55M11");
    }
    public void test2(){
    	Response response = new Response();
    	String  responseCode = irbService.commitHeBaoInfo( response);
    }*/
    
    
}
