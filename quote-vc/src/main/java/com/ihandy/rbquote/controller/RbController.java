package com.ihandy.rbquote.controller;

import com.ihandy.quote_common.jedis.BinaryJedisCluster;
import com.ihandy.quote_core.bean.Cookie;
import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.service.IRBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/4/28.
 */
@Controller
@RequestMapping("/Rb")
public class RbController {
    private static Logger logger = LoggerFactory.getLogger(RbController.class);

     @Autowired
     private IRBService rbService;
     @Autowired
     private BinaryJedisCluster binaryJedisCluster;
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
    public Map<String, Object> getRenewalInfo( String LicenseNo, int CityCode,String CustKey,String SecCode) {

        try {

        } catch (Exception e) {

        }
        return null;
    }

    /**
     *  根据车牌号获取车架号、发动机号、品牌型号接口
     * @param LicenseNo
     * @return
     */
    @RequestMapping("/getCarInfoByLicenseNo1")
    @Transactional
    @ResponseBody
    public Map<String, Object> getCarInfoByLicenseNo( String LicenseNo ,  Cookie cookie) {
        Map map =new HashMap();
        try {
            CarInfoResponse response = rbService.getCarInfoByLicenseNo(LicenseNo,cookie);
        } catch (Exception e) {

        }
        return map;
    }




}
