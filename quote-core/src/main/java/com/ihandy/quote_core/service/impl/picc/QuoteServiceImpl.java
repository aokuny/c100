package com.ihandy.quote_core.service.impl.picc;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.service.IQuoteService;
import com.ihandy.quote_core.utils.CacheConstant;
import com.ihandy.quote_core.utils.SysConfigInfo;

/**
 * 报价实现类
 * @author liming
 *
 */
@Service
public class QuoteServiceImpl implements IQuoteService {
	
	private static Logger logger = LoggerFactory.getLogger(QuoteServiceImpl.class);

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
		Map<String, Object> cacheParamMap = CacheConstant.uploadInsurInfo.get(LicenseNo);
		if(cacheParamMap == null){
			cacheParamMap = new HashMap<>();
		}
		Integer IsSingleSubmitInt = Integer.parseInt(IsSingleSubmit);
		Integer IntentionCompanyInt = Integer.parseInt(IntentionCompany);
		if(IsSingleSubmitInt == 0 && IntentionCompanyInt == -1){//三家报价不核保
			IsSingleSubmit = "0";//不核保
		}
		if(IsSingleSubmitInt == 2 && IntentionCompanyInt > -1){//一家报价不核保
			IsSingleSubmit = "0";//不核保
		}
		if(IsSingleSubmitInt == 0 && IntentionCompanyInt > -1){//三家报价，一家核保
			IsSingleSubmit = "1";//核保
		}
		if(IsSingleSubmitInt == 1 && IntentionCompanyInt > -1){//一家报价，一家核保
			IsSingleSubmit = "1";//核保
		}
		Map<String, String> param = this.takeParamToMap(LicenseNo, CarOwnersName, IdCard, IsSingleSubmit, IntentionCompany, InsuredName, InsuredIdCard, InsuredIdType, InsuredMobile, IsNewCar, CarType, CarUsedType, CityCode, EngineNo, CarVin, RegisterDate, MoldName, ForceTax, BizStartDate, BoLi, BuJiMianCheSun, BuJiMianDaoQiang, BuJiMianFuJia, BuJiMianRenYuan, BuJiMianSanZhe, CheDeng, SheShui, HuaHen, SiJi, ChengKe, CheSun, DaoQiang, SanZhe, ZiRan, SeatCount, TonCount, HcSheBeiSunshi, HcHuoWuZeRen, HcFeiYongBuChang, HcJingShenSunShi, HcSanFangTeYue, HcXiuLiChang, DName, DQuantity, DAmount, PDate, DName1, DQuantity1, DAmount1, PDate1, DName2, DQuantity2, DAmount2, PDate2, DName3, DQuantity3, DAmount3, PDate3, CustKey, Agent, SecCode);
		cacheParamMap.put(IntentionCompany, param);
		CacheConstant.uploadInsurInfo.put(LicenseNo, cacheParamMap);
		//开启线程进行查询
		switch (IntentionCompany) {
			case "0"://平安
				QuoteThreadPingan t1 = new QuoteThreadPingan(LicenseNo, param);
				t1.start();
				break;
			case "1"://太平洋
				QuoteThreadCpic t2 = new QuoteThreadCpic(LicenseNo, param);
				t2.start();
				break;
			case "2"://人保
				QuoteThreadPicc t3 = new QuoteThreadPicc(LicenseNo, param);
				t3.start();
				break;
			case "-1"://全部
				param.put("IntentionCompany", SysConfigInfo.PICC_FLAG.toString());
				QuoteThreadPicc t6 = new QuoteThreadPicc(LicenseNo, param);
				t6.start();
				break;
		}
		//上传前，把以前的报价缓存进行删除
		Map<String, Object> quoteResultMap = CacheConstant.quoteResultInfo.get(LicenseNo);
		if(quoteResultMap != null){
			if("-1".equals(IntentionCompany)){//全部清除
				CacheConstant.quoteResultInfo.remove(LicenseNo);
				logger.info("人保 API，【清除全部报价结果成功】，LicenseNo：" + LicenseNo);
			}else{
				quoteResultMap.remove(IntentionCompany);//存在的时候，就删除
				CacheConstant.quoteResultInfo.put(LicenseNo, quoteResultMap);//在存放
				logger.info("人保 API，【清除报价结果成功】，LicenseNo：" + LicenseNo + "，IntentionCompany：" + IntentionCompany);
			}
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
		if ((!"0".equals(IsSingleSubmit)) && (!"1".equals(IsSingleSubmit) && (!"2".equals(IsSingleSubmit)))) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("IsSingleSubmit 参数必须为0或1或2");
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
		map.put("InsuredIdType", InsuredIdType);
		map.put("InsuredMobile", InsuredMobile);
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

	@Override
	public JSONObject getPrecisePrice(String LicenseNo, String IntentionCompany, String Agent, String CustKey, String SecCode) {
		Map<String, Object> quoteMap = CacheConstant.quoteResultInfo.get(LicenseNo);
		JSONObject quoteJson = null;
		for(int i=1;i<=4; i++){
			try {
				Thread.sleep(8000);//休眠10秒，再次进行查询
			} catch (Exception e) {
			}
			logger.info("人保 API，【报价结果查询 " + i + " 次】，LicenseNo：" + LicenseNo);
			if(quoteMap == null){//没有报价结果集
				quoteMap = CacheConstant.quoteResultInfo.get(LicenseNo);
				continue;
			}
			quoteJson = (JSONObject) quoteMap.get(IntentionCompany);
			if(quoteJson == null){
				continue;//没查询到，继续
			}else{
				break;//查询到了，结束
			}
		}
		logger.info("人保 API，【报价结果查询】，LicenseNo：" + LicenseNo + "， IntentionCompany：" + IntentionCompany + "，结果：" + quoteJson);
		return quoteJson;
	}
	
}
