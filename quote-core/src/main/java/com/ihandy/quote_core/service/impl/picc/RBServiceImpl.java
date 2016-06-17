package com.ihandy.quote_core.service.impl.picc;

import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.*;
import com.ihandy.quote_core.bean.other.*;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.bean.other.RelaPeopleResponse;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;
import com.ihandy.quote_core.serverpage.picc.*;
import com.ihandy.quote_core.service.IService;
import com.ihandy.quote_core.utils.CacheConstant;
import com.ihandy.quote_core.utils.SysConfigInfo;

import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
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
						String BusinessExpireDate = String.valueOf(lastResultSearchMap.get("BusinessExpireDate"));
						if(StringUtils.isNoneBlank(BusinessExpireDate) && !"null".equals(BusinessExpireDate)){
							carBaseInfoResponse.setBusinessExpireDate(BusinessExpireDate);//商业险到期日期
						}
						String ForceExpireDate = String.valueOf(lastResultSearchMap.get("ForceExpireDate"));
						if(StringUtils.isNoneBlank(ForceExpireDate) && !"null".equals(ForceExpireDate)){
							carBaseInfoResponse.setForceExpireDate(ForceExpireDate);//交强险到期日期
						}
						String biPolicyNo = String.valueOf(lastResultSearchMap.get("biPolicyNo"));
						if(StringUtils.isNoneBlank(biPolicyNo) && !"null".equals(biPolicyNo)){
							carBaseInfoResponse.setBiPolicyNo(biPolicyNo);
						}
						String ciPolicyNo = String.valueOf(lastResultSearchMap.get("ciPolicyNo"));
						if(StringUtils.isNoneBlank(ciPolicyNo) && !"null".equals(ciPolicyNo)){
							carBaseInfoResponse.setCiPolicyNo(ciPolicyNo);
						}
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
	public String commitHeBaoInfo(Response resp) {
		String code="";
		String DAAno="";
		String DZAno="";
		Response response = new Response();
		HebaoCalAnciInfoPage hebaoCalAnciInfoPage = new HebaoCalAnciInfoPage(1);
		Request request = new Request();
		String limingparam = "carShipTaxPlatFormFlag=&randomProposalNo=6987058901464069287702 &initemKind_Flag=1&editType=NEW&bizType=PROPOSAL&ABflag=&isBICI=&prpCmain.renewalFlag=&activityFlag=0&INTEGRAL_SWITCH=0&GuangdongSysFlag=&GDREALTIMECARFlag=&GDREALTIMEMOTORFlag=&GDCANCIINFOFlag=0&prpCmain.checkFlag=&prpCmain.othFlag=&prpCmain.dmFlag=&prpCmainCI.dmFlag=&prpCmain.underWriteCode=&prpCmain.underWriteName=&prpCmain.underWriteEndDate=&prpCmain.underWriteFlag=0&prpCmainCI.checkFlag=&prpCmainCI.underWriteFlag=&bizNo=&applyNo=&oldPolicyNo=&bizNoBZ=&bizNoCI=&prpPhead.endorDate=&prpPhead.validDate=&prpPhead.comCode=&sumAmountBI=&isTaxDemand=1&cIInsureFlag=1&bIInsureFlag=1&ciInsureSwitchKindCode=E01,E11,E12,D01,D02,D03&ciInsureSwitchValues=1111111&cIInsureMotorFlag=1&mtPlatformTime=&noPermissionsCarKindCode=E12&isTaxFlag=1&rePolicyNo=&oldPolicyType=&ZGRS_PURCHASEPRICE=200000&ZGRS_LOWESTPREMIUM=0&clauseFlag=&prpCinsuredOwn_Flag=0&prpCinsuredDiv_Flag=0&prpCinsuredBon_Flag=0&relationType=&ciLimitDays=90&udFlag=0&kbFlag=0&sbFlag=0&xzFlag=0&userType=08&noNcheckFlag=0&planFlag=0&R_SWITCH=1&biStartDate=2016-06-13&ciStartDate=2016-08-24&ciStartHour=0&ciEndDate=2017-08-23&ciEndHour=24&AGENTSWITCH=1&JFCDSWITCH=19&carShipTaxFlag=11&commissionFlag=&ICCardCHeck=&riskWarningFlag=&comCodePrefix=11&DAGMobilePhoneNum=&scanSwitch=1000000000&haveScanFlag=0&diffDay=90&cylinderFlag=0&ciPlateVersion=&biPlateVersion=&criterionFlag=0&isQuotatonFlag=2&quotationRisk=DAA&getReplenishfactor=&useYear=9&FREEINSURANCEFLAG=011111&isMotoDrunkDriv=0&immediateFlag=0&immediateFlagCI=0&claimAmountReason=&isQueryCarModelFlag=&isDirectFee=&userCode=020083&comCode=11010286&chgProfitFlag=00&ciPlatTask=&biPlatTask=&upperCostRateBI=&upperCostRateCI=&rescueFundRate=0.01&resureFundFee=5.65&useCarshiptaxFlag=1&taxFreeLicenseNo=&isTaxFree=0&premiumChangeFlag=&operationTimeStamp=2016-06-12 11:04:43&VEHICLEPLAT=&MOTORFASTTRACK=&motorFastTrack_flag=&MOTORFASTTRACK_INSUREDCODE=&currentDate=2016-06-12 11:04:43&vinModifyFlag=&addPolicyProjectCode=&isAddPolicy=0&commissionView=0&specialflag=&accountCheck=2&projectBak=&projectCodeBT=&projectCodeBTback=&checkTimeFlag=&checkUndwrt=0&carDamagedNum=&insurePayTimes=&claimAdjustValue=&operatorProjectCode=1-1326,2-1326,4-1326,5-1326&lossFlagKind=&chooseFlagCI=1&unitedSaleRelatioStr=&purchasePriceU=&countryNatureU=&insurancefee_reform=0&operateDateForFG=&prpCmainCommon.clauseIssue=1&amountFloat=30&vat_switch=1&BiLastPolicyFlag=&CiLastPolicyFlag=&CiLastEffectiveDate=&CiLastExpireDate=&benchMarkPremium=&BiLastEffectiveDate=&BiLastExpireDate=&lastTotalPremium=&purchasePriceUFlag=&startDateU=&endDateU=&biCiFlagU=&biCiFlagIsChange=&biCiDateIsChange=&switchFlag=0&relatedFlag=0&riskCode=DAA&prpCmain.riskCode=&riskName=&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&agentCodeValidType=U&agentCodeValidValue=106023BJ&agentCodeValidIPPer=&qualificationNo=201951000000800&qualificationName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&OLD_STARTDATE_CI=&OLD_ENDDATE_CI=&prpCmainCommon.greyList=&prpCmainCommon.image=&reinComPany=&reinPolicyNo=&reinStartDate=&reinEndDate=&prpCmain.proposalNo=&prpCmain.policyNo=&prpCmainCI.proposalNo=&prpCmainCI.policyNo=&prpPhead.applyNo=&prpPhead.endorseNo=&prpPheadCI.applyNo=&prpPheadCI.endorseNo=&prpCmain.comCode=11010286&comCodeDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.handler1Code=13154215  &handler1CodeDes=%BA%AB%B6%AB%D0%F1&homePhone=15801381299&officePhone=15801381299&moblie=&checkHandler1Code=1&handler1CodeDesFlag=A&handler1Info=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR__FIELD_SEPARATOR_A_FIELD_SEPARATOR_1211010268&prpCmainCommon.handler1code_uni=1211010268&prpCmain.handlerCode=13154215  &handlerCodeDes=%BA%AB%B6%AB%D0%F1&homePhonebak=&officePhonebak=&mobliebak=&handler1CodeDesFlagbak=&prpCmainCommon.handlercode_uni=1211010268&handlerInfo=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR_1211010268&prpCmain.businessNature=2&businessNatureTranslation=%D7%A8%D2%B5%B4%FA%C0%ED%D2%B5%CE%F1&prpCmain.agentCode=110021100065&prpCmainagentName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&agentType=211047&agentCode=110021100065&tempAgentCode=211047&sumPremiumChgFlag=1&prpCmain.sumPremium1=565.3&sumPayTax1=400.00&prpCmain.contractNo=&prpCmain.operateDate=2016-06-12 11:04:43&Today=2016-06-12 11:04:43&OperateDate=2016-06-12 11:04:43&prpCmain.makeCom=11010286&makeComDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.startDate=2016-06-13&prpCmain.startHour=0&prpCmain.endDate=2017-06-12&prpCmain.endHour=24&prpCmain.checkUpCode=&prpCmainCI.startDate=2016-08-24&prpCmainCI.startHour=0&prpCmainCI.endDate=2017-08-23&prpCmainCI.endHour=24&carPremium=0.0&insuredChangeFlag=0&refreshEadFlag=1&imageAdjustPixels=20&prpBatchVehicle.id.contractNo=&prpBatchVehicle.id.serialNo=&prpBatchVehicle.motorCadeNo=&prpBatchVehicle.licenseNo=&prpBatchVehicle.licenseType=&prpBatchVehicle.carKindCode=&prpBatchVehicle.proposalNo=&prpBatchVehicle.policyNo=&prpBatchVehicle.sumAmount=&prpBatchVehicle.sumPremium=&prpBatchVehicle.prpProjectCode=&prpBatchVehicle.coinsProjectCode=&prpBatchVehicle.profitProjectCode=&prpBatchVehicle.facProjectCode=&prpBatchVehicle.flag=&prpBatchVehicle.carId=&prpBatchVehicle.versionNo=&prpBatchMain.discountmode=&minusFlag=&paramIndex=&batchCIFlag=&batchBIFlag=&pageEndorRecorder.endorFlags=&endorDateEdit=&validDateEdit=&endDateEdit=&endorType=&prpPhead.endorType=&generatePtextFlag=0&generatePtextAgainFlag=0&quotationNo=&quotationFlag=&customerCode=&customerFlag=&compensateNo=&dilutiveType=&prpCfixationTemp.discount=&prpCfixationTemp.id.riskCode=&prpCfixationTemp.profits=&prpCfixationTemp.cost=&prpCfixationTemp.taxorAppend=&prpCfixationTemp.payMentR=&prpCfixationTemp.basePayMentR=&prpCfixationTemp.poundAge=&prpCfixationTemp.basePremium=&prpCfixationTemp.riskPremium=&prpCfixationTemp.riskSumPremium=&prpCfixationTemp.signPremium=&prpCfixationTemp.isQuotation=&prpCfixationTemp.riskClass=&prpCfixationTemp.operationInfo=&prpCfixationTemp.realDisCount=&prpCfixationTemp.realProfits=&prpCfixationTemp.realPayMentR=&prpCfixationTemp.remark=&prpCfixationTemp.responseCode=&prpCfixationTemp.errorMessage=&prpCfixationTemp.profitClass=&prpCfixationTemp.costRate=&prpCfixationCITemp.discount=&prpCfixationCITemp.id.riskCode=&prpCfixationCITemp.profits=&prpCfixationCITemp.cost=&prpCfixationCITemp.taxorAppend=&prpCfixationCITemp.payMentR=&prpCfixationCITemp.basePayMentR=&prpCfixationCITemp.poundAge=&prpCfixationCITemp.basePremium=&prpCfixationCITemp.riskPremium=&prpCfixationCITemp.riskSumPremium=&prpCfixationCITemp.signPremium=&prpCfixationCITemp.isQuotation=&prpCfixationCITemp.riskClass=&prpCfixationCITemp.operationInfo=&prpCfixationCITemp.realDisCount=&prpCfixationCITemp.realProfits=&prpCfixationCITemp.realPayMentR=&prpCfixationCITemp.remark=&prpCfixationCITemp.responseCode=&prpCfixationCITemp.errorMessage=&prpCfixationCITemp.profitClass=&prpCfixationCITemp.costRate=&prpCsalesFixes_%5B0%5D.id.proposalNo=&prpCsalesFixes_%5B0%5D.id.serialNo=&prpCsalesFixes_%5B0%5D.comCode=&prpCsalesFixes_%5B0%5D.businessNature=&prpCsalesFixes_%5B0%5D.riskCode=&prpCsalesFixes_%5B0%5D.version=&prpCsalesFixes_%5B0%5D.isForMal=&IS_LOAN_MODIFY=0&kindAndAmount=&isSpecialFlag=&specialEngage=&licenseNoCar=&prpCitemCar.carLoanFlag=&carModelPlatFlag=&updateQuotation=&prpCitemCar.licenseNo1=&prpCitemCar.monopolyFlag=0&prpCitemCar.monopolyCode=&prpCitemCar.monopolyName=&queryCarModelInfo=%B3%B5%D0%CD%D0%C5%CF%A2%C6%BD%CC%A8%BD%BB%BB%A5&prpCitemCar.id.itemNo=1&oldClauseType=F42&prpCitemCar.actualValue=125031.80&prpCitemCar.carId=&prpCitemCar.versionNo=&prpCmainCar.newDeviceFlag=&prpCitemCar.otherNature=&prpCitemCar.flag=&newCarFlagValue=2&prpCitemCar.discountType=&prpCitemCar.colorCode=&prpCitemCar.safeDevice=&prpCitemCar.coefficient1=&prpCitemCar.coefficient2=&prpCitemCar.coefficient3=&prpCitemCar.startSiteName=&prpCitemCar.endSiteName=&prpCmainCommon.netsales=0&prpCitemCar.newCarFlag=0&prpCitemCar.noNlocalFlag=0&prpCitemCar.licenseFlag=1&prpCitemCar.licenseNo=%BE%A9Q8D075&codeLicenseType=LicenseType01,04,LicenseType02,01,LicenseType03,02,LicenseType04,02,LicenseType05,02,LicenseType06,02,LicenseType07,04,LicenseType08,04,LicenseType09,01,LicenseType10,01,LicenseType11,01,LicenseType12,01,LicenseType13,04,LicenseType14,04,LicenseType15,04,	LicenseType16,04,LicenseType17,04,LicenseType18,01,LicenseType19,01,LicenseType20,01,LicenseType21,01,LicenseType22,01,LicenseType23,03,LicenseType24,01,LicenseType25,01,LicenseType31,03,LicenseType32,03,LicenseType90,02&prpCitemCar.licenseType=02&LicenseTypeDes=%D0%A1%D0%CD%C6%FB%B3%B5%BA%C5%C5%C6&prpCitemCar.licenseColorCode=01&LicenseColorCodeDes=%C0%B6&prpCitemCar.engineNo=131680128&prpCitemCar.vinNo=LSGGA54Y4DH157325&prpCitemCar.frameNo=LSGGA54Y4DH157325&prpCitemCar.carKindCode=A01&CarKindCodeDes=%BF%CD%B3%B5&carKindCodeBak=A01&prpCitemCar.useNatureCode=211&useNatureCodeBak=211&useNatureCodeTrue=211&prpCitemCar.clauseType=F42&clauseTypeBak=F42&prpCitemCar.enrollDate=2013-08-23&enrollDateTrue=&prpCitemCar.useYears=2&prpCitemCar.runMiles=10000&taxAbateForPlat=&taxAbateForPlatCarModel=&prpCitemCar.modelDemandNo=39PICC02160000000000908482014D&owner=%CC%C6%C0%F6%D3%B1&prpCitemCar.remark=&prpCitemCar.modelCode=BKAAMD0036&prpCitemCar.brandName=%B1%F0%BF%CBSGM7206ATA%BD%CE%B3%B5&PurchasePriceScal=10&prpCitemCar.purchasePrice=155900&CarActualValueTrue=155900&CarActualValueTrue1=&SZpurchasePriceUp=&SZpurchasePriceDown=155900&purchasePriceF48=200000&purchasePriceUp=100&purchasePriceDown=155900&purchasePriceOld=155900&vehiclePricer=&prpCitemCar.tonCount=0&prpCitemCar.exhaustScale=1.998&prpCitemCar.seatCount=5&seatCountTrue=&prpCitemCar.runAreaCode=11&prpCitemCar.carInsuredRelation=1&prpCitemCar.countryNature=02&prpCitemCar.cylinderCount=&prpCitemCar.loanVehicleFlag=0&prpCitemCar.transferVehicleFlag=0&prpCitemCar.transferDate=&prpCitemCar.modelCodeAlias=%B1%F0%BF%CB%C5%C6SGM7206ATA&prpCitemCar.carLotEquQuality=0.00&isQuotation=1&prpCitemCar.fuelType=A&prpCitemCar.carProofType=01&prpCitemCar.isDropinVisitInsure=0&prpCitemCar.energyType=0&prpCitemCar.carProofNo=&prpCitemCar.carProofDate=&prpCmainChannel.assetAgentName=&prpCmainChannel.assetAgentCode=&prpCmainChannel.assetAgentPhone=&SYFlag=0&MTFlag=0&BMFlag=0&STFlag=0&prpCcarDevices_%5B0%5D.deviceName=&prpCcarDevices_%5B0%5D.id.itemNo=1&prpCcarDevices_%5B0%5D.id.proposalNo=&prpCcarDevices_%5B0%5D.id.serialNo=&prpCcarDevices_%5B0%5D.flag=&prpCcarDevices_%5B0%5D.quantity=&prpCcarDevices_%5B0%5D.purchasePrice=&prpCcarDevices_%5B0%5D.buyDate=&prpCcarDevices_%5B0%5D.actualValue=&hidden_index_citemcar=0&editFlag=1&prpCmainCommon.ext2=&configedRepeatTimesLocal=5&prpCinsureds_%5B0%5D.insuredFlag=1&iinsuredFlag=%CD%B6%B1%A3%C8%CB/%B1%BB%B1%A3%CF%D5%C8%CB&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=320684198411040279&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds_%5B0%5D.id.serialNo=1&prpCinsureds_%5B0%5D.insuredType=1&prpCinsureds_%5B0%5D.insuredNature=1&prpCinsureds_%5B0%5D.insuredCode=1100100001900281&prpCinsureds_%5B0%5D.insuredName=%D6%EC%BC%D1%BC%D1&prpCinsureds_%5B0%5D.unitType=&prpCinsureds_%5B0%5D.identifyType=01&prpCinsureds_%5B0%5D.identifyNumber=320684198411040279&prpCinsureds_%5B0%5D.insuredAddress=&prpCinsureds_%5B0%5D.email=&prpCinsureds_%5B0%5D.phoneNumber=&prpCinsureds_%5B0%5D.drivingYears=&prpCinsureds_%5B0%5D.mobile=18610713420&prpCinsureds_%5B0%5D.postCode=&prpCinsureds_%5B0%5D.versionNo=2&prpCinsureds_%5B0%5D.auditStatus=2&prpCinsureds_%5B0%5D.sex=1&prpCinsureds_%5B0%5D.countryCode=CHN&prpCinsureds_%5B0%5D.flag=&prpCinsureds_%5B0%5D.age=32&prpCinsureds_%5B0%5D.drivingLicenseNo=320684198411040279&prpCinsureds_%5B0%5D.drivingCarType=&prpCinsureds_%5B0%5D.appendPrintName=&prpCinsureds_%5B0%5D.causetroubleTimes=&prpCinsureds_%5B0%5D.acceptLicenseDate=&isCheckRepeat_%5B0%5D=&configedRepeatTimes_%5B0%5D=&repeatTimes_%5B0%5D=&prpCinsureds_%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo_%5B0%5D.insuredcode=&idCardCheckInfo_%5B0%5D.insuredFlag=&idCardCheckInfo_%5B0%5D.mobile=&idCardCheckInfo_%5B0%5D.idcardCode=&idCardCheckInfo_%5B0%5D.name=&idCardCheckInfo_%5B0%5D.nation=&idCardCheckInfo_%5B0%5D.birthday=&idCardCheckInfo_%5B0%5D.sex=&idCardCheckInfo_%5B0%5D.address=&idCardCheckInfo_%5B0%5D.issure=&idCardCheckInfo_%5B0%5D.validStartDate=&idCardCheckInfo_%5B0%5D.validEndDate=&idCardCheckInfo_%5B0%5D.samCode=&idCardCheckInfo_%5B0%5D.samType=&idCardCheckInfo_%5B0%5D.flag=0&imobile=186****3420&iauditStatus=2&iversionNo=2&hidden_index_insured=0&prpCinsureds%5B0%5D.insuredFlag=001000000000000000000000000000&iinsuredFlag=%B3%B5%D6%F7&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=%D6%EC%BC%D1%BC%D1&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds%5B0%5D.id.serialNo=1&prpCinsureds%5B0%5D.insuredType=1&prpCinsureds%5B0%5D.insuredNature=1&prpCinsureds%5B0%5D.insuredCode=1100100001900281&prpCinsureds%5B0%5D.insuredName=%CC%C6%C0%F6%D3%B1&prpCinsureds%5B0%5D.unitType=&prpCinsureds%5B0%5D.identifyType=01&prpCinsureds%5B0%5D.identifyNumber=110105197612134822&prpCinsureds%5B0%5D.insuredAddress=%B1%B1%BE%A9&prpCinsureds%5B0%5D.email=&prpCinsureds%5B0%5D.phoneNumber=null&prpCinsureds%5B0%5D.drivingYears=&prpCinsureds%5B0%5D.mobile=null&prpCinsureds%5B0%5D.postCode=&prpCinsureds%5B0%5D.versionNo=2&prpCinsureds%5B0%5D.auditStatus=2&prpCinsureds%5B0%5D.sex=&prpCinsureds%5B0%5D.countryCode=CHN&prpCinsureds%5B0%5D.flag=&prpCinsureds%5B0%5D.age=&prpCinsureds%5B0%5D.drivingLicenseNo=&prpCinsureds%5B0%5D.drivingCarType=&prpCinsureds%5B0%5D.appendPrintName=&prpCinsureds%5B0%5D.causetroubleTimes=&prpCinsureds%5B0%5D.acceptLicenseDate=&isCheckRepeat%5B0%5D=&configedRepeatTimes%5B0%5D=&repeatTimes%5B0%5D=&prpCinsureds%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B0%5D.insuredcode=&idCardCheckInfo%5B0%5D.insuredFlag=&idCardCheckInfo%5B0%5D.mobile=&idCardCheckInfo%5B0%5D.idcardCode=&idCardCheckInfo%5B0%5D.name=&idCardCheckInfo%5B0%5D.nation=&idCardCheckInfo%5B0%5D.birthday=&idCardCheckInfo%5B0%5D.sex=&idCardCheckInfo%5B0%5D.address=&idCardCheckInfo%5B0%5D.issure=&idCardCheckInfo%5B0%5D.validStartDate=&idCardCheckInfo%5B0%5D.validEndDate=&idCardCheckInfo%5B0%5D.samCode=&idCardCheckInfo%5B0%5D.samType=&idCardCheckInfo%5B0%5D.flag=&imobile=186****3420&iauditStatus=2&iversionNo=2&prpCinsureds%5B1%5D.insuredFlag=11000000000000000000000000000A&iinsuredFlag=%CD%B6%B1%A3%C8%CB/%B1%BB%B1%A3%CF%D5%C8%CB&iinsuredType=%B8%F6%C8%CB&iinsuredCode=1100100001900281&iinsuredName=&iunitType=&iidentifyType=%C9%ED%B7%DD%D6%A4&iidentifyNumber=&iinsuredAddress=&iemail=&iphoneNumber=&prpCinsureds%5B1%5D.id.serialNo=1&prpCinsureds%5B1%5D.insuredType=1&prpCinsureds%5B1%5D.insuredNature=1&prpCinsureds%5B1%5D.insuredCode=1100100001900281&prpCinsureds%5B1%5D.insuredName=%C0%EE%C3%F7&prpCinsureds%5B1%5D.unitType=&prpCinsureds%5B1%5D.identifyType=01&prpCinsureds%5B1%5D.identifyNumber=239005198802252314&prpCinsureds%5B1%5D.insuredAddress=%B1%B1%BE%A9&prpCinsureds%5B1%5D.email=&prpCinsureds%5B1%5D.phoneNumber=13520030193&prpCinsureds%5B1%5D.drivingYears=&prpCinsureds%5B1%5D.mobile=13520030193&prpCinsureds%5B1%5D.postCode=&prpCinsureds%5B1%5D.versionNo=2&prpCinsureds%5B1%5D.auditStatus=2&prpCinsureds%5B1%5D.sex=&prpCinsureds%5B1%5D.countryCode=CHN&prpCinsureds%5B1%5D.flag=&prpCinsureds%5B1%5D.age=&prpCinsureds%5B1%5D.drivingLicenseNo=&prpCinsureds%5B1%5D.drivingCarType=&prpCinsureds%5B1%5D.appendPrintName=&prpCinsureds%5B1%5D.causetroubleTimes=&prpCinsureds%5B1%5D.acceptLicenseDate=&isCheckRepeat%5B1%5D=&configedRepeatTimes%5B1%5D=&repeatTimes%5B1%5D=&prpCinsureds%5B1%5D.unifiedSocialCreditCode=&idCardCheckInfo%5B1%5D.insuredcode=&idCardCheckInfo%5B1%5D.insuredFlag=&idCardCheckInfo%5B1%5D.mobile=&idCardCheckInfo%5B1%5D.idcardCode=&idCardCheckInfo%5B1%5D.name=&idCardCheckInfo%5B1%5D.nation=&idCardCheckInfo%5B1%5D.birthday=&idCardCheckInfo%5B1%5D.sex=&idCardCheckInfo%5B1%5D.address=&idCardCheckInfo%5B1%5D.issure=&idCardCheckInfo%5B1%5D.validStartDate=&idCardCheckInfo%5B1%5D.validEndDate=&idCardCheckInfo%5B1%5D.samCode=&idCardCheckInfo%5B1%5D.samType=&idCardCheckInfo%5B1%5D.flag=&imobile=186****3420&iauditStatus=2&iversionNo=2&_insuredFlag_hide=%CD%B6%B1%A3%C8%CB&_insuredFlag_hide=%B1%BB%B1%A3%CF%D5%C8%CB&_insuredFlag_hide=%B3%B5%D6%F7&_insuredFlag_hide=%D6%B8%B6%A8%BC%DD%CA%BB%C8%CB&_insuredFlag_hide=%CA%DC%D2%E6%C8%CB&_insuredFlag_hide=%B8%DB%B0%C4%B3%B5%B3%B5%D6%F7&_insuredFlag_hide=%C1%AA%CF%B5%C8%CB&_insuredFlag=0&_insuredFlag_hide=%CE%AF%CD%D0%C8%CB&_resident=&_insuredType=1&_insuredCode=&_insuredName=&customerURL=http://10.134.136.48:8300/cif&_isCheckRepeat=&_configedRepeatTimes=&_repeatTimes=&_identifyType=01&_identifyNumber=&_unifiedSocialCreditCode=&_mobile=&_mobile1=&_sex=1&_age=&_drivingYears=&_countryCode=CHN&_insuredAddress=&_postCode=&_appendPrintName=&group_code=&_auditStatus=&_auditStatusDes=&_versionNo=&_drivingLicenseNo=&_email=&idCardCheckInfo.idcardCode=&idCardCheckInfo.name=&idCardCheckInfo.nation=&idCardCheckInfo.birthday=&idCardCheckInfo.sex=&idCardCheckInfo.address=&idCardCheckInfo.issure=&idCardCheckInfo.validStartDate=&idCardCheckInfo.validEndDate=&idCardCheckInfo.samCode=&idCardCheckInfo.samType=&idCardCheckInfo.flag=0&_drivingCarType=&CarKindLicense=&_causetroubleTimes=&_acceptLicenseDate=&prpCmainCar.agreeDriverFlag=&updateIndex=-1&prpBatchProposal.profitType=&motorFastTrack_Amount=&insurancefee_reform=0&prpCmainCommon.clauseIssue=1&prpCprofitDetailsTemp_%5B0%5D.chooseFlag=&prpCprofitDetailsTemp_%5B0%5D.profitName=&prpCprofitDetailsTemp_%5B0%5D.condition=&profitRateTemp_%5B0%5D=&prpCprofitDetailsTemp_%5B0%5D.profitRate=&prpCprofitDetailsTemp_%5B0%5D.profitRateMin=&prpCprofitDetailsTemp_%5B0%5D.profitRateMax=&prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=&prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=&prpCprofitDetailsTemp_%5B0%5D.id.profitCode=&prpCprofitDetailsTemp_%5B0%5D.id.serialNo=1&prpCprofitDetailsTemp_%5B0%5D.id.profitType=&prpCprofitDetailsTemp_%5B0%5D.kindCode=&prpCprofitDetailsTemp_%5B0%5D.conditionCode=&prpCprofitDetailsTemp_%5B0%5D.flag=&prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on&serialNo_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.profitName=&prpCprofitFactorsTemp_%5B0%5D.condition=&rateTemp_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.rate=&prpCprofitFactorsTemp_%5B0%5D.lowerRate=&prpCprofitFactorsTemp_%5B0%5D.upperRate=&prpCprofitFactorsTemp_%5B0%5D.id.profitCode=&prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=&prpCprofitFactorsTemp_%5B0%5D.flag=&prpCitemKind.shortRateFlag=2&prpCitemKind.shortRate=100&prpCitemKind.currency=CNY&prpCmainCommon.groupFlag=0&sumBenchPremium=&prpCmain.discount=&prpCmain.sumPremium=&premiumF48=5000&prpCmain.sumNetPremium=&prpCmain.sumTaxPremium=&passengersSwitchFlag=&prpCitemKindsTemp%5B0%5D.min=&prpCitemKindsTemp%5B0%5D.max=&prpCitemKindsTemp%5B0%5D.chooseFlag=&prpCitemKindsTemp%5B0%5D.itemKindNo=&prpCitemKindsTemp%5B0%5D.clauseCode=050002&prpCitemKindsTemp%5B0%5D.kindCode=050200&prpCitemKindsTemp%5B0%5D.kindName=%BB%FA%B6%AF%B3%B5%CB%F0%CA%A7%B1%A3%CF%D5&prpCitemKindsTemp%5B0%5D.unitAmount=&prpCitemKindsTemp%5B0%5D.quantity=&prpCitemKindsTemp%5B0%5D.specialFlag=on&prpCitemKindsTemp%5B0%5D.amount=&prpCitemKindsTemp%5B0%5D.calculateFlag=Y11Y000&prpCitemKindsTemp%5B0%5D.startDate=&prpCitemKindsTemp%5B0%5D.startHour=&prpCitemKindsTemp%5B0%5D.endDate=&prpCitemKindsTemp%5B0%5D.endHour=&relateSpecial%5B0%5D=050911&coachCar%5B0%5D=050941&prpCitemKindsTemp%5B0%5D.flag= 100000&prpCitemKindsTemp%5B0%5D.basePremium=&prpCitemKindsTemp%5B0%5D.rate=&prpCitemKindsTemp%5B0%5D.benchMarkPremium=&prpCitemKindsTemp%5B0%5D.disCount=&prpCitemKindsTemp%5B0%5D.premium=&prpCitemKindsTemp%5B0%5D.netPremium=&prpCitemKindsTemp%5B0%5D.taxPremium=&prpCitemKindsTemp%5B0%5D.taxRate=&prpCitemKindsTemp%5B0%5D.dutyFlag=&prpCitemKindsTemp%5B1%5D.min=&prpCitemKindsTemp%5B1%5D.max=&prpCitemKindsTemp%5B1%5D.chooseFlag=&prpCitemKindsTemp%5B1%5D.itemKindNo=&prpCitemKindsTemp%5B1%5D.clauseCode=050005&prpCitemKindsTemp%5B1%5D.kindCode=050500&prpCitemKindsTemp%5B1%5D.kindName=%B5%C1%C7%C0%CF%D5&prpCitemKindsTemp%5B1%5D.unitAmount=&prpCitemKindsTemp%5B1%5D.quantity=&prpCitemKindsTemp%5B1%5D.specialFlag=on&prpCitemKindsTemp%5B1%5D.amount=&prpCitemKindsTemp%5B1%5D.calculateFlag=N11Y000&prpCitemKindsTemp%5B1%5D.startDate=&prpCitemKindsTemp%5B1%5D.startHour=&prpCitemKindsTemp%5B1%5D.endDate=&prpCitemKindsTemp%5B1%5D.endHour=&relateSpecial%5B1%5D=050921&coachCar%5B1%5D=&prpCitemKindsTemp%5B1%5D.flag= 100000&prpCitemKindsTemp%5B1%5D.basePremium=&prpCitemKindsTemp%5B1%5D.rate=&prpCitemKindsTemp%5B1%5D.benchMarkPremium=&prpCitemKindsTemp%5B1%5D.disCount=&prpCitemKindsTemp%5B1%5D.premium=&prpCitemKindsTemp%5B1%5D.netPremium=&prpCitemKindsTemp%5B1%5D.taxPremium=&prpCitemKindsTemp%5B1%5D.taxRate=&prpCitemKindsTemp%5B1%5D.dutyFlag=&prpCitemKindsTemp%5B2%5D.min=&prpCitemKindsTemp%5B2%5D.max=&prpCitemKindsTemp%5B2%5D.chooseFlag=&prpCitemKindsTemp%5B2%5D.itemKindNo=&prpCitemKindsTemp%5B2%5D.clauseCode=050003&prpCitemKindsTemp%5B2%5D.kindCode=050600&prpCitemKindsTemp%5B2%5D.kindName=%B5%DA%C8%FD%D5%DF%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B2%5D.unitAmount=&prpCitemKindsTemp%5B2%5D.quantity=&prpCitemKindsTemp%5B2%5D.specialFlag=on&prpCitemKindsTemp%5B2%5D.amount=&prpCitemKindsTemp%5B2%5D.calculateFlag=Y21Y000&prpCitemKindsTemp%5B2%5D.startDate=&prpCitemKindsTemp%5B2%5D.startHour=&prpCitemKindsTemp%5B2%5D.endDate=&prpCitemKindsTemp%5B2%5D.endHour=&relateSpecial%5B2%5D=050912&coachCar%5B2%5D=050942&prpCitemKindsTemp%5B2%5D.flag= 100000&prpCitemKindsTemp%5B2%5D.basePremium=&prpCitemKindsTemp%5B2%5D.rate=&prpCitemKindsTemp%5B2%5D.benchMarkPremium=&prpCitemKindsTemp%5B2%5D.disCount=&prpCitemKindsTemp%5B2%5D.premium=&prpCitemKindsTemp%5B2%5D.netPremium=&prpCitemKindsTemp%5B2%5D.taxPremium=&prpCitemKindsTemp%5B2%5D.taxRate=&prpCitemKindsTemp%5B2%5D.dutyFlag=&prpCitemKindsTemp%5B3%5D.min=&prpCitemKindsTemp%5B3%5D.max=&prpCitemKindsTemp%5B3%5D.chooseFlag=&prpCitemKindsTemp%5B3%5D.itemKindNo=&prpCitemKindsTemp%5B3%5D.clauseCode=050004&prpCitemKindsTemp%5B3%5D.kindCode=050701&prpCitemKindsTemp%5B3%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9&prpCitemKindsTemp%5B3%5D.unitAmount=&prpCitemKindsTemp%5B3%5D.quantity=&prpCitemKindsTemp%5B3%5D.specialFlag=on&prpCitemKindsTemp%5B3%5D.amount=&prpCitemKindsTemp%5B3%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B3%5D.startDate=&prpCitemKindsTemp%5B3%5D.startHour=&prpCitemKindsTemp%5B3%5D.endDate=&prpCitemKindsTemp%5B3%5D.endHour=&relateSpecial%5B3%5D=050928&coachCar%5B3%5D=050943&prpCitemKindsTemp%5B3%5D.flag= 100000&prpCitemKindsTemp%5B3%5D.basePremium=&prpCitemKindsTemp%5B3%5D.rate=&prpCitemKindsTemp%5B3%5D.benchMarkPremium=&prpCitemKindsTemp%5B3%5D.disCount=&prpCitemKindsTemp%5B3%5D.premium=&prpCitemKindsTemp%5B3%5D.netPremium=&prpCitemKindsTemp%5B3%5D.taxPremium=&prpCitemKindsTemp%5B3%5D.taxRate=&prpCitemKindsTemp%5B3%5D.dutyFlag=&prpCitemKindsTemp%5B4%5D.min=&prpCitemKindsTemp%5B4%5D.max=&prpCitemKindsTemp%5B4%5D.chooseFlag=&prpCitemKindsTemp%5B4%5D.itemKindNo=&prpCitemKindsTemp%5B4%5D.clauseCode=050004&prpCitemKindsTemp%5B4%5D.kindCode=050702&prpCitemKindsTemp%5B4%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9&prpCitemKindsTemp%5B4%5D.unitAmount=&prpCitemKindsTemp%5B4%5D.quantity=&prpCitemKindsTemp%5B4%5D.specialFlag=on&prpCitemKindsTemp%5B4%5D.amount=&prpCitemKindsTemp%5B4%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B4%5D.startDate=&prpCitemKindsTemp%5B4%5D.startHour=&prpCitemKindsTemp%5B4%5D.endDate=&prpCitemKindsTemp%5B4%5D.endHour=&relateSpecial%5B4%5D=050929&coachCar%5B4%5D=050944&prpCitemKindsTemp%5B4%5D.flag= 100000&prpCitemKindsTemp%5B4%5D.basePremium=&prpCitemKindsTemp%5B4%5D.rate=&prpCitemKindsTemp%5B4%5D.benchMarkPremium=&prpCitemKindsTemp%5B4%5D.disCount=&prpCitemKindsTemp%5B4%5D.premium=&prpCitemKindsTemp%5B4%5D.netPremium=&prpCitemKindsTemp%5B4%5D.taxPremium=&prpCitemKindsTemp%5B4%5D.taxRate=&prpCitemKindsTemp%5B4%5D.dutyFlag=&prpCitemKindsTemp%5B5%5D.min=&prpCitemKindsTemp%5B5%5D.max=&prpCitemKindsTemp%5B5%5D.chooseFlag=&prpCitemKindsTemp%5B5%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B5%5D.clauseCode=050006&prpCitemKindsTemp%5B5%5D.kindCode=050210&relateSpecial%5B5%5D=050922&prpCitemKindsTemp%5B5%5D.kindName=%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B5%5D.specialFlag=on&prpCitemKindsTemp%5B5%5D.amount=&prpCitemKindsTemp%5B5%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B5%5D.startDate=&prpCitemKindsTemp%5B5%5D.startHour=&prpCitemKindsTemp%5B5%5D.endDate=&prpCitemKindsTemp%5B5%5D.endHour=&prpCitemKindsTemp%5B5%5D.flag= 200000&prpCitemKindsTemp%5B5%5D.basePremium=&prpCitemKindsTemp%5B5%5D.rate=&prpCitemKindsTemp%5B5%5D.benchMarkPremium=&prpCitemKindsTemp%5B5%5D.disCount=&prpCitemKindsTemp%5B5%5D.premium=&prpCitemKindsTemp%5B5%5D.netPremium=&prpCitemKindsTemp%5B5%5D.taxPremium=&prpCitemKindsTemp%5B5%5D.taxRate=&prpCitemKindsTemp%5B5%5D.dutyFlag=&prpCitemKindsTemp%5B6%5D.min=&prpCitemKindsTemp%5B6%5D.max=&prpCitemKindsTemp%5B6%5D.chooseFlag=&prpCitemKindsTemp%5B6%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B6%5D.clauseCode=050008&prpCitemKindsTemp%5B6%5D.kindCode=050231&relateSpecial%5B6%5D=      &prpCitemKindsTemp%5B6%5D.kindName=%B2%A3%C1%A7%B5%A5%B6%C0%C6%C6%CB%E9%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B6%5D.modeCode=&prpCitemKindsTemp%5B6%5D.amount=&prpCitemKindsTemp%5B6%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B6%5D.startDate=&prpCitemKindsTemp%5B6%5D.startHour=&prpCitemKindsTemp%5B6%5D.endDate=&prpCitemKindsTemp%5B6%5D.endHour=&prpCitemKindsTemp%5B6%5D.flag= 200000&prpCitemKindsTemp%5B6%5D.basePremium=&prpCitemKindsTemp%5B6%5D.rate=&prpCitemKindsTemp%5B6%5D.benchMarkPremium=&prpCitemKindsTemp%5B6%5D.disCount=&prpCitemKindsTemp%5B6%5D.premium=&prpCitemKindsTemp%5B6%5D.netPremium=&prpCitemKindsTemp%5B6%5D.taxPremium=&prpCitemKindsTemp%5B6%5D.taxRate=&prpCitemKindsTemp%5B6%5D.dutyFlag=&prpCitemKindsTemp%5B7%5D.min=&prpCitemKindsTemp%5B7%5D.max=&prpCitemKindsTemp%5B7%5D.chooseFlag=&prpCitemKindsTemp%5B7%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B7%5D.clauseCode=050016&prpCitemKindsTemp%5B7%5D.kindCode=050310&relateSpecial%5B7%5D=      &prpCitemKindsTemp%5B7%5D.kindName=%D7%D4%C8%BC%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B7%5D.amount=&prpCitemKindsTemp%5B7%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B7%5D.startDate=&prpCitemKindsTemp%5B7%5D.startHour=&prpCitemKindsTemp%5B7%5D.endDate=&prpCitemKindsTemp%5B7%5D.endHour=&prpCitemKindsTemp%5B7%5D.flag= 200000&prpCitemKindsTemp%5B7%5D.basePremium=&prpCitemKindsTemp%5B7%5D.rate=&prpCitemKindsTemp%5B7%5D.benchMarkPremium=&prpCitemKindsTemp%5B7%5D.disCount=&prpCitemKindsTemp%5B7%5D.premium=&prpCitemKindsTemp%5B7%5D.netPremium=&prpCitemKindsTemp%5B7%5D.taxPremium=&prpCitemKindsTemp%5B7%5D.taxRate=&prpCitemKindsTemp%5B7%5D.dutyFlag=&prpCitemKindsTemp%5B8%5D.min=&prpCitemKindsTemp%5B8%5D.max=&prpCitemKindsTemp%5B8%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B8%5D.clauseCode=050021&prpCitemKindsTemp%5B8%5D.kindCode=050370&relateSpecial%5B8%5D=      &prpCitemKindsTemp%5B8%5D.kindName=%D4%BC%B6%A8%C7%F8%D3%F2%CD%A8%D0%D0%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B8%5D.modeCode=1&prpCitemKindsTemp%5B8%5D.amount=5000.00&prpCitemKindsTemp%5B8%5D.calculateFlag=N12N000&prpCitemKindsTemp%5B8%5D.startDate=&prpCitemKindsTemp%5B8%5D.startHour=&prpCitemKindsTemp%5B8%5D.endDate=&prpCitemKindsTemp%5B8%5D.endHour=&prpCitemKindsTemp%5B8%5D.flag= 200000&prpCitemKindsTemp%5B8%5D.basePremium=&prpCitemKindsTemp%5B8%5D.rate=&prpCitemKindsTemp%5B8%5D.benchMarkPremium=&prpCitemKindsTemp%5B8%5D.disCount=&prpCitemKindsTemp%5B8%5D.premium=&prpCitemKindsTemp%5B8%5D.netPremium=&prpCitemKindsTemp%5B8%5D.taxPremium=&prpCitemKindsTemp%5B8%5D.taxRate=&prpCitemKindsTemp%5B8%5D.dutyFlag=&prpCitemKindsTemp%5B9%5D.min=&prpCitemKindsTemp%5B9%5D.max=&prpCitemKindsTemp%5B9%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B9%5D.clauseCode=050032&prpCitemKindsTemp%5B9%5D.kindCode=050611&relateSpecial%5B9%5D=      &prpCitemKindsTemp%5B9%5D.kindName=%B7%A8%C2%C9%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B9%5D.amount=10000.00&prpCitemKindsTemp%5B9%5D.calculateFlag=N22N000&prpCitemKindsTemp%5B9%5D.startDate=&prpCitemKindsTemp%5B9%5D.startHour=&prpCitemKindsTemp%5B9%5D.endDate=&prpCitemKindsTemp%5B9%5D.endHour=&prpCitemKindsTemp%5B9%5D.flag= 200000&prpCitemKindsTemp%5B9%5D.basePremium=&prpCitemKindsTemp%5B9%5D.rate=&prpCitemKindsTemp%5B9%5D.benchMarkPremium=&prpCitemKindsTemp%5B9%5D.disCount=&prpCitemKindsTemp%5B9%5D.premium=&prpCitemKindsTemp%5B9%5D.netPremium=&prpCitemKindsTemp%5B9%5D.taxPremium=&prpCitemKindsTemp%5B9%5D.taxRate=&prpCitemKindsTemp%5B9%5D.dutyFlag=&prpCitemKindsTemp%5B10%5D.min=&prpCitemKindsTemp%5B10%5D.max=&prpCitemKindsTemp%5B10%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B10%5D.clauseCode=050033&prpCitemKindsTemp%5B10%5D.kindCode=050630&relateSpecial%5B10%5D=050926&prpCitemKindsTemp%5B10%5D.kindName=%B8%BD%BC%D3%D3%CD%CE%DB%CE%DB%C8%BE%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B10%5D.amount=50000.00&prpCitemKindsTemp%5B10%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B10%5D.startDate=&prpCitemKindsTemp%5B10%5D.startHour=&prpCitemKindsTemp%5B10%5D.endDate=&prpCitemKindsTemp%5B10%5D.endHour=&prpCitemKindsTemp%5B10%5D.flag= 200000&prpCitemKindsTemp%5B10%5D.basePremium=&prpCitemKindsTemp%5B10%5D.rate=&prpCitemKindsTemp%5B10%5D.benchMarkPremium=&prpCitemKindsTemp%5B10%5D.disCount=&prpCitemKindsTemp%5B10%5D.premium=&prpCitemKindsTemp%5B10%5D.netPremium=&prpCitemKindsTemp%5B10%5D.taxPremium=&prpCitemKindsTemp%5B10%5D.taxRate=&prpCitemKindsTemp%5B10%5D.dutyFlag=&prpCitemKindsTemp%5B21%5D.chooseFlag=&prpCitemKindsTemp%5B21%5D.itemKindNo=&prpCitemKindsTemp%5B21%5D.startDate=&prpCitemKindsTemp%5B21%5D.kindCode=050291&prpCitemKindsTemp%5B21%5D.kindName=%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B21%5D.startHour=&prpCitemKindsTemp%5B21%5D.endDate=&prpCitemKindsTemp%5B21%5D.endHour=&prpCitemKindsTemp%5B21%5D.calculateFlag=N32Y000&relateSpecial%5B16%5D=050924&prpCitemKindsTemp%5B21%5D.flag= 200000&prpCitemKindsTemp%5B21%5D.basePremium=&prpCitemKindsTemp%5B21%5D.specialFlag=on&prpCitemKindsTemp%5B21%5D.amount=&prpCitemKindsTemp%5B21%5D.rate=&prpCitemKindsTemp%5B21%5D.benchMarkPremium=&prpCitemKindsTemp%5B21%5D.disCount=&prpCitemKindsTemp%5B21%5D.premium=&prpCitemKindsTemp%5B21%5D.netPremium=&prpCitemKindsTemp%5B21%5D.taxPremium=&prpCitemKindsTemp%5B21%5D.taxRate=&prpCitemKindsTemp%5B21%5D.dutyFlag=&prpCitemKindsTemp.itemKindSpecialSumPremium=&prpCitemKindsTemp%5B16%5D.chooseFlag=&prpCitemKindsTemp%5B16%5D.itemKindNo=&prpCitemKindsTemp%5B16%5D.startDate=&prpCitemKindsTemp%5B16%5D.kindCode=050911&prpCitemKindsTemp%5B16%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%CB%F0%CF%D5%A3%A9&prpCitemKindsTemp%5B16%5D.startHour=&prpCitemKindsTemp%5B16%5D.endDate=&prpCitemKindsTemp%5B16%5D.endHour=&prpCitemKindsTemp%5B16%5D.calculateFlag=N33Y000&relateSpecial%5B11%5D=&prpCitemKindsTemp%5B16%5D.flag= 200000&prpCitemKindsTemp%5B16%5D.basePremium=&prpCitemKindsTemp%5B16%5D.amount=&prpCitemKindsTemp%5B16%5D.rate=&prpCitemKindsTemp%5B16%5D.benchMarkPremium=&prpCitemKindsTemp%5B16%5D.disCount=&prpCitemKindsTemp%5B16%5D.premium=&prpCitemKindsTemp%5B16%5D.netPremium=&prpCitemKindsTemp%5B16%5D.taxPremium=&prpCitemKindsTemp%5B16%5D.taxRate=&prpCitemKindsTemp%5B16%5D.dutyFlag=&prpCitemKindsTemp%5B17%5D.chooseFlag=&prpCitemKindsTemp%5B17%5D.itemKindNo=&prpCitemKindsTemp%5B17%5D.startDate=&prpCitemKindsTemp%5B17%5D.kindCode=050921&prpCitemKindsTemp%5B17%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%BB%FA%B6%AF%B3%B5%B5%C1%C7%C0%CF%D5%A3%A9&prpCitemKindsTemp%5B17%5D.startHour=&prpCitemKindsTemp%5B17%5D.endDate=&prpCitemKindsTemp%5B17%5D.endHour=&prpCitemKindsTemp%5B17%5D.calculateFlag=N33Y000&relateSpecial%5B12%5D=&prpCitemKindsTemp%5B17%5D.flag= 200000&prpCitemKindsTemp%5B17%5D.basePremium=&prpCitemKindsTemp%5B17%5D.amount=&prpCitemKindsTemp%5B17%5D.rate=&prpCitemKindsTemp%5B17%5D.benchMarkPremium=&prpCitemKindsTemp%5B17%5D.disCount=&prpCitemKindsTemp%5B17%5D.premium=&prpCitemKindsTemp%5B17%5D.netPremium=&prpCitemKindsTemp%5B17%5D.taxPremium=&prpCitemKindsTemp%5B17%5D.taxRate=&prpCitemKindsTemp%5B17%5D.dutyFlag=&prpCitemKindsTemp%5B18%5D.chooseFlag=&prpCitemKindsTemp%5B18%5D.itemKindNo=&prpCitemKindsTemp%5B18%5D.startDate=&prpCitemKindsTemp%5B18%5D.kindCode=050912&prpCitemKindsTemp%5B18%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%C8%FD%D5%DF%CF%D5%A3%A9&prpCitemKindsTemp%5B18%5D.startHour=&prpCitemKindsTemp%5B18%5D.endDate=&prpCitemKindsTemp%5B18%5D.endHour=&prpCitemKindsTemp%5B18%5D.calculateFlag=N33Y000&relateSpecial%5B13%5D=&prpCitemKindsTemp%5B18%5D.flag= 200000&prpCitemKindsTemp%5B18%5D.basePremium=&prpCitemKindsTemp%5B18%5D.amount=&prpCitemKindsTemp%5B18%5D.rate=&prpCitemKindsTemp%5B18%5D.benchMarkPremium=&prpCitemKindsTemp%5B18%5D.disCount=&prpCitemKindsTemp%5B18%5D.premium=&prpCitemKindsTemp%5B18%5D.netPremium=&prpCitemKindsTemp%5B18%5D.taxPremium=&prpCitemKindsTemp%5B18%5D.taxRate=&prpCitemKindsTemp%5B18%5D.dutyFlag=&prpCitemKindsTemp%5B19%5D.chooseFlag=&prpCitemKindsTemp%5B19%5D.itemKindNo=&prpCitemKindsTemp%5B19%5D.startDate=&prpCitemKindsTemp%5B19%5D.kindCode=050928&prpCitemKindsTemp%5B19%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9%A3%A9&prpCitemKindsTemp%5B19%5D.startHour=&prpCitemKindsTemp%5B19%5D.endDate=&prpCitemKindsTemp%5B19%5D.endHour=&prpCitemKindsTemp%5B19%5D.calculateFlag=N33Y000&relateSpecial%5B14%5D=&prpCitemKindsTemp%5B19%5D.flag= 200000&prpCitemKindsTemp%5B19%5D.basePremium=&prpCitemKindsTemp%5B19%5D.amount=&prpCitemKindsTemp%5B19%5D.rate=&prpCitemKindsTemp%5B19%5D.benchMarkPremium=&prpCitemKindsTemp%5B19%5D.disCount=&prpCitemKindsTemp%5B19%5D.premium=&prpCitemKindsTemp%5B19%5D.netPremium=&prpCitemKindsTemp%5B19%5D.taxPremium=&prpCitemKindsTemp%5B19%5D.taxRate=&prpCitemKindsTemp%5B19%5D.dutyFlag=&prpCitemKindsTemp%5B20%5D.chooseFlag=&prpCitemKindsTemp%5B20%5D.itemKindNo=&prpCitemKindsTemp%5B20%5D.startDate=&prpCitemKindsTemp%5B20%5D.kindCode=050929&prpCitemKindsTemp%5B20%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9%A3%A9&prpCitemKindsTemp%5B20%5D.startHour=&prpCitemKindsTemp%5B20%5D.endDate=&prpCitemKindsTemp%5B20%5D.endHour=&prpCitemKindsTemp%5B20%5D.calculateFlag=N33Y000&relateSpecial%5B15%5D=&prpCitemKindsTemp%5B20%5D.flag= 200000&prpCitemKindsTemp%5B20%5D.basePremium=&prpCitemKindsTemp%5B20%5D.amount=&prpCitemKindsTemp%5B20%5D.rate=&prpCitemKindsTemp%5B20%5D.benchMarkPremium=&prpCitemKindsTemp%5B20%5D.disCount=&prpCitemKindsTemp%5B20%5D.premium=&prpCitemKindsTemp%5B20%5D.netPremium=&prpCitemKindsTemp%5B20%5D.taxPremium=&prpCitemKindsTemp%5B20%5D.taxRate=&prpCitemKindsTemp%5B20%5D.dutyFlag=&prpCitemKindsTemp%5B22%5D.chooseFlag=&prpCitemKindsTemp%5B22%5D.itemKindNo=&prpCitemKindsTemp%5B22%5D.startDate=&prpCitemKindsTemp%5B22%5D.kindCode=050924&prpCitemKindsTemp%5B22%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B7%A2%B6%AF%BB%FA%CC%D8%B1%F0%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B22%5D.startHour=&prpCitemKindsTemp%5B22%5D.endDate=&prpCitemKindsTemp%5B22%5D.endHour=&prpCitemKindsTemp%5B22%5D.calculateFlag=N33Y000&relateSpecial%5B17%5D=&prpCitemKindsTemp%5B22%5D.flag= 200000&prpCitemKindsTemp%5B22%5D.basePremium=&prpCitemKindsTemp%5B22%5D.amount=&prpCitemKindsTemp%5B22%5D.rate=&prpCitemKindsTemp%5B22%5D.benchMarkPremium=&prpCitemKindsTemp%5B22%5D.disCount=&prpCitemKindsTemp%5B22%5D.premium=&prpCitemKindsTemp%5B22%5D.netPremium=&prpCitemKindsTemp%5B22%5D.taxPremium=&prpCitemKindsTemp%5B22%5D.taxRate=&prpCitemKindsTemp%5B22%5D.dutyFlag=&prpCitemKindsTemp%5B23%5D.chooseFlag=&prpCitemKindsTemp%5B23%5D.itemKindNo=&prpCitemKindsTemp%5B23%5D.startDate=&prpCitemKindsTemp%5B23%5D.kindCode=050922&prpCitemKindsTemp%5B23%5D.kindName=%B2%BB%BC%C6%C3%E2%C5%E2%C2%CA%A3%A8%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%A3%A9&prpCitemKindsTemp%5B23%5D.startHour=&prpCitemKindsTemp%5B23%5D.endDate=&prpCitemKindsTemp%5B23%5D.endHour=&prpCitemKindsTemp%5B23%5D.calculateFlag=N33Y000&relateSpecial%5B18%5D=&prpCitemKindsTemp%5B23%5D.flag= 200000&prpCitemKindsTemp%5B23%5D.basePremium=&prpCitemKindsTemp%5B23%5D.amount=&prpCitemKindsTemp%5B23%5D.rate=&prpCitemKindsTemp%5B23%5D.benchMarkPremium=&prpCitemKindsTemp%5B23%5D.disCount=&prpCitemKindsTemp%5B23%5D.premium=&prpCitemKindsTemp%5B23%5D.netPremium=&prpCitemKindsTemp%5B23%5D.taxPremium=&prpCitemKindsTemp%5B23%5D.taxRate=&prpCitemKindsTemp%5B23%5D.dutyFlag=&hidden_index_itemKind=11&hidden_index_profitDetial=0&prpCitemKindsTemp_%5B0%5D.chooseFlag=&prpCitemKindsTemp_%5B0%5D.itemKindNo=&prpCitemKindsTemp_%5B0%5D.startDate=&prpCitemKindsTemp_%5B0%5D.kindCode=&prpCitemKindsTemp_%5B0%5D.kindName=&prpCitemKindsTemp_%5B0%5D.startHour=&prpCitemKindsTemp_%5B0%5D.endDate=&prpCitemKindsTemp_%5B0%5D.endHour=&prpCitemKindsTemp_%5B0%5D.calculateFlag=&relateSpecial_%5B0%5D=&prpCitemKindsTemp_%5B0%5D.flag=&prpCitemKindsTemp_%5B0%5D.basePremium=&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.rate=&prpCitemKindsTemp_%5B0%5D.benchMarkPremium=&prpCitemKindsTemp_%5B0%5D.disCount=&prpCitemKindsTemp_%5B0%5D.premium=&prpCitemKindsTemp_%5B0%5D.netPremium=&prpCitemKindsTemp_%5B0%5D.taxPremium=&prpCitemKindsTemp_%5B0%5D.taxRate=&prpCitemKindsTemp_%5B0%5D.dutyFlag=&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.value=&prpCitemKindsTemp_%5B0%5D.value=50&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.modeCode=10&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.value=1000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=10000&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=60&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=90&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.amount=50000.00&prpCitemKindsTemp_%5B0%5D.amount=10000.00&prpCitemKindsTemp_%5B0%5D.amount=5000.00&itemKindLoadFlag=&BIdemandNo=&BIdemandTime=&bIRiskWarningType=&noDamageYearsBIPlat=0&prpCitemCarExt.lastDamagedBI=0&lastDamagedBITemp=&DAZlastDamagedBI=&prpCitemCarExt.thisDamagedBI=0&prpCitemCarExt.noDamYearsBI=0&noDamYearsBINumber=0&prpCitemCarExt.lastDamagedCI=0&BIDemandClaim_Flag=&BiInsureDemandPay_%5B0%5D.id.serialNo=&BiInsureDemandPay_%5B0%5D.payCompany=&BiInsureDemandPay_%5B0%5D.claimregistrationno=&BiInsureDemandPay_%5B0%5D.compensateNo=&BiInsureDemandPay_%5B0%5D.lossTime=&BiInsureDemandPay_%5B0%5D.endcCaseTime=&PrpCmain_%5B0%5D.startDate=&PrpCmain_%5B0%5D.endDate=&BiInsureDemandPay_%5B0%5D.lossFee=&BiInsureDemandPay_%5B0%5D.payType=&BiInsureDemandPay_%5B0%5D.personpayType=&bIRiskWarningClaimItems_%5B0%5D.id.serialNo=&bIRiskWarningClaimItems_%5B0%5D.riskWarningType=&bIRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&bIRiskWarningClaimItems_%5B0%5D.insurerCode=&bIRiskWarningClaimItems_%5B0%5D.lossTime=&bIRiskWarningClaimItems_%5B0%5D.lossArea=&prpCitemKindCI.shortRate=100&cIBPFlag=1&prpCitemKindCI.unitAmount=0&prpCitemKindCI.id.itemKindNo=&prpCitemKindCI.kindCode=050100&prpCitemKindCI.kindName=%BB%FA%B6%AF%B3%B5%BD%BB%CD%A8%CA%C2%B9%CA%C7%BF%D6%C6%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindCI.calculateFlag=Y&prpCitemKindCI.basePremium=&prpCitemKindCI.quantity=1&prpCitemKindCI.amount=122000&prpCitemKindCI.deductible=0&prpCitemKindCI.adjustRate=0.70&prpCitemKindCI.rate=0&prpCitemKindCI.benchMarkPremium=950&prpCitemKindCI.disCount=1&prpCitemKindCI.premium=565.3&prpCitemKindCI.flag=&prpCitemKindCI.netPremium=533.30&prpCitemKindCI.taxPremium=32.00&prpCitemKindCI.taxRate=6.00&prpCitemKindCI.dutyFlag=2&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCitemCarExt_CI.rateRloatFlag=01&prpCitemCarExt_CI.noDamYearsCI=1&prpCitemCarExt_CI.lastDamagedCI=0&prpCitemCarExt_CI.flag=&prpCitemCarExt_CI.damFloatRatioCI=0&prpCitemCarExt_CI.offFloatRatioCI=0&prpCitemCarExt_CI.thisDamagedCI=0&prpCitemCarExt_CI.flag=&hidden_index_ctraffic_NOPlat_Drink=0&hidden_index_ctraffic_NOPlat=0&ciInsureDemand.demandNo=01PICC02160000000000908482124F&ciInsureDemand.demandTime=2016-06-12&ciInsureDemand.restricFlag=0001&ciInsureDemand.preferentialDay=53&ciInsureDemand.preferentialPremium=99.7&ciInsureDemand.preferentialFormula%20=%BC%F5%C3%E2%B1%A3%B7%D1=(638.4/366*33) %2B (769.11/365*20)&ciInsureDemand.lastyearenddate=2016-08-23&prpCitemCar.noDamageYears=0&ciInsureDemand.rateRloatFlag=00&ciInsureDemand.claimAdjustReason=A1&ciInsureDemand.peccancyAdjustReason=V1&cIRiskWarningType=&CIDemandFecc_Flag=&ciInsureDemandLoss_%5B0%5D.id.serialNo=&ciInsureDemandLoss_%5B0%5D.lossTime=&ciInsureDemandLoss_%5B0%5D.lossDddress=&ciInsureDemandLoss_%5B0%5D.lossAction=&ciInsureDemandLoss_%5B0%5D.coeff=&ciInsureDemandLoss_%5B0%5D.lossType=&ciInsureDemandLoss_%5B0%5D.identifyType=&ciInsureDemandLoss_%5B0%5D.identifyNumber=&ciInsureDemandLoss_%5B0%5D.lossAcceptDate=&ciInsureDemandLoss_%5B0%5D.processingStatus=&ciInsureDemandLoss_%5B0%5D.lossActionDesc=&CIDemandClaim_Flag=&ciInsureDemandPay_%5B0%5D.id.serialNo=&ciInsureDemandPay_%5B0%5D.payCompany=&ciInsureDemandPay_%5B0%5D.claimregistrationno=&ciInsureDemandPay_%5B0%5D.compensateNo=&ciInsureDemandPay_%5B0%5D.lossTime=&ciInsureDemandPay_%5B0%5D.endcCaseTime=&ciInsureDemandPay_%5B0%5D.lossFee=&ciInsureDemandPay_%5B0%5D.payType=&ciInsureDemandPay_%5B0%5D.personpayType=&ciRiskWarningClaimItems_%5B0%5D.id.serialNo=&ciRiskWarningClaimItems_%5B0%5D.riskWarningType=&ciRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&ciRiskWarningClaimItems_%5B0%5D.insurerCode=&ciRiskWarningClaimItems_%5B0%5D.lossTime=&ciRiskWarningClaimItems_%5B0%5D.lossArea=&ciInsureDemand.licenseNo=%BE%A9Q8D075&ciInsureDemand.licenseType=02&ciInsureDemand.useNatureCode=A&ciInsureDemand.frameNo=LSGGA54Y4DH157325&ciInsureDemand.engineNo=131680128&ciInsureDemand.licenseColorCode=E&ciInsureDemand.carOwner=%CC%C6%C0%F6%D3%B1&ciInsureDemand.enrollDate=&ciInsureDemand.makeDate=&ciInsureDemand.seatCount=5&ciInsureDemand.tonCount=0&ciInsureDemand.validCheckDate=&ciInsureDemand.manufacturerName=%C9%CF%BA%A3%CD%A8%D3%C3%C6%FB%B3%B5%D3%D0%CF%DE%B9%AB%CB%BE&ciInsureDemand.modelCode=SGM7206ATA&ciInsureDemand.brandCName=%B1%F0%BF%CB%C5%C6&ciInsureDemand.brandName=&ciInsureDemand.carKindCode=K33&ciInsureDemand.checkDate=&ciInsureDemand.endValidDate=&ciInsureDemand.carStatus=A&ciInsureDemand.haulage=&AccidentFlag=&rateFloatFlag=ND4&prpCtrafficRecordTemps_%5B0%5D.id.serialNo=&prpCtrafficRecordTemps_%5B0%5D.accidentDate=&prpCtrafficRecordTemps_%5B0%5D.claimDate=&hidden_index_ctraffic=0&_taxUnit=&taxPlatFormTime=2012-04-21&iniPrpCcarShipTax_Flag=&strCarShipFlag=1&prpCcarShipTax.taxType=1&prpCcarShipTax.calculateMode=C1&prpCcarShipTax.leviedDate=&prpCcarShipTax.carKindCode=A01&prpCcarShipTax.model=B11&prpCcarShipTax.taxPayerIdentNo=null&prpCcarShipTax.taxPayerNumber=null&prpCcarShipTax.carLotEquQuality=1585&prpCcarShipTax.taxPayerCode=1100100001900281&prpCcarShipTax.id.itemNo=1&prpCcarShipTax.taxPayerNature=3&prpCcarShipTax.taxPayerName=%CC%C6%C0%F6%D3%B1&prpCcarShipTax.taxUnit=&prpCcarShipTax.taxComCode=&prpCcarShipTax.taxComName=&prpCcarShipTax.taxExplanation=&prpCcarShipTax.taxAbateReason=&prpCcarShipTax.dutyPaidProofNo_1=&prpCcarShipTax.dutyPaidProofNo_2=&prpCcarShipTax.dutyPaidProofNo=&prpCcarShipTax.taxAbateRate=&prpCcarShipTax.taxAbateAmount=&prpCcarShipTax.taxAbateType=1&prpCcarShipTax.taxUnitAmount=&prpCcarShipTax.prePayTaxYear=2015&prpCcarShipTax.prePolicyEndDate=&prpCcarShipTax.payStartDate=2016-01-01&prpCcarShipTax.payEndDate=2016-12-31&prpCcarShipTax.thisPayTax=400&prpCcarShipTax.prePayTax=0&prpCcarShipTax.taxItemCode=&prpCcarShipTax.taxItemName=&prpCcarShipTax.baseTaxation=&prpCcarShipTax.taxRelifFlag=&prpCcarShipTax.delayPayTax=0&prpCcarShipTax.sumPayTax=400&CarShipInit_Flag=&prpCcarShipTax.flag=&quotationtaxPayerCode=&noBringOutEngage=&prpCengageTemps_%5B0%5D.id.serialNo=1&prpCengageTemps_%5B0%5D.clauseCode=910012&prpCengageTemps_%5B0%5D.clauseName=%CE%B2%BA%C5%BC%F5%C3%E2%CC%D8%D4%BC&clauses_%5B0%5D=&prpCengageTemps_%5B0%5D.flag=&prpCengageTemps_%5B0%5D.engageFlag=&prpCengageTemps_%5B0%5D.maxCount=&prpCengageTemps_%5B0%5D.clauses=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA53.0%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D199.7%D4%AA%A3%AC %BC%F5%C3%E2%B1%A3%B7%D1=(638.4/366*33) %2B (769.11/365*20)%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3&iniPrpCengage_Flag=&hidden_index_engage=0&prpCengageTemps%5B0%5D.id.serialNo=1&prpCengageTemps%5B0%5D.clauseCode=910012&prpCengageTemps%5B0%5D.clauseName=%CE%B2%BA%C5%BC%F5%C3%E2%CC%D8%D4%BC&clauses%5B0%5D=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA53.0%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D199.7%D4%AA%A3%AC %BC%F5%C3%E2%B1%A3%B7%D1=(638.4/366*33) %2B (769.11/365*20)%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3&prpCengageTemps%5B0%5D.flag=&prpCengageTemps%5B0%5D.engageFlag=&prpCengageTemps%5B0%5D.maxCount=&prpCengageTemps%5B0%5D.clauses=%C4%FA%B5%C4%B3%B5%C1%BE%B2%BB%CF%ED%CA%DC%B0%C2%D4%CB%CF%DE%D0%D0%BC%F5%C3%E2%A3%AC%CA%F4%B0%B4%CE%B2%BA%C5%CF%DE%D0%D0%B7%B6%CE%A7%A3%AC %BD%F8%D0%D0%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BC%F5%C3%E2%A3%AC %BC%F5%C3%E2%D7%DC%CC%EC%CA%FD%CE%AA53.0%CC%EC%A3%AC%BC%F5%C3%E2%B1%A3%B7%D199.7%D4%AA%A3%AC %BC%F5%C3%E2%B1%A3%B7%D1=(638.4/366*33) %2B (769.11/365*20)%A3%AC%BC%F5%C3%E2%BA%F3%B1%A3%B7%D1%BD%F0%B6%EE=%B1%BE%C4%EA%B6%C8%BD%BB%C7%BF%CF%D5%B1%A3%B7%D1%BD%F0%B6%EE-%BC%F5%C3%E2%B1%A3%B7%D1%BD%F0%B6%EE %A1%A3&costRateForPG=&certificateNo=&levelMaxRate=&maxRateScm=&levelMaxRateCi=&maxRateScmCi=&isModifyBI=&isModifyCI=&sumBICoinsRate=&sumCICoinsRate=&agentsRateBI=&agentsRateCI=&prpVisaRecordP.id.visaNo=&prpVisaRecordP.id.visaCode=&prpVisaRecordP.visaName=&prpVisaRecordP.printType=101&prpVisaRecordT.id.visaNo=&prpVisaRecordT.id.visaCode=&prpVisaRecordT.visaName=&prpVisaRecordT.printType=103&prpCmain.sumAmount=&prpCmain.sumDiscount=&prpCstampTaxBI.biTaxRate=&prpCstampTaxBI.biPayTax=&prpCmain.sumPremium=&prpVisaRecordPCI.id.visaNo=&prpVisaRecordPCI.id.visaCode=&prpVisaRecordPCI.visaName=&prpVisaRecordPCI.printType=201&prpVisaRecordTCI.id.visaNo=&prpVisaRecordTCI.id.visaCode=&prpVisaRecordTCI.visaName=&prpVisaRecordTCI.printType=203&prpCmainCI.sumAmount=122000&prpCmainCI.sumDiscount=384.70&prpCstampTaxCI.ciTaxRate=&prpCstampTaxCI.ciPayTax=0&prpCmainCI.sumPremium=565.3&prpCmainCar.rescueFundRate=0.01&prpCmainCar.resureFundFee=5.65&prpCmain.projectCode=&projectCode=&costRateUpper=&prpCmainCommon.ext3=&importantProjectCode=&prpCmain.operatorCode=020083&operatorName=%D6%DA%BA%CF%CB%C4%BA%A3&operateDateShow=&prpCmain.coinsFlag=00&coinsFlagBak=00&premium=&prpCmain.language=CNY&prpCmain.policySort=1&prpCmain.policyRelCode=&prpCmain.policyRelName=&subsidyRate=&policyRel=&prpCmain.reinsFlag=0&prpCmain.agriFlag=0&premium=&prpCmainCar.carCheckStatus=0&prpCmainCar.carChecker=&carCheckerTranslate=&prpCmainCar.carCheckTime=&prpCmainCommon.DBCFlag=0&prpCmain.argueSolution=1&prpCmain.arbitBoardName=&arbitBoardNameDes=&prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&hidden_index_commission=0&scmIsOpen=1111100000&prpCagents_%5B0%5D.roleType=&roleTypeName_%5B0%5D=&prpCagents_%5B0%5D.id.roleCode=&prpCagents_%5B0%5D.roleCode_uni=&prpCagents_%5B0%5D.roleName=&prpCagents_%5B0%5D.costRate=&prpCagents_%5B0%5D.costFee=&prpCagents_%5B0%5D.flag=&prpCagents_%5B0%5D.businessNature=&prpCagents_%5B0%5D.isMain=&prpCagentCIs_%5B0%5D.roleType=&roleTypeNameCI_%5B0%5D=&prpCagentCIs_%5B0%5D.id.roleCode=&prpCagentCIs_%5B0%5D.roleCode_uni=&prpCagentCIs_%5B0%5D.roleName=&prpCagentCIs_%5B0%5D.costRate=&prpCagentCIs_%5B0%5D.costFee=&prpCagentCIs_%5B0%5D.flag=&prpCagentCIs_%5B0%5D.businessNature=&prpCagentCIs_%5B0%5D.isMain=&commissionCount=&prpCsaless_%5B0%5D.salesDetailName=&prpCsaless_%5B0%5D.riskCode=&prpCsaless_%5B0%5D.splitRate=&prpCsaless_%5B0%5D.oriSplitNumber=&prpCsaless_%5B0%5D.splitFee=&prpCsaless_%5B0%5D.agreementNo=&prpCsaless_%5B0%5D.id.salesCode=&prpCsaless_%5B0%5D.salesName=&prpCsaless_%5B0%5D.id.proposalNo=&prpCsaless_%5B0%5D.id.salesDetailCode=&prpCsaless_%5B0%5D.totalRate=&prpCsaless_%5B0%5D.splitWay=&prpCsaless_%5B0%5D.totalRateMax=&prpCsaless_%5B0%5D.flag=&prpCsaless_%5B0%5D.remark=&commissionPower=&hidden_index_prpCsales=0&prpCsalesDatils_%5B0%5D.id.salesCode=&prpCsalesDatils_%5B0%5D.id.proposalNo=&prpCsalesDatils_%5B0%5D.id.%20%20=&prpCsalesDatils_%5B0%5D.id.roleType=&prpCsalesDatils_%5B0%5D.id.roleCode=&prpCsalesDatils_%5B0%5D.currency=&prpCsalesDatils_%5B0%5D.splitDatilRate=&prpCsalesDatils_%5B0%5D.splitDatilFee=&prpCsalesDatils_%5B0%5D.roleName=&prpCsalesDatils_%5B0%5D.splitWay=&prpCsalesDatils_%5B0%5D.flag=&prpCsalesDatils_%5B0%5D.remark=&hidden_index_prpCsalesDatil=0&csManageSwitch=1&prpCmainChannel.agentCode=&prpCmainChannel.agentName=&prpCmainChannel.organCode=&prpCmainChannel.organCName=&comCodeType=&prpCmainChannel.identifyNumber=&prpCmainChannel.identifyType=&prpCmainChannel.manOrgCode=&prpCmain.remark=&prpDdismantleDetails_%5B0%5D.id.agreementNo=&prpDdismantleDetails_%5B0%5D.flag=&prpDdismantleDetails_%5B0%5D.id.configCode=&prpDdismantleDetails_%5B0%5D.id.assignType=&prpDdismantleDetails_%5B0%5D.id.roleCode=&prpDdismantleDetails_%5B0%5D.roleName=&prpDdismantleDetails_%5B0%5D.costRate=&prpDdismantleDetails_%5B0%5D.roleFlag=&prpDdismantleDetails_%5B0%5D.businessNature=&prpDdismantleDetails_%5B0%5D.roleCode_uni=&hidden_index_prpDdismantleDetails=0&payTimes=1&prpCplanTemps_%5B0%5D.payNo=&prpCplanTemps_%5B0%5D.serialNo=&prpCplanTemps_%5B0%5D.endorseNo=&cplan_%5B0%5D.payReasonC=&prpCplanTemps_%5B0%5D.payReason=&prpCplanTemps_%5B0%5D.planDate=&prpCplanTemps_%5B0%5D.currency=&description_%5B0%5D.currency=&prpCplanTemps_%5B0%5D.planFee=&cplans_%5B0%5D.planFee=&cplans_%5B0%5D.backPlanFee=&prpCplanTemps_%5B0%5D.netPremium=&prpCplanTemps_%5B0%5D.taxPremium=&prpCplanTemps_%5B0%5D.delinquentFee=&prpCplanTemps_%5B0%5D.flag=&prpCplanTemps_%5B0%5D.subsidyRate=&prpCplanTemps_%5B0%5D.isBICI=&iniPrpCplan_Flag=&loadFlag9=&planfee_index=0&planStr=&planPayTimes=&prpAnciInfo.sellExpensesRate=&prpAnciInfo.sellExpensesAmount=&prpAnciInfo.sellExpensesRateCIUp=&prpAnciInfo.sellExpensesCIUpAmount=&prpAnciInfo.sellExpensesRateBIUp=&prpAnciInfo.sellExpensesBIUpAmount=&prpAnciInfo.operSellExpensesRate=&prpAnciInfo.operSellExpensesAmount=&prpAnciInfo.operSellExpensesRateCI=&prpAnciInfo.operSellExpensesAmountCI=&prpAnciInfo.operSellExpensesRateBI=&prpAnciInfo.operSellExpensesAmountBI=&prpAnciInfo.operCommRateCIUp=&operCommRateCIUpAmount=&prpAnciInfo.operCommRateBIUp=&operCommRateBIUpAmount=&prpAnciInfo.operCommRate=&prpAnciInfo.operCommRateAmount=&prpAnciInfo.operateCommRateCI=&prpAnciInfo.operateCommCI=&prpAnciInfo.operateCommRateBI=&prpAnciInfo.operateCommBI=&prpAnciInfo.discountRateUp=&prpAnciInfo.discountRateUpAmount=&prpAnciInfo.discountRateCIUp=&prpAnciInfo.discountRateCIUpAmount=&prpAnciInfo.profitRateBIUp=&prpAnciInfo.discountRateBIUpAmountp=&prpAnciInfo.discountRate=&prpAnciInfo.discountRateAmount=&prpAnciInfo.discountRateCI=&prpAnciInfo.discountRateCIAmount=&prpAnciInfo.discountRateBI=&prpAnciInfo.discountRateBIAmount=&prpAnciInfo.riskCode=&prpAnciInfo.standPayRate=&prpAnciInfo.operatePayRate=&prpAnciInfo.busiStandardBalanRate=&prpAnciInfo.busiBalanRate=&prpAnciInfo.busiRiskRate=&prpAnciInfo.averProfitRate=&prpAnciInfo.averageRate=&prpAnciInfo.minNetSumPremiumBI=&prpAnciInfo.minNetSumPremiumCI=&prpAnciInfo.baseActBusiType=&prpAnciInfo.baseExpBusiType=&prpAnciInfo.operateProfitRate=&prpAnciInfo.breakEvenValue=&prpAnciInfo.profitRateBIUp=&prpAnciInfo.proCommRateBIUp=&prpAnciInfo.busiTypeCommBIUp=&prpAnciInfo.busiTypeCommCIUp=&prpAnciInfo.standbyField1=&switchFlag=0&actProfitRate=&prpAnciInfo.businessCode=&prpAnciInfo.minNetSumPremium=&prpAnciInfo.origBusiType=&prpAnciInfo.expProCommRateUp=&expProCommRateUp_Disc=&prpAnciInfo.expBusiType=&prpAnciInfo.actProCommRateUp=&actProCommRateUp_Disc=&prpAnciInfo.actBusiType=&expRiskNote=&kindBusiTypeA=&kindBusiTypeB=&kindBusiTypeC=&kindBusiTypeD=&kindBusiTypeE=&prpCmainCar.flag=1&prpCmainCarFlag=1&coinsSchemeCode=&coinsSchemeName=&mainPolicyNo=&prpCcoinsMains_%5B0%5D.id.serialNo=1&prpCcoinsMains_%5B0%5D.coIdentity=1&prpCcoinsMains_%5B0%5D.coinsCode=002&prpCcoinsMains_%5B0%5D.coinsName=%C8%CB%B1%A3%B2%C6%B2%FA&prpCcoinsMains_%5B0%5D.coinsRate=&prpCcoinsMains_%5B0%5D.id.currency=CNY&prpCcoinsMains_%5B0%5D.coinsAmount=&prpCcoinsMains_%5B0%5D.coinsPremium=&prpCcoinsMains_%5B0%5D.coinsPremium=&iniPrpCcoins_Flag=&hidden_index_ccoins=0&prpCpayeeAccountBIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountBIs_%5B0%5D.id.serialNo=&prpCpayeeAccountBIs_%5B0%5D.itemNo=&prpCpayeeAccountBIs_%5B0%5D.payReason=&prpCpayeeAccountBIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountBIs_%5B0%5D.accountName=&prpCpayeeAccountBIs_%5B0%5D.basicBankCode=&prpCpayeeAccountBIs_%5B0%5D.basicBankName=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountBIs_%5B0%5D.bankCode=&prpCpayeeAccountBIs_%5B0%5D.bankName=&prpCpayeeAccountBIs_%5B0%5D.cnaps=&prpCpayeeAccountBIs_%5B0%5D.accountNo=&prpCpayeeAccountBIs_%5B0%5D.isPrivate=&prpCpayeeAccountBIs_%5B0%5D.cardType=&prpCpayeeAccountBIs_%5B0%5D.paySumFee=&prpCpayeeAccountBIs_%5B0%5D.payType=&prpCpayeeAccountBIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountBIs_%5B0%5D.sendSms=&prpCpayeeAccountBIs_%5B0%5D.identifyType=&prpCpayeeAccountBIs_%5B0%5D.identifyNo=&prpCpayeeAccountBIs_%5B0%5D.telephone=&prpCpayeeAccountBIs_%5B0%5D.sendMail=&prpCpayeeAccountBIs_%5B0%5D.mailAddr=&prpCpayeeAccountCIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountCIs_%5B0%5D.id.serialNo=&prpCpayeeAccountCIs_%5B0%5D.itemNo=&prpCpayeeAccountCIs_%5B0%5D.payReason=&prpCpayeeAccountCIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountCIs_%5B0%5D.accountName=&prpCpayeeAccountCIs_%5B0%5D.basicBankCode=&prpCpayeeAccountCIs_%5B0%5D.basicBankName=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountCIs_%5B0%5D.bankCode=&prpCpayeeAccountCIs_%5B0%5D.bankName=&prpCpayeeAccountCIs_%5B0%5D.cnaps=&prpCpayeeAccountCIs_%5B0%5D.accountNo=&prpCpayeeAccountCIs_%5B0%5D.isPrivate=&prpCpayeeAccountCIs_%5B0%5D.cardType=&prpCpayeeAccountCIs_%5B0%5D.paySumFee=&prpCpayeeAccountCIs_%5B0%5D.payType=&prpCpayeeAccountCIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountCIs_%5B0%5D.sendSms=&prpCpayeeAccountCIs_%5B0%5D.identifyType=&prpCpayeeAccountCIs_%5B0%5D.identifyNo=&prpCpayeeAccountCIs_%5B0%5D.telephone=&prpCpayeeAccountCIs_%5B0%5D.sendMail=&prpCpayeeAccountCIs_%5B0%5D.mailAddr=&iReinsCode=&prpCspecialFacs_%5B0%5D.reinsCode=001&iFReinsCode=&iPayCode=&iShareRate=&iCommRate=&iTaxRate=&iOthRate=&iCommission=&iOthPremium=&prpCspecialFacs_%5B0%5D.id.reinsNo=1&prpCspecialFacs_%5B0%5D.freinsCode=001&prpCspecialFacs_%5B0%5D.payCode=001&prpCspecialFacs_%5B0%5D.shareRate=001&prpCspecialFacs_%5B0%5D.sharePremium=001&prpCspecialFacs_%5B0%5D.commRate=001&prpCspecialFacs_%5B0%5D.taxRate=001&prpCspecialFacs_%5B0%5D.tax=001&prpCspecialFacs_%5B0%5D.othRate=001&prpCspecialFacs_%5B0%5D.commission=001&prpCspecialFacs_%5B0%5D.othPremium=001&prpCspecialFacs_%5B0%5D.reinsName=001&prpCspecialFacs_%5B0%5D.freinsName=001&prpCspecialFacs_%5B0%5D.payName=001&prpCspecialFacs_%5B0%5D.remark=001&prpCspecialFacs_%5B0%5D.flag=&hidden_index_specialFac=0&updateIndex=-1&iniCspecialFac_Flag=&_ReinsCode=&loadFlag8=&_FReinsCode=&_PayCode=&_ReinsName=&_FReinsName=&_PayName=&_CommRate=&_OthRate=&_ShareRate=&_Commission=&_OthPremium=&_SharePremium=&_TaxRate=&_Tax=&_Remark=&prpCsettlement.buyerUnitRank=3&prpCsettlement.buyerPreFee=565.3&prpCsettlement.buyerUnitCode=&prpCsettlement.buyerUnitName=&prpCsettlement.upperUnitCode=&upperUnitName=&prpCsettlement.buyerUnitAddress=&prpCsettlement.buyerLinker=&prpCsettlement.buyerPhone=&prpCsettlement.buyerMobile=&prpCsettlement.buyerFax=&prpCsettlement.buyerUnitNature=1&prpCsettlement.buyerProvince=11000000&buyerProvinceDes=%C8%CB%B1%A3%B2%C6%CF%D5%B1%B1%BE%A9%CA%D0%B7%D6%B9%AB%CB%BE&prpCsettlement.buyerBusinessSort=01&prpCsettlement.comCname=&prpCsettlement.linkerCode=&linkerName=&linkerPhone=&linkerMobile=&linkerFax=&prpCsettlement.comCode=&prpCsettlement.fundForm=1&prpCsettlement.flag=&settlement_Flag=&prpCcontriutions_%5B0%5D.id.serialNo=1&prpCcontriutions_%5B0%5D.contribType=F&prpCcontriutions_%5B0%5D.contribCode=&prpCcontriutions_%5B0%5D.contribName=&prpCcontriutions_%5B0%5D.contribCode_uni=&prpCcontriutions_%5B0%5D.contribPercent=&prpCcontriutions_%5B0%5D.contribPremium=&prpCcontriutions_%5B0%5D.remark=&hidden_index_ccontriutions=0&userCode=020083&iProposalNo=&CProposalNo=&timeFlag=&prpCremarks_%5B0%5D.id.proposalNo=&prpCremarks_%5B0%5D.id.serialNo=&prpCremarks_%5B0%5D.operatorCode=020083&prpCremarks_%5B0%5D.remark=&prpCremarks_%5B0%5D.flag=&prpCremarks_%5B0%5D.insertTimeForHis=&hidden_index_remark=0&ciInsureDemandCheckVo.demandNo=&ciInsureDemandCheckVo.checkQuestion=&ciInsureDemandCheckVo.checkAnswer=&ciInsureDemandCheckVo.flag=DEMAND&ciInsureDemandCheckVo.riskCode=&prpCitemKindCI.familyNo=1";
		//替换日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		limingparam = limingparam.replace("2016-05-23", sdf.format(new Date()));
		Map preMap =new HashMap();
		preMap.put("nextParams", limingparam);
		request.setRequestParam(preMap);//
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CALANCIINFO);// GET
		Response responseHebaoCalAnciInfo = hebaoCalAnciInfoPage.run(request);
		if(responseHebaoCalAnciInfo.getReturnCode()==SysConfigInfo.SUCCESS200){
			Map nextParamsMap  = responseHebaoCalAnciInfo.getResponseMap();
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
			request2.setRequestParam(response1.getResponseMap());
			Response response2 = heBaoSaveCheckAgentTypePage.run(request2);
			//保存3操作
			HebaoSaveQueryPayForPage hebaoSaveQueryPayForPage = new HebaoSaveQueryPayForPage(1);
			Request request3 =new Request();
			request3.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE3);
			request3.setRequestParam((Map)response2.getResponseMap().get("nextParams"));
			Response response3 = hebaoSaveQueryPayForPage.run(request3);
			//保存4操作
			HebaoSaveRefreshPlanByTimesPage hebaoSaveRefreshPlanByTimesPage = new HebaoSaveRefreshPlanByTimesPage(1);
			Request request4 =new Request();
			request4.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE4);
			request4.setRequestParam((Map)response3.getResponseMap().get("nextParams"));
			Response response4 = hebaoSaveRefreshPlanByTimesPage.run(request4);
			//保存5操作
			HeBaoSaveCheckBeforeSavePage heBaoSaveCheckBeforeSavePage = new HeBaoSaveCheckBeforeSavePage(1);
			Request request5 =new Request();
			request5.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE5);
			request5.setRequestParam((Map)response4.getResponseMap().get("nextParams"));
			Response response5 = heBaoSaveCheckBeforeSavePage.run(request5);
			//保存6操作
			HebaoSaveInsertPage hebaoSaveInsertPage = new HebaoSaveInsertPage(1);
			Request request6 =new Request();
			request6.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSAVE6);
			request6.setRequestParam((Map)response4.getResponseMap().get("nextParams"));
			Response response6 = hebaoSaveInsertPage.run(request6);

			//提交核保1操作
			HebaoCommitEditCheckFlagPage hebaoCommitEditCheckFlagPage = new HebaoCommitEditCheckFlagPage(1);
			Request requestCommit1 =new Request();
			requestCommit1.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOCOMMIT1);
			requestCommit1.setRequestParam((Map)response6.getResponseMap().get("nextParams"));
			Response responseCommit1 = hebaoCommitEditCheckFlagPage.run(requestCommit1);

			//提交核保2操作
			HebaoCommitEditSubmitUndwrtPage hebaoCommitEditSubmitUndwrtPage = new HebaoCommitEditSubmitUndwrtPage(1);
			Request requestCommit2 =new Request();
			requestCommit2.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOCOMMIT2);
			requestCommit2.setRequestParam((Map)response6.getResponseMap().get("nextParams"));
			Response responseCommit2 = hebaoCommitEditSubmitUndwrtPage.run(requestCommit2);
			//返回核保单号
			Map mapTDAA = (Map) response6.getResponseMap().get("nextParams");
			DAAno = mapTDAA.get("TDAA").toString();
			//返回关联保单号
			DZAno = mapTDAA.get("TDZA").toString();
		}else{
			logger.info("机器人抓取，获取辅助计算核保参数失败");
		}
		code = "DAAno = "+DAAno+",DZAno = "+DZAno;
		return code;
	}

	@Override
	public HebaoResponse  getHebaoResponse(String licenseNo) {
		HebaoResponse response = new HebaoResponse();
		//在缓存中拿出投保单号，并查询
		Map<String, String> noMap = CacheConstant.proposalNoInfo.get(licenseNo);
		String biNo = noMap.get("biNo");//商业险投保单号
		String ciNo = noMap.get("ciNo");//交强险投保单号
		HebaoSearchQueryUndwrtMsgPage hebaoSearchQueryUndwrtMsgPage =new HebaoSearchQueryUndwrtMsgPage(1);
		Request request3 =new Request();
		Map request3ParamMap = new HashMap();
		request3.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_HEBAOSEARCHUNDWRTMSG);
		request3ParamMap.put("bizType", "PROPOSAL");
		request3ParamMap.put("bizNo", biNo);
		request3.setRequestParam(request3ParamMap);
		Response response3 = new Response();
		Map<String, String> biMap = new HashMap<>();//商业险
		Map<String, String> ciMap = new HashMap<>();//交强险
		//执行查询 商业险
		if(StringUtils.isNoneBlank(biNo)){
			response3 = hebaoSearchQueryUndwrtMsgPage.run(request3);
			biMap = (Map)response3.getResponseMap().get("nextParams");
		}
		request3ParamMap.remove("bizNo");
		request3ParamMap.put("bizNoCI", ciNo);
		//执行查询 交强险
		if(StringUtils.isNoneBlank(ciNo)){
			response3 = hebaoSearchQueryUndwrtMsgPage.run(request3);
			ciMap = (Map)response3.getResponseMap().get("nextParams");
		}
		response.setSource(2);//人保
		response.setBizNo(biNo);
		response.setForceNo(ciNo);
		if(!biMap.isEmpty()){//判断商业险信息
			String msg = biMap.get("msg");
			logger.info("人保API，【核保信息查询：商业险】，licenseNo：" + licenseNo + "，biNo：" + biNo + "，msg：" + msg);
			if(msg.contains("不通过") || msg.contains("不成功") || msg.contains("未通过") || msg.contains("未成功")){//未通过
				response.setSubmitStatus(0);
				response.setSubmitResult(msg);
				return response;
			}
		}
		if(!ciMap.isEmpty()){//判断商业险信息
			String msg = ciMap.get("msg");
			logger.info("人保API，【核保信息查询：交强险】，licenseNo：" + licenseNo + "，ciNo：" + ciNo + "，msg：" + msg);
			if(msg.contains("不通过") || msg.contains("不成功") || msg.contains("未通过") || msg.contains("未成功")){//未通过
				response.setSubmitStatus(0);
				response.setSubmitResult(msg);
				return response;
			}
		}
		if(biMap.isEmpty() && ciMap.isEmpty()){//都未空的时候，审核中
			response.setSubmitStatus(3);
			response.setSubmitResult("核保中");
			return response;
		}
		response.setSubmitStatus(1);
		response.setSubmitResult("核保成功");
		return response ;
	}

	@Override
	public JSONObject getRenewalInfo(String licenseNo, int CityCode, String CustKey, String IsPublic) {
		//缓存是否是公车
		if(StringUtils.isBlank(IsPublic)){
			IsPublic = "0";
		}
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("IsPublic", IsPublic);//是否是公车：0 否、1 是
		CacheConstant.queryparam.put(licenseNo, paramMap);
		//TODO 这里可以做缓存
		JSONObject renewalInfoJson = new JSONObject();
		JSONObject UserInfo = new JSONObject();
		UserInfo.put("CarUsedType", 0);
		UserInfo.put("LicenseNo", licenseNo);
		UserInfo.put("CityCode", CityCode);
		try {
			CarInfoResponse result = this.getAllCarInfoByLicenseNo(licenseNo, CityCode);
			//车辆信息
			BaseCarInfoResponse carInfo = result.getCarInfoBaseResponse();
			JSONObject carInfoJson = (JSONObject) JSONObject.toJSON(carInfo);
			for (Map.Entry<String, Object> entry : carInfoJson.entrySet()) {
	            String key = entry.getKey();
	            key  = key.substring(0,1).toUpperCase() + key.substring(1);
	            Object value = entry.getValue();
	            if(key.contains("RegisterDate")){
	            	key = "RegisterDate";
	            }
	            if(key.contains("MoldName")){
	            	key = "ModleName";
	            }
	            if(key.contains("CarSeated")){
	            	key = "SeatCount";
	            }
	            UserInfo.put(key, value);
	        }
			//险种信息
			SaveQuoteResponse saveQuoteResponse = result.getSaveQuoteResponse();
			saveQuoteResponse.setSource(SysConfigInfo.PICC_FLAG);
			JSONObject saveQuoteObj = (JSONObject) JSONObject.toJSON(saveQuoteResponse);
			JSONObject SaveQuote = new JSONObject();
			for (Map.Entry<String, Object> entry : saveQuoteObj.entrySet()) {
	            String key = entry.getKey();
	            key  = key.substring(0,1).toUpperCase() + key.substring(1);
	            Object value = entry.getValue();
	            if(key.contains("Boli")){
	            	key = "BoLi";
	            }
	            SaveQuote.put(key, value);
	        }
			renewalInfoJson.put("SaveQuote", SaveQuote);
			renewalInfoJson.put("BusinessStatus", 1);
			renewalInfoJson.put("StatusMessage", "获取续保信息成功");
			//存入缓存，报价使用
			Map<String, Object> map1 = new HashMap<>();
			map1.put("reCiPolicyNo", carInfo.getCiPolicyNo());//上一年交强险投保单号
			map1.put("reBiPolicyNo", carInfo.getBiPolicyNo());//上一年商业险投保单号
			map1.put("ciEndDate", carInfo.getForceExpireDate());//上一年交强险结束日期
			map1.put("biEndDate", carInfo.getBusinessExpireDate());//上一年商业险结束日期  
			map1.put("identifyNumber", carInfo.getCredentislasNum());//车主身份证
			map1.put("mobile", "13520030193");//车主手机号
			map1.put("owner", carInfo.getLicenseOwner());//车主姓名
			CacheConstant.renewalInfo.put(licenseNo, map1);
		} catch (Exception e) {
			e.printStackTrace();
			//出现异常的时候，证明不是不是人保续保，只能返回车架号，发动机号，品牌型号，初等日期
			renewalInfoJson.put("BusinessStatus", 3);
			renewalInfoJson.put("StatusMessage", "获取续保信息失败");
			try {
				licenseNo =  java.net.URLEncoder.encode(licenseNo,   "gb2312");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			String param = "carShipTaxPlatFormFlag=1&randomProposalNo=7847409371464077318727 &initemKind_Flag=0&editType=NEW&bizType=PROPOSAL&ABflag=&isBICI=&prpCmain.renewalFlag=&activityFlag=0&INTEGRAL_SWITCH=0&GuangdongSysFlag=&GDREALTIMECARFlag=&GDREALTIMEMOTORFlag=&GDCANCIINFOFlag=0&prpCmain.checkFlag=&prpCmain.othFlag=&prpCmain.dmFlag=&prpCmainCI.dmFlag=&prpCmain.underWriteCode=&prpCmain.underWriteName=&prpCmain.underWriteEndDate=&prpCmain.underWriteFlag=0&prpCmainCI.checkFlag=&prpCmainCI.underWriteFlag=&bizNo=&applyNo=&oldPolicyNo=&bizNoBZ=&bizNoCI=&prpPhead.endorDate=&prpPhead.validDate=&prpPhead.comCode=&sumAmountBI=&isTaxDemand=1&cIInsureFlag=1&bIInsureFlag=1&ciInsureSwitchKindCode=E01,E11,E12,D01,D02,D03&ciInsureSwitchValues=1111111&cIInsureMotorFlag=1&mtPlatformTime=&noPermissionsCarKindCode=E12&isTaxFlag=&rePolicyNo=&oldPolicyType=&ZGRS_PURCHASEPRICE=200000&ZGRS_LOWESTPREMIUM=0&clauseFlag=&prpCinsuredOwn_Flag=&prpCinsuredDiv_Flag=&prpCinsuredBon_Flag=&relationType=&ciLimitDays=90&udFlag=&kbFlag=&sbFlag=&xzFlag=&userType=08&noNcheckFlag=0&planFlag=0&R_SWITCH=1&biStartDate=2016-05-25&ciStartDate=2016-05-25&ciStartHour=0&ciEndDate=2017-05-24&ciEndHour=24&AGENTSWITCH=1&JFCDSWITCH=19&carShipTaxFlag=11&commissionFlag=&ICCardCHeck=&riskWarningFlag=&comCodePrefix=11&DAGMobilePhoneNum=&scanSwitch=1000000000&haveScanFlag=0&diffDay=90&cylinderFlag=0&ciPlateVersion=&biPlateVersion=&criterionFlag=0&isQuotatonFlag=2&quotationRisk=DAA&getReplenishfactor=&useYear=9&FREEINSURANCEFLAG=011111&isMotoDrunkDriv=0&immediateFlag=0&immediateFlagCI=0&claimAmountReason=&isQueryCarModelFlag=&isDirectFee=&userCode=020083&comCode=11010286&chgProfitFlag=00&ciPlatTask=&biPlatTask=&upperCostRateBI=&upperCostRateCI=&rescueFundRate=&resureFundFee=&useCarshiptaxFlag=1&taxFreeLicenseNo=&isTaxFree=0&premiumChangeFlag=1&operationTimeStamp=2016-05-24 16:08:38&VEHICLEPLAT=&MOTORFASTTRACK=&motorFastTrack_flag=&MOTORFASTTRACK_INSUREDCODE=&currentDate=&vinModifyFlag=&addPolicyProjectCode=&isAddPolicy=0&commissionView=0&specialflag=&accountCheck=2&projectBak=&projectCodeBT=&projectCodeBTback=&checkTimeFlag=&checkUndwrt=0&carDamagedNum=&insurePayTimes=&claimAdjustValue=&operatorProjectCode=1-1326,2-1326,4-1326,5-1326&lossFlagKind=&chooseFlagCI=0&unitedSaleRelatioStr=&purchasePriceU=&countryNatureU=&insurancefee_reform=0&operateDateForFG=&prpCmainCommon.clauseIssue=1&amountFloat=30&vat_switch=1&BiLastPolicyFlag=&CiLastPolicyFlag=&CiLastEffectiveDate=&CiLastExpireDate=&benchMarkPremium=&BiLastEffectiveDate=&BiLastExpireDate=&lastTotalPremium=&purchasePriceUFlag=&startDateU=&endDateU=&biCiFlagU=&biCiFlagIsChange=&biCiDateIsChange=&switchFlag=0&relatedFlag=0&riskCode=DAA&prpCmain.riskCode=&riskName=&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&agentCodeValidType=&agentCodeValidValue=&agentCodeValidIPPer=&qualificationNo=201951000000800&qualificationName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&OLD_STARTDATE_CI=&OLD_ENDDATE_CI=&prpCmainCommon.greyList=&prpCmainCommon.image=&reinComPany=&reinPolicyNo=&reinStartDate=&reinEndDate=&prpCmain.proposalNo=&prpCmain.policyNo=&prpCmainCI.proposalNo=&prpCmainCI.policyNo=&prpPhead.applyNo=&prpPhead.endorseNo=&prpPheadCI.applyNo=&prpPheadCI.endorseNo=&prpCmain.comCode=11010286&comCodeDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.handler1Code=13154215  &handler1CodeDes=%BA%AB%B6%AB%D0%F1&homePhone=15801381299&officePhone=15801381299&moblie=&checkHandler1Code=1&handler1CodeDesFlag=A&handler1Info=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR_15801381299_FIELD_SEPARATOR__FIELD_SEPARATOR_A_FIELD_SEPARATOR_1211010268&prpCmainCommon.handler1code_uni=1211010268&prpCmain.handlerCode=13154215  &handlerCodeDes=%BA%AB%B6%AB%D0%F1&homePhonebak=&officePhonebak=&mobliebak=&handler1CodeDesFlagbak=&prpCmainCommon.handlercode_uni=1211010268&handlerInfo=13154215_FIELD_SEPARATOR_%BA%AB%B6%AB%D0%F1_FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR__FIELD_SEPARATOR_1211010268&prpCmain.businessNature=2&businessNatureTranslation=%D7%A8%D2%B5%B4%FA%C0%ED%D2%B5%CE%F1&prpCmain.agentCode=110021100065&prpCmainagentName=%B1%B1%BE%A9%D6%DA%BA%CF%CB%C4%BA%A3%B1%A3%CF%D5%B4%FA%C0%ED%D3%D0%CF%DE%B9%AB%CB%BE&agentType=211047&agentCode=110021100065&tempAgentCode=211047&sumPremiumChgFlag=0&prpCmain.sumPremium1=0&sumPayTax1=0&prpCmain.contractNo=&prpCmain.operateDate=2016-05-24&Today=2016-05-24&OperateDate=2016-06-24&prpCmain.makeCom=11010286&makeComDes=%B1%B1%BE%A9%CA%D0%CE%F7%B3%C7%D6%A7%B9%AB%CB%BE%D6%D0%BD%E9%D2%B5%CE%F1%B6%FE%B2%BF&prpCmain.startDate=2016-05-25&prpCmain.startHour=0&prpCmain.endDate=2017-05-24&prpCmain.endHour=24&prpCmain.checkUpCode=&prpCmainCI.startDate=2016-05-25&prpCmainCI.startHour=0&prpCmainCI.endDate=2017-05-24&prpCmainCI.endHour=24&carPremium=0.0&insuredChangeFlag=0&refreshEadFlag=1&imageAdjustPixels=20&prpBatchVehicle.id.contractNo=&prpBatchVehicle.id.serialNo=&prpBatchVehicle.motorCadeNo=&prpBatchVehicle.licenseNo=&prpBatchVehicle.licenseType=&prpBatchVehicle.carKindCode=&prpBatchVehicle.proposalNo=&prpBatchVehicle.policyNo=&prpBatchVehicle.sumAmount=&prpBatchVehicle.sumPremium=&prpBatchVehicle.prpProjectCode=&prpBatchVehicle.coinsProjectCode=&prpBatchVehicle.profitProjectCode=&prpBatchVehicle.facProjectCode=&prpBatchVehicle.flag=&prpBatchVehicle.carId=&prpBatchVehicle.versionNo=&prpBatchMain.discountmode=&minusFlag=&paramIndex=&batchCIFlag=&batchBIFlag=&pageEndorRecorder.endorFlags=&endorDateEdit=&validDateEdit=&endDateEdit=&endorType=&prpPhead.endorType=&generatePtextFlag=0&generatePtextAgainFlag=0&quotationNo=&quotationFlag=&customerCode=&customerFlag=&compensateNo=&dilutiveType=&prpCfixationTemp.discount=&prpCfixationTemp.id.riskCode=&prpCfixationTemp.profits=&prpCfixationTemp.cost=&prpCfixationTemp.taxorAppend=&prpCfixationTemp.payMentR=&prpCfixationTemp.basePayMentR=&prpCfixationTemp.poundAge=&prpCfixationTemp.basePremium=&prpCfixationTemp.riskPremium=&prpCfixationTemp.riskSumPremium=&prpCfixationTemp.signPremium=&prpCfixationTemp.isQuotation=&prpCfixationTemp.riskClass=&prpCfixationTemp.operationInfo=&prpCfixationTemp.realDisCount=&prpCfixationTemp.realProfits=&prpCfixationTemp.realPayMentR=&prpCfixationTemp.remark=&prpCfixationTemp.responseCode=&prpCfixationTemp.errorMessage=&prpCfixationTemp.profitClass=&prpCfixationTemp.costRate=&prpCfixationCITemp.discount=&prpCfixationCITemp.id.riskCode=&prpCfixationCITemp.profits=&prpCfixationCITemp.cost=&prpCfixationCITemp.taxorAppend=&prpCfixationCITemp.payMentR=&prpCfixationCITemp.basePayMentR=&prpCfixationCITemp.poundAge=&prpCfixationCITemp.basePremium=&prpCfixationCITemp.riskPremium=&prpCfixationCITemp.riskSumPremium=&prpCfixationCITemp.signPremium=&prpCfixationCITemp.isQuotation=&prpCfixationCITemp.riskClass=&prpCfixationCITemp.operationInfo=&prpCfixationCITemp.realDisCount=&prpCfixationCITemp.realProfits=&prpCfixationCITemp.realPayMentR=&prpCfixationCITemp.remark=&prpCfixationCITemp.responseCode=&prpCfixationCITemp.errorMessage=&prpCfixationCITemp.profitClass=&prpCfixationCITemp.costRate=&prpCsalesFixes_%5B0%5D.id.proposalNo=&prpCsalesFixes_%5B0%5D.id.serialNo=&prpCsalesFixes_%5B0%5D.comCode=&prpCsalesFixes_%5B0%5D.businessNature=&prpCsalesFixes_%5B0%5D.riskCode=&prpCsalesFixes_%5B0%5D.version=&prpCsalesFixes_%5B0%5D.isForMal=&IS_LOAN_MODIFY=0&kindAndAmount=&isSpecialFlag=&specialEngage=&licenseNoCar=&prpCitemCar.carLoanFlag=&carModelPlatFlag=&updateQuotation=&prpCitemCar.licenseNo1=&prpCitemCar.monopolyFlag=0&prpCitemCar.monopolyCode=&prpCitemCar.monopolyName=&queryCarModelInfo=%B3%B5%D0%CD%D0%C5%CF%A2%C6%BD%CC%A8%BD%BB%BB%A5&prpCitemCar.id.itemNo=1&oldClauseType=&prpCitemCar.actualValue=&prpCitemCar.carId=&prpCitemCar.versionNo=&prpCmainCar.newDeviceFlag=&prpCitemCar.otherNature=&prpCitemCar.flag=&newCarFlagValue=2&prpCitemCar.discountType=&prpCitemCar.colorCode=&prpCitemCar.safeDevice=&prpCitemCar.coefficient1=&prpCitemCar.coefficient2=&prpCitemCar.coefficient3=&prpCitemCar.startSiteName=&prpCitemCar.endSiteName=&prpCmainCommon.netsales=0&prpCitemCar.newCarFlag=0&prpCitemCar.noNlocalFlag=0&prpCitemCar.licenseFlag=1&prpCitemCar.licenseNo=" + licenseNo + "&codeLicenseType=LicenseType01,04,LicenseType02,01,LicenseType03,02,LicenseType04,02,LicenseType05,02,LicenseType06,02,LicenseType07,04,LicenseType08,04,LicenseType09,01,LicenseType10,01,LicenseType11,01,LicenseType12,01,LicenseType13,04,LicenseType14,04,LicenseType15,04,	LicenseType16,04,LicenseType17,04,LicenseType18,01,LicenseType19,01,LicenseType20,01,LicenseType21,01,LicenseType22,01,LicenseType23,03,LicenseType24,01,LicenseType25,01,LicenseType31,03,LicenseType32,03,LicenseType90,02&prpCitemCar.licenseType=02 &LicenseTypeDes=%D0%A1%D0%CD%C6%FB%B3%B5%BA%C5%C5%C6&prpCitemCar.licenseColorCode=01&LicenseColorCodeDes=%C0%B6&prpCitemCar.engineNo=&prpCitemCar.vinNo=&prpCitemCar.frameNo=&prpCitemCar.carKindCode=A01&CarKindCodeDes=%BF%CD%B3%B5&carKindCodeBak=A01&prpCitemCar.useNatureCode=211&useNatureCodeBak=211&useNatureCodeTrue=211&prpCitemCar.clauseType=F42&clauseTypeBak=F42&prpCitemCar.enrollDate=&enrollDateTrue=&prpCitemCar.useYears=&prpCitemCar.runMiles=&taxAbateForPlat=&taxAbateForPlatCarModel=&prpCitemCar.modelDemandNo=&owner=&prpCitemCar.remark=&prpCitemCar.modelCode=&prpCitemCar.brandName=&PurchasePriceScal=10&prpCitemCar.purchasePrice=&CarActualValueTrue=&CarActualValueTrue1=&SZpurchasePriceUp=&SZpurchasePriceDown=&purchasePriceF48=200000&purchasePriceUp=100&purchasePriceDown=&purchasePriceOld=&vehiclePricer=&prpCitemCar.tonCount=0&prpCitemCar.exhaustScale=&prpCitemCar.seatCount=&seatCountTrue=&prpCitemCar.runAreaCode=11&prpCitemCar.carInsuredRelation=1&prpCitemCar.countryNature=01&prpCitemCar.cylinderCount=&prpCitemCar.loanVehicleFlag=0&prpCitemCar.transferVehicleFlag=0&prpCitemCar.transferDate=&prpCitemCar.modelCodeAlias=&prpCitemCar.carLotEquQuality=&isQuotation=1&prpCitemCar.fuelType=A&prpCitemCar.carProofType=01&prpCitemCar.isDropinVisitInsure=0&prpCitemCar.energyType=0&prpCitemCar.carProofNo=&prpCitemCar.carProofDate=&prpCmainChannel.assetAgentName=&prpCmainChannel.assetAgentCode=&prpCmainChannel.assetAgentPhone=&SYFlag=0&MTFlag=0&BMFlag=0&STFlag=0&prpCcarDevices_%5B0%5D.deviceName=&prpCcarDevices_%5B0%5D.id.itemNo=1&prpCcarDevices_%5B0%5D.id.proposalNo=&prpCcarDevices_%5B0%5D.id.serialNo=&prpCcarDevices_%5B0%5D.flag=&prpCcarDevices_%5B0%5D.quantity=&prpCcarDevices_%5B0%5D.purchasePrice=&prpCcarDevices_%5B0%5D.buyDate=&prpCcarDevices_%5B0%5D.actualValue=&hidden_index_citemcar=0&editFlag=1&prpCmainCommon.ext2=&configedRepeatTimesLocal=5&prpCinsureds_%5B0%5D.insuredFlag=1&iinsuredFlag=001&iinsuredType=001&iinsuredCode=001&iinsuredName=001&iunitType=001&iidentifyType=001&iidentifyNumber=001&iinsuredAddress=001&iemail=001&iphoneNumber=001&prpCinsureds_%5B0%5D.id.serialNo=1&prpCinsureds_%5B0%5D.insuredType=1&prpCinsureds_%5B0%5D.insuredNature=1&prpCinsureds_%5B0%5D.insuredCode=001&prpCinsureds_%5B0%5D.insuredName=1&prpCinsureds_%5B0%5D.unitType=1&prpCinsureds_%5B0%5D.identifyType=1&prpCinsureds_%5B0%5D.identifyNumber=1&prpCinsureds_%5B0%5D.insuredAddress=1&prpCinsureds_%5B0%5D.email=1&prpCinsureds_%5B0%5D.phoneNumber=1&prpCinsureds_%5B0%5D.drivingYears=&prpCinsureds_%5B0%5D.mobile=1&prpCinsureds_%5B0%5D.postCode=1&prpCinsureds_%5B0%5D.versionNo=1&prpCinsureds_%5B0%5D.auditStatus=1&prpCinsureds_%5B0%5D.sex=1&prpCinsureds_%5B0%5D.countryCode=1&prpCinsureds_%5B0%5D.flag=&prpCinsureds_%5B0%5D.age=&prpCinsureds_%5B0%5D.drivingLicenseNo=&prpCinsureds_%5B0%5D.drivingCarType=&prpCinsureds_%5B0%5D.appendPrintName=&prpCinsureds_%5B0%5D.causetroubleTimes=&prpCinsureds_%5B0%5D.acceptLicenseDate=&isCheckRepeat_%5B0%5D=&configedRepeatTimes_%5B0%5D=&repeatTimes_%5B0%5D=&prpCinsureds_%5B0%5D.unifiedSocialCreditCode=&idCardCheckInfo_%5B0%5D.insuredcode=&idCardCheckInfo_%5B0%5D.insuredFlag=&idCardCheckInfo_%5B0%5D.mobile=&idCardCheckInfo_%5B0%5D.idcardCode=&idCardCheckInfo_%5B0%5D.name=&idCardCheckInfo_%5B0%5D.nation=&idCardCheckInfo_%5B0%5D.birthday=&idCardCheckInfo_%5B0%5D.sex=&idCardCheckInfo_%5B0%5D.address=&idCardCheckInfo_%5B0%5D.issure=&idCardCheckInfo_%5B0%5D.validStartDate=&idCardCheckInfo_%5B0%5D.validEndDate=&idCardCheckInfo_%5B0%5D.samCode=&idCardCheckInfo_%5B0%5D.samType=&idCardCheckInfo_%5B0%5D.flag=&imobile=001&iauditStatus=001&iversionNo=001&hidden_index_insured=0&_insuredFlag_hide=%CD%B6%B1%A3%C8%CB&_insuredFlag_hide=%B1%BB%B1%A3%CF%D5%C8%CB&_insuredFlag_hide=%B3%B5%D6%F7&_insuredFlag_hide=%D6%B8%B6%A8%BC%DD%CA%BB%C8%CB&_insuredFlag_hide=%CA%DC%D2%E6%C8%CB&_insuredFlag_hide=%B8%DB%B0%C4%B3%B5%B3%B5%D6%F7&_insuredFlag_hide=%C1%AA%CF%B5%C8%CB&_insuredFlag=0&_insuredFlag_hide=%CE%AF%CD%D0%C8%CB&_resident=&_insuredType=1&_insuredCode=&_insuredName=&customerURL=http://10.134.136.48:8300/cif&_isCheckRepeat=&_configedRepeatTimes=&_repeatTimes=&_identifyType=01&_identifyNumber=&_unifiedSocialCreditCode=&_mobile=&_mobile1=&_sex=0&_age=&_drivingYears=&_countryCode=CHN&_insuredAddress=&_postCode=&_appendPrintName=&group_code=&_auditStatus=&_auditStatusDes=&_versionNo=&_drivingLicenseNo=&_email=&idCardCheckInfo.idcardCode=&idCardCheckInfo.name=&idCardCheckInfo.nation=&idCardCheckInfo.birthday=&idCardCheckInfo.sex=&idCardCheckInfo.address=&idCardCheckInfo.issure=&idCardCheckInfo.validStartDate=&idCardCheckInfo.validEndDate=&idCardCheckInfo.samCode=&idCardCheckInfo.samType=&idCardCheckInfo.flag=0&_drivingCarType=&CarKindLicense=&_causetroubleTimes=&_acceptLicenseDate=&prpCmainCar.agreeDriverFlag=&updateIndex=-1&prpBatchProposal.profitType=&motorFastTrack_Amount=&insurancefee_reform=0&prpCmainCommon.clauseIssue=1&prpCprofitDetailsTemp_%5B0%5D.chooseFlag=on&prpCprofitDetailsTemp_%5B0%5D.profitName=&prpCprofitDetailsTemp_%5B0%5D.condition=&profitRateTemp_%5B0%5D=&prpCprofitDetailsTemp_%5B0%5D.profitRate=&prpCprofitDetailsTemp_%5B0%5D.profitRateMin=&prpCprofitDetailsTemp_%5B0%5D.profitRateMax=&prpCprofitDetailsTemp_%5B0%5D.id.proposalNo=&prpCprofitDetailsTemp_%5B0%5D.id.itemKindNo=&prpCprofitDetailsTemp_%5B0%5D.id.profitCode=&prpCprofitDetailsTemp_%5B0%5D.id.serialNo=1&prpCprofitDetailsTemp_%5B0%5D.id.profitType=&prpCprofitDetailsTemp_%5B0%5D.kindCode=&prpCprofitDetailsTemp_%5B0%5D.conditionCode=&prpCprofitDetailsTemp_%5B0%5D.flag=&prpCprofitFactorsTemp_%5B0%5D.chooseFlag=on&serialNo_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.profitName=&prpCprofitFactorsTemp_%5B0%5D.condition=&rateTemp_%5B0%5D=&prpCprofitFactorsTemp_%5B0%5D.rate=&prpCprofitFactorsTemp_%5B0%5D.lowerRate=&prpCprofitFactorsTemp_%5B0%5D.upperRate=&prpCprofitFactorsTemp_%5B0%5D.id.profitCode=&prpCprofitFactorsTemp_%5B0%5D.id.conditionCode=&prpCprofitFactorsTemp_%5B0%5D.flag=&prpCitemKind.shortRateFlag=2&prpCitemKind.shortRate=100&prpCitemKind.currency=CNY&prpCmainCommon.groupFlag=0&sumBenchPremium=&prpCmain.discount=&prpCmain.sumPremium=&premiumF48=5000&prpCmain.sumNetPremium=&prpCmain.sumTaxPremium=&passengersSwitchFlag=&prpCitemKindsTemp%5B0%5D.min=&prpCitemKindsTemp%5B0%5D.max=&prpCitemKindsTemp%5B0%5D.itemKindNo=&prpCitemKindsTemp%5B0%5D.clauseCode=050002&prpCitemKindsTemp%5B0%5D.kindCode=050200&prpCitemKindsTemp%5B0%5D.kindName=%BB%FA%B6%AF%B3%B5%CB%F0%CA%A7%B1%A3%CF%D5&prpCitemKindsTemp%5B0%5D.unitAmount=&prpCitemKindsTemp%5B0%5D.quantity=&prpCitemKindsTemp%5B0%5D.amount=&prpCitemKindsTemp%5B0%5D.calculateFlag=Y11Y000&prpCitemKindsTemp%5B0%5D.startDate=&prpCitemKindsTemp%5B0%5D.startHour=&prpCitemKindsTemp%5B0%5D.endDate=&prpCitemKindsTemp%5B0%5D.endHour=&relateSpecial%5B0%5D=050911&coachCar%5B0%5D=050941&prpCitemKindsTemp%5B0%5D.flag= 100000&prpCitemKindsTemp%5B0%5D.basePremium=&prpCitemKindsTemp%5B0%5D.rate=&prpCitemKindsTemp%5B0%5D.benchMarkPremium=&prpCitemKindsTemp%5B0%5D.disCount=&prpCitemKindsTemp%5B0%5D.premium=&prpCitemKindsTemp%5B0%5D.netPremium=&prpCitemKindsTemp%5B0%5D.taxPremium=&prpCitemKindsTemp%5B0%5D.taxRate=&prpCitemKindsTemp%5B0%5D.dutyFlag=&prpCitemKindsTemp%5B1%5D.min=&prpCitemKindsTemp%5B1%5D.max=&prpCitemKindsTemp%5B1%5D.itemKindNo=&prpCitemKindsTemp%5B1%5D.clauseCode=050005&prpCitemKindsTemp%5B1%5D.kindCode=050500&prpCitemKindsTemp%5B1%5D.kindName=%B5%C1%C7%C0%CF%D5&prpCitemKindsTemp%5B1%5D.unitAmount=&prpCitemKindsTemp%5B1%5D.quantity=&prpCitemKindsTemp%5B1%5D.amount=&prpCitemKindsTemp%5B1%5D.calculateFlag=N11Y000&prpCitemKindsTemp%5B1%5D.startDate=&prpCitemKindsTemp%5B1%5D.startHour=&prpCitemKindsTemp%5B1%5D.endDate=&prpCitemKindsTemp%5B1%5D.endHour=&relateSpecial%5B1%5D=050921&coachCar%5B1%5D=&prpCitemKindsTemp%5B1%5D.flag= 100000&prpCitemKindsTemp%5B1%5D.basePremium=&prpCitemKindsTemp%5B1%5D.rate=&prpCitemKindsTemp%5B1%5D.benchMarkPremium=&prpCitemKindsTemp%5B1%5D.disCount=&prpCitemKindsTemp%5B1%5D.premium=&prpCitemKindsTemp%5B1%5D.netPremium=&prpCitemKindsTemp%5B1%5D.taxPremium=&prpCitemKindsTemp%5B1%5D.taxRate=&prpCitemKindsTemp%5B1%5D.dutyFlag=&prpCitemKindsTemp%5B2%5D.min=&prpCitemKindsTemp%5B2%5D.max=&prpCitemKindsTemp%5B2%5D.itemKindNo=&prpCitemKindsTemp%5B2%5D.clauseCode=050003&prpCitemKindsTemp%5B2%5D.kindCode=050600&prpCitemKindsTemp%5B2%5D.kindName=%B5%DA%C8%FD%D5%DF%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B2%5D.unitAmount=&prpCitemKindsTemp%5B2%5D.quantity=&prpCitemKindsTemp%5B2%5D.amount=&prpCitemKindsTemp%5B2%5D.calculateFlag=Y21Y000&prpCitemKindsTemp%5B2%5D.startDate=&prpCitemKindsTemp%5B2%5D.startHour=&prpCitemKindsTemp%5B2%5D.endDate=&prpCitemKindsTemp%5B2%5D.endHour=&relateSpecial%5B2%5D=050912&coachCar%5B2%5D=050942&prpCitemKindsTemp%5B2%5D.flag= 100000&prpCitemKindsTemp%5B2%5D.basePremium=&prpCitemKindsTemp%5B2%5D.rate=&prpCitemKindsTemp%5B2%5D.benchMarkPremium=&prpCitemKindsTemp%5B2%5D.disCount=&prpCitemKindsTemp%5B2%5D.premium=&prpCitemKindsTemp%5B2%5D.netPremium=&prpCitemKindsTemp%5B2%5D.taxPremium=&prpCitemKindsTemp%5B2%5D.taxRate=&prpCitemKindsTemp%5B2%5D.dutyFlag=&prpCitemKindsTemp%5B3%5D.min=&prpCitemKindsTemp%5B3%5D.max=&prpCitemKindsTemp%5B3%5D.itemKindNo=&prpCitemKindsTemp%5B3%5D.clauseCode=050004&prpCitemKindsTemp%5B3%5D.kindCode=050701&prpCitemKindsTemp%5B3%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%CB%BE%BB%FA%A3%A9&prpCitemKindsTemp%5B3%5D.unitAmount=&prpCitemKindsTemp%5B3%5D.quantity=&prpCitemKindsTemp%5B3%5D.amount=&prpCitemKindsTemp%5B3%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B3%5D.startDate=&prpCitemKindsTemp%5B3%5D.startHour=&prpCitemKindsTemp%5B3%5D.endDate=&prpCitemKindsTemp%5B3%5D.endHour=&relateSpecial%5B3%5D=050928&coachCar%5B3%5D=050943&prpCitemKindsTemp%5B3%5D.flag= 100000&prpCitemKindsTemp%5B3%5D.basePremium=&prpCitemKindsTemp%5B3%5D.rate=&prpCitemKindsTemp%5B3%5D.benchMarkPremium=&prpCitemKindsTemp%5B3%5D.disCount=&prpCitemKindsTemp%5B3%5D.premium=&prpCitemKindsTemp%5B3%5D.netPremium=&prpCitemKindsTemp%5B3%5D.taxPremium=&prpCitemKindsTemp%5B3%5D.taxRate=&prpCitemKindsTemp%5B3%5D.dutyFlag=&prpCitemKindsTemp%5B4%5D.min=&prpCitemKindsTemp%5B4%5D.max=&prpCitemKindsTemp%5B4%5D.itemKindNo=&prpCitemKindsTemp%5B4%5D.clauseCode=050004&prpCitemKindsTemp%5B4%5D.kindCode=050702&prpCitemKindsTemp%5B4%5D.kindName=%B3%B5%C9%CF%C8%CB%D4%B1%D4%F0%C8%CE%CF%D5%A3%A8%B3%CB%BF%CD%A3%A9&prpCitemKindsTemp%5B4%5D.unitAmount=&prpCitemKindsTemp%5B4%5D.quantity=&prpCitemKindsTemp%5B4%5D.amount=&prpCitemKindsTemp%5B4%5D.calculateFlag=Y21Y00&prpCitemKindsTemp%5B4%5D.startDate=&prpCitemKindsTemp%5B4%5D.startHour=&prpCitemKindsTemp%5B4%5D.endDate=&prpCitemKindsTemp%5B4%5D.endHour=&relateSpecial%5B4%5D=050929&coachCar%5B4%5D=050944&prpCitemKindsTemp%5B4%5D.flag= 100000&prpCitemKindsTemp%5B4%5D.basePremium=&prpCitemKindsTemp%5B4%5D.rate=&prpCitemKindsTemp%5B4%5D.benchMarkPremium=&prpCitemKindsTemp%5B4%5D.disCount=&prpCitemKindsTemp%5B4%5D.premium=&prpCitemKindsTemp%5B4%5D.netPremium=&prpCitemKindsTemp%5B4%5D.taxPremium=&prpCitemKindsTemp%5B4%5D.taxRate=&prpCitemKindsTemp%5B4%5D.dutyFlag=&prpCitemKindsTemp%5B5%5D.min=&prpCitemKindsTemp%5B5%5D.max=&prpCitemKindsTemp%5B5%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B5%5D.clauseCode=050006&prpCitemKindsTemp%5B5%5D.kindCode=050210&relateSpecial%5B5%5D=050922&prpCitemKindsTemp%5B5%5D.kindName=%B3%B5%C9%ED%BB%AE%BA%DB%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B5%5D.amount=2000.00&prpCitemKindsTemp%5B5%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B5%5D.startDate=&prpCitemKindsTemp%5B5%5D.startHour=&prpCitemKindsTemp%5B5%5D.endDate=&prpCitemKindsTemp%5B5%5D.endHour=&prpCitemKindsTemp%5B5%5D.flag= 200000&prpCitemKindsTemp%5B5%5D.basePremium=&prpCitemKindsTemp%5B5%5D.rate=&prpCitemKindsTemp%5B5%5D.benchMarkPremium=&prpCitemKindsTemp%5B5%5D.disCount=&prpCitemKindsTemp%5B5%5D.premium=&prpCitemKindsTemp%5B5%5D.netPremium=&prpCitemKindsTemp%5B5%5D.taxPremium=&prpCitemKindsTemp%5B5%5D.taxRate=&prpCitemKindsTemp%5B5%5D.dutyFlag=&prpCitemKindsTemp%5B6%5D.min=&prpCitemKindsTemp%5B6%5D.max=&prpCitemKindsTemp%5B6%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B6%5D.clauseCode=050008&prpCitemKindsTemp%5B6%5D.kindCode=050231&relateSpecial%5B6%5D=      &prpCitemKindsTemp%5B6%5D.kindName=%B2%A3%C1%A7%B5%A5%B6%C0%C6%C6%CB%E9%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B6%5D.modeCode=10&prpCitemKindsTemp%5B6%5D.amount=&prpCitemKindsTemp%5B6%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B6%5D.startDate=&prpCitemKindsTemp%5B6%5D.startHour=&prpCitemKindsTemp%5B6%5D.endDate=&prpCitemKindsTemp%5B6%5D.endHour=&prpCitemKindsTemp%5B6%5D.flag= 200000&prpCitemKindsTemp%5B6%5D.basePremium=&prpCitemKindsTemp%5B6%5D.rate=&prpCitemKindsTemp%5B6%5D.benchMarkPremium=&prpCitemKindsTemp%5B6%5D.disCount=&prpCitemKindsTemp%5B6%5D.premium=&prpCitemKindsTemp%5B6%5D.netPremium=&prpCitemKindsTemp%5B6%5D.taxPremium=&prpCitemKindsTemp%5B6%5D.taxRate=&prpCitemKindsTemp%5B6%5D.dutyFlag=&prpCitemKindsTemp%5B7%5D.min=&prpCitemKindsTemp%5B7%5D.max=&prpCitemKindsTemp%5B7%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B7%5D.clauseCode=050016&prpCitemKindsTemp%5B7%5D.kindCode=050310&relateSpecial%5B7%5D=      &prpCitemKindsTemp%5B7%5D.kindName=%D7%D4%C8%BC%CB%F0%CA%A7%CF%D5%CC%F5%BF%EE&prpCitemKindsTemp%5B7%5D.amount=&prpCitemKindsTemp%5B7%5D.calculateFlag=N12Y000&prpCitemKindsTemp%5B7%5D.startDate=&prpCitemKindsTemp%5B7%5D.startHour=&prpCitemKindsTemp%5B7%5D.endDate=&prpCitemKindsTemp%5B7%5D.endHour=&prpCitemKindsTemp%5B7%5D.flag= 200000&prpCitemKindsTemp%5B7%5D.basePremium=&prpCitemKindsTemp%5B7%5D.rate=&prpCitemKindsTemp%5B7%5D.benchMarkPremium=&prpCitemKindsTemp%5B7%5D.disCount=&prpCitemKindsTemp%5B7%5D.premium=&prpCitemKindsTemp%5B7%5D.netPremium=&prpCitemKindsTemp%5B7%5D.taxPremium=&prpCitemKindsTemp%5B7%5D.taxRate=&prpCitemKindsTemp%5B7%5D.dutyFlag=&prpCitemKindsTemp%5B8%5D.min=&prpCitemKindsTemp%5B8%5D.max=&prpCitemKindsTemp%5B8%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B8%5D.clauseCode=050021&prpCitemKindsTemp%5B8%5D.kindCode=050370&relateSpecial%5B8%5D=      &prpCitemKindsTemp%5B8%5D.kindName=%D4%BC%B6%A8%C7%F8%D3%F2%CD%A8%D0%D0%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B8%5D.modeCode=1&prpCitemKindsTemp%5B8%5D.amount=5000.00&prpCitemKindsTemp%5B8%5D.calculateFlag=N12N000&prpCitemKindsTemp%5B8%5D.startDate=&prpCitemKindsTemp%5B8%5D.startHour=&prpCitemKindsTemp%5B8%5D.endDate=&prpCitemKindsTemp%5B8%5D.endHour=&prpCitemKindsTemp%5B8%5D.flag= 200000&prpCitemKindsTemp%5B8%5D.basePremium=&prpCitemKindsTemp%5B8%5D.rate=&prpCitemKindsTemp%5B8%5D.benchMarkPremium=&prpCitemKindsTemp%5B8%5D.disCount=&prpCitemKindsTemp%5B8%5D.premium=&prpCitemKindsTemp%5B8%5D.netPremium=&prpCitemKindsTemp%5B8%5D.taxPremium=&prpCitemKindsTemp%5B8%5D.taxRate=&prpCitemKindsTemp%5B8%5D.dutyFlag=&prpCitemKindsTemp%5B9%5D.min=&prpCitemKindsTemp%5B9%5D.max=&prpCitemKindsTemp%5B9%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B9%5D.clauseCode=050032&prpCitemKindsTemp%5B9%5D.kindCode=050611&relateSpecial%5B9%5D=      &prpCitemKindsTemp%5B9%5D.kindName=%B7%A8%C2%C9%B7%D1%D3%C3%CC%D8%D4%BC%CC%F5%BF%EE&prpCitemKindsTemp%5B9%5D.amount=10000.00&prpCitemKindsTemp%5B9%5D.calculateFlag=N22N000&prpCitemKindsTemp%5B9%5D.startDate=&prpCitemKindsTemp%5B9%5D.startHour=&prpCitemKindsTemp%5B9%5D.endDate=&prpCitemKindsTemp%5B9%5D.endHour=&prpCitemKindsTemp%5B9%5D.flag= 200000&prpCitemKindsTemp%5B9%5D.basePremium=&prpCitemKindsTemp%5B9%5D.rate=&prpCitemKindsTemp%5B9%5D.benchMarkPremium=&prpCitemKindsTemp%5B9%5D.disCount=&prpCitemKindsTemp%5B9%5D.premium=&prpCitemKindsTemp%5B9%5D.netPremium=&prpCitemKindsTemp%5B9%5D.taxPremium=&prpCitemKindsTemp%5B9%5D.taxRate=&prpCitemKindsTemp%5B9%5D.dutyFlag=&prpCitemKindsTemp%5B10%5D.min=&prpCitemKindsTemp%5B10%5D.max=&prpCitemKindsTemp%5B10%5D.itemKindNo=&kindcodesub=&prpCitemKindsTemp%5B10%5D.clauseCode=050033&prpCitemKindsTemp%5B10%5D.kindCode=050630&relateSpecial%5B10%5D=050926&prpCitemKindsTemp%5B10%5D.kindName=%B8%BD%BC%D3%D3%CD%CE%DB%CE%DB%C8%BE%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindsTemp%5B10%5D.amount=50000.00&prpCitemKindsTemp%5B10%5D.calculateFlag=N32Y000&prpCitemKindsTemp%5B10%5D.startDate=&prpCitemKindsTemp%5B10%5D.startHour=&prpCitemKindsTemp%5B10%5D.endDate=&prpCitemKindsTemp%5B10%5D.endHour=&prpCitemKindsTemp%5B10%5D.flag= 200000&prpCitemKindsTemp%5B10%5D.basePremium=&prpCitemKindsTemp%5B10%5D.rate=&prpCitemKindsTemp%5B10%5D.benchMarkPremium=&prpCitemKindsTemp%5B10%5D.disCount=&prpCitemKindsTemp%5B10%5D.premium=&prpCitemKindsTemp%5B10%5D.netPremium=&prpCitemKindsTemp%5B10%5D.taxPremium=&prpCitemKindsTemp%5B10%5D.taxRate=&prpCitemKindsTemp%5B10%5D.dutyFlag=&prpCitemKindsTemp.itemKindSpecialSumPremium=&hidden_index_itemKind=11&hidden_index_profitDetial=0&prpCitemKindsTemp_%5B0%5D.chooseFlag=on&prpCitemKindsTemp_%5B0%5D.itemKindNo=&prpCitemKindsTemp_%5B0%5D.startDate=&prpCitemKindsTemp_%5B0%5D.kindCode=&prpCitemKindsTemp_%5B0%5D.kindName=&prpCitemKindsTemp_%5B0%5D.startHour=&prpCitemKindsTemp_%5B0%5D.endDate=&prpCitemKindsTemp_%5B0%5D.endHour=&prpCitemKindsTemp_%5B0%5D.calculateFlag=&relateSpecial_%5B0%5D=&prpCitemKindsTemp_%5B0%5D.flag=&prpCitemKindsTemp_%5B0%5D.basePremium=&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.rate=&prpCitemKindsTemp_%5B0%5D.benchMarkPremium=&prpCitemKindsTemp_%5B0%5D.disCount=&prpCitemKindsTemp_%5B0%5D.premium=&prpCitemKindsTemp_%5B0%5D.netPremium=&prpCitemKindsTemp_%5B0%5D.taxPremium=&prpCitemKindsTemp_%5B0%5D.taxRate=&prpCitemKindsTemp_%5B0%5D.dutyFlag=&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.value=&prpCitemKindsTemp_%5B0%5D.value=50&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=&prpCitemKindsTemp_%5B0%5D.modeCode=10&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.modeCode=1&prpCitemKindsTemp_%5B0%5D.value=1000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=2000&prpCitemKindsTemp_%5B0%5D.amount=10000&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=60&prpCitemKindsTemp_%5B0%5D.unitAmount=&prpCitemKindsTemp_%5B0%5D.quantity=90&prpCitemKindsTemp_%5B0%5D.amount=&prpCitemKindsTemp_%5B0%5D.amount=50000.00&prpCitemKindsTemp_%5B0%5D.amount=10000.00&prpCitemKindsTemp_%5B0%5D.amount=5000.00&itemKindLoadFlag=&BIdemandNo=&BIdemandTime=&bIRiskWarningType=&noDamageYearsBIPlat=0&prpCitemCarExt.lastDamagedBI=&lastDamagedBITemp=&DAZlastDamagedBI=&prpCitemCarExt.thisDamagedBI=0&prpCitemCarExt.noDamYearsBI=0&noDamYearsBINumber=0&prpCitemCarExt.lastDamagedCI=0&BIDemandClaim_Flag=&BiInsureDemandPay_%5B0%5D.id.serialNo=&BiInsureDemandPay_%5B0%5D.payCompany=&BiInsureDemandPay_%5B0%5D.claimregistrationno=&BiInsureDemandPay_%5B0%5D.compensateNo=&BiInsureDemandPay_%5B0%5D.lossTime=&BiInsureDemandPay_%5B0%5D.endcCaseTime=&PrpCmain_%5B0%5D.startDate=&PrpCmain_%5B0%5D.endDate=&BiInsureDemandPay_%5B0%5D.lossFee=&BiInsureDemandPay_%5B0%5D.payType=&BiInsureDemandPay_%5B0%5D.personpayType=&bIRiskWarningClaimItems_%5B0%5D.id.serialNo=&bIRiskWarningClaimItems_%5B0%5D.riskWarningType=&bIRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&bIRiskWarningClaimItems_%5B0%5D.insurerCode=&bIRiskWarningClaimItems_%5B0%5D.lossTime=&bIRiskWarningClaimItems_%5B0%5D.lossArea=&prpCitemKindCI.shortRate=100&cIBPFlag=1&prpCitemKindCI.unitAmount=0&prpCitemKindCI.id.itemKindNo=&prpCitemKindCI.kindCode=050100&prpCitemKindCI.kindName=%BB%FA%B6%AF%B3%B5%BD%BB%CD%A8%CA%C2%B9%CA%C7%BF%D6%C6%D4%F0%C8%CE%B1%A3%CF%D5&prpCitemKindCI.calculateFlag=Y&prpCitemKindCI.basePremium=&prpCitemKindCI.quantity=1&prpCitemKindCI.amount=&prpCitemKindCI.deductible=&prpCitemKindCI.adjustRate=1&prpCitemKindCI.rate=0&prpCitemKindCI.benchMarkPremium=&prpCitemKindCI.disCount=1&prpCitemKindCI.premium=&prpCitemKindCI.flag=&prpCitemKindCI.netPremium=&prpCitemKindCI.taxPremium=&prpCitemKindCI.taxRate=&prpCitemKindCI.dutyFlag=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCtrafficDetails_%5B0%5D.trafficType=1&prpCtrafficDetails_%5B0%5D.accidentType=1&prpCtrafficDetails_%5B0%5D.indemnityDuty=%D3%D0%D4%F0&prpCtrafficDetails_%5B0%5D.sumPaid=&prpCtrafficDetails_%5B0%5D.accidentDate=&prpCtrafficDetails_%5B0%5D.payComCode=&prpCtrafficDetails_%5B0%5D.flag=&prpCtrafficDetails_%5B0%5D.id.serialNo=&prpCitemCarExt_CI.rateRloatFlag=01&prpCitemCarExt_CI.noDamYearsCI=1&prpCitemCarExt_CI.lastDamagedCI=0&prpCitemCarExt_CI.flag=&prpCitemCarExt_CI.damFloatRatioCI=0&prpCitemCarExt_CI.offFloatRatioCI=0&prpCitemCarExt_CI.thisDamagedCI=0&prpCitemCarExt_CI.flag=&hidden_index_ctraffic_NOPlat_Drink=0&hidden_index_ctraffic_NOPlat=0&ciInsureDemand.demandNo=&ciInsureDemand.demandTime=&ciInsureDemand.restricFlag=&ciInsureDemand.preferentialDay=&ciInsureDemand.preferentialPremium=&ciInsureDemand.preferentialFormula%20=&ciInsureDemand.lastyearenddate=&prpCitemCar.noDamageYears=0&ciInsureDemand.rateRloatFlag=00&ciInsureDemand.claimAdjustReason=A1&ciInsureDemand.peccancyAdjustReason=V1&cIRiskWarningType=&CIDemandFecc_Flag=&ciInsureDemandLoss_%5B0%5D.id.serialNo=&ciInsureDemandLoss_%5B0%5D.lossTime=&ciInsureDemandLoss_%5B0%5D.lossDddress=&ciInsureDemandLoss_%5B0%5D.lossAction=&ciInsureDemandLoss_%5B0%5D.coeff=&ciInsureDemandLoss_%5B0%5D.lossType=&ciInsureDemandLoss_%5B0%5D.identifyType=&ciInsureDemandLoss_%5B0%5D.identifyNumber=&ciInsureDemandLoss_%5B0%5D.lossAcceptDate=&ciInsureDemandLoss_%5B0%5D.processingStatus=&ciInsureDemandLoss_%5B0%5D.lossActionDesc=&CIDemandClaim_Flag=&ciInsureDemandPay_%5B0%5D.id.serialNo=&ciInsureDemandPay_%5B0%5D.payCompany=&ciInsureDemandPay_%5B0%5D.claimregistrationno=&ciInsureDemandPay_%5B0%5D.compensateNo=&ciInsureDemandPay_%5B0%5D.lossTime=&ciInsureDemandPay_%5B0%5D.endcCaseTime=&ciInsureDemandPay_%5B0%5D.lossFee=&ciInsureDemandPay_%5B0%5D.payType=&ciInsureDemandPay_%5B0%5D.personpayType=&ciRiskWarningClaimItems_%5B0%5D.id.serialNo=&ciRiskWarningClaimItems_%5B0%5D.riskWarningType=&ciRiskWarningClaimItems_%5B0%5D.claimSequenceNo=&ciRiskWarningClaimItems_%5B0%5D.insurerCode=&ciRiskWarningClaimItems_%5B0%5D.lossTime=&ciRiskWarningClaimItems_%5B0%5D.lossArea=&ciInsureDemand.licenseNo=&ciInsureDemand.licenseType=&ciInsureDemand.useNatureCode=&ciInsureDemand.frameNo=&ciInsureDemand.engineNo=&ciInsureDemand.licenseColorCode=&ciInsureDemand.carOwner=&ciInsureDemand.enrollDate=&ciInsureDemand.makeDate=&ciInsureDemand.seatCount=&ciInsureDemand.tonCount=&ciInsureDemand.validCheckDate=&ciInsureDemand.manufacturerName=&ciInsureDemand.modelCode=&ciInsureDemand.brandCName=&ciInsureDemand.brandName=&ciInsureDemand.carKindCode=&ciInsureDemand.checkDate=&ciInsureDemand.endValidDate=&ciInsureDemand.carStatus=&ciInsureDemand.haulage=&AccidentFlag=&rateFloatFlag=ND4&prpCtrafficRecordTemps_%5B0%5D.id.serialNo=&prpCtrafficRecordTemps_%5B0%5D.accidentDate=&prpCtrafficRecordTemps_%5B0%5D.claimDate=&hidden_index_ctraffic=0&_taxUnit=&taxPlatFormTime=2012-04-21&iniPrpCcarShipTax_Flag=&strCarShipFlag=1&prpCcarShipTax.taxType=1&prpCcarShipTax.calculateMode=C1&prpCcarShipTax.leviedDate=&prpCcarShipTax.carKindCode=A01&prpCcarShipTax.model=B11&prpCcarShipTax.taxPayerIdentNo=&prpCcarShipTax.taxPayerNumber=&prpCcarShipTax.carLotEquQuality=&prpCcarShipTax.taxPayerCode=&prpCcarShipTax.id.itemNo=1&prpCcarShipTax.taxPayerNature=3&prpCcarShipTax.taxPayerName=&prpCcarShipTax.taxUnit=&prpCcarShipTax.taxComCode=&prpCcarShipTax.taxComName=&prpCcarShipTax.taxExplanation=&prpCcarShipTax.taxAbateReason=&prpCcarShipTax.dutyPaidProofNo_1=&prpCcarShipTax.dutyPaidProofNo_2=&prpCcarShipTax.dutyPaidProofNo=&prpCcarShipTax.taxAbateRate=&prpCcarShipTax.taxAbateAmount=&prpCcarShipTax.taxAbateType=1&prpCcarShipTax.taxUnitAmount=&prpCcarShipTax.prePayTaxYear=&prpCcarShipTax.prePolicyEndDate=&prpCcarShipTax.payStartDate=&prpCcarShipTax.payEndDate=&prpCcarShipTax.thisPayTax=&prpCcarShipTax.prePayTax=&prpCcarShipTax.taxItemCode=&prpCcarShipTax.taxItemName=&prpCcarShipTax.baseTaxation=&prpCcarShipTax.taxRelifFlag=&prpCcarShipTax.delayPayTax=&prpCcarShipTax.sumPayTax=&CarShipInit_Flag=&prpCcarShipTax.flag=&quotationtaxPayerCode=&noBringOutEngage=&prpCengageTemps_%5B0%5D.id.serialNo=&prpCengageTemps_%5B0%5D.clauseCode=&prpCengageTemps_%5B0%5D.clauseName=&clauses_%5B0%5D=&prpCengageTemps_%5B0%5D.flag=&prpCengageTemps_%5B0%5D.engageFlag=&prpCengageTemps_%5B0%5D.maxCount=&prpCengageTemps_%5B0%5D.clauses=&iniPrpCengage_Flag=&hidden_index_engage=0&costRateForPG=&certificateNo=&levelMaxRate=&maxRateScm=&levelMaxRateCi=&maxRateScmCi=&isModifyBI=&isModifyCI=&sumBICoinsRate=&sumCICoinsRate=&agentsRateBI=&agentsRateCI=&prpVisaRecordP.id.visaNo=&prpVisaRecordP.id.visaCode=&prpVisaRecordP.visaName=&prpVisaRecordP.printType=101&prpVisaRecordT.id.visaNo=&prpVisaRecordT.id.visaCode=&prpVisaRecordT.visaName=&prpVisaRecordT.printType=103&prpCmain.sumAmount=&prpCmain.sumDiscount=&prpCstampTaxBI.biTaxRate=&prpCstampTaxBI.biPayTax=&prpCmain.sumPremium=&prpVisaRecordPCI.id.visaNo=&prpVisaRecordPCI.id.visaCode=&prpVisaRecordPCI.visaName=&prpVisaRecordPCI.printType=201&prpVisaRecordTCI.id.visaNo=&prpVisaRecordTCI.id.visaCode=&prpVisaRecordTCI.visaName=&prpVisaRecordTCI.printType=203&prpCmainCI.sumAmount=&prpCmainCI.sumDiscount=&prpCstampTaxCI.ciTaxRate=&prpCstampTaxCI.ciPayTax=&prpCmainCI.sumPremium=&prpCmainCar.rescueFundRate=&prpCmainCar.resureFundFee=&prpCmain.projectCode=&projectCode=&costRateUpper=&prpCmainCommon.ext3=&importantProjectCode=&prpCmain.operatorCode=020083&operatorName=%D6%DA%BA%CF%CB%C4%BA%A3&operateDateShow=&prpCmain.coinsFlag=00&coinsFlagBak=00&premium=&prpCmain.language=CNY&prpCmain.policySort=1&prpCmain.policyRelCode=&prpCmain.policyRelName=&subsidyRate=&policyRel=&prpCmain.reinsFlag=0&prpCmain.agriFlag=0&premium=&prpCmainCar.carCheckStatus=0&prpCmainCar.carChecker=&carCheckerTranslate=&prpCmainCar.carCheckTime=&prpCmainCommon.DBCFlag=0&prpCmain.argueSolution=1&prpCmain.arbitBoardName=&arbitBoardNameDes=&prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&hidden_index_commission=0&scmIsOpen=1111100000&prpCagents_%5B0%5D.roleType=&roleTypeName_%5B0%5D=&prpCagents_%5B0%5D.id.roleCode=&prpCagents_%5B0%5D.roleCode_uni=&prpCagents_%5B0%5D.roleName=&prpCagents_%5B0%5D.costRate=&prpCagents_%5B0%5D.costFee=&prpCagents_%5B0%5D.flag=&prpCagents_%5B0%5D.businessNature=&prpCagents_%5B0%5D.isMain=&prpCagentCIs_%5B0%5D.roleType=&roleTypeNameCI_%5B0%5D=&prpCagentCIs_%5B0%5D.id.roleCode=&prpCagentCIs_%5B0%5D.roleCode_uni=&prpCagentCIs_%5B0%5D.roleName=&prpCagentCIs_%5B0%5D.costRate=&prpCagentCIs_%5B0%5D.costFee=&prpCagentCIs_%5B0%5D.flag=&prpCagentCIs_%5B0%5D.businessNature=&prpCagentCIs_%5B0%5D.isMain=&commissionCount=&prpCsaless_%5B0%5D.salesDetailName=&prpCsaless_%5B0%5D.riskCode=&prpCsaless_%5B0%5D.splitRate=&prpCsaless_%5B0%5D.oriSplitNumber=&prpCsaless_%5B0%5D.splitFee=&prpCsaless_%5B0%5D.agreementNo=&prpCsaless_%5B0%5D.id.salesCode=&prpCsaless_%5B0%5D.salesName=&prpCsaless_%5B0%5D.id.proposalNo=&prpCsaless_%5B0%5D.id.salesDetailCode=&prpCsaless_%5B0%5D.totalRate=&prpCsaless_%5B0%5D.splitWay=&prpCsaless_%5B0%5D.totalRateMax=&prpCsaless_%5B0%5D.flag=&prpCsaless_%5B0%5D.remark=&commissionPower=&hidden_index_prpCsales=0&prpCsalesDatils_%5B0%5D.id.salesCode=&prpCsalesDatils_%5B0%5D.id.proposalNo=&prpCsalesDatils_%5B0%5D.id.%20%20=&prpCsalesDatils_%5B0%5D.id.roleType=&prpCsalesDatils_%5B0%5D.id.roleCode=&prpCsalesDatils_%5B0%5D.currency=&prpCsalesDatils_%5B0%5D.splitDatilRate=&prpCsalesDatils_%5B0%5D.splitDatilFee=&prpCsalesDatils_%5B0%5D.roleName=&prpCsalesDatils_%5B0%5D.splitWay=&prpCsalesDatils_%5B0%5D.flag=&prpCsalesDatils_%5B0%5D.remark=&hidden_index_prpCsalesDatil=0&csManageSwitch=1&prpCmainChannel.agentCode=&prpCmainChannel.agentName=&prpCmainChannel.organCode=&prpCmainChannel.organCName=&comCodeType=&prpCmainChannel.identifyNumber=&prpCmainChannel.identifyType=&prpCmainChannel.manOrgCode=&prpCmain.remark=&prpDdismantleDetails_%5B0%5D.id.agreementNo=&prpDdismantleDetails_%5B0%5D.flag=&prpDdismantleDetails_%5B0%5D.id.configCode=&prpDdismantleDetails_%5B0%5D.id.assignType=&prpDdismantleDetails_%5B0%5D.id.roleCode=&prpDdismantleDetails_%5B0%5D.roleName=&prpDdismantleDetails_%5B0%5D.costRate=&prpDdismantleDetails_%5B0%5D.roleFlag=&prpDdismantleDetails_%5B0%5D.businessNature=&prpDdismantleDetails_%5B0%5D.roleCode_uni=&hidden_index_prpDdismantleDetails=0&payTimes=1&prpCplanTemps_%5B0%5D.payNo=&prpCplanTemps_%5B0%5D.serialNo=&prpCplanTemps_%5B0%5D.endorseNo=&cplan_%5B0%5D.payReasonC=&prpCplanTemps_%5B0%5D.payReason=&prpCplanTemps_%5B0%5D.planDate=&prpCplanTemps_%5B0%5D.currency=&description_%5B0%5D.currency=&prpCplanTemps_%5B0%5D.planFee=&cplans_%5B0%5D.planFee=&cplans_%5B0%5D.backPlanFee=&prpCplanTemps_%5B0%5D.netPremium=&prpCplanTemps_%5B0%5D.taxPremium=&prpCplanTemps_%5B0%5D.delinquentFee=&prpCplanTemps_%5B0%5D.flag=&prpCplanTemps_%5B0%5D.subsidyRate=&prpCplanTemps_%5B0%5D.isBICI=&iniPrpCplan_Flag=&loadFlag9=&planfee_index=0&planStr=&planPayTimes=&prpCmainCar.flag=1&prpCmainCarFlag=1&coinsSchemeCode=&coinsSchemeName=&mainPolicyNo=&prpCcoinsMains_%5B0%5D.id.serialNo=1&prpCcoinsMains_%5B0%5D.coIdentity=1&prpCcoinsMains_%5B0%5D.coinsCode=002&prpCcoinsMains_%5B0%5D.coinsName=%C8%CB%B1%A3%B2%C6%B2%FA&prpCcoinsMains_%5B0%5D.coinsRate=&prpCcoinsMains_%5B0%5D.id.currency=CNY&prpCcoinsMains_%5B0%5D.coinsAmount=&prpCcoinsMains_%5B0%5D.coinsPremium=&prpCcoinsMains_%5B0%5D.coinsPremium=&iniPrpCcoins_Flag=&hidden_index_ccoins=0&prpCpayeeAccountBIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountBIs_%5B0%5D.id.serialNo=&prpCpayeeAccountBIs_%5B0%5D.itemNo=&prpCpayeeAccountBIs_%5B0%5D.payReason=&prpCpayeeAccountBIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountBIs_%5B0%5D.accountName=&prpCpayeeAccountBIs_%5B0%5D.basicBankCode=&prpCpayeeAccountBIs_%5B0%5D.basicBankName=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountBIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountBIs_%5B0%5D.bankCode=&prpCpayeeAccountBIs_%5B0%5D.bankName=&prpCpayeeAccountBIs_%5B0%5D.cnaps=&prpCpayeeAccountBIs_%5B0%5D.accountNo=&prpCpayeeAccountBIs_%5B0%5D.isPrivate=&prpCpayeeAccountBIs_%5B0%5D.cardType=&prpCpayeeAccountBIs_%5B0%5D.paySumFee=&prpCpayeeAccountBIs_%5B0%5D.payType=&prpCpayeeAccountBIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountBIs_%5B0%5D.sendSms=&prpCpayeeAccountBIs_%5B0%5D.identifyType=&prpCpayeeAccountBIs_%5B0%5D.identifyNo=&prpCpayeeAccountBIs_%5B0%5D.telephone=&prpCpayeeAccountBIs_%5B0%5D.sendMail=&prpCpayeeAccountBIs_%5B0%5D.mailAddr=&prpCpayeeAccountCIs_%5B0%5D.id.proposalNo=&prpCpayeeAccountCIs_%5B0%5D.id.serialNo=&prpCpayeeAccountCIs_%5B0%5D.itemNo=&prpCpayeeAccountCIs_%5B0%5D.payReason=&prpCpayeeAccountCIs_%5B0%5D.payeeInfoid=&prpCpayeeAccountCIs_%5B0%5D.accountName=&prpCpayeeAccountCIs_%5B0%5D.basicBankCode=&prpCpayeeAccountCIs_%5B0%5D.basicBankName=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaCode=&prpCpayeeAccountCIs_%5B0%5D.recBankAreaName=&prpCpayeeAccountCIs_%5B0%5D.bankCode=&prpCpayeeAccountCIs_%5B0%5D.bankName=&prpCpayeeAccountCIs_%5B0%5D.cnaps=&prpCpayeeAccountCIs_%5B0%5D.accountNo=&prpCpayeeAccountCIs_%5B0%5D.isPrivate=&prpCpayeeAccountCIs_%5B0%5D.cardType=&prpCpayeeAccountCIs_%5B0%5D.paySumFee=&prpCpayeeAccountCIs_%5B0%5D.payType=&prpCpayeeAccountCIs_%5B0%5D.intention=%D6%A7%B8%B6%CB%FB%B7%BD%B1%A3%B7%D1&prpCpayeeAccountCIs_%5B0%5D.sendSms=&prpCpayeeAccountCIs_%5B0%5D.identifyType=&prpCpayeeAccountCIs_%5B0%5D.identifyNo=&prpCpayeeAccountCIs_%5B0%5D.telephone=&prpCpayeeAccountCIs_%5B0%5D.sendMail=&prpCpayeeAccountCIs_%5B0%5D.mailAddr=&iReinsCode=&prpCspecialFacs_%5B0%5D.reinsCode=001&iFReinsCode=&iPayCode=&iShareRate=&iCommRate=&iTaxRate=&iOthRate=&iCommission=&iOthPremium=&prpCspecialFacs_%5B0%5D.id.reinsNo=1&prpCspecialFacs_%5B0%5D.freinsCode=001&prpCspecialFacs_%5B0%5D.payCode=001&prpCspecialFacs_%5B0%5D.shareRate=001&prpCspecialFacs_%5B0%5D.sharePremium=001&prpCspecialFacs_%5B0%5D.commRate=001&prpCspecialFacs_%5B0%5D.taxRate=001&prpCspecialFacs_%5B0%5D.tax=001&prpCspecialFacs_%5B0%5D.othRate=001&prpCspecialFacs_%5B0%5D.commission=001&prpCspecialFacs_%5B0%5D.othPremium=001&prpCspecialFacs_%5B0%5D.reinsName=001&prpCspecialFacs_%5B0%5D.freinsName=001&prpCspecialFacs_%5B0%5D.payName=001&prpCspecialFacs_%5B0%5D.remark=001&prpCspecialFacs_%5B0%5D.flag=&hidden_index_specialFac=0&updateIndex=-1&iniCspecialFac_Flag=&_ReinsCode=&loadFlag8=&_FReinsCode=&_PayCode=&_ReinsName=&_FReinsName=&_PayName=&_CommRate=&_OthRate=&_ShareRate=&_Commission=&_OthPremium=&_SharePremium=&_TaxRate=&_Tax=&_Remark=&prpCsettlement.buyerUnitRank=3&prpCsettlement.buyerPreFee=&prpCsettlement.buyerUnitCode=&prpCsettlement.buyerUnitName=&prpCsettlement.upperUnitCode=&upperUnitName=&prpCsettlement.buyerUnitAddress=&prpCsettlement.buyerLinker=&prpCsettlement.buyerPhone=&prpCsettlement.buyerMobile=&prpCsettlement.buyerFax=&prpCsettlement.buyerUnitNature=1&prpCsettlement.buyerProvince=11000000&buyerProvinceDes=%C8%CB%B1%A3%B2%C6%CF%D5%B1%B1%BE%A9%CA%D0%B7%D6%B9%AB%CB%BE&prpCsettlement.buyerBusinessSort=01&prpCsettlement.comCname=&prpCsettlement.linkerCode=&linkerName=&linkerPhone=&linkerMobile=&linkerFax=&prpCsettlement.comCode=&prpCsettlement.fundForm=1&prpCsettlement.flag=&settlement_Flag=&prpCcontriutions_%5B0%5D.id.serialNo=1&prpCcontriutions_%5B0%5D.contribType=F&prpCcontriutions_%5B0%5D.contribCode=&prpCcontriutions_%5B0%5D.contribName=&prpCcontriutions_%5B0%5D.contribCode_uni=&prpCcontriutions_%5B0%5D.contribPercent=&prpCcontriutions_%5B0%5D.contribPremium=&prpCcontriutions_%5B0%5D.remark=&hidden_index_ccontriutions=0&userCode=020083&iProposalNo=&CProposalNo=&timeFlag=&prpCremarks_%5B0%5D.id.proposalNo=&prpCremarks_%5B0%5D.id.serialNo=&prpCremarks_%5B0%5D.operatorCode=020083&prpCremarks_%5B0%5D.remark=&prpCremarks_%5B0%5D.flag=&prpCremarks_%5B0%5D.insertTimeForHis=&hidden_index_remark=0&ciInsureDemandCheckVo.demandNo=&ciInsureDemandCheckVo.checkQuestion=&ciInsureDemandCheckVo.checkAnswer=&ciInsureDemandCheckVo.flag=DEMAND&ciInsureDemandCheckVo.riskCode=";
	    	Request request1 = new Request();//第一步的参数
	    	request1.setUrl("http://" + SysConfigInfo.PICC_MAIN_URL +":8000/prpall/carInf/getDataFromCiCarInfo.do");
	    	Map<String, String> map1 = new HashMap<>();
	    	map1.put("param", param);
	    	request1.setRequestParam(map1);
	    	QuoteGetCarInfoPage getCarInfoPage = new QuoteGetCarInfoPage(1);
	    	Response response1 = getCarInfoPage.run(request1);//第一步的返回结果
	    	Map<String, String> carInfoMap = response1.getResponseMap();
	    	String engineNo = carInfoMap.get("engineNo");//发动机号
	    	String vin = carInfoMap.get("vin");//vin
	    	String enrollDate = carInfoMap.get("enrollDate");//注册日期
	    	String seatCount = carInfoMap.get("seatCount");//车辆座位数
	    	String modelCodeAlias = carInfoMap.get("modelCodeAlias");
	    	UserInfo.put("EngineNo", engineNo);
	    	UserInfo.put("RegisterDate", enrollDate);
	    	UserInfo.put("CarVin", vin);
	    	UserInfo.put("ModleName", modelCodeAlias);
		}
		renewalInfoJson.put("UserInfo", UserInfo);//用户信息
		renewalInfoJson.put("CustKey", CustKey);//用户信息
		return renewalInfoJson;
	}
}
