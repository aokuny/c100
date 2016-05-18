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
	private static Map<String, Map<String, Object>> uploadInsurInfo = new HashMap<>();
    private static String licenseType="02";//车牌类型小型汽车
    @Override
    public BaseCarInfoResponse getBaseCarInfoByLicenseNo(String licenseNo,int CityCode) {
        BaseCarInfoResponse carBaseInfoResponse = new BaseCarInfoResponse();
        Response responseIndex = goXubaoIndex();
        if(responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200){
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
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                }else if(value.get("role").toString().equals("被保险人")){
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
                                }else if (value.get("role").toString().equals("被保险人/车主")) {
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                }
                                else if (value.get("role").toString().equals("投保人/车主")) {
                                    carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                }
                                else if(value.get("role").toString().equals("投保人/被保险人")){
                                    carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人/车主
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
        }else{
            logger.info("抓取机器人，【 PICC 跳转续保页面错误】");
        }
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
		Response response = new Response();
		//TODO 需要根据实际应用来进行参数传递与配置，若缓存中有sessionId
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
        return saveQuoteResponse;
    }

    @Override
    public List<RelaPeopleResponse> getRelaPeopleInfoByCarInfoList(String licenseNo) {
        return null;
    }

    @Override
    public List<ClaimResponse> getClaimInfoList(String licenseNo ) {
		List<ClaimResponse> ClaimResponseList = new ArrayList<>();
		Response responseIndex = goXubaoIndex();
		if (responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200) {
			Response responseSearch = xubaoSearchByLicenseNo(responseIndex, licenseNo, licenseType);
			if (responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200) {
				Response claimResponse1 = xubaoQueryClaimsMsg(responseSearch);
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

		}
		return ClaimResponseList;
	}

	/*******************************************************************************
	 * 跳转续保页面
	 * 
	 * @return response nextParams null lastResult null
	 *******************************************************************************/
	public Response goXubaoIndex() {
		XubaoIndexPage xubaoIndexPage = new XubaoIndexPage();
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
		XubaoSearchPage xubaoSearchPage = new XubaoSearchPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		map.put("prpCrenewalVo.engineNo", null);
		map.put("prpCrenewalVo.frameNo", null);
		map.put("prpCrenewalVo.licenseColorCode", null);
		map.put("prpCrenewalVo.licenseNo", licenseNo);
		map.put("prpCrenewalVo.licenseType", licenseType);
		map.put("prpCrenewalVo.othFlag", null);
		map.put("prpCrenewalVo.policyNo", null);
		map.put("prpCrenewalVo.vinNo", null);
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
		XubaoBrowsePolicyPage xubaoBrowsePolicyPage = new XubaoBrowsePolicyPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号

		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_BROWSEPOLICYNO);// GET
		Response responseBrowse = xubaoBrowsePolicyPage.run(request);
		return responseBrowse;
	}

	/*******************************************************************************
	 * 查询车辆基本信息
	 * 
	 * @param response
	 * @return
	 *******************************************************************************/
	public Response xubaoGetCitemCar(Response response) {
		XubaoShowCitemCarPage xubaoShowCitemCarPage = new XubaoShowCitemCarPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", "POLICY");
		map.put("comCode", "11029204");
		map.put("contractNo", null);
		map.put("editType", "SHOW_POLICY");
		map.put("minusFlag", "originQuery");
		map.put("proposalNo", null);
		map.put("riskCode", "DAA");
		map.put("rnd704", new Date().toString());
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
		XubaoShowCinsuredPage xubaoShowCinsuredPage = new XubaoShowCinsuredPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", "POLICY");
		map.put("comCode", "11029204");
		map.put("contractNo", null);
		map.put("editType", "SHOW_POLICY");
		map.put("minusFlag", "originQuery");
		map.put("proposalNo", null);
		map.put("riskCode", "DAA");
		map.put("rnd704", new Date().toString());
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);// GET
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

		XubaoShowCitemKindPage xubaoShowCitemKindPage = new XubaoShowCitemKindPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", "POLICY");
		map.put("comCode", null);
		map.put("contractNo", null);
		map.put("editType", "SHOW_POLICY");
		map.put("minusFlag", "originQuery");
		map.put("proposalNo", null);
		map.put("riskCode", "DAA");
		map.put("rnd704", null);
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);// GET
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

		XubaoClaimsMsgPage xubaoClaimsMsgPage = new XubaoClaimsMsgPage();
		Request request = new Request();
		Map<String, String> map = new HashMap<String, String>();
		Map responseParam = response.getResponseMap();
		Map nextParamsMap = (Map) responseParam.get("nextParams");
		map.put("bizNo", nextParamsMap.get("bizNo").toString());// 上年商业保单号
		map.put("bizType", "POLICY");
		request.setRequestParam(map);
		request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_QUERYCLAIMSMSG);// GET
		Response responseClaimMsg = xubaoClaimsMsgPage.run(request);
		return responseClaimMsg;
	}

	@Override
	public PostPrecisePricerResponse postPrecisePrice(String LicenseNo, String CarOwnersName, String IdCard,
			String IsSingleSubmit, String IntentionCompany, String InsuredName, String InsuredIdCard,
			String InsuredIdType, String InsuredMobile, String IsNewCar, String CarType, String CarUsedType,
			String CityCode, String EngineNo, String CarVin, String RegisterDate, String MoldName, String ForceTax,
			String BizStartDate, String BoLi, String BuJiMianCheSun, String BuJiMianDaoQiang, String BuJiMianFuJia,
			String BuJiMianRenYuan, String BuJiMianSanZhe, String CheDeng, String SheShui, String HuaHen, String SiJi,
			String ChengKe, String CheSun, String DaoQiang, String SanZhe, String ZiRan, String SeatCount,
			String TonCount, String HcSheBeiSunshi, String HcHuoWuZeRen, String HcFeiYongBuChang,
			String HcJingShenSunShi, String HcSanFangTeYue, String HcXiuLiChang, String DName, String DQuantity,
			String DAmount, String PDate, String DName1, String DQuantity1, String DAmount1, String PDate1,
			String DName2, String DQuantity2, String DAmount2, String PDate2, String DName3, String DQuantity3,
			String DAmount3, String PDate3, String CustKey, String Agent, String SecCode) {
		PostPrecisePricerResponse postPrecisePricerResponse = new PostPrecisePricerResponse();
		// 验证参数
		postPrecisePricerResponse = this.verifyPostPrecisePriceParam(LicenseNo, CarOwnersName, IdCard, IsSingleSubmit,
				IntentionCompany, InsuredName, InsuredIdCard, InsuredIdType, InsuredMobile, IsNewCar, CarType,
				CarUsedType, CityCode, EngineNo, CarVin, RegisterDate, MoldName, ForceTax, BizStartDate, BoLi,
				BuJiMianCheSun, BuJiMianDaoQiang, BuJiMianFuJia, BuJiMianRenYuan, BuJiMianSanZhe, CheDeng, SheShui,
				HuaHen, SiJi, ChengKe, CheSun, DaoQiang, SanZhe, ZiRan, SeatCount, TonCount, HcSheBeiSunshi,
				HcHuoWuZeRen, HcFeiYongBuChang, HcJingShenSunShi, HcSanFangTeYue, HcXiuLiChang, DName, DQuantity,
				DAmount, PDate, DName1, DQuantity1, DAmount1, PDate1, DName2, DQuantity2, DAmount2, PDate2, DName3,
				DQuantity3, DAmount3, PDate3, CustKey, Agent, SecCode);
		if (!"1".equals(postPrecisePricerResponse.getBusinessStatus())) {// 参数验证没通过
			return postPrecisePricerResponse;
		}
		//对用户信息进行缓存
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, String> param = this.takeParamToMap(LicenseNo, CarOwnersName, IdCard, IsSingleSubmit, IntentionCompany, InsuredName, InsuredIdCard, InsuredIdType, InsuredMobile, IsNewCar, CarType, CarUsedType, CityCode, EngineNo, CarVin, RegisterDate, MoldName, ForceTax, BizStartDate, BoLi, BuJiMianCheSun, BuJiMianDaoQiang, BuJiMianFuJia, BuJiMianRenYuan, BuJiMianSanZhe, CheDeng, SheShui, HuaHen, SiJi, ChengKe, CheSun, DaoQiang, SanZhe, ZiRan, SeatCount, TonCount, HcSheBeiSunshi, HcHuoWuZeRen, HcFeiYongBuChang, HcJingShenSunShi, HcSanFangTeYue, HcXiuLiChang, DName, DQuantity, DAmount, PDate, DName1, DQuantity1, DAmount1, PDate1, DName2, DQuantity2, DAmount2, PDate2, DName3, DQuantity3, DAmount3, PDate3, CustKey, Agent, SecCode);
		paramMap.put(IntentionCompany, param);
		uploadInsurInfo.put(LicenseNo, paramMap);
		//开启线程进行查询
		switch (IntentionCompany) {
		case "0"://平安
			QuoteThreadPingan t1=new QuoteThreadPingan(LicenseNo, param);
			t1.start();
			break;
		case "1"://太平洋
			QuoteThreadCpic t2=new QuoteThreadCpic(LicenseNo, param);
			t2.start();
			break;
		case "2"://人保
			QuoteThreadPicc t3=new QuoteThreadPicc(LicenseNo, param);
			t3.start();
			break;
		case "-1"://全部
			QuoteThreadPingan t4=new QuoteThreadPingan(LicenseNo, param);
			t4.start();
			QuoteThreadCpic t5=new QuoteThreadCpic(LicenseNo, param);
			t5.start();
			QuoteThreadPicc t6=new QuoteThreadPicc(LicenseNo, param);
			t6.start();
			break;
		}
		postPrecisePricerResponse.setStatusMessage("险种信息上传成功");
		return postPrecisePricerResponse;
	}

	/**
	 * 对必要参数进行验证
	 * 
	 * @return
	 */
	private PostPrecisePricerResponse verifyPostPrecisePriceParam(String LicenseNo, String CarOwnersName, String IdCard,
			String IsSingleSubmit, String IntentionCompany, String InsuredName, String InsuredIdCard,
			String InsuredIdType, String InsuredMobile, String IsNewCar, String CarType, String CarUsedType,
			String CityCode, String EngineNo, String CarVin, String RegisterDate, String MoldName, String ForceTax,
			String BizStartDate, String BoLi, String BuJiMianCheSun, String BuJiMianDaoQiang, String BuJiMianFuJia,
			String BuJiMianRenYuan, String BuJiMianSanZhe, String CheDeng, String SheShui, String HuaHen, String SiJi,
			String ChengKe, String CheSun, String DaoQiang, String SanZhe, String ZiRan, String SeatCount,
			String TonCount, String HcSheBeiSunshi, String HcHuoWuZeRen, String HcFeiYongBuChang,
			String HcJingShenSunShi, String HcSanFangTeYue, String HcXiuLiChang, String DName, String DQuantity,
			String DAmount, String PDate, String DName1, String DQuantity1, String DAmount1, String PDate1,
			String DName2, String DQuantity2, String DAmount2, String PDate2, String DName3, String DQuantity3,
			String DAmount3, String PDate3, String CustKey, String Agent, String SecCode) {
		PostPrecisePricerResponse postPrecisePricerResponse = new PostPrecisePricerResponse();
		// 验证LicenseNo
		if (StringUtils.isBlank(LicenseNo)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("LicenseNo 参数为空");
			return postPrecisePricerResponse;
		}
		// 验证IsSingleSubmit
		if ((!"0".equals(IsSingleSubmit)) && (!"1".equals(IsSingleSubmit))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("IsSingleSubmit 参数必须为0或1");
			return postPrecisePricerResponse;
		}
		// 验证IntentionCompany
		if ((!"2".equals(IntentionCompany)) && (!"1".equals(IntentionCompany)) && (!"0".equals(IntentionCompany)) && (!"-1".equals(IntentionCompany))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("IntentionCompany -1:只报价不核保、0:平安、1:太平洋、2:人保");
			return postPrecisePricerResponse;
		}
		// 验证IsNewCar
		if ((!"0".equals(IsNewCar)) && (!"1".equals(IsNewCar))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("IsNewCar 参数必须为0或1");
			return postPrecisePricerResponse;
		}
		// 验证CarType
		if ((!"0".equals(CarType)) && (!"1".equals(CarType))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CarType 参数必须为0或1");
			return postPrecisePricerResponse;
		}
		// 验证CarUsedType
		if ((!"0".equals(CarUsedType)) && (!"1".equals(CarUsedType))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CarUsedType 参数必须为0或1");
			return postPrecisePricerResponse;
		}
		// 验证CityCode
		if ((!"1".equals(CityCode))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CityCode 参数必须为1");
			return postPrecisePricerResponse;
		}
		// 车辆信息EngineNo、CarVin
		if (StringUtils.isBlank(EngineNo) || StringUtils.isBlank(CarVin) || StringUtils.isBlank(MoldName)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("EngineNo或CarVin或MoldName  参数为空");
			return postPrecisePricerResponse;
		}
		SimpleDateFormat YEARMMDD = new SimpleDateFormat("yyyy-MM-dd");// 时间转换
		// 验证注册日期
		try {
			YEARMMDD.parse(RegisterDate);
		} catch (Exception e) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("RegisterDate  格式不对，2014-08-07");
			return postPrecisePricerResponse;
		}
		// 验证ForceTax
		if ((!"0".equals(ForceTax)) && (!"1".equals(ForceTax))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("ForceTax 参数必须为0或1");
			return postPrecisePricerResponse;
		}
		// 验证BizStartDate
		if ("0".equals(BizStartDate)) {
			try {
				YEARMMDD.parse(BizStartDate);
			} catch (Exception e) {
				postPrecisePricerResponse.setBusinessStatus("-1");
				postPrecisePricerResponse.setStatusMessage("BizStartDate  单商业的时候，该字段必须传递正确");
				return postPrecisePricerResponse;
			}
		}

		// 玻璃险
		if (!SysConfigInfo.boliValueList.contains(BoLi)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BizStartDate 参数值错误");
			return postPrecisePricerResponse;
		}
		// 车损不计免赔
		if (!SysConfigInfo.chooseValueList.contains(BuJiMianCheSun)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BuJiMianCheSun 参数值错误");
			return postPrecisePricerResponse;
		}
		// 盗抢不计免赔
		if (!SysConfigInfo.chooseValueList.contains(BuJiMianDaoQiang)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BuJiMianDaoQiang 参数值错误");
			return postPrecisePricerResponse;
		}
		// 附加不计免赔
		if (!SysConfigInfo.chooseValueList.contains(BuJiMianFuJia)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BuJiMianFuJia 参数值错误");
			return postPrecisePricerResponse;
		}
		// 人员不计免赔
		if (!SysConfigInfo.chooseValueList.contains(BuJiMianRenYuan)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BuJiMianRenYuan 参数值错误");
			return postPrecisePricerResponse;
		}
		// 三者不计免赔
		if (!SysConfigInfo.chooseValueList.contains(BuJiMianSanZhe)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("BuJiMianSanZhe 参数值错误");
			return postPrecisePricerResponse;
		}
		// 车灯
		if (!SysConfigInfo.boliValueList.contains(CheDeng)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CheDeng 参数值错误");
			return postPrecisePricerResponse;
		}
		// 盗抢
		if (!SysConfigInfo.chooseValueList.contains(DaoQiang)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("DaoQiang 参数值错误");
			return postPrecisePricerResponse;
		}
		// 车损
		if (!SysConfigInfo.chooseValueList.contains(CheSun)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CheSun 参数值错误");
			return postPrecisePricerResponse;
		}
		// 自燃
		if (!SysConfigInfo.chooseValueList.contains(ZiRan)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("ZiRan 参数值错误");
			return postPrecisePricerResponse;
		}
		// 涉水
		if (!SysConfigInfo.boliValueList.contains(SheShui)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("SheShui 参数值错误");
			return postPrecisePricerResponse;
		}
		// 划痕
		if (!SysConfigInfo.huahenValueList.contains(HuaHen)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("HuaHen 参数值错误");
			return postPrecisePricerResponse;
		}
		// 司机/乘客
		if (!SysConfigInfo.renyuanValueList.contains(SiJi)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("SiJi 参数值错误");
			return postPrecisePricerResponse;
		}
		// 司机/乘客
		if (!SysConfigInfo.renyuanValueList.contains(ChengKe)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("ChengKe 参数值错误");
			return postPrecisePricerResponse;
		}
		// 车损
		if (!SysConfigInfo.chesunValueList.contains(SanZhe)) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("SanZhe 参数值错误");
			return postPrecisePricerResponse;
		}
		
		if(StringUtils.isBlank(CustKey)){
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("CustKey 不能为空");
			return postPrecisePricerResponse;
		}
		if(StringUtils.isBlank(Agent)){
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("Agent 不能为空");
			return postPrecisePricerResponse;
		}
		if(StringUtils.isBlank(SecCode)){
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("SecCode 不能为空");
			return postPrecisePricerResponse;
		}
		postPrecisePricerResponse.setBusinessStatus("1");
		postPrecisePricerResponse.setStatusMessage("参数验证成功");
		logger.info("PICC API，【上传险种信息参数验证成功】");
		return postPrecisePricerResponse;
	}
	
	/**
	 * 参数变为Map
	 * @return
	 */
	private Map<String, String> takeParamToMap(String LicenseNo, String CarOwnersName, String IdCard,
			String IsSingleSubmit, String IntentionCompany, String InsuredName, String InsuredIdCard,
			String InsuredIdType, String InsuredMobile, String IsNewCar, String CarType, String CarUsedType,
			String CityCode, String EngineNo, String CarVin, String RegisterDate, String MoldName, String ForceTax,
			String BizStartDate, String BoLi, String BuJiMianCheSun, String BuJiMianDaoQiang, String BuJiMianFuJia,
			String BuJiMianRenYuan, String BuJiMianSanZhe, String CheDeng, String SheShui, String HuaHen, String SiJi,
			String ChengKe, String CheSun, String DaoQiang, String SanZhe, String ZiRan, String SeatCount,
			String TonCount, String HcSheBeiSunshi, String HcHuoWuZeRen, String HcFeiYongBuChang,
			String HcJingShenSunShi, String HcSanFangTeYue, String HcXiuLiChang, String DName, String DQuantity,
			String DAmount, String PDate, String DName1, String DQuantity1, String DAmount1, String PDate1,
			String DName2, String DQuantity2, String DAmount2, String PDate2, String DName3, String DQuantity3,
			String DAmount3, String PDate3, String CustKey, String Agent, String SecCode){
		Map<String, String> map = new HashMap<>();
		map.put("LicenseNo", LicenseNo);
		map.put("CarOwnersName", CarOwnersName);
		map.put("IdCard", IdCard);
		map.put("IsSingleSubmit", IsSingleSubmit);
		map.put("IntentionCompany", IntentionCompany);
		map.put("InsuredName", InsuredName);
		map.put("InsuredIdCard", InsuredIdCard);
		map.put("CityCode", CityCode);
		map.put("EngineNo", EngineNo);
		map.put("CarVin", CarVin);
		map.put("RegisterDate", RegisterDate);
		map.put("MoldName", MoldName);
		map.put("ForceTax", ForceTax);
		map.put("BizStartDate", BizStartDate);
		map.put("BoLi", BoLi);
		map.put("BuJiMianCheSun", BuJiMianCheSun);
		map.put("BuJiMianDaoQiang", BuJiMianDaoQiang);
		map.put("BuJiMianFuJia", BuJiMianFuJia);
		map.put("BuJiMianRenYuan", BuJiMianRenYuan);
		map.put("BuJiMianSanZhe", BuJiMianSanZhe);
		map.put("CheDeng", CheDeng);
		map.put("SheShui", SheShui);
		map.put("HuaHen", HuaHen);
		map.put("SiJi", SiJi);
		map.put("ChengKe", ChengKe);
		map.put("CheSun", CheSun);
		map.put("DaoQiang", DaoQiang);
		map.put("SanZhe", SanZhe);
		map.put("ZiRan", ZiRan);
		map.put("SeatCount", SeatCount);
		map.put("TonCount", TonCount);
		map.put("HcSheBeiSunshi", HcSheBeiSunshi);
		map.put("HcHuoWuZeRen", HcHuoWuZeRen);
		map.put("HcFeiYongBuChang", HcFeiYongBuChang);
		map.put("DAmount", DAmount);
		map.put("PDate", PDate);
		map.put("DName1", DName1);
		map.put("DQuantity1", DQuantity1);
		map.put("DAmount1", DAmount1);
		map.put("PDate1", PDate1);
		map.put("DAmount3", DAmount3);
		map.put("PDate3", PDate3);
		map.put("PDate1", PDate1);
		map.put("DName2", DName2);
		map.put("DQuantity2", DQuantity2);
		map.put("PDate2", PDate2);
		map.put("DAmount2", DAmount2);
		map.put("DName3", DName3);
		map.put("DQuantity3", DQuantity3);
		map.put("CustKey", CustKey);
		map.put("Agent", Agent);
		map.put("SecCode", SecCode);
		return map;
	}

}
