package com.ihandy.quote_core.service;


import com.ihandy.quote_core.bean.other.*;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.bean.other.RelaPeopleResponse;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;


import java.util.List;

/**
 * Created by fengwen on 2016/4/29.
 */
public interface IService {
    /**
     * 通过车牌号获取车辆基本信息
     */
    BaseCarInfoResponse getBaseCarInfoByLicenseNo(String licenseNo, int CityCode);
    /**
     * 通过车牌号获取车辆所有关联信息
     */
    CarInfoResponse getAllCarInfoByLicenseNo(String licenseNo, int CityCode);
    /**
     * 通过车辆信息获取险种信息
     */
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo );
    /**
     * 通过车辆信息获取关系人信息
     */
     List<RelaPeopleResponse> getRelaPeopleInfoByCarInfoList(String licenseNo);
    /**
     * 通过车辆信息获取出险信息list
     */
     List<ClaimResponse> getClaimInfoList(String licenseNo);

    
    /**
	 * 上传险种信息
	 * @param LicenseNo
	 *            	车牌号 必须
	 * @param CarOwnersName
	 *            	车主姓名 非必需
	 * @param IdCard
	 *            	车主身份证信息 非必需
	 * @param IsSingleSubmit
	 *            	是否对单个保险公司核保，1=是，0=否 必须
	 * @param IntentionCompany
	 *            	意向投保公司(-1:只报价不核保、0:平安、1:太平洋、2:人保) 必须
	 * @param InsuredName
	 * 				被保险人姓名(与投保人是同一个) 非必需
	 * @param InsuredIdCard
	 * 				被保险人证件号(与投保人是同一个) 非必需
	 * @param InsuredIdType
	 * 				被保险人证件类型（与投保人是同一个） 非必须
	 * @param InsuredMobile
	 * 				被保险人手机号。非必需
	 * @param IsNewCar
	 * 				是否新车（0：否；1：新车） 必须
	 * @param CarType
	 * 				车辆类型：0:客车 1:货车 必须
	 * @param CarUsedType
	 * 				使用性质（0：非营运、1营运） 必须
	 * @param CityCode
	 * 				城市Id(北京：1，重庆：2 ，天津3，成都4，昆明：5) 必须
	 * @param EngineNo
	 * 				发动机号 必须
	 * @param CarVin
	 * 				车架号 必须
	 * @param RegisterDate
	 * 				注册日期(如：2014-08-07) 必须
	 * @param MoldName
	 * 				品牌型号 必须
	 * @param ForceTax
	 * 				交强险+车船税(1:报价交强车船+商业险，0：不报价交强车船（单商业）)  必须
	 * @param BizStartDate
	 * 				商业险开始日期（只有在单商业的情况下 ，这个字段才有意义,而且必须有值） 必须
	 * @param BoLi
	 * 				玻璃单独破碎险，0-不投保，1国产，2进口 必须
	 * @param BuJiMianCheSun
	 * 				不计免赔险(车损) ，0-不投保，1投保 必须
	 * @param BuJiMianDaoQiang
	 * 				不计免赔险(盗抢) ，0-不投保，1投保 必须
	 * @param BuJiMianFuJia
	 * 				不计免赔险(附加险) ，0-不投保，1投保 必须
	 * @param BuJiMianRenYuan
	 * 				不计免赔险(车上人员) ，0-不投保，1投保 必须
	 * @param BuJiMianSanZhe
	 * 				不计免赔险(三者) ，0-不投保，1投保 必须
	 * @param CheDeng
	 * 				倒车镜、车灯单独损坏险，0-不投保，1-国产，2-进口 必须
	 * @param SheShui
	 * 				涉水行驶损失险，0-不投保，1投保 必须
	 * @param HuaHen
	 * 				车身划痕损失险，0-不投保，>0投保(具体金额)（2，000；5，000；10，000；20，000）必须
	 * @param SiJi
	 * 				车上人员责任险(司机) ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；200，000） (金额是单个座位的保额) 必须
	 * @param ChengKe
	 * 				车上人员责任险(乘客) ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；200，000） (金额是单个座位的保额)  必须 
	 * @param CheSun
	 * 				机动车损失保险，0-不投保，1-投保 必须
	 * @param DaoQiang
	 * 				全车盗抢保险，0-不投保，1-投保 必须
	 * @param SanZhe
	 * 				第三者责任保险，0-不投保，>0投保(具体金额)（50，000；100，000；150，000；200，000；300，000；500，000；1，000，000；1，500，000）必须
	 * @param ZiRan
	 * 				自燃损失险，0-不投保，1投保 必须
	 * @param SeatCount
	 * 				核定载客量 非必需
	 * @param TonCount
	 * 				核定载质量 非必需
	 * @param HcSheBeiSunshi
	 * 				新增设备损失险（0:不投，1：投） 非必需
	 * @param HcHuoWuZeRen
	 * 				车上货物责任险（0:不投，>0：保额） 非必需
	 * @param HcFeiYongBuChang
	 * 				修理期间费用补偿险（0:不投，>0：保额） 非必需
	 * @param HcJingShenSunShi
	 * 				精神损失抚慰金责任险（0:不投，>0：保额） 非必需
	 * @param HcSanFangTeYue
	 * 				机动车损失保险无法找到第三方特约险（0:不投，>0：保额） 非必需
	 * @param HcXiuLiChang
	 * 				指定修理厂险（0:不投，>0：保额） 非必需
	 * @param DName
	 * 				设备名称 非必需
	 * @param DQuantity
	 * 				设备数量 非必需
	 * @param DAmount
	 * 				设备金额 非必需
	 * @param PDate
	 * 				购买日期 非必需
	 * @param DName1
	 * 				设备名称 非必需
	 * @param DQuantity1
	 * 				设备数量 非必需
	 * @param DAmount1
	 * 				设备金额 非必需
	 * @param PDate1
	 * 				购买日期 非必需
	 * @param DName2
	 * 				设备名称 非必需
	 * @param DQuantity2
	 * 				设备数量 非必需
	 * @param DAmount2
	 * 				设备金额 非必需
	 * @param PDate2
	 * 				购买日期 非必需
	 * @param DName3
	 * 				设备名称 非必需
	 * @param DQuantity3
	 * 				设备数量 非必需
	 * @param DAmount3
	 * 				设备金额 非必需
	 * @param PDate3
	 * 				购买日期 非必需
	 * @param CustKey
	 * 				客户端标识（用来区分客户）（10-32位字符） 必需
	 * @param Agent
	 * 				调用平台标识 必需
	 * @param SecCode 
	 * 				除了secCode参数之外的所有参数拼接后再加密钥的字符串后的MD5值（32位小写）（壁虎提供）必需
	 * @return
	 */
    PostPrecisePricerResponse postPrecisePrice(String LicenseNo, String CarOwnersName, String IdCard,
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
			String DAmount3, String PDate3, String CustKey, String Agent, String SecCode);

	String commitHeBaoInfo();
}
