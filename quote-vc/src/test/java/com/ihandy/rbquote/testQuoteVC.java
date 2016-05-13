package com.ihandy.rbquote;

import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.service.IService;
import net.sf.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.Inflater;

/**
 * Created by fengwen on 2016/5/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-config.xml")
public class testQuoteVC   {
    @Resource(name="RBServiceImpl")
    private IService irbService;

    @Test
    public void testGetCarInfoByLicenseNo() {

        irbService.getCarInfoByLicenseNo("123","02");


    }

    @Test
    public void testGetAdmin() {
        String html = "{\"totalRecords\":4,\"data\":[{\"frameNo\":\"LGXC16DF4A0169664\",\"lastDamagedBI\":0,\"noDamYearsCI\":null,\"policyNo\":\"PDAT20141102T000135272\",\"endDate\":{\"date\":25,\"day\":4,\"timezoneOffset\":-480,\"year\":115,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1435161600000,\"nanos\":0},\"lastDamagedCI\":null,\"noDamYearsBI\":0,\"riskCode\":\"DAT\",\"licenseNo\":\"京P55M11\",\"engineNo\":\"4LA4D8297\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LGXC16DF4A0169664\",\"lastDamagedBI\":2,\"noDamYearsCI\":null,\"policyNo\":\"PDAT20151102T000182528\",\"endDate\":{\"date\":25,\"day\":6,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466784000000,\"nanos\":0},\"lastDamagedCI\":null,\"noDamYearsBI\":0,\"riskCode\":\"DAT\",\"licenseNo\":\"京P55M11\",\"engineNo\":\"4LA4D8297\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LGXC16DF4A0169664\",\"lastDamagedBI\":null,\"noDamYearsCI\":0,\"policyNo\":\"PDZA20151102T000186793\",\"endDate\":{\"date\":25,\"day\":6,\"timezoneOffset\":-480,\"year\":116,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1466784000000,\"nanos\":0},\"lastDamagedCI\":0,\"noDamYearsBI\":null,\"riskCode\":\"DZA\",\"licenseNo\":\"京P55M11\",\"engineNo\":\"4LA4D8297\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LGXC16DF4A0169664\",\"lastDamagedBI\":null,\"noDamYearsCI\":1,\"policyNo\":\"PDZA20141102T000140848\",\"endDate\":{\"date\":25,\"day\":4,\"timezoneOffset\":-480,\"year\":115,\"month\":5,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1435161600000,\"nanos\":0},\"lastDamagedCI\":0,\"noDamYearsBI\":null,\"riskCode\":\"DZA\",\"licenseNo\":\"京P55M11\",\"engineNo\":\"4LA4D8297\",\"carKindCode\":\"客车\"}],\"startIndex\":1,\"recordsReturned\":10}";
        Map returnPolicyNoMap = new HashMap<>();
        Map map  = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map)jsonArray.get(0);
        JSONArray jsonArray2 = (JSONArray)map1.get("data");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        int thisYear = Integer.parseInt( sdf.format(date));
        for(int i=0;i<jsonArray2.size();i++){
            Map map2 = (Map)jsonArray2.get(i);
            String policyNo = map2.get("policyNo").toString();
            String riskCode = map2.get("riskCode").toString();
            int year = Integer.parseInt(policyNo.substring(4,8));

            if(riskCode.equals("DAT") && year+1 == thisYear){
                    returnPolicyNoMap.put("DAT",policyNo);
            }
            else if(riskCode.equals("DZA") && year+1 == thisYear){
                    returnPolicyNoMap.put("DZA",policyNo);
            }else{}
            System.out.println("policyNo = "+policyNo+" riskCode = "+riskCode+"\n");
        }
        System.out.println( "DAT = " +returnPolicyNoMap.get("DAT").toString() + "  DZA = " + returnPolicyNoMap.get("DZA"));

    }
}
