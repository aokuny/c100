package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
/**
 * 交强险请求
 * @author liming
 *
 */
public class QuoteJqxPage extends BasePage{
	
	private static Logger logger = Logger.getLogger(QuoteJqxPage.class);

	@Override
	public String doRequest(Request request) {
		String url = request.getUrl();
		Map<String, String> param = request.getRequestParam();
		String paramStr = StringBaseUtils.Map2GetParam(param);
		//发送http请求
		Map<String, String> result = HttpsUtil.sendPost(url, paramStr, super.piccSessionId);
		return result.get("html");
	}

	@Override
	public Response getResponse(String html) {
		Response response = new Response();
		html = "{\"totalRecords\":1,\"data\":[{\"ciInsureTax\":{\"declareStatus\":\"\",\"taxPayerName\":\"\",\"taxConditionCode\":\"\",\"calctaxFlag\":\"\",\"taxPayerIdentificationCode\":\"\",\"sumTax\":0,\"policyNo\":\"\",\"sumOverdue\":0,\"declareDate\":\"\",\"demandNo\":\"\",\"flag\":\"\",\"annualTaxDue\":0,\"quotationNo\":\"\",\"proposalNo\":\"\",\"taxTermTypeCode\":\"\",\"operateTimeForHis\":null,\"taxDescription\":\"\",\"sumTaxDefault\":0,\"ciInsureAnnualTaxes\":[],\"insertTimeForHis\":null,\"taxRegistryNumber\":\"\",\"taxAmountFlag\":\"\"},\"prpCfixations\":null,\"ciInsureDemandLossList\":[],\"ciInsureDemandPayList\":[],\"ciInsureDemandRisk\":null,\"errMessage\":\"\",\"errorMessageVo\":{\"errorCode\":\"\",\"flag\":\"\",\"errorMessage\":\"\"},\"ciRiskWarningClaimItems\":[],\"ciInsureDemandCheckVo\":{\"checkQuestion\":\"\",\"validCheckDate\":\"\",\"demandNo\":\"\",\"riskCode\":\"\",\"checkCode\":\"\",\"errMessage\":\"\",\"renewalFlag\":\"\",\"flag\":\"\",\"checkAnswer\":\"\"},\"ciInsureDemand\":{\"policyadjustvalue\":-0.3,\"tonCount\":0,\"frameNo\":\"LGXC16DF4A0169664\",\"lastpolicyexpiredate\":null,\"demandNo\":\"01PICC02160000000000897066369B\",\"demandeffectendtime\":null,\"preferentialDay\":53,\"commissionRate\":0,\"resureFundFee\":5.81,\"previousPay\":0,\"lastpolicyquerydate\":null,\"extendChar1\":\"\",\"proposalNo\":\"\",\"responseRemark\":null,\"premium\":580.83,\"operatorCode\":\"\",\"enrollDate\":null,\"districtCoeff\":0,\"chgVehicleMessage\":\"\",\"ciCommissionMessage\":null,\"ciRiskWarningClaimItems\":[],\"reinsureFlag\":\"0\",\"usbkey\":\"106023BJ\",\"carOwner\":\"朱佳佳\",\"ciCoveragePremiums\":[],\"rescueFundRate\":0.01,\"lastproducercode\":\"\",\"disPlacement\":\"\",\"posNo\":\"\",\"lastpolicybilldate\":null,\"carStatus\":\"A\",\"flag\":\"\",\"ineffectualDate\":null,\"dummyresponseremark\":\"\",\"rateRloatFlag\":\"\",\"lastEffectiveDate\":null,\"certificateDate\":null,\"licenseColorCode\":\"A\",\"demandTime\":{\"date\":18,\"day\":3,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":11,\"seconds\":23,\"minutes\":36,\"time\":1463542583195},\"lastpolicytotalpremium\":null,\"exhaustCapacity\":1488,\"carKindCode\":\"K33\",\"amount\":0,\"taxFlag\":\"2\",\"brandName\":\"\",\"computerip\":\"13.245.135.1\",\"preferentialPremium\":84.17,\"adjustEnd\":null,\"remark\":\"\",\"restricFlag\":\"0001\",\"preferentialFormula\":\"减免保费=(581.11/366*42) + (580.15/365*11)\",\"requestRemark\":null,\"peccancyAdjustReason\":\"\",\"benchMarkPremium\":0,\"seatCount\":5,\"pmVehicleMessage\":\"\",\"coverageCode\":\"CLIBJ2\",\"prevalidno\":\"\",\"busilastyearstartdate\":null,\"driverRateReason\":\"\",\"manufacturerName\":\"比亚迪汽车有限公司\",\"lastyearenddate\":{\"date\":25,\"day\":6,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466784000000},\"validCheckDate\":null,\"ipPart\":\"\",\"coverageType\":\"1\",\"transferDate\":null,\"engineNo\":\"4LA4D8297\",\"adjustStart\":null,\"driverCoeff\":0,\"dummyrequestremark\":\"\",\"areaCode\":\"\",\"netPremium\":547.95,\"endValidDate\":null,\"useNatureCode\":\"A\",\"policyNo\":\"\",\"lastExpireDate\":null,\"peccancyCoeff\":0,\"busiLastYearEndDate\":null,\"licenseNo\":\"京P55M11\",\"comCode\":\"11010286\",\"taxActual\":0,\"proconfirmenddate\":\"\",\"vehicleCategory\":\"\",\"modelCode\":\"QCJ7150A6\",\"insertTimeForHis\":null,\"ciLastPolicyInfo\":null,\"taxPremium\":32.88,\"startDate\":null,\"lastBillDate\":null,\"lateFee\":0,\"salePrice\":\"\",\"haulage\":\"\",\"taxTotal\":\"350.00\",\"fuelType\":\"A\",\"makeDate\":null,\"pmUserType\":\"\",\"basePremium\":950,\"operateTimeForHis\":null,\"useTypeSource\":\"\",\"claimCoeff\":-0.3,\"wholeWeight\":\"\",\"taxRate\":6,\"useTypeMessage\":\"\",\"ownerName\":\"朱佳佳\",\"licenseType\":\"02\",\"claimAdjustReason\":\"\",\"checkDate\":null,\"lastpolicyeffectivedate\":null,\"channeltype\":\"\",\"lastyearstartdate\":{\"date\":26,\"day\":5,\"timezoneOffset\":-480,\"year\":115,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1435248000000},\"querypastdate\":null,\"endDate\":null,\"vehicleOwnerMessageType\":\"\",\"brandCName\":\"比亚迪牌\",\"dzflag\":\"\",\"noVehicleMessageType\":\"\",\"districtRateReason\":\"\",\"proconfirmstartdate\":\"\"},\"ciCarShipTax\":{\"taxpayerNo\":\"\",\"thisPayTax\":350,\"policyNo\":\"\",\"payStartDate\":{\"date\":1,\"day\":5,\"timezoneOffset\":-480,\"year\":116,\"month\":0,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1451577600000},\"sumPayTax\":0,\"carNumber\":\"1002261803\",\"delayPayTax\":0,\"demandNo\":\"01PICC02160000000000897066369B\",\"taxPayerCertiCode\":\"\",\"flag\":\"\",\"poCategory\":\"\",\"payId\":\"03\",\"poWeight\":1200,\"taxFlag\":\"1\",\"freeNo\":\"\",\"proposalNo\":\"\",\"payEndDate\":{\"date\":31,\"day\":6,\"timezoneOffset\":-480,\"year\":116,\"month\":11,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1483113600000},\"remarks\":\"\",\"taxPayerCertiType\":\"\",\"taxComName\":\"\",\"dutyPaidProofNo\":\"\",\"prePayTax\":0}}]}";
		//TODO 对数据进行缓存
		if(StringUtils.isNoneBlank(html)){
			// data ciInsureDemand netPremium 字段
			JSONObject jsonObj = JSONObject.parseObject(html);
			JSONArray dataArray = jsonObj.getJSONArray("data");
			JSONObject ciInsureDemandObj = dataArray.getJSONObject(0).getJSONObject("ciInsureDemand");
			Double netPremium = ciInsureDemandObj.getDouble("netPremium");
			Double taxTotal = ciInsureDemandObj.getDouble("taxTotal");
			logger.info("PICC API 解析，交强险：报价信息->" + netPremium);
			logger.info("PICC API 解析，车船税：报价信息->" + taxTotal);
			Map<String, Double> returnMap = new HashMap<>();
			returnMap.put("netPremium", netPremium);
			returnMap.put("taxTotal", taxTotal);
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
		String html = null;
		try {
			//html = this.doRequest(request);
		} catch (Exception e) {
			logger.error("PICC API 【HTTP请求出错】" + e.getMessage() + "，url：" + request.getUrl());
		}
		return this.getResponse(html);
	}
	
}
