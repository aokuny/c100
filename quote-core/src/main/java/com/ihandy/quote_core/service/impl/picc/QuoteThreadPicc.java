package com.ihandy.quote_core.service.impl.picc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.serverpage.picc.QuoteBefore1Page;
import com.ihandy.quote_core.serverpage.picc.QuoteBefore2Page;
import com.ihandy.quote_core.serverpage.picc.QuoteGetCarInfoOtherPage;
import com.ihandy.quote_core.serverpage.picc.QuoteGetCarInfoPage;
import com.ihandy.quote_core.serverpage.picc.QuoteGetDepreciationPage;
import com.ihandy.quote_core.serverpage.picc.QuoteGetExhaustPage;
import com.ihandy.quote_core.serverpage.picc.QuoteGetUserInfoByNamePage;
import com.ihandy.quote_core.serverpage.picc.QuoteJqxPage;
import com.ihandy.quote_core.serverpage.picc.QuotePage;
import com.ihandy.quote_core.utils.CacheConstant;
import com.ihandy.quote_core.utils.QuoteCalculateUtils;
import com.ihandy.quote_core.utils.SysConfigInfo;

/**
 * 人保报价线程
 * @author liming
 *
 */
public class QuoteThreadPicc extends Thread{
	
	private Map<String, String> quoteMap = new HashMap<>();//报价参数
	
	private static Logger logger = LoggerFactory.getLogger(QuoteThreadPicc.class);
	
	public QuoteThreadPicc() {
	}

	public QuoteThreadPicc(String name, Map<String, String> quoteMap) {
		super(name);
		this.quoteMap = quoteMap;
	}

	@Override
	public void run() {
		try {
			String LicenseNo = quoteMap.get("LicenseNo");
			//根据牌照查询车辆信息
			Map<String, Object> carMap = this.getInfoByCarNo(LicenseNo);
			carMap.put("carNo", LicenseNo);
			//报价前需要发送两个请求，并不进行操作
			Request quoteBeforeRequest1 = new Request();
			quoteBeforeRequest1.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/bindvalid/bjptBindValid.do");
			Map<String, String> quoteBeforeMap1 = new HashMap<>();
			quoteBeforeMap1.put("operatorCode", SysConfigInfo.PICC_USERNAME);
			quoteBeforeMap1.put("checkOperaType", "BJ_PT");
			quoteBeforeRequest1.setRequestParam(quoteBeforeMap1);
			QuoteBefore1Page quoteBefore1Page = new QuoteBefore1Page(1);
			quoteBefore1Page.run(quoteBeforeRequest1);
			Request quoteBeforeRequest2 = new Request();
			quoteBeforeRequest2.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/bindvalid/bjptBindValid.do");
			Map<String, String> quoteBeforeMap2 = new HashMap<>();
			quoteBeforeMap1.put("operatorCode", SysConfigInfo.PICC_USERNAME);
			quoteBeforeMap1.put("comCode", "11010286");
			quoteBeforeMap1.put("agentCode", "2-110021100065");
			quoteBeforeMap1.put("106023BJ", "106023BJ");
			quoteBeforeRequest2.setRequestParam(quoteBeforeMap2);
			QuoteBefore2Page quoteBefore2Page = new QuoteBefore2Page(1);
			quoteBefore2Page.run(quoteBeforeRequest2);
			
			String param  = null;//请求参数
			//封装请求参数
			boolean f = true;//是否续保
			//从缓存中获取上一年的保单号、保险结束日期
			Map<String, Object> renewalMap = CacheConstant.renewalInfo.get(LicenseNo);
			if(renewalMap == null || (!renewalMap.containsKey("reCiPolicyNo") && !renewalMap.containsKey("reBiPolicyNo"))){
				f = false;
			}else{
				carMap.put("reCiPolicyNo", renewalMap.get("reCiPolicyNo"));//上一年交强险投保单号
				carMap.put("reBiPolicyNo", renewalMap.get("reBiPolicyNo"));//上一年商业险投保单号
				carMap.put("ciEndDate", renewalMap.get("ciEndDate"));//上一年交强险结束日期
				carMap.put("biEndDate", renewalMap.get("biEndDate"));//上一年商业险结束日期
				carMap.put("identifyNumber", renewalMap.get("identifyNumber"));//车主身份证
				carMap.put("mobile", renewalMap.get("mobile"));//车主手机号
			}
			
			if(f){
				param = this.makeQuoteParam1(carMap, quoteMap);
			}else{
				param = this.makeQuoteParam2(carMap, quoteMap);
			}
			//封装险种信息
			param = this.makeQuoteInsurParam(quoteMap, param, (String) carMap.get("purchasePrice"), (String) carMap.get("depreciationPrice"), Integer.parseInt(carMap.get("seatCount").toString()));
			//封装投保人、被保人、车主信息
			param = this.makeQuoteInsuredInfoParam(quoteMap, param, (String) carMap.get("owner"), (String) carMap.get("identifyNumber"), (String) carMap.get("mobile"));
			
			long startTime = System.currentTimeMillis();
			String msg = "报价成功";
			logger.info("人保  API接口，【报价开始】，车牌照：" + LicenseNo);
			JSONObject quoteResultJson = new JSONObject();//三部分信息：BusinessStatus 报价结果、StatusMessage 信息描述、Userinfo用户信息、Item 报价信息
			//封装Item信息
			JSONObject Item = new JSONObject();//报价结果的JSON对象
			Item.put("Source", quoteMap.get("IntentionCompany"));
			QuotePage quotePage = new QuotePage(1);
			Request quoteRequest = new Request();
			quoteRequest.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/business/premiumCalculate.do");
			Map<String, String> quoteParamMap = new HashMap<>();
			quoteParamMap.put("param", param);
			quoteParamMap.put("carNo", LicenseNo);
			quoteRequest.setRequestParam(quoteParamMap);
			Response quoteResponse = quotePage.run(quoteRequest);
			if(quoteResponse.getReturnCode() == SysConfigInfo.ERROR404){
				//TODO 报价失败
				return;
			}else{
				Double total = 0D;
				Map<String, Map<String, Double>> quoteMap = quoteResponse.getResponseMap();
				for(String key : quoteMap.keySet()){
					Map<String, Double> map = quoteMap.get(key);
					JSONObject obj = new JSONObject();//每个险种的报价结果JSON对象
					obj.put("BaoE", map.get("amount"));
					obj.put("BaoFei", map.get("premium"));
					total = total + map.get("premium");
					Item.put(key, obj);
				}
				Item.put("BizTotal", QuoteCalculateUtils.m2(total));
			}
			//查看是否报价交强险
			if("1".equals(quoteMap.get("ForceTax"))){
				param = param.replace("prpCitemKindCI.amount=", "prpCitemKindCI.amount=122000");//保额
				param = param.replace("prpCitemKindCI.adjustRate=1", "prpCitemKindCI.adjustRate=0.9");//保额
				param = param.replace("chooseFlagCI=0", "chooseFlagCI=1");//是否选择交强险
				param = param + "&prpCitemKindCI.familyNo=";
				QuoteJqxPage quoteJqxPage = new QuoteJqxPage(1);
				Request quoteJqxRequest = new Request();
				quoteJqxRequest.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL + ":8000/prpall/business/caculatePremiun.do");
				Map<String, String> quoteJqxParamMap = new HashMap<>();
				quoteJqxParamMap.put("param", param);
				quoteJqxParamMap.put("carNo", LicenseNo);
				quoteJqxRequest.setRequestParam(quoteJqxParamMap);
				Response quoteJqxResponse = quoteJqxPage.run(quoteJqxRequest);
				if(quoteJqxResponse.getReturnCode() == SysConfigInfo.ERROR404){
					msg = quoteJqxResponse.getErrMsg();
					Item.put("ForceTotal", 0);
					Item.put("TaxTotal", 0);
				}else{
					Map<String, Double> quoteJqxMap = quoteJqxResponse.getResponseMap();
					Item.put("ForceTotal", quoteJqxMap.get("netPremium"));
					Item.put("TaxTotal", quoteJqxMap.get("taxTotal"));
				}
			}else{
				Item.put("ForceTotal", 0);
				Item.put("TaxTotal", 0);
			}
			Item.put("QuoteStatus", "1");
			Item.put("QuoteResult", msg);
			quoteResultJson.put("Item", Item);
			quoteResultJson.put("BusinessStatus", "1");
			quoteResultJson.put("StatusMessage", msg);
			//封装Userinfo信息
			JSONObject Userinfo = new JSONObject();
			Userinfo.put("LinenseNo", LicenseNo);
			Userinfo.put("ForceExpireDate", "");
			Userinfo.put("BusinessExpireDate", "");
			Userinfo.put("BusinessStartDate", "");
			Userinfo.put("ForceStartDate", "");
			quoteResultJson.put("Userinfo", Userinfo);
			Map<String, Object> quoteResultMap = CacheConstant.quoteResultInfo.get(LicenseNo);//先获取
			if(quoteResultMap == null){
				quoteResultMap = new HashMap<>();
			}
			String IntentionCompany = (String) quoteMap.get("IntentionCompany");
			quoteResultMap.put(IntentionCompany, quoteResultJson);//存放
			CacheConstant.quoteResultInfo.put(LicenseNo, quoteResultMap);
			logger.info("人保   API接口，【报价结束】，使用时间：" + ((System.currentTimeMillis() - startTime)/1000) + "S，结果：" + quoteResultJson.toJSONString());
			param = this.setFuzuHebaoJisuanParam(param);
			param = this.setSyxHebaoParam(param, LicenseNo, (String) carMap.get("identifyNumber"));
			if("1".equals(quoteMap.get("ForceTax"))){//报价交强险
				param = this.setJqxHebaoParam(param, LicenseNo, (String) carMap.get("identifyNumber"));
			}
			//System.err.println(param);
			//辅助核保计算
//			String url = "http://10.134.136.48:8000/prpall/business/refreshPlanByTimes.do";
//			String html = HttpsUtil.sendPost(url,param, quotePage.piccSessionId, "utf-8").get("html");
//			System.err.println("缴费计划结果：" + html);
//			String url = "http://10.134.136.48:8000/prpall/undwrtassist/calAnciInfo.do";
//			String html = HttpsUtil.sendPost(url,param, quotePage.piccSessionId, "utf-8").get("html");
//			System.err.println("辅助核保计算结果：" + html);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据车辆牌照查询相关数据
	 * @param carNo
	 * @return
	 */
	private Map<String, Object> getInfoByCarNo(String carNo){
		String carNo1 = carNo;
		try {
			carNo =  java.net.URLEncoder.encode(carNo,   "gb2312");
		} catch (Exception e) {
		}
		long startTime = System.currentTimeMillis();
    	//第一步请求，根据品牌获取车辆基础信息
		String param = "carShipTaxPlatFormFlag=1&randomProposalNo=7847409371464077318727 &initemKind_Flag=0&editType=NEW&bizType=PROPOSAL&ABflag=&isBICI=&prpCmain.renewalFlag=&activityFlag=0&INTEGRAL_SWITCH=0&GuangdongSysFlag=&GDREALTIMECARFlag=&GDREALTIMEMOTORFlag=&GDCANCIINFOFlag=0&prpCmain.checkFlag=&prpCmain.othFlag=&prpCmain.dmFlag=&prpCmainCI.dmFlag=&prpCmain.underWriteCode=&prpCmain.underWriteName=&prpCmain.underWriteEndDate=&prpCmain.underWriteFlag=0&prpCmainCI.checkFlag=&prpCmainCI.underWriteFlag=&bizNo=&applyNo=&oldPolicyNo=&bizNoBZ=&bizNoCI=&prpPhead.endorDate=&prpPhead.validDate=&prpPhead.comCode=&sumAmountBI=&isTaxDemand=1&cIInsureFlag=1&bIInsureFlag=1&ciInsureSwitchKindCode=E01,E11,E12,D01,D02,D03&ciInsureSwitchValues=1111111&cIInsureMotorFlag=1&mtPlatformTime=&noPermissionsCarKindCode=E12&isTaxFlag=&rePolicyNo=&oldPolicyType=&ZGRS_PURCHASEPRICE=200000&ZGRS_LOWESTPREMIUM=0&clauseFlag=&prpCinsuredOwn_Flag=&prpCinsuredDiv_Flag=&prpCinsuredBon_Flag=&relationType=&ciLimitDays=90&udFlag=&kbFlag=&sbFlag=&xzFlag=&userType=08&noNcheckFlag=0&planFlag=0&R_SWITCH=1&biStartDate=2016-05-25&ciStartDate=2016-05-25&ciStartHour=0&ciEndDate=2017-05-24&ciEndHour=24&AGENTSWITCH=1&JFCDSWITCH=19&carShipTaxFlag=11&commissionFlag=&ICCardCHeck=&riskWarningFlag=&comCodePrefix=11&DAGMobilePhoneNum=&scanSwitch=1000000000&haveScanFlag=0&diffDay=90&cylinderFlag=0&ciPlateVersion=&biPlateVersion=&criterionFlag=0&isQuotatonFlag=2&quotationRisk=DAA&getReplenishfactor=&useYear=9&FREEINSURANCEFLAG=011111&isMotoDrunkDriv=0&immediateFlag=0&immediateFlagCI=0&claimAmountReason=&isQueryCarModelFlag=&isDirectFee=&userCode=020083&comCode=11010286&chgProfitFlag=00&ciPlatTask=&biPlatTask=&upperCostRateBI=&upperCostRateCI=&rescueFundRate=&resureFundFee=&useCarshiptaxFlag=1&taxFreeLicenseNo=&isTaxFree=0&premiumChangeFlag=1&operationTimeStamp=2016-05-24 16:08:38&VEHICLEPLAT=&MOTORFASTTRACK=&motorFastTrack_flag=&MOTORFASTTRACK_INSUREDCODE=&currentDate=&vinModifyFlag=&addPolicyProjectCode=&isAddPolicy=0&commissionView=0&specialflag=&accountCheck=2&projectBak=&projectCodeBT=&projectCodeBTback=&checkTimeFlag=&checkUndwrt=0&carDamagedNum=&insurePayTimes=&claimAdjustValue=&operatorProjectCode=1-1326,2-1326,4-1326,5-1326&lossFlagKind=&chooseFlagCI=0&unitedSaleRelatioStr=&purchasePriceU=&countryNatureU=&insurancefee_reform=0&operateDateForFG=&prpCmainCommon.clauseIssue=1&amountFloat=30&vat_switch=1&BiLastPolicyFlag=&CiLastPolicyFlag=&CiLastEffectiveDate=&CiLastExpireDate=&benchMarkPremium=&BiLastEffectiveDate=&BiLastExpireDate=&lastTotalPremium=&purchasePriceUFlag=&startDateU=&endDateU=&biCiFlagU=&biCiFlagIsChange=&biCiDateIsChange=&switchFlag=0&relatedFlag=0&riskCode=DAA&prpCmain.riskCode=&riskName=&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&agentCodeValidType=&agentCodeValidValue=&agentCodeValidIPPer=&qualificationNo=201951000000800&qualificationName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&OLD_STARTDATE_CI=&OLD_ENDDATE_CI=&prpCmainCommon.greyList=&prpCmainCommon.image=&reinComPany=&reinPolicyNo=&reinStartDate=&reinEndDate=&prpCmain.proposalNo=&prpCmain.policyNo=&prpCmainCI.proposalNo=&prpCmainCI.policyNo=&prpPhead.applyNo=&prpPhead.endorseNo=&prpPheadCI.applyNo=&prpPheadCI.endorseNo=&prpCmain.comCode=11010286&comCodeDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.handler1Code=13154215  &handler1CodeDes=%BA%AB%B6%AB%D0%F1&homePhone=15801381299&officePhone=15801381299&moblie=&checkHandler1Code=1&handler1CodeDesFlag=A&handler1Info=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR__FIELD_SEPARATOR_A_FIELD_SEPARATOR_1211010268&prpCmainCommon.handler1code_uni=1211010268&prpCmain.handlerCode=13154215  &handlerCodeDes=%BA%AB%B6%AB%D0%F1&homePhonebak=&officePhonebak=&mobliebak=&handler1CodeDesFlagbak=&prpCmainCommon.handlercode_uni=1211010268&handlerInfo=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR_1211010268&prpCmain.businessNature=2&businessNatureTranslation=%D7%A8%D2%B5%B4%FA%C0%ED%D2%B5%CE%F1&prpCmain.agentCode=110021100065&prpCmainagentName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&agentType=211047&agentCode=110021100065&tempAgentCode=211047&sumPremiumChgFlag=0&prpCmain.sumPremium1=0&sumPayTax1=0&prpCmain.contractNo=&prpCmain.operateDate=2016-05-24&Today=2016-05-24&OperateDate=2016-06-24&prpCmain.makeCom=11010286&makeComDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.startDate=2016-05-25&prpCmain.startHour=0&prpCmain.endDate=2017-05-24&prpCmain.endHour=24&prpCmain.checkUpCode=&prpCmainCI.startDate=2016-05-25&prpCmainCI.startHour=0&prpCmainCI.endDate=2017-05-24&prpCmainCI.endHour=24&carPremium=0.0&insuredChangeFlag=0&refreshEadFlag=1&imageAdjustPixels=20&prpBatchVehicle.id.contractNo=&prpBatchVehicle.id.serialNo=&prpBatchVehicle.motorCadeNo=&prpBatchVehicle.licenseNo=&prpBatchVehicle.licenseType=&prpBatchVehicle.carKindCode=&prpBatchVehicle.proposalNo=&prpBatchVehicle.policyNo=&prpBatchVehicle.sumAmount=&prpBatchVehicle.sumPremium=&prpBatchVehicle.prpProjectCode=&prpBatchVehicle.coinsProjectCode=&prpBatchVehicle.profitProjectCode=&prpBatchVehicle.facProjectCode=&prpBatchVehicle.flag=&prpBatchVehicle.carId=&prpBatchVehicle.versionNo=&prpBatchMain.discountmode=&minusFlag=&paramIndex=&batchCIFlag=&batchBIFlag=&pageEndorRecorder.endorFlags=&endorDateEdit=&validDateEdit=&endDateEdit=&endorType=&prpPhead.endorType=&generatePtextFlag=0&generatePtextAgainFlag=0&quotationNo=&quotationFlag=&customerCode=&customerFlag=&compensateNo=&dilutiveType=&prpCfixationTemp.discount=&prpCfixationTemp.id.riskCode=&prpCfixationTemp.profits=&prpCfixationTemp.cost=&prpCfixationTemp.taxorAppend=&prpCfixationTemp.payMentR=&prpCfixationTemp.basePayMentR=&prpCfixationTemp.poundAge=&prpCfixationTemp.basePremium=&prpCfixationTemp.riskPremium=&prpCfixationTemp.riskSumPremium=&prpCfixationTemp.signPremium=&prpCfixationTemp.isQuotation=&prpCfixationTemp.riskClass=&prpCfixationTemp.operationInfo=&prpCfixationTemp.realDisCount=&prpCfixationTemp.realProfits=&prpCfixationTemp.realPayMentR=&prpCfixationTemp.remark=&prpCfixationTemp.responseCode=&prpCfixationTemp.errorMessage=&prpCfixationTemp.profitClass=&prpCfixationTemp.costRate=&prpCfixationCITemp.discount=&prpCfixationCITemp.id.riskCode=&prpCfixationCITemp.profits=&prpCfixationCITemp.cost=&prpCfixationCITemp.taxorAppend=&prpCfixationCITemp.payMentR=&prpCfixationCITemp.basePayMentR=&prpCfixationCITemp.poundAge=&prpCfixationCITemp.basePremium=&prpCfixationCITemp.riskPremium=&prpCfixationCITemp.riskSumPremium=&prpCfixationCITemp.signPremium=&prpCfixationCITemp.isQuotation=&prpCfixationCITemp.riskClass=&prpCfixationCITemp.operationInfo=&prpCfixationCITemp.realDisCount=&prpCfixationCITemp.realProfits=&prpCfixationCITemp.realPayMentR=&prpCfixationCITemp.remark=&prpCfixationCITemp.responseCode=&prpCfixationCITemp.errorMessage=&prpCfixationCITemp.profitClass=&prpCfixationCITemp.costRate=&prpCsalesFixes_%5B0%5D.id.proposalNo=&prpCsalesFixes_%5B0%5D.id.serialNo=&prpCsalesFixes_%5B0%5D.comCode=&prpCsalesFixes_%5B0%5D.businessNature=&prpCsalesFixes_%5B0%5D.riskCode=&prpCsalesFixes_%5B0%5D.version=&prpCsalesFixes_%5B0%5D.isForMal=&IS_LOAN_MODIFY=0&kindAndAmount=&isSpecialFlag=&specialEngage=&licenseNoCar=&prpCitemCar.carLoanFlag=&carModelPlatFlag=&updateQuotation=&prpCitemCar.licenseNo1=&prpCitemCar.monopolyFlag=0&prpCitemCar.monopolyCode=&prpCitemCar.monopolyName=&queryCarModelInfo=%B3%B5%D0%CD%D0%C5%CF%A2%C6%BD%CC%A8%BD%BB%BB%A5&prpCitemCar.id.itemNo=1&oldClauseType=&prpCitemCar.actualValue=&prpCitemCar.carId=&prpCitemCar.versionNo=&prpCmainCar.newDeviceFlag=&prpCitemCar.otherNature=&prpCitemCar.flag=&newCarFlagValue=2&prpCitemCar.discountType=&prpCitemCar.colorCode=&prpCitemCar.safeDevice=&prpCitemCar.coefficient1=&prpCitemCar.coefficient2=&prpCitemCar.coefficient3=&prpCitemCar.startSiteName=&prpCitemCar.endSiteName=&prpCmainCommon.netsales=0&prpCitemCar.newCarFlag=0&prpCitemCar.noNlocalFlag=0&prpCitemCar.licenseFlag=1&prpCitemCar.licenseNo=" + carNo + "&codeLicenseType=LicenseType01,04,LicenseType02,01,LicenseType03,02,LicenseType04,02,LicenseType05,02,LicenseType06,02,LicenseType07,04,LicenseType08,04,LicenseType09,01,LicenseType10,01,LicenseType11,01,LicenseType12,01,LicenseType13,04,LicenseType14,04,LicenseType15,04,	LicenseType16,04,LicenseType17,04,LicenseType18,01,LicenseType19,01,LicenseType20,01,LicenseType21,01,LicenseType22,01,LicenseType23,03,LicenseType24,01,LicenseType25,01,LicenseType31,03,LicenseType32,03,LicenseType90,02&prpCitemCar.licenseType=02 &LicenseTypeDes=%D0%A1%D0%CD%C6%FB%B3%B5%BA%C5%C5%C6&prpCitemCar.licenseColorCode=01&LicenseColorCodeDes=%C0%B6&prpCitemCar.engineNo=&prpCitemCar.vinNo=&prpCitemCar.frameNo=&prpCitemCar.carKindCode=A01&CarKindCodeDes=%BF%CD%B3%B5&carKindCodeBak=A01&prpCitemCar.useNatureCode=211&useNatureCodeBak=211&useNatureCodeTrue=211&prpCitemCar.clauseType=F42&clauseTypeBak=F42&prpCitemCar.enrollDate=&enrollDateTrue=&prpCitemCar.useYears=&prpCitemCar.runMiles=&taxAbateForPlat=&taxAbateForPlatCarModel=&prpCitemCar.modelDemandNo=&owner=&prpCitemCar.remark=&prpCitemCar.modelCode=&prpCitemCar.brandName=&PurchasePriceScal=10&prpCitemCar.purchasePrice=&CarActualValueTrue=&CarActualValueTrue1=&SZpurchasePriceUp=&SZpurchasePriceDown=&purchasePriceF48=200000&purchasePriceUp=100&purchasePriceDown=&purchasePriceOld=&vehiclePricer=&prpCitemCar.tonCount=0&prpCitemCar.exhaustScale=&prpCitemCar.seatCount=&seatCountTrue=&prpCitemCar.runAreaCode=11&prpCitemCar.carInsuredRelation=1&prpCitemCar.countryNature=01&prpCitemCar.cylinderCount=&prpCitemCar.loanVehicleFlag=0&prpCitemCar.transferVehicleFlag=0&prpCitemCar.transferDate=&prpCitemCar.modelCodeAlias=&prpCitemCar.carLotEquQuality=&isQuotation=1&prpCitemCar.fuelType=A&prpCitemCar.carProofType=01&prpCitemCar.isDropinVisitInsure=0&prpCitemCar.energyType=0&prpCitemCar.carProofNo=&prpCitemCar.carProofDate=&prpCmainChannel.assetAgentName=&prpCmainChannel.assetAgentCode=&prpCmainChannel.assetAgentPhone=&SYFlag=0&MTFlag=0&BMFlag=0&STFlag=0&prpCcarDevices_%5B0%5D.deviceName=&prpCcarDevices_%5B0%5D.id.itemNo=1&prpCcarDevices_%5B0%5D.id.proposalNo=&prpCcarDevices_%5B0%5D.id.serialNo=&prpCcarDevices_%5B0%5D.flag=&prpCcarDevices_%5B0%5D.quantity=&prpCcarDevices_%5B0%5D.purchasePrice=&prpCcarDevices_%5B0%5D.buyDate=&prpCcarDevices_%5B0%5D.actualValue=&hidden_index_citemcar=0&editFlag=1&prpCmainCommon.ext2=&configedRepeatTimesLocal=5&prpCinsureds_%5B0%5D.insuredFlag=1&iinsuredFlag=001&iinsuredType=001&iinsuredCode=001&iinsuredName=001&iunitType=001&iidentifyType=001&iidentifyNumber=001&iinsuredAddress=001&iemail=001&iphoneNumber=001&prpCinsureds_%5B0%5D.id.serialNo=1&prpCinsureds_%5B0%5D.insuredType=1&prpCinsureds_%5B0%5D.insuredNature=1&prpCinsureds_%5B0%5D.insuredCode=001&prpCinsureds_%5B0%5D.insuredName=1&prpCinsureds_%5B0%5D.unitType=1&prpCinsureds_%5B0%5D.identifyType=1&prpCinsureds_%5B0%5D.identifyNumber=1&prpCinsureds_%5B0%5D.insuredAddress=1&prpCinsureds_%5B0%5D.email=1&prpCinsureds_%5B0%5D.phoneNumber=1&prpCinsureds_%5B0%5D.drivingYears=&prpCinsureds_%5B0%5D.mobile=1&prpCinsureds_%5B0%5D.postCode=1&prpCinsureds_%5B0%5D.versionNo=1&prpCinsureds_%5B0%5D.auditStatus=1&prpCinsureds_%5B0%5D.sex=1&prpCinsureds_%5B0%5D.countryCode=1&prpCinsureds_%5B0%5D.flag=&prpCinsureds_%5B0%5D.age=&prpCinsureds_%5B0%5D.drivingLicenseNo=&prpCinsureds_%5B0%5D.drivingCarType=&prpCinsureds_%5B0%5D.appendPrintName=&prpCinsureds_%5B0%5D.causetroubleTimes=&prpCinsureds_%5B0%5D.acceptLicenseDate=&isCheckRepeat_%5B0%5D=&configedRepeatTimes_%5B0%5D=&repeatTimes_%5B0%5D=&prpCinsureds_%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo_%5B0%5D.insuredcode=&idCardCheckInfo_%5B0%5D.insuredFlag=&idCardCheckInfo_%5B0%5D.mobile=&idCardCheckInfo_%5B0%5D.idcardCode=&idCardCheckInfo_%5B0%5D.name=&idCardCheckInfo_%5B0%5D.nation=&idCardCheckInfo_%5B0%5D.birthday=&idCardCheckInfo_%5B0%5D.sex=&idCardCheckInfo_%5B0%5D.address=&idCardCheckInfo_%5B0%5D.issure=&idCardCheckInfo_%5B0%5D.validStartDate=&idCardCheckInfo_%5B0%5D.validEndDate=&idCardCheckInfo_%5B0%5D.samCode=&idCardCheckInfo_%5B0%5D.samType=&idCardCheckInfo_%5B0%5D.flag=&imobile=001&iauditStatus=001&iversionNo=001&hidden_index_insured=0&_insuredFlag_hide=%CD%B6%B1%A3%C8%CB&_insuredFlag_hide=%B1%BB%B1%A3%CF%D5%C8%CB&_insuredFlag_hide=%B3%B5%D6%F7&_insuredFlag_hide=%D6%B8%B6%A8%BC%DD%CA%BB%C8%CB&_insuredFlag_hide=%CA%DC%D2%E6%C8%CB&_insuredFlag_hide=%B8%DB%B0%C4%B3%B5%B3%B5%D6%F7&_insuredFlag_hide=%C1%AA%CF%B5%C8%CB&_insuredFlag=0&_insuredFlag_hide=%CE%AF%CD%D0%C8%CB&_resident=&_insuredType=1&_insuredCode=&_insuredName=&customerURL=http://10.134.136.48:8300/cif&_isCheckRepeat=&_configedRepeatTimes=&_repeatTimes=&_identifyType=01&_identifyNumber=&_unifiedSocialCreditCode=&_mobile=&_mobile1=&_sex=0&_age=&_drivingYears=&_countryCode=CHN&_insuredAddress=&_postCode=&_appendPrintName=&group_code=&_auditStatus=&_auditStatusDes=&_versionNo=&_drivingLicenseNo=&_email=&idCardCheckInfo.idcardCode=&idCardCheckInfo.name=&idCardCheckInfo.nation=&idCardCheckInfo.birthday=&idCardCheckInfo.sex=&idCardCheckInfo.address=&idCardCheckInfo.issure=&idCardCheckInfo.validStartDate=&idCardCheckInfo.validEndDate=&idCardCheckInfo.samCode=&idCardCheckInfo.samType=&idCardCheckInfo.flag=0&_drivingCarType=&CarKindLicense=&_causetroubleTimes=&_acceptLicenseDate=&prpCmainCar.agreeDriverFlag=&updateIndex=-1&prpBatchProposal.profitType=&motorFastTrack_Amount=&insurancefee_reform=0&prpCmainCommon.clauseIssue=1&prpCprofitDetailsTemp_%5B0%5D.chooseFlag=on&prpCprofitDetailsTemp_%5B0%5D.profitName=&prpCprofitDetailsTemp_%5B0%5D.condition=&profitRateTemp_%5B0%5D=&prpCprofitDetailsTemp_%5B0%5D.profitRate=&prpCprofitDetailsTemp_%5B0%5D.profitRateMin=&prpCprofitDetailsTemp_%5B0%5D.profitRateMax=&prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=&prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=&prpCprofitDetailsTemp_%5B0%5D.id.profitCode=&prpCprofitDetailsTemp_%5B0%5D.id.serialNo=1&prpCprofitDetailsTemp_%5B0%5D.id.profitType=&prpCprofitDetailsTemp_%5B0%5D.kindCode=&prpCprofitDetailsTemp_%5B0%5D.conditionCode=&prpCprofitDetailsTemp_%5B0%5D.flag=&prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on&serialNo_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.profitName=&prpCprofitFactorsTemp_%5B0%5D.condition=&rateTemp_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.rate=&prpCprofitFactorsTemp_%5B0%5D.lowerRate=&prpCprofitFactorsTemp_%5B0%5D.upperRate=&prpCprofitFactorsTemp_%5B0%5D.id.profitCode=&prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=&prpCprofitFactorsTemp_%5B0%5D.flag=&prpCitemKind.shortRateFlag=2&prpCitemKind.shortRate=100&prpCitemKind.currency=CNY&prpCmainCommon.groupFlag=0&sumBenchPremium=&prpCmain.discount=&prpCmain.sumPremium=&premiumF48=5000&prpCmain.sumNetPremium=&prpCmain.sumTaxPremium=&passengersSwitchFlag=&prpCitemKindsTemp%5B0%5D.min=&prpCitemKindsTemp%5B0%5D.max=&prpCitemKindsTemp%5B0%5D.itemKindNo=&prpCitemKindsTemp%5B0%5D.clauseCode=050002&prpCitemKindsTemp%5B0%5D.kindCode=050200&prpCitemKindsTemp%5B0%5D.kindName=%BB%FA%B6%AF%B3%B5%CB%F0%CA%A7%B1%A3%CF%D5&prpCitemKindsTemp%5B0%5D.unitAmount=&prpCitemKindsTemp%5B0%5D.quantity=&prpCitemKindsTemp%5B0%5D.amount=&prpCitemKindsTemp%5B0%5D.calculateFlag=Y11Y000&prpCitemKindsTemp%5B0%5D.startDate=&prpCitemKindsTemp%5B0%5D.startHour=&prpCitemKindsTemp%5B0%5D.endDate=&prpCitemKindsTemp%5B0%5D.endHour=&relateSpecial%5B0%5D=050911&coachCar%5B0%5D=050941&prpCitemKindsTemp%5B0%5D.flag= 100000&prpCitemKindsTemp%5B0%5D.basePremium=&prpCitemKindsTemp%5B0%5D.rate=&prpCitemKindsTemp%5B0%5D.benchMarkPremium=&prpCitemKindsTemp%5B0%5D.disCount=&prpCitemKindsTemp%5B0%5D.premium=&prpCitemKindsTemp%5B0%5D.netPremium=&prpCitemKindsTemp%5B0%5D.taxPremium=&prpCitemKindsTemp%5B0%5D.taxRate=&prpCitemKindsTemp%5B0%5D.dutyFlag=&prpCitemKindsTemp%5B1%5D.min=&prpCitemKindsTemp%5B1%5D.max=&prpCitemKindsTemp%5B1%5D.itemKindNo=&prpCitemKindsTemp%5B1%5D.clauseCode=050005&prpCitemKindsTemp%5B1%5D.kindCode=050500&prpCitemKindsTemp%5B1%5D.kindName=%B5%C1%C7%C0%CF%D5&prpCitemKindsTemp%5B1%5D.unitAmount=&prpCitemKindsTemp%5B1%5D.quantity=&prpCitemKindsTemp%5B1%5D.amount=&prpCitemKindsTemp%5B1%5D.calculateFlag=N11Y000&prpCitemKindsTemp%5B1%5D.startDate=&prpCitemKindsTemp%5B1%5D.startHour=&prpCitemKindsTemp%5B1%5D.endDate=&prpCitemKindsTemp%5B1%5D.endHour=&relateSpecial%5B1%5D=050921&coachCar%5B1%5D=&prpCitemKindsTemp%5B1%5D.flag= 100000&prpCitemKindsTemp%5B1%5D.basePremium=&prpCitemKindsTemp%5B1%5D.rate=&prpCitemKindsTemp%5B1%5D.benchMarkPremium=&prpCitemKindsTemp%5B1%5D.disCount=&prpCitemKindsTemp%5B1%5D.premium=&prpCitemKindsTemp%5B1%5D.netPremium=&prpCitemKindsTemp%5B1%5D.taxPremium=&prpCitemKindsTemp%5B1%5D.taxRate=&prpCitemKindsTemp%5B1%5D.dutyFlag=&prpCitemKindsTemp%5B2%5D.min=&prpCitemKindsTemp%5B2%5D.max=&prpCitemKindsTemp%5B2%5D.itemKindNo=&prpCitemKindsTemp%5B2%5D.clauseCode=050003&prpCitemKindsTemp%5B2%5D.kindCode=050600&prpCitemKindsTemp%5B2%5D.kindName=%B5%DA%C8%FD%D5%DF%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B2%5D.unitAmount=&prpCitemKindsTemp%5B2%5D.quantity=&prpCitemKindsTemp%5B2%5D.amount=&prpCitemKindsTemp%5B2%5D.calculateFlag=Y21Y000&prpCitemKindsTemp%5B2%5D.startDate=&prpCitemKindsTemp%5B2%5D.startHour=&prpCitemKindsTemp%5B2%5D.endDate=&prpCitemKindsTemp%5B2%5D.endHour=&relateSpecial%5B2%5D=050912&coachCar%5B2%5D=050942&prpCitemKindsTemp%5B2%5D.flag= 100000&prpCitemKindsTemp%5B2%5D.basePremium=&prpCitemKindsTemp%5B2%5D.rate=&prpCitemKindsTemp%5B2%5D.benchMarkPremium=&prpCitemKindsTemp%5B2%5D.disCount=&prpCitemKindsTemp%5B2%5D.premium=&prpCitemKindsTemp%5B2%5D.netPremium=&prpCitemKindsTemp%5B2%5D.taxPremium=&prpCitemKindsTemp%5B2%5D.taxRate=&prpCitemKindsTemp%5B2%5D.dutyFlag=&prpCitemKindsTemp%5B3%5D.min=&prpCitemKindsTemp%5B3%5D.max=&prpCitemKindsTemp%5B3%5D.itemKindNo=&prpCitemKindsTemp%5B3%5D.clauseCode=050004&prpCitemKindsTemp%5B3%5D.kindCode=050701&prpCitemKindsTemp%5B3%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9&prpCitemKindsTemp%5B3%5D.unitAmount=&prpCitemKindsTemp%5B3%5D.quantity=&prpCitemKindsTemp%5B3%5D.amount=&prpCitemKindsTemp%5B3%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B3%5D.startDate=&prpCitemKindsTemp%5B3%5D.startHour=&prpCitemKindsTemp%5B3%5D.endDate=&prpCitemKindsTemp%5B3%5D.endHour=&relateSpecial%5B3%5D=050928&coachCar%5B3%5D=050943&prpCitemKindsTemp%5B3%5D.flag= 100000&prpCitemKindsTemp%5B3%5D.basePremium=&prpCitemKindsTemp%5B3%5D.rate=&prpCitemKindsTemp%5B3%5D.benchMarkPremium=&prpCitemKindsTemp%5B3%5D.disCount=&prpCitemKindsTemp%5B3%5D.premium=&prpCitemKindsTemp%5B3%5D.netPremium=&prpCitemKindsTemp%5B3%5D.taxPremium=&prpCitemKindsTemp%5B3%5D.taxRate=&prpCitemKindsTemp%5B3%5D.dutyFlag=&prpCitemKindsTemp%5B4%5D.min=&prpCitemKindsTemp%5B4%5D.max=&prpCitemKindsTemp%5B4%5D.itemKindNo=&prpCitemKindsTemp%5B4%5D.clauseCode=050004&prpCitemKindsTemp%5B4%5D.kindCode=050702&prpCitemKindsTemp%5B4%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9&prpCitemKindsTemp%5B4%5D.unitAmount=&prpCitemKindsTemp%5B4%5D.quantity=&prpCitemKindsTemp%5B4%5D.amount=&prpCitemKindsTemp%5B4%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B4%5D.startDate=&prpCitemKindsTemp%5B4%5D.startHour=&prpCitemKindsTemp%5B4%5D.endDate=&prpCitemKindsTemp%5B4%5D.endHour=&relateSpecial%5B4%5D=050929&coachCar%5B4%5D=050944&prpCitemKindsTemp%5B4%5D.flag= 100000&prpCitemKindsTemp%5B4%5D.basePremium=&prpCitemKindsTemp%5B4%5D.rate=&prpCitemKindsTemp%5B4%5D.benchMarkPremium=&prpCitemKindsTemp%5B4%5D.disCount=&prpCitemKindsTemp%5B4%5D.premium=&prpCitemKindsTemp%5B4%5D.netPremium=&prpCitemKindsTemp%5B4%5D.taxPremium=&prpCitemKindsTemp%5B4%5D.taxRate=&prpCitemKindsTemp%5B4%5D.dutyFlag=&prpCitemKindsTemp%5B5%5D.min=&prpCitemKindsTemp%5B5%5D.max=&prpCitemKindsTemp%5B5%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B5%5D.clauseCode=050006&prpCitemKindsTemp%5B5%5D.kindCode=050210&relateSpecial%5B5%5D=050922&prpCitemKindsTemp%5B5%5D.kindName=%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B5%5D.amount=2000.00&prpCitemKindsTemp%5B5%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B5%5D.startDate=&prpCitemKindsTemp%5B5%5D.startHour=&prpCitemKindsTemp%5B5%5D.endDate=&prpCitemKindsTemp%5B5%5D.endHour=&prpCitemKindsTemp%5B5%5D.flag= 200000&prpCitemKindsTemp%5B5%5D.basePremium=&prpCitemKindsTemp%5B5%5D.rate=&prpCitemKindsTemp%5B5%5D.benchMarkPremium=&prpCitemKindsTemp%5B5%5D.disCount=&prpCitemKindsTemp%5B5%5D.premium=&prpCitemKindsTemp%5B5%5D.netPremium=&prpCitemKindsTemp%5B5%5D.taxPremium=&prpCitemKindsTemp%5B5%5D.taxRate=&prpCitemKindsTemp%5B5%5D.dutyFlag=&prpCitemKindsTemp%5B6%5D.min=&prpCitemKindsTemp%5B6%5D.max=&prpCitemKindsTemp%5B6%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B6%5D.clauseCode=050008&prpCitemKindsTemp%5B6%5D.kindCode=050231&relateSpecial%5B6%5D=      &prpCitemKindsTemp%5B6%5D.kindName=%B2%A3%C1%A7%B5%A5%B6%C0%C6%C6%CB%E9%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B6%5D.modeCode=10&prpCitemKindsTemp%5B6%5D.amount=&prpCitemKindsTemp%5B6%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B6%5D.startDate=&prpCitemKindsTemp%5B6%5D.startHour=&prpCitemKindsTemp%5B6%5D.endDate=&prpCitemKindsTemp%5B6%5D.endHour=&prpCitemKindsTemp%5B6%5D.flag= 200000&prpCitemKindsTemp%5B6%5D.basePremium=&prpCitemKindsTemp%5B6%5D.rate=&prpCitemKindsTemp%5B6%5D.benchMarkPremium=&prpCitemKindsTemp%5B6%5D.disCount=&prpCitemKindsTemp%5B6%5D.premium=&prpCitemKindsTemp%5B6%5D.netPremium=&prpCitemKindsTemp%5B6%5D.taxPremium=&prpCitemKindsTemp%5B6%5D.taxRate=&prpCitemKindsTemp%5B6%5D.dutyFlag=&prpCitemKindsTemp%5B7%5D.min=&prpCitemKindsTemp%5B7%5D.max=&prpCitemKindsTemp%5B7%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B7%5D.clauseCode=050016&prpCitemKindsTemp%5B7%5D.kindCode=050310&relateSpecial%5B7%5D=      &prpCitemKindsTemp%5B7%5D.kindName=%D7%D4%C8%BC%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B7%5D.amount=&prpCitemKindsTemp%5B7%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B7%5D.startDate=&prpCitemKindsTemp%5B7%5D.startHour=&prpCitemKindsTemp%5B7%5D.endDate=&prpCitemKindsTemp%5B7%5D.endHour=&prpCitemKindsTemp%5B7%5D.flag= 200000&prpCitemKindsTemp%5B7%5D.basePremium=&prpCitemKindsTemp%5B7%5D.rate=&prpCitemKindsTemp%5B7%5D.benchMarkPremium=&prpCitemKindsTemp%5B7%5D.disCount=&prpCitemKindsTemp%5B7%5D.premium=&prpCitemKindsTemp%5B7%5D.netPremium=&prpCitemKindsTemp%5B7%5D.taxPremium=&prpCitemKindsTemp%5B7%5D.taxRate=&prpCitemKindsTemp%5B7%5D.dutyFlag=&prpCitemKindsTemp%5B8%5D.min=&prpCitemKindsTemp%5B8%5D.max=&prpCitemKindsTemp%5B8%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B8%5D.clauseCode=050021&prpCitemKindsTemp%5B8%5D.kindCode=050370&relateSpecial%5B8%5D=      &prpCitemKindsTemp%5B8%5D.kindName=%D4%BC%B6%A8%C7%F8%D3%F2%CD%A8%D0%D0%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B8%5D.modeCode=1&prpCitemKindsTemp%5B8%5D.amount=5000.00&prpCitemKindsTemp%5B8%5D.calculateFlag=N12N000&prpCitemKindsTemp%5B8%5D.startDate=&prpCitemKindsTemp%5B8%5D.startHour=&prpCitemKindsTemp%5B8%5D.endDate=&prpCitemKindsTemp%5B8%5D.endHour=&prpCitemKindsTemp%5B8%5D.flag= 200000&prpCitemKindsTemp%5B8%5D.basePremium=&prpCitemKindsTemp%5B8%5D.rate=&prpCitemKindsTemp%5B8%5D.benchMarkPremium=&prpCitemKindsTemp%5B8%5D.disCount=&prpCitemKindsTemp%5B8%5D.premium=&prpCitemKindsTemp%5B8%5D.netPremium=&prpCitemKindsTemp%5B8%5D.taxPremium=&prpCitemKindsTemp%5B8%5D.taxRate=&prpCitemKindsTemp%5B8%5D.dutyFlag=&prpCitemKindsTemp%5B9%5D.min=&prpCitemKindsTemp%5B9%5D.max=&prpCitemKindsTemp%5B9%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B9%5D.clauseCode=050032&prpCitemKindsTemp%5B9%5D.kindCode=050611&relateSpecial%5B9%5D=      &prpCitemKindsTemp%5B9%5D.kindName=%B7%A8%C2%C9%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B9%5D.amount=10000.00&prpCitemKindsTemp%5B9%5D.calculateFlag=N22N000&prpCitemKindsTemp%5B9%5D.startDate=&prpCitemKindsTemp%5B9%5D.startHour=&prpCitemKindsTemp%5B9%5D.endDate=&prpCitemKindsTemp%5B9%5D.endHour=&prpCitemKindsTemp%5B9%5D.flag= 200000&prpCitemKindsTemp%5B9%5D.basePremium=&prpCitemKindsTemp%5B9%5D.rate=&prpCitemKindsTemp%5B9%5D.benchMarkPremium=&prpCitemKindsTemp%5B9%5D.disCount=&prpCitemKindsTemp%5B9%5D.premium=&prpCitemKindsTemp%5B9%5D.netPremium=&prpCitemKindsTemp%5B9%5D.taxPremium=&prpCitemKindsTemp%5B9%5D.taxRate=&prpCitemKindsTemp%5B9%5D.dutyFlag=&prpCitemKindsTemp%5B10%5D.min=&prpCitemKindsTemp%5B10%5D.max=&prpCitemKindsTemp%5B10%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B10%5D.clauseCode=050033&prpCitemKindsTemp%5B10%5D.kindCode=050630&relateSpecial%5B10%5D=050926&prpCitemKindsTemp%5B10%5D.kindName=%B8%BD%BC%D3%D3%CD%CE%DB%CE%DB%C8%BE%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B10%5D.amount=50000.00&prpCitemKindsTemp%5B10%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B10%5D.startDate=&prpCitemKindsTemp%5B10%5D.startHour=&prpCitemKindsTemp%5B10%5D.endDate=&prpCitemKindsTemp%5B10%5D.endHour=&prpCitemKindsTemp%5B10%5D.flag= 200000&prpCitemKindsTemp%5B10%5D.basePremium=&prpCitemKindsTemp%5B10%5D.rate=&prpCitemKindsTemp%5B10%5D.benchMarkPremium=&prpCitemKindsTemp%5B10%5D.disCount=&prpCitemKindsTemp%5B10%5D.premium=&prpCitemKindsTemp%5B10%5D.netPremium=&prpCitemKindsTemp%5B10%5D.taxPremium=&prpCitemKindsTemp%5B10%5D.taxRate=&prpCitemKindsTemp%5B10%5D.dutyFlag=&prpCitemKindsTemp.itemKindSpecialSumPremium=&hidden_index_itemKind=11&hidden_index_profitDetial=0&prpCitemKindsTemp_%5B0%5D.chooseFlag=on&prpCitemKindsTemp_%5B0%5D.itemKindNo=&prpCitemKindsTemp_%5B0%5D.startDate=&prpCitemKindsTemp_%5B0%5D.kindCode=&prpCitemKindsTemp_%5B0%5D.kindName=&prpCitemKindsTemp_%5B0%5D.startHour=&prpCitemKindsTemp_%5B0%5D.endDate=&prpCitemKindsTemp_%5B0%5D.endHour=&prpCitemKindsTemp_%5B0%5D.calculateFlag=&relateSpecial_%5B0%5D=&prpCitemKindsTemp_%5B0%5D.flag=&prpCitemKindsTemp_%5B0%5D.basePremium=&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.rate=&prpCitemKindsTemp_%5B0%5D.benchMarkPremium=&prpCitemKindsTemp_%5B0%5D.disCount=&prpCitemKindsTemp_%5B0%5D.premium=&prpCitemKindsTemp_%5B0%5D.netPremium=&prpCitemKindsTemp_%5B0%5D.taxPremium=&prpCitemKindsTemp_%5B0%5D.taxRate=&prpCitemKindsTemp_%5B0%5D.dutyFlag=&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.value=&prpCitemKindsTemp_%5B0%5D.value=50&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.modeCode=10&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.value=1000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=10000&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=60&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=90&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.amount=50000.00&prpCitemKindsTemp_%5B0%5D.amount=10000.00&prpCitemKindsTemp_%5B0%5D.amount=5000.00&itemKindLoadFlag=&BIdemandNo=&BIdemandTime=&bIRiskWarningType=&noDamageYearsBIPlat=0&prpCitemCarExt.lastDamagedBI=&lastDamagedBITemp=&DAZlastDamagedBI=&prpCitemCarExt.thisDamagedBI=0&prpCitemCarExt.noDamYearsBI=0&noDamYearsBINumber=0&prpCitemCarExt.lastDamagedCI=0&BIDemandClaim_Flag=&BiInsureDemandPay_%5B0%5D.id.serialNo=&BiInsureDemandPay_%5B0%5D.payCompany=&BiInsureDemandPay_%5B0%5D.claimregistrationno=&BiInsureDemandPay_%5B0%5D.compensateNo=&BiInsureDemandPay_%5B0%5D.lossTime=&BiInsureDemandPay_%5B0%5D.endcCaseTime=&PrpCmain_%5B0%5D.startDate=&PrpCmain_%5B0%5D.endDate=&BiInsureDemandPay_%5B0%5D.lossFee=&BiInsureDemandPay_%5B0%5D.payType=&BiInsureDemandPay_%5B0%5D.personpayType=&bIRiskWarningClaimItems_%5B0%5D.id.serialNo=&bIRiskWarningClaimItems_%5B0%5D.riskWarningType=&bIRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&bIRiskWarningClaimItems_%5B0%5D.insurerCode=&bIRiskWarningClaimItems_%5B0%5D.lossTime=&bIRiskWarningClaimItems_%5B0%5D.lossArea=&prpCitemKindCI.shortRate=100&cIBPFlag=1&prpCitemKindCI.unitAmount=0&prpCitemKindCI.id.itemKindNo=&prpCitemKindCI.kindCode=050100&prpCitemKindCI.kindName=%BB%FA%B6%AF%B3%B5%BD%BB%CD%A8%CA%C2%B9%CA%C7%BF%D6%C6%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindCI.calculateFlag=Y&prpCitemKindCI.basePremium=&prpCitemKindCI.quantity=1&prpCitemKindCI.amount=&prpCitemKindCI.deductible=&prpCitemKindCI.adjustRate=1&prpCitemKindCI.rate=0&prpCitemKindCI.benchMarkPremium=&prpCitemKindCI.disCount=1&prpCitemKindCI.premium=&prpCitemKindCI.flag=&prpCitemKindCI.netPremium=&prpCitemKindCI.taxPremium=&prpCitemKindCI.taxRate=&prpCitemKindCI.dutyFlag=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCitemCarExt_CI.rateRloatFlag=01&prpCitemCarExt_CI.noDamYearsCI=1&prpCitemCarExt_CI.lastDamagedCI=0&prpCitemCarExt_CI.flag=&prpCitemCarExt_CI.damFloatRatioCI=0&prpCitemCarExt_CI.offFloatRatioCI=0&prpCitemCarExt_CI.thisDamagedCI=0&prpCitemCarExt_CI.flag=&hidden_index_ctraffic_NOPlat_Drink=0&hidden_index_ctraffic_NOPlat=0&ciInsureDemand.demandNo=&ciInsureDemand.demandTime=&ciInsureDemand.restricFlag=&ciInsureDemand.preferentialDay=&ciInsureDemand.preferentialPremium=&ciInsureDemand.preferentialFormula%20=&ciInsureDemand.lastyearenddate=&prpCitemCar.noDamageYears=0&ciInsureDemand.rateRloatFlag=00&ciInsureDemand.claimAdjustReason=A1&ciInsureDemand.peccancyAdjustReason=V1&cIRiskWarningType=&CIDemandFecc_Flag=&ciInsureDemandLoss_%5B0%5D.id.serialNo=&ciInsureDemandLoss_%5B0%5D.lossTime=&ciInsureDemandLoss_%5B0%5D.lossDddress=&ciInsureDemandLoss_%5B0%5D.lossAction=&ciInsureDemandLoss_%5B0%5D.coeff=&ciInsureDemandLoss_%5B0%5D.lossType=&ciInsureDemandLoss_%5B0%5D.identifyType=&ciInsureDemandLoss_%5B0%5D.identifyNumber=&ciInsureDemandLoss_%5B0%5D.lossAcceptDate=&ciInsureDemandLoss_%5B0%5D.processingStatus=&ciInsureDemandLoss_%5B0%5D.lossActionDesc=&CIDemandClaim_Flag=&ciInsureDemandPay_%5B0%5D.id.serialNo=&ciInsureDemandPay_%5B0%5D.payCompany=&ciInsureDemandPay_%5B0%5D.claimregistrationno=&ciInsureDemandPay_%5B0%5D.compensateNo=&ciInsureDemandPay_%5B0%5D.lossTime=&ciInsureDemandPay_%5B0%5D.endcCaseTime=&ciInsureDemandPay_%5B0%5D.lossFee=&ciInsureDemandPay_%5B0%5D.payType=&ciInsureDemandPay_%5B0%5D.personpayType=&ciRiskWarningClaimItems_%5B0%5D.id.serialNo=&ciRiskWarningClaimItems_%5B0%5D.riskWarningType=&ciRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&ciRiskWarningClaimItems_%5B0%5D.insurerCode=&ciRiskWarningClaimItems_%5B0%5D.lossTime=&ciRiskWarningClaimItems_%5B0%5D.lossArea=&ciInsureDemand.licenseNo=&ciInsureDemand.licenseType=&ciInsureDemand.useNatureCode=&ciInsureDemand.frameNo=&ciInsureDemand.engineNo=&ciInsureDemand.licenseColorCode=&ciInsureDemand.carOwner=&ciInsureDemand.enrollDate=&ciInsureDemand.makeDate=&ciInsureDemand.seatCount=&ciInsureDemand.tonCount=&ciInsureDemand.validCheckDate=&ciInsureDemand.manufacturerName=&ciInsureDemand.modelCode=&ciInsureDemand.brandCName=&ciInsureDemand.brandName=&ciInsureDemand.carKindCode=&ciInsureDemand.checkDate=&ciInsureDemand.endValidDate=&ciInsureDemand.carStatus=&ciInsureDemand.haulage=&AccidentFlag=&rateFloatFlag=ND4&prpCtrafficRecordTemps_%5B0%5D.id.serialNo=&prpCtrafficRecordTemps_%5B0%5D.accidentDate=&prpCtrafficRecordTemps_%5B0%5D.claimDate=&hidden_index_ctraffic=0&_taxUnit=&taxPlatFormTime=2012-04-21&iniPrpCcarShipTax_Flag=&strCarShipFlag=1&prpCcarShipTax.taxType=1&prpCcarShipTax.calculateMode=C1&prpCcarShipTax.leviedDate=&prpCcarShipTax.carKindCode=A01&prpCcarShipTax.model=B11&prpCcarShipTax.taxPayerIdentNo=&prpCcarShipTax.taxPayerNumber=&prpCcarShipTax.carLotEquQuality=&prpCcarShipTax.taxPayerCode=&prpCcarShipTax.id.itemNo=1&prpCcarShipTax.taxPayerNature=3&prpCcarShipTax.taxPayerName=&prpCcarShipTax.taxUnit=&prpCcarShipTax.taxComCode=&prpCcarShipTax.taxComName=&prpCcarShipTax.taxExplanation=&prpCcarShipTax.taxAbateReason=&prpCcarShipTax.dutyPaidProofNo_1=&prpCcarShipTax.dutyPaidProofNo_2=&prpCcarShipTax.dutyPaidProofNo=&prpCcarShipTax.taxAbateRate=&prpCcarShipTax.taxAbateAmount=&prpCcarShipTax.taxAbateType=1&prpCcarShipTax.taxUnitAmount=&prpCcarShipTax.prePayTaxYear=&prpCcarShipTax.prePolicyEndDate=&prpCcarShipTax.payStartDate=&prpCcarShipTax.payEndDate=&prpCcarShipTax.thisPayTax=&prpCcarShipTax.prePayTax=&prpCcarShipTax.taxItemCode=&prpCcarShipTax.taxItemName=&prpCcarShipTax.baseTaxation=&prpCcarShipTax.taxRelifFlag=&prpCcarShipTax.delayPayTax=&prpCcarShipTax.sumPayTax=&CarShipInit_Flag=&prpCcarShipTax.flag=&quotationtaxPayerCode=&noBringOutEngage=&prpCengageTemps_%5B0%5D.id.serialNo=&prpCengageTemps_%5B0%5D.clauseCode=&prpCengageTemps_%5B0%5D.clauseName=&clauses_%5B0%5D=&prpCengageTemps_%5B0%5D.flag=&prpCengageTemps_%5B0%5D.engageFlag=&prpCengageTemps_%5B0%5D.maxCount=&prpCengageTemps_%5B0%5D.clauses=&iniPrpCengage_Flag=&hidden_index_engage=0&costRateForPG=&certificateNo=&levelMaxRate=&maxRateScm=&levelMaxRateCi=&maxRateScmCi=&isModifyBI=&isModifyCI=&sumBICoinsRate=&sumCICoinsRate=&agentsRateBI=&agentsRateCI=&prpVisaRecordP.id.visaNo=&prpVisaRecordP.id.visaCode=&prpVisaRecordP.visaName=&prpVisaRecordP.printType=101&prpVisaRecordT.id.visaNo=&prpVisaRecordT.id.visaCode=&prpVisaRecordT.visaName=&prpVisaRecordT.printType=103&prpCmain.sumAmount=&prpCmain.sumDiscount=&prpCstampTaxBI.biTaxRate=&prpCstampTaxBI.biPayTax=&prpCmain.sumPremium=&prpVisaRecordPCI.id.visaNo=&prpVisaRecordPCI.id.visaCode=&prpVisaRecordPCI.visaName=&prpVisaRecordPCI.printType=201&prpVisaRecordTCI.id.visaNo=&prpVisaRecordTCI.id.visaCode=&prpVisaRecordTCI.visaName=&prpVisaRecordTCI.printType=203&prpCmainCI.sumAmount=&prpCmainCI.sumDiscount=&prpCstampTaxCI.ciTaxRate=&prpCstampTaxCI.ciPayTax=&prpCmainCI.sumPremium=&prpCmainCar.rescueFundRate=&prpCmainCar.resureFundFee=&prpCmain.projectCode=&projectCode=&costRateUpper=&prpCmainCommon.ext3=&importantProjectCode=&prpCmain.operatorCode=020083&operatorName=%D6%DA%BA%CF%CB%C4%BA%A3&operateDateShow=&prpCmain.coinsFlag=00&coinsFlagBak=00&premium=&prpCmain.language=CNY&prpCmain.policySort=1&prpCmain.policyRelCode=&prpCmain.policyRelName=&subsidyRate=&policyRel=&prpCmain.reinsFlag=0&prpCmain.agriFlag=0&premium=&prpCmainCar.carCheckStatus=0&prpCmainCar.carChecker=&carCheckerTranslate=&prpCmainCar.carCheckTime=&prpCmainCommon.DBCFlag=0&prpCmain.argueSolution=1&prpCmain.arbitBoardName=&arbitBoardNameDes=&prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&hidden_index_commission=0&scmIsOpen=1111100000&prpCagents_%5B0%5D.roleType=&roleTypeName_%5B0%5D=&prpCagents_%5B0%5D.id.roleCode=&prpCagents_%5B0%5D.roleCode_uni=&prpCagents_%5B0%5D.roleName=&prpCagents_%5B0%5D.costRate=&prpCagents_%5B0%5D.costFee=&prpCagents_%5B0%5D.flag=&prpCagents_%5B0%5D.businessNature=&prpCagents_%5B0%5D.isMain=&prpCagentCIs_%5B0%5D.roleType=&roleTypeNameCI_%5B0%5D=&prpCagentCIs_%5B0%5D.id.roleCode=&prpCagentCIs_%5B0%5D.roleCode_uni=&prpCagentCIs_%5B0%5D.roleName=&prpCagentCIs_%5B0%5D.costRate=&prpCagentCIs_%5B0%5D.costFee=&prpCagentCIs_%5B0%5D.flag=&prpCagentCIs_%5B0%5D.businessNature=&prpCagentCIs_%5B0%5D.isMain=&commissionCount=&prpCsaless_%5B0%5D.salesDetailName=&prpCsaless_%5B0%5D.riskCode=&prpCsaless_%5B0%5D.splitRate=&prpCsaless_%5B0%5D.oriSplitNumber=&prpCsaless_%5B0%5D.splitFee=&prpCsaless_%5B0%5D.agreementNo=&prpCsaless_%5B0%5D.id.salesCode=&prpCsaless_%5B0%5D.salesName=&prpCsaless_%5B0%5D.id.proposalNo=&prpCsaless_%5B0%5D.id.salesDetailCode=&prpCsaless_%5B0%5D.totalRate=&prpCsaless_%5B0%5D.splitWay=&prpCsaless_%5B0%5D.totalRateMax=&prpCsaless_%5B0%5D.flag=&prpCsaless_%5B0%5D.remark=&commissionPower=&hidden_index_prpCsales=0&prpCsalesDatils_%5B0%5D.id.salesCode=&prpCsalesDatils_%5B0%5D.id.proposalNo=&prpCsalesDatils_%5B0%5D.id.%20%20=&prpCsalesDatils_%5B0%5D.id.roleType=&prpCsalesDatils_%5B0%5D.id.roleCode=&prpCsalesDatils_%5B0%5D.currency=&prpCsalesDatils_%5B0%5D.splitDatilRate=&prpCsalesDatils_%5B0%5D.splitDatilFee=&prpCsalesDatils_%5B0%5D.roleName=&prpCsalesDatils_%5B0%5D.splitWay=&prpCsalesDatils_%5B0%5D.flag=&prpCsalesDatils_%5B0%5D.remark=&hidden_index_prpCsalesDatil=0&csManageSwitch=1&prpCmainChannel.agentCode=&prpCmainChannel.agentName=&prpCmainChannel.organCode=&prpCmainChannel.organCName=&comCodeType=&prpCmainChannel.identifyNumber=&prpCmainChannel.identifyType=&prpCmainChannel.manOrgCode=&prpCmain.remark=&prpDdismantleDetails_%5B0%5D.id.agreementNo=&prpDdismantleDetails_%5B0%5D.flag=&prpDdismantleDetails_%5B0%5D.id.configCode=&prpDdismantleDetails_%5B0%5D.id.assignType=&prpDdismantleDetails_%5B0%5D.id.roleCode=&prpDdismantleDetails_%5B0%5D.roleName=&prpDdismantleDetails_%5B0%5D.costRate=&prpDdismantleDetails_%5B0%5D.roleFlag=&prpDdismantleDetails_%5B0%5D.businessNature=&prpDdismantleDetails_%5B0%5D.roleCode_uni=&hidden_index_prpDdismantleDetails=0&payTimes=1&prpCplanTemps_%5B0%5D.payNo=&prpCplanTemps_%5B0%5D.serialNo=&prpCplanTemps_%5B0%5D.endorseNo=&cplan_%5B0%5D.payReasonC=&prpCplanTemps_%5B0%5D.payReason=&prpCplanTemps_%5B0%5D.planDate=&prpCplanTemps_%5B0%5D.currency=&description_%5B0%5D.currency=&prpCplanTemps_%5B0%5D.planFee=&cplans_%5B0%5D.planFee=&cplans_%5B0%5D.backPlanFee=&prpCplanTemps_%5B0%5D.netPremium=&prpCplanTemps_%5B0%5D.taxPremium=&prpCplanTemps_%5B0%5D.delinquentFee=&prpCplanTemps_%5B0%5D.flag=&prpCplanTemps_%5B0%5D.subsidyRate=&prpCplanTemps_%5B0%5D.isBICI=&iniPrpCplan_Flag=&loadFlag9=&planfee_index=0&planStr=&planPayTimes=&prpCmainCar.flag=1&prpCmainCarFlag=1&coinsSchemeCode=&coinsSchemeName=&mainPolicyNo=&prpCcoinsMains_%5B0%5D.id.serialNo=1&prpCcoinsMains_%5B0%5D.coIdentity=1&prpCcoinsMains_%5B0%5D.coinsCode=002&prpCcoinsMains_%5B0%5D.coinsName=%C8%CB%B1%A3%B2%C6%B2%FA&prpCcoinsMains_%5B0%5D.coinsRate=&prpCcoinsMains_%5B0%5D.id.currency=CNY&prpCcoinsMains_%5B0%5D.coinsAmount=&prpCcoinsMains_%5B0%5D.coinsPremium=&prpCcoinsMains_%5B0%5D.coinsPremium=&iniPrpCcoins_Flag=&hidden_index_ccoins=0&prpCpayeeAccountBIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountBIs_%5B0%5D.id.serialNo=&prpCpayeeAccountBIs_%5B0%5D.itemNo=&prpCpayeeAccountBIs_%5B0%5D.payReason=&prpCpayeeAccountBIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountBIs_%5B0%5D.accountName=&prpCpayeeAccountBIs_%5B0%5D.basicBankCode=&prpCpayeeAccountBIs_%5B0%5D.basicBankName=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountBIs_%5B0%5D.bankCode=&prpCpayeeAccountBIs_%5B0%5D.bankName=&prpCpayeeAccountBIs_%5B0%5D.cnaps=&prpCpayeeAccountBIs_%5B0%5D.accountNo=&prpCpayeeAccountBIs_%5B0%5D.isPrivate=&prpCpayeeAccountBIs_%5B0%5D.cardType=&prpCpayeeAccountBIs_%5B0%5D.paySumFee=&prpCpayeeAccountBIs_%5B0%5D.payType=&prpCpayeeAccountBIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountBIs_%5B0%5D.sendSms=&prpCpayeeAccountBIs_%5B0%5D.identifyType=&prpCpayeeAccountBIs_%5B0%5D.identifyNo=&prpCpayeeAccountBIs_%5B0%5D.telephone=&prpCpayeeAccountBIs_%5B0%5D.sendMail=&prpCpayeeAccountBIs_%5B0%5D.mailAddr=&prpCpayeeAccountCIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountCIs_%5B0%5D.id.serialNo=&prpCpayeeAccountCIs_%5B0%5D.itemNo=&prpCpayeeAccountCIs_%5B0%5D.payReason=&prpCpayeeAccountCIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountCIs_%5B0%5D.accountName=&prpCpayeeAccountCIs_%5B0%5D.basicBankCode=&prpCpayeeAccountCIs_%5B0%5D.basicBankName=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountCIs_%5B0%5D.bankCode=&prpCpayeeAccountCIs_%5B0%5D.bankName=&prpCpayeeAccountCIs_%5B0%5D.cnaps=&prpCpayeeAccountCIs_%5B0%5D.accountNo=&prpCpayeeAccountCIs_%5B0%5D.isPrivate=&prpCpayeeAccountCIs_%5B0%5D.cardType=&prpCpayeeAccountCIs_%5B0%5D.paySumFee=&prpCpayeeAccountCIs_%5B0%5D.payType=&prpCpayeeAccountCIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountCIs_%5B0%5D.sendSms=&prpCpayeeAccountCIs_%5B0%5D.identifyType=&prpCpayeeAccountCIs_%5B0%5D.identifyNo=&prpCpayeeAccountCIs_%5B0%5D.telephone=&prpCpayeeAccountCIs_%5B0%5D.sendMail=&prpCpayeeAccountCIs_%5B0%5D.mailAddr=&iReinsCode=&prpCspecialFacs_%5B0%5D.reinsCode=001&iFReinsCode=&iPayCode=&iShareRate=&iCommRate=&iTaxRate=&iOthRate=&iCommission=&iOthPremium=&prpCspecialFacs_%5B0%5D.id.reinsNo=1&prpCspecialFacs_%5B0%5D.freinsCode=001&prpCspecialFacs_%5B0%5D.payCode=001&prpCspecialFacs_%5B0%5D.shareRate=001&prpCspecialFacs_%5B0%5D.sharePremium=001&prpCspecialFacs_%5B0%5D.commRate=001&prpCspecialFacs_%5B0%5D.taxRate=001&prpCspecialFacs_%5B0%5D.tax=001&prpCspecialFacs_%5B0%5D.othRate=001&prpCspecialFacs_%5B0%5D.commission=001&prpCspecialFacs_%5B0%5D.othPremium=001&prpCspecialFacs_%5B0%5D.reinsName=001&prpCspecialFacs_%5B0%5D.freinsName=001&prpCspecialFacs_%5B0%5D.payName=001&prpCspecialFacs_%5B0%5D.remark=001&prpCspecialFacs_%5B0%5D.flag=&hidden_index_specialFac=0&updateIndex=-1&iniCspecialFac_Flag=&_ReinsCode=&loadFlag8=&_FReinsCode=&_PayCode=&_ReinsName=&_FReinsName=&_PayName=&_CommRate=&_OthRate=&_ShareRate=&_Commission=&_OthPremium=&_SharePremium=&_TaxRate=&_Tax=&_Remark=&prpCsettlement.buyerUnitRank=3&prpCsettlement.buyerPreFee=&prpCsettlement.buyerUnitCode=&prpCsettlement.buyerUnitName=&prpCsettlement.upperUnitCode=&upperUnitName=&prpCsettlement.buyerUnitAddress=&prpCsettlement.buyerLinker=&prpCsettlement.buyerPhone=&prpCsettlement.buyerMobile=&prpCsettlement.buyerFax=&prpCsettlement.buyerUnitNature=1&prpCsettlement.buyerProvince=11000000&buyerProvinceDes=%C8%CB%B1%A3%B2%C6%CF%D5%B1%B1%BE%A9%CA%D0%B7%D6%B9%AB%CB%BE&prpCsettlement.buyerBusinessSort=01&prpCsettlement.comCname=&prpCsettlement.linkerCode=&linkerName=&linkerPhone=&linkerMobile=&linkerFax=&prpCsettlement.comCode=&prpCsettlement.fundForm=1&prpCsettlement.flag=&settlement_Flag=&prpCcontriutions_%5B0%5D.id.serialNo=1&prpCcontriutions_%5B0%5D.contribType=F&prpCcontriutions_%5B0%5D.contribCode=&prpCcontriutions_%5B0%5D.contribName=&prpCcontriutions_%5B0%5D.contribCode_uni=&prpCcontriutions_%5B0%5D.contribPercent=&prpCcontriutions_%5B0%5D.contribPremium=&prpCcontriutions_%5B0%5D.remark=&hidden_index_ccontriutions=0&userCode=020083&iProposalNo=&CProposalNo=&timeFlag=&prpCremarks_%5B0%5D.id.proposalNo=&prpCremarks_%5B0%5D.id.serialNo=&prpCremarks_%5B0%5D.operatorCode=020083&prpCremarks_%5B0%5D.remark=&prpCremarks_%5B0%5D.flag=&prpCremarks_%5B0%5D.insertTimeForHis=&hidden_index_remark=0&ciInsureDemandCheckVo.demandNo=&ciInsureDemandCheckVo.checkQuestion=&ciInsureDemandCheckVo.checkAnswer=&ciInsureDemandCheckVo.flag=DEMAND&ciInsureDemandCheckVo.riskCode=";
    	Request request1 = new Request();//第一步的参数
    	request1.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL +":8000/prpall/carInf/getDataFromCiCarInfo.do");
    	Map<String, String> map1 = new HashMap<>();
    	map1.put("param", param);
    	request1.setRequestParam(map1);
    	QuoteGetCarInfoPage getCarInfoPage = new QuoteGetCarInfoPage(1);
    	Response response1 = getCarInfoPage.run(request1);//第一步的返回结果
    	Map<String, Object> resultMap1 = response1.getResponseMap();
    	//第二部请求，根据上一步请求获取车辆其他信息
		param = param.replace("prpCitemCar.engineNo=", "prpCitemCar.engineNo=" + resultMap1.get("engineNo"));
		param = param.replace("prpCitemCar.frameNo=", "prpCitemCar.frameNo=" + resultMap1.get("vin"));
		param = param.replace("prpCitemCar.vinNo=", "prpCitemCar.vinNo=" + resultMap1.get("vin"));//vin
		param = param.replace("prpCitemCar.enrollDate=", "prpCitemCar.enrollDate=" + resultMap1.get("enrollDate"));
		param = param.replace("prpCitemCar.useYears=", "prpCitemCar.useYears=" + resultMap1.get("useYears"));
		param = param.replace("prpCitemCar.seatCount=", "prpCitemCar.seatCount="  + resultMap1.get("seatCount"));
		try {
			param = param.replace("prpCitemCar.modelCodeAlias=", "prpCitemCar.modelCodeAlias=" + java.net.URLEncoder.encode(resultMap1.get("modelCodeAlias").toString(),   "gb2312"));
		} catch (Exception e) {
			param = param.replace("prpCitemCar.modelCodeAlias=", "prpCitemCar.modelCodeAlias=");
		}
		Request request2 = new Request();//第二步的参数
    	request2.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL +":8000/prpall/carInf/getCarModelInfo.do");
    	Map<String, String> map2 = new HashMap<>();
    	map2.put("param", param);
    	request2.setRequestParam(map2);
    	QuoteGetCarInfoOtherPage getCarInfoOtherPage = new QuoteGetCarInfoOtherPage(1);
    	Response response2 = getCarInfoOtherPage.run(request2);//第二步的返回结果
    	Map<String, Object> resultMap2 = response2.getResponseMap();
    	//第三部请求，根据modelCode获取排量信息（可能带有验证）
    	Request request3= new Request();//第三步的参数
    	request3.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL +":8000/prpall/vehicle/findVehicleByVehicleId.do?vehicleId=" + resultMap2.get("modelCode"));
    	Map<String, String> map3 = new HashMap<>();
    	request3.setRequestParam(map3);
    	QuoteGetExhaustPage getExhaustPage = new QuoteGetExhaustPage(1);
    	Response response3 = getExhaustPage.run(request3);//第三步的返回结果
    	Map<String, Object> resultMap3 = response3.getResponseMap();
    	//TODO 前三部都可以做缓存
    	//第四部请求，根据以上结果返回折旧价格
    	param = param.replace("prpCitemCar.runMiles=","prpCitemCar.runMiles=10000");
		try {
			param = param.replace("owner=", "owner=" + java.net.URLEncoder.encode(resultMap2.get("owner").toString(),   "gb2312"));
		} catch (Exception e) {
			param = param.replace("prpCitemCar.modelCodeAlias=", "prpCitemCar.modelCodeAlias=");
		}
		param = param.replace("prpCitemCar.modelCode=","prpCitemCar.modelCode=" + resultMap2.get("modelCode"));
		try {
			param = param.replace("prpCitemCar.brandName=","prpCitemCar.brandName=" + java.net.URLEncoder.encode(resultMap2.get("brandName").toString(),   "gb2312"));
		} catch (Exception e) {
			param = param.replace("prpCitemCar.brandName=","prpCitemCar.brandName=");
		}
		param = param.replace("prpCitemCar.purchasePrice=","prpCitemCar.purchasePrice=" + resultMap2.get("purchasePrice"));
		param = param.replace("CarActualValueTrue=","CarActualValueTrue=" + resultMap2.get("purchasePrice"));
		param = param.replace("purchasePriceDown=","purchasePriceDown=" + resultMap2.get("purchasePrice"));
		param = param.replace("purchasePriceOld=","purchasePriceOld=" + resultMap2.get("purchasePrice"));
		param = param.replace("prpCitemCar.exhaustScale=","prpCitemCar.exhaustScale="  + resultMap3.get("vehicleExhaust"));
		param = param.replace("prpCitemCar.carLotEquQuality=","prpCitemCar.carLotEquQuality=" + resultMap2.get("wholeWeight"));
		param = param.replace("prpCcarShipTax.carLotEquQuality=","prpCcarShipTax.carLotEquQuality=" + resultMap2.get("wholeWeight"));
		Request request4= new Request();//第四步的参数
		request4.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL +":8000/prpall/business/calActualValue.do");
    	Map<String, String> map4 = new HashMap<>();
    	map4.put("param", param);
    	request4.setRequestParam(map4);
    	QuoteGetDepreciationPage quoteGetDepreciationPage = new QuoteGetDepreciationPage(1);
    	Response response4 = quoteGetDepreciationPage.run(request4);//第四步的返回结果
    	Map<String, Object> resultMap4 = response4.getResponseMap();
    	Map<String, Object> result = new HashMap<>();//车辆信息结果
    	result.putAll(resultMap4);
    	result.putAll(resultMap3);
    	result.putAll(resultMap2);
    	result.putAll(resultMap1);
    	result.put("param", param);
    	logger.info("人保  API接口，【根据品牌获取车辆信息完成】，车牌照：" + carNo1 + "，使用时间：" + ((System.currentTimeMillis() - startTime)/1000) + "S");
    	return result;
	}
	
	/**
	 * 拼装续保的报价参数
	 * @param carInfoMap
	 * @param quoteParam
	 * @return
	 */
	private String makeQuoteParam1(Map<String, Object> carInfoMap, Map<String, String> quoteParam){
		String param3 = "carShipTaxPlatFormFlag=&randomProposalNo=2334758541463991488035 &initemKind_Flag=1&editType=RENEWAL&bizType=PROPOSAL&ABflag=&isBICI=&prpCmain.renewalFlag=  &activityFlag=&INTEGRAL_SWITCH=0&GuangdongSysFlag=&GDREALTIMECARFlag=&GDREALTIMEMOTORFlag=&GDCANCIINFOFlag=0&prpCmain.checkFlag= &prpCmain.othFlag=110000YY00  &prpCmain.dmFlag=0&prpCmainCI.dmFlag=&prpCmain.underWriteCode=UnderWrite&prpCmain.underWriteName=%D7%D4%B6%AF%BA%CB%B1%A3            &prpCmain.underWriteEndDate=2015-06-25 13:30:34.0&prpCmain.underWriteFlag=3&prpCmainCI.checkFlag=&prpCmainCI.underWriteFlag=&bizNo=&applyNo=&oldPolicyNo=&bizNoBZ=&bizNoCI=&prpPhead.endorDate=&prpPhead.validDate=&prpPhead.comCode=&sumAmountBI=&isTaxDemand=1&cIInsureFlag=1&bIInsureFlag=1&ciInsureSwitchKindCode=E01,E11,E12,D01,D02,D03&ciInsureSwitchValues=1111111&cIInsureMotorFlag=1&mtPlatformTime=&noPermissionsCarKindCode=E12&isTaxFlag=1&rePolicyNo=&oldPolicyType=3&ZGRS_PURCHASEPRICE=&ZGRS_LOWESTPREMIUM=&clauseFlag=&prpCinsuredOwn_Flag=0&prpCinsuredDiv_Flag=0&prpCinsuredBon_Flag=0&relationType=&ciLimitDays=90&udFlag=0&kbFlag=0&sbFlag=0&xzFlag=0&userType=08&noNcheckFlag=0&planFlag=0&R_SWITCH=1&biStartDate=&ciStartDate=&ciStartHour=0&ciEndDate=&ciEndHour=24&AGENTSWITCH=1&JFCDSWITCH=19&carShipTaxFlag=11&commissionFlag=&ICCardCHeck=&riskWarningFlag=&comCodePrefix=11&DAGMobilePhoneNum=&scanSwitch=&haveScanFlag=0&diffDay=90&cylinderFlag=0&ciPlateVersion=&biPlateVersion=&criterionFlag=0&isQuotatonFlag=2&quotationRisk=DAA&getReplenishfactor=&useYear=9&FREEINSURANCEFLAG=011111&isMotoDrunkDriv=0&immediateFlag=0&immediateFlagCI=0&claimAmountReason=&isQueryCarModelFlag=&isDirectFee=&userCode=020083&comCode=11010286&chgProfitFlag=&ciPlatTask=&biPlatTask=&upperCostRateBI=&upperCostRateCI=&rescueFundRate=&resureFundFee=&useCarshiptaxFlag=1&taxFreeLicenseNo=&isTaxFree=0&premiumChangeFlag=1&operationTimeStamp=&VEHICLEPLAT=&MOTORFASTTRACK=&motorFastTrack_flag=&MOTORFASTTRACK_INSUREDCODE=&currentDate=&vinModifyFlag=&addPolicyProjectCode=&isAddPolicy=0&commissionView=0&specialflag=&accountCheck=2&projectBak=&projectCodeBT=&projectCodeBTback=&checkTimeFlag=&checkUndwrt=0&carDamagedNum=0&insurePayTimes=&claimAdjustValue=&operatorProjectCode=&lossFlagKind=&chooseFlagCI=0&unitedSaleRelatioStr=&purchasePriceU=&countryNatureU=&insurancefee_reform=0&operateDateForFG=&prpCmainCommon.clauseIssue=1&amountFloat=30&vat_switch=1&BiLastPolicyFlag=&CiLastPolicyFlag=&CiLastEffectiveDate=&CiLastExpireDate=&benchMarkPremium=&BiLastEffectiveDate=&BiLastExpireDate=&lastTotalPremium=&purchasePriceUFlag=&startDateU=&endDateU=&biCiFlagU=&biCiFlagIsChange=&biCiDateIsChange=&switchFlag=0&relatedFlag=0&riskCode=DAA&prpCmain.riskCode=DAA&riskName=&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&agentCodeValidType=U&agentCodeValidValue=106023BJ&agentCodeValidIPPer=&qualificationNo=201951000000800&qualificationName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&OLD_STARTDATE_CI=&OLD_ENDDATE_CI=&prpCmainCommon.greyList=&prpCmainCommon.image=&reinComPany=&reinPolicyNo=&reinStartDate=&reinEndDate=&prpCmain.proposalNo=&prpCmain.policyNo=&prpCmainCI.proposalNo=&prpCmainCI.policyNo=&prpPhead.applyNo=&prpPhead.endorseNo=&prpPheadCI.applyNo=&prpPheadCI.endorseNo=&prpCmain.comCode=11010286&comCodeDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.handler1Code=13154215  &handler1CodeDes=%BA%AB%B6%AB%D0%F1&homePhone=&officePhone=&moblie=&checkHandler1Code=1&handler1CodeDesFlag=&handler1Info=&prpCmainCommon.handler1code_uni=&prpCmain.handlerCode=13154215  &handlerCodeDes=%BA%AB%B6%AB%D0%F1&homePhonebak=&officePhonebak=&mobliebak=&handler1CodeDesFlagbak=&prpCmainCommon.handlercode_uni=&handlerInfo=&prpCmain.businessNature=2&businessNatureTranslation=%D7%A8%D2%B5%B4%FA%C0%ED%D2%B5%CE%F1&prpCmain.agentCode=110021100065&prpCmainagentName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&agentType=211047&agentCode=110021100065&tempAgentCode=211047&sumPremiumChgFlag=0&prpCmain.sumPremium1=0&sumPayTax1=0&prpCmain.contractNo=&prpCmain.operateDate=2016-05-23&Today=2016-05-23&OperateDate=&prpCmain.makeCom=11010286&makeComDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.startDate=&prpCmain.startHour=0&prpCmain.endDate=&prpCmain.endHour=24&prpCmain.checkUpCode=          &prpCmainCI.startDate=&prpCmainCI.startHour=0&prpCmainCI.endDate=&prpCmainCI.endHour=24&carPremium=0.0&insuredChangeFlag=0&refreshEadFlag=1&imageAdjustPixels=20&prpBatchVehicle.id.contractNo=&prpBatchVehicle.id.serialNo=&prpBatchVehicle.motorCadeNo=&prpBatchVehicle.licenseNo=&prpBatchVehicle.licenseType=&prpBatchVehicle.carKindCode=&prpBatchVehicle.proposalNo=&prpBatchVehicle.policyNo=&prpBatchVehicle.sumAmount=&prpBatchVehicle.sumPremium=&prpBatchVehicle.prpProjectCode=&prpBatchVehicle.coinsProjectCode=&prpBatchVehicle.profitProjectCode=&prpBatchVehicle.facProjectCode=&prpBatchVehicle.flag=&prpBatchVehicle.carId=&prpBatchVehicle.versionNo=&prpBatchMain.discountmode=&minusFlag=&paramIndex=&batchCIFlag=&batchBIFlag=&pageEndorRecorder.endorFlags=&endorDateEdit=&validDateEdit=&endDateEdit=&endorType=&prpPhead.endorType=&generatePtextFlag=0&generatePtextAgainFlag=0&quotationNo=&quotationFlag=&customerCode=&customerFlag=&compensateNo=&dilutiveType=&prpCfixationTemp.discount=&prpCfixationTemp.id.riskCode=&prpCfixationTemp.profits=&prpCfixationTemp.cost=&prpCfixationTemp.taxorAppend=&prpCfixationTemp.payMentR=&prpCfixationTemp.basePayMentR=&prpCfixationTemp.poundAge=&prpCfixationTemp.basePremium=&prpCfixationTemp.riskPremium=&prpCfixationTemp.riskSumPremium=&prpCfixationTemp.signPremium=&prpCfixationTemp.isQuotation=&prpCfixationTemp.riskClass=&prpCfixationTemp.operationInfo=&prpCfixationTemp.realDisCount=&prpCfixationTemp.realProfits=&prpCfixationTemp.realPayMentR=&prpCfixationTemp.remark=&prpCfixationTemp.responseCode=&prpCfixationTemp.errorMessage=&prpCfixationTemp.profitClass=&prpCfixationTemp.costRate=&prpCfixationCITemp.discount=&prpCfixationCITemp.id.riskCode=&prpCfixationCITemp.profits=&prpCfixationCITemp.cost=&prpCfixationCITemp.taxorAppend=&prpCfixationCITemp.payMentR=&prpCfixationCITemp.basePayMentR=&prpCfixationCITemp.poundAge=&prpCfixationCITemp.basePremium=&prpCfixationCITemp.riskPremium=&prpCfixationCITemp.riskSumPremium=&prpCfixationCITemp.signPremium=&prpCfixationCITemp.isQuotation=&prpCfixationCITemp.riskClass=&prpCfixationCITemp.operationInfo=&prpCfixationCITemp.realDisCount=&prpCfixationCITemp.realProfits=&prpCfixationCITemp.realPayMentR=&prpCfixationCITemp.remark=&prpCfixationCITemp.responseCode=&prpCfixationCITemp.errorMessage=&prpCfixationCITemp.profitClass=&prpCfixationCITemp.costRate=&prpCsalesFixes_%5B0%5D.id.proposalNo=&prpCsalesFixes_%5B0%5D.id.serialNo=&prpCsalesFixes_%5B0%5D.comCode=&prpCsalesFixes_%5B0%5D.businessNature=&prpCsalesFixes_%5B0%5D.riskCode=&prpCsalesFixes_%5B0%5D.version=&prpCsalesFixes_%5B0%5D.isForMal=&IS_LOAN_MODIFY=0&kindAndAmount=&isSpecialFlag=&specialEngage=&licenseNoCar=&prpCitemCar.carLoanFlag=0&carModelPlatFlag=&updateQuotation=&prpCitemCar.licenseNo1=          &prpCitemCar.monopolyFlag=0&prpCitemCar.monopolyCode=&prpCitemCar.monopolyName=&queryCarModelInfo=%B3%B5%D0%CD%D0%C5%CF%A2%C6%BD%CC%A8%BD%BB%BB%A5&prpCitemCar.id.itemNo=1&oldClauseType=F42&prpCitemCar.actualValue=&prpCitemCar.carId=          &prpCitemCar.versionNo=    &prpCmainCar.newDeviceFlag=&prpCitemCar.otherNature=&prpCitemCar.flag=    CC B  &newCarFlagValue=2&prpCitemCar.discountType=&prpCitemCar.colorCode=99    &prpCitemCar.safeDevice=                              &prpCitemCar.coefficient1=&prpCitemCar.coefficient2=&prpCitemCar.coefficient3=&prpCitemCar.startSiteName=&prpCitemCar.endSiteName=&prpCmainCommon.netsales=0&prpCitemCar.newCarFlag=0&prpCitemCar.noNlocalFlag=0&prpCitemCar.licenseFlag=1&prpCitemCar.licenseNo=&codeLicenseType=LicenseType01,04,LicenseType02,01,LicenseType03,02,LicenseType04,02,LicenseType05,02,LicenseType06,02,LicenseType07,04,LicenseType08,04,LicenseType09,01,LicenseType10,01,LicenseType11,01,LicenseType12,01,LicenseType13,04,LicenseType14,04,LicenseType15,04,	LicenseType16,04,LicenseType17,04,LicenseType18,01,LicenseType19,01,LicenseType20,01,LicenseType21,01,LicenseType22,01,LicenseType23,03,LicenseType24,01,LicenseType25,01,LicenseType31,03,LicenseType32,03,LicenseType90,02&prpCitemCar.licenseType=02&LicenseTypeDes=%D0%A1%D0%CD%C6%FB%B3%B5%BA%C5%C5%C6&prpCitemCar.licenseColorCode=01&LicenseColorCodeDes=%C0%B6&prpCitemCar.engineNo=&prpCitemCar.vinNo=&prpCitemCar.frameNo=&prpCitemCar.carKindCode=A01&CarKindCodeDes=%BF%CD%B3%B5&carKindCodeBak=A01&prpCitemCar.useNatureCode=211&useNatureCodeBak=211&useNatureCodeTrue=211&prpCitemCar.clauseType=F42&clauseTypeBak=F42&prpCitemCar.enrollDate=&enrollDateTrue=&prpCitemCar.useYears=&prpCitemCar.runMiles=10000.00&taxAbateForPlat=&taxAbateForPlatCarModel=&prpCitemCar.modelDemandNo=&owner=&prpCitemCar.remark=&prpCitemCar.modelCode=&prpCitemCar.brandName=&PurchasePriceScal=10&prpCitemCar.purchasePrice=&CarActualValueTrue=&CarActualValueTrue1=&SZpurchasePriceUp=&SZpurchasePriceDown=&purchasePriceF48=200000&purchasePriceUp=100&purchasePriceDown=&purchasePriceOld=&vehiclePricer=&prpCitemCar.tonCount=0&prpCitemCar.exhaustScale=&prpCitemCar.seatCount=&seatCountTrue=&prpCitemCar.runAreaCode=11&prpCitemCar.carInsuredRelation=1&prpCitemCar.countryNature=&prpCitemCar.cylinderCount=&prpCitemCar.loanVehicleFlag=0&prpCitemCar.transferVehicleFlag=0&prpCitemCar.transferDate=&prpCitemCar.modelCodeAlias=&prpCitemCar.carLotEquQuality=0.00&isQuotation=1&prpCitemCar.fuelType=A&prpCitemCar.carProofType=01&prpCitemCar.isDropinVisitInsure=0&prpCitemCar.energyType=0&prpCitemCar.carProofNo=                                                  &prpCitemCar.carProofDate=&prpCmainChannel.assetAgentName=&prpCmainChannel.assetAgentCode=&prpCmainChannel.assetAgentPhone=&SYFlag=0&MTFlag=0&BMFlag=0&STFlag=0&prpCcarDevices_%5B0%5D.deviceName=&prpCcarDevices_%5B0%5D.id.itemNo=1&prpCcarDevices_%5B0%5D.id.proposalNo=&prpCcarDevices_%5B0%5D.id.serialNo=&prpCcarDevices_%5B0%5D.flag=&prpCcarDevices_%5B0%5D.quantity=&prpCcarDevices_%5B0%5D.purchasePrice=&prpCcarDevices_%5B0%5D.buyDate=&prpCcarDevices_%5B0%5D.actualValue=&hidden_index_citemcar=0&editFlag=1&prpCmainCommon.ext2=&configedRepeatTimesLocal=5&prpCinsureds_%5B0%5D.insuredFlag=1&iinsuredFlag=001&iinsuredType=001&iinsuredCode=001&iinsuredName=001&iunitType=001&iidentifyType=001&iidentifyNumber=001&iinsuredAddress=001&iemail=001&iphoneNumber=001&prpCinsureds_%5B0%5D.id.serialNo=1&prpCinsureds_%5B0%5D.insuredType=1&prpCinsureds_%5B0%5D.insuredNature=1&prpCinsureds_%5B0%5D.insuredCode=001&prpCinsureds_%5B0%5D.insuredName=1&prpCinsureds_%5B0%5D.unitType=1&prpCinsureds_%5B0%5D.identifyType=1&prpCinsureds_%5B0%5D.identifyNumber=1&prpCinsureds_%5B0%5D.insuredAddress=1&prpCinsureds_%5B0%5D.email=1&prpCinsureds_%5B0%5D.phoneNumber=1&prpCinsureds_%5B0%5D.drivingYears=&prpCinsureds_%5B0%5D.mobile=1&prpCinsureds_%5B0%5D.postCode=1&prpCinsureds_%5B0%5D.versionNo=1&prpCinsureds_%5B0%5D.auditStatus=1&prpCinsureds_%5B0%5D.sex=1&prpCinsureds_%5B0%5D.countryCode=1&prpCinsureds_%5B0%5D.flag=&prpCinsureds_%5B0%5D.age=&prpCinsureds_%5B0%5D.drivingLicenseNo=&prpCinsureds_%5B0%5D.drivingCarType=&prpCinsureds_%5B0%5D.appendPrintName=&prpCinsureds_%5B0%5D.causetroubleTimes=&prpCinsureds_%5B0%5D.acceptLicenseDate=&isCheckRepeat_%5B0%5D=&configedRepeatTimes_%5B0%5D=&repeatTimes_%5B0%5D=&prpCinsureds_%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo_%5B0%5D.insuredcode=&idCardCheckInfo_%5B0%5D.insuredFlag=&idCardCheckInfo_%5B0%5D.mobile=&idCardCheckInfo_%5B0%5D.idcardCode=&idCardCheckInfo_%5B0%5D.name=&idCardCheckInfo_%5B0%5D.nation=&idCardCheckInfo_%5B0%5D.birthday=&idCardCheckInfo_%5B0%5D.sex=&idCardCheckInfo_%5B0%5D.address=&idCardCheckInfo_%5B0%5D.issure=&idCardCheckInfo_%5B0%5D.validStartDate=&idCardCheckInfo_%5B0%5D.validEndDate=&idCardCheckInfo_%5B0%5D.samCode=&idCardCheckInfo_%5B0%5D.samType=&idCardCheckInfo_%5B0%5D.flag=&imobile=001&iauditStatus=001&iversionNo=001&prpCinsureds%5B0%5D.insuredFlag=001000000000000000000000000000&display_insuredFlag=%B3%B5%D6%F7&prpCinsureds%5B0%5D.id.serialNo=0&display_InsuredNature=%B8%F6%C8%CB&prpCinsureds%5B0%5D.insuredType=1&prpCinsureds%5B0%5D.insuredNature=3&prpCinsureds%5B0%5D.insuredCode=&prpCinsureds%5B0%5D.insuredName=&unitTypeText=&prpCinsureds%5B0%5D.unitType=&display_identifyType=%C9%ED%B7%DD%D6%A4&prpCinsureds%5B0%5D.identifyType=&prpCinsureds%5B0%5D.identifyNumber=&prpCinsureds%5B0%5D.insuredAddress=...&prpCinsureds%5B0%5D.email=&phoneNumber%5B0%5D=18911***071&prpCinsureds%5B0%5D.phoneNumber=&prpCinsureds%5B0%5D.sex=&prpCinsureds%5B0%5D.drivingYears=&prpCinsureds%5B0%5D.postCode=100000&prpCinsureds%5B0%5D.versionNo=0&prpCinsureds%5B0%5D.auditStatus=&prpCinsureds%5B0%5D.countryCode=CHN&prpCinsureds%5B0%5D.flag=&prpCinsureds%5B0%5D.age=&prpCinsureds%5B0%5D.drivingLicenseNo=&prpCinsureds%5B0%5D.appendPrintName=&prpCinsureds%5B0%5D.drivingCarType=&reLoadFlag%5B0%5D=true&prpCinsureds%5B0%5D.causetroubleTimes=0&prpCinsureds%5B0%5D.acceptLicenseDate=&isCheckRepeat%5B0%5D=0&configedRepeatTimes%5B0%5D=&repeatTimes%5B0%5D=&prpCinsureds%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B0%5D.insuredcode=&idCardCheckInfo%5B0%5D.insuredFlag=&idCardCheckInfo%5B0%5D.mobile=&idCardCheckInfo%5B0%5D.idcardCode=&idCardCheckInfo%5B0%5D.name=&idCardCheckInfo%5B0%5D.nation=&idCardCheckInfo%5B0%5D.birthday=&idCardCheckInfo%5B0%5D.sex=&idCardCheckInfo%5B0%5D.address=&idCardCheckInfo%5B0%5D.issure=&idCardCheckInfo%5B0%5D.validStartDate=&idCardCheckInfo%5B0%5D.validEndDate=&idCardCheckInfo%5B0%5D.samCode=&idCardCheckInfo%5B0%5D.samType=&idCardCheckInfo%5B0%5D.flag=&mobile%5B0%5D=189****0047&prpCinsureds%5B0%5D.mobile=&prpCinsureds%5B1%5D.insuredFlag=11000000000000000000000000000A&display_insuredFlag=%CD%B6%B1%A3%C8%CB%2F%B1%BB%B1%A3%CF%D5%C8%CB&prpCinsureds%5B1%5D.id.serialNo=1&display_InsuredNature=%B8%F6%C8%CB&prpCinsureds%5B1%5D.insuredType=1&prpCinsureds%5B1%5D.insuredNature=3&prpCinsureds%5B1%5D.insuredCode=&prpCinsureds%5B1%5D.insuredName=&unitTypeText=&prpCinsureds%5B1%5D.unitType=&display_identifyType=%C9%ED%B7%DD%D6%A4&prpCinsureds%5B1%5D.identifyType=&prpCinsureds%5B1%5D.identifyNumber=&prpCinsureds%5B1%5D.insuredAddress=...&prpCinsureds%5B1%5D.email=&phoneNumber%5B1%5D=1***518&prpCinsureds%5B1%5D.phoneNumber=&prpCinsureds%5B1%5D.sex=&prpCinsureds%5B1%5D.drivingYears=&prpCinsureds%5B1%5D.postCode=100000&prpCinsureds%5B1%5D.versionNo=0&prpCinsureds%5B1%5D.auditStatus=&prpCinsureds%5B1%5D.countryCode=&prpCinsureds%5B1%5D.flag=&prpCinsureds%5B1%5D.age=&prpCinsureds%5B1%5D.drivingLicenseNo=&prpCinsureds%5B1%5D.appendPrintName=&prpCinsureds%5B1%5D.drivingCarType=&reLoadFlag%5B1%5D=true&prpCinsureds%5B1%5D.causetroubleTimes=0&prpCinsureds%5B1%5D.acceptLicenseDate=&isCheckRepeat%5B1%5D=0&configedRepeatTimes%5B1%5D=&repeatTimes%5B1%5D=&prpCinsureds%5B1%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B1%5D.insuredcode=&idCardCheckInfo%5B1%5D.insuredFlag=&idCardCheckInfo%5B1%5D.mobile=&idCardCheckInfo%5B1%5D.idcardCode=&idCardCheckInfo%5B1%5D.name=&idCardCheckInfo%5B1%5D.nation=&idCardCheckInfo%5B1%5D.birthday=&idCardCheckInfo%5B1%5D.sex=&idCardCheckInfo%5B1%5D.address=&idCardCheckInfo%5B1%5D.issure=&idCardCheckInfo%5B1%5D.validStartDate=&idCardCheckInfo%5B1%5D.validEndDate=&idCardCheckInfo%5B1%5D.samCode=&idCardCheckInfo%5B1%5D.samType=&idCardCheckInfo%5B1%5D.flag=&mobile%5B1%5D=189****0047&prpCinsureds%5B1%5D.mobile=&hidden_index_insured=2&_insuredFlag_hide=%CD%B6%B1%A3%C8%CB&_insuredFlag_hide=%B1%BB%B1%A3%CF%D5%C8%CB&_insuredFlag_hide=%B3%B5%D6%F7&_insuredFlag_hide=%D6%B8%B6%A8%BC%DD%CA%BB%C8%CB&_insuredFlag_hide=%CA%DC%D2%E6%C8%CB&_insuredFlag_hide=%B8%DB%B0%C4%B3%B5%B3%B5%D6%F7&_insuredFlag_hide=%C1%AA%CF%B5%C8%CB&_insuredFlag=0&_insuredFlag_hide=%CE%AF%CD%D0%C8%CB&_resident=&_insuredType=1&_insuredCode=&_insuredName=%D6%EC%BC%D1%BC%D1&customerURL=http://10.134.136.48:8300/cif&_isCheckRepeat=&_configedRepeatTimes=&_repeatTimes=&_identifyType=01&_identifyNumber=&_unifiedSocialCreditCode=&_mobile=&_mobile1=&_sex=0&_age=&_drivingYears=&_countryCode=CHN&_insuredAddress=&_postCode=&_appendPrintName=&group_code=&_auditStatus=&_auditStatusDes=&_versionNo=&_drivingLicenseNo=&_email=&idCardCheckInfo.idcardCode=&idCardCheckInfo.name=&idCardCheckInfo.nation=&idCardCheckInfo.birthday=&idCardCheckInfo.sex=&idCardCheckInfo.address=&idCardCheckInfo.issure=&idCardCheckInfo.validStartDate=&idCardCheckInfo.validEndDate=&idCardCheckInfo.samCode=&idCardCheckInfo.samType=&idCardCheckInfo.flag=0&_drivingCarType=&CarKindLicense=&_causetroubleTimes=&_acceptLicenseDate=&prpCmainCar.agreeDriverFlag=&updateIndex=-1&prpBatchProposal.profitType=&motorFastTrack_Amount=&insurancefee_reform=0&prpCmainCommon.clauseIssue=1&prpCprofitDetailsTemp_%5B0%5D.chooseFlag=&prpCprofitDetailsTemp_%5B0%5D.profitName=&prpCprofitDetailsTemp_%5B0%5D.condition=&profitRateTemp_%5B0%5D=&prpCprofitDetailsTemp_%5B0%5D.profitRate=&prpCprofitDetailsTemp_%5B0%5D.profitRateMin=&prpCprofitDetailsTemp_%5B0%5D.profitRateMax=&prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=&prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=&prpCprofitDetailsTemp_%5B0%5D.id.profitCode=&prpCprofitDetailsTemp_%5B0%5D.id.serialNo=1&prpCprofitDetailsTemp_%5B0%5D.id.profitType=&prpCprofitDetailsTemp_%5B0%5D.kindCode=&prpCprofitDetailsTemp_%5B0%5D.conditionCode=&prpCprofitDetailsTemp_%5B0%5D.flag=&prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on&serialNo_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.profitName=&prpCprofitFactorsTemp_%5B0%5D.condition=&rateTemp_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.rate=&prpCprofitFactorsTemp_%5B0%5D.lowerRate=&prpCprofitFactorsTemp_%5B0%5D.upperRate=&prpCprofitFactorsTemp_%5B0%5D.id.profitCode=&prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=&prpCprofitFactorsTemp_%5B0%5D.flag=&prpCitemKind.shortRateFlag=2&prpCitemKind.shortRate=100&prpCitemKind.currency=CNY&prpCmainCommon.groupFlag=0&sumBenchPremium=&prpCmain.discount=&prpCmain.sumPremium=&premiumF48=5000&prpCmain.sumNetPremium=&prpCmain.sumTaxPremium=&passengersSwitchFlag=&prpCitemKindsTemp%5B0%5D.min=&prpCitemKindsTemp%5B0%5D.max=&prpCitemKindsTemp%5B0%5D.chooseFlag=&prpCitemKindsTemp%5B0%5D.itemKindNo=2&prpCitemKindsTemp%5B0%5D.clauseCode=050002&prpCitemKindsTemp%5B0%5D.kindCode=050200&prpCitemKindsTemp%5B0%5D.kindName=%BB%FA%B6%AF%B3%B5%CB%F0%CA%A7%B1%A3%CF%D5&prpCitemKindsTemp%5B0%5D.unitAmount=&prpCitemKindsTemp%5B0%5D.quantity=&prpCitemKindsTemp%5B0%5D.specialFlag=on&prpCitemKindsTemp%5B0%5D.amount=&prpCitemKindsTemp%5B0%5D.calculateFlag=Y&prpCitemKindsTemp%5B0%5D.startDate=&prpCitemKindsTemp%5B0%5D.startHour=&prpCitemKindsTemp%5B0%5D.endDate=&prpCitemKindsTemp%5B0%5D.endHour=&relateSpecial%5B0%5D=050911&coachCar%5B0%5D=050941&prpCitemKindsTemp%5B0%5D.flag= 1001000  &prpCitemKindsTemp%5B0%5D.basePremium=&prpCitemKindsTemp%5B0%5D.rate=&prpCitemKindsTemp%5B0%5D.benchMarkPremium=&prpCitemKindsTemp%5B0%5D.disCount=&prpCitemKindsTemp%5B0%5D.premium=&prpCitemKindsTemp%5B0%5D.netPremium=&prpCitemKindsTemp%5B0%5D.taxPremium=&prpCitemKindsTemp%5B0%5D.taxRate=&prpCitemKindsTemp%5B0%5D.dutyFlag=&prpCitemKindsTemp%5B1%5D.min=&prpCitemKindsTemp%5B1%5D.max=&prpCitemKindsTemp%5B1%5D.chooseFlag=&prpCitemKindsTemp%5B1%5D.itemKindNo=3&prpCitemKindsTemp%5B1%5D.clauseCode=050005&prpCitemKindsTemp%5B1%5D.kindCode=050500&prpCitemKindsTemp%5B1%5D.kindName=%B5%C1%C7%C0%CF%D5&prpCitemKindsTemp%5B1%5D.unitAmount=&prpCitemKindsTemp%5B1%5D.quantity=&prpCitemKindsTemp%5B1%5D.specialFlag=on&prpCitemKindsTemp%5B1%5D.amount=&prpCitemKindsTemp%5B1%5D.calculateFlag=N&prpCitemKindsTemp%5B1%5D.startDate=&prpCitemKindsTemp%5B1%5D.startHour=&prpCitemKindsTemp%5B1%5D.endDate=&prpCitemKindsTemp%5B1%5D.endHour=&relateSpecial%5B1%5D=050921&coachCar%5B1%5D=&prpCitemKindsTemp%5B1%5D.flag= 1001000  &prpCitemKindsTemp%5B1%5D.basePremium=&prpCitemKindsTemp%5B1%5D.rate=&prpCitemKindsTemp%5B1%5D.benchMarkPremium=&prpCitemKindsTemp%5B1%5D.disCount=&prpCitemKindsTemp%5B1%5D.premium=&prpCitemKindsTemp%5B1%5D.netPremium=&prpCitemKindsTemp%5B1%5D.taxPremium=&prpCitemKindsTemp%5B1%5D.taxRate=&prpCitemKindsTemp%5B1%5D.dutyFlag=&prpCitemKindsTemp%5B2%5D.min=&prpCitemKindsTemp%5B2%5D.max=&prpCitemKindsTemp%5B2%5D.chooseFlag=&prpCitemKindsTemp%5B2%5D.itemKindNo=1&prpCitemKindsTemp%5B2%5D.clauseCode=050003&prpCitemKindsTemp%5B2%5D.kindCode=050600&prpCitemKindsTemp%5B2%5D.kindName=%B5%DA%C8%FD%D5%DF%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B2%5D.unitAmount=&prpCitemKindsTemp%5B2%5D.quantity=&prpCitemKindsTemp%5B2%5D.specialFlag=on&prpCitemKindsTemp%5B2%5D.amount=&prpCitemKindsTemp%5B2%5D.calculateFlag=Y&prpCitemKindsTemp%5B2%5D.startDate=&prpCitemKindsTemp%5B2%5D.startHour=&prpCitemKindsTemp%5B2%5D.endDate=&prpCitemKindsTemp%5B2%5D.endHour=&relateSpecial%5B2%5D=050912&coachCar%5B2%5D=050942&prpCitemKindsTemp%5B2%5D.flag= 1001000  &prpCitemKindsTemp%5B2%5D.basePremium=&prpCitemKindsTemp%5B2%5D.rate=&prpCitemKindsTemp%5B2%5D.benchMarkPremium=&prpCitemKindsTemp%5B2%5D.disCount=&prpCitemKindsTemp%5B2%5D.premium=&prpCitemKindsTemp%5B2%5D.netPremium=&prpCitemKindsTemp%5B2%5D.taxPremium=&prpCitemKindsTemp%5B2%5D.taxRate=&prpCitemKindsTemp%5B2%5D.dutyFlag=&prpCitemKindsTemp%5B3%5D.min=&prpCitemKindsTemp%5B3%5D.max=&prpCitemKindsTemp%5B3%5D.chooseFlag=&prpCitemKindsTemp%5B3%5D.itemKindNo=4&prpCitemKindsTemp%5B3%5D.clauseCode=050004&prpCitemKindsTemp%5B3%5D.kindCode=050701&prpCitemKindsTemp%5B3%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9&prpCitemKindsTemp%5B3%5D.unitAmount=&prpCitemKindsTemp%5B3%5D.quantity=&prpCitemKindsTemp%5B3%5D.specialFlag=on&prpCitemKindsTemp%5B3%5D.amount=&prpCitemKindsTemp%5B3%5D.calculateFlag=Y&prpCitemKindsTemp%5B3%5D.startDate=&prpCitemKindsTemp%5B3%5D.startHour=&prpCitemKindsTemp%5B3%5D.endDate=&prpCitemKindsTemp%5B3%5D.endHour=&relateSpecial%5B3%5D=050928&coachCar%5B3%5D=050943&prpCitemKindsTemp%5B3%5D.flag= 1001000  &prpCitemKindsTemp%5B3%5D.basePremium=&prpCitemKindsTemp%5B3%5D.rate=&prpCitemKindsTemp%5B3%5D.benchMarkPremium=&prpCitemKindsTemp%5B3%5D.disCount=&prpCitemKindsTemp%5B3%5D.premium=&prpCitemKindsTemp%5B3%5D.netPremium=&prpCitemKindsTemp%5B3%5D.taxPremium=&prpCitemKindsTemp%5B3%5D.taxRate=&prpCitemKindsTemp%5B3%5D.dutyFlag=&prpCitemKindsTemp%5B4%5D.min=&prpCitemKindsTemp%5B4%5D.max=&prpCitemKindsTemp%5B4%5D.chooseFlag=&prpCitemKindsTemp%5B4%5D.itemKindNo=5&prpCitemKindsTemp%5B4%5D.clauseCode=050004&prpCitemKindsTemp%5B4%5D.kindCode=050702&prpCitemKindsTemp%5B4%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9&prpCitemKindsTemp%5B4%5D.unitAmount=&prpCitemKindsTemp%5B4%5D.quantity=&prpCitemKindsTemp%5B4%5D.specialFlag=on&prpCitemKindsTemp%5B4%5D.amount=&prpCitemKindsTemp%5B4%5D.calculateFlag=Y&prpCitemKindsTemp%5B4%5D.startDate=&prpCitemKindsTemp%5B4%5D.startHour=&prpCitemKindsTemp%5B4%5D.endDate=&prpCitemKindsTemp%5B4%5D.endHour=&relateSpecial%5B4%5D=050929&coachCar%5B4%5D=050944&prpCitemKindsTemp%5B4%5D.flag= 1001000  &prpCitemKindsTemp%5B4%5D.basePremium=&prpCitemKindsTemp%5B4%5D.rate=&prpCitemKindsTemp%5B4%5D.benchMarkPremium=&prpCitemKindsTemp%5B4%5D.disCount=&prpCitemKindsTemp%5B4%5D.premium=&prpCitemKindsTemp%5B4%5D.netPremium=&prpCitemKindsTemp%5B4%5D.taxPremium=&prpCitemKindsTemp%5B4%5D.taxRate=&prpCitemKindsTemp%5B4%5D.dutyFlag=&prpCitemKindsTemp%5B5%5D.min=&prpCitemKindsTemp%5B5%5D.max=&prpCitemKindsTemp%5B5%5D.chooseFlag=&prpCitemKindsTemp%5B5%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B5%5D.clauseCode=050006&prpCitemKindsTemp%5B5%5D.kindCode=050210&relateSpecial%5B5%5D=050922&prpCitemKindsTemp%5B5%5D.kindName=%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B5%5D.specialFlag=on&prpCitemKindsTemp%5B5%5D.amount=&prpCitemKindsTemp%5B5%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B5%5D.startDate=&prpCitemKindsTemp%5B5%5D.startHour=&prpCitemKindsTemp%5B5%5D.endDate=&prpCitemKindsTemp%5B5%5D.endHour=&prpCitemKindsTemp%5B5%5D.flag= 200000&prpCitemKindsTemp%5B5%5D.basePremium=&prpCitemKindsTemp%5B5%5D.rate=&prpCitemKindsTemp%5B5%5D.benchMarkPremium=&prpCitemKindsTemp%5B5%5D.disCount=&prpCitemKindsTemp%5B5%5D.premium=&prpCitemKindsTemp%5B5%5D.netPremium=&prpCitemKindsTemp%5B5%5D.taxPremium=&prpCitemKindsTemp%5B5%5D.taxRate=&prpCitemKindsTemp%5B5%5D.dutyFlag=&prpCitemKindsTemp%5B6%5D.min=&prpCitemKindsTemp%5B6%5D.max=&prpCitemKindsTemp%5B6%5D.chooseFlag=&prpCitemKindsTemp%5B6%5D.itemKindNo=6&kindcodesub=&prpCitemKindsTemp%5B6%5D.clauseCode=050008&prpCitemKindsTemp%5B6%5D.kindCode=050231&relateSpecial%5B6%5D=      &prpCitemKindsTemp%5B6%5D.kindName=%B2%A3%C1%A7%B5%A5%B6%C0%C6%C6%CB%E9%CF%D5&prpCitemKindsTemp%5B6%5D.modeCode=&prpCitemKindsTemp%5B6%5D.amount=&prpCitemKindsTemp%5B6%5D.calculateFlag=N&prpCitemKindsTemp%5B6%5D.startDate=&prpCitemKindsTemp%5B6%5D.startHour=&prpCitemKindsTemp%5B6%5D.endDate=&prpCitemKindsTemp%5B6%5D.endHour=&prpCitemKindsTemp%5B6%5D.flag= 2000000  &prpCitemKindsTemp%5B6%5D.basePremium=&prpCitemKindsTemp%5B6%5D.rate=&prpCitemKindsTemp%5B6%5D.benchMarkPremium=&prpCitemKindsTemp%5B6%5D.disCount=&prpCitemKindsTemp%5B6%5D.premium=&prpCitemKindsTemp%5B6%5D.netPremium=&prpCitemKindsTemp%5B6%5D.taxPremium=&prpCitemKindsTemp%5B6%5D.taxRate=&prpCitemKindsTemp%5B6%5D.dutyFlag=&prpCitemKindsTemp%5B7%5D.min=&prpCitemKindsTemp%5B7%5D.max=&prpCitemKindsTemp%5B7%5D.chooseFlag=&prpCitemKindsTemp%5B7%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B7%5D.clauseCode=050016&prpCitemKindsTemp%5B7%5D.kindCode=050310&relateSpecial%5B7%5D=      &prpCitemKindsTemp%5B7%5D.kindName=%D7%D4%C8%BC%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B7%5D.amount=&prpCitemKindsTemp%5B7%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B7%5D.startDate=&prpCitemKindsTemp%5B7%5D.startHour=&prpCitemKindsTemp%5B7%5D.endDate=&prpCitemKindsTemp%5B7%5D.endHour=&prpCitemKindsTemp%5B7%5D.flag= 200000&prpCitemKindsTemp%5B7%5D.basePremium=&prpCitemKindsTemp%5B7%5D.rate=&prpCitemKindsTemp%5B7%5D.benchMarkPremium=&prpCitemKindsTemp%5B7%5D.disCount=&prpCitemKindsTemp%5B7%5D.premium=&prpCitemKindsTemp%5B7%5D.netPremium=&prpCitemKindsTemp%5B7%5D.taxPremium=&prpCitemKindsTemp%5B7%5D.taxRate=&prpCitemKindsTemp%5B7%5D.dutyFlag=&prpCitemKindsTemp%5B8%5D.min=&prpCitemKindsTemp%5B8%5D.max=&prpCitemKindsTemp%5B8%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B8%5D.clauseCode=050021&prpCitemKindsTemp%5B8%5D.kindCode=050370&relateSpecial%5B8%5D=      &prpCitemKindsTemp%5B8%5D.kindName=%D4%BC%B6%A8%C7%F8%D3%F2%CD%A8%D0%D0%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B8%5D.modeCode=1&prpCitemKindsTemp%5B8%5D.amount=5000.00&prpCitemKindsTemp%5B8%5D.calculateFlag=N12N000&prpCitemKindsTemp%5B8%5D.startDate=&prpCitemKindsTemp%5B8%5D.startHour=&prpCitemKindsTemp%5B8%5D.endDate=&prpCitemKindsTemp%5B8%5D.endHour=&prpCitemKindsTemp%5B8%5D.flag= 200000&prpCitemKindsTemp%5B8%5D.basePremium=&prpCitemKindsTemp%5B8%5D.rate=&prpCitemKindsTemp%5B8%5D.benchMarkPremium=&prpCitemKindsTemp%5B8%5D.disCount=&prpCitemKindsTemp%5B8%5D.premium=&prpCitemKindsTemp%5B8%5D.netPremium=&prpCitemKindsTemp%5B8%5D.taxPremium=&prpCitemKindsTemp%5B8%5D.taxRate=&prpCitemKindsTemp%5B8%5D.dutyFlag=&prpCitemKindsTemp%5B9%5D.min=&prpCitemKindsTemp%5B9%5D.max=&prpCitemKindsTemp%5B9%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B9%5D.clauseCode=050032&prpCitemKindsTemp%5B9%5D.kindCode=050611&relateSpecial%5B9%5D=      &prpCitemKindsTemp%5B9%5D.kindName=%B7%A8%C2%C9%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B9%5D.amount=10000.00&prpCitemKindsTemp%5B9%5D.calculateFlag=N22N000&prpCitemKindsTemp%5B9%5D.startDate=&prpCitemKindsTemp%5B9%5D.startHour=&prpCitemKindsTemp%5B9%5D.endDate=&prpCitemKindsTemp%5B9%5D.endHour=&prpCitemKindsTemp%5B9%5D.flag= 200000&prpCitemKindsTemp%5B9%5D.basePremium=&prpCitemKindsTemp%5B9%5D.rate=&prpCitemKindsTemp%5B9%5D.benchMarkPremium=&prpCitemKindsTemp%5B9%5D.disCount=&prpCitemKindsTemp%5B9%5D.premium=&prpCitemKindsTemp%5B9%5D.netPremium=&prpCitemKindsTemp%5B9%5D.taxPremium=&prpCitemKindsTemp%5B9%5D.taxRate=&prpCitemKindsTemp%5B9%5D.dutyFlag=&prpCitemKindsTemp%5B10%5D.min=&prpCitemKindsTemp%5B10%5D.max=&prpCitemKindsTemp%5B10%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B10%5D.clauseCode=050033&prpCitemKindsTemp%5B10%5D.kindCode=050630&relateSpecial%5B10%5D=050926&prpCitemKindsTemp%5B10%5D.kindName=%B8%BD%BC%D3%D3%CD%CE%DB%CE%DB%C8%BE%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B10%5D.amount=50000.00&prpCitemKindsTemp%5B10%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B10%5D.startDate=&prpCitemKindsTemp%5B10%5D.startHour=&prpCitemKindsTemp%5B10%5D.endDate=&prpCitemKindsTemp%5B10%5D.endHour=&prpCitemKindsTemp%5B10%5D.flag= 200000&prpCitemKindsTemp%5B10%5D.basePremium=&prpCitemKindsTemp%5B10%5D.rate=&prpCitemKindsTemp%5B10%5D.benchMarkPremium=&prpCitemKindsTemp%5B10%5D.disCount=&prpCitemKindsTemp%5B10%5D.premium=&prpCitemKindsTemp%5B10%5D.netPremium=&prpCitemKindsTemp%5B10%5D.taxPremium=&prpCitemKindsTemp%5B10%5D.taxRate=&prpCitemKindsTemp%5B10%5D.dutyFlag=&prpCitemKindsTemp%5B21%5D.chooseFlag=&prpCitemKindsTemp%5B21%5D.itemKindNo=&prpCitemKindsTemp%5B21%5D.startDate=&prpCitemKindsTemp%5B21%5D.kindCode=050291&prpCitemKindsTemp%5B21%5D.kindName=%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B21%5D.startHour=&prpCitemKindsTemp%5B21%5D.endDate=&prpCitemKindsTemp%5B21%5D.endHour=&prpCitemKindsTemp%5B21%5D.calculateFlag=N32Y000&relateSpecial%5B21%5D=050924&prpCitemKindsTemp%5B21%5D.flag= 200000&prpCitemKindsTemp%5B21%5D.basePremium=&prpCitemKindsTemp%5B21%5D.specialFlag=on&prpCitemKindsTemp%5B21%5D.amount=&prpCitemKindsTemp%5B21%5D.rate=&prpCitemKindsTemp%5B21%5D.benchMarkPremium=&prpCitemKindsTemp%5B21%5D.disCount=&prpCitemKindsTemp%5B21%5D.premium=&prpCitemKindsTemp%5B21%5D.netPremium=&prpCitemKindsTemp%5B21%5D.taxPremium=&prpCitemKindsTemp%5B21%5D.taxRate=&prpCitemKindsTemp%5B21%5D.dutyFlag=&prpCitemKindsTemp.itemKindSpecialSumPremium=&prpCitemKindsTemp%5B16%5D.chooseFlag=&prpCitemKindsTemp%5B16%5D.itemKindNo=&prpCitemKindsTemp%5B16%5D.startDate=&prpCitemKindsTemp%5B16%5D.kindCode=050911&prpCitemKindsTemp%5B16%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%CB%F0%CF%D5%A3%A9&prpCitemKindsTemp%5B16%5D.startHour=&prpCitemKindsTemp%5B16%5D.endDate=&prpCitemKindsTemp%5B16%5D.endHour=&prpCitemKindsTemp%5B16%5D.calculateFlag=N33Y000&relateSpecial%5B16%5D=&prpCitemKindsTemp%5B16%5D.flag= 200000&prpCitemKindsTemp%5B16%5D.basePremium=&prpCitemKindsTemp%5B16%5D.amount=&prpCitemKindsTemp%5B16%5D.rate=&prpCitemKindsTemp%5B16%5D.benchMarkPremium=&prpCitemKindsTemp%5B16%5D.disCount=&prpCitemKindsTemp%5B16%5D.premium=&prpCitemKindsTemp%5B16%5D.netPremium=&prpCitemKindsTemp%5B16%5D.taxPremium=&prpCitemKindsTemp%5B16%5D.taxRate=&prpCitemKindsTemp%5B16%5D.dutyFlag=&prpCitemKindsTemp%5B17%5D.chooseFlag=&prpCitemKindsTemp%5B17%5D.itemKindNo=&prpCitemKindsTemp%5B17%5D.startDate=&prpCitemKindsTemp%5B17%5D.kindCode=050921&prpCitemKindsTemp%5B17%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%BB%FA%B6%AF%B3%B5%B5%C1%C7%C0%CF%D5%A3%A9&prpCitemKindsTemp%5B17%5D.startHour=&prpCitemKindsTemp%5B17%5D.endDate=&prpCitemKindsTemp%5B17%5D.endHour=&prpCitemKindsTemp%5B17%5D.calculateFlag=N33Y000&relateSpecial%5B17%5D=&prpCitemKindsTemp%5B17%5D.flag= 200000&prpCitemKindsTemp%5B17%5D.basePremium=&prpCitemKindsTemp%5B17%5D.amount=&prpCitemKindsTemp%5B17%5D.rate=&prpCitemKindsTemp%5B17%5D.benchMarkPremium=&prpCitemKindsTemp%5B17%5D.disCount=&prpCitemKindsTemp%5B17%5D.premium=&prpCitemKindsTemp%5B17%5D.netPremium=&prpCitemKindsTemp%5B17%5D.taxPremium=&prpCitemKindsTemp%5B17%5D.taxRate=&prpCitemKindsTemp%5B17%5D.dutyFlag=&prpCitemKindsTemp%5B18%5D.chooseFlag=&prpCitemKindsTemp%5B18%5D.itemKindNo=&prpCitemKindsTemp%5B18%5D.startDate=&prpCitemKindsTemp%5B18%5D.kindCode=050912&prpCitemKindsTemp%5B18%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%C8%FD%D5%DF%CF%D5%A3%A9&prpCitemKindsTemp%5B18%5D.startHour=&prpCitemKindsTemp%5B18%5D.endDate=&prpCitemKindsTemp%5B18%5D.endHour=&prpCitemKindsTemp%5B18%5D.calculateFlag=N33Y000&relateSpecial%5B18%5D=&prpCitemKindsTemp%5B18%5D.flag= 200000&prpCitemKindsTemp%5B18%5D.basePremium=&prpCitemKindsTemp%5B18%5D.amount=&prpCitemKindsTemp%5B18%5D.rate=&prpCitemKindsTemp%5B18%5D.benchMarkPremium=&prpCitemKindsTemp%5B18%5D.disCount=&prpCitemKindsTemp%5B18%5D.premium=&prpCitemKindsTemp%5B18%5D.netPremium=&prpCitemKindsTemp%5B18%5D.taxPremium=&prpCitemKindsTemp%5B18%5D.taxRate=&prpCitemKindsTemp%5B18%5D.dutyFlag=&prpCitemKindsTemp%5B19%5D.chooseFlag=&prpCitemKindsTemp%5B19%5D.itemKindNo=&prpCitemKindsTemp%5B19%5D.startDate=&prpCitemKindsTemp%5B19%5D.kindCode=050928&prpCitemKindsTemp%5B19%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9%A3%A9&prpCitemKindsTemp%5B19%5D.startHour=&prpCitemKindsTemp%5B19%5D.endDate=&prpCitemKindsTemp%5B19%5D.endHour=&prpCitemKindsTemp%5B19%5D.calculateFlag=N33Y000&relateSpecial%5B19%5D=&prpCitemKindsTemp%5B19%5D.flag= 200000&prpCitemKindsTemp%5B19%5D.basePremium=&prpCitemKindsTemp%5B19%5D.amount=&prpCitemKindsTemp%5B19%5D.rate=&prpCitemKindsTemp%5B19%5D.benchMarkPremium=&prpCitemKindsTemp%5B19%5D.disCount=&prpCitemKindsTemp%5B19%5D.premium=&prpCitemKindsTemp%5B19%5D.netPremium=&prpCitemKindsTemp%5B19%5D.taxPremium=&prpCitemKindsTemp%5B19%5D.taxRate=&prpCitemKindsTemp%5B19%5D.dutyFlag=&prpCitemKindsTemp%5B20%5D.chooseFlag=&prpCitemKindsTemp%5B20%5D.itemKindNo=&prpCitemKindsTemp%5B20%5D.startDate=&prpCitemKindsTemp%5B20%5D.kindCode=050929&prpCitemKindsTemp%5B20%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9%A3%A9&prpCitemKindsTemp%5B20%5D.startHour=&prpCitemKindsTemp%5B20%5D.endDate=&prpCitemKindsTemp%5B20%5D.endHour=&prpCitemKindsTemp%5B20%5D.calculateFlag=N33Y000&relateSpecial%5B20%5D=&prpCitemKindsTemp%5B20%5D.flag= 200000&prpCitemKindsTemp%5B20%5D.basePremium=&prpCitemKindsTemp%5B20%5D.amount=&prpCitemKindsTemp%5B20%5D.rate=&prpCitemKindsTemp%5B20%5D.benchMarkPremium=&prpCitemKindsTemp%5B20%5D.disCount=&prpCitemKindsTemp%5B20%5D.premium=&prpCitemKindsTemp%5B20%5D.netPremium=&prpCitemKindsTemp%5B20%5D.taxPremium=&prpCitemKindsTemp%5B20%5D.taxRate=&prpCitemKindsTemp%5B20%5D.dutyFlag=&prpCitemKindsTemp%5B22%5D.chooseFlag=&prpCitemKindsTemp%5B22%5D.itemKindNo=&prpCitemKindsTemp%5B22%5D.startDate=&prpCitemKindsTemp%5B22%5D.kindCode=050924&prpCitemKindsTemp%5B22%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B22%5D.startHour=&prpCitemKindsTemp%5B22%5D.endDate=&prpCitemKindsTemp%5B22%5D.endHour=&prpCitemKindsTemp%5B22%5D.calculateFlag=N33Y000&relateSpecial%5B22%5D=&prpCitemKindsTemp%5B22%5D.flag= 200000&prpCitemKindsTemp%5B22%5D.basePremium=&prpCitemKindsTemp%5B22%5D.amount=&prpCitemKindsTemp%5B22%5D.rate=&prpCitemKindsTemp%5B22%5D.benchMarkPremium=&prpCitemKindsTemp%5B22%5D.disCount=&prpCitemKindsTemp%5B22%5D.premium=&prpCitemKindsTemp%5B22%5D.netPremium=&prpCitemKindsTemp%5B22%5D.taxPremium=&prpCitemKindsTemp%5B22%5D.taxRate=&prpCitemKindsTemp%5B22%5D.dutyFlag=&prpCitemKindsTemp%5B23%5D.chooseFlag=&prpCitemKindsTemp%5B23%5D.itemKindNo=&prpCitemKindsTemp%5B23%5D.startDate=&prpCitemKindsTemp%5B23%5D.kindCode=050922&prpCitemKindsTemp%5B23%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B23%5D.startHour=&prpCitemKindsTemp%5B23%5D.endDate=&prpCitemKindsTemp%5B23%5D.endHour=&prpCitemKindsTemp%5B23%5D.calculateFlag=N33Y000&relateSpecial%5B23%5D=&prpCitemKindsTemp%5B23%5D.flag= 200000&prpCitemKindsTemp%5B23%5D.basePremium=&prpCitemKindsTemp%5B23%5D.amount=&prpCitemKindsTemp%5B23%5D.rate=&prpCitemKindsTemp%5B23%5D.benchMarkPremium=&prpCitemKindsTemp%5B23%5D.disCount=&prpCitemKindsTemp%5B23%5D.premium=&prpCitemKindsTemp%5B23%5D.netPremium=&prpCitemKindsTemp%5B23%5D.taxPremium=&prpCitemKindsTemp%5B23%5D.taxRate=&prpCitemKindsTemp%5B23%5D.dutyFlag=&hidden_index_itemKind=16&hidden_index_profitDetial=0&prpCitemKindsTemp_%5B0%5D.chooseFlag=&prpCitemKindsTemp_%5B0%5D.itemKindNo=&prpCitemKindsTemp_%5B0%5D.startDate=&prpCitemKindsTemp_%5B0%5D.kindCode=&prpCitemKindsTemp_%5B0%5D.kindName=&prpCitemKindsTemp_%5B0%5D.startHour=&prpCitemKindsTemp_%5B0%5D.endDate=&prpCitemKindsTemp_%5B0%5D.endHour=&prpCitemKindsTemp_%5B0%5D.calculateFlag=&relateSpecial_%5B0%5D=&prpCitemKindsTemp_%5B0%5D.flag=&prpCitemKindsTemp_%5B0%5D.basePremium=&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.rate=&prpCitemKindsTemp_%5B0%5D.benchMarkPremium=&prpCitemKindsTemp_%5B0%5D.disCount=&prpCitemKindsTemp_%5B0%5D.premium=&prpCitemKindsTemp_%5B0%5D.netPremium=&prpCitemKindsTemp_%5B0%5D.taxPremium=&prpCitemKindsTemp_%5B0%5D.taxRate=&prpCitemKindsTemp_%5B0%5D.dutyFlag=&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.value=&prpCitemKindsTemp_%5B0%5D.value=50&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.modeCode=10&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.value=1000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=10000&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=60&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=90&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.amount=50000.00&prpCitemKindsTemp_%5B0%5D.amount=10000.00&prpCitemKindsTemp_%5B0%5D.amount=5000.00&itemKindLoadFlag=&BIdemandNo=&BIdemandTime=&bIRiskWarningType=&noDamageYearsBIPlat=&prpCitemCarExt.lastDamagedBI=0&lastDamagedBITemp=0&DAZlastDamagedBI=2&prpCitemCarExt.thisDamagedBI=0&prpCitemCarExt.noDamYearsBI=0&noDamYearsBINumber=1&prpCitemCarExt.lastDamagedCI=0&BIDemandClaim_Flag=&BiInsureDemandPay_%5B0%5D.id.serialNo=&BiInsureDemandPay_%5B0%5D.payCompany=&BiInsureDemandPay_%5B0%5D.claimregistrationno=&BiInsureDemandPay_%5B0%5D.compensateNo=&BiInsureDemandPay_%5B0%5D.lossTime=&BiInsureDemandPay_%5B0%5D.endcCaseTime=&PrpCmain_%5B0%5D.startDate=&PrpCmain_%5B0%5D.endDate=&BiInsureDemandPay_%5B0%5D.lossFee=&BiInsureDemandPay_%5B0%5D.payType=&BiInsureDemandPay_%5B0%5D.personpayType=&bIRiskWarningClaimItems_%5B0%5D.id.serialNo=&bIRiskWarningClaimItems_%5B0%5D.riskWarningType=&bIRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&bIRiskWarningClaimItems_%5B0%5D.insurerCode=&bIRiskWarningClaimItems_%5B0%5D.lossTime=&bIRiskWarningClaimItems_%5B0%5D.lossArea=&prpCitemKindCI.shortRate=100&cIBPFlag=1&prpCitemKindCI.unitAmount=0&prpCitemKindCI.id.itemKindNo=&prpCitemKindCI.kindCode=050100&prpCitemKindCI.kindName=%BB%FA%B6%AF%B3%B5%BD%BB%CD%A8%CA%C2%B9%CA%C7%BF%D6%C6%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindCI.calculateFlag=Y&prpCitemKindCI.basePremium=&prpCitemKindCI.quantity=1&prpCitemKindCI.amount=&prpCitemKindCI.deductible=&prpCitemKindCI.adjustRate=1&prpCitemKindCI.rate=0&prpCitemKindCI.benchMarkPremium=&prpCitemKindCI.disCount=1&prpCitemKindCI.premium=&prpCitemKindCI.flag=&prpCitemKindCI.netPremium=&prpCitemKindCI.taxPremium=&prpCitemKindCI.taxRate=&prpCitemKindCI.dutyFlag=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCitemCarExt_CI.rateRloatFlag=01&prpCitemCarExt_CI.noDamYearsCI=1&prpCitemCarExt_CI.lastDamagedCI=0&prpCitemCarExt_CI.flag=&prpCitemCarExt_CI.damFloatRatioCI=0&prpCitemCarExt_CI.offFloatRatioCI=0&prpCitemCarExt_CI.thisDamagedCI=0&prpCitemCarExt_CI.flag=&hidden_index_ctraffic_NOPlat_Drink=0&hidden_index_ctraffic_NOPlat=0&ciInsureDemand.demandNo=&ciInsureDemand.demandTime=&ciInsureDemand.restricFlag=&ciInsureDemand.preferentialDay=&ciInsureDemand.preferentialPremium=&ciInsureDemand.preferentialFormula%20=&ciInsureDemand.lastyearenddate=&prpCitemCar.noDamageYears=0&ciInsureDemand.rateRloatFlag=00&ciInsureDemand.claimAdjustReason=A1&ciInsureDemand.peccancyAdjustReason=V1&cIRiskWarningType=&CIDemandFecc_Flag=&ciInsureDemandLoss_%5B0%5D.id.serialNo=&ciInsureDemandLoss_%5B0%5D.lossTime=&ciInsureDemandLoss_%5B0%5D.lossDddress=&ciInsureDemandLoss_%5B0%5D.lossAction=&ciInsureDemandLoss_%5B0%5D.coeff=&ciInsureDemandLoss_%5B0%5D.lossType=&ciInsureDemandLoss_%5B0%5D.identifyType=&ciInsureDemandLoss_%5B0%5D.identifyNumber=&ciInsureDemandLoss_%5B0%5D.lossAcceptDate=&ciInsureDemandLoss_%5B0%5D.processingStatus=&ciInsureDemandLoss_%5B0%5D.lossActionDesc=&CIDemandClaim_Flag=&ciInsureDemandPay_%5B0%5D.id.serialNo=&ciInsureDemandPay_%5B0%5D.payCompany=&ciInsureDemandPay_%5B0%5D.claimregistrationno=&ciInsureDemandPay_%5B0%5D.compensateNo=&ciInsureDemandPay_%5B0%5D.lossTime=&ciInsureDemandPay_%5B0%5D.endcCaseTime=&ciInsureDemandPay_%5B0%5D.lossFee=&ciInsureDemandPay_%5B0%5D.payType=&ciInsureDemandPay_%5B0%5D.personpayType=&ciRiskWarningClaimItems_%5B0%5D.id.serialNo=&ciRiskWarningClaimItems_%5B0%5D.riskWarningType=&ciRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&ciRiskWarningClaimItems_%5B0%5D.insurerCode=&ciRiskWarningClaimItems_%5B0%5D.lossTime=&ciRiskWarningClaimItems_%5B0%5D.lossArea=&ciInsureDemand.licenseNo=&ciInsureDemand.licenseType=&ciInsureDemand.useNatureCode=&ciInsureDemand.frameNo=&ciInsureDemand.engineNo=&ciInsureDemand.licenseColorCode=&ciInsureDemand.carOwner=&ciInsureDemand.enrollDate=&ciInsureDemand.makeDate=&ciInsureDemand.seatCount=&ciInsureDemand.tonCount=&ciInsureDemand.validCheckDate=&ciInsureDemand.manufacturerName=&ciInsureDemand.modelCode=&ciInsureDemand.brandCName=&ciInsureDemand.brandName=&ciInsureDemand.carKindCode=&ciInsureDemand.checkDate=&ciInsureDemand.endValidDate=&ciInsureDemand.carStatus=&ciInsureDemand.haulage=&AccidentFlag=&rateFloatFlag=ND4&prpCtrafficRecordTemps_%5B0%5D.id.serialNo=&prpCtrafficRecordTemps_%5B0%5D.accidentDate=&prpCtrafficRecordTemps_%5B0%5D.claimDate=&hidden_index_ctraffic=0&_taxUnit=&taxPlatFormTime=2012-04-21&iniPrpCcarShipTax_Flag=&strCarShipFlag=1&prpCcarShipTax.taxType=1&prpCcarShipTax.calculateMode=C1&prpCcarShipTax.leviedDate=&prpCcarShipTax.carKindCode=A01&prpCcarShipTax.model=B11&prpCcarShipTax.taxPayerIdentNo=&prpCcarShipTax.taxPayerNumber=&prpCcarShipTax.carLotEquQuality=&prpCcarShipTax.taxPayerCode=&prpCcarShipTax.id.itemNo=1&prpCcarShipTax.taxPayerNature=3&prpCcarShipTax.taxPayerName=&prpCcarShipTax.taxUnit=&prpCcarShipTax.taxComCode=&prpCcarShipTax.taxComName=&prpCcarShipTax.taxExplanation=&prpCcarShipTax.taxAbateReason=&prpCcarShipTax.dutyPaidProofNo_1=&prpCcarShipTax.dutyPaidProofNo_2=&prpCcarShipTax.dutyPaidProofNo=&prpCcarShipTax.taxAbateRate=&prpCcarShipTax.taxAbateAmount=&prpCcarShipTax.taxAbateType=1&prpCcarShipTax.taxUnitAmount=&prpCcarShipTax.prePayTaxYear=&prpCcarShipTax.prePolicyEndDate=&prpCcarShipTax.payStartDate=&prpCcarShipTax.payEndDate=&prpCcarShipTax.thisPayTax=&prpCcarShipTax.prePayTax=&prpCcarShipTax.taxItemCode=&prpCcarShipTax.taxItemName=&prpCcarShipTax.baseTaxation=&prpCcarShipTax.taxRelifFlag=&prpCcarShipTax.delayPayTax=&prpCcarShipTax.sumPayTax=&CarShipInit_Flag=&prpCcarShipTax.flag=&quotationtaxPayerCode=&noBringOutEngage=&prpCengageTemps_%5B0%5D.id.serialNo=&prpCengageTemps_%5B0%5D.clauseCode=&prpCengageTemps_%5B0%5D.clauseName=&clauses_%5B0%5D=&prpCengageTemps_%5B0%5D.flag=&prpCengageTemps_%5B0%5D.engageFlag=&prpCengageTemps_%5B0%5D.maxCount=&prpCengageTemps_%5B0%5D.clauses=&iniPrpCengage_Flag=&hidden_index_engage=0&costRateForPG=&certificateNo=&levelMaxRate=&maxRateScm=&levelMaxRateCi=&maxRateScmCi=&isModifyBI=&isModifyCI=&sumBICoinsRate=&sumCICoinsRate=&agentsRateBI=&agentsRateCI=&prpVisaRecordP.id.visaNo=&prpVisaRecordP.id.visaCode=&prpVisaRecordP.visaName=&prpVisaRecordP.printType=101&prpVisaRecordT.id.visaNo=&prpVisaRecordT.id.visaCode=&prpVisaRecordT.visaName=&prpVisaRecordT.printType=103&prpCmain.sumAmount=&prpCmain.sumDiscount=&prpCstampTaxBI.biTaxRate=&prpCstampTaxBI.biPayTax=0&prpCmain.sumPremium=&prpVisaRecordPCI.id.visaNo=&prpVisaRecordPCI.id.visaCode=&prpVisaRecordPCI.visaName=&prpVisaRecordPCI.printType=201&prpVisaRecordTCI.id.visaNo=&prpVisaRecordTCI.id.visaCode=&prpVisaRecordTCI.visaName=&prpVisaRecordTCI.printType=203&prpCmainCI.sumAmount=&prpCmainCI.sumDiscount=&prpCstampTaxCI.ciTaxRate=&prpCstampTaxCI.ciPayTax=&prpCmainCI.sumPremium=&prpCmainCar.rescueFundRate=&prpCmainCar.resureFundFee=&prpCmain.projectCode=&projectCode=&costRateUpper=&prpCmainCommon.ext3=&importantProjectCode=&prpCmain.operatorCode=020083&operatorName=%D6%DA%BA%CF%CB%C4%BA%A3&operateDateShow=2015-06-25&prpCmain.coinsFlag=00&coinsFlagBak=00&premium=&prpCmain.language=CNY&prpCmain.policySort=1&prpCmain.policyRelCode=&prpCmain.policyRelName=&subsidyRate=&policyRel=&prpCmain.reinsFlag=0&prpCmain.agriFlag=0&premium=&prpCmainCar.carCheckStatus=0&prpCmainCar.carChecker=&carCheckerTranslate=&prpCmainCar.carCheckTime=&prpCmainCommon.DBCFlag=0&prpCmain.argueSolution=1&prpCmain.arbitBoardName=&arbitBoardNameDes=&prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&hidden_index_commission=0&scmIsOpen=1111100000&prpCagents_%5B0%5D.roleType=&roleTypeName_%5B0%5D=&prpCagents_%5B0%5D.id.roleCode=&prpCagents_%5B0%5D.roleCode_uni=&prpCagents_%5B0%5D.roleName=&prpCagents_%5B0%5D.costRate=&prpCagents_%5B0%5D.costFee=&prpCagents_%5B0%5D.flag=&prpCagents_%5B0%5D.businessNature=&prpCagents_%5B0%5D.isMain=&prpCagentCIs_%5B0%5D.roleType=&roleTypeNameCI_%5B0%5D=&prpCagentCIs_%5B0%5D.id.roleCode=&prpCagentCIs_%5B0%5D.roleCode_uni=&prpCagentCIs_%5B0%5D.roleName=&prpCagentCIs_%5B0%5D.costRate=&prpCagentCIs_%5B0%5D.costFee=&prpCagentCIs_%5B0%5D.flag=&prpCagentCIs_%5B0%5D.businessNature=&prpCagentCIs_%5B0%5D.isMain=&commissionCount=&prpCsaless_%5B0%5D.salesDetailName=&prpCsaless_%5B0%5D.riskCode=&prpCsaless_%5B0%5D.splitRate=&prpCsaless_%5B0%5D.oriSplitNumber=&prpCsaless_%5B0%5D.splitFee=&prpCsaless_%5B0%5D.agreementNo=&prpCsaless_%5B0%5D.id.salesCode=&prpCsaless_%5B0%5D.salesName=&prpCsaless_%5B0%5D.id.proposalNo=&prpCsaless_%5B0%5D.id.salesDetailCode=&prpCsaless_%5B0%5D.totalRate=&prpCsaless_%5B0%5D.splitWay=&prpCsaless_%5B0%5D.totalRateMax=&prpCsaless_%5B0%5D.flag=&prpCsaless_%5B0%5D.remark=&commissionPower=&hidden_index_prpCsales=0&prpCsalesDatils_%5B0%5D.id.salesCode=&prpCsalesDatils_%5B0%5D.id.proposalNo=&prpCsalesDatils_%5B0%5D.id.%20%20=&prpCsalesDatils_%5B0%5D.id.roleType=&prpCsalesDatils_%5B0%5D.id.roleCode=&prpCsalesDatils_%5B0%5D.currency=&prpCsalesDatils_%5B0%5D.splitDatilRate=&prpCsalesDatils_%5B0%5D.splitDatilFee=&prpCsalesDatils_%5B0%5D.roleName=&prpCsalesDatils_%5B0%5D.splitWay=&prpCsalesDatils_%5B0%5D.flag=&prpCsalesDatils_%5B0%5D.remark=&hidden_index_prpCsalesDatil=0&csManageSwitch=1&prpCmainChannel.agentCode=&prpCmainChannel.agentName=&prpCmainChannel.organCode=&prpCmainChannel.organCName=&comCodeType=&prpCmainChannel.identifyNumber=&prpCmainChannel.identifyType=&prpCmainChannel.manOrgCode=&prpCmain.remark=&prpDdismantleDetails_%5B0%5D.id.agreementNo=&prpDdismantleDetails_%5B0%5D.flag=&prpDdismantleDetails_%5B0%5D.id.configCode=&prpDdismantleDetails_%5B0%5D.id.assignType=&prpDdismantleDetails_%5B0%5D.id.roleCode=&prpDdismantleDetails_%5B0%5D.roleName=&prpDdismantleDetails_%5B0%5D.costRate=&prpDdismantleDetails_%5B0%5D.roleFlag=&prpDdismantleDetails_%5B0%5D.businessNature=&prpDdismantleDetails_%5B0%5D.roleCode_uni=&hidden_index_prpDdismantleDetails=0&payTimes=1&prpCplanTemps_%5B0%5D.payNo=&prpCplanTemps_%5B0%5D.serialNo=&prpCplanTemps_%5B0%5D.endorseNo=&cplan_%5B0%5D.payReasonC=&prpCplanTemps_%5B0%5D.payReason=&prpCplanTemps_%5B0%5D.planDate=&prpCplanTemps_%5B0%5D.currency=&description_%5B0%5D.currency=&prpCplanTemps_%5B0%5D.planFee=&cplans_%5B0%5D.planFee=&cplans_%5B0%5D.backPlanFee=&prpCplanTemps_%5B0%5D.netPremium=&prpCplanTemps_%5B0%5D.taxPremium=&prpCplanTemps_%5B0%5D.delinquentFee=&prpCplanTemps_%5B0%5D.flag=&prpCplanTemps_%5B0%5D.subsidyRate=&prpCplanTemps_%5B0%5D.isBICI=&iniPrpCplan_Flag=&loadFlag9=&prpCplanTemps%5B0%5D.payNo=1&prpCplanTemps%5B0%5D.serialNo=1&prpCplanTemps%5B0%5D.endorseNo=&PayRefReason=%CA%D5%B1%A3%B7%D1&prpCplanTemps%5B0%5D.payReason=R21&prpCplanTemps%5B0%5D.planDate=&prpCplanTemps%5B0%5D.currency=CNY&prpCplanTemps%5B0%5D.planFee=2698.25&prpCplanTemps%5B0%5D.netPremium=&prpCplanTemps%5B0%5D.taxPremium=&prpCplanTemps%5B0%5D.delinquentFee=0.00&prpCplanTemps%5B0%5D.flag=&prpCplanTemps%5B0%5D.subsidyRate=0.00&prpCplanTemps%5B0%5D.isBICI=BI&planfee_index=1&planStr=&planPayTimes=&prpCmainCar.flag=1&prpCmainCarFlag=1&coinsSchemeCode=&coinsSchemeName=&mainPolicyNo=&prpCcoinsMains_%5B0%5D.id.serialNo=1&prpCcoinsMains_%5B0%5D.coIdentity=1&prpCcoinsMains_%5B0%5D.coinsCode=002&prpCcoinsMains_%5B0%5D.coinsName=%C8%CB%B1%A3%B2%C6%B2%FA&prpCcoinsMains_%5B0%5D.coinsRate=&prpCcoinsMains_%5B0%5D.id.currency=CNY&prpCcoinsMains_%5B0%5D.coinsAmount=&prpCcoinsMains_%5B0%5D.coinsPremium=&prpCcoinsMains_%5B0%5D.coinsPremium=&iniPrpCcoins_Flag=&hidden_index_ccoins=0&prpCpayeeAccountBIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountBIs_%5B0%5D.id.serialNo=&prpCpayeeAccountBIs_%5B0%5D.itemNo=&prpCpayeeAccountBIs_%5B0%5D.payReason=&prpCpayeeAccountBIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountBIs_%5B0%5D.accountName=&prpCpayeeAccountBIs_%5B0%5D.basicBankCode=&prpCpayeeAccountBIs_%5B0%5D.basicBankName=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountBIs_%5B0%5D.bankCode=&prpCpayeeAccountBIs_%5B0%5D.bankName=&prpCpayeeAccountBIs_%5B0%5D.cnaps=&prpCpayeeAccountBIs_%5B0%5D.accountNo=&prpCpayeeAccountBIs_%5B0%5D.isPrivate=&prpCpayeeAccountBIs_%5B0%5D.cardType=&prpCpayeeAccountBIs_%5B0%5D.paySumFee=&prpCpayeeAccountBIs_%5B0%5D.payType=&prpCpayeeAccountBIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountBIs_%5B0%5D.sendSms=&prpCpayeeAccountBIs_%5B0%5D.identifyType=&prpCpayeeAccountBIs_%5B0%5D.identifyNo=&prpCpayeeAccountBIs_%5B0%5D.telephone=&prpCpayeeAccountBIs_%5B0%5D.sendMail=&prpCpayeeAccountBIs_%5B0%5D.mailAddr=&prpCpayeeAccountCIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountCIs_%5B0%5D.id.serialNo=&prpCpayeeAccountCIs_%5B0%5D.itemNo=&prpCpayeeAccountCIs_%5B0%5D.payReason=&prpCpayeeAccountCIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountCIs_%5B0%5D.accountName=&prpCpayeeAccountCIs_%5B0%5D.basicBankCode=&prpCpayeeAccountCIs_%5B0%5D.basicBankName=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountCIs_%5B0%5D.bankCode=&prpCpayeeAccountCIs_%5B0%5D.bankName=&prpCpayeeAccountCIs_%5B0%5D.cnaps=&prpCpayeeAccountCIs_%5B0%5D.accountNo=&prpCpayeeAccountCIs_%5B0%5D.isPrivate=&prpCpayeeAccountCIs_%5B0%5D.cardType=&prpCpayeeAccountCIs_%5B0%5D.paySumFee=&prpCpayeeAccountCIs_%5B0%5D.payType=&prpCpayeeAccountCIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountCIs_%5B0%5D.sendSms=&prpCpayeeAccountCIs_%5B0%5D.identifyType=&prpCpayeeAccountCIs_%5B0%5D.identifyNo=&prpCpayeeAccountCIs_%5B0%5D.telephone=&prpCpayeeAccountCIs_%5B0%5D.sendMail=&prpCpayeeAccountCIs_%5B0%5D.mailAddr=&iReinsCode=&prpCspecialFacs_%5B0%5D.reinsCode=001&iFReinsCode=&iPayCode=&iShareRate=&iCommRate=&iTaxRate=&iOthRate=&iCommission=&iOthPremium=&prpCspecialFacs_%5B0%5D.id.reinsNo=1&prpCspecialFacs_%5B0%5D.freinsCode=001&prpCspecialFacs_%5B0%5D.payCode=001&prpCspecialFacs_%5B0%5D.shareRate=001&prpCspecialFacs_%5B0%5D.sharePremium=001&prpCspecialFacs_%5B0%5D.commRate=001&prpCspecialFacs_%5B0%5D.taxRate=001&prpCspecialFacs_%5B0%5D.tax=001&prpCspecialFacs_%5B0%5D.othRate=001&prpCspecialFacs_%5B0%5D.commission=001&prpCspecialFacs_%5B0%5D.othPremium=001&prpCspecialFacs_%5B0%5D.reinsName=001&prpCspecialFacs_%5B0%5D.freinsName=001&prpCspecialFacs_%5B0%5D.payName=001&prpCspecialFacs_%5B0%5D.remark=001&prpCspecialFacs_%5B0%5D.flag=&hidden_index_specialFac=0&updateIndex=-1&iniCspecialFac_Flag=&_ReinsCode=&loadFlag8=&_FReinsCode=&_PayCode=&_ReinsName=&_FReinsName=&_PayName=&_CommRate=&_OthRate=&_ShareRate=&_Commission=&_OthPremium=&_SharePremium=&_TaxRate=&_Tax=&_Remark=&prpCsettlement.buyerUnitRank=3&prpCsettlement.buyerPreFee=&prpCsettlement.buyerUnitCode=&prpCsettlement.buyerUnitName=&prpCsettlement.upperUnitCode=&upperUnitName=&prpCsettlement.buyerUnitAddress=&prpCsettlement.buyerLinker=&prpCsettlement.buyerPhone=&prpCsettlement.buyerMobile=&prpCsettlement.buyerFax=&prpCsettlement.buyerUnitNature=1&prpCsettlement.buyerProvince=11000000&buyerProvinceDes=%C8%CB%B1%A3%B2%C6%CF%D5%B1%B1%BE%A9%CA%D0%B7%D6%B9%AB%CB%BE&prpCsettlement.buyerBusinessSort=01&prpCsettlement.comCname=&prpCsettlement.linkerCode=&linkerName=&linkerPhone=&linkerMobile=&linkerFax=&prpCsettlement.comCode=&prpCsettlement.fundForm=1&prpCsettlement.flag=&settlement_Flag=&prpCcontriutions_%5B0%5D.id.serialNo=1&prpCcontriutions_%5B0%5D.contribType=F&prpCcontriutions_%5B0%5D.contribCode=&prpCcontriutions_%5B0%5D.contribName=&prpCcontriutions_%5B0%5D.contribCode_uni=&prpCcontriutions_%5B0%5D.contribPercent=&prpCcontriutions_%5B0%5D.contribPremium=&prpCcontriutions_%5B0%5D.remark=&hidden_index_ccontriutions=0&userCode=&iProposalNo=&CProposalNo=&timeFlag=&prpCremarks_%5B0%5D.id.proposalNo=&prpCremarks_%5B0%5D.id.serialNo=&prpCremarks_%5B0%5D.operatorCode=&prpCremarks_%5B0%5D.remark=&prpCremarks_%5B0%5D.flag=&prpCremarks_%5B0%5D.insertTimeForHis=&hidden_index_remark=0&ciInsureDemandCheckVo.demandNo=&ciInsureDemandCheckVo.checkQuestion=&ciInsureDemandCheckVo.checkAnswer=&ciInsureDemandCheckVo.flag=DEMAND&ciInsureDemandCheckVo.riskCode=";
		String purchasePrice = (String) carInfoMap.get("purchasePrice");//新车购置价格
		String depreciationPrice = (String) carInfoMap.get("depreciationPrice");//折旧价格
		String no = (String) carInfoMap.get("reBiPolicyNo");//交强险或者商业险上一年投保单号
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String biEndDateOldStr = (String) carInfoMap.get("biEndDate");//上一年商业险结束日期
		String ciEndDateOldStr = (String) carInfoMap.get("ciEndDate");//上一年交强险结束日期
		
		if(StringUtils.isBlank(biEndDateOldStr)){
			biEndDateOldStr = ciEndDateOldStr;
		}
		if(StringUtils.isBlank(ciEndDateOldStr)){
			ciEndDateOldStr = biEndDateOldStr;
		}
		if(StringUtils.isBlank(ciEndDateOldStr) && StringUtils.isBlank(biEndDateOldStr)){
			ciEndDateOldStr = sdf.format(new Date());
			biEndDateOldStr = sdf.format(new Date());
		}
		
		Date biEndDateOld = null;
		Date bizStartDate = null;
		Date bizEndDate = null;
		try {
			biEndDateOld = sdf.parse(biEndDateOldStr);
			bizStartDate = DateUtils.addDays(biEndDateOld, 1);//开始时间为 上一年结束时间 + 1天
			bizEndDate = DateUtils.addYears(biEndDateOld, 1);//结束时间为 上一年结束时间 + 1年
		} catch (Exception e) {
			logger.error("人保 API 服务，【续保，商业险开始时间转换报错】，" + e.getMessage());
		}
		
		String bizStartDateStr = sdf.format(bizStartDate);//商业险开始时间
		String bizEndDateStr = sdf.format(bizEndDate);//商业险结束时间
		
		Date ciStartDateOld = null;
		Date ciEndDateOld = null;
		Date ciStartDate = null;
		Date ciEndDate = null;
		try {
			ciEndDateOld = sdf.parse(ciEndDateOldStr);
			ciStartDateOld = DateUtils.addDays(DateUtils.addYears(ciEndDateOld, -1), 1);//减1年 加1天
			ciStartDate = DateUtils.addDays(ciEndDateOld, 1);//开始时间为 上一年结束时间 + 1天
			ciEndDate = DateUtils.addYears(ciEndDateOld, 1);//结束时间为 上一年结束时间 + 1年
		} catch (Exception e) {
			logger.error("人保 API 服务，【续保，交强险开始时间转换报错】，" + e.getMessage());
		}
		
		String ciStartDateStr = sdf.format(ciStartDate);//交强险开始时间
		String ciEndDateStr = sdf.format(ciEndDate);//交强险结束时间
		
		sdf = new SimpleDateFormat("yyyy/MM/dd");
		String ciStartDateOther = sdf.format(ciStartDateOld) + "/0";//交强险开始时间
		String ciEndDateOther = sdf.format(ciEndDateOld) + "/24";//交强险结束时间
		
		String carNo = (String) carInfoMap.get("carNo");
		String engineNo = (String) carInfoMap.get("engineNo");
		String vin = (String) carInfoMap.get("vin");
		String registerDate = (String) carInfoMap.get("enrollDate");
		String userYear = carInfoMap.get("useYears").toString();
		String owner = (String) carInfoMap.get("owner");
		String modelCode = (String) carInfoMap.get("modelCode");
		String brandName = (String) carInfoMap.get("brandName");
		String exhaustScale = (String) carInfoMap.get("vehicleExhaust");
		String seatCount = (String) carInfoMap.get("seatCount");
		String countryNature = "02";
		String modelCodeAlias = (String) carInfoMap.get("modelCodeAlias");
		
		try {
			carNo =  java.net.URLEncoder.encode(carNo,   "gb2312");
			owner =  java.net.URLEncoder.encode(owner,   "gb2312");
			brandName =  java.net.URLEncoder.encode(brandName,   "gb2312");
			modelCodeAlias =  java.net.URLEncoder.encode(modelCodeAlias,   "gb2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		param3 = param3.replace("prpCitemCar.modelDemandNo=", "prpCitemCar.modelDemandNo=" + carInfoMap.get("pmQueryNo"));
		//用户、车辆信息信息
		param3 = param3.replace("bizNo=", "bizNo=" + no);
		param3 = param3.replace("oldPolicyNo=", "oldPolicyNo=" + no);
		param3 = param3.replace("rePolicyNo=", "rePolicyNo=" + no);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		param3 = param3.replace("operationTimeStamp=", "operationTimeStamp=" + sdf.format(new Date()));//操作时间
		param3 = param3.replace("biStartDate=", "biStartDate=" + bizStartDateStr);
		param3 = param3.replace("ciStartDate=", "ciStartDate=" + ciStartDateStr);
		param3 = param3.replace("ciEndDate=", "ciEndDate=" + ciEndDateStr);
		param3 = param3.replace("OLD_STARTDATE_CI=", "OLD_STARTDATE_CI=" + ciStartDateOther);
		param3 = param3.replace("OLD_ENDDATE_CI=", "OLD_ENDDATE_CI=" + ciEndDateOther);
		param3 = param3.replace("prpCmain.startDate=", "prpCmain.startDate=" + bizStartDateStr);
		param3 = param3.replace("prpCmain.endDate=", "prpCmain.endDate=" + bizEndDateStr);
		param3 = param3.replace("prpCmainCI.startDate=", "prpCmainCI.startDate=" + ciStartDateStr);//交强险开始日期
		param3 = param3.replace("prpCmainCI.endDate=", "prpCmainCI.endDate=" + ciEndDateStr);//交强险结束日期
		param3 = param3.replace("prpCplanTemps%5B0%5D.planDate=", "prpCplanTemps%5B0%5D.planDate=" + bizStartDateStr);
		param3 = param3.replace("prpCitemCar.actualValue=", "prpCitemCar.actualValue=" + depreciationPrice);//折旧价格
		param3 = param3.replace("prpCitemCar.licenseNo=", "prpCitemCar.licenseNo=" + carNo);//牌照
		param3 = param3.replace("prpCitemCar.engineNo=", "prpCitemCar.engineNo=" + engineNo);//发动机号
		param3 = param3.replace("prpCitemCar.vinNo=", "prpCitemCar.vinNo=" + vin);//vin
		param3 = param3.replace("prpCitemCar.frameNo=", "prpCitemCar.frameNo=" + vin);//vin
		param3 = param3.replace("prpCitemCar.enrollDate=", "prpCitemCar.enrollDate=" + registerDate);//注册日期
		param3 = param3.replace("enrollDateTrue=", "enrollDateTrue=" + registerDate);//注册日期
		param3 = param3.replace("prpCitemCar.useYears=", "prpCitemCar.useYears=" + userYear);//使用年份
		param3 = param3.replace("owner=", "owner=" + owner);//车主姓名
		param3 = param3.replace("prpCitemCar.modelCode=", "prpCitemCar.modelCode=" + modelCode);//?
		param3 = param3.replace("prpCitemCar.brandName=", "prpCitemCar.brandName=" + brandName);//品牌
		param3 = param3.replace("prpCitemCar.purchasePrice=", "prpCitemCar.purchasePrice=" + purchasePrice);
		param3 = param3.replace("CarActualValueTrue=", "CarActualValueTrue=" + purchasePrice);//新车购置价格
		//param3 = param3.replace("CarActualValueTrue1=", "CarActualValueTrue1=" + purchasePrice1);//？
		param3 = param3.replace("purchasePriceDown=", "purchasePriceDown=" + purchasePrice);
		param3 = param3.replace("purchasePriceOld=", "purchasePriceOld=" + purchasePrice);
		param3 = param3.replace("prpCitemCar.exhaustScale=", "prpCitemCar.exhaustScale=" + exhaustScale);
		param3 = param3.replace("prpCitemCar.seatCount=", "prpCitemCar.seatCount=" + seatCount);
		param3 = param3.replace("seatCountTrue=", "seatCountTrue=" + seatCount);
		param3 = param3.replace("prpCitemCar.countryNature=", "prpCitemCar.countryNature=" + countryNature);
		param3 = param3.replace("prpCitemCar.modelCodeAlias=", "prpCitemCar.modelCodeAlias=" + modelCodeAlias);
		
		return param3;
	}
	
	/**
	 * 拼装非续保的报价参数
	 * @param carInfoMap
	 * @param quoteParam
	 * @return
	 */
	private String makeQuoteParam2(Map<String, Object> carInfoMap, Map<String, String> quoteParam){
		String param3  = "carShipTaxPlatFormFlag=&randomProposalNo=6987058901464069287702 &initemKind_Flag=1&editType=NEW&bizType=PROPOSAL&ABflag=&isBICI=&prpCmain.renewalFlag=&activityFlag=0&INTEGRAL_SWITCH=0&GuangdongSysFlag=&GDREALTIMECARFlag=&GDREALTIMEMOTORFlag=&GDCANCIINFOFlag=0&prpCmain.checkFlag=&prpCmain.othFlag=&prpCmain.dmFlag=&prpCmainCI.dmFlag=&prpCmain.underWriteCode=&prpCmain.underWriteName=&prpCmain.underWriteEndDate=&prpCmain.underWriteFlag=0&prpCmainCI.checkFlag=&prpCmainCI.underWriteFlag=&bizNo=&applyNo=&oldPolicyNo=&bizNoBZ=&bizNoCI=&prpPhead.endorDate=&prpPhead.validDate=&prpPhead.comCode=&sumAmountBI=&isTaxDemand=1&cIInsureFlag=1&bIInsureFlag=1&ciInsureSwitchKindCode=E01,E11,E12,D01,D02,D03&ciInsureSwitchValues=1111111&cIInsureMotorFlag=1&mtPlatformTime=&noPermissionsCarKindCode=E12&isTaxFlag=1&rePolicyNo=&oldPolicyType=&ZGRS_PURCHASEPRICE=200000&ZGRS_LOWESTPREMIUM=0&clauseFlag=&prpCinsuredOwn_Flag=0&prpCinsuredDiv_Flag=0&prpCinsuredBon_Flag=0&relationType=&ciLimitDays=90&udFlag=0&kbFlag=0&sbFlag=0&xzFlag=0&userType=08&noNcheckFlag=0&planFlag=0&R_SWITCH=1&biStartDate=&ciStartDate=&ciStartHour=0&ciEndDate=&ciEndHour=24&AGENTSWITCH=1&JFCDSWITCH=19&carShipTaxFlag=11&commissionFlag=&ICCardCHeck=&riskWarningFlag=&comCodePrefix=11&DAGMobilePhoneNum=&scanSwitch=1000000000&haveScanFlag=0&diffDay=90&cylinderFlag=0&ciPlateVersion=&biPlateVersion=&criterionFlag=0&isQuotatonFlag=2&quotationRisk=DAA&getReplenishfactor=&useYear=9&FREEINSURANCEFLAG=011111&isMotoDrunkDriv=0&immediateFlag=0&immediateFlagCI=0&claimAmountReason=&isQueryCarModelFlag=&isDirectFee=&userCode=020083&comCode=11010286&chgProfitFlag=00&ciPlatTask=&biPlatTask=&upperCostRateBI=&upperCostRateCI=&rescueFundRate=&resureFundFee=&useCarshiptaxFlag=1&taxFreeLicenseNo=&isTaxFree=0&premiumChangeFlag=1&operationTimeStamp=&VEHICLEPLAT=&MOTORFASTTRACK=&motorFastTrack_flag=&MOTORFASTTRACK_INSUREDCODE=&currentDate=&vinModifyFlag=&addPolicyProjectCode=&isAddPolicy=0&commissionView=0&specialflag=&accountCheck=2&projectBak=&projectCodeBT=&projectCodeBTback=&checkTimeFlag=&checkUndwrt=0&carDamagedNum=&insurePayTimes=&claimAdjustValue=&operatorProjectCode=1-1326,2-1326,4-1326,5-1326&lossFlagKind=&chooseFlagCI=0&unitedSaleRelatioStr=&purchasePriceU=&countryNatureU=&insurancefee_reform=0&operateDateForFG=&prpCmainCommon.clauseIssue=1&amountFloat=30&vat_switch=1&BiLastPolicyFlag=&CiLastPolicyFlag=&CiLastEffectiveDate=&CiLastExpireDate=&benchMarkPremium=&BiLastEffectiveDate=&BiLastExpireDate=&lastTotalPremium=&purchasePriceUFlag=&startDateU=&endDateU=&biCiFlagU=&biCiFlagIsChange=&biCiDateIsChange=&switchFlag=0&relatedFlag=0&riskCode=DAA&prpCmain.riskCode=&riskName=&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&agentCodeValidType=U&agentCodeValidValue=106023BJ&agentCodeValidIPPer=&qualificationNo=201951000000800&qualificationName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&OLD_STARTDATE_CI=&OLD_ENDDATE_CI=&prpCmainCommon.greyList=&prpCmainCommon.image=&reinComPany=&reinPolicyNo=&reinStartDate=&reinEndDate=&prpCmain.proposalNo=&prpCmain.policyNo=&prpCmainCI.proposalNo=&prpCmainCI.policyNo=&prpPhead.applyNo=&prpPhead.endorseNo=&prpPheadCI.applyNo=&prpPheadCI.endorseNo=&prpCmain.comCode=11010286&comCodeDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.handler1Code=13154215  &handler1CodeDes=%BA%AB%B6%AB%D0%F1&homePhone=15801381299&officePhone=15801381299&moblie=&checkHandler1Code=1&handler1CodeDesFlag=A&handler1Info=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR__FIELD_SEPARATOR_A_FIELD_SEPARATOR_1211010268&prpCmainCommon.handler1code_uni=1211010268&prpCmain.handlerCode=13154215  &handlerCodeDes=%BA%AB%B6%AB%D0%F1&homePhonebak=&officePhonebak=&mobliebak=&handler1CodeDesFlagbak=&prpCmainCommon.handlercode_uni=1211010268&handlerInfo=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR_1211010268&prpCmain.businessNature=2&businessNatureTranslation=%D7%A8%D2%B5%B4%FA%C0%ED%D2%B5%CE%F1&prpCmain.agentCode=110021100065&prpCmainagentName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&agentType=211047&agentCode=110021100065&tempAgentCode=211047&sumPremiumChgFlag=0&prpCmain.sumPremium1=0&sumPayTax1=0&prpCmain.contractNo=&prpCmain.operateDate=&Today=&OperateDate=&prpCmain.makeCom=11010286&makeComDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.startDate=&prpCmain.startHour=0&prpCmain.endDate=&prpCmain.endHour=24&prpCmain.checkUpCode=&prpCmainCI.startDate=&prpCmainCI.startHour=0&prpCmainCI.endDate=&prpCmainCI.endHour=24&carPremium=0.0&insuredChangeFlag=0&refreshEadFlag=1&imageAdjustPixels=20&prpBatchVehicle.id.contractNo=&prpBatchVehicle.id.serialNo=&prpBatchVehicle.motorCadeNo=&prpBatchVehicle.licenseNo=&prpBatchVehicle.licenseType=&prpBatchVehicle.carKindCode=&prpBatchVehicle.proposalNo=&prpBatchVehicle.policyNo=&prpBatchVehicle.sumAmount=&prpBatchVehicle.sumPremium=&prpBatchVehicle.prpProjectCode=&prpBatchVehicle.coinsProjectCode=&prpBatchVehicle.profitProjectCode=&prpBatchVehicle.facProjectCode=&prpBatchVehicle.flag=&prpBatchVehicle.carId=&prpBatchVehicle.versionNo=&prpBatchMain.discountmode=&minusFlag=&paramIndex=&batchCIFlag=&batchBIFlag=&pageEndorRecorder.endorFlags=&endorDateEdit=&validDateEdit=&endDateEdit=&endorType=&prpPhead.endorType=&generatePtextFlag=0&generatePtextAgainFlag=0&quotationNo=&quotationFlag=&customerCode=&customerFlag=&compensateNo=&dilutiveType=&prpCfixationTemp.discount=&prpCfixationTemp.id.riskCode=&prpCfixationTemp.profits=&prpCfixationTemp.cost=&prpCfixationTemp.taxorAppend=&prpCfixationTemp.payMentR=&prpCfixationTemp.basePayMentR=&prpCfixationTemp.poundAge=&prpCfixationTemp.basePremium=&prpCfixationTemp.riskPremium=&prpCfixationTemp.riskSumPremium=&prpCfixationTemp.signPremium=&prpCfixationTemp.isQuotation=&prpCfixationTemp.riskClass=&prpCfixationTemp.operationInfo=&prpCfixationTemp.realDisCount=&prpCfixationTemp.realProfits=&prpCfixationTemp.realPayMentR=&prpCfixationTemp.remark=&prpCfixationTemp.responseCode=&prpCfixationTemp.errorMessage=&prpCfixationTemp.profitClass=&prpCfixationTemp.costRate=&prpCfixationCITemp.discount=&prpCfixationCITemp.id.riskCode=&prpCfixationCITemp.profits=&prpCfixationCITemp.cost=&prpCfixationCITemp.taxorAppend=&prpCfixationCITemp.payMentR=&prpCfixationCITemp.basePayMentR=&prpCfixationCITemp.poundAge=&prpCfixationCITemp.basePremium=&prpCfixationCITemp.riskPremium=&prpCfixationCITemp.riskSumPremium=&prpCfixationCITemp.signPremium=&prpCfixationCITemp.isQuotation=&prpCfixationCITemp.riskClass=&prpCfixationCITemp.operationInfo=&prpCfixationCITemp.realDisCount=&prpCfixationCITemp.realProfits=&prpCfixationCITemp.realPayMentR=&prpCfixationCITemp.remark=&prpCfixationCITemp.responseCode=&prpCfixationCITemp.errorMessage=&prpCfixationCITemp.profitClass=&prpCfixationCITemp.costRate=&prpCsalesFixes_%5B0%5D.id.proposalNo=&prpCsalesFixes_%5B0%5D.id.serialNo=&prpCsalesFixes_%5B0%5D.comCode=&prpCsalesFixes_%5B0%5D.businessNature=&prpCsalesFixes_%5B0%5D.riskCode=&prpCsalesFixes_%5B0%5D.version=&prpCsalesFixes_%5B0%5D.isForMal=&IS_LOAN_MODIFY=0&kindAndAmount=&isSpecialFlag=&specialEngage=&licenseNoCar=&prpCitemCar.carLoanFlag=&carModelPlatFlag=&updateQuotation=&prpCitemCar.licenseNo1=&prpCitemCar.monopolyFlag=0&prpCitemCar.monopolyCode=&prpCitemCar.monopolyName=&queryCarModelInfo=%B3%B5%D0%CD%D0%C5%CF%A2%C6%BD%CC%A8%BD%BB%BB%A5&prpCitemCar.id.itemNo=1&oldClauseType=F42&prpCitemCar.actualValue=&prpCitemCar.carId=&prpCitemCar.versionNo=&prpCmainCar.newDeviceFlag=&prpCitemCar.otherNature=&prpCitemCar.flag=&newCarFlagValue=2&prpCitemCar.discountType=&prpCitemCar.colorCode=&prpCitemCar.safeDevice=&prpCitemCar.coefficient1=&prpCitemCar.coefficient2=&prpCitemCar.coefficient3=&prpCitemCar.startSiteName=&prpCitemCar.endSiteName=&prpCmainCommon.netsales=0&prpCitemCar.newCarFlag=0&prpCitemCar.noNlocalFlag=0&prpCitemCar.licenseFlag=1&prpCitemCar.licenseNo=&codeLicenseType=LicenseType01,04,LicenseType02,01,LicenseType03,02,LicenseType04,02,LicenseType05,02,LicenseType06,02,LicenseType07,04,LicenseType08,04,LicenseType09,01,LicenseType10,01,LicenseType11,01,LicenseType12,01,LicenseType13,04,LicenseType14,04,LicenseType15,04,	LicenseType16,04,LicenseType17,04,LicenseType18,01,LicenseType19,01,LicenseType20,01,LicenseType21,01,LicenseType22,01,LicenseType23,03,LicenseType24,01,LicenseType25,01,LicenseType31,03,LicenseType32,03,LicenseType90,02&prpCitemCar.licenseType=02&LicenseTypeDes=%D0%A1%D0%CD%C6%FB%B3%B5%BA%C5%C5%C6&prpCitemCar.licenseColorCode=01&LicenseColorCodeDes=%C0%B6&prpCitemCar.engineNo=&prpCitemCar.vinNo=&prpCitemCar.frameNo=&prpCitemCar.carKindCode=A01&CarKindCodeDes=%BF%CD%B3%B5&carKindCodeBak=A01&prpCitemCar.useNatureCode=211&useNatureCodeBak=211&useNatureCodeTrue=211&prpCitemCar.clauseType=F42&clauseTypeBak=F42&prpCitemCar.enrollDate=&enrollDateTrue=&prpCitemCar.useYears=&prpCitemCar.runMiles=10000&taxAbateForPlat=&taxAbateForPlatCarModel=&prpCitemCar.modelDemandNo=&owner=&prpCitemCar.remark=&prpCitemCar.modelCode=&prpCitemCar.brandName=&PurchasePriceScal=10&prpCitemCar.purchasePrice=&CarActualValueTrue=&CarActualValueTrue1=&SZpurchasePriceUp=&SZpurchasePriceDown=&purchasePriceF48=200000&purchasePriceUp=100&purchasePriceDown=&purchasePriceOld=&vehiclePricer=&prpCitemCar.tonCount=0&prpCitemCar.exhaustScale=&prpCitemCar.seatCount=&seatCountTrue=&prpCitemCar.runAreaCode=11&prpCitemCar.carInsuredRelation=1&prpCitemCar.countryNature=&prpCitemCar.cylinderCount=&prpCitemCar.loanVehicleFlag=0&prpCitemCar.transferVehicleFlag=0&prpCitemCar.transferDate=&prpCitemCar.modelCodeAlias=&prpCitemCar.carLotEquQuality=0.00&isQuotation=1&prpCitemCar.fuelType=A&prpCitemCar.carProofType=01&prpCitemCar.isDropinVisitInsure=0&prpCitemCar.energyType=0&prpCitemCar.carProofNo=&prpCitemCar.carProofDate=&prpCmainChannel.assetAgentName=&prpCmainChannel.assetAgentCode=&prpCmainChannel.assetAgentPhone=&SYFlag=0&MTFlag=0&BMFlag=0&STFlag=0&prpCcarDevices_%5B0%5D.deviceName=&prpCcarDevices_%5B0%5D.id.itemNo=1&prpCcarDevices_%5B0%5D.id.proposalNo=&prpCcarDevices_%5B0%5D.id.serialNo=&prpCcarDevices_%5B0%5D.flag=&prpCcarDevices_%5B0%5D.quantity=&prpCcarDevices_%5B0%5D.purchasePrice=&prpCcarDevices_%5B0%5D.buyDate=&prpCcarDevices_%5B0%5D.actualValue=&hidden_index_citemcar=0&editFlag=1&prpCmainCommon.ext2=&configedRepeatTimesLocal=5&prpCinsureds_%5B0%5D.insuredFlag=1&iinsuredFlag=%CD%B6%B1%A3%C8%CB/%B1%BB%B1%A3%CF%D5%C8%CB&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=320684198411040279&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds_%5B0%5D.id.serialNo=1&prpCinsureds_%5B0%5D.insuredType=1&prpCinsureds_%5B0%5D.insuredNature=1&prpCinsureds_%5B0%5D.insuredCode=1100100001900281&prpCinsureds_%5B0%5D.insuredName=%D6%EC%BC%D1%BC%D1&prpCinsureds_%5B0%5D.unitType=&prpCinsureds_%5B0%5D.identifyType=01&prpCinsureds_%5B0%5D.identifyNumber=320684198411040279&prpCinsureds_%5B0%5D.insuredAddress=&prpCinsureds_%5B0%5D.email=&prpCinsureds_%5B0%5D.phoneNumber=&prpCinsureds_%5B0%5D.drivingYears=&prpCinsureds_%5B0%5D.mobile=18610713420&prpCinsureds_%5B0%5D.postCode=&prpCinsureds_%5B0%5D.versionNo=2&prpCinsureds_%5B0%5D.auditStatus=2&prpCinsureds_%5B0%5D.sex=1&prpCinsureds_%5B0%5D.countryCode=CHN&prpCinsureds_%5B0%5D.flag=&prpCinsureds_%5B0%5D.age=32&prpCinsureds_%5B0%5D.drivingLicenseNo=320684198411040279&prpCinsureds_%5B0%5D.drivingCarType=&prpCinsureds_%5B0%5D.appendPrintName=&prpCinsureds_%5B0%5D.causetroubleTimes=&prpCinsureds_%5B0%5D.acceptLicenseDate=&isCheckRepeat_%5B0%5D=&configedRepeatTimes_%5B0%5D=&repeatTimes_%5B0%5D=&prpCinsureds_%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo_%5B0%5D.insuredcode=&idCardCheckInfo_%5B0%5D.insuredFlag=&idCardCheckInfo_%5B0%5D.mobile=&idCardCheckInfo_%5B0%5D.idcardCode=&idCardCheckInfo_%5B0%5D.name=&idCardCheckInfo_%5B0%5D.nation=&idCardCheckInfo_%5B0%5D.birthday=&idCardCheckInfo_%5B0%5D.sex=&idCardCheckInfo_%5B0%5D.address=&idCardCheckInfo_%5B0%5D.issure=&idCardCheckInfo_%5B0%5D.validStartDate=&idCardCheckInfo_%5B0%5D.validEndDate=&idCardCheckInfo_%5B0%5D.samCode=&idCardCheckInfo_%5B0%5D.samType=&idCardCheckInfo_%5B0%5D.flag=0&imobile=186****3420&iauditStatus=2&iversionNo=2&hidden_index_insured=0&prpCinsureds%5B0%5D.insuredFlag=001000000000000000000000000000&iinsuredFlag=%B3%B5%D6%F7&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=%D6%EC%BC%D1%BC%D1&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds%5B0%5D.id.serialNo=1&prpCinsureds%5B0%5D.insuredType=1&prpCinsureds%5B0%5D.insuredNature=1&prpCinsureds%5B0%5D.insuredCode=1100100001900281&prpCinsureds%5B0%5D.insuredName=&prpCinsureds%5B0%5D.unitType=&prpCinsureds%5B0%5D.identifyType=&prpCinsureds%5B0%5D.identifyNumber=&prpCinsureds%5B0%5D.insuredAddress=%B1%B1%BE%A9&prpCinsureds%5B0%5D.email=&prpCinsureds%5B0%5D.phoneNumber=&prpCinsureds%5B0%5D.drivingYears=&prpCinsureds%5B0%5D.mobile=&prpCinsureds%5B0%5D.postCode=&prpCinsureds%5B0%5D.versionNo=2&prpCinsureds%5B0%5D.auditStatus=2&prpCinsureds%5B0%5D.sex=&prpCinsureds%5B0%5D.countryCode=CHN&prpCinsureds%5B0%5D.flag=&prpCinsureds%5B0%5D.age=&prpCinsureds%5B0%5D.drivingLicenseNo=&prpCinsureds%5B0%5D.drivingCarType=&prpCinsureds%5B0%5D.appendPrintName=&prpCinsureds%5B0%5D.causetroubleTimes=&prpCinsureds%5B0%5D.acceptLicenseDate=&isCheckRepeat%5B0%5D=&configedRepeatTimes%5B0%5D=&repeatTimes%5B0%5D=&prpCinsureds%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B0%5D.insuredcode=&idCardCheckInfo%5B0%5D.insuredFlag=&idCardCheckInfo%5B0%5D.mobile=&idCardCheckInfo%5B0%5D.idcardCode=&idCardCheckInfo%5B0%5D.name=&idCardCheckInfo%5B0%5D.nation=&idCardCheckInfo%5B0%5D.birthday=&idCardCheckInfo%5B0%5D.sex=&idCardCheckInfo%5B0%5D.address=&idCardCheckInfo%5B0%5D.issure=&idCardCheckInfo%5B0%5D.validStartDate=&idCardCheckInfo%5B0%5D.validEndDate=&idCardCheckInfo%5B0%5D.samCode=&idCardCheckInfo%5B0%5D.samType=&idCardCheckInfo%5B0%5D.flag=&imobile=186****3420&iauditStatus=2&iversionNo=2&prpCinsureds%5B1%5D.insuredFlag=11000000000000000000000000000A&iinsuredFlag=%CD%B6%B1%A3%C8%CB/%B1%BB%B1%A3%CF%D5%C8%CB&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds%5B1%5D.id.serialNo=1&prpCinsureds%5B1%5D.insuredType=1&prpCinsureds%5B1%5D.insuredNature=1&prpCinsureds%5B1%5D.insuredCode=1100100001900281&prpCinsureds%5B1%5D.insuredName=&prpCinsureds%5B1%5D.unitType=&prpCinsureds%5B1%5D.identifyType=&prpCinsureds%5B1%5D.identifyNumber=&prpCinsureds%5B1%5D.insuredAddress=%B1%B1%BE%A9&prpCinsureds%5B1%5D.email=&prpCinsureds%5B1%5D.phoneNumber=&prpCinsureds%5B1%5D.drivingYears=&prpCinsureds%5B1%5D.mobile=&prpCinsureds%5B1%5D.postCode=&prpCinsureds%5B1%5D.versionNo=2&prpCinsureds%5B1%5D.auditStatus=2&prpCinsureds%5B1%5D.sex=&prpCinsureds%5B1%5D.countryCode=CHN&prpCinsureds%5B1%5D.flag=&prpCinsureds%5B1%5D.age=&prpCinsureds%5B1%5D.drivingLicenseNo=&prpCinsureds%5B1%5D.drivingCarType=&prpCinsureds%5B1%5D.appendPrintName=&prpCinsureds%5B1%5D.causetroubleTimes=&prpCinsureds%5B1%5D.acceptLicenseDate=&isCheckRepeat%5B1%5D=&configedRepeatTimes%5B1%5D=&repeatTimes%5B1%5D=&prpCinsureds%5B1%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B1%5D.insuredcode=&idCardCheckInfo%5B1%5D.insuredFlag=&idCardCheckInfo%5B1%5D.mobile=&idCardCheckInfo%5B1%5D.idcardCode=&idCardCheckInfo%5B1%5D.name=&idCardCheckInfo%5B1%5D.nation=&idCardCheckInfo%5B1%5D.birthday=&idCardCheckInfo%5B1%5D.sex=&idCardCheckInfo%5B1%5D.address=&idCardCheckInfo%5B1%5D.issure=&idCardCheckInfo%5B1%5D.validStartDate=&idCardCheckInfo%5B1%5D.validEndDate=&idCardCheckInfo%5B1%5D.samCode=&idCardCheckInfo%5B1%5D.samType=&idCardCheckInfo%5B1%5D.flag=&imobile=186****3420&iauditStatus=2&iversionNo=2&_insuredFlag_hide=%CD%B6%B1%A3%C8%CB&_insuredFlag_hide=%B1%BB%B1%A3%CF%D5%C8%CB&_insuredFlag_hide=%B3%B5%D6%F7&_insuredFlag_hide=%D6%B8%B6%A8%BC%DD%CA%BB%C8%CB&_insuredFlag_hide=%CA%DC%D2%E6%C8%CB&_insuredFlag_hide=%B8%DB%B0%C4%B3%B5%B3%B5%D6%F7&_insuredFlag_hide=%C1%AA%CF%B5%C8%CB&_insuredFlag=0&_insuredFlag_hide=%CE%AF%CD%D0%C8%CB&_resident=&_insuredType=1&_insuredCode=&_insuredName=&customerURL=http://10.134.136.48:8300/cif&_isCheckRepeat=&_configedRepeatTimes=&_repeatTimes=&_identifyType=01&_identifyNumber=&_unifiedSocialCreditCode=&_mobile=&_mobile1=&_sex=1&_age=&_drivingYears=&_countryCode=CHN&_insuredAddress=&_postCode=&_appendPrintName=&group_code=&_auditStatus=&_auditStatusDes=&_versionNo=&_drivingLicenseNo=&_email=&idCardCheckInfo.idcardCode=&idCardCheckInfo.name=&idCardCheckInfo.nation=&idCardCheckInfo.birthday=&idCardCheckInfo.sex=&idCardCheckInfo.address=&idCardCheckInfo.issure=&idCardCheckInfo.validStartDate=&idCardCheckInfo.validEndDate=&idCardCheckInfo.samCode=&idCardCheckInfo.samType=&idCardCheckInfo.flag=0&_drivingCarType=&CarKindLicense=&_causetroubleTimes=&_acceptLicenseDate=&prpCmainCar.agreeDriverFlag=&updateIndex=-1&prpBatchProposal.profitType=&motorFastTrack_Amount=&insurancefee_reform=0&prpCmainCommon.clauseIssue=1&prpCprofitDetailsTemp_%5B0%5D.chooseFlag=&prpCprofitDetailsTemp_%5B0%5D.profitName=&prpCprofitDetailsTemp_%5B0%5D.condition=&profitRateTemp_%5B0%5D=&prpCprofitDetailsTemp_%5B0%5D.profitRate=&prpCprofitDetailsTemp_%5B0%5D.profitRateMin=&prpCprofitDetailsTemp_%5B0%5D.profitRateMax=&prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=&prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=&prpCprofitDetailsTemp_%5B0%5D.id.profitCode=&prpCprofitDetailsTemp_%5B0%5D.id.serialNo=1&prpCprofitDetailsTemp_%5B0%5D.id.profitType=&prpCprofitDetailsTemp_%5B0%5D.kindCode=&prpCprofitDetailsTemp_%5B0%5D.conditionCode=&prpCprofitDetailsTemp_%5B0%5D.flag=&prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on&serialNo_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.profitName=&prpCprofitFactorsTemp_%5B0%5D.condition=&rateTemp_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.rate=&prpCprofitFactorsTemp_%5B0%5D.lowerRate=&prpCprofitFactorsTemp_%5B0%5D.upperRate=&prpCprofitFactorsTemp_%5B0%5D.id.profitCode=&prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=&prpCprofitFactorsTemp_%5B0%5D.flag=&prpCitemKind.shortRateFlag=2&prpCitemKind.shortRate=100&prpCitemKind.currency=CNY&prpCmainCommon.groupFlag=0&sumBenchPremium=&prpCmain.discount=&prpCmain.sumPremium=&premiumF48=5000&prpCmain.sumNetPremium=&prpCmain.sumTaxPremium=&passengersSwitchFlag=&prpCitemKindsTemp%5B0%5D.min=&prpCitemKindsTemp%5B0%5D.max=&prpCitemKindsTemp%5B0%5D.chooseFlag=&prpCitemKindsTemp%5B0%5D.itemKindNo=&prpCitemKindsTemp%5B0%5D.clauseCode=050002&prpCitemKindsTemp%5B0%5D.kindCode=050200&prpCitemKindsTemp%5B0%5D.kindName=%BB%FA%B6%AF%B3%B5%CB%F0%CA%A7%B1%A3%CF%D5&prpCitemKindsTemp%5B0%5D.unitAmount=&prpCitemKindsTemp%5B0%5D.quantity=&prpCitemKindsTemp%5B0%5D.specialFlag=on&prpCitemKindsTemp%5B0%5D.amount=&prpCitemKindsTemp%5B0%5D.calculateFlag=Y11Y000&prpCitemKindsTemp%5B0%5D.startDate=&prpCitemKindsTemp%5B0%5D.startHour=&prpCitemKindsTemp%5B0%5D.endDate=&prpCitemKindsTemp%5B0%5D.endHour=&relateSpecial%5B0%5D=050911&coachCar%5B0%5D=050941&prpCitemKindsTemp%5B0%5D.flag= 100000&prpCitemKindsTemp%5B0%5D.basePremium=&prpCitemKindsTemp%5B0%5D.rate=&prpCitemKindsTemp%5B0%5D.benchMarkPremium=&prpCitemKindsTemp%5B0%5D.disCount=&prpCitemKindsTemp%5B0%5D.premium=&prpCitemKindsTemp%5B0%5D.netPremium=&prpCitemKindsTemp%5B0%5D.taxPremium=&prpCitemKindsTemp%5B0%5D.taxRate=&prpCitemKindsTemp%5B0%5D.dutyFlag=&prpCitemKindsTemp%5B1%5D.min=&prpCitemKindsTemp%5B1%5D.max=&prpCitemKindsTemp%5B1%5D.chooseFlag=&prpCitemKindsTemp%5B1%5D.itemKindNo=&prpCitemKindsTemp%5B1%5D.clauseCode=050005&prpCitemKindsTemp%5B1%5D.kindCode=050500&prpCitemKindsTemp%5B1%5D.kindName=%B5%C1%C7%C0%CF%D5&prpCitemKindsTemp%5B1%5D.unitAmount=&prpCitemKindsTemp%5B1%5D.quantity=&prpCitemKindsTemp%5B1%5D.specialFlag=on&prpCitemKindsTemp%5B1%5D.amount=&prpCitemKindsTemp%5B1%5D.calculateFlag=N11Y000&prpCitemKindsTemp%5B1%5D.startDate=&prpCitemKindsTemp%5B1%5D.startHour=&prpCitemKindsTemp%5B1%5D.endDate=&prpCitemKindsTemp%5B1%5D.endHour=&relateSpecial%5B1%5D=050921&coachCar%5B1%5D=&prpCitemKindsTemp%5B1%5D.flag= 100000&prpCitemKindsTemp%5B1%5D.basePremium=&prpCitemKindsTemp%5B1%5D.rate=&prpCitemKindsTemp%5B1%5D.benchMarkPremium=&prpCitemKindsTemp%5B1%5D.disCount=&prpCitemKindsTemp%5B1%5D.premium=&prpCitemKindsTemp%5B1%5D.netPremium=&prpCitemKindsTemp%5B1%5D.taxPremium=&prpCitemKindsTemp%5B1%5D.taxRate=&prpCitemKindsTemp%5B1%5D.dutyFlag=&prpCitemKindsTemp%5B2%5D.min=&prpCitemKindsTemp%5B2%5D.max=&prpCitemKindsTemp%5B2%5D.chooseFlag=&prpCitemKindsTemp%5B2%5D.itemKindNo=&prpCitemKindsTemp%5B2%5D.clauseCode=050003&prpCitemKindsTemp%5B2%5D.kindCode=050600&prpCitemKindsTemp%5B2%5D.kindName=%B5%DA%C8%FD%D5%DF%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B2%5D.unitAmount=&prpCitemKindsTemp%5B2%5D.quantity=&prpCitemKindsTemp%5B2%5D.specialFlag=on&prpCitemKindsTemp%5B2%5D.amount=&prpCitemKindsTemp%5B2%5D.calculateFlag=Y21Y000&prpCitemKindsTemp%5B2%5D.startDate=&prpCitemKindsTemp%5B2%5D.startHour=&prpCitemKindsTemp%5B2%5D.endDate=&prpCitemKindsTemp%5B2%5D.endHour=&relateSpecial%5B2%5D=050912&coachCar%5B2%5D=050942&prpCitemKindsTemp%5B2%5D.flag= 100000&prpCitemKindsTemp%5B2%5D.basePremium=&prpCitemKindsTemp%5B2%5D.rate=&prpCitemKindsTemp%5B2%5D.benchMarkPremium=&prpCitemKindsTemp%5B2%5D.disCount=&prpCitemKindsTemp%5B2%5D.premium=&prpCitemKindsTemp%5B2%5D.netPremium=&prpCitemKindsTemp%5B2%5D.taxPremium=&prpCitemKindsTemp%5B2%5D.taxRate=&prpCitemKindsTemp%5B2%5D.dutyFlag=&prpCitemKindsTemp%5B3%5D.min=&prpCitemKindsTemp%5B3%5D.max=&prpCitemKindsTemp%5B3%5D.chooseFlag=&prpCitemKindsTemp%5B3%5D.itemKindNo=&prpCitemKindsTemp%5B3%5D.clauseCode=050004&prpCitemKindsTemp%5B3%5D.kindCode=050701&prpCitemKindsTemp%5B3%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9&prpCitemKindsTemp%5B3%5D.unitAmount=&prpCitemKindsTemp%5B3%5D.quantity=&prpCitemKindsTemp%5B3%5D.specialFlag=on&prpCitemKindsTemp%5B3%5D.amount=&prpCitemKindsTemp%5B3%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B3%5D.startDate=&prpCitemKindsTemp%5B3%5D.startHour=&prpCitemKindsTemp%5B3%5D.endDate=&prpCitemKindsTemp%5B3%5D.endHour=&relateSpecial%5B3%5D=050928&coachCar%5B3%5D=050943&prpCitemKindsTemp%5B3%5D.flag= 100000&prpCitemKindsTemp%5B3%5D.basePremium=&prpCitemKindsTemp%5B3%5D.rate=&prpCitemKindsTemp%5B3%5D.benchMarkPremium=&prpCitemKindsTemp%5B3%5D.disCount=&prpCitemKindsTemp%5B3%5D.premium=&prpCitemKindsTemp%5B3%5D.netPremium=&prpCitemKindsTemp%5B3%5D.taxPremium=&prpCitemKindsTemp%5B3%5D.taxRate=&prpCitemKindsTemp%5B3%5D.dutyFlag=&prpCitemKindsTemp%5B4%5D.min=&prpCitemKindsTemp%5B4%5D.max=&prpCitemKindsTemp%5B4%5D.chooseFlag=&prpCitemKindsTemp%5B4%5D.itemKindNo=&prpCitemKindsTemp%5B4%5D.clauseCode=050004&prpCitemKindsTemp%5B4%5D.kindCode=050702&prpCitemKindsTemp%5B4%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9&prpCitemKindsTemp%5B4%5D.unitAmount=&prpCitemKindsTemp%5B4%5D.quantity=&prpCitemKindsTemp%5B4%5D.specialFlag=on&prpCitemKindsTemp%5B4%5D.amount=&prpCitemKindsTemp%5B4%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B4%5D.startDate=&prpCitemKindsTemp%5B4%5D.startHour=&prpCitemKindsTemp%5B4%5D.endDate=&prpCitemKindsTemp%5B4%5D.endHour=&relateSpecial%5B4%5D=050929&coachCar%5B4%5D=050944&prpCitemKindsTemp%5B4%5D.flag= 100000&prpCitemKindsTemp%5B4%5D.basePremium=&prpCitemKindsTemp%5B4%5D.rate=&prpCitemKindsTemp%5B4%5D.benchMarkPremium=&prpCitemKindsTemp%5B4%5D.disCount=&prpCitemKindsTemp%5B4%5D.premium=&prpCitemKindsTemp%5B4%5D.netPremium=&prpCitemKindsTemp%5B4%5D.taxPremium=&prpCitemKindsTemp%5B4%5D.taxRate=&prpCitemKindsTemp%5B4%5D.dutyFlag=&prpCitemKindsTemp%5B5%5D.min=&prpCitemKindsTemp%5B5%5D.max=&prpCitemKindsTemp%5B5%5D.chooseFlag=&prpCitemKindsTemp%5B5%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B5%5D.clauseCode=050006&prpCitemKindsTemp%5B5%5D.kindCode=050210&relateSpecial%5B5%5D=050922&prpCitemKindsTemp%5B5%5D.kindName=%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B5%5D.specialFlag=on&prpCitemKindsTemp%5B5%5D.amount=&prpCitemKindsTemp%5B5%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B5%5D.startDate=&prpCitemKindsTemp%5B5%5D.startHour=&prpCitemKindsTemp%5B5%5D.endDate=&prpCitemKindsTemp%5B5%5D.endHour=&prpCitemKindsTemp%5B5%5D.flag= 200000&prpCitemKindsTemp%5B5%5D.basePremium=&prpCitemKindsTemp%5B5%5D.rate=&prpCitemKindsTemp%5B5%5D.benchMarkPremium=&prpCitemKindsTemp%5B5%5D.disCount=&prpCitemKindsTemp%5B5%5D.premium=&prpCitemKindsTemp%5B5%5D.netPremium=&prpCitemKindsTemp%5B5%5D.taxPremium=&prpCitemKindsTemp%5B5%5D.taxRate=&prpCitemKindsTemp%5B5%5D.dutyFlag=&prpCitemKindsTemp%5B6%5D.min=&prpCitemKindsTemp%5B6%5D.max=&prpCitemKindsTemp%5B6%5D.chooseFlag=&prpCitemKindsTemp%5B6%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B6%5D.clauseCode=050008&prpCitemKindsTemp%5B6%5D.kindCode=050231&relateSpecial%5B6%5D=      &prpCitemKindsTemp%5B6%5D.kindName=%B2%A3%C1%A7%B5%A5%B6%C0%C6%C6%CB%E9%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B6%5D.modeCode=&prpCitemKindsTemp%5B6%5D.amount=&prpCitemKindsTemp%5B6%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B6%5D.startDate=&prpCitemKindsTemp%5B6%5D.startHour=&prpCitemKindsTemp%5B6%5D.endDate=&prpCitemKindsTemp%5B6%5D.endHour=&prpCitemKindsTemp%5B6%5D.flag= 200000&prpCitemKindsTemp%5B6%5D.basePremium=&prpCitemKindsTemp%5B6%5D.rate=&prpCitemKindsTemp%5B6%5D.benchMarkPremium=&prpCitemKindsTemp%5B6%5D.disCount=&prpCitemKindsTemp%5B6%5D.premium=&prpCitemKindsTemp%5B6%5D.netPremium=&prpCitemKindsTemp%5B6%5D.taxPremium=&prpCitemKindsTemp%5B6%5D.taxRate=&prpCitemKindsTemp%5B6%5D.dutyFlag=&prpCitemKindsTemp%5B7%5D.min=&prpCitemKindsTemp%5B7%5D.max=&prpCitemKindsTemp%5B7%5D.chooseFlag=&prpCitemKindsTemp%5B7%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B7%5D.clauseCode=050016&prpCitemKindsTemp%5B7%5D.kindCode=050310&relateSpecial%5B7%5D=      &prpCitemKindsTemp%5B7%5D.kindName=%D7%D4%C8%BC%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B7%5D.amount=&prpCitemKindsTemp%5B7%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B7%5D.startDate=&prpCitemKindsTemp%5B7%5D.startHour=&prpCitemKindsTemp%5B7%5D.endDate=&prpCitemKindsTemp%5B7%5D.endHour=&prpCitemKindsTemp%5B7%5D.flag= 200000&prpCitemKindsTemp%5B7%5D.basePremium=&prpCitemKindsTemp%5B7%5D.rate=&prpCitemKindsTemp%5B7%5D.benchMarkPremium=&prpCitemKindsTemp%5B7%5D.disCount=&prpCitemKindsTemp%5B7%5D.premium=&prpCitemKindsTemp%5B7%5D.netPremium=&prpCitemKindsTemp%5B7%5D.taxPremium=&prpCitemKindsTemp%5B7%5D.taxRate=&prpCitemKindsTemp%5B7%5D.dutyFlag=&prpCitemKindsTemp%5B8%5D.min=&prpCitemKindsTemp%5B8%5D.max=&prpCitemKindsTemp%5B8%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B8%5D.clauseCode=050021&prpCitemKindsTemp%5B8%5D.kindCode=050370&relateSpecial%5B8%5D=      &prpCitemKindsTemp%5B8%5D.kindName=%D4%BC%B6%A8%C7%F8%D3%F2%CD%A8%D0%D0%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B8%5D.modeCode=1&prpCitemKindsTemp%5B8%5D.amount=5000.00&prpCitemKindsTemp%5B8%5D.calculateFlag=N12N000&prpCitemKindsTemp%5B8%5D.startDate=&prpCitemKindsTemp%5B8%5D.startHour=&prpCitemKindsTemp%5B8%5D.endDate=&prpCitemKindsTemp%5B8%5D.endHour=&prpCitemKindsTemp%5B8%5D.flag= 200000&prpCitemKindsTemp%5B8%5D.basePremium=&prpCitemKindsTemp%5B8%5D.rate=&prpCitemKindsTemp%5B8%5D.benchMarkPremium=&prpCitemKindsTemp%5B8%5D.disCount=&prpCitemKindsTemp%5B8%5D.premium=&prpCitemKindsTemp%5B8%5D.netPremium=&prpCitemKindsTemp%5B8%5D.taxPremium=&prpCitemKindsTemp%5B8%5D.taxRate=&prpCitemKindsTemp%5B8%5D.dutyFlag=&prpCitemKindsTemp%5B9%5D.min=&prpCitemKindsTemp%5B9%5D.max=&prpCitemKindsTemp%5B9%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B9%5D.clauseCode=050032&prpCitemKindsTemp%5B9%5D.kindCode=050611&relateSpecial%5B9%5D=      &prpCitemKindsTemp%5B9%5D.kindName=%B7%A8%C2%C9%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B9%5D.amount=10000.00&prpCitemKindsTemp%5B9%5D.calculateFlag=N22N000&prpCitemKindsTemp%5B9%5D.startDate=&prpCitemKindsTemp%5B9%5D.startHour=&prpCitemKindsTemp%5B9%5D.endDate=&prpCitemKindsTemp%5B9%5D.endHour=&prpCitemKindsTemp%5B9%5D.flag= 200000&prpCitemKindsTemp%5B9%5D.basePremium=&prpCitemKindsTemp%5B9%5D.rate=&prpCitemKindsTemp%5B9%5D.benchMarkPremium=&prpCitemKindsTemp%5B9%5D.disCount=&prpCitemKindsTemp%5B9%5D.premium=&prpCitemKindsTemp%5B9%5D.netPremium=&prpCitemKindsTemp%5B9%5D.taxPremium=&prpCitemKindsTemp%5B9%5D.taxRate=&prpCitemKindsTemp%5B9%5D.dutyFlag=&prpCitemKindsTemp%5B10%5D.min=&prpCitemKindsTemp%5B10%5D.max=&prpCitemKindsTemp%5B10%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B10%5D.clauseCode=050033&prpCitemKindsTemp%5B10%5D.kindCode=050630&relateSpecial%5B10%5D=050926&prpCitemKindsTemp%5B10%5D.kindName=%B8%BD%BC%D3%D3%CD%CE%DB%CE%DB%C8%BE%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B10%5D.amount=50000.00&prpCitemKindsTemp%5B10%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B10%5D.startDate=&prpCitemKindsTemp%5B10%5D.startHour=&prpCitemKindsTemp%5B10%5D.endDate=&prpCitemKindsTemp%5B10%5D.endHour=&prpCitemKindsTemp%5B10%5D.flag= 200000&prpCitemKindsTemp%5B10%5D.basePremium=&prpCitemKindsTemp%5B10%5D.rate=&prpCitemKindsTemp%5B10%5D.benchMarkPremium=&prpCitemKindsTemp%5B10%5D.disCount=&prpCitemKindsTemp%5B10%5D.premium=&prpCitemKindsTemp%5B10%5D.netPremium=&prpCitemKindsTemp%5B10%5D.taxPremium=&prpCitemKindsTemp%5B10%5D.taxRate=&prpCitemKindsTemp%5B10%5D.dutyFlag=&prpCitemKindsTemp%5B21%5D.chooseFlag=&prpCitemKindsTemp%5B21%5D.itemKindNo=&prpCitemKindsTemp%5B21%5D.startDate=&prpCitemKindsTemp%5B21%5D.kindCode=050291&prpCitemKindsTemp%5B21%5D.kindName=%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B21%5D.startHour=&prpCitemKindsTemp%5B21%5D.endDate=&prpCitemKindsTemp%5B21%5D.endHour=&prpCitemKindsTemp%5B21%5D.calculateFlag=N32Y000&relateSpecial%5B16%5D=050924&prpCitemKindsTemp%5B21%5D.flag= 200000&prpCitemKindsTemp%5B21%5D.basePremium=&prpCitemKindsTemp%5B21%5D.specialFlag=on&prpCitemKindsTemp%5B21%5D.amount=&prpCitemKindsTemp%5B21%5D.rate=&prpCitemKindsTemp%5B21%5D.benchMarkPremium=&prpCitemKindsTemp%5B21%5D.disCount=&prpCitemKindsTemp%5B21%5D.premium=&prpCitemKindsTemp%5B21%5D.netPremium=&prpCitemKindsTemp%5B21%5D.taxPremium=&prpCitemKindsTemp%5B21%5D.taxRate=&prpCitemKindsTemp%5B21%5D.dutyFlag=&prpCitemKindsTemp.itemKindSpecialSumPremium=&prpCitemKindsTemp%5B16%5D.chooseFlag=&prpCitemKindsTemp%5B16%5D.itemKindNo=&prpCitemKindsTemp%5B16%5D.startDate=&prpCitemKindsTemp%5B16%5D.kindCode=050911&prpCitemKindsTemp%5B16%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%CB%F0%CF%D5%A3%A9&prpCitemKindsTemp%5B16%5D.startHour=&prpCitemKindsTemp%5B16%5D.endDate=&prpCitemKindsTemp%5B16%5D.endHour=&prpCitemKindsTemp%5B16%5D.calculateFlag=N33Y000&relateSpecial%5B11%5D=&prpCitemKindsTemp%5B16%5D.flag= 200000&prpCitemKindsTemp%5B16%5D.basePremium=&prpCitemKindsTemp%5B16%5D.amount=&prpCitemKindsTemp%5B16%5D.rate=&prpCitemKindsTemp%5B16%5D.benchMarkPremium=&prpCitemKindsTemp%5B16%5D.disCount=&prpCitemKindsTemp%5B16%5D.premium=&prpCitemKindsTemp%5B16%5D.netPremium=&prpCitemKindsTemp%5B16%5D.taxPremium=&prpCitemKindsTemp%5B16%5D.taxRate=&prpCitemKindsTemp%5B16%5D.dutyFlag=&prpCitemKindsTemp%5B17%5D.chooseFlag=&prpCitemKindsTemp%5B17%5D.itemKindNo=&prpCitemKindsTemp%5B17%5D.startDate=&prpCitemKindsTemp%5B17%5D.kindCode=050921&prpCitemKindsTemp%5B17%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%BB%FA%B6%AF%B3%B5%B5%C1%C7%C0%CF%D5%A3%A9&prpCitemKindsTemp%5B17%5D.startHour=&prpCitemKindsTemp%5B17%5D.endDate=&prpCitemKindsTemp%5B17%5D.endHour=&prpCitemKindsTemp%5B17%5D.calculateFlag=N33Y000&relateSpecial%5B12%5D=&prpCitemKindsTemp%5B17%5D.flag= 200000&prpCitemKindsTemp%5B17%5D.basePremium=&prpCitemKindsTemp%5B17%5D.amount=&prpCitemKindsTemp%5B17%5D.rate=&prpCitemKindsTemp%5B17%5D.benchMarkPremium=&prpCitemKindsTemp%5B17%5D.disCount=&prpCitemKindsTemp%5B17%5D.premium=&prpCitemKindsTemp%5B17%5D.netPremium=&prpCitemKindsTemp%5B17%5D.taxPremium=&prpCitemKindsTemp%5B17%5D.taxRate=&prpCitemKindsTemp%5B17%5D.dutyFlag=&prpCitemKindsTemp%5B18%5D.chooseFlag=&prpCitemKindsTemp%5B18%5D.itemKindNo=&prpCitemKindsTemp%5B18%5D.startDate=&prpCitemKindsTemp%5B18%5D.kindCode=050912&prpCitemKindsTemp%5B18%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%C8%FD%D5%DF%CF%D5%A3%A9&prpCitemKindsTemp%5B18%5D.startHour=&prpCitemKindsTemp%5B18%5D.endDate=&prpCitemKindsTemp%5B18%5D.endHour=&prpCitemKindsTemp%5B18%5D.calculateFlag=N33Y000&relateSpecial%5B13%5D=&prpCitemKindsTemp%5B18%5D.flag= 200000&prpCitemKindsTemp%5B18%5D.basePremium=&prpCitemKindsTemp%5B18%5D.amount=&prpCitemKindsTemp%5B18%5D.rate=&prpCitemKindsTemp%5B18%5D.benchMarkPremium=&prpCitemKindsTemp%5B18%5D.disCount=&prpCitemKindsTemp%5B18%5D.premium=&prpCitemKindsTemp%5B18%5D.netPremium=&prpCitemKindsTemp%5B18%5D.taxPremium=&prpCitemKindsTemp%5B18%5D.taxRate=&prpCitemKindsTemp%5B18%5D.dutyFlag=&prpCitemKindsTemp%5B19%5D.chooseFlag=&prpCitemKindsTemp%5B19%5D.itemKindNo=&prpCitemKindsTemp%5B19%5D.startDate=&prpCitemKindsTemp%5B19%5D.kindCode=050928&prpCitemKindsTemp%5B19%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9%A3%A9&prpCitemKindsTemp%5B19%5D.startHour=&prpCitemKindsTemp%5B19%5D.endDate=&prpCitemKindsTemp%5B19%5D.endHour=&prpCitemKindsTemp%5B19%5D.calculateFlag=N33Y000&relateSpecial%5B14%5D=&prpCitemKindsTemp%5B19%5D.flag= 200000&prpCitemKindsTemp%5B19%5D.basePremium=&prpCitemKindsTemp%5B19%5D.amount=&prpCitemKindsTemp%5B19%5D.rate=&prpCitemKindsTemp%5B19%5D.benchMarkPremium=&prpCitemKindsTemp%5B19%5D.disCount=&prpCitemKindsTemp%5B19%5D.premium=&prpCitemKindsTemp%5B19%5D.netPremium=&prpCitemKindsTemp%5B19%5D.taxPremium=&prpCitemKindsTemp%5B19%5D.taxRate=&prpCitemKindsTemp%5B19%5D.dutyFlag=&prpCitemKindsTemp%5B20%5D.chooseFlag=&prpCitemKindsTemp%5B20%5D.itemKindNo=&prpCitemKindsTemp%5B20%5D.startDate=&prpCitemKindsTemp%5B20%5D.kindCode=050929&prpCitemKindsTemp%5B20%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9%A3%A9&prpCitemKindsTemp%5B20%5D.startHour=&prpCitemKindsTemp%5B20%5D.endDate=&prpCitemKindsTemp%5B20%5D.endHour=&prpCitemKindsTemp%5B20%5D.calculateFlag=N33Y000&relateSpecial%5B15%5D=&prpCitemKindsTemp%5B20%5D.flag= 200000&prpCitemKindsTemp%5B20%5D.basePremium=&prpCitemKindsTemp%5B20%5D.amount=&prpCitemKindsTemp%5B20%5D.rate=&prpCitemKindsTemp%5B20%5D.benchMarkPremium=&prpCitemKindsTemp%5B20%5D.disCount=&prpCitemKindsTemp%5B20%5D.premium=&prpCitemKindsTemp%5B20%5D.netPremium=&prpCitemKindsTemp%5B20%5D.taxPremium=&prpCitemKindsTemp%5B20%5D.taxRate=&prpCitemKindsTemp%5B20%5D.dutyFlag=&prpCitemKindsTemp%5B22%5D.chooseFlag=&prpCitemKindsTemp%5B22%5D.itemKindNo=&prpCitemKindsTemp%5B22%5D.startDate=&prpCitemKindsTemp%5B22%5D.kindCode=050924&prpCitemKindsTemp%5B22%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B22%5D.startHour=&prpCitemKindsTemp%5B22%5D.endDate=&prpCitemKindsTemp%5B22%5D.endHour=&prpCitemKindsTemp%5B22%5D.calculateFlag=N33Y000&relateSpecial%5B17%5D=&prpCitemKindsTemp%5B22%5D.flag= 200000&prpCitemKindsTemp%5B22%5D.basePremium=&prpCitemKindsTemp%5B22%5D.amount=&prpCitemKindsTemp%5B22%5D.rate=&prpCitemKindsTemp%5B22%5D.benchMarkPremium=&prpCitemKindsTemp%5B22%5D.disCount=&prpCitemKindsTemp%5B22%5D.premium=&prpCitemKindsTemp%5B22%5D.netPremium=&prpCitemKindsTemp%5B22%5D.taxPremium=&prpCitemKindsTemp%5B22%5D.taxRate=&prpCitemKindsTemp%5B22%5D.dutyFlag=&prpCitemKindsTemp%5B23%5D.chooseFlag=&prpCitemKindsTemp%5B23%5D.itemKindNo=&prpCitemKindsTemp%5B23%5D.startDate=&prpCitemKindsTemp%5B23%5D.kindCode=050922&prpCitemKindsTemp%5B23%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B23%5D.startHour=&prpCitemKindsTemp%5B23%5D.endDate=&prpCitemKindsTemp%5B23%5D.endHour=&prpCitemKindsTemp%5B23%5D.calculateFlag=N33Y000&relateSpecial%5B18%5D=&prpCitemKindsTemp%5B23%5D.flag= 200000&prpCitemKindsTemp%5B23%5D.basePremium=&prpCitemKindsTemp%5B23%5D.amount=&prpCitemKindsTemp%5B23%5D.rate=&prpCitemKindsTemp%5B23%5D.benchMarkPremium=&prpCitemKindsTemp%5B23%5D.disCount=&prpCitemKindsTemp%5B23%5D.premium=&prpCitemKindsTemp%5B23%5D.netPremium=&prpCitemKindsTemp%5B23%5D.taxPremium=&prpCitemKindsTemp%5B23%5D.taxRate=&prpCitemKindsTemp%5B23%5D.dutyFlag=&hidden_index_itemKind=11&hidden_index_profitDetial=0&prpCitemKindsTemp_%5B0%5D.chooseFlag=&prpCitemKindsTemp_%5B0%5D.itemKindNo=&prpCitemKindsTemp_%5B0%5D.startDate=&prpCitemKindsTemp_%5B0%5D.kindCode=&prpCitemKindsTemp_%5B0%5D.kindName=&prpCitemKindsTemp_%5B0%5D.startHour=&prpCitemKindsTemp_%5B0%5D.endDate=&prpCitemKindsTemp_%5B0%5D.endHour=&prpCitemKindsTemp_%5B0%5D.calculateFlag=&relateSpecial_%5B0%5D=&prpCitemKindsTemp_%5B0%5D.flag=&prpCitemKindsTemp_%5B0%5D.basePremium=&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.rate=&prpCitemKindsTemp_%5B0%5D.benchMarkPremium=&prpCitemKindsTemp_%5B0%5D.disCount=&prpCitemKindsTemp_%5B0%5D.premium=&prpCitemKindsTemp_%5B0%5D.netPremium=&prpCitemKindsTemp_%5B0%5D.taxPremium=&prpCitemKindsTemp_%5B0%5D.taxRate=&prpCitemKindsTemp_%5B0%5D.dutyFlag=&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.value=&prpCitemKindsTemp_%5B0%5D.value=50&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.modeCode=10&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.value=1000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=10000&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=60&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=90&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.amount=50000.00&prpCitemKindsTemp_%5B0%5D.amount=10000.00&prpCitemKindsTemp_%5B0%5D.amount=5000.00&itemKindLoadFlag=&BIdemandNo=&BIdemandTime=&bIRiskWarningType=&noDamageYearsBIPlat=0&prpCitemCarExt.lastDamagedBI=0&lastDamagedBITemp=&DAZlastDamagedBI=&prpCitemCarExt.thisDamagedBI=0&prpCitemCarExt.noDamYearsBI=0&noDamYearsBINumber=0&prpCitemCarExt.lastDamagedCI=0&BIDemandClaim_Flag=&BiInsureDemandPay_%5B0%5D.id.serialNo=&BiInsureDemandPay_%5B0%5D.payCompany=&BiInsureDemandPay_%5B0%5D.claimregistrationno=&BiInsureDemandPay_%5B0%5D.compensateNo=&BiInsureDemandPay_%5B0%5D.lossTime=&BiInsureDemandPay_%5B0%5D.endcCaseTime=&PrpCmain_%5B0%5D.startDate=&PrpCmain_%5B0%5D.endDate=&BiInsureDemandPay_%5B0%5D.lossFee=&BiInsureDemandPay_%5B0%5D.payType=&BiInsureDemandPay_%5B0%5D.personpayType=&bIRiskWarningClaimItems_%5B0%5D.id.serialNo=&bIRiskWarningClaimItems_%5B0%5D.riskWarningType=&bIRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&bIRiskWarningClaimItems_%5B0%5D.insurerCode=&bIRiskWarningClaimItems_%5B0%5D.lossTime=&bIRiskWarningClaimItems_%5B0%5D.lossArea=&prpCitemKindCI.shortRate=100&cIBPFlag=1&prpCitemKindCI.unitAmount=0&prpCitemKindCI.id.itemKindNo=&prpCitemKindCI.kindCode=050100&prpCitemKindCI.kindName=%BB%FA%B6%AF%B3%B5%BD%BB%CD%A8%CA%C2%B9%CA%C7%BF%D6%C6%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindCI.calculateFlag=Y&prpCitemKindCI.basePremium=&prpCitemKindCI.quantity=1&prpCitemKindCI.amount=&prpCitemKindCI.deductible=&prpCitemKindCI.adjustRate=1&prpCitemKindCI.rate=0&prpCitemKindCI.benchMarkPremium=&prpCitemKindCI.disCount=1&prpCitemKindCI.premium=&prpCitemKindCI.flag=&prpCitemKindCI.netPremium=&prpCitemKindCI.taxPremium=&prpCitemKindCI.taxRate=&prpCitemKindCI.dutyFlag=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCitemCarExt_CI.rateRloatFlag=01&prpCitemCarExt_CI.noDamYearsCI=1&prpCitemCarExt_CI.lastDamagedCI=0&prpCitemCarExt_CI.flag=&prpCitemCarExt_CI.damFloatRatioCI=0&prpCitemCarExt_CI.offFloatRatioCI=0&prpCitemCarExt_CI.thisDamagedCI=0&prpCitemCarExt_CI.flag=&hidden_index_ctraffic_NOPlat_Drink=0&hidden_index_ctraffic_NOPlat=0&ciInsureDemand.demandNo=&ciInsureDemand.demandTime=&ciInsureDemand.restricFlag=&ciInsureDemand.preferentialDay=&ciInsureDemand.preferentialPremium=&ciInsureDemand.preferentialFormula%20=&ciInsureDemand.lastyearenddate=&prpCitemCar.noDamageYears=0&ciInsureDemand.rateRloatFlag=00&ciInsureDemand.claimAdjustReason=A1&ciInsureDemand.peccancyAdjustReason=V1&cIRiskWarningType=&CIDemandFecc_Flag=&ciInsureDemandLoss_%5B0%5D.id.serialNo=&ciInsureDemandLoss_%5B0%5D.lossTime=&ciInsureDemandLoss_%5B0%5D.lossDddress=&ciInsureDemandLoss_%5B0%5D.lossAction=&ciInsureDemandLoss_%5B0%5D.coeff=&ciInsureDemandLoss_%5B0%5D.lossType=&ciInsureDemandLoss_%5B0%5D.identifyType=&ciInsureDemandLoss_%5B0%5D.identifyNumber=&ciInsureDemandLoss_%5B0%5D.lossAcceptDate=&ciInsureDemandLoss_%5B0%5D.processingStatus=&ciInsureDemandLoss_%5B0%5D.lossActionDesc=&CIDemandClaim_Flag=&ciInsureDemandPay_%5B0%5D.id.serialNo=&ciInsureDemandPay_%5B0%5D.payCompany=&ciInsureDemandPay_%5B0%5D.claimregistrationno=&ciInsureDemandPay_%5B0%5D.compensateNo=&ciInsureDemandPay_%5B0%5D.lossTime=&ciInsureDemandPay_%5B0%5D.endcCaseTime=&ciInsureDemandPay_%5B0%5D.lossFee=&ciInsureDemandPay_%5B0%5D.payType=&ciInsureDemandPay_%5B0%5D.personpayType=&ciRiskWarningClaimItems_%5B0%5D.id.serialNo=&ciRiskWarningClaimItems_%5B0%5D.riskWarningType=&ciRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&ciRiskWarningClaimItems_%5B0%5D.insurerCode=&ciRiskWarningClaimItems_%5B0%5D.lossTime=&ciRiskWarningClaimItems_%5B0%5D.lossArea=&ciInsureDemand.licenseNo=&ciInsureDemand.licenseType=&ciInsureDemand.useNatureCode=&ciInsureDemand.frameNo=&ciInsureDemand.engineNo=&ciInsureDemand.licenseColorCode=&ciInsureDemand.carOwner=&ciInsureDemand.enrollDate=&ciInsureDemand.makeDate=&ciInsureDemand.seatCount=&ciInsureDemand.tonCount=&ciInsureDemand.validCheckDate=&ciInsureDemand.manufacturerName=&ciInsureDemand.modelCode=&ciInsureDemand.brandCName=&ciInsureDemand.brandName=&ciInsureDemand.carKindCode=&ciInsureDemand.checkDate=&ciInsureDemand.endValidDate=&ciInsureDemand.carStatus=&ciInsureDemand.haulage=&AccidentFlag=&rateFloatFlag=ND4&prpCtrafficRecordTemps_%5B0%5D.id.serialNo=&prpCtrafficRecordTemps_%5B0%5D.accidentDate=&prpCtrafficRecordTemps_%5B0%5D.claimDate=&hidden_index_ctraffic=0&_taxUnit=&taxPlatFormTime=2012-04-21&iniPrpCcarShipTax_Flag=&strCarShipFlag=1&prpCcarShipTax.taxType=1&prpCcarShipTax.calculateMode=C1&prpCcarShipTax.leviedDate=&prpCcarShipTax.carKindCode=A01&prpCcarShipTax.model=B11&prpCcarShipTax.taxPayerIdentNo=&prpCcarShipTax.taxPayerNumber=&prpCcarShipTax.carLotEquQuality=&prpCcarShipTax.taxPayerCode=&prpCcarShipTax.id.itemNo=1&prpCcarShipTax.taxPayerNature=3&prpCcarShipTax.taxPayerName=&prpCcarShipTax.taxUnit=&prpCcarShipTax.taxComCode=&prpCcarShipTax.taxComName=&prpCcarShipTax.taxExplanation=&prpCcarShipTax.taxAbateReason=&prpCcarShipTax.dutyPaidProofNo_1=&prpCcarShipTax.dutyPaidProofNo_2=&prpCcarShipTax.dutyPaidProofNo=&prpCcarShipTax.taxAbateRate=&prpCcarShipTax.taxAbateAmount=&prpCcarShipTax.taxAbateType=1&prpCcarShipTax.taxUnitAmount=&prpCcarShipTax.prePayTaxYear=&prpCcarShipTax.prePolicyEndDate=&prpCcarShipTax.payStartDate=&prpCcarShipTax.payEndDate=&prpCcarShipTax.thisPayTax=&prpCcarShipTax.prePayTax=&prpCcarShipTax.taxItemCode=&prpCcarShipTax.taxItemName=&prpCcarShipTax.baseTaxation=&prpCcarShipTax.taxRelifFlag=&prpCcarShipTax.delayPayTax=&prpCcarShipTax.sumPayTax=&CarShipInit_Flag=&prpCcarShipTax.flag=&quotationtaxPayerCode=&noBringOutEngage=&prpCengageTemps_%5B0%5D.id.serialNo=&prpCengageTemps_%5B0%5D.clauseCode=&prpCengageTemps_%5B0%5D.clauseName=&clauses_%5B0%5D=&prpCengageTemps_%5B0%5D.flag=&prpCengageTemps_%5B0%5D.engageFlag=&prpCengageTemps_%5B0%5D.maxCount=&prpCengageTemps_%5B0%5D.clauses=&iniPrpCengage_Flag=&hidden_index_engage=0&costRateForPG=&certificateNo=&levelMaxRate=&maxRateScm=&levelMaxRateCi=&maxRateScmCi=&isModifyBI=&isModifyCI=&sumBICoinsRate=&sumCICoinsRate=&agentsRateBI=&agentsRateCI=&prpVisaRecordP.id.visaNo=&prpVisaRecordP.id.visaCode=&prpVisaRecordP.visaName=&prpVisaRecordP.printType=101&prpVisaRecordT.id.visaNo=&prpVisaRecordT.id.visaCode=&prpVisaRecordT.visaName=&prpVisaRecordT.printType=103&prpCmain.sumAmount=&prpCmain.sumDiscount=&prpCstampTaxBI.biTaxRate=&prpCstampTaxBI.biPayTax=&prpCmain.sumPremium=&prpVisaRecordPCI.id.visaNo=&prpVisaRecordPCI.id.visaCode=&prpVisaRecordPCI.visaName=&prpVisaRecordPCI.printType=201&prpVisaRecordTCI.id.visaNo=&prpVisaRecordTCI.id.visaCode=&prpVisaRecordTCI.visaName=&prpVisaRecordTCI.printType=203&prpCmainCI.sumAmount=&prpCmainCI.sumDiscount=&prpCstampTaxCI.ciTaxRate=&prpCstampTaxCI.ciPayTax=&prpCmainCI.sumPremium=&prpCmainCar.rescueFundRate=&prpCmainCar.resureFundFee=&prpCmain.projectCode=&projectCode=&costRateUpper=&prpCmainCommon.ext3=&importantProjectCode=&prpCmain.operatorCode=020083&operatorName=%D6%DA%BA%CF%CB%C4%BA%A3&operateDateShow=&prpCmain.coinsFlag=00&coinsFlagBak=00&premium=&prpCmain.language=CNY&prpCmain.policySort=1&prpCmain.policyRelCode=&prpCmain.policyRelName=&subsidyRate=&policyRel=&prpCmain.reinsFlag=0&prpCmain.agriFlag=0&premium=&prpCmainCar.carCheckStatus=0&prpCmainCar.carChecker=&carCheckerTranslate=&prpCmainCar.carCheckTime=&prpCmainCommon.DBCFlag=0&prpCmain.argueSolution=1&prpCmain.arbitBoardName=&arbitBoardNameDes=&prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&hidden_index_commission=0&scmIsOpen=1111100000&prpCagents_%5B0%5D.roleType=&roleTypeName_%5B0%5D=&prpCagents_%5B0%5D.id.roleCode=&prpCagents_%5B0%5D.roleCode_uni=&prpCagents_%5B0%5D.roleName=&prpCagents_%5B0%5D.costRate=&prpCagents_%5B0%5D.costFee=&prpCagents_%5B0%5D.flag=&prpCagents_%5B0%5D.businessNature=&prpCagents_%5B0%5D.isMain=&prpCagentCIs_%5B0%5D.roleType=&roleTypeNameCI_%5B0%5D=&prpCagentCIs_%5B0%5D.id.roleCode=&prpCagentCIs_%5B0%5D.roleCode_uni=&prpCagentCIs_%5B0%5D.roleName=&prpCagentCIs_%5B0%5D.costRate=&prpCagentCIs_%5B0%5D.costFee=&prpCagentCIs_%5B0%5D.flag=&prpCagentCIs_%5B0%5D.businessNature=&prpCagentCIs_%5B0%5D.isMain=&commissionCount=&prpCsaless_%5B0%5D.salesDetailName=&prpCsaless_%5B0%5D.riskCode=&prpCsaless_%5B0%5D.splitRate=&prpCsaless_%5B0%5D.oriSplitNumber=&prpCsaless_%5B0%5D.splitFee=&prpCsaless_%5B0%5D.agreementNo=&prpCsaless_%5B0%5D.id.salesCode=&prpCsaless_%5B0%5D.salesName=&prpCsaless_%5B0%5D.id.proposalNo=&prpCsaless_%5B0%5D.id.salesDetailCode=&prpCsaless_%5B0%5D.totalRate=&prpCsaless_%5B0%5D.splitWay=&prpCsaless_%5B0%5D.totalRateMax=&prpCsaless_%5B0%5D.flag=&prpCsaless_%5B0%5D.remark=&commissionPower=&hidden_index_prpCsales=0&prpCsalesDatils_%5B0%5D.id.salesCode=&prpCsalesDatils_%5B0%5D.id.proposalNo=&prpCsalesDatils_%5B0%5D.id.%20%20=&prpCsalesDatils_%5B0%5D.id.roleType=&prpCsalesDatils_%5B0%5D.id.roleCode=&prpCsalesDatils_%5B0%5D.currency=&prpCsalesDatils_%5B0%5D.splitDatilRate=&prpCsalesDatils_%5B0%5D.splitDatilFee=&prpCsalesDatils_%5B0%5D.roleName=&prpCsalesDatils_%5B0%5D.splitWay=&prpCsalesDatils_%5B0%5D.flag=&prpCsalesDatils_%5B0%5D.remark=&hidden_index_prpCsalesDatil=0&csManageSwitch=1&prpCmainChannel.agentCode=&prpCmainChannel.agentName=&prpCmainChannel.organCode=&prpCmainChannel.organCName=&comCodeType=&prpCmainChannel.identifyNumber=&prpCmainChannel.identifyType=&prpCmainChannel.manOrgCode=&prpCmain.remark=&prpDdismantleDetails_%5B0%5D.id.agreementNo=&prpDdismantleDetails_%5B0%5D.flag=&prpDdismantleDetails_%5B0%5D.id.configCode=&prpDdismantleDetails_%5B0%5D.id.assignType=&prpDdismantleDetails_%5B0%5D.id.roleCode=&prpDdismantleDetails_%5B0%5D.roleName=&prpDdismantleDetails_%5B0%5D.costRate=&prpDdismantleDetails_%5B0%5D.roleFlag=&prpDdismantleDetails_%5B0%5D.businessNature=&prpDdismantleDetails_%5B0%5D.roleCode_uni=&hidden_index_prpDdismantleDetails=0&payTimes=1&prpCplanTemps_%5B0%5D.payNo=&prpCplanTemps_%5B0%5D.serialNo=&prpCplanTemps_%5B0%5D.endorseNo=&cplan_%5B0%5D.payReasonC=&prpCplanTemps_%5B0%5D.payReason=&prpCplanTemps_%5B0%5D.planDate=&prpCplanTemps_%5B0%5D.currency=&description_%5B0%5D.currency=&prpCplanTemps_%5B0%5D.planFee=&cplans_%5B0%5D.planFee=&cplans_%5B0%5D.backPlanFee=&prpCplanTemps_%5B0%5D.netPremium=&prpCplanTemps_%5B0%5D.taxPremium=&prpCplanTemps_%5B0%5D.delinquentFee=&prpCplanTemps_%5B0%5D.flag=&prpCplanTemps_%5B0%5D.subsidyRate=&prpCplanTemps_%5B0%5D.isBICI=&iniPrpCplan_Flag=&loadFlag9=&planfee_index=0&planStr=&planPayTimes=&prpCmainCar.flag=1&prpCmainCarFlag=1&coinsSchemeCode=&coinsSchemeName=&mainPolicyNo=&prpCcoinsMains_%5B0%5D.id.serialNo=1&prpCcoinsMains_%5B0%5D.coIdentity=1&prpCcoinsMains_%5B0%5D.coinsCode=002&prpCcoinsMains_%5B0%5D.coinsName=%C8%CB%B1%A3%B2%C6%B2%FA&prpCcoinsMains_%5B0%5D.coinsRate=&prpCcoinsMains_%5B0%5D.id.currency=CNY&prpCcoinsMains_%5B0%5D.coinsAmount=&prpCcoinsMains_%5B0%5D.coinsPremium=&prpCcoinsMains_%5B0%5D.coinsPremium=&iniPrpCcoins_Flag=&hidden_index_ccoins=0&prpCpayeeAccountBIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountBIs_%5B0%5D.id.serialNo=&prpCpayeeAccountBIs_%5B0%5D.itemNo=&prpCpayeeAccountBIs_%5B0%5D.payReason=&prpCpayeeAccountBIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountBIs_%5B0%5D.accountName=&prpCpayeeAccountBIs_%5B0%5D.basicBankCode=&prpCpayeeAccountBIs_%5B0%5D.basicBankName=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountBIs_%5B0%5D.bankCode=&prpCpayeeAccountBIs_%5B0%5D.bankName=&prpCpayeeAccountBIs_%5B0%5D.cnaps=&prpCpayeeAccountBIs_%5B0%5D.accountNo=&prpCpayeeAccountBIs_%5B0%5D.isPrivate=&prpCpayeeAccountBIs_%5B0%5D.cardType=&prpCpayeeAccountBIs_%5B0%5D.paySumFee=&prpCpayeeAccountBIs_%5B0%5D.payType=&prpCpayeeAccountBIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountBIs_%5B0%5D.sendSms=&prpCpayeeAccountBIs_%5B0%5D.identifyType=&prpCpayeeAccountBIs_%5B0%5D.identifyNo=&prpCpayeeAccountBIs_%5B0%5D.telephone=&prpCpayeeAccountBIs_%5B0%5D.sendMail=&prpCpayeeAccountBIs_%5B0%5D.mailAddr=&prpCpayeeAccountCIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountCIs_%5B0%5D.id.serialNo=&prpCpayeeAccountCIs_%5B0%5D.itemNo=&prpCpayeeAccountCIs_%5B0%5D.payReason=&prpCpayeeAccountCIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountCIs_%5B0%5D.accountName=&prpCpayeeAccountCIs_%5B0%5D.basicBankCode=&prpCpayeeAccountCIs_%5B0%5D.basicBankName=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountCIs_%5B0%5D.bankCode=&prpCpayeeAccountCIs_%5B0%5D.bankName=&prpCpayeeAccountCIs_%5B0%5D.cnaps=&prpCpayeeAccountCIs_%5B0%5D.accountNo=&prpCpayeeAccountCIs_%5B0%5D.isPrivate=&prpCpayeeAccountCIs_%5B0%5D.cardType=&prpCpayeeAccountCIs_%5B0%5D.paySumFee=&prpCpayeeAccountCIs_%5B0%5D.payType=&prpCpayeeAccountCIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountCIs_%5B0%5D.sendSms=&prpCpayeeAccountCIs_%5B0%5D.identifyType=&prpCpayeeAccountCIs_%5B0%5D.identifyNo=&prpCpayeeAccountCIs_%5B0%5D.telephone=&prpCpayeeAccountCIs_%5B0%5D.sendMail=&prpCpayeeAccountCIs_%5B0%5D.mailAddr=&iReinsCode=&prpCspecialFacs_%5B0%5D.reinsCode=001&iFReinsCode=&iPayCode=&iShareRate=&iCommRate=&iTaxRate=&iOthRate=&iCommission=&iOthPremium=&prpCspecialFacs_%5B0%5D.id.reinsNo=1&prpCspecialFacs_%5B0%5D.freinsCode=001&prpCspecialFacs_%5B0%5D.payCode=001&prpCspecialFacs_%5B0%5D.shareRate=001&prpCspecialFacs_%5B0%5D.sharePremium=001&prpCspecialFacs_%5B0%5D.commRate=001&prpCspecialFacs_%5B0%5D.taxRate=001&prpCspecialFacs_%5B0%5D.tax=001&prpCspecialFacs_%5B0%5D.othRate=001&prpCspecialFacs_%5B0%5D.commission=001&prpCspecialFacs_%5B0%5D.othPremium=001&prpCspecialFacs_%5B0%5D.reinsName=001&prpCspecialFacs_%5B0%5D.freinsName=001&prpCspecialFacs_%5B0%5D.payName=001&prpCspecialFacs_%5B0%5D.remark=001&prpCspecialFacs_%5B0%5D.flag=&hidden_index_specialFac=0&updateIndex=-1&iniCspecialFac_Flag=&_ReinsCode=&loadFlag8=&_FReinsCode=&_PayCode=&_ReinsName=&_FReinsName=&_PayName=&_CommRate=&_OthRate=&_ShareRate=&_Commission=&_OthPremium=&_SharePremium=&_TaxRate=&_Tax=&_Remark=&prpCsettlement.buyerUnitRank=3&prpCsettlement.buyerPreFee=&prpCsettlement.buyerUnitCode=&prpCsettlement.buyerUnitName=&prpCsettlement.upperUnitCode=&upperUnitName=&prpCsettlement.buyerUnitAddress=&prpCsettlement.buyerLinker=&prpCsettlement.buyerPhone=&prpCsettlement.buyerMobile=&prpCsettlement.buyerFax=&prpCsettlement.buyerUnitNature=1&prpCsettlement.buyerProvince=11000000&buyerProvinceDes=%C8%CB%B1%A3%B2%C6%CF%D5%B1%B1%BE%A9%CA%D0%B7%D6%B9%AB%CB%BE&prpCsettlement.buyerBusinessSort=01&prpCsettlement.comCname=&prpCsettlement.linkerCode=&linkerName=&linkerPhone=&linkerMobile=&linkerFax=&prpCsettlement.comCode=&prpCsettlement.fundForm=1&prpCsettlement.flag=&settlement_Flag=&prpCcontriutions_%5B0%5D.id.serialNo=1&prpCcontriutions_%5B0%5D.contribType=F&prpCcontriutions_%5B0%5D.contribCode=&prpCcontriutions_%5B0%5D.contribName=&prpCcontriutions_%5B0%5D.contribCode_uni=&prpCcontriutions_%5B0%5D.contribPercent=&prpCcontriutions_%5B0%5D.contribPremium=&prpCcontriutions_%5B0%5D.remark=&hidden_index_ccontriutions=0&userCode=020083&iProposalNo=&CProposalNo=&timeFlag=&prpCremarks_%5B0%5D.id.proposalNo=&prpCremarks_%5B0%5D.id.serialNo=&prpCremarks_%5B0%5D.operatorCode=020083&prpCremarks_%5B0%5D.remark=&prpCremarks_%5B0%5D.flag=&prpCremarks_%5B0%5D.insertTimeForHis=&hidden_index_remark=0&ciInsureDemandCheckVo.demandNo=&ciInsureDemandCheckVo.checkQuestion=&ciInsureDemandCheckVo.checkAnswer=&ciInsureDemandCheckVo.flag=DEMAND&ciInsureDemandCheckVo.riskCode=";
		//用户相关信息
		String purchasePrice = (String) carInfoMap.get("purchasePrice");//新车购置价格
		String depreciationPrice = (String) carInfoMap.get("depreciationPrice");//折旧价格
		String carNo = (String) carInfoMap.get("carNo");
		String engineNo = (String) carInfoMap.get("engineNo");
		String vin = (String) carInfoMap.get("vin");
		String registerDate = (String) carInfoMap.get("enrollDate");
		String userYear = carInfoMap.get("useYears").toString();
		String owner = (String) carInfoMap.get("owner");
		String modelCode = (String) carInfoMap.get("modelCode");
		String brandName = (String) carInfoMap.get("brandName");
		String exhaustScale = (String) carInfoMap.get("vehicleExhaust");
		String seatCount = (String) carInfoMap.get("seatCount");
		String countryNature = "02";
		String modelCodeAlias = (String) carInfoMap.get("modelCodeAlias");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String bizStartDate = sdf.format(DateUtils.addDays(new Date(), 1));
		String bizEndDate = sdf.format(DateUtils.addYears(new Date(), 1));
		
		String ciStartDate = bizStartDate;
		String ciEndDate = bizEndDate;
		//查询缓存
		Map<String, Object> jqxDateInfo = CacheConstant.lastJqxEndDateInfo.get(carNo);
		if(jqxDateInfo != null){
			ciStartDate =  (String)  jqxDateInfo.get("ciStartDateStr");
			ciEndDate = (String)  jqxDateInfo.get("ciEndDateStr");
		}
		
		try {
			carNo =  java.net.URLEncoder.encode(carNo,   "gb2312");
			owner =  java.net.URLEncoder.encode(owner,   "gb2312");
			brandName =  java.net.URLEncoder.encode(brandName,   "gb2312");
			modelCodeAlias =  java.net.URLEncoder.encode(modelCodeAlias,   "gb2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		param3 = param3.replace("prpCitemCar.modelDemandNo=", "prpCitemCar.modelDemandNo=" + carInfoMap.get("pmQueryNo"));
		//用户、车辆信息信息
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		param3 = param3.replace("operationTimeStamp=", "operationTimeStamp=" + sdf.format(new Date()));//操作时间
		param3 = param3.replace("prpCitemCar.actualValue=", "prpCitemCar.actualValue=" + depreciationPrice);//折旧价格
		param3 = param3.replace("prpCitemCar.licenseNo=", "prpCitemCar.licenseNo=" + carNo);//牌照
		param3 = param3.replace("prpCitemCar.engineNo=", "prpCitemCar.engineNo=" + engineNo);//发动机号
		param3 = param3.replace("prpCitemCar.vinNo=", "prpCitemCar.vinNo=" + vin);//vin
		param3 = param3.replace("prpCitemCar.frameNo=", "prpCitemCar.frameNo=" + vin);//vin
		param3 = param3.replace("prpCitemCar.enrollDate=", "prpCitemCar.enrollDate=" + registerDate);//注册日期
		param3 = param3.replace("prpCitemCar.useYears=", "prpCitemCar.useYears=" + userYear);//使用年份
		param3 = param3.replace("owner=", "owner=" + owner);//车主姓名
		param3 = param3.replace("prpCitemCar.modelCode=", "prpCitemCar.modelCode=" + modelCode);//?
		param3 = param3.replace("prpCitemCar.brandName=", "prpCitemCar.brandName=" + brandName);//品牌
		param3 = param3.replace("prpCitemCar.purchasePrice=", "prpCitemCar.purchasePrice=" + purchasePrice);
		param3 = param3.replace("CarActualValueTrue=", "CarActualValueTrue=" + purchasePrice);//新车购置价格
		param3 = param3.replace("purchasePriceDown=", "purchasePriceDown=" + purchasePrice);
		param3 = param3.replace("purchasePriceOld=", "purchasePriceOld=" + purchasePrice);
		param3 = param3.replace("prpCitemCar.exhaustScale=", "prpCitemCar.exhaustScale=" + exhaustScale);
		param3 = param3.replace("prpCitemCar.seatCount=", "prpCitemCar.seatCount=" + seatCount);
		param3 = param3.replace("prpCitemCar.countryNature=", "prpCitemCar.countryNature=" + countryNature);
		param3 = param3.replace("prpCitemCar.modelCodeAlias=", "prpCitemCar.modelCodeAlias=" + modelCodeAlias);
		
		//时间相关  20150626-20160625
		String nowDateStr =   sdf.format(new Date());
		param3 = param3.replace("currentDate=", "currentDate=" + nowDateStr);
		param3 = param3.replace("prpCmain.operateDate=", "prpCmain.operateDate=" + nowDateStr);
		param3 = param3.replace("Today=", "Today=" + nowDateStr);
		param3 = param3.replace("OperateDate=", "OperateDate=" + nowDateStr);
		
		param3 = param3.replace("biStartDate=", "biStartDate=" + bizStartDate);
		
		param3 = param3.replace("prpCmain.startDate=", "prpCmain.startDate=" + bizStartDate);
		param3 = param3.replace("prpCmain.endDate=", "prpCmain.endDate=" + bizEndDate);
		param3 = param3.replace("prpCmainCI.startDate=", "prpCmainCI.startDate=" + ciStartDate);//交强险开始日期
		param3 = param3.replace("prpCmainCI.endDate=", "prpCmainCI.endDate=" + ciEndDate);//交强险结束日期
		param3 = param3.replace("ciStartDate=", "ciStartDate=" + ciStartDate);
		param3 = param3.replace("ciEndDate=", "ciEndDate=" + ciEndDate);
		
		return param3;
	}
	
	/**
	 * 设置投保人、被保人、车主信息
	 * @param quoteParam
	 * @param param3
	 * @param owner
	 * @param id
	 * @param mobile
	 * @return
	 */
	private String makeQuoteInsuredInfoParam(Map<String, String> quoteParam, String param3, String owner, String id, String mobile){
		//车主身份证为空的时候，查询
		if(StringUtils.isBlank(id)){
			QuoteGetUserInfoByNamePage quoteGetUserInfoByNamePage = new QuoteGetUserInfoByNamePage(1);
			Request request = new Request();
			request.setUrl("http://10.134.136.48:8300/cif/customperson/findCustomPersonIntf.do?pageSize=10&pageNo=1");
			Map<String, String> param = new HashMap<>();
			param.put("name", owner);
			request.setRequestParam(param);
			Response response = quoteGetUserInfoByNamePage.run(request);
			Map<String, String> result = response.getResponseMap();
			id = result.get("identifyNumber");
			mobile = result.get("customMobile");
		}
		try {
			owner =  java.net.URLEncoder.encode(owner,   "gb2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//车主
		param3 = param3.replace("prpCinsureds%5B0%5D.insuredName=", "prpCinsureds%5B0%5D.insuredName=" + owner);//车主姓名
		param3 = param3.replace("prpCinsureds%5B0%5D.identifyType=", "prpCinsureds%5B0%5D.identifyType=01");//证件类型
		param3 = param3.replace("prpCinsureds%5B0%5D.identifyNumber=", "prpCinsureds%5B0%5D.identifyNumber=" + id);//证件号码
		param3 = param3.replace("prpCinsureds%5B0%5D.phoneNumber=", "prpCinsureds%5B0%5D.phoneNumber=" + mobile);//手机
		param3 = param3.replace("prpCinsureds%5B0%5D.mobile=", "prpCinsureds%5B0%5D.mobile=" + mobile);//手机
		
		String InsuredName = quoteParam.get("InsuredName");
		String InsuredIdCard = quoteParam.get("InsuredIdCard");
		String InsuredIdType = quoteParam.get("InsuredIdType");
		String InsuredMobile = quoteParam.get("InsuredMobile");
		if (StringUtils.isBlank(InsuredName)) {// 没有输入投保人信息、被保人信息的时候，直接用车主
			// 投保人\被保人
			param3 = param3.replace("prpCinsureds%5B1%5D.insuredName=", "prpCinsureds%5B1%5D.insuredName=" + owner);// 车主姓名
			param3 = param3.replace("prpCinsureds%5B1%5D.identifyType=", "prpCinsureds%5B1%5D.identifyType=01");// 证件类型
			param3 = param3.replace("prpCinsureds%5B1%5D.identifyNumber=", "prpCinsureds%5B1%5D.identifyNumber=" + id);// 证件号码
			param3 = param3.replace("prpCinsureds%5B1%5D.phoneNumber=", "prpCinsureds%5B1%5D.phoneNumber=" + mobile);// 手机
			param3 = param3.replace("prpCinsureds%5B1%5D.mobile=", "prpCinsureds%5B1%5D.mobile=" + mobile);// 手机
		} else {
			// 投保人\被保人
			try {
				InsuredName = java.net.URLEncoder.encode(InsuredName, "gb2312");
			} catch (Exception e) {
				e.printStackTrace();
			}
			param3 = param3.replace("prpCinsureds%5B1%5D.insuredName=", "prpCinsureds%5B1%5D.insuredName=" + InsuredName);// 车主姓名
			param3 = param3.replace("prpCinsureds%5B1%5D.identifyType=", "prpCinsureds%5B1%5D.identifyType=01");// 证件类型
			param3 = param3.replace("prpCinsureds%5B1%5D.identifyNumber=","prpCinsureds%5B1%5D.identifyNumber=" + InsuredIdCard);// 证件号码
			param3 = param3.replace("prpCinsureds%5B1%5D.phoneNumber=", "prpCinsureds%5B1%5D.phoneNumber=" + InsuredMobile);// 手机
			param3 = param3.replace("prpCinsureds%5B1%5D.mobile=", "prpCinsureds%5B1%5D.mobile=" + InsuredMobile);// 手机
		}
		return param3;
	}
	
	/**
	 * 根据用户选择险种信息进行拼接
	 * @param quoteParam
	 * @param param3
	 * @return
	 */
	private String makeQuoteInsurParam(Map<String, String> quoteParam, String param3, String purchasePrice, String depreciationPrice, int seatCount){
		//险种信息
		String CheSun = quoteParam.get("CheSun");
		String BuJiMianCheSun = quoteParam.get("BuJiMianCheSun");
		if(!"0".equals(CheSun)){//投保车损
			param3 = param3.replace("prpCitemKindsTemp_%5B0%5D.chooseFlag=", "prpCitemKindsTemp_%5B0%5D.chooseFlag=on");//未知险种 _0
			param3 = param3.replace("prpCitemKindsTemp%5B0%5D.chooseFlag=", "prpCitemKindsTemp%5B0%5D.chooseFlag=on");//机动车损失保险 0
			purchasePrice = QuoteCalculateUtils.mN(purchasePrice, 2);
			param3 = param3.replace("prpCitemKindsTemp%5B0%5D.amount=", "prpCitemKindsTemp%5B0%5D.amount=" + purchasePrice);//新车购置价格
			if(!"0".equals(BuJiMianCheSun)){//投保车损不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B16%5D.chooseFlag=", "prpCitemKindsTemp%5B16%5D.chooseFlag=on");//不计免赔率（车辆损失险） 16	
			}
		}
		String DaoQiang = quoteParam.get("DaoQiang");
		String BuJiMianDaoQiang = quoteParam.get("BuJiMianDaoQiang");
		if(!"0".equals(DaoQiang)){//投保盗抢
			param3 = param3.replace("prpCitemKindsTemp%5B1%5D.chooseFlag=", "prpCitemKindsTemp%5B1%5D.chooseFlag=on");//盗抢险
			depreciationPrice = QuoteCalculateUtils.mN(depreciationPrice, 2);
			param3 = param3.replace("prpCitemKindsTemp%5B1%5D.amount=", "prpCitemKindsTemp%5B1%5D.amount=" + depreciationPrice);//折旧价格
			if(!"0".equals(BuJiMianDaoQiang)){//投保盗抢不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B17%5D.chooseFlag=", "prpCitemKindsTemp%5B17%5D.chooseFlag=on");//不计免赔率（机动车盗抢险） 17
			}
		}
		
		String SanZhe = quoteParam.get("SanZhe");
		String BuJiMianSanZhe = quoteParam.get("BuJiMianSanZhe");
		if(!"0".equals(SanZhe)){//投保三者
			param3 = param3.replace("prpCitemKindsTemp%5B2%5D.chooseFlag=", "prpCitemKindsTemp%5B2%5D.chooseFlag=on");//第三者责任保险 2
			SanZhe = QuoteCalculateUtils.mN(SanZhe, 2);
			param3 = param3.replace("prpCitemKindsTemp%5B2%5D.amount=", "prpCitemKindsTemp%5B2%5D.amount=" + SanZhe);//保额
			if(!"0".equals(BuJiMianSanZhe)){//投保三者不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B18%5D.chooseFlag=", "prpCitemKindsTemp%5B18%5D.chooseFlag=on");// 不计免赔率（三者险）18
			}
		}

		String SiJi = quoteParam.get("SiJi");
		String BuJiMianRenYuan = quoteParam.get("BuJiMianRenYuan");
		if(!"0".equals(SiJi)){//投保司机
			param3 = param3.replace("prpCitemKindsTemp%5B3%5D.chooseFlag=", "prpCitemKindsTemp%5B3%5D.chooseFlag=on");//车上人员责任险（司机） 3
			SiJi = QuoteCalculateUtils.mN(SiJi, 2);
			param3 = param3.replace("prpCitemKindsTemp%5B3%5D.unitAmount=", "prpCitemKindsTemp%5B3%5D.unitAmount=" + SiJi);//保额 
			param3 = param3.replace("prpCitemKindsTemp%5B3%5D.amount=", "prpCitemKindsTemp%5B3%5D.amount=" + SiJi);//保额 
			if(!"0".equals(BuJiMianRenYuan)){//投保司机不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B19%5D.chooseFlag=", "prpCitemKindsTemp%5B19%5D.chooseFlag=on");//不计免赔率（车上人员责任险（司机）） 19
			}
		}
		
		String ChengKe = quoteParam.get("ChengKe");
		if(!"0".equals(ChengKe)){//投保乘客
			param3 = param3.replace("prpCitemKindsTemp%5B4%5D.chooseFlag=", "prpCitemKindsTemp%5B4%5D.chooseFlag=on");//车上人员责任险（乘客） 4
			int amout4 = Integer.parseInt(ChengKe) * (seatCount-1);
			param3 = param3.replace("prpCitemKindsTemp%5B4%5D.unitAmount=", "prpCitemKindsTemp%5B4%5D.unitAmount=" + ChengKe);//保额 
			param3 = param3.replace("prpCitemKindsTemp%5B4%5D.amount=", "prpCitemKindsTemp%5B4%5D.amount=" + QuoteCalculateUtils.mN(String.valueOf(amout4), 2));//保额
			param3 = param3.replace("prpCitemKindsTemp%5B4%5D.quantity=", "prpCitemKindsTemp%5B4%5D.quantity=" + (seatCount-1));//座位个数
			if(!"0".equals(BuJiMianRenYuan)){//投保乘客不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B20%5D.chooseFlag=", "prpCitemKindsTemp%5B20%5D.chooseFlag=on");//不计免赔率（车上人员责任险（乘客）） 20
			}
		}

		String HuaHen = quoteParam.get("HuaHen");
		String BuJiMianFuJia = quoteParam.get("BuJiMianFuJia");
		if(!"0".equals(HuaHen)){//投保划痕
			param3 = param3.replace("prpCitemKindsTemp%5B5%5D.chooseFlag=", "prpCitemKindsTemp%5B5%5D.chooseFlag=on");//车身划痕损失险条款 5
			HuaHen = QuoteCalculateUtils.mN(HuaHen, 2);
			param3 = param3.replace("prpCitemKindsTemp%5B5%5D.amount=", "prpCitemKindsTemp%5B5%5D.amount=" + HuaHen);//保额
			if(!"0".equals(BuJiMianFuJia)){//投保划痕不计免赔
				param3 = param3.replace("prpCitemKindsTemp%5B23%5D.chooseFlag=", "prpCitemKindsTemp%5B23%5D.chooseFlag=on");//不计免赔率（车身划痕损失险） 23
			}
		}
		
		String BoLi = quoteParam.get("BoLi");
		if(!"0".equals(BoLi)){//投保玻璃
			param3 = param3.replace("prpCitemKindsTemp%5B6%5D.chooseFlag=", "prpCitemKindsTemp%5B6%5D.chooseFlag=on");//玻璃单独破碎险 6
			String modeCode = ("1".equals(BoLi)) ? "10" : "20";//1、国产，2、进口
			param3 = param3.replace("prpCitemKindsTemp%5B6%5D.modeCode=", "prpCitemKindsTemp%5B6%5D.modeCode=" + modeCode);//10 国产玻璃  20 进口玻璃
		}

		String ZiRan = quoteParam.get("ZiRan");
		if(!"0".equals(ZiRan)){//投保自然
			param3 = param3.replace("prpCitemKindsTemp%5B7%5D.chooseFlag=", "prpCitemKindsTemp%5B7%5D.chooseFlag=on");//自燃损失险条款 7
			param3 = param3.replace("prpCitemKindsTemp%5B7%5D.amount=", "prpCitemKindsTemp%5B7%5D.amount=" + depreciationPrice);
		}
		
		String SheShui = quoteParam.get("SheShui");
		if(!"0".equals(SheShui)){//投保涉水
			param3 = param3.replace("prpCitemKindsTemp%5B21%5D.chooseFlag=", "prpCitemKindsTemp%5B21%5D.chooseFlag=on");//发动机特别损失险条款 21
			if(!"0".equals(BuJiMianFuJia)){//投保涉水不急免赔
				param3 = param3.replace("prpCitemKindsTemp%5B22%5D.chooseFlag=", "prpCitemKindsTemp%5B22%5D.chooseFlag=on");//不计免赔率（发动机特别损失险） 22
			}
		}
		return param3;
	}
	
	/**
	 * 辅助核保计算参数拼接
	 * @param param
	 * @return
	 */
	private String setFuzuHebaoJisuanParam(String param){
		//拼接到中间
		String reg1 = "&planPayTimes=";
		String[] paramArray1 = param.split(reg1);
		StringBuffer paramBuffer1 = new StringBuffer();
		paramBuffer1.append("&prpAnciInfo.sellExpensesRate=");
		paramBuffer1.append("&prpAnciInfo.sellExpensesAmount=");
		paramBuffer1.append("&prpAnciInfo.sellExpensesRateCIUp=");
		paramBuffer1.append("&prpAnciInfo.sellExpensesCIUpAmount=");
		paramBuffer1.append("&prpAnciInfo.sellExpensesRateBIUp=");
		paramBuffer1.append("&prpAnciInfo.sellExpensesBIUpAmount=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesRate=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesAmount=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesRateCI=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesAmountCI=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesRateBI=");
		paramBuffer1.append("&prpAnciInfo.operSellExpensesAmountBI=");
		paramBuffer1.append("&prpAnciInfo.operCommRateCIUp=");
		paramBuffer1.append("&operCommRateCIUpAmount=");
		paramBuffer1.append("&prpAnciInfo.operCommRateBIUp=");
		paramBuffer1.append("&operCommRateBIUpAmount=");
		paramBuffer1.append("&prpAnciInfo.operCommRate=");
		paramBuffer1.append("&prpAnciInfo.operCommRateAmount=");
		paramBuffer1.append("&prpAnciInfo.operateCommRateCI=");
		paramBuffer1.append("&prpAnciInfo.operateCommCI=");
		paramBuffer1.append("&prpAnciInfo.operateCommRateBI=");
		paramBuffer1.append("&prpAnciInfo.operateCommBI=");
		paramBuffer1.append("&prpAnciInfo.discountRateUp=");
		paramBuffer1.append("&prpAnciInfo.discountRateUpAmount=");
		paramBuffer1.append("&prpAnciInfo.discountRateCIUp=");
		paramBuffer1.append("&prpAnciInfo.discountRateCIUpAmount=");
		paramBuffer1.append("&prpAnciInfo.profitRateBIUp=");
		paramBuffer1.append("&prpAnciInfo.discountRateBIUpAmountp=");
		paramBuffer1.append("&prpAnciInfo.discountRate=");
		paramBuffer1.append("&prpAnciInfo.discountRateAmount=");
		paramBuffer1.append("&prpAnciInfo.discountRateCI=");
		paramBuffer1.append("&prpAnciInfo.discountRateCIAmount=");
		paramBuffer1.append("&prpAnciInfo.discountRateBI=");
		paramBuffer1.append("&prpAnciInfo.discountRateBIAmount=");
		paramBuffer1.append("&prpAnciInfo.riskCode=");
		paramBuffer1.append("&prpAnciInfo.standPayRate=");
		paramBuffer1.append("&prpAnciInfo.operatePayRate=");
		paramBuffer1.append("&prpAnciInfo.busiStandardBalanRate=");
		paramBuffer1.append("&prpAnciInfo.busiBalanRate=");
		paramBuffer1.append("&prpAnciInfo.busiRiskRate=");
		paramBuffer1.append("&prpAnciInfo.averProfitRate=");
		paramBuffer1.append("&prpAnciInfo.averageRate=");
		paramBuffer1.append("&prpAnciInfo.minNetSumPremiumBI=");
		paramBuffer1.append("&prpAnciInfo.minNetSumPremiumCI=");
		paramBuffer1.append("&prpAnciInfo.baseActBusiType=");
		paramBuffer1.append("&prpAnciInfo.baseExpBusiType=");
		paramBuffer1.append("&prpAnciInfo.operateProfitRate=");
		paramBuffer1.append("&prpAnciInfo.breakEvenValue=");
		paramBuffer1.append("&prpAnciInfo.profitRateBIUp=");
		paramBuffer1.append("&prpAnciInfo.proCommRateBIUp=");
		paramBuffer1.append("&prpAnciInfo.busiTypeCommBIUp=");
		paramBuffer1.append("&prpAnciInfo.busiTypeCommCIUp=");
		paramBuffer1.append("&prpAnciInfo.standbyField1=");
		paramBuffer1.append("&switchFlag=0");
		paramBuffer1.append("&actProfitRate=");
		paramBuffer1.append("&prpAnciInfo.businessCode=");
		paramBuffer1.append("&prpAnciInfo.minNetSumPremium=");
		paramBuffer1.append("&prpAnciInfo.origBusiType=");
		paramBuffer1.append("&prpAnciInfo.expProCommRateUp=");
		paramBuffer1.append("&expProCommRateUp_Disc=");
		paramBuffer1.append("&prpAnciInfo.expBusiType=");
		paramBuffer1.append("&prpAnciInfo.actProCommRateUp=");
		paramBuffer1.append("&actProCommRateUp_Disc=");
		paramBuffer1.append("&prpAnciInfo.actBusiType=");
		paramBuffer1.append("&expRiskNote=");
		paramBuffer1.append("&kindBusiTypeA=");
		paramBuffer1.append("&kindBusiTypeB=");
		paramBuffer1.append("&kindBusiTypeC=");
		paramBuffer1.append("&kindBusiTypeD=");
		paramBuffer1.append("&kindBusiTypeE=");
		param = paramArray1[0] + reg1 + paramBuffer1.toString() + paramArray1[1];
		return param;
	}
	
	/**
	 * 更新核保的相关参数
	 * @param param
	 * @return
	 */
	private String setSyxHebaoParam(String param, String carNo, String idCode){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, String> quoteJsonMap = CacheConstant.quoteResultJsonInfo.get(carNo);
		JSONObject syxJson = JSONObject.parseObject(quoteJsonMap.get("syxJson"));
		JSONArray prpCitemKindsArray = syxJson.getJSONArray("data").getJSONObject(0).getJSONArray("prpCitemKinds");//险种报价信息
		if(prpCitemKindsArray.size() == 0){//未报价商业险
			return param;//不做任何操作
		}
		// 第一部分: 商业险报价JSON对象中的 prpCfixations
		JSONObject prpCfixations = syxJson.getJSONArray("data").getJSONObject(0).getJSONArray("prpCfixations").getJSONObject(0);
		param = param.replace("prpCfixationTemp.discount=", "prpCfixationTemp.discount=" + prpCfixations.getString("discount"));
		JSONObject id = prpCfixations.getJSONObject("id");
		param = param.replace("prpCfixationTemp.id.riskCode=", "prpCfixationTemp.id.riskCode=" + id.getString("riskCode"));
		param = param.replace("prpCfixationTemp.profits=", "prpCfixationTemp.profits=" + prpCfixations.getString("profits"));
		param = param.replace("prpCfixationTemp.cost=", "prpCfixationTemp.cost=" + prpCfixations.getString("cost"));
		param = param.replace("prpCfixationTemp.taxorAppend=", "prpCfixationTemp.taxorAppend=" + prpCfixations.getString("taxorAppend"));
		param = param.replace("prpCfixationTemp.payMentR=", "prpCfixationTemp.payMentR=" + prpCfixations.getString("payMentR"));
		param = param.replace("prpCfixationTemp.basePayMentR=", "prpCfixationTemp.basePayMentR=" + prpCfixations.getString("basePayMentR"));
		param = param.replace("prpCfixationTemp.poundAge=", "prpCfixationTemp.poundAge=" + prpCfixations.getString("poundAge"));
		param = param.replace("prpCfixationTemp.basePremium=", "prpCfixationTemp.basePremium=" + prpCfixations.getString("basePremium"));
		param = param.replace("prpCfixationTemp.riskPremium=", "prpCfixationTemp.riskPremium=" + prpCfixations.getString("riskPremium"));
		param = param.replace("prpCfixationTemp.riskSumPremium=", "prpCfixationTemp.riskSumPremium=" + prpCfixations.getString("riskSumPremium"));
		param = param.replace("prpCfixationTemp.signPremium=", "prpCfixationTemp.signPremium=" + prpCfixations.getString("signPremium"));
		param = param.replace("prpCfixationTemp.isQuotation=", "prpCfixationTemp.isQuotation=" + prpCfixations.getString("isQuotation"));
		param = param.replace("prpCfixationTemp.riskClass=", "prpCfixationTemp.riskClass=" + prpCfixations.getString("riskClass"));
		String operationInfo = "";
		try {
			operationInfo = java.net.URLEncoder.encode(prpCfixations.getString("operationInfo"), "gb2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		param = param.replace("prpCfixationTemp.operationInfo=", "prpCfixationTemp.operationInfo=" + operationInfo);
		param = param.replace("prpCfixationTemp.realDisCount=", "prpCfixationTemp.realDisCount=" + prpCfixations.getString("realDisCount"));
		param = param.replace("prpCfixationTemp.realProfits=", "prpCfixationTemp.realProfits=" + prpCfixations.getString("realProfits"));
		param = param.replace("prpCfixationTemp.realPayMentR=", "prpCfixationTemp.realPayMentR=" + prpCfixations.getString("realPayMentR"));
		param = param.replace("prpCfixationTemp.remark=", "prpCfixationTemp.remark=" + prpCfixations.getString("remark"));
		param = param.replace("prpCfixationTemp.responseCode=", "prpCfixationTemp.responseCode=" + prpCfixations.getString("responseCode"));
		param = param.replace("prpCfixationTemp.errorMessage=", "prpCfixationTemp.errorMessage=" + prpCfixations.getString("errorMessage"));
		param = param.replace("prpCfixationTemp.profitClass=", "prpCfixationTemp.profitClass=" + prpCfixations.getString("profitClass"));
		param = param.replace("prpCfixationTemp.costRate=", "prpCfixationTemp.costRate=" + prpCfixations.getString("costRate"));
		//第三部分：商业险 json对象 prpCprofitFactors， 没有按照顺序来
		JSONArray prpCprofitFactors = syxJson.getJSONArray("data").getJSONObject(0).getJSONArray("prpCprofitFactors");
		StringBuffer paramBuffer2 = new StringBuffer();
		for(int i=0; i<prpCprofitFactors.size(); i++){
			JSONObject prpCprofitFactor = prpCprofitFactors.getJSONObject(i);
			JSONObject prpCprofitFactorId = prpCprofitFactor.getJSONObject("id");
			String profitName = prpCprofitFactor.getString("profitName");
			String condition = prpCprofitFactor.getString("condition");
			String rate =prpCprofitFactor.getString("rate");
			try {
				profitName = java.net.URLEncoder.encode(profitName, "gb2312");
				condition = java.net.URLEncoder.encode(condition, "gb2312");
			} catch (Exception e) {
				e.getMessage();
			}
			if(i == 0){
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on", "prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on");
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.profitName=", "prpCprofitFactorsTemp_%5B0%5D.profitName=" + profitName);
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.condition=", "prpCprofitFactorsTemp_%5B0%5D.condition=" + condition);
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.rate=", "prpCprofitFactorsTemp_%5B0%5D.rate=" + prpCprofitFactor.getString("rate"));
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.lowerRate=", "prpCprofitFactorsTemp_%5B0%5D.lowerRate=" + prpCprofitFactor.getString("lowerRate"));
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.upperRate=", "prpCprofitFactorsTemp_%5B0%5D.upperRate=" + prpCprofitFactor.getString("upperRate"));
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.id.profitCode=", "prpCprofitFactorsTemp_%5B0%5D.id.profitCode=" + prpCprofitFactorId.getString("profitCode"));
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=", "prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=" + prpCprofitFactorId.getString("conditionCode"));
				param = param.replace("prpCprofitFactorsTemp_%5B0%5D.flag=", "prpCprofitFactorsTemp_%5B0%5D.flag=");
				param = param.replace("serialNo_%5B0%5D=", "serialNo_%5B0%5D=" + (i + 1));
				param = param.replace("rateTemp_%5B0%5D=", "rateTemp_%5B0%5D=" + QuoteCalculateUtils.mN(String.valueOf(Integer.parseInt(rate) * 0.01), 4));
			}
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.chooseFlag=on");
			paramBuffer2.append("&serialNo%5B" + i + "%5D=" + (i + 1));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.profitName=" + profitName);
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.condition=" + condition);
			paramBuffer2.append("&rateTemp%5B" + i + "%5D=" + QuoteCalculateUtils.mN(String.valueOf(Integer.parseInt(rate) * 0.01), 4));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.rate=" + prpCprofitFactor.getString("rate"));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.lowerRate=" + prpCprofitFactor.getString("lowerRate"));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.upperRate=" + prpCprofitFactor.getString("upperRate"));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.id.profitCode=" + prpCprofitFactorId.getString("profitCode"));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.id.conditionCode=" + prpCprofitFactorId.getString("conditionCode"));
			paramBuffer2.append("&prpCprofitFactorsTemp%5B" + i + "%5D.flag=");
		}
		String reg2 = "&itemKindLoadFlag=";
		String[] paramArray2 = param.split(reg2);
		param = paramArray2[0] + reg2 + paramBuffer2.toString() + paramArray2[1];
		
		//第四部分：商业险 json对象 prpCitemCarExt
		JSONObject prpCitemCarExt = syxJson.getJSONArray("data").getJSONObject(0).getJSONObject("prpCitemCarExt");
		param = param.replace("prpCitemCarExt.lastDamagedBI=0", "prpCitemCarExt.lastDamagedBI=" + prpCitemCarExt.getString("lastDamagedBI"));
		param = param.replace("prpCitemCarExt.thisDamagedBI=0", "prpCitemCarExt.thisDamagedBI=" + prpCitemCarExt.getString("thisDamagedBI"));
		param = param.replace("prpCitemCarExt.noDamYearsBI=0", "prpCitemCarExt.noDamYearsBI=" + prpCitemCarExt.getString("noDamYearsBI"));
		param = param.replace("prpCitemCarExt.lastDamagedCI=0", "prpCitemCarExt.lastDamagedCI=" + prpCitemCarExt.getString("lastDamagedCI"));
		param = param.replace("prpCitemCarExt_CI.rateRloatFlag=01", "prpCitemCarExt_CI.rateRloatFlag=" + prpCitemCarExt.getString("rateRloatFlag"));
		param = param.replace("prpCitemCarExt_CI.noDamYearsCI=1", "prpCitemCarExt_CI.noDamYearsCI=" + prpCitemCarExt.getString("noDamYearsCI"));
		param = param.replace("prpCitemCarExt_CI.lastDamagedCI=0", "prpCitemCarExt_CI.lastDamagedCI=" + prpCitemCarExt.getString("lastDamagedCI"));
		param = param.replace("prpCitemCarExt_CI.flag=", "prpCitemCarExt_CI.flag=" + prpCitemCarExt.getString("flag"));
		param = param.replace("prpCitemCarExt_CI.damFloatRatioCI=0", "prpCitemCarExt_CI.damFloatRatioCI=" + prpCitemCarExt.getString("damFloatRatioCI"));
		param = param.replace("prpCitemCarExt_CI.offFloatRatioCI=0", "prpCitemCarExt_CI.offFloatRatioCI=" + prpCitemCarExt.getString("offFloatRatioCI"));
		param = param.replace("prpCitemCarExt_CI.thisDamagedCI=0", "prpCitemCarExt_CI.thisDamagedCI=" + prpCitemCarExt.getString("thisDamagedCI"));
		param = param.replace("prpCitemCarExt_CI.flag=", "prpCitemCarExt_CI.flag=" + prpCitemCarExt.getString("flag"));
		//第六部分：设置险种
		for(int i=0;i<prpCitemKindsArray.size();i++){
			JSONObject prpCitemKinds = prpCitemKindsArray.getJSONObject(i);
			String kindCode = prpCitemKinds.getString("kindCode");
			String kindNum = "-1";
			switch (kindCode) {
			case "050200":
				kindNum = "0";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050500":
				kindNum = "1";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050600":
				kindNum = "2";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050701":
				kindNum = "3";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050702":
				kindNum = "4";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050210":
				kindNum = "5";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050231":
				kindNum = "6";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050310":
				kindNum = "7";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050911":
				kindNum = "16";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050921":
				kindNum = "17";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050912":
				kindNum = "18";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050928":
				kindNum = "19";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050929":
				kindNum = "20";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050291":
				kindNum = "21";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050924":
				kindNum = "22";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			case "050922":
				kindNum = "23";
				param = replaceInsInfo(param, kindNum, prpCitemKinds);
				break;
			default:
				break;
			}
		}
		//第七部分：BIdemandNo，商业险投保查询码
		String BIdemandNo = syxJson.getJSONArray("data").getJSONObject(0).getJSONObject("ciInsureDemandDAA").getString("demandNo");
		param = param.replace("BIdemandNo=", "BIdemandNo=" + BIdemandNo);
		param = param.replace("BIdemandTime=", "BIdemandTime=" + sdf.format(new Date()));
		//第十一部分：计算相关数据
		Double sumBenchPremium = 0D;//标准保费
		Double sumPremium = 0D; //含税总保费
		Double discount = 0D;//总折扣
		Double sumNetPremium = 0D;// 总净保费
		Double sumTaxPremium = 0D;//总税额
		Double sumAmount = 0D;//总保额
		Double sumDiscount = 0D;//总折扣金额
		int insurePayTimes = 0;
		Double claimAdjustValue = 0D;
		Double itemKindSpecialSumPremium = 0D;//不计免赔
		
		for(int i = 0; i< prpCitemKindsArray.size(); i++){
			JSONObject prpCitemKinds = prpCitemKindsArray.getJSONObject(i);
			Double benchMarkPremium = prpCitemKinds.getDoubleValue("benchMarkPremium");
			benchMarkPremium = QuoteCalculateUtils.m2(benchMarkPremium);
			Double premium = prpCitemKinds.getDoubleValue("premium");
			premium = QuoteCalculateUtils.m2(premium);
			Double netPremium = prpCitemKinds.getDoubleValue("netPremium");
			netPremium = QuoteCalculateUtils.m2(netPremium);
			Double taxPremium = prpCitemKinds.getDoubleValue("taxPremium");
			taxPremium = QuoteCalculateUtils.m2(taxPremium);
			
			//计算标准保费
			String flag = prpCitemKinds.getString("flag");
			String calculateFlag = prpCitemKinds.getString("calculateFlag");
			if(!"D".equals(flag.substring(0, 1)) && !"B".equals(flag.substring(0, 1))){
				sumBenchPremium = sumBenchPremium + benchMarkPremium;
			}else{
				// 批改后总保准保费中也应该加上删除险别的已了责任保费 SPB-1972
				sumBenchPremium = sumBenchPremium + premium;
			}
			//计算含税保费
			sumPremium = sumPremium + premium;
			//计算总净保费
			sumNetPremium = sumNetPremium + netPremium;
			//计算总税额
			sumTaxPremium = sumTaxPremium + taxPremium;
			//计算总保额
			if(!"D".equals(flag.substring(0, 1)) && !"B".equals(flag.substring(0, 1)) && !"Y".equals(calculateFlag.substring(0, 1))){
				sumAmount = sumAmount + prpCitemKinds.getDoubleValue("amount");
			}
			//计算总折扣金额
			sumDiscount = sumBenchPremium - sumPremium;
			//计算不计免赔
			String kindName = prpCitemKinds.getString("kindName");
			if(kindName.contains("不计免赔")){
				itemKindSpecialSumPremium = itemKindSpecialSumPremium + premium;
			}
		}
		//计算总折扣 8位有效数字
		discount = sumPremium/sumBenchPremium;
		insurePayTimes = syxJson.getJSONArray("data").getJSONObject(0).getJSONArray("ciInsureDemandPayListBI").size();
		claimAdjustValue = syxJson.getJSONArray("data").getJSONObject(0).getJSONObject("ciInsureRiskItem").getDouble("claimAdjustValue");
		param = param.replace("sumBenchPremium=", "sumBenchPremium=" + QuoteCalculateUtils.mN(String.valueOf(sumBenchPremium), 2));
		param = param.replace("prpCmain.sumPremium=", "prpCmain.sumPremium="+ QuoteCalculateUtils.mN(String.valueOf(sumPremium), 2));
		param = param.replace("prpCmain.discount=", "prpCmain.discount="+ QuoteCalculateUtils.mN(String.valueOf(discount), 8));
		param = param.replace("prpCmain.sumNetPremium=", "prpCmain.sumNetPremium=" + QuoteCalculateUtils.mN(String.valueOf(sumNetPremium), 2));
		param = param.replace("prpCmain.sumTaxPremium=", "prpCmain.sumTaxPremium=" + QuoteCalculateUtils.mN(String.valueOf(sumTaxPremium), 2));
		String sumAmountStr = QuoteCalculateUtils.mN(String.valueOf(sumAmount), 0);
		sumAmountStr = sumAmountStr.replace(".", "");
		param = param.replace("sumAmountBI=", "sumAmountBI=" + sumAmountStr);
		param = param.replace("prpCmain.sumAmount=", "prpCmain.sumAmount=" + sumAmountStr);
		param = param.replace("prpCmain.sumDiscount=", "prpCmain.sumDiscount=" + QuoteCalculateUtils.mN(String.valueOf(sumDiscount), 2));
		param = param.replace("insurePayTimes=", "insurePayTimes=" + insurePayTimes);
		param = param.replace("claimAdjustValue=", "claimAdjustValue=" + claimAdjustValue);
		param = param.replace("prpCitemKindsTemp.itemKindSpecialSumPremium=", "prpCitemKindsTemp.itemKindSpecialSumPremium="  + QuoteCalculateUtils.mN(String.valueOf(itemKindSpecialSumPremium), 2));
		JSONObject prpCitemCars = syxJson.getJSONArray("data").getJSONObject(0).getJSONArray("prpCitemCars").getJSONObject(0);
		param = param.replace("prpCitemCar.coefficient1=", "prpCitemCar.coefficient1=" + QuoteCalculateUtils.mN(prpCitemCars.getString("coefficient1"), 2));
		param = param.replace("prpCitemCar.coefficient2=", "prpCitemCar.coefficient2=" + QuoteCalculateUtils.mN(prpCitemCars.getString("coefficient2"), 2));
		param = param.replace("prpCitemCar.coefficient3=", "prpCitemCar.coefficient3=" + QuoteCalculateUtils.mN(prpCitemCars.getString("coefficient3"), 2));
		
		Double sumPremium1 = 0D;
		Pattern p = Pattern.compile("&prpCmain.sumPremium1=(.*)&sumPayTax1");   
		Matcher m = p.matcher(param);   
		while(m.find()){   
			try {
				sumPremium1 = Double.parseDouble(m.group(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sumPremium1 = sumPremium1 + Double.parseDouble(QuoteCalculateUtils.mN(String.valueOf(sumPremium), 2));
		param = param.replace("prpCmain.sumPremium1=0", "prpCmain.sumPremium1=" + sumPremium1);//交强险 + 商业险总价
		param = param.replace("prpCsettlement.buyerPreFee=", "prpCsettlement.buyerPreFee=" + sumPremium1);
		
		//写死参数
		param = param.replace("CarActualValueTrue1=", "CarActualValueTrue1=49530");
		//param = param.replace("prpCitemCar.useYears=5", "prpCitemCar.useYears=6");
		param = param.replace("prpCitemCar.carLotEquQuality=0.00", "prpCitemCar.carLotEquQuality=1200.00");
		param = param.replace("prpCitemCarExt_CI.rateRloatFlag=", "prpCitemCarExt_CI.rateRloatFlag=01");
		param = param.replace("prpCitemCarExt_CI.damFloatRatioCI=0", "prpCitemCarExt_CI.damFloatRatioCI=10.0000");
		param = param.replace("prpCitemCarExt_CI.offFloatRatioCI=0", "prpCitemCarExt_CI.offFloatRatioCI=0.0000");
		
		return param;
	}
	
	/**
	 * 替换险种的相关报价信息
	 * @param param
	 * @param kindNum
	 * @param prpCitemKinds
	 * @return
	 */
	private String replaceInsInfo(String param, String kindNum, JSONObject prpCitemKinds){
		String unitAmount = prpCitemKinds.getString("unitAmount");
		unitAmount = QuoteCalculateUtils.mN(unitAmount, 2);
		if(!"3".equals(kindNum) && !"4".equals(kindNum)){//3 司机、4乘客
			param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.unitAmount=", "prpCitemKindsTemp%5B" + kindNum + "%5D.unitAmount=" + unitAmount);
		}
		//param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.quantity=", "prpCitemKindsTemp%5B" + kindNum + "%5D.quantity=" + prpCitemKinds.getString("quantity"));
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.amount=&", "prpCitemKindsTemp%5B" + kindNum + "%5D.amount=" + prpCitemKinds.getString("amount") + "&");
		JSONObject stareTime = prpCitemKinds.getJSONObject("startDate");
		Long time1 = stareTime.getLong("time");
		Date startDate = new Date(time1);
		startDate = DateUtils.addYears(startDate, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.startDate=", "prpCitemKindsTemp%5B" + kindNum + "%5D.startDate=" + sdf.format(startDate));
		JSONObject endTime = prpCitemKinds.getJSONObject("endDate");
		Long time2 = endTime.getLong("time");
		Date endDate = new Date(time2);
		endDate = DateUtils.addYears(endDate, -1);
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.endDate=", "prpCitemKindsTemp%5B" + kindNum + "%5D.endDate=" + sdf.format(endDate));
		String basePremium = prpCitemKinds.getString("basePremium");
		if("0".equals(basePremium) || StringUtils.isBlank(basePremium)){
			basePremium = "0";
		}else{
			basePremium = QuoteCalculateUtils.mN(basePremium, 2);
		}
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.basePremium=", "prpCitemKindsTemp%5B" + kindNum + "%5D.basePremium=" + basePremium);
		String rate = prpCitemKinds.getString("rate");
		if("0".equals(rate) || StringUtils.isBlank(rate)){
			rate = "";
		}else{
			rate = QuoteCalculateUtils.mN(rate, 4);
		}
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.rate=", "prpCitemKindsTemp%5B" + kindNum + "%5D.rate=" + rate);
		String benchMarkPremium = prpCitemKinds.getString("benchMarkPremium");
		benchMarkPremium = QuoteCalculateUtils.mN(benchMarkPremium, 2);
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.benchMarkPremium=", "prpCitemKindsTemp%5B" + kindNum + "%5D.benchMarkPremium=" + benchMarkPremium);
		String disCount = prpCitemKinds.getString("disCount");
		disCount = QuoteCalculateUtils.mN(disCount, 4);
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.disCount=", "prpCitemKindsTemp%5B" + kindNum + "%5D.disCount=" + disCount);
		String premium = prpCitemKinds.getString("premium");
		premium = QuoteCalculateUtils.mN(premium, 2);
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.premium=", "prpCitemKindsTemp%5B" + kindNum + "%5D.premium=" + premium);
		String netPremium = prpCitemKinds.getString("netPremium");
		netPremium = QuoteCalculateUtils.mN(netPremium, 2); 
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.netPremium=", "prpCitemKindsTemp%5B" + kindNum + "%5D.netPremium=" + netPremium);
		String taxPremium = prpCitemKinds.getString("taxPremium");
		taxPremium = QuoteCalculateUtils.mN(taxPremium, 2); 
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.taxPremium=", "prpCitemKindsTemp%5B" + kindNum + "%5D.taxPremium=" + taxPremium);
		String taxRate = prpCitemKinds.getString("taxRate");
		taxRate = QuoteCalculateUtils.mN(taxRate, 2); 
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.taxRate=", "prpCitemKindsTemp%5B" + kindNum + "%5D.taxRate=" + taxRate);
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.dutyFlag=", "prpCitemKindsTemp%5B" + kindNum + "%5D.dutyFlag=" + prpCitemKinds.getString("dutyFlag"));
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.endHour=", "prpCitemKindsTemp%5B" + kindNum + "%5D.endHour=24");
		param = param.replace("prpCitemKindsTemp%5B" + kindNum + "%5D.startHour=", "prpCitemKindsTemp%5B" + kindNum + "%5D.startHour=0");
		
		StringBuffer paramTempBuffer = new StringBuffer();
		JSONArray prpCprofitDetailsArray = prpCitemKinds.getJSONArray("prpCprofits").getJSONObject(0).getJSONArray("prpCprofitDetails");
		for(int i = 0; i<prpCprofitDetailsArray.size() ; i++){
			JSONObject prpCprofitDetails = prpCprofitDetailsArray.getJSONObject(i);
            JSONObject prpCprofitDetailsId = prpCprofitDetails.getJSONObject("id");
            int kindNumInt = Integer.parseInt(kindNum);
            int x = 0;
            if(kindNumInt >= 16){
            	switch (kindNumInt) {
				case 16:
					x = 32 + i;
					break;
				case 17:
					x = 36 + i;
					break;
				case 18:
					x = 40 + i;
					break;
				case 19:
					x = 44 + i;
					break;
				case 20:
					x = 48 + i;
					break;
				case 21:
					x = 52 + i;
					break;
				case 22:
					x = 56 + i;
					break;
				case 23:
					x = 60 + i;
					break;
				}
            }else{
            	x = (Integer.parseInt(kindNum) * 4) + i;
            }
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.chooseFlag=on";
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.chooseFlag=on");
			String profitName = prpCprofitDetails.getString("profitName");
			try {
				profitName = java.net.URLEncoder.encode(profitName, "gb2312");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.profitName=" + profitName;
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.profitName=" + profitName);
			String condition = prpCprofitDetails.getString("condition");
			try {
				condition = java.net.URLEncoder.encode(condition, "gb2312");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.condition=" + condition;
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.condition=" + condition);
			int profitRate = prpCprofitDetails.getIntValue("profitRate");
			String profitRateStr = QuoteCalculateUtils.mN(String.valueOf(profitRate * 0.01), 4);
			//param = param + "&profitRateTemp%5B" + x + "%5D=" + profitRateStr;
			paramTempBuffer.append("&profitRateTemp%5B" + x + "%5D=" + profitRateStr);
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.profitRate=" + profitRate;
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.profitRate=" + profitRate);
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.profitRateMin=" + prpCprofitDetails.getString("profitRateMin");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.profitRateMin=" + prpCprofitDetails.getString("profitRateMin"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.profitRateMax=" + prpCprofitDetails.getString("profitRateMax");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.profitRateMax=" + prpCprofitDetails.getString("profitRateMax"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.id.proposalNo=" + prpCprofitDetailsId.getString("proposalNo");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.id.proposalNo=" + prpCprofitDetailsId.getString("proposalNo"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.id.itemKindNo=" + prpCprofitDetailsId.getString("itemKindNo");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.id.itemKindNo=" + prpCprofitDetailsId.getString("itemKindNo"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.id.profitCode=" + prpCprofitDetailsId.getString("profitCode");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.id.profitCode=" + prpCprofitDetailsId.getString("profitCode"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.id.serialNo=" + prpCprofitDetailsId.getString("serialNo");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.id.serialNo=" + prpCprofitDetailsId.getString("serialNo"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.id.profitType=" + prpCprofitDetailsId.getString("profitType");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.id.profitType=" + prpCprofitDetailsId.getString("profitType"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.kindCode=" + prpCitemKinds.getString("kindCode");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.kindCode=" + prpCitemKinds.getString("kindCode"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.conditionCode=" + prpCprofitDetails.getString("conditionCode");
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.conditionCode=" + prpCprofitDetails.getString("conditionCode"));
			//param = param + "&prpCprofitDetailsTemp%5B" + x + "%5D.flag=";
			paramTempBuffer.append("&prpCprofitDetailsTemp%5B" + x + "%5D.flag=");
			if("23".equals(kindNum) && i == 0){
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.chooseFlag=", "prpCprofitDetailsTemp_%5B0%5D.chooseFlag=on");
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.profitName=", "prpCprofitDetailsTemp_%5B0%5D.profitName=" + profitName);
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.condition=", "prpCprofitDetailsTemp_%5B0%5D.condition=" + condition);
				param = param.replace("profitRateTemp_%5B0%5D=", "profitRateTemp_%5B0%5D=" + profitRateStr);
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.profitRate=", "prpCprofitDetailsTemp_%5B0%5D.profitRate=" + profitRate);
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.profitRateMin=", "prpCprofitDetailsTemp_%5B0%5D.profitRateMin=" + prpCprofitDetails.getString("profitRateMin"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.profitRateMax=", "prpCprofitDetailsTemp_%5B0%5D.profitRateMax=" + prpCprofitDetails.getString("profitRateMax"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=", "prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=" + prpCprofitDetailsId.getString("proposalNo"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=", "prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=" + prpCprofitDetailsId.getString("itemKindNo"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.id.profitCode=", "prpCprofitDetailsTemp_%5B0%5D.id.profitCode="+ prpCprofitDetailsId.getString("profitCode"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.id.serialNo=", "prpCprofitDetailsTemp_%5B0%5D.id.serialNo="+ prpCprofitDetailsId.getString("serialNo"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.id.profitType=", "prpCprofitDetailsTemp_%5B0%5D.id.profitType="+ prpCprofitDetailsId.getString("profitType"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.kindCode=", "prpCprofitDetailsTemp_%5B0%5D.kindCode=" + prpCitemKinds.getString("kindCode"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.conditionCode=", "prpCprofitDetailsTemp_%5B0%5D.conditionCode=" + prpCprofitDetails.getString("conditionCode"));
				param = param.replace("prpCprofitDetailsTemp_%5B0%5D.flag=", "prpCprofitDetailsTemp_%5B0%5D.flag=");
			}
		}
		String reg = "&prpCitemKindsTemp%5B" + kindNum + "%5D.disCount=" + disCount;
		String[] paramArray = param.split(reg);
		param = paramArray[0] + reg + paramTempBuffer.toString() + paramArray[1];
		return param;
	}
	
	/**
	 * 替换交强险参数
	 * @param param
	 * @param carNo
	 * @param idCode
	 * @return
	 */
	private String setJqxHebaoParam(String param,  String carNo, String idCode){
		Map<String, String> quoteJsonMap = CacheConstant.quoteResultJsonInfo.get(carNo);
		JSONObject jqxJson = JSONObject.parseObject(quoteJsonMap.get("jqxJson"));
		//第二部分：交强险 json对象 ciInsureDemand
		JSONObject ciInsureDemand = jqxJson.getJSONArray("data").getJSONObject(0).getJSONObject("ciInsureDemand");
		param = param.replace("ciInsureDemand.demandNo=", "ciInsureDemand.demandNo=" + ciInsureDemand.getString("demandNo"));
		JSONObject demandTime = ciInsureDemand.getJSONObject("demandTime");
		Long time1 = demandTime.getLong("time");
		Date demandDate = new Date(time1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		param = param.replace("ciInsureDemand.demandTime=", "ciInsureDemand.demandTime=" + sdf.format(demandDate));
		param = param.replace("ciInsureDemand.restricFlag=", "ciInsureDemand.restricFlag=" + ciInsureDemand.getString("restricFlag"));
		param = param.replace("ciInsureDemand.preferentialDay=", "ciInsureDemand.preferentialDay=" + ciInsureDemand.getString("preferentialDay"));
		param = param.replace("ciInsureDemand.preferentialPremium=", "ciInsureDemand.preferentialPremium=" + ciInsureDemand.getString("preferentialPremium"));
		String preferentialFormula = ciInsureDemand.getString("preferentialFormula");
		try {
			String[] preferentialFormulas = preferentialFormula.split("=");
			preferentialFormulas[0] = java.net.URLEncoder.encode(preferentialFormulas[0], "gb2312");
			preferentialFormulas[1] = preferentialFormulas[1].replace(" + ", " %2B ");
			preferentialFormula = preferentialFormulas[0] + "=" + preferentialFormulas[1];
		} catch (Exception e) {
			e.getMessage();
		}
		param = param.replace("ciInsureDemand.preferentialFormula%20=", "ciInsureDemand.preferentialFormula%20=" + preferentialFormula);
		JSONObject lastyearenddate = ciInsureDemand.getJSONObject("lastyearenddate");
		Long time2 = lastyearenddate.getLong("time");
		Date lastyearendTime = new Date(time2);
		param = param.replace("ciInsureDemand.lastyearenddate=", "ciInsureDemand.lastyearenddate=" + sdf.format(lastyearendTime));
		param = param.replace("ciInsureDemand.rateRloatFlag=00", "ciInsureDemand.rateRloatFlag=00");
		param = param.replace("ciInsureDemand.claimAdjustReason=A1", "ciInsureDemand.claimAdjustReason=A1");
		param = param.replace("ciInsureDemand.peccancyAdjustReason=V1", "ciInsureDemand.peccancyAdjustReason=V1");
		String licenseNo = ciInsureDemand.getString("licenseNo");
		try {
			licenseNo = java.net.URLEncoder.encode(licenseNo, "gb2312");
		} catch (Exception e) {
			e.getMessage();
		}
		param = param.replace("ciInsureDemand.licenseNo=", "ciInsureDemand.licenseNo=" + licenseNo);
		param = param.replace("ciInsureDemand.licenseType=", "ciInsureDemand.licenseType=" + ciInsureDemand.getString("licenseType"));
		param = param.replace("ciInsureDemand.useNatureCode=", "ciInsureDemand.useNatureCode=" + ciInsureDemand.getString("useNatureCode"));
		param = param.replace("ciInsureDemand.frameNo=", "ciInsureDemand.frameNo=" + ciInsureDemand.getString("frameNo"));
		param = param.replace("ciInsureDemand.engineNo=", "ciInsureDemand.engineNo=" + ciInsureDemand.getString("engineNo"));
		param = param.replace("ciInsureDemand.licenseColorCode=", "ciInsureDemand.licenseColorCode=" + ciInsureDemand.getString("licenseColorCode"));
		String carOwner = ciInsureDemand.getString("carOwner");
		try {
			carOwner = java.net.URLEncoder.encode(carOwner, "gb2312");
		} catch (Exception e) {
			e.getMessage();
		}
		param = param.replace("ciInsureDemand.carOwner=", "ciInsureDemand.carOwner=" + carOwner);
		param = param.replace("ciInsureDemand.enrollDate=", "ciInsureDemand.enrollDate=");
		param = param.replace("ciInsureDemand.makeDate=", "ciInsureDemand.makeDate=");
		param = param.replace("ciInsureDemand.seatCount=", "ciInsureDemand.seatCount=" + ciInsureDemand.getString("seatCount"));
		param = param.replace("ciInsureDemand.tonCount=", "ciInsureDemand.tonCount=" + ciInsureDemand.getString("tonCount"));
		param = param.replace("ciInsureDemand.validCheckDate=", "ciInsureDemand.validCheckDate=");
		String manufacturerName = ciInsureDemand.getString("manufacturerName");
		try {
			manufacturerName = java.net.URLEncoder.encode(manufacturerName, "gb2312");
		} catch (Exception e) {
			e.getMessage();
		}
		param = param.replace("ciInsureDemand.manufacturerName=", "ciInsureDemand.manufacturerName=" + manufacturerName);
		param = param.replace("ciInsureDemand.modelCode=", "ciInsureDemand.modelCode=" + ciInsureDemand.getString("modelCode"));
		String brandCName = ciInsureDemand.getString("brandCName");
		try {
			brandCName = java.net.URLEncoder.encode(brandCName, "gb2312");
		} catch (Exception e) {
			e.getMessage();
		}
		param = param.replace("ciInsureDemand.brandCName=", "ciInsureDemand.brandCName=" + brandCName);
		param = param.replace("ciInsureDemand.brandName=", "ciInsureDemand.brandName=");
		param = param.replace("ciInsureDemand.carKindCode=", "ciInsureDemand.carKindCode=" + ciInsureDemand.getString("carKindCode"));
		param = param.replace("ciInsureDemand.checkDate=", "ciInsureDemand.checkDate=");
		param = param.replace("ciInsureDemand.endValidDate=", "ciInsureDemand.endValidDate=");
		param = param.replace("ciInsureDemand.carStatus=", "ciInsureDemand.carStatus=" + ciInsureDemand.getString("carStatus"));
		param = param.replace("ciInsureDemand.haulage=", "ciInsureDemand.haulage=");
		//第五部分：交强险json对象 ciInsureDemandCheckVo
		JSONObject ciInsureDemandCheckVo = jqxJson.getJSONArray("data").getJSONObject(0).getJSONObject("ciInsureDemandCheckVo");
		param = param.replace("ciInsureDemandCheckVo.demandNo=", "ciInsureDemandCheckVo.demandNo=" + ciInsureDemandCheckVo.getString("demandNo"));
		param = param.replace("ciInsureDemandCheckVo.checkQuestion=", "ciInsureDemandCheckVo.checkQuestion=" + ciInsureDemandCheckVo.getString("checkQuestion"));
		param = param.replace("ciInsureDemandCheckVo.checkAnswer=", "ciInsureDemandCheckVo.checkAnswer=" + ciInsureDemandCheckVo.getString("checkAnswer"));
		if(StringUtils.isNotBlank(ciInsureDemandCheckVo.getString("flag"))){
			param = param.replace("ciInsureDemandCheckVo.flag=DEMAND", "ciInsureDemandCheckVo.flag=" + ciInsureDemandCheckVo.getString("flag"));
		}
		param = param.replace("ciInsureDemandCheckVo.riskCode=", "ciInsureDemandCheckVo.riskCode=" + ciInsureDemandCheckVo.getString("riskCode"));
		//第五部分：特别约定  值判断一个 （应该有很多）
		//1、910012
		Double preferentialDay = ciInsureDemand.getDouble("preferentialDay");
		Double preferentialPremium = ciInsureDemand.getDouble("preferentialPremium");
		StringBuffer paramBuffer3 = new StringBuffer();
		if(preferentialDay>0 && preferentialPremium>0 && preferentialFormula.length()>0){//存在910012
			param = param.replace("prpCengageTemps_%5B0%5D.id.serialNo=", "prpCengageTemps_%5B0%5D.id.serialNo=1");
			param = param.replace("prpCengageTemps_%5B0%5D.clauseCode=", "prpCengageTemps_%5B0%5D.clauseCode=910012");
			param = param.replace("prpCengageTemps_%5B0%5D.clauseName=", "prpCengageTemps_%5B0%5D.clauseName=%CE%B2%BA%C5%BC%F5%C3%E2%CC%D8%D4%BC");
			param = param.replace("prpCengageTemps_%5B0%5D.flag=", "prpCengageTemps_%5B0%5D.flag=");
			param = param.replace("prpCengageTemps_%5B0%5D.engageFlag=", "prpCengageTemps_%5B0%5D.engageFlag=");
			param = param.replace("prpCengageTemps_%5B0%5D.maxCount=", "prpCengageTemps_%5B0%5D.maxCount=");
			param = param.replace("prpCengageTemps_%5B0%5D.clauses=", "prpCengageTemps_%5B0%5D.clauses=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA" + preferentialDay + "%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D1" + preferentialPremium + "%D4%AA%A3%AC " + preferentialFormula + "%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3");
			
			paramBuffer3.append("&prpCengageTemps%5B0%5D.id.serialNo=1");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.clauseCode=910012");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.clauseName=%CE%B2%BA%C5%BC%F5%C3%E2%CC%D8%D4%BC");
			paramBuffer3.append("&clauses%5B0%5D=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA" + preferentialDay + "%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D1" + preferentialPremium + "%D4%AA%A3%AC " + preferentialFormula + "%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.flag=");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.engageFlag=");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.maxCount=");
			paramBuffer3.append("&prpCengageTemps%5B0%5D.clauses=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA" + preferentialDay + "%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D1" + preferentialPremium + "%D4%AA%A3%AC " + preferentialFormula + "%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3");
		}
		String reg3 = "&hidden_index_engage=0";
		String[] paramArray3 = param.split(reg3);
		param = paramArray3[0] + reg3 + paramBuffer3.toString() + paramArray3[1];
		//第九部分：设置车船税
		JSONObject ciCarShipTax = jqxJson.getJSONArray("data").getJSONObject(0).getJSONObject("ciCarShipTax");
		param = param.replace("prpCcarShipTax.taxPayerIdentNo=", "prpCcarShipTax.taxPayerIdentNo=" + idCode);//身份证 ？
		param = param.replace("prpCcarShipTax.taxPayerNumber=", "prpCcarShipTax.taxPayerNumber=" + idCode);
		param = param.replace("prpCcarShipTax.taxPayerCode=", "prpCcarShipTax.taxPayerCode=1100100001900281");//？
		param = param.replace("prpCcarShipTax.taxPayerName=", "prpCcarShipTax.taxPayerName=" + carOwner);//姓名？
		
		param = param.replace("prpCcarShipTax.carLotEquQuality=", "prpCcarShipTax.carLotEquQuality=" + ciCarShipTax.getString("poWeight"));//重量
		param = param.replace("prpCcarShipTax.taxComName=", "prpCcarShipTax.taxComName=" + ciCarShipTax.getString("taxComName"));
		param = param.replace("prpCcarShipTax.dutyPaidProofNo=", "prpCcarShipTax.dutyPaidProofNo=" + ciCarShipTax.getString("dutyPaidProofNo"));
		JSONObject payStartDate = ciCarShipTax.getJSONObject("payStartDate");
		Long time3= payStartDate.getLong("time");
		Date payStartTime = new Date(time3);
		param = param.replace("prpCcarShipTax.payStartDate=", "prpCcarShipTax.payStartDate=" + sdf.format(payStartTime));
		JSONObject payEndDate = ciCarShipTax.getJSONObject("payEndDate");
		Long time4 = payEndDate.getLong("time");
		Date payEndTime = new Date(time4);
		param = param.replace("prpCcarShipTax.payEndDate=", "prpCcarShipTax.payEndDate=" + sdf.format(payEndTime));
		param = param.replace("prpCcarShipTax.thisPayTax=", "prpCcarShipTax.thisPayTax=" + ciCarShipTax.getString("thisPayTax"));
		param = param.replace("prpCcarShipTax.prePayTax=", "prpCcarShipTax.prePayTax=" + ciCarShipTax.getString("prePayTax"));
		param = param.replace("prpCcarShipTax.delayPayTax=", "prpCcarShipTax.delayPayTax=" + ciCarShipTax.getString("delayPayTax"));
		param = param.replace("prpCcarShipTax.sumPayTax=", "prpCcarShipTax.sumPayTax=" + ciCarShipTax.getString("thisPayTax"));
		param = param.replace("prpCcarShipTax.flag=", "prpCcarShipTax.flag=" + ciCarShipTax.getString("flag"));
		//第十部分：设置交强险
		param = param.replace("prpCitemKindCI.benchMarkPremium=", "prpCitemKindCI.benchMarkPremium=950");
		String ciPremium = ciInsureDemand.getString("premium");
		ciPremium = QuoteCalculateUtils.mN(ciPremium, 2);
		param = param.replace("prpCitemKindCI.premium=", "prpCitemKindCI.premium=" + ciPremium);
		String ciNetPremium = ciInsureDemand.getString("netPremium");
		ciNetPremium = QuoteCalculateUtils.mN(ciNetPremium, 2);
		param = param.replace("prpCitemKindCI.netPremium=", "prpCitemKindCI.netPremium=" + ciNetPremium);
		String ciTaxPremium = ciInsureDemand.getString("taxPremium");
		ciTaxPremium = QuoteCalculateUtils.mN(ciTaxPremium, 2);
		param = param.replace("prpCitemKindCI.taxPremium=", "prpCitemKindCI.taxPremium=" + ciTaxPremium);
		String ciTaxRate = ciInsureDemand.getString("taxRate");
		ciTaxRate = QuoteCalculateUtils.mN(ciTaxRate, 2);
		param = param.replace("prpCitemKindCI.taxRate=", "prpCitemKindCI.taxRate=" + ciTaxRate);
		param = param.replace("prpCitemKindCI.dutyFlag=", "prpCitemKindCI.dutyFlag=2");
		param = param.replace("prpCmainCI.sumAmount=", "prpCmainCI.sumAmount=122000");
		param = param.replace("prpCmainCI.sumPremium=", "prpCmainCI.sumPremium=" + ciPremium);
		param = param.replace("prpCmainCar.rescueFundRate=", "prpCmainCar.rescueFundRate=" + ciInsureDemand.getString("rescueFundRate"));
		param = param.replace("prpCmainCar.resureFundFee=", "prpCmainCar.resureFundFee=" + ciInsureDemand.getString("resureFundFee"));
		param = param.replace("rescueFundRate=&", "rescueFundRate=" + ciInsureDemand.getString("rescueFundRate") + "&");
		param = param.replace("resureFundFee=&", "resureFundFee=" + ciInsureDemand.getString("resureFundFee") + "&");
		
		Double adjustRate = 0D;//交强险系数  ciInsureDemand
		Double ciSumDiscount = 0D;
		String rateFloatFlag = "ND4";
		adjustRate =  1 + ciInsureDemand.getDoubleValue("peccancyCoeff") + ciInsureDemand.getDoubleValue("claimCoeff");
		param = param.replace("prpCitemKindCI.adjustRate=0.9", "prpCitemKindCI.adjustRate=" + QuoteCalculateUtils.mN(String.valueOf(adjustRate), 2));
		param = param.replace("prpCcarShipTax.prePayTaxYear=", "prpCcarShipTax.prePayTaxYear=2015");
		param = param.replace("prpCitemKindCI.deductible=", "prpCitemKindCI.deductible=0");
		switch (String.valueOf(adjustRate)) {
		case "0.90":
			rateFloatFlag = "ND1";
			break;
		case "0.80":
			rateFloatFlag = "ND2";
			break;
		case "0.70":
			rateFloatFlag = "ND3";
			break;
		case "1.00":
			rateFloatFlag = "ND4";
			break;
		case "1.10":
			rateFloatFlag = "ND5";
			break;
		case "1.30":
			rateFloatFlag = "ND6";
			break;
		}
		param = param.replace("rateFloatFlag=ND4", "rateFloatFlag=" + rateFloatFlag);
		param = param.replace("prpCitemKindCI.familyNo=", "prpCitemKindCI.familyNo=1");//选择交强险
		param = param.replace("sumPremiumChgFlag=0", "sumPremiumChgFlag=1");
		param = param.replace("premiumChangeFlag=1", "premiumChangeFlag=");
		param = param.replace("prpCmain.othFlag=110000YY00", "prpCmain.othFlag=100000YY00");//?
		ciSumDiscount = QuoteCalculateUtils.sub(950, Double.parseDouble(ciPremium)) ;
		param = param.replace("prpCmainCI.sumDiscount=", "prpCmainCI.sumDiscount=" + QuoteCalculateUtils.mN(String.valueOf(ciSumDiscount), 2));
		param = param.replace("sumPayTax1=0", "sumPayTax1=" + QuoteCalculateUtils.mN(ciCarShipTax.getString("thisPayTax"), 2));
		param = param.replace("prpCstampTaxCI.ciPayTax=", "prpCstampTaxCI.ciPayTax=0");
		
		Double sumPremium1 = 0D;
		Pattern p = Pattern.compile("&prpCmain.sumPremium1=(.*)&sumPayTax1");   
		Matcher m = p.matcher(param);   
		while(m.find()){   
			try {
				sumPremium1 = Double.parseDouble(m.group(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//前替换
		param = param.replace("prpCmain.sumPremium1=" + sumPremium1, "prpCmain.sumPremium1=0");//交强险 + 商业险总价
		param = param.replace("prpCsettlement.buyerPreFee=" + sumPremium1, "prpCsettlement.buyerPreFee=");
		sumPremium1 =sumPremium1 + Double.parseDouble(QuoteCalculateUtils.mN(String.valueOf(ciPremium), 2));
		param = param.replace("prpCmain.sumPremium1=0", "prpCmain.sumPremium1=" + sumPremium1);//交强险 + 商业险总价
		param = param.replace("prpCsettlement.buyerPreFee=", "prpCsettlement.buyerPreFee=" + sumPremium1);
		
		return param;
	}
}
