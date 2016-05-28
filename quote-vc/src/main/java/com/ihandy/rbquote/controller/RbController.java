package com.ihandy.rbquote.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.ihandy.qoute_common.springutils.SpringMVCUtils;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.service.IQuoteService;
import com.ihandy.quote_core.service.IService;

/**
 * Created by fengwen on 2016/4/28.
 */
@Controller
@RequestMapping("/Rb")
public class RbController {
	private static Logger logger = LoggerFactory.getLogger(RbController.class);

	@Autowired
	private IService rbService;

	@Resource(name = "quoteServiceImpl")
	private IQuoteService quoteServiceImpl;

	/**
	 * 查询续保信息
	 *
	 * @param LicenseNo
	 * @param CityCode
	 * @param CustKey
	 * @param SecCode
	 * @return
	 */
	@RequestMapping("/getRenewalInfo")
	@Transactional
	@ResponseBody
	public Map<String, Object> getRenewalInfo(String LicenseNo, int CityCode, String CustKey, String SecCode) {

		try {

			// CarInfoResponse response =
			// rbService.getBaseCarInfoByLicenseNo(LicenseNo);

		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 根据车牌号获取车架号、发动机号、品牌型号接口
	 * 
	 * @param LicenseNo
	 * @return
	 */
	@RequestMapping("/getCarInfoByLicenseNo")
	@Transactional
	@ResponseBody
	public Map<String, Object> getCarInfoByLicenseNo(String LicenseNo) {
		Map map = new HashMap();
		try {

			// CarInfoResponse response =
			// rbService.getCarInfoByLicenseNo(LicenseNo, "02");

		} catch (Exception e) {

		}
		return map;
	}

	/**
	 * 上传险种信息
	 * 
	 * @param LicenseNo
	 *            车牌号 必须
	 * @param CarOwnersName
	 *            车主姓名 非必需
	 * @param IdCard
	 *            车主身份证信息 非必需
	 * @param IsSingleSubmit
	 *            是否对单个保险公司核保，1=是，0=否 必须
	 * @param IntentionCompany
	 *            意向投保公司(-1:只报价不核保、0:平安、1:太平洋、2:人保) 必须
	 * @param InsuredName
	 *            被保险人姓名(与投保人是同一个) 非必需
	 * @param InsuredIdCard
	 *            被保险人证件号(与投保人是同一个) 非必需
	 * @param InsuredIdType
	 *            被保险人证件类型（与投保人是同一个） 非必须
	 * @param InsuredMobile
	 *            被保险人手机号。非必需
	 * @param IsNewCar
	 *            是否新车（0：否；1：新车） 必须
	 * @param CarType
	 *            车辆类型：0:客车 1:货车 必须
	 * @param CarUsedType
	 *            使用性质（0：非营运、1营运） 必须
	 * @param CityCode
	 *            城市Id(北京：1，重庆：2 ，天津3，成都4，昆明：5) 必须
	 * @param EngineNo
	 *            发动机号 必须
	 * @param CarVin
	 *            车架号 必须
	 * @param RegisterDate
	 *            注册日期(如：2014-08-07) 必须
	 * @param MoldName
	 *            品牌型号 必须
	 * @param ForceTax
	 *            交强险+车船税(1:报价交强车船+商业险，0：不报价交强车船（单商业）) 必须
	 * @param BizStartDate
	 *            商业险开始日期（只有在单商业的情况下 ，这个字段才有意义,而且必须有值） 必须
	 * @param BoLi
	 *            玻璃单独破碎险，0-不投保，1国产，2进口 必须
	 * @param BuJiMianCheSun
	 *            不计免赔险(车损) ，0-不投保，1投保 必须
	 * @param BuJiMianDaoQiang
	 *            不计免赔险(盗抢) ，0-不投保，1投保 必须
	 * @param BuJiMianFuJia
	 *            不计免赔险(附加险) ，0-不投保，1投保 必须
	 * @param BuJiMianRenYuan
	 *            不计免赔险(车上人员) ，0-不投保，1投保 必须
	 * @param BuJiMianSanZhe
	 *            不计免赔险(三者) ，0-不投保，1投保 必须
	 * @param CheDeng
	 *            倒车镜、车灯单独损坏险，0-不投保，1-国产，2-进口 必须
	 * @param SheShui
	 *            涉水行驶损失险，0-不投保，1投保 必须
	 * @param HuaHen
	 *            车身划痕损失险，0-不投保，>0投保(具体金额)（2，000；5，000；10，000；20，000）必须
	 * @param SiJi
	 *            车上人员责任险(司机)
	 *            ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；
	 *            200，000） (金额是单个座位的保额) 必须
	 * @param ChengKe
	 *            车上人员责任险(乘客)
	 *            ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；
	 *            200，000） (金额是单个座位的保额) 必须
	 * @param CheSun
	 *            机动车损失保险，0-不投保，1-投保 必须
	 * @param DaoQiang
	 *            全车盗抢保险，0-不投保，1-投保 必须
	 * @param SanZhe
	 *            第三者责任保险，0-不投保，>0投保(具体金额)（50，000；100，000；150，000；200，000；300，
	 *            000；500，000；1，000，000；1，500，000）必须
	 * @param ZiRan
	 *            自燃损失险，0-不投保，1投保 必须
	 * @param SeatCount
	 *            核定载客量 非必需
	 * @param TonCount
	 *            核定载质量 非必需
	 * @param HcSheBeiSunshi
	 *            新增设备损失险（0:不投，1：投） 非必需
	 * @param HcHuoWuZeRen
	 *            车上货物责任险（0:不投，>0：保额） 非必需
	 * @param HcFeiYongBuChang
	 *            修理期间费用补偿险（0:不投，>0：保额） 非必需
	 * @param HcJingShenSunShi
	 *            精神损失抚慰金责任险（0:不投，>0：保额） 非必需
	 * @param HcSanFangTeYue
	 *            机动车损失保险无法找到第三方特约险（0:不投，>0：保额） 非必需
	 * @param HcXiuLiChang
	 *            指定修理厂险（0:不投，>0：保额） 非必需
	 * @param DName
	 *            设备名称 非必需
	 * @param DQuantity
	 *            设备数量 非必需
	 * @param DAmount
	 *            设备金额 非必需
	 * @param PDate
	 *            购买日期 非必需
	 * @param DName1
	 *            设备名称 非必需
	 * @param DQuantity1
	 *            设备数量 非必需
	 * @param DAmount1
	 *            设备金额 非必需
	 * @param PDate1
	 *            购买日期 非必需
	 * @param DName2
	 *            设备名称 非必需
	 * @param DQuantity2
	 *            设备数量 非必需
	 * @param DAmount2
	 *            设备金额 非必需
	 * @param PDate2
	 *            购买日期 非必需
	 * @param DName3
	 *            设备名称 非必需
	 * @param DQuantity3
	 *            设备数量 非必需
	 * @param DAmount3
	 *            设备金额 非必需
	 * @param PDate3
	 *            购买日期 非必需
	 * @param CustKey
	 *            客户端标识（用来区分客户）（10-32位字符） 必需
	 * @param Agent
	 *            调用平台标识 必需
	 * @param SecCode
	 *            除了secCode参数之外的所有参数拼接后再加密钥的字符串后的MD5值（32位小写）（壁虎提供）必需
	 * @return
	 */
	@RequestMapping("/postPrecisePrice")
	@Transactional
	@ResponseBody
	public void postPrecisePrice(HttpServletRequest request, HttpServletResponse response, String LicenseNo,
			String CarOwnersName, String IdCard, String IsSingleSubmit, String IntentionCompany, String InsuredName,
			String InsuredIdCard, String InsuredIdType, String InsuredMobile, String IsNewCar, String CarType,
			String CarUsedType, String CityCode, String EngineNo, String CarVin, String RegisterDate, String MoldName,
			String ForceTax, String BizStartDate, String BoLi, String BuJiMianCheSun, String BuJiMianDaoQiang,
			String BuJiMianFuJia, String BuJiMianRenYuan, String BuJiMianSanZhe, String CheDeng, String SheShui,
			String HuaHen, String SiJi, String ChengKe, String CheSun, String DaoQiang, String SanZhe, String ZiRan,
			String SeatCount, String TonCount, String HcSheBeiSunshi, String HcHuoWuZeRen, String HcFeiYongBuChang,
			String HcJingShenSunShi, String HcSanFangTeYue, String HcXiuLiChang, String DName, String DQuantity,
			String DAmount, String PDate, String DName1, String DQuantity1, String DAmount1, String PDate1,
			String DName2, String DQuantity2, String DAmount2, String PDate2, String DName3, String DQuantity3,
			String DAmount3, String PDate3, String CustKey, String Agent, String SecCode) {
		// 判断是否是get请求
		if (request.getMethod().equals("GET")) {
			try {
				if (StringUtils.isNotBlank(LicenseNo)) {
					LicenseNo = new String(LicenseNo.getBytes("iso8859-1"), "GBK");
				}
				if (StringUtils.isNotBlank(CarOwnersName)) {
					CarOwnersName = new String(CarOwnersName.getBytes("iso8859-1"), "GBK");
				}
				if (StringUtils.isNotBlank(InsuredName)) {
					InsuredName = new String(InsuredName.getBytes("iso8859-1"), "GBK");
				}
				if (StringUtils.isNotBlank(MoldName)) {
					MoldName = new String(MoldName.getBytes("iso8859-1"), "GBK");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		PostPrecisePricerResponse postPrecisePricerResponse = new PostPrecisePricerResponse();
		try {
			postPrecisePricerResponse = quoteServiceImpl.postPrecisePrice(LicenseNo, CarOwnersName, IdCard,
					IsSingleSubmit, IntentionCompany, InsuredName, InsuredIdCard, InsuredIdType, InsuredMobile,
					IsNewCar, CarType, CarUsedType, CityCode, EngineNo, CarVin, RegisterDate, MoldName, ForceTax,
					BizStartDate, BoLi, BuJiMianCheSun, BuJiMianDaoQiang, BuJiMianFuJia, BuJiMianRenYuan,
					BuJiMianSanZhe, CheDeng, SheShui, HuaHen, SiJi, ChengKe, CheSun, DaoQiang, SanZhe, ZiRan, SeatCount,
					TonCount, HcSheBeiSunshi, HcHuoWuZeRen, HcFeiYongBuChang, HcJingShenSunShi, HcSanFangTeYue,
					HcXiuLiChang, DName, DQuantity, DAmount, PDate, DName1, DQuantity1, DAmount1, PDate1, DName2,
					DQuantity2, DAmount2, PDate2, DName3, DQuantity3, DAmount3, PDate3, CustKey, Agent, SecCode);
			logger.info("PICC API ，【上传险种信息响应成功】，LicenseNo：" + LicenseNo);
		} catch (Exception e) {
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("接口报错：" + e.getMessage());
			logger.error("PICC API ，【上传险种信息报错】，" + e.getMessage());
		}
		SpringMVCUtils.renderJson(response, postPrecisePricerResponse);
	}

	/**
	 * 查询报价结果
	 * 
	 * @param request
	 * @param response
	 * @param LicenseNo
	 * @param IntentionCompany
	 * @param Agent
	 * @param CustKey
	 * @param SecCode
	 */
	@RequestMapping("/getPrecisePrice")
	@ResponseBody
	public void postPrecisePrice(HttpServletRequest request, HttpServletResponse response, String LicenseNo, String IntentionCompany, String Agent, String CustKey, String SecCode) {
		// 判断是否是get请求
		if (request.getMethod().equals("GET")) {
			try {
				if (StringUtils.isNotBlank(LicenseNo)) {
					LicenseNo = new String(LicenseNo.getBytes("iso8859-1"), "GBK");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		JSONObject result = null;
		try {
			 result = quoteServiceImpl.getPrecisePrice(LicenseNo, IntentionCompany, Agent, CustKey, SecCode);
			logger.info("PICC API ，【查询报价结果响应成功】，LicenseNo：" + LicenseNo);
		} catch (Exception e) {
			logger.error("PICC API ，【查询报价结果响应成功】，" + e.getMessage());
		}
		if(result == null){
			PostPrecisePricerResponse postPrecisePricerResponse = new PostPrecisePricerResponse();
			postPrecisePricerResponse.setBusinessStatus("-1");
			postPrecisePricerResponse.setStatusMessage("报价信息获取失败");
			SpringMVCUtils.renderJson(response, postPrecisePricerResponse);
			return;
		}
		SpringMVCUtils.renderJson(response, result);
	}

}
