package com.ihandy.quote_core.service.impl.picc;

import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;


import com.ihandy.quote_core.bean.other.*;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.bean.other.RelaPeopleResponse;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;
import com.ihandy.quote_core.serverpage.picc.*;
import com.ihandy.quote_core.service.IService;

import com.ihandy.quote_core.utils.SysConfigInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fengwen on 2016/4/29.
 */
@Service
public class RBServiceImpl implements IService {

	private static Logger logger = LoggerFactory.getLogger(RBServiceImpl.class);
	private static String licenseType="02";//车牌类型小型汽车
	private static Map  SysXubaoParamsMap =new HashMap();
	@Override
	public BaseCarInfoResponse getBaseCarInfoByLicenseNo(String licenseNo,int CityCode) {
		BaseCarInfoResponse carBaseInfoResponse = new BaseCarInfoResponse();
		//  Response responseIndex = goXubaoIndex();
		//  if(responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200){
		Response responseIndex =new Response();

		Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);
		if(responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200){
			Response responseBrowse = xubaoBrowsePolicyNo(responseSearch);
			if(responseBrowse.getReturnCode() == SysConfigInfo.SUCCESS200){
				//获取车辆基本信息
				Response responseCitemCar = xubaoGetCitemCar(responseBrowse);
				if(responseCitemCar.getReturnCode() == SysConfigInfo.SUCCESS200) {
					//获取车辆关系人信息
					Response responseCinsure = xubaoGetCinsure(responseBrowse);
					if(responseCinsure.getReturnCode() == SysConfigInfo.SUCCESS200) {
						// TODO: 2016/5/17 将返回数据填充到carInfoResponse中
						// 1 ) 从search中返回的保单list中获取
						Map returnSearchMap = responseSearch.getResponseMap();
						Map lastResultSearchMap = (Map) returnSearchMap.get("lastResult");
						carBaseInfoResponse.setCarVin(lastResultSearchMap.get("CarVin").toString());//车架号
						carBaseInfoResponse.setLicenseNo(lastResultSearchMap.get("LicenseNo").toString());//车牌号
						carBaseInfoResponse.setEngineNo(lastResultSearchMap.get("EngineNo").toString());//发动机号
						carBaseInfoResponse.setBusinessExpireDate(lastResultSearchMap.get("BusinessExpireDate").toString());//商业险到期日期
						carBaseInfoResponse.setForceExpireDate(lastResultSearchMap.get("ForceExpireDate").toString());//交强险到期日期
						// 2 ) 从浏览保单基本车辆信息中获取
						Map returnCitemCarMap = responseCitemCar.getResponseMap();
						Map lastResultCitemCarMap = (Map) returnCitemCarMap.get("lastResult");
						carBaseInfoResponse.setCarSeated(Integer.parseInt(lastResultCitemCarMap.get("CarSeated").toString()));//核定座位数
						carBaseInfoResponse.setMoldName(lastResultCitemCarMap.get("MoldName").toString());//车型
						carBaseInfoResponse.setPurchasePrice(Double.parseDouble(lastResultCitemCarMap.get("PurchasePrice").toString()));//新车购买价格
						carBaseInfoResponse.setCarRegisterDate(lastResultCitemCarMap.get("CarRegisterDate").toString());//车辆首次登记日期
						carBaseInfoResponse.setCarUsedType(lastResultCitemCarMap.get("CarUsedType").toString());//车辆使用性质
						// 3 ) 从浏览保单保险关系人信息中获取
						Map returnCinsureMap = responseCinsure.getResponseMap();
						Map lastResultCinsureMap = (Map) returnCinsureMap.get("lastResult");
						Iterator entries = lastResultCinsureMap.entrySet().iterator();
						while (entries.hasNext()) {
							Map.Entry entry = (Map.Entry) entries.next();
							Map value = (Map) entry.getValue();
							if (value.get("role").toString().equals("投保人")) {
								carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
							}else if(value.get("role").toString().equals("车主")){
								carBaseInfoResponse.setCredentislasNum(value.get("CredentislasNum").toString());//证件号码
								carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
								carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
							}else if(value.get("role").toString().equals("被保险人")){
								carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
							}else if (value.get("role").toString().equals("被保险人/车主")) {
								carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
								carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
								carBaseInfoResponse.setCredentislasNum(value.get("CredentislasNum").toString());//证件号码
								carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
							}
							else if (value.get("role").toString().equals("投保人/车主")) {
								carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
								carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
								carBaseInfoResponse.setCredentislasNum(value.get("CredentislasNum").toString());//证件号码
								carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
							}
							else if(value.get("role").toString().equals("投保人/被保险人")){
								carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
								carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人/车主
							}
							else if (value.get("role").toString().equals("投保人/被保险人/车主")) {
								carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
								carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
								carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
								carBaseInfoResponse.setCredentislasNum(value.get("CredentislasNum").toString());//证件号码
								carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
							}
						}
						// 4 ) 从参数中获取
						carBaseInfoResponse.setCityCode(CityCode);
					}else{
						logger.info("抓取机器人，【 PICC 获取保单中车辆相关保险人信息错误】");
					}
				}else{
					logger.info("抓取机器人，【 PICC 获取保单中车辆基本信息错误】");
				}
			}else{
				logger.info("抓取机器人，【 PICC 按保单号查看保单错误】");
			}
		}else{
			logger.info("抓取机器人，【 PICC 按车牌号和类型查询保单错误】");
		}
		//  }else{
		//   logger.info("抓取机器人，【 PICC 跳转续保页面错误】");
		// }
		return carBaseInfoResponse;
	}

	@Override
	public CarInfoResponse getAllCarInfoByLicenseNo(String licenseNo,int CityCode) {
		CarInfoResponse carInfoResponse = new CarInfoResponse();
		//1）获取车辆基本信息
		BaseCarInfoResponse carBaseInfoResponse  = getBaseCarInfoByLicenseNo(licenseNo,CityCode);
		//2) 获取投被保人信息
		List<RelaPeopleResponse> relaPeopleResponseList = getRelaPeopleInfoByCarInfoList(licenseNo);
		//3) 获取险种信息
		SaveQuoteResponse saveQuoteResponse = getQuoteInfoByCarInfo(licenseNo);
		//4) 获取出险信息
		List<ClaimResponse> claimResponseList = getClaimInfoList(licenseNo);
		//信息封装成车辆全部信息
		carInfoResponse.setCarInfoBaseResponse(carBaseInfoResponse);
		carInfoResponse.setClaimResponseList(claimResponseList);
		carInfoResponse.setRelaPeopleResponseList(relaPeopleResponseList);
		carInfoResponse.setSaveQuoteResponse(saveQuoteResponse);
		return carInfoResponse;
	}

	@Override
	public SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo ) {
		SaveQuoteResponse saveQuoteResponse = new SaveQuoteResponse();
		// 需要根据实际应用来进行参数传递与配置，若缓存中有完整参数信息且session未过期,直接使用续保单号去查询险种信息；
		// 若参数信息不完整或session已过期，调用获取这些信息的接口后，才开始查询险种信息
		Response response = new Response();
		response = getBeforeXubao(licenseNo);
		if(response.getReturnCode()==SysConfigInfo.SUCCESS200){
			//成功返回
			Response responseCitemKind = xubaoGetCitemKind(response);
			Map returnCitemKindMap = responseCitemKind.getResponseMap();
			Map lastResultCitemKindMap = (Map) returnCitemKindMap.get("lastResult");
			saveQuoteResponse.setCheSun(Double.parseDouble(lastResultCitemKindMap.get("CheSun").toString()));//CheSun
			saveQuoteResponse.setDaoQiang(Double.parseDouble(lastResultCitemKindMap.get("DaoQiang").toString()));//DaoQiang
			saveQuoteResponse.setSanZhe(Double.parseDouble(lastResultCitemKindMap.get("SanZhe").toString()));//SanZhe
			saveQuoteResponse.setSiJi(Double.parseDouble(lastResultCitemKindMap.get("SiJi").toString()));//SiJi
			saveQuoteResponse.setChengKe(Double.parseDouble(lastResultCitemKindMap.get("ChengKe").toString()));//ChengKe
			saveQuoteResponse.setBoli(Double.parseDouble(lastResultCitemKindMap.get("Boli").toString()));//boli
			saveQuoteResponse.setZiRan(Double.parseDouble(lastResultCitemKindMap.get("ZiRan").toString()));//ZiRan
			saveQuoteResponse.setHuaHen(Double.parseDouble(lastResultCitemKindMap.get("HuaHen").toString()));//HuaHen
			saveQuoteResponse.setSheShui(Double.parseDouble(lastResultCitemKindMap.get("SheShui").toString()));//SheShui
			saveQuoteResponse.setCheDeng(Double.parseDouble(lastResultCitemKindMap.get("CheDeng").toString()));//CheDeng
			saveQuoteResponse.setBuJiMianCheSun(Double.parseDouble(lastResultCitemKindMap.get("BuJiMianCheSun").toString()));//BuJiMianCheSun
			saveQuoteResponse.setBuJiMianSanZhe(Double.parseDouble(lastResultCitemKindMap.get("BuJiMianSanZhe").toString()));//BuJiMianSanZhe
			saveQuoteResponse.setBuJiMianDaoQiang(Double.parseDouble(lastResultCitemKindMap.get("BuJiMianDaoQiang").toString()));//BuJiMianDaoQiang
			saveQuoteResponse.setBuJiMianRenYuan(Double.parseDouble(lastResultCitemKindMap.get("BuJiMianRenYuan").toString()));//BuJiMianRenYuan
			saveQuoteResponse.setBuJiMianFuJia(Double.parseDouble(lastResultCitemKindMap.get("BuJiMianFuJia").toString()));//BuJiMianFuJia
			saveQuoteResponse.setSource(1);//PICC
		}
		else{
			logger.info("抓取机器人，【 PICC  查看续保险种信息页面信息时的参数信息错误】");
		}
		return saveQuoteResponse;
	}

	@Override
	public List<RelaPeopleResponse> getRelaPeopleInfoByCarInfoList(String licenseNo) {
		List<RelaPeopleResponse> relaPeopleResponseList = new ArrayList<>();
		Response response = new Response();
		response = getBeforeXubao(licenseNo);
		if(response.getReturnCode()==SysConfigInfo.SUCCESS200){
			Response responseCinsure = xubaoGetCinsure(response);
			Map returnCinsureMap = responseCinsure.getResponseMap();
			Map lastResultCinsureMap = (Map) returnCinsureMap.get("lastResult");
			Iterator it = lastResultCinsureMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				RelaPeopleResponse relaPeopleResponse = new RelaPeopleResponse();
				Map value = (Map) entry.getValue();
				relaPeopleResponse.setRole(value.get("role").toString());
				relaPeopleResponse.setType(value.get("type").toString());
				relaPeopleResponse.setName(value.get("name").toString());
				relaPeopleResponse.setCompanyType(value.get("companyType").toString());
				relaPeopleResponse.setIdType(value.get("IdCardType").toString());
				relaPeopleResponse.setCredentislasNum(value.get("CredentislasNum").toString());
				relaPeopleResponse.setAddress(value.get("address").toString());
				relaPeopleResponse.setEmail(value.get("email").toString());
				relaPeopleResponse.setTelNum(value.get("telNum").toString());
				relaPeopleResponse.setMobilePhone(value.get("mobilePhone").toString());
				relaPeopleResponseList.add(relaPeopleResponse);
			}
		}
		else{
			logger.info("抓取机器人，【 PICC 查看续保投被保人页面信息时的参数信息错误】");
		}
		return relaPeopleResponseList;
	}

	@Override
	public List<ClaimResponse> getClaimInfoList(String licenseNo ) {
		List<ClaimResponse> ClaimResponseList = new ArrayList<>();
		Response response = new Response();
		response = getBeforeXubao(licenseNo);
		if(response.getReturnCode()==SysConfigInfo.SUCCESS200){
			Response claimResponse1 = xubaoQueryClaimsMsg(response);
			Map lastResultMap = (Map) claimResponse1.getResponseMap().get("lastResult");
			Iterator it = lastResultMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				ClaimResponse claimResponse = new ClaimResponse();
				Map value = (Map) entry.getValue();
				claimResponse.setEndCaseTime(value.get("EndCaseTime").toString());
				claimResponse.setLossTime(value.get("LossTime").toString());
				claimResponse.setPayAmount(Double.parseDouble(value.get("PayAmount").toString()));
				claimResponse.setPayCompanyName(value.get("PayCompanyName").toString());
				ClaimResponseList.add(claimResponse);
			}
		}
		else{
			logger.info("抓取机器人，【 PICC 查看续保投被保人页面信息时的参数信息错误】");
		}
		return ClaimResponseList;
	}
	/*******************************************************************************
	 * 获取查询续保车辆基本信息页面、投被保人页面、车险页面、出险页面需要response
	 *
	 * @return response nextParams null lastResult null
	 *******************************************************************************/
	public Response getBeforeXubao(String licenseNo ){
		Response response = new Response();
		Map  returnMap  = new HashMap<>();
		Map SysXubaoParamsMapItem =null;
		Map nextParamMap = new HashMap<>();
		try{
			SysXubaoParamsMapItem = (Map)SysXubaoParamsMap.get(licenseNo);//从内存中读取是否已存在该车牌号的续保信息
		}catch(Exception e){
		}
		if(SysXubaoParamsMapItem==null){
			// 重新生成
			try{
				Response responseIndex =new Response();
				Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);
				if(responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200){
					Response responseBrowse = xubaoBrowsePolicyNo(responseSearch);
					Map browseMap = (Map) responseBrowse.getResponseMap().get("nextParams");

					nextParamMap.put("bizNo", browseMap.get("bizNo").toString());// 上年商业保单号
					nextParamMap.put("bizType", browseMap.get("bizType").toString());
					nextParamMap.put("comCode", browseMap.get("comCode").toString());
					nextParamMap.put("contractNo", browseMap.get("contractNo").toString());
					nextParamMap.put("editType", browseMap.get("editType").toString());
					nextParamMap.put("minusFlag", "");
					nextParamMap.put("proposalNo", browseMap.get("proposalNo").toString());
					nextParamMap.put("riskCode", browseMap.get("riskCode").toString());
					nextParamMap.put("rnd704", "");

					//将续保信息写入系统内存中
					SysXubaoParamsMap.put(licenseNo, nextParamMap);


					returnMap.put("nextParams", nextParamMap);
					returnMap.put("lastResult", null);
					response.setResponseMap(returnMap);
					response.setReturnCode(SysConfigInfo.SUCCESS200);
					response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
				}else{
					returnMap.put("nextParams", null);
					returnMap.put("lastResult", null);
					response.setResponseMap(returnMap);
					response.setReturnCode(SysConfigInfo.ERROR404);
					response.setErrMsg(SysConfigInfo.ERROR404MSG);
				}

			}catch(Exception e){
				logger.info(" 机器人抓取，获取续保的参数失败");
				returnMap.put("nextParams", null);
				returnMap.put("lastResult", null);
				response.setResponseMap(returnMap);
				response.setReturnCode(SysConfigInfo.ERROR404);
				response.setErrMsg(SysConfigInfo.ERROR404MSG);
			}
		}else{
			try {

				nextParamMap.put("bizNo", SysXubaoParamsMapItem.get("bizNo").toString());// 上年商业保单号
				nextParamMap.put("bizType", SysXubaoParamsMapItem.get("bizType").toString());
				nextParamMap.put("comCode", SysXubaoParamsMapItem.get("comCode").toString());
				nextParamMap.put("contractNo", SysXubaoParamsMapItem.get("contractNo").toString());
				nextParamMap.put("editType", SysXubaoParamsMapItem.get("editType").toString());
				nextParamMap.put("minusFlag", "");
				nextParamMap.put("proposalNo", SysXubaoParamsMapItem.get("proposalNo").toString());
				nextParamMap.put("riskCode", SysXubaoParamsMapItem.get("riskCode").toString());
				nextParamMap.put("rnd704", "");

				returnMap.put("nextParams", nextParamMap);
				returnMap.put("lastResult", null);
				response.setResponseMap(returnMap);
				response.setReturnCode(SysConfigInfo.SUCCESS200);
				response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
			}catch (Exception e){
				returnMap.put("nextParams", null);
				returnMap.put("lastResult", null);
				response.setResponseMap(returnMap);
				response.setReturnCode(SysConfigInfo.ERROR404);
				response.setErrMsg(SysConfigInfo.ERROR404MSG);
			}
		}
		return response;
	}
	/*******************************************************************************
	 * 跳转续保页面
	 *
	 * @return response nextParams null lastResult null
	 *******************************************************************************/
	public Response goXubaoIndex() {
		XubaoIndexPage xubaoIndexPage = new XubaoIndexPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_EDITRENEWALSEARCH);// GET
		Response response = xubaoIndexPage.run(request);
		return response;
	}

	/*******************************************************************************
	 * 按车牌号查询续保单
	 *
	 * @param response
	 * @param licenseNo
	 * @param licenseType
	 * @return response nextParams(上年 DAT,上年 DZA) lastResult(车牌号，发动机号，车架号)
	 *******************************************************************************/
	public Response xubaoSearchByLicenseNo(Response response, String licenseNo, String licenseType) {
		XubaoSearchPage xubaoSearchPage = new XubaoSearchPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		//map.put("prpCrenewalVo.engineNo", "");
		//map.put("prpCrenewalVo.frameNo",  "");
		//map.put("prpCrenewalVo.licenseColorCode",  "");
		map.put("prpCrenewalVo.licenseNo", licenseNo);
		map.put("prpCrenewalVo.licenseType", licenseType);
		map.put("pageNo","1");
		map.put("pageSize","10");
		//map.put("prpCrenewalVo.othFlag",  "");
		//map.put("prpCrenewalVo.policyNo",  "");
		//map.put("prpCrenewalVo.vinNo",  "");

		//test add
		//map.put("jumpToPage","1");
		//map.put("ipolicyNo", "PDAT20141102T000135272");
		//map.put("ipolicyNo", "PDAT20151102T000182528");
		//map.put("ipolicyNo", "PDZA20151102T000186793");
		//map.put("ipolicyNo", "PDZA20141102T000140848");
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_SELECTRENEWAL);// POST
		Response responseSearch = xubaoSearchPage.run(request);
		return responseSearch;
	}


	/*******************************************************************************
	 * 按保单号浏览保单
	 *
	 * @param response
	 * @return
	 *******************************************************************************/
	public Response xubaoBrowsePolicyNo(Response response) {
		XubaoBrowsePolicyPage xubaoBrowsePolicyPage = new XubaoBrowsePolicyPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();

		Map nextParamsMap = (Map) responseParam.get("nextParams");
		Map lastResultMap = (Map) responseParam.get("lastResult");
		String licenseNo = lastResultMap.get("LicenseNo").toString() ;
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号

		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_BROWSEPOLICYNO);// GET
		Response responseBrowse = xubaoBrowsePolicyPage.run(request);
		//将续保信息写入系统内存中
		Map<String, String> sysMap = new HashMap<String, String>();
		Map responseSysParam = responseBrowse.getResponseMap();
		Map nextSysParamsMap = (Map) responseSysParam.get("nextParams");
		sysMap.put("bizNo", nextSysParamsMap.get("bizNo").toString());// 上年商业保单号
		sysMap.put("bizType", nextSysParamsMap.get("bizType").toString());
		sysMap.put("comCode", nextSysParamsMap.get("comCode").toString());
		sysMap.put("contractNo", nextSysParamsMap.get("contractNo").toString());
		sysMap.put("editType", nextSysParamsMap.get("editType").toString());
		sysMap.put("minusFlag", "");
		sysMap.put("proposalNo", nextSysParamsMap.get("proposalNo").toString());
		sysMap.put("riskCode", nextSysParamsMap.get("riskCode").toString());
		sysMap.put("rnd704", "");


		SysXubaoParamsMap.put(licenseNo, sysMap);



		return responseBrowse;
	}

	/*******************************************************************************
	 * 查询车辆基本信息
	 *
	 * @param response
	 * @return
	 *******************************************************************************/
	public Response xubaoGetCitemCar(Response response) {
		XubaoShowCitemCarPage xubaoShowCitemCarPage = new XubaoShowCitemCarPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", nextParamsMap.get("bizType").toString());
		map.put("comCode", nextParamsMap.get("comCode").toString());
		map.put("contractNo", nextParamsMap.get("contractNo").toString());
		map.put("editType", nextParamsMap.get("editType").toString());
		map.put("minusFlag", "");
		map.put("proposalNo", nextParamsMap.get("proposalNo").toString());
		map.put("riskCode", nextParamsMap.get("riskCode").toString());
		map.put("rnd704", "");
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);// GET
		Response responseShowCitemCar = xubaoShowCitemCarPage.run(request);
		return responseShowCitemCar;
	}

	/*******************************************************************************
	 * 查询车辆人员关系信息
	 *
	 * @param response
	 * @return
	 *******************************************************************************/
	public Response xubaoGetCinsure(Response response) {
		XubaoShowCinsuredPage xubaoShowCinsuredPage = new XubaoShowCinsuredPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", nextParamsMap.get("bizType").toString());
		map.put("comCode", nextParamsMap.get("comCode").toString());
		map.put("contractNo", nextParamsMap.get("contractNo").toString());
		map.put("editType", nextParamsMap.get("editType").toString());
		map.put("minusFlag", "");
		map.put("proposalNo", nextParamsMap.get("proposalNo").toString());
		map.put("riskCode", nextParamsMap.get("riskCode").toString());
		map.put("rnd704", "");
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_INSUREDTAB);// GET
		Response responseShowCinsured = xubaoShowCinsuredPage.run(request);
		return responseShowCinsured;
	}

	/*******************************************************************************
	 * 获取车辆保险责任
	 *
	 * @param response
	 * @return
	 *******************************************************************************/
	public Response xubaoGetCitemKind(Response response) {
		XubaoShowCitemKindPage xubaoShowCitemKindPage = new XubaoShowCitemKindPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", nextParamsMap.get("bizType").toString());
		map.put("comCode", nextParamsMap.get("comCode").toString());
		map.put("contractNo", nextParamsMap.get("contractNo").toString());
		map.put("editType", nextParamsMap.get("editType").toString());
		map.put("minusFlag", "");
		map.put("proposalNo", nextParamsMap.get("proposalNo").toString());
		map.put("riskCode", nextParamsMap.get("riskCode").toString());
		map.put("rnd704", "");
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_KINDTAB);// GET
		Response responseShowCitemKind = xubaoShowCitemKindPage.run(request);
		return responseShowCitemKind;
	}

	/*******************************************************************************
	 * 查询出险理赔信息
	 *
	 * @param response
	 * @return response PayCompanyName;//保险公司 PayAmount;//出险金额
	 *         EndCaseTime;//结案时间 LossTime;//出险时间
	 *******************************************************************************/
	public Response xubaoQueryClaimsMsg(Response response) {
		XubaoClaimsMsgPage xubaoClaimsMsgPage = new XubaoClaimsMsgPage(1);
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", nextParamsMap.get("bizType").toString());
		map.put("comCode", nextParamsMap.get("comCode").toString());
		map.put("contractNo", nextParamsMap.get("contractNo").toString());
		map.put("editType", nextParamsMap.get("editType").toString());
		map.put("minusFlag", "");
		map.put("proposalNo", nextParamsMap.get("proposalNo").toString());
		map.put("riskCode", nextParamsMap.get("riskCode").toString());
		map.put("rnd704", "");
		request.setRequestParam(map);
		//request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_QUERYCLAIMSMSG);// 查看理赔信息
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_KINDTAB);// 从险种信息中查看上一年出险信息
		Response responseClaimMsg = xubaoClaimsMsgPage.run(request);
		return responseClaimMsg;
	}

	@Override
	public String commitHeBaoInfo(Map preMap) {
		String code="";
		Response response = new Response();
		HebaoCalAnciInfoPage hebaoCalAnciInfoPage = new HebaoCalAnciInfoPage(1);
		Request request = new Request();
		request.setRequestParam(preMap);//
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CALANCIINFO);// GET
		Response responseHebaoCalAnciInfo = hebaoCalAnciInfoPage.run(request);
	    if(responseHebaoCalAnciInfo.getReturnCode()==SysConfigInfo.SUCCESS200){
			Map nextParamsMap = (Map)responseHebaoCalAnciInfo.getResponseMap().get("nextParams");
	        //保存1操作
			HebaoSaveCheckEngageTimePage hebaoSaveCheckEngageTimePage = new HebaoSaveCheckEngageTimePage(1);
			Request request1 =new Request();
			request1.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE1);
			request1.setRequestParam(nextParamsMap);
			Response response1 = hebaoSaveCheckEngageTimePage.run(request1);
			//保存2操作
			HeBaoSaveCheckAgentTypePage heBaoSaveCheckAgentTypePage = new HeBaoSaveCheckAgentTypePage(1);
			Request request2 =new Request();
			request2.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE2);
			request2.setRequestParam(nextParamsMap);
			Response response2 = heBaoSaveCheckAgentTypePage.run(request2);
			//保存3操作
			HebaoSaveQueryPayForPage hebaoSaveQueryPayForPage = new HebaoSaveQueryPayForPage(1);
			Request request3 =new Request();
			request3.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE3);
			request3.setRequestParam(nextParamsMap);
			Response response3 = hebaoSaveQueryPayForPage.run(request3);
	    }else{
		   logger.info("机器人抓取，获取辅助计算核保参数失败");
	    }
		return code;
	}
}
