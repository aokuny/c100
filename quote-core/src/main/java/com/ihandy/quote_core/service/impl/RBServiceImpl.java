package com.ihandy.quote_core.service.impl;


import com.ihandy.quote_core.bean.*;
import com.ihandy.quote_core.bean.linkedList.Node;
import com.ihandy.quote_core.bean.linkedList.Request;
import com.ihandy.quote_core.bean.linkedList.SortedHSLinkedList;
import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;
import com.ihandy.quote_core.service.IRBService;
import com.ihandy.quote_core.bean.request.IRequest;

import com.ihandy.quote_core.utils.SysConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by fengwen on 2016/4/29.
 */
@Service
public class RBServiceImpl implements IRBService {
    private static Logger logger = LoggerFactory.getLogger(RBServiceImpl.class);
    @Autowired
    private  IRequest iRequest;
    public CarInfoResponse getCarInfoByLicenseNo(String licenseNo, Cookie cookie) {
        SortedHSLinkedList<Integer,Request> list = new SortedHSLinkedList<Integer,Request>();
        CarInfoResponse  carInfoResponse =null;
        //填充链表id和 data中的url信息
        Map initMap = SysConfigInfo.getSelectedFields("RB_");
        Set<String> Key = initMap.keySet();
        for (Iterator it = Key.iterator(); it.hasNext();) {
            String url = (String) it.next();
            Integer num = (Integer) initMap.get(url);
            Request request = iRequest.combineRequest(url);
            list.insertNode(num,request);
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Node position = list.getHead();
        int count =1;
        while(position!=null){

            Request request = null;
            if(null != position){
                if(null != position.id) {
                    request = (Request) position.data;
                    int nodeId = (Integer) position.id;
                    System.out.println("id = " + nodeId + " ------->  data = " + position.data + "\n");
                    if (count == 1) {
                        //第一次发出页面请求
                        Map map = new HashMap();
                        map.put("carNo", licenseNo);
                        request.setRequestParam(map);
                    }
                    Map<String, Object> map = iRequest.sendPostAndParseResponseForCarInfo(nodeId, request);
                    position = position.next;//获取下一个节点;
                    if (position == null) {
                        //即将跳出循环，返回信息
                        returnMap.put(nodeId + "", map.get("responseResult"));//将最后一次请求返回的结果写入最终结果中
                        break;
                    } else {
                        //赋值下一页面需求参数
                        Request requestNext = (Request) position.data;
                        Map requestMap = (Map) map.get("requestParam");
                        requestNext.setRequestParam(requestMap);
                        //将返回信息赋值到 carInfoResponse 对象
                        returnMap.put(nodeId + "", map.get("responseResult"));
                    }
                    count++;
                }
                else{
                    position = position.next;//获取下一个节点;
                }
            }
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

    public void getAdminTest(String admin, String password) {
        SortedHSLinkedList<Integer,Request> list = new SortedHSLinkedList<Integer,Request>();
        CarInfoResponse  carInfoResponse =null;
        //填充链表id和 data中的url信息
        Map initMap = SysConfigInfo.getSelectedFields("TEST_");
        Set<String> Key = initMap.keySet();
        for (Iterator it = Key.iterator(); it.hasNext();) {
            String url = (String) it.next();
            Integer num = (Integer) initMap.get(url);
            Request request = iRequest.combineRequest(url);
            list.insertNode(num,request);
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Node position = list.getHead();
        int count =1;
        while(position!=null) {

            Request request = null;
            if (null != position) {
                if (null != position.id) {
                    request = (Request) position.data;
                    int nodeId = (Integer) position.id;
                    System.out.println("id = " + nodeId + " ------->  data = " + position.data + "\n");
                    if (count == 1) {
                        //第一次发出页面请求
                        Map map = new HashMap();
                        map.put("vc2Username", admin);
                        map.put("vc2Userpwd", password);
                        request.setRequestParam(map);
                    }
                    Map<String, Object> map = iRequest.sendPostAndParseResponseForCarInfo(nodeId, request);
                    position = position.next;//获取下一个节点;
                    if (position == null) {
                        //即将跳出循环，返回信息
                        returnMap.put(nodeId + "", map.get("responseResult"));//将最后一次请求返回的结果写入最终结果中
                        break;
                    } else {
                        //赋值下一页面需求参数
                        Request requestNext = (Request) position.data;
                        Map requestMap = (Map) map.get("requestParam");
                        requestNext.setRequestParam(requestMap);
                        //将返回信息赋值到 carInfoResponse 对象
                        returnMap.put(nodeId + "", map.get("responseResult"));
                    }
                    count++;
                } else {
                    position = position.next;//获取下一个节点;
                }
            }
        }
    }

}
