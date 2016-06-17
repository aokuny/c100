package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.other.SaveQuoteResponse;

/**
 * Created by fengwen on 2016/6/6.
 */
public interface IAxatpService {
    /**
     * 查询报价信息
     *
     * @param licenseNo  (车牌号)
     * @param engineNo   (发动机号)
     * @param vehicleFrameNo (车架号)
     * @param licenseOwner (车主)
     * @param certificateNo (身份证号)
     * @param cheSun (车损   投保:OD N:不投保:N)
     * @param sanZhe (三者   投保:50000/100000/150000/200000/300000/500000/1000000  不投保:N)
     * @param siJi (司机     投保:10000/20000/50000/10000  不投保:N)
     * @param chengKe (乘客  投保:10000/20000/50000/10000  不投保:N)
     * @param daoQiang (盗抢 投保:THEFT 不投保:N）
     * @param boLi (玻璃     投保:1  不投保:N）
     * @param huaHen (划痕   投保:2000/5000/10000  不投保:N）
     * @param ziRan  (自燃   投保:N  不投保:NDNE）
     * @param sheShui (涉水  投保:N  不投保:FEDPC)
     * @return
     */
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo,String engineNo,String vehicleFrameNo,String licenseOwner,String mobilePhone,String certificateNo,String cheSun,String sanZhe,String siJi,String chengKe,String daoQiang,String boLi,String huaHen,String ziRan,String sheShui );

    void createOrderByCarInfo(String licenseNo,String engineNo,String vehicleFrameNo,String licenseOwner,String mobilePhone,String certificateNo,String cheSun,String sanZhe,String siJi,String chengKe,String daoQiang,String boLi,String huaHen,String ziRan,String sheShui );


}
