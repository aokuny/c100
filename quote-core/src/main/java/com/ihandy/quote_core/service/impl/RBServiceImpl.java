package com.ihandy.quote_core.service.impl;


import com.ihandy.quote_core.bean.*;
import com.ihandy.quote_core.bean.response.CarInfoResponse;
import com.ihandy.quote_core.bean.response.ClaimResponse;
import com.ihandy.quote_core.bean.response.QuoteResponse;
import com.ihandy.quote_core.dao.IRBDao;
import com.ihandy.quote_core.service.IRBService;
import com.ihandy.quote_core.service.IRequest;

import com.ihandy.quote_core.utils.SysConfigInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by fengwen on 2016/4/29.
 */
@Service
public class RBServiceImpl implements IRBService {
    private  String[] carInfoStrArr={"a.html","b.html","c.html"};
    private  Integer[] carInfoIntArr={1,3,2};
    @Autowired
    private IRBDao iRbDao;
    @Autowired
    private  IRequest iRequest;
    public CarInfoResponse getCarInfoByLicenseNo(String licenseNo, Cookie cookie) {
        SortedHSLinkedList<Integer,Request> list = new SortedHSLinkedList<Integer,Request>();
        CarInfoResponse  carInfoResponse =null;
        Map initMap = SysConfigInfo.getSelectedFields("RB_");
        Set<String> Key = initMap.keySet();
        for (Iterator it = Key.iterator(); it.hasNext();) {
            String s = (String) it.next();

           // SysConfigInfo.getRB_index();
        }
        //SysConfigInfo.
        //填充链表id和部分url data
        for(int i=0;i<carInfoIntArr.length;i++){
                    Request request = iRequest.combineRequest(carInfoStrArr[i]);
                    list.insertNode(i,request);
        }
        Node position = list.getHead();
        int count =1;
        while(position!=null){
            position = position.next;
            Request request = (Request) position.data;
            int nodeId = (Integer) position.id;
            System.out.println("id = "+ nodeId + " ------->  data = " + position.data+"\n");
            if(count==1){
                //第一次发出页面请求
                Map map=new HashMap();
                map.put("carNo",licenseNo);
                request.setRequestParam(map);
            }
            Map<String, Object> map = iRequest.sendPostAndParseResponseForCarInfo(nodeId ,request);
            Map<String, Object> returnMap = new HashMap<String, Object>();
            position = position.next;
            if(position == null){
                //即将跳出循环，返回信息
            }else {
                //赋值下一页面需求参数
                Request requestNext = (Request) position.data;
                Map requestMap =(Map) map.get("requestParam");
                requestNext.setRequestParam(requestMap);
                //将返回信息赋值到 carInfoResponse 对象
                returnMap.put(nodeId+"", map.get("responseResult"));
            }
            count++;
        }

        return carInfoResponse;
    }

    public QuoteResponse getQuoteInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }

    public ClaimResponse getClaimInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }

    public List<ClaimResponse> getClaimInfoList(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }



}
