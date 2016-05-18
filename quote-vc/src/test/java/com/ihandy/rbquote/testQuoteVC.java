package com.ihandy.rbquote;


import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.other.BaseCarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.service.IService;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.Inflater;

/**
 * Created by fengwen on 2016/5/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-config.xml")
public class testQuoteVC   {

    private static Logger logger = LoggerFactory.getLogger(testQuoteVC.class);
    @Resource(name="RBServiceImpl")
    private IService irbService;

    @Test
    public void testGetCarInfoByLicenseNo() {

       // irbService.getCarInfoByLicenseNo("123","02");


    }

    @Test
    public void testGetBaodanList() {
        String html = "{\"totalRecords\":4,\"data\":[{\"frameNo\":\"LFV2A21K1E4106774\",\"lastDamagedBI\":0,\"noDamYearsCI\":null,\"policyNo\":\"PDAT20151102T000098018\",\"endDate\":{\"date\":24,\"day\":2,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464019200000,\"nanos\":0},\"lastDamagedCI\":null,\"noDamYearsBI\":0,\"riskCode\":\"DAT\",\"licenseNo\":\"京QA5M75\",\"engineNo\":\"Z28075\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LFV2A21K1E4106774\",\"lastDamagedBI\":null,\"noDamYearsCI\":0,\"policyNo\":\"PDZA20151102T000100166\",\"endDate\":{\"date\":24,\"day\":2,\"timezoneOffset\":-480,\"year\":116,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1464019200000,\"nanos\":0},\"lastDamagedCI\":0,\"noDamYearsBI\":null,\"riskCode\":\"DZA\",\"licenseNo\":\"京QA5M75\",\"engineNo\":\"Z28075\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LFV2A21K1E4106774\",\"lastDamagedBI\":2,\"noDamYearsCI\":null,\"policyNo\":\"PDAT20161102T000134751\",\"endDate\":{\"date\":24,\"day\":3,\"timezoneOffset\":-480,\"year\":117,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1495555200000,\"nanos\":0},\"lastDamagedCI\":null,\"noDamYearsBI\":0,\"riskCode\":\"DAT\",\"licenseNo\":\"京QA5M75\",\"engineNo\":\"Z28075\",\"carKindCode\":\"客车\"},{\"frameNo\":\"LFV2A21K1E4106774\",\"lastDamagedBI\":null,\"noDamYearsCI\":0,\"policyNo\":\"PDZA20161102T000139126\",\"endDate\":{\"date\":24,\"day\":3,\"timezoneOffset\":-480,\"year\":117,\"month\":4,\"hours\":0,\"seconds\":0,\"minutes\":0,\"time\":1495555200000,\"nanos\":0},\"lastDamagedCI\":0,\"noDamYearsBI\":null,\"riskCode\":\"DZA\",\"licenseNo\":\"京QA5M75\",\"engineNo\":\"Z28075\",\"carKindCode\":\"客车\"}],\"startIndex\":1,\"recordsReturned\":10}";
        Map returnPolicyNoMap = new HashMap<>();
        Map map  = new HashMap<>();
        map = StringBaseUtils.parseJSON2Map(html);
        JSONArray jsonArray = new JSONArray();
        jsonArray = JSONArray.fromObject(map);
        Map map1 = (Map)jsonArray.get(0);
        JSONArray jsonArray2 = (JSONArray)map1.get("data");

      //  SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
      //  Date date = new Date();
      //  int thisYear = Integer.parseInt( sdf.format(date));

        int maxSyYear = 0;
        int maxJqYear = 0;
        for(int i=0;i<jsonArray2.size();i++){
            Map map2 = (Map)jsonArray2.get(i);
            String policyNo = map2.get("policyNo").toString();
            String riskCode = map2.get("riskCode").toString();
            Map  expireDate = (Map) map2.get("endDate");
            int year = Integer.parseInt(policyNo.substring(4,8));
            String year1 = Integer.parseInt(expireDate.get("year").toString())+1900+"";
            String month1="";
            String day1="";
            int month = Integer.parseInt(expireDate.get("month").toString())+1;
            if(month<10){
                month1 ="0"+month;
            }else{
                month1 = month+"";
            }
            int day = Integer.parseInt(expireDate.get("date").toString());
            if(day<10){
                day1 ="0"+day;
            }else{
                day1 = day+"";
            }
            String expireDateStr = year1+"-"+month1+"-"+day1;

            if(riskCode.equals("DAT")){
                if(year>maxSyYear){
                    returnPolicyNoMap.put("bizNo",policyNo);//上次商业险保单号
                    //"date":25,"day":4,"timezoneOffset":-480,"year":115,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1435161600000,"nanos":0

                    returnPolicyNoMap.put("BusinessExpireDate",expireDateStr);//商业险到期日期
                    maxSyYear = year;
                }
            }
            else if(riskCode.equals("DZA") ){
                if(year>maxJqYear) {
                    returnPolicyNoMap.put("DZA",policyNo);//上次交强险保单号
                    returnPolicyNoMap.put("ForceExpireDate",expireDateStr);//交强险到期日期
                    maxJqYear = year;
                }
            }
            //返回最终需要的车辆基本信息
            try {
                Map map3 = (Map) jsonArray2.get(0);
                returnPolicyNoMap.put("LicenseNo", map3.get("licenseNo"));//车牌号
                returnPolicyNoMap.put("EngineNo", map3.get("engineNo"));//发动机号
                returnPolicyNoMap.put("CarVin", map3.get("frameNo"));//车架号
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 在查询保单表中解析车辆车牌号/发动机号/车架号失败】");
            }
        }
        System.out.println( "bizNo = " +returnPolicyNoMap.get("bizNo").toString() + "  DZA = " + returnPolicyNoMap.get("DZA"));

    }

    @Test
    public void testJsoupTabel(){
        Map returnMap = new HashMap<>();
        Map lastResult = new HashMap<>();
        returnMap.put("nextParams", null);
        File file=new File("d:/3.html");
        Document doc=null;
        try{
            doc = Jsoup.parse(file, "GB2312");
        }catch (Exception e){}

        Elements trs = doc.getElementById("BIDemandClaim").select("tr");
        for(int i = 1;i<trs.size();i++){
            Elements tds = trs.get(i).select("td");
            Map lastResult1 = new HashMap<>();

            lastResult1.put("EndCaseTime", tds.get(5).select("input").val());
            lastResult1.put("PayCompanyName", SysConfigInfo.PICC_NAME);
            lastResult1.put("PayAmount", tds.get(8).select("input").val());
            lastResult1.put("LossTime", tds.get(4).select("input").val());
            lastResult.put(i,lastResult1);
        }
        returnMap.put("lastResult",lastResult);

        List<ClaimResponse> ClaimResponseList = new ArrayList<>();
        Map lastResultMap = (Map) returnMap.get("lastResult");
        Iterator it = lastResultMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            ClaimResponse claimResponse = new ClaimResponse();
            Map value = (Map) entry.getValue();
            System.out.println(" value = " + value);
            claimResponse.setEndCaseTime( value.get("EndCaseTime").toString());
            claimResponse.setLossTime( value.get("LossTime").toString());
            claimResponse.setPayAmount( Double.parseDouble(value.get("PayAmount").toString()) );
            claimResponse.setPayCompanyName( value.get("PayCompanyName").toString());
            ClaimResponseList.add(claimResponse);
        }
        System.out.println("ClaimResponseList = "+ClaimResponseList);
     /*   for(int i = 1;i<trs.size();i++){
            Elements tds = trs.get(i).select("td");
            for(int j = 0;j<tds.size();j++){
                String text = tds.get(j).select("input").val();
                System.out.println(j+"---->"+trs.get(0).select("th").get(j).text() +"---->"+text+"\n");
            }
        }*/
    }


    @Test
    public void testCitemCar(){

        String html ="\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<title></title>\n" +
                "\t\t\n" +
                "\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "  var contextRootPath = \"/prpall\";\n" +
                "var guangDongFlag = false;\n" +
                "var guangZhouFlag = false;\n" +
                "\n" +
                "var riskCodeGD= \"DAA\";\n" +
                "</script>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<table width=\"100%\" border=\"0\" class=\"fix_table\">\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t是否推荐送修：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.monopolyFlag\" title=\"0\" id=\"prpCitemCar.monopolyFlag1\" value=\"1\"><label for=\"prpCitemCar.monopolyFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.monopolyFlag\" title=\"0\" id=\"prpCitemCar.monopolyFlag0\" value=\"0\" checked><label for=\"prpCitemCar.monopolyFlag0\">否</label>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t<td class=\"left4\" id=\"monopolyCode\" id=\"monopolyCode\" style=\"display: none\">\n" +
                "\t\t\t\t\t\t推荐送修代码：\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"right3\" id=\"monopolyName\" id=\"monopolyName\" style=\"display: none\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.monopolyCode\"\n" +
                "\t\t\t\t\t\t\t\tid=\"prpCitemCar.monopolyCode\" value=\"\" class='codecode1_1'\n" +
                "\t\t\t\t\t\t\t\ttitle=\"\" width=\"100\"> <input type=\"text\"\n" +
                "\t\t\t\t\t\t\t\tname=\"prpCitemCar.monopolyName\" id=\"prpCitemCar.monopolyName\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"\" title=\"\"\n" +
                "\t\t\t\t\t\t\t\tclass='input_w w_p35' width=\"100\"> </span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<td colspan=\"3\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<input type=\"hidden\" name=\"prpCitemCar.id.itemNo\" id=\"prpCitemCar.id.itemNo\" value=\"1\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80'>\n" +
                "\t\t\t\t\t<input type=\"hidden\" name=\"oldClauseType\" id=\"oldClauseType\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.actualValue' id=\"prpCitemCar.actualValue\"\n" +
                "\t\t\t\t\t\tvalue=\"55563.20\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.carId' id=\"prpCitemCar.carId\"\n" +
                "\t\t\t\t\t\tvalue=\"          \">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.versionNo' id=\"prpCitemCar.versionNo\"\n" +
                "\t\t\t\t\t\tvalue=\"    \">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCmainCar.newDeviceFlag' id=\"prpCmainCar.newDeviceFlag\"\n" +
                "\t\t\t\t\t\tvalue=\"\">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.otherNature' id=\"prpCitemCar.otherNature\"\n" +
                "\t\t\t\t\t\tvalue=\"\">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.flag' id=\"prpCitemCar.flag\" value=\"    CC B  \">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='newCarFlagValue' id='newCarFlagValue' value=\"\" />\n" +
                "\t\t\t\t\t<input type=\"hidden\" name='prpCitemCar.discountType' id='prpCitemCar.discountType'\n" +
                "\t\t\t\t\t\tvalue=\"\" />\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<input type=\"button\" class=\"button_ty\" name=\"queryCarInfChangeBtn\" id=\"queryCarInfChangeBtn\" value=\"车型变动查询\" onclick=\"ItemCar.getCarInfByChange();\"/>\t\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t    \n" +
                "\t\t\t\t  \n" +
                "\t\t\t\t\t<!--北分单子保单个性：添加保单类型-->\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<span id=\"newnetsalesyesORno\">\n" +
                "\t\t\t\t\t   <input type=\"radio\" disabled=\"\" name=\"prpCmainCommon.netsales\" title=\"0\" id=\"prpCmainCommon.netsales1\" value=\"1\"><label for=\"prpCmainCommon.netsales1\">电子保单</label><input type=\"radio\" disabled=\"\" name=\"prpCmainCommon.netsales\" title=\"0\" id=\"prpCmainCommon.netsales0\" value=\"0\" checked><label for=\"prpCmainCommon.netsales0\">监制保单</label>\n" +
                "\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<!-- end -->\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t是否新车：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.newCarFlag\" title=\"\" id=\"prpCitemCar.newCarFlag1\" value=\"1\"><label for=\"prpCitemCar.newCarFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.newCarFlag\" title=\"\" id=\"prpCitemCar.newCarFlag0\" value=\"0\" checked><label for=\"prpCitemCar.newCarFlag0\">否</label>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t是否外地车：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.noNlocalFlag\" title=\"\" id=\"prpCitemCar.noNlocalFlag1\" value=\"1\"><label for=\"prpCitemCar.noNlocalFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.noNlocalFlag\" title=\"\" id=\"prpCitemCar.noNlocalFlag0\" value=\"0\" checked><label for=\"prpCitemCar.noNlocalFlag0\">否</label>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t是否已上牌照：\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\n" +
                "\n" +
                "\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t            \t\n" +
                "\t\t\t\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.licenseFlag\" onchange=\"ItemCar.changeLicenseFlag();\" onclick=\"\" title=\"\" id=\"prpCitemCar.licenseFlag1\" value=\"1\" checked><label for=\"prpCitemCar.licenseFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.licenseFlag\" onchange=\"ItemCar.changeLicenseFlag();\" onclick=\"\" title=\"\" id=\"prpCitemCar.licenseFlag0\" value=\"0\"><label for=\"prpCitemCar.licenseFlag0\">否</label>\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t号牌号码：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input class=\"input_w w_p80\" type=\"text\" name=\"prpCitemCar.licenseNo\"\n" +
                "\t\t\t\t\t\tid=\"prpCitemCar.licenseNo\" value=\"京N3HU88\" title=\"\"\n" +
                "\t\t\t\t\t\treadonly=\"readonly\" />\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t号牌种类：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name=\"codeLicenseType\" id=\"codeLicenseType\" value=\"\" />\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t<input type=\"hidden\" name=\"prpCitemCar.licenseType\" id=\"prpCitemCar.licenseType\"\n" +
                "\t\t\t\t\t\t\tvalue=\"02 \" />\n" +
                "\t\t\t\t\t\t<input type=\"text\" name=\"LicenseTypeDes\" id=\"LicenseTypeDes\" class=\"codecode1\"\n" +
                "\t\t\t\t\t\t\treadonly=\"readonly\" title=\"\"\n" +
                "\t\t\t\t\t\t\tvalue=\"小型汽车号牌\" />\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t号牌底色：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<input type=\"hidden\" name=\"prpCitemCar.licenseColorCode\" id=\"prpCitemCar.licenseColorCode\"\n" +
                "\t\t\t\t\t\t\tvalue=\"01\" title=\"\" />\n" +
                "\t\t\t\t\t\t<input  name=\"LicenseColorCodeDes\" id=\"LicenseColorCodeDes\"  class=\"codecode1\"\n" +
                "\t\t\t\t\t\t\t title=\"\"\n" +
                "\t\t\t\t\t\t\tvalue=\"蓝\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t发动机号：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.engineNo\" id=\"prpCitemCar.engineNo\" maxlength=\"30\"\n" +
                "\t\t\t\t\t\tvalue=\"8****42\" title=\"\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80' />\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\tVIN码：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.vinNo\" id=\"prpCitemCar.vinNo\" maxlength=\"17\"\n" +
                "\t\t\t\t\t\tvalue=\"\" readonly=\"readonly\" title=\"\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80' />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t车架号：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.frameNo\" id=\"prpCitemCar.frameNo\" maxlength=\"17\"\n" +
                "\t\t\t\t\t\tvalue=\"LDCC13*******6503\" readonly=\"readonly\" title=\"\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80' />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t车辆种类：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<span class=\"long\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"prpCitemCar.carKindCode\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCitemCar.carKindCode\" value=\"A01\" /> \n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" name=\"CarKindCodeDes\" id=\"CarKindCodeDes\" class=\"codecode1\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"客车\"  readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t<span class=\"long\" style=\"display: none\"> \n" +
                "\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"disabled\" name=\"carKindCodeBak\" id=\"carKindCodeBak\"><option value=\"A01\" selected>客车</option><option value=\"B01\">货车</option><option value=\"B02\">半挂牵引车</option><option value=\"B11\">三轮汽车</option><option value=\"B12\">低速货车</option><option value=\"B13\">客货两用车</option><option value=\"B21\">自卸货车</option><option value=\"B91\">货车挂车</option><option value=\"C01\">油罐车</option><option value=\"C02\">气罐车</option><option value=\"C03\">液罐车</option><option value=\"C04\">冷藏车</option><option value=\"C11\">罐车挂车</option><option value=\"C20\">推土车</option><option value=\"C22\">清障车</option><option value=\"C23\">清扫车</option><option value=\"C24\">清洁车</option><option value=\"C25\">起重车</option><option value=\"C26\">装卸车</option><option value=\"C27\">升降车</option><option value=\"C28\">混凝土搅拌车</option><option value=\"C29\">挖掘车</option><option value=\"C30\">专业拖车</option><option value=\"C31\">特种车二挂车</option><option value=\"C39\">特种车二类其它</option><option value=\"C41\">电视转播车</option><option value=\"C42\">消防车</option><option value=\"C43\">医疗车</option><option value=\"C44\">油汽田操作用车</option><option value=\"C45\">压路车</option><option value=\"C46\">矿山车</option><option value=\"C47\">运钞车</option><option value=\"C48\">救护车</option><option value=\"C49\">监测车</option><option value=\"C50\">雷达车</option><option value=\"C51\">X光检查车</option><option value=\"C52\">电信抢修车/电信工程车</option><option value=\"C53\">电力抢修车/电力工程车</option><option value=\"C54\">专业净水车</option><option value=\"C55\">保温车</option><option value=\"C56\">邮电车</option><option value=\"C57\">警用特种车</option><option value=\"C58\">混凝土泵车</option><option value=\"C61\">特种车三类挂车</option><option value=\"C69\">特种车三类其它</option><option value=\"C90\">集装箱拖头</option><option value=\"D01\">摩托车</option><option value=\"D02\">正三轮摩托车</option><option value=\"D03\">侧三轮摩托车</option><option value=\"E01\">拖拉机</option><option value=\"E11\">联合收割机</option><option value=\"E12\">变形拖拉机/其它</option><option value=\"Z99\">其它车辆</option></select> \n" +
                "\t\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<!--  \n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t<span class=\"long\"> <select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.carKindCode\" title=\"\" id=\"prpCitemCar.carKindCode\"><option value=\"A01\" selected>客车</option><option value=\"B01\">货车</option><option value=\"B02\">半挂牵引车</option><option value=\"B11\">三轮汽车</option><option value=\"B12\">低速货车</option><option value=\"B13\">客货两用车</option><option value=\"B21\">自卸货车</option><option value=\"B91\">货车挂车</option><option value=\"C01\">油罐车</option><option value=\"C02\">气罐车</option><option value=\"C03\">液罐车</option><option value=\"C04\">冷藏车</option><option value=\"C11\">罐车挂车</option><option value=\"C20\">推土车</option><option value=\"C22\">清障车</option><option value=\"C23\">清扫车</option><option value=\"C24\">清洁车</option><option value=\"C25\">起重车</option><option value=\"C26\">装卸车</option><option value=\"C27\">升降车</option><option value=\"C28\">混凝土搅拌车</option><option value=\"C29\">挖掘车</option><option value=\"C30\">专业拖车</option><option value=\"C31\">特种车二挂车</option><option value=\"C39\">特种车二类其它</option><option value=\"C41\">电视转播车</option><option value=\"C42\">消防车</option><option value=\"C43\">医疗车</option><option value=\"C44\">油汽田操作用车</option><option value=\"C45\">压路车</option><option value=\"C46\">矿山车</option><option value=\"C47\">运钞车</option><option value=\"C48\">救护车</option><option value=\"C49\">监测车</option><option value=\"C50\">雷达车</option><option value=\"C51\">X光检查车</option><option value=\"C52\">电信抢修车/电信工程车</option><option value=\"C53\">电力抢修车/电力工程车</option><option value=\"C54\">专业净水车</option><option value=\"C55\">保温车</option><option value=\"C56\">邮电车</option><option value=\"C57\">警用特种车</option><option value=\"C58\">混凝土泵车</option><option value=\"C61\">特种车三类挂车</option><option value=\"C69\">特种车三类其它</option><option value=\"C90\">集装箱拖头</option><option value=\"D01\">摩托车</option><option value=\"D02\">正三轮摩托车</option><option value=\"D03\">侧三轮摩托车</option><option value=\"E01\">拖拉机</option><option value=\"E11\">联合收割机</option><option value=\"E12\">变形拖拉机/其它</option><option value=\"Z99\">其它车辆</option></select>\n" +
                "\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t-->\n" +
                "\t\t\t\t</td>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t使用性质：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.useNatureCode\" title=\"\" id=\"prpCitemCar.useNatureCode\"><option value=\"000\">不区分营业非营业</option><option value=\"111\">出租租赁</option><option value=\"112\">城市公交</option><option value=\"113\">公路客运</option><option value=\"114\">旅游客运</option><option value=\"120\">营业货车</option><option value=\"180\">运输型拖拉机</option><option value=\"190\">其它营业车辆</option><option value=\"211\" selected>家庭自用汽车</option><option value=\"212\">非营业企业客车</option><option value=\"213\">非营业机关、事业团体客车</option><option value=\"220\">非营业货车</option><option value=\"280\">兼用型拖拉机</option><option value=\"290\">其它非营业车辆</option></select>\n" +
                "\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t条款类型：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> \n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.clauseType\" title=\"\" id=\"prpCitemCar.clauseType\"><option value=\"F41\">非营业用汽车产品</option><option value=\"F42\" selected>家庭自用汽车产品</option><option value=\"F43\">营业用汽车产品</option><option value=\"F44\">摩托车产品</option><option value=\"F45\">拖拉机产品</option><option value=\"F46\">特种车产品</option></select>\n" +
                "\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t初登日期：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.enrollDate\" id=\"prpCitemCar.enrollDate\"\n" +
                "\t\t\t\t\t\tvalue=\"2010-04-28\" title=\"\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80' />\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t实际使用年数：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"><input type=\"text\" name=\"prpCitemCar.useYears\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.useYears\" value=\"5\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80' /> </span>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t平均行驶里程(公里)：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"><input type=\"text\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t id=\"prpCitemCar.runMiles\" \n" +
                "\t\t\t\t\t\tname=\"prpCitemCar.runMiles\"\n" +
                "\t\t\t\t\t\t\tvalue=\"10000.00\" title=\"\" \n" +
                "\t\t\t\t\t\t\tclass='input_w w_p80' width=\"100\" /></span>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\t车型编码/名称：\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" class='input_w w_p30' name=\"prpCitemCar.modelCode\" id=\"prpCitemCar.modelCode\"\n" +
                "\t\t\t\t\t\tvalue=\"XTAAOD0018    \" title=\"\" readonly=\"readonly\">\n" +
                "\t\t\t\t\t<span class=\"long\"> \n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.brandName\"\n" +
                "\t\t\t\t\t\t\t\t\t\tid=\"prpCitemCar.brandName\" value=\"东风雪铁龙DC7165DTA轿车\" \n" +
                "\t\t\t\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p51' readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\t\tonchange=\"ItemKind.clearSumPremium();\"> \n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t新车购置价格：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"hidden\" name=\"PurchasePriceScal\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.purchasePrice\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.purchasePrice\" value=\"96800.00\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80' /> </span>\n" +
                "\t\t\t\t\t<input type=\"hidden\" name=\"CarActualValueTrue\" id=\"\" value=\"96800.00\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t核定载质量(千克)：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.tonCount\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.tonCount\" readonly=\"readonly\" value=\"0.000\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80' /> </span>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t排量/功率(升)：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.exhaustScale\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.exhaustScale\" readonly=\"readonly\" value=\"1.5870\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80'></span>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t核定载客量(人)：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.seatCount\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.seatCount\" value=\"5\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80' /> </span>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t行驶区域：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.runAreaCode\" onmouseover=\"variableDropDownBox_ProductConfig_Ration(this);\" title=\"\" id=\"prpCitemCar.runAreaCode\"><option value=\"03\">省内行驶</option><option value=\"11\" selected>中华人民共和国境内(不含港澳台)</option><option value=\"12\">有固定行驶路线</option><option value=\"13\">场内</option></select>\n" +
                "\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t被保险人和车辆关系：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.carInsuredRelation\" title=\"\" id=\"prpCitemCar.carInsuredRelation\"><option value=\"1\">所有</option><option value=\"2\" selected>使用</option><option value=\"3\">管理</option></select>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t进口/国产类：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"prpCitemCar.countryNature\" title=\"\" id=\"prpCitemCar.countryNature\"><option value=\"01\">国产</option><option value=\"02\" selected>进口</option><option value=\"03\">合资</option></select>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t是否未还清贷款：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.loanVehicleFlag\" title=\"\" id=\"prpCitemCar.loanVehicleFlag1\" value=\"1\"><label for=\"prpCitemCar.loanVehicleFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.loanVehicleFlag\" title=\"\" id=\"prpCitemCar.loanVehicleFlag0\" value=\"0\" checked><label for=\"prpCitemCar.loanVehicleFlag0\">否</label>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t是否为过户车：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.transferVehicleFlag\" title=\"\" id=\"prpCitemCar.transferVehicleFlag1\" value=\"1\"><label for=\"prpCitemCar.transferVehicleFlag1\">是</label><input type=\"radio\" disabled=\"disabled\" name=\"prpCitemCar.transferVehicleFlag\" title=\"\" id=\"prpCitemCar.transferVehicleFlag0\" value=\"0\" checked><label for=\"prpCitemCar.transferVehicleFlag0\">否</label>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<td class=\"left4\" id=\"prpCitemCar.transferDate.label\" style=\"\">\n" +
                "\t\t\t\t\t过户日期：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\" id=\"prpCitemCar.transferDate.label2\"  style=\"\"  readonly=\"readonly\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.transferDate\" id=\"prpCitemCar.transferDate\"\n" +
                "\t\t\t\t\t\tvalue=\"\" title=\"\"   required=\"true\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\tclass='input_w w_p80' description=\"过户日期\"  />\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left3\">\n" +
                "\t\t\t\t\t整备质量(千克)：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.carLotEquQuality\" \n" +
                "\t\t\t\t\t\tid=\"prpCitemCar.carLotEquQuality\" class=\"input_w w_p80\" maxlength=\"10\"\n" +
                "\t\t\t\t\t\tvalue=\"1340.00\" readonly=\"readonly\" \n" +
                "\t\t\t\t\t\ttitle=\"\" >\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\n" +
                "\t\t\t\n" +
                "\t\t\t\n" +
                "\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t车型别名：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.modelCodeAlias\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.modelCodeAlias\" value=\"东风雪铁龙牌DC7165DTA\" title=\"\" required=\"true\"\n" +
                "\t\t\t\t\t\t\tclass='input_w w_p80' description=\"车型别名\"></span>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t<td class=\"left4\"></td>\n" +
                "\t\t\t\t\t<td class=\"right3\"></td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<!--\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t港澳车牌：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<span class=\"long\"> <input type=\"text\" name=\"prpCitemCar.hkLicenseNo\"\n" +
                "\t\t\t\t\t\t\tid=\"prpCitemCar.hkLicenseNo\" readonly=\"readonly\" value=\"\"\n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80'> </span>\n" +
                "\t\t\t\t</td>-->\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t<td class=\"left4\">机动车燃料种类：</td>\n" +
                "\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"><select class=\"w_p80\" disabled=\"disabled\" name=\"prpCitemCar.fuelType\" title=\"\" id=\"prpCitemCar.fuelType\"><option value=\"A\" selected>汽油</option><option value=\"B\">柴油</option><option value=\"C\">电</option><option value=\"D\">混合油</option><option value=\"E\">天然气</option><option value=\"F\">液化石油气</option><option value=\"L\">甲醇</option><option value=\"M\">乙醇</option><option value=\"N\">太阳能</option><option value=\"O\">混合动力</option><option value=\"Y\">无</option><option value=\"Z\">其它</option></select>\n" +
                "\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"left4\">车辆来历凭证种类：</td>\n" +
                "\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"><select class=\"w_p80\" disabled=\"disabled\" name=\"prpCitemCar.carProofType\" title=\"\" id=\"prpCitemCar.carProofType\"><option value=\"01\">销售发票</option><option value=\"02\">法院调解书</option><option value=\"03\">法院裁定书</option><option value=\"04\">法院判决书</option><option value=\"05\">仲裁裁决书</option><option value=\"06\">相关文书（继承、赠予、协议抵债）</option><option value=\"07\">批准文件</option><option value=\"08\">调拨证明</option><option value=\"09\">修理发票</option></select>\n" +
                "\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"left4\"></td><td class=\"right3\"></td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t团单申报编码\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<input type=\"text\" name=\"prpCitemCar.groupCode\" id=\"prpCitemCar.groupCode\" class='input_w w_p80' description=\"团单申报编码\" value=\"\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"left4\">购车发票日期：</td>\n" +
                "\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"><input type=\"text\" name=\"prpCitemCar.certifiCateDate\" id=\"prpCitemCar.certifiCateDate\" value=\"\" \n" +
                "\t\t\t\t\t\t\ttitle=\"\" onblur=\"ItemCar.checkEnrollDate_1(this);\" class='input_w w_p80' description=\"购车发票日期\" readonly=\"readonly\"></span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"left4\">交易方式：</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"prpCmainCommon.payMethod\" title=\"\" id=\"prpCmainCommon.payMethod\"><option value=\"01\">pos机交易</option><option value=\"02\">支票交易</option><option value=\"03\">贷记凭证交易</option><option value=\"04\">银行对私现金</option><option value=\"05\">银行对公现金</option><option value=\"06\">网上银行</option><option value=\"07\">快钱票据交易</option><option value=\"08\">无卡交易</option><option value=\"09\">快钱pos机交易</option><option value=\"\" selected></option><option value=\"null\"></option></select>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t\t<!--<tr>\n" +
                "\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t车身颜色：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t<select class=\"w_p80\" disabled=\"disabled\" name=\"prpCitemCar.colorCode\" id=\"prpCitemCar.colorCode\"><option value=\"01\">蓝</option><option value=\"02\">黑</option><option value=\"03\">白</option><option value=\"04\">黄</option><option value=\"06\">红</option><option value=\"07\">灰</option><option value=\"08\">绿</option><option value=\"09\">棕</option><option value=\"10\">粉</option><option value=\"11\">紫</option><option value=\"99\" selected>其他</option></select>\n" +
                "\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t</td>\n" +
                "                <td class=\"left4\">\n" +
                "\t\t\t\t\t 交管车辆类型：\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t    <td class=\"right3\">\n" +
                "\t\t\t\t    <input type=\"hidden\" name=\"prpCitemCar.vehicleType\" id=\"prpCitemCar.vehicleType\"\n" +
                "\t\t\t\t\t    required=\"true\" description=\"交管车辆类型\" value=\"\" title=\"\"/>\n" +
                "\t\t\t\t    <input type=\"text\" name=\"ModelCodeLicense\" id=\"ModelCodeLicense\" class=\"codecode1\"\n" +
                "\t\t\t\t       title=\"\" \n" +
                "\t\t\t\t\t\tvalue=\"\" />\n" +
                "\t\t\t\t    </td>\n" +
                "\t\t\t</tr>-->\n" +
                "\t\t\t\n" +
                "\t\t\t\t<tr>\n" +
                "\t\t\t\t\t<td class=\"left4\">车辆来历凭证编号：</td>\n" +
                "\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"><input type=\"text\" name=\"prpCitemCar.carProofNo\" id=\"prpCitemCar.carProofNo\" value=\"                                                  \" \n" +
                "\t\t\t\t\t\t\ttitle=\"\" class='input_w w_p80' description=\"车辆来历凭证编号\" readonly=\"readonly\"></span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"left4\">开具车辆来历凭证日期：</td>\n" +
                "\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t<span class=\"long\"><input type=\"text\" name=\"prpCitemCar.carProofDate\" id=\"prpCitemCar.carProofDate\" value=\"\" \n" +
                "\t\t\t\t\t\t\ttitle=\"\" onblur=\"ItemCar.checkEnrollDate_1(this);\" class='input_w w_p80' description=\"开具车辆来历凭证所载日期\" readonly=\"readonly\"></span>\n" +
                "\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t<td class=\"left4\"></td><td class=\"right3\"></td>\n" +
                "\t\t\t\t</tr>\n" +
                "\t\t\t\n" +
                "\t\t\t\n" +
                "\t\t\t</table>\n" +
                "\t\t<!-- 广东个性:增加投保方式选项-begin \n" +
                "\t\tzouyx 20110428\n" +
                "\t\t-->\n" +
                "\t\t\n" +
                "\t\t<!-- 广东个性-end -->\n" +
                "\t\t<div id=\"content\" class=\"sort\"></div>\n" +
                "\t\t<div id=\"content_navigation\" class=\"query\"></div>\n" +
                "\t</body>\n" +
                "</html>\n";
        Document doc = Jsoup.parse(html);

        Map lastResult = new HashMap<>();
        String CarUsedType=""; //使用性质
        String ForceExpireDate="";//交强险到期日期
        String BusinessExpireDate="";//商业险到期日期
        String CarVin;//车架号
        String MoldName="";//品牌型号
        String EngineNo;//发动机号
        String LicenseOwner;// 车主姓名
        String CredentislasNum;//证件号码
        String IdType;//证件类型
        String InsuredName; //被保险人
        String PostedName;//投保人
        String LicenseNo;//车牌号
        double PurchasePrice=0; //购买价格
        String CarRegisterDate="";//车辆注册日期
        int CarSeated=0;//座位数量
        int CityCode ; //城市编码
        Elements trs = doc.getElementsByClass("fix_table").select("tr");
        /*Elements trs = doc.getElementsByClass("fix_table").select("tr");
        for(int i = 1;i<trs.size();i++) {
            Elements tds = trs.get(i).select("td");
            for(int j=0;j<tds.size();j++){
                Element td = tds.get(j);
                if(td.attributes().get("id").equals("prpCitemCar.enrollDate")){
                    CarRegisterDate =td.attributes().get("value");
                    lastResult.put("CarRegisterDate",CarRegisterDate);
                }
                if(td.attributes().get("id").equals("prpCitemCar.purchasePrice")){
                    PurchasePrice = Double.parseDouble(td.attributes().get("value"));
                    lastResult.put("PurchasePrice",PurchasePrice);
                }
                if(td.attributes().get("id").equals("prpCitemCar.seatCount")){
                    CarSeated =Integer.parseInt(td.attributes().get("value"));
                    lastResult.put("CarSeated",CarSeated);
                }

            }
        }*/


        //汽车使用性质
        Elements tdsCarUsedType = trs.get(4).select("td");
        Element elementCarUsedType =  tdsCarUsedType.get(3).getElementById("prpCitemCar.useNatureCode");
        if(elementCarUsedType.tagName().equals("select")){
           Elements CarUsedTypeElements = elementCarUsedType.getAllElements();
            for(int i=0;i<CarUsedTypeElements.size();i++){
                if(CarUsedTypeElements.get(i).attributes().hasKey("selected")){
                    CarUsedType =  CarUsedTypeElements.get(9).text();
                }
            }
        }else{
            logger.info("抓取机器人，【 PICC 解析车辆信息中车辆使用用途失败】");
        }
        lastResult.put("CarUsedType",CarUsedType);
        //首次登记日期
        Elements tdsCarRegisterDate = trs.get(5).select("td");
        Element elementCarRegisterDate =  tdsCarRegisterDate.get(1).getElementById("prpCitemCar.enrollDate");
        if(elementCarRegisterDate.tagName().equals("input")){
            CarRegisterDate =elementCarRegisterDate.attributes().get("value");
        }else{
            logger.info("抓取机器人，【 PICC 解析车辆信息中初次登记日期失败】");
        }
        lastResult.put("CarRegisterDate",CarRegisterDate);
        //汽车购买价格    //车型编码
        Elements tdsPurchasePrice = trs.get(6).select("td");
        Element elementPurchasePrice =  tdsPurchasePrice.get(3).getElementById("prpCitemCar.purchasePrice");
        if(elementPurchasePrice.tagName().equals("input")){
            PurchasePrice =Double.parseDouble( elementPurchasePrice.attributes().get("value"));
        }else{
            logger.info("抓取机器人，【 PICC 解析车辆信息新车购买价格失败】");
        }
        lastResult.put("PurchasePrice",PurchasePrice);
        Element elementMoldName =  tdsPurchasePrice.get(1).getElementById("prpCitemCar.brandName");
        if(elementMoldName.tagName().equals("input")){
            MoldName = elementMoldName.attributes().get("value");
        }else{
            logger.info("抓取机器人，【 PICC 解析车辆信息品牌型号失败】");
        }
        lastResult.put("MoldName",MoldName);
        //汽车核定座位数
        Elements tdsCarSeated = trs.get(7).select("td");
        Element elementCarSeated =  tdsCarSeated.get(3).getElementById("prpCitemCar.seatCount");
        if(elementCarSeated.tagName().equals("input")){
            CarSeated =Integer.parseInt(elementCarSeated.attributes().get("value"));
        }else{
            logger.info("抓取机器人，【 PICC 解析车辆信息核定座位数失败】");
        }
        lastResult.put("CarSeated",CarSeated);

    }

    @Test
    public void testCarInsurePeople() {
        String html = "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "<html>\n" +
                "  <head>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "\t<script type=\"text/javascript\" src=\"/pages/common/cb/UIPoEnInsuredEdit.js\"></script>\n" +
                "    <table border=\"0\" id=\"tb_layer\" width=\"100%\" >\n" +
                "    \t<input type=\"hidden\" name=\"editFlag\" id = \"editFlag\" value = \"0\"/>\n" +
                "    \t<input type=\"hidden\" name=\"prpCmainCommon.ext2\" id = \"prpCmainCommon.ext2\" value = \"\"/>\n" +
                "    \t<input type=\"hidden\" name=\"configedRepeatTimesLocal\" id = \"configedRepeatTimesLocal\" value = \"\"/>\n" +
                "\t    <tr>\n" +
                "\t    \t<td>\n" +
                "\t    \t  <div id=\"InsureListArea\" class=\"selectui-container\" style=\"OVERFLOW-y:auto;overflow-x:hidden\">\n" +
                "\t    \t\t  <table width=\"100%\" border=\"1\"  class=\"common\"  id=\"insertInsuredRow\" vtitle = \"MULLINE\">\n" +
                "\t    \t\t  \t<thead>\n" +
                "\t    \t\t  \t\t<tr class=\"other\">\n" +
                "\t    \t\t  \t\t  <td width=\"11%\">角色</td>\n" +
                "\t\t\t\t\t\t  <td width=\"8%\">类型</td>\n" +
                "\t\t\t\t\t\t  <td width=\"7%\">名称</td>\n" +
                "\t\t\t\t\t\t  <td width=\"9%\">单位性质</td>\n" +
                "\t\t\t\t\t\t  <td width=\"8%\">证件类型</td>\n" +
                "\t\t\t\t\t\t  <!-- 北分电子保单个性 -->\n" +
                "\t\t\t\t\t\t  \n" +
                "\t\t\t\t\t\t\t  <td width=\"10%\">证件号码</td>\n" +
                "\t\t\t\t\t\t\t  <td width=\"10%\">地址</td>\n" +
                "\t\t\t\t\t\t\t  <td width=\"10%\">邮箱</td>\n" +
                "\t\t\t\t\t\t  \n" +
                "\t\t\t\t\t\t  \n" +
                "\t\t\t\t\t\t  <!-- beifen Then end -->\n" +
                "\t\t\t\t\t\t  <td width=\"9%\">固定电话</td>\n" +
                "\t\t\t\t\t\t  <td width=\"10%\">移动电话</td>\n" +
                "\t\t\t\t\t\t  <td width=\"7%\">操作</td>\n" +
                "\t    \t\t  \t\t</tr>\n" +
                "\t    \t\t  \t</thead>\n" +
                "\t    \t\t  \t<tbody>\n" +
                "\t    \t\t  \t\n" +
                "                        \n" +
                "                         <tr>\n" +
                "                          <td>\n" +
                "                         \t<input type=\"hidden\" name=\"prpCinsureds[0].flag\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].flag\" value=\"\" readonly />\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t </tr>\n" +
                "\t\t             \t <tr id=\"prpCinsuredsTr[0]\" ondblclick=\"Insured.showInsuredDetail(this);\">\n" +
                "\t    \t                <td>\n" +
                "\t\t                     \t  \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t           \n" +
                "\t\t          \t\t\t        \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t           \n" +
                "\t\t          \t\t           \n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t             \n" +
                "\t                        \t  <input class=\"readonly w_p80\" style=\"text-align:center\" value=\"车主\" readonly />\n" +
                "\t    \t                      <input type=\"hidden\" name=\"prpCinsureds[0].insuredFlag\" id=\"prpCinsureds[0].insuredFlag\" class=\"input_w w_p80\" value=\"001000000000000000000000000000\" title=\"001000000000000000000000000000\" readonly /></td>\n" +
                "\t\t\t\t\t\t\t <td>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t<input class=\"readonly w_p80\" style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\t\t value=\"个人客户\" readonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].insuredType\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].insuredType\"\n" +
                "\t\t\t\t\t\t\t\t\tclass=\"readonly w_p80\" value=\"1\"\n" +
                "\t\t\t\t\t\t\t\t\ttitle=\"1\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t  </td>\t\n" +
                "\t    \t                  <td><input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].insuredCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].insuredCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\treadonly=\"readonly\" />\n" +
                " \t\t\t\t\t\t\t\t<input style=\"text-align:center\" name=\"prpCinsureds[0].insuredName\" id=\"prpCinsureds[0].insuredName\" class=\"input_w w_p80\" readonly value=\"张晓燕\" title=\"张晓燕\" /></td>\n" +
                "\t    \t                  <td>\n" +
                "\t\t\t\t\t\t\t<input style=\"text-align: center\" name=\"unitTypeText\" id=\"unitTypeText\" class=\"readonly w_p80\" onchange=\"\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\ttitle=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t\t\t\t<input type=\"hidden\" style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].unitType\"\n" +
                "\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].unitType\" class=\"readonly w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t  </td>\n" +
                "\t    \t                  <td>\t\t\t\n" +
                "\t                              <input class=\"readonly w_p80\" style=\"text-align:center\" value=\"其他\" readonly />\n" +
                "\t    \t                      <input type=\"hidden\" name=\"prpCinsureds[0].identifyType\" id=\"prpCinsureds[0].identifyType\" class=\"readonly w_p80\" value=\"99\" readonly title=\"99\"/></td>\n" +
                "\t    \t                  <td><input style=\"text-align:center\" name=\"prpCinsureds[0].identifyNumber\" id=\"prpCinsureds[0].identifyNumber\" class=\"readonly w_p80\" value=\"。\" readonly title=\"。\"/></td>\n" +
                "\t    \t                  <td><input style=\"text-align:center\" name=\"prpCinsureds[0].insuredAddress\" id=\"prpCinsureds[0].insuredAddress\"  class=\"readonly w_p80\" value=\"北京\" readonly title=\"北京\"/></td>\n" +
                "\t    \t                   \n" +
                "\t    \t                 \t <td><input style=\"text-align:center\" name=\"prpCinsureds[0].email\" id=\"prpCinsureds[0].email\"  class=\"readonly w_p80\" value=\"\" readonly title=\"\"/></td>\n" +
                "\t    \t                  \n" +
                "\t    \t                  <td>\n" +
                "\t    \t                  \t<input style=\"text-align:center\" name=\"prpCinsureds[0].phoneNumber\" id=\"prpCinsureds[0].phoneNumber\" class=\"readonly w_p80\" value=\"13691***762\" readonly title=\"13691***762\"/>\n" +
                "\t    \t                  \t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].sex\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].sex\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t   <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].drivingYears\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].drivingYears\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"0\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].postCode\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].postCode\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"100000\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" \n" +
                "\t\t\t\t\t\t\t\t    name=\"prpCinsureds[0].versionNo\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].versionNo\" \n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"0\" \n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" \n" +
                "\t\t\t\t\t\t\t\t    name=\"prpCinsureds[0].auditStatus\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].auditStatus\" \n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" \n" +
                "\t\t\t\t\t\t\t\t\treadonly />\t\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].countryCode\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].countryCode\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].flag\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].flag\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].age\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].age\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"0\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].drivingLicenseNo\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].drivingLicenseNo\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<!-- add by lujunfeng 20110811 浏览上年违章次数和初次领证日期  -->\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].causetroubleTimes\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].causetroubleTimes\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"0\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].acceptLicenseDate\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].acceptLicenseDate\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"prpCinsureds[0].appendPrintName\"\n" +
                "\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].appendPrintName\" class=\"input_r\"  value=\"\" readonly=\"readonly\" />\t\t\n" +
                "\t\t\t\t\t\t\t\t  <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].drivingCarType\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].drivingCarType\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t    value=\"\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"isCheckRepeat[0]\" id=\"isCheckRepeat[0]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"configedRepeatTimes[0]\" id=\"configedRepeatTimes[0]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"repeatTimes[0]\" id=\"repeatTimes[0]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].unifiedSocialCreditCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].unifiedSocialCreditCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tvalue=\"\" title=\"\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\treadonly=\"readonly\" />\n" +
                "\t    \t                  </td>\n" +
                "\t    \t                  <td>\n" +
                "\t\t\t\t\t\t\t\t<input style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[0].mobile\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[0].mobile\"\n" +
                "\t\t\t\t\t\t\t\t\tclass=\"readonly w_p80\" value=\"136****7762\"\n" +
                "\t\t\t\t\t\t\t\t\ttitle=\"136****7762\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\n" +
                "\t\t\t\t\t\t\t  </td>\n" +
                "\t\t\t    \t\t\t  <td>\n" +
                "\t\t\t    \t\t\t      <input type='button' name=\"btn_showInsured\"  id=\"btn_showInsured\" class='button_ty' value='删除' \n" +
                "\t\t\t    \t\t\t      \ttitle=\"0\" onClick=\"Insured.showInsured(this);\" disabled=\"disabled\"/>\n" +
                "\t\t\t    \t\t\t  </td>\t\t \n" +
                "\t\t\t    \t\t\t  </tr> \n" +
                "\t\t\t    \t\t\t  \n" +
                "                               \n" +
                "                         <tr>\n" +
                "                          <td>\n" +
                "                         \t<input type=\"hidden\" name=\"prpCinsureds[1].flag\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].flag\" value=\"\" readonly />\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t </tr>\n" +
                "\t\t             \t <tr id=\"prpCinsuredsTr[1]\" ondblclick=\"Insured.showInsuredDetail(this);\">\n" +
                "\t    \t                <td>\n" +
                "\t\t                     \t  \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t\t        \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t\t        \n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t\t           \n" +
                "\t\t          \t\t           \n" +
                "\t\t          \t\t           \n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t          \t\t          \n" +
                "\t\t          \t             \n" +
                "\t                        \t  <input class=\"readonly w_p80\" style=\"text-align:center\" value=\"投保人/被保险人\" readonly />\n" +
                "\t    \t                      <input type=\"hidden\" name=\"prpCinsureds[1].insuredFlag\" id=\"prpCinsureds[1].insuredFlag\" class=\"input_w w_p80\" value=\"11000000000000000000000000000A\" title=\"11000000000000000000000000000A\" readonly /></td>\n" +
                "\t\t\t\t\t\t\t <td>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t<input class=\"readonly w_p80\" style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\t\t value=\"个人客户\" readonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].insuredType\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].insuredType\"\n" +
                "\t\t\t\t\t\t\t\t\tclass=\"readonly w_p80\" value=\"1\"\n" +
                "\t\t\t\t\t\t\t\t\ttitle=\"1\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t  </td>\t\n" +
                "\t    \t                  <td><input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].insuredCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].insuredCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tvalue=\"1100100003270975\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\treadonly=\"readonly\" />\n" +
                " \t\t\t\t\t\t\t\t<input style=\"text-align:center\" name=\"prpCinsureds[1].insuredName\" id=\"prpCinsureds[1].insuredName\" class=\"input_w w_p80\" readonly value=\"李永翔\" title=\"李永翔\" /></td>\n" +
                "\t    \t                  <td>\n" +
                "\t\t\t\t\t\t\t<input style=\"text-align: center\" name=\"unitTypeText\" id=\"unitTypeText\" class=\"readonly w_p80\" onchange=\"\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\ttitle=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t\t\t\t<input type=\"hidden\" style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].unitType\"\n" +
                "\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].unitType\" class=\"readonly w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t  </td>\n" +
                "\t    \t                  <td>\t\t\t\n" +
                "\t                              <input class=\"readonly w_p80\" style=\"text-align:center\" value=\"身份证\" readonly />\n" +
                "\t    \t                      <input type=\"hidden\" name=\"prpCinsureds[1].identifyType\" id=\"prpCinsureds[1].identifyType\" class=\"readonly w_p80\" value=\"01\" readonly title=\"01\"/></td>\n" +
                "\t    \t                  <td><input style=\"text-align:center\" name=\"prpCinsureds[1].identifyNumber\" id=\"prpCinsureds[1].identifyNumber\" class=\"readonly w_p80\" value=\"150202197705280611\" readonly title=\"150202197705280611\"/></td>\n" +
                "\t    \t                  <td><input style=\"text-align:center\" name=\"prpCinsureds[1].insuredAddress\" id=\"prpCinsureds[1].insuredAddress\"  class=\"readonly w_p80\" value=\"北京\" readonly title=\"北京\"/></td>\n" +
                "\t    \t                   \n" +
                "\t    \t                 \t <td><input style=\"text-align:center\" name=\"prpCinsureds[1].email\" id=\"prpCinsureds[1].email\"  class=\"readonly w_p80\" value=\"\" readonly title=\"\"/></td>\n" +
                "\t    \t                  \n" +
                "\t    \t                  <td>\n" +
                "\t    \t                  \t<input style=\"text-align:center\" name=\"prpCinsureds[1].phoneNumber\" id=\"prpCinsureds[1].phoneNumber\" class=\"readonly w_p80\" value=\"\" readonly title=\"\"/>\n" +
                "\t    \t                  \t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].sex\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].sex\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"1\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t   <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].drivingYears\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].drivingYears\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].postCode\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].postCode\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" \n" +
                "\t\t\t\t\t\t\t\t    name=\"prpCinsureds[1].versionNo\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].versionNo\" \n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"4\" \n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" \n" +
                "\t\t\t\t\t\t\t\t    name=\"prpCinsureds[1].auditStatus\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].auditStatus\" \n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"2\" \n" +
                "\t\t\t\t\t\t\t\t\treadonly />\t\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].countryCode\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].countryCode\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"CHN\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].flag\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].flag\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].age\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].age\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"39\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].drivingLicenseNo\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].drivingLicenseNo\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"150202197705280611\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<!-- add by lujunfeng 20110811 浏览上年违章次数和初次领证日期  -->\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].causetroubleTimes\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].causetroubleTimes\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"0\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].acceptLicenseDate\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].acceptLicenseDate\"\n" +
                "\t\t\t\t\t\t\t\t    value=\"\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"prpCinsureds[1].appendPrintName\"\n" +
                "\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].appendPrintName\" class=\"input_r\"  value=\"\" readonly=\"readonly\" />\t\t\n" +
                "\t\t\t\t\t\t\t\t  <input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].drivingCarType\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].drivingCarType\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t    value=\"\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"isCheckRepeat[1]\" id=\"isCheckRepeat[1]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"configedRepeatTimes[1]\" id=\"configedRepeatTimes[1]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"repeatTimes[1]\" id=\"repeatTimes[1]\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].unifiedSocialCreditCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].unifiedSocialCreditCode\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tvalue=\"\" title=\"\"\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\treadonly=\"readonly\" />\n" +
                "\t    \t                  </td>\n" +
                "\t    \t                  <td>\n" +
                "\t\t\t\t\t\t\t\t<input style=\"text-align: center\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"prpCinsureds[1].mobile\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"prpCinsureds[1].mobile\"\n" +
                "\t\t\t\t\t\t\t\t\tclass=\"readonly w_p80\" value=\"135****1177\"\n" +
                "\t\t\t\t\t\t\t\t\ttitle=\"135****1177\"\n" +
                "\t\t\t\t\t\t\t\t\treadonly />\n" +
                "\n" +
                "\t\t\t\t\t\t\t  </td>\n" +
                "\t\t\t    \t\t\t  <td>\n" +
                "\t\t\t    \t\t\t      <input type='button' name=\"btn_showInsured\"  id=\"btn_showInsured\" class='button_ty' value='删除' \n" +
                "\t\t\t    \t\t\t      \ttitle=\"1\" onClick=\"Insured.showInsured(this);\" disabled=\"disabled\"/>\n" +
                "\t\t\t    \t\t\t  </td>\t\t \n" +
                "\t\t\t    \t\t\t  </tr> \n" +
                "\t\t\t    \t\t\t  \n" +
                "                               \n" +
                "                                  <input type='hidden' name='hidden_index_insured' id='hidden_index_insured' value=\"2\" />\t    \t\t  \t\n" +
                "\t    \t\t  \t</tbody>\n" +
                "\t    \t\t  </table>\n" +
                "\t    \t  </div>\n" +
                "\t      </td>\n" +
                "\t    </tr>\n" +
                "\t    <tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<table width=\"100%\" border=\"0\" class=\"fix_table\" id=\"testtable\" style=\"\">\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t<span>角色：</span>\n" +
                "\t\t\t\t\t\t\t\t<input id=\"loadFlag1\" type=\"hidden\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td colspan=\"5\" class=\"right4\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t投保人\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"投保人\" />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t被保险人\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"被保险人\" />\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t\t车主\n" +
                "\t\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"车主\" />\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t\t指定驾驶人\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"指定驾驶人\" />\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t受益人\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"受益人\" />\n" +
                "\t\t\t\t\t\t\t\t\t<!--modify by cj zhaoxiaojie 20100823  规则：提车险和定额单不显示车主、指定驾驶人、港澳车车主 start -->\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t\t港澳车车主\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"港澳车车主\" />\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t<!--modify by cj zhaoxiaojie 20100823  规则：提车险和定额单不显示车主、指定驾驶人、港澳车车主 end -->\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t<input type=\"checkbox\" name=\"_insuredFlag\" id=\"_insuredFlag\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" disabled>\n" +
                "\t\t\t\t\t\t\t\t联系人\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_insuredFlag_hide\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_insuredFlag_hide\" value=\"联系人\" />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" id=\"_resident\" name=\"_resident\" description=\"居民与非居民\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t类型：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"_insuredType\" onchange=\"Insured.changeInsuredType();\" id=\"_insuredType\"><option value=\"1\" selected>个人</option><option value=\"2\">团体</option></select>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_insuredCode_td\">\n" +
                "\t\t\t\t\t\t\t\t客户代码：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_insuredCode_td\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" name=\"_insuredCode\" id=\"_insuredCode\" required=\"true\" description=\"代码\" class=\"input_w w_p80\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t名称：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"td_inp_insuredName\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" name=\"_insuredName\" id=\"_insuredName\" class=\"input_w w_p80\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" required=\"true\" description=\"名称\" onchange=\"Insured.checkInsuredName();\"/>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"customerURL\" id=\"customerURL\" value=\"http://10.134.136.48:8300/cif\" />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_isCheckRepeat\" id=\"_isCheckRepeat\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_configedRepeatTimes\" id=\"_configedRepeatTimes\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"_repeatTimes\" id=\"_repeatTimes\" value=\"\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_identifyType\">\n" +
                "\t\t\t\t\t\t\t\t证件类型：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_identifyType\">\n" +
                "\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"_identifyType\" onchange=\"Insured.clearIdentifyNumber();\" id=\"_identifyType\"><option value=\"01\">身份证</option><option value=\"02\">户口薄</option><option value=\"03\">护照</option><option value=\"04\">军人证件</option><option value=\"05\">驾驶执照</option><option value=\"06\">返乡证</option><option value=\"07\">港澳身份证</option><option value=\"08\">工号</option><option value=\"09\">赴台通行证</option><option value=\"10\">港澳通行证</option><option value=\"15\">士兵证</option><option value=\"25\">港澳居民来往内地通行证</option><option value=\"26\">台湾居民来往内地通行证</option><option value=\"31\">组织机构代码证</option><option value=\"99\">其他</option></select>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t    <td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t<span id=\"des_identifyNumber\">证件号码：</span>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_identifyNumber\" maxlength=\"20\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_identifyNumber\" value=\"\" description=\"证件号码\" required=\"true\"\n" +
                "\t\t\t\t\t\t\t\t\tonblur=\"Insured.checkIdentifyNumberID(this);\" onchange=\"Insured.changeIdentifyNumber();\"/>\n" +
                "\t\t\t\t\t\t\t\t<img id=\"mustInput\" name=\"mustInput\" src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_unifiedSocialCreditCode\" style= \"display:none\">\n" +
                "\t\t\t\t\t\t\t\t<span>统一社会信用代码：</span>\n" +
                "\t\t\t\t\t\t\t</td >\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_unifiedSocialCreditCode\" style= \"display:none\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_unifiedSocialCreditCode\" maxlength=\"20\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_unifiedSocialCreditCode\" value=\"\" description=\"统一社会信用代码\" required=\"true\"\n" +
                "\t\t\t\t\t\t\t\t\tonchange=\"\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t移动电话：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=right3>\n" +
                "\t\t\t\t\t\t\t\t<input class=\"input_w\" type=\"text\" name=\"_mobile\" style=\"width: 40%\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_mobile\" value=\"\" maxlength=\"11\"/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"button\" value=\"查\" name=\"save2\" id=\"save2\" class=\"button\"\n" +
                "\t\t\t\t\t\t\t\t\tdisabled=\"disabled\"\n" +
                "\t\t\t\t\t\t\t\t/><input type=\"button\" value=\"改\" name=\"editCustom\" id=\"editCustom\" class=\"button\"\n" +
                "\t\t\t\t\t\t\t\t\tdisabled=\"disabled\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class= \"left4\" id = \"decorationId1\" style= \"display:none\"></td>\n" +
                "\t\t\t\t\t\t\t<td class= \"right3\" id = \"decorationValue1\" style= \"display:none\"></td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_sex\">\n" +
                "\t\t\t\t\t\t\t     性别：\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_sex\">\n" +
                "\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"_sex\" id=\"_sex\"><option value=\"0\">未知的性别</option><option value=\"1\">男性</option><option value=\"2\">女性</option><option value=\"9\">未说明的性别</option></select>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_age\">\n" +
                "\t\t\t\t\t\t\t    年龄：\t\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_age\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_age\" maxlength=\"3\" id=\"_age\" value=\"\" \n" +
                "\t\t\t\t\t\t\t\t\treadonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t\t<img id=\"imageone\" src=\"/prpall/pages/image/imgMustInput.gif\" style=\"display:none\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"des_drivingYears\">\n" +
                "\t\t\t\t\t\t\t    驾龄：\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"inp_drivingYears\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_drivingYears\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_drivingYears\" value=\"\" \n" +
                "\t\t\t\t\t\t\t\t\tonblur=\"Insured.showDrivingYears(this);\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t\t<img id=\"imagetwo\" src=\"/prpall/pages/image/imgMustInput.gif\" style=\"display:none\"/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t<span id=\"des_countryCode\">\n" +
                "\t\t\t\t\t\t\t\t\t国籍：\n" +
                "\t\t\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\t\t\t<span id=\"des_unitType\" style=\"display:none\">单位性质：</span>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<span id=\"inp_countryCode\">\n" +
                "\t\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"_countryCode\" onmouseover=\"variableDropDownBox_ProductConfig_Ration(this);\" id=\"_countryCode\"><option value=\"ABW\">阿鲁巴</option><option value=\"AFG\">阿富汗</option><option value=\"AGO\">安哥拉</option><option value=\"AIA\">安圭拉</option><option value=\"ALB\">阿尔巴尼亚</option><option value=\"AND\">安道尔</option><option value=\"ANT\">荷属安的列斯</option><option value=\"ARE\">阿联酋</option><option value=\"ARG\">阿根廷</option><option value=\"ARM\">亚美尼亚</option><option value=\"ASM\">美属萨摩亚</option><option value=\"ATA\">南极洲</option><option value=\"ATF\">法属南部领土</option><option value=\"ATG\">安提瓜和巴布达</option><option value=\"AUS\">澳大利亚</option><option value=\"AUT\">奥地利</option><option value=\"AZE\">阿塞拜疆</option><option value=\"BDI\">布隆迪</option><option value=\"BEL\">比利时</option><option value=\"BEN\">贝宁</option><option value=\"BFA\">布基纳法索</option><option value=\"BGD\">孟加拉国</option><option value=\"BGR\">保加利亚</option><option value=\"BHR\">巴林</option><option value=\"BHS\">巴哈马</option><option value=\"BIH\">波黑</option><option value=\"BLR\">白俄罗斯</option><option value=\"BLZ\">伯利兹</option><option value=\"BMU\">百慕大</option><option value=\"BOL\">玻利维亚</option><option value=\"BRA\">巴西</option><option value=\"BRB\">巴巴多斯</option><option value=\"BRN\">文莱</option><option value=\"BTN\">不丹</option><option value=\"BVT\">布维岛</option><option value=\"BWA\">博茨瓦纳</option><option value=\"CAF\">中非</option><option value=\"CAN\">加拿大</option><option value=\"CCK\">科科斯（基林）群岛</option><option value=\"CHE\">瑞士</option><option value=\"CHL\">智利</option><option value=\"CHN\" selected>中国</option><option value=\"CIV\">科特迪瓦</option><option value=\"CMR\">喀麦隆</option><option value=\"COG\">刚果</option><option value=\"COK\">库克群岛</option><option value=\"COL\">哥伦比亚</option><option value=\"COM\">科摩罗</option><option value=\"CPV\">佛得角</option><option value=\"CR\">哥斯达黎加</option><option value=\"CUB\">古巴</option><option value=\"CYM\">开曼群岛</option><option value=\"CYP\">塞浦路斯</option><option value=\"CZE\">捷克</option><option value=\"DEU\">德国</option><option value=\"DJI\">吉布提</option><option value=\"DMA\">多米尼克</option><option value=\"DNK\">丹麦</option><option value=\"DOM\">多米尼加共和国</option><option value=\"DZA\">阿尔及利亚</option><option value=\"ECU\">厄瓜多尔</option><option value=\"EGY\">埃及</option><option value=\"ERI\">厄立特里亚</option><option value=\"ESH\">西撒哈拉</option><option value=\"ESP\">西班牙</option><option value=\"EST\">爱沙尼亚</option><option value=\"ETH\">埃塞俄比亚</option><option value=\"FIN\">芬兰</option><option value=\"FJI\">斐济</option><option value=\"FLK\">福克兰群岛(马尔维纳斯)</option><option value=\"FRA\">法国</option><option value=\"FRO\">法罗群岛</option><option value=\"FSM\">密克罗尼西亚联邦</option><option value=\"GAB\">加蓬</option><option value=\"GBR\">英国</option><option value=\"GEO\">格鲁吉亚</option><option value=\"GHA\">加纳</option><option value=\"GIB\">直布罗陀</option><option value=\"GIN\">几内亚</option><option value=\"GLP\">瓜德罗普</option><option value=\"GMB\">冈比亚</option><option value=\"GNB\">几内亚比绍</option><option value=\"GNQ\">赤道几内亚</option><option value=\"GRC\">希腊</option><option value=\"GRD\">格林纳达</option><option value=\"GRL\">格陵兰</option><option value=\"GTM\">危地马拉</option><option value=\"GUF\">法属圭亚那</option><option value=\"GUM\">关岛</option><option value=\"GUY\">圭亚那</option><option value=\"HKG\">香港</option><option value=\"HMD\">赫德岛和麦克唐纳岛</option><option value=\"HND\">洪都拉斯</option><option value=\"HRV\">克罗地亚</option><option value=\"HTI\">海地</option><option value=\"HUN\">匈牙利</option><option value=\"IDN\">印度尼西亚</option><option value=\"IND\">印度</option><option value=\"IOT\">英属印度洋领土</option><option value=\"IRL\">爱尔兰</option><option value=\"IRN\">伊朗</option><option value=\"IRQ\">伊拉克</option><option value=\"ISL\">冰岛</option><option value=\"ISR\">以色列</option><option value=\"ITA\">意大利</option><option value=\"JAM\">牙买加</option><option value=\"JOR\">约旦</option><option value=\"JPN\">日本</option><option value=\"KAZ\">哈萨克斯坦</option><option value=\"KEN\">肯尼亚</option><option value=\"KGZ\">吉尔吉斯斯坦</option><option value=\"KHM\">柬埔寨</option><option value=\"KIR\">基里巴斯</option><option value=\"KNA\">圣基茨和尼维斯</option><option value=\"KOR\">韩国</option><option value=\"KWT\">科威特</option><option value=\"LAO\">老挝</option><option value=\"LBN\">黎巴嫩</option><option value=\"LBR\">利比里亚</option><option value=\"LBY\">利比亚</option><option value=\"LCA\">圣卢西亚</option><option value=\"LIE\">列支敦士登</option><option value=\"LKA\">斯里兰卡</option><option value=\"LSO\">莱索托</option><option value=\"LTU\">立陶宛</option><option value=\"LUX\">卢森堡</option><option value=\"LVA\">拉脱维亚</option><option value=\"MAC\">澳门</option><option value=\"MAR\">摩洛哥</option><option value=\"MCO\">摩纳哥</option><option value=\"MDA\">摩尔多瓦</option><option value=\"MDG\">马达加斯加</option><option value=\"MDV\">马尔代夫</option><option value=\"MEX\">墨西哥</option><option value=\"MHL\">马绍尔群岛</option><option value=\"MKD\">前南马其顿</option><option value=\"MLI\">马里</option><option value=\"MLT\">马耳他</option><option value=\"MMR\">缅甸</option><option value=\"MNG\">蒙古</option><option value=\"MNP\">北马里亚纳</option><option value=\"MOZ\">莫桑比克</option><option value=\"MRT\">毛里塔尼亚</option><option value=\"MSR\">蒙特塞拉特</option><option value=\"MTQ\">马提尼克</option><option value=\"MUS\">毛里求斯</option><option value=\"MWI\">马拉维</option><option value=\"MYS\">马来西亚</option><option value=\"MYT\">马约特</option><option value=\"NAM\">纳米比亚</option><option value=\"NCL\">新喀里多尼亚</option><option value=\"NER\">尼日尔</option><option value=\"NFK\">诺福克岛</option><option value=\"NGA\">尼日利亚</option><option value=\"NIC\">尼加拉瓜</option><option value=\"NIU\">纽埃</option><option value=\"NLD\">荷兰</option><option value=\"NOR\">挪威</option><option value=\"NPL\">尼泊尔</option><option value=\"NRU\">瑙鲁</option><option value=\"NZL\">新西兰</option><option value=\"OMN\">阿曼</option><option value=\"PAK\">巴基斯坦</option><option value=\"PAN\">巴拿马</option><option value=\"PCN\">皮特凯恩群岛</option><option value=\"PER\">秘鲁</option><option value=\"PHL\">菲律宾</option><option value=\"PLW\">帕劳</option><option value=\"PNG\">巴布亚新几内亚</option><option value=\"POL\">波兰</option><option value=\"PRI\">波多黎各</option><option value=\"PRK\">朝鲜</option><option value=\"PRT\">葡萄牙</option><option value=\"PRY\">巴拉圭</option><option value=\"PST\">巴勒斯坦</option><option value=\"PYF\">法属波利尼西亚</option><option value=\"QAT\">卡塔尔</option><option value=\"REU\">留尼汪</option><option value=\"ROM\">罗马尼亚</option><option value=\"RUS\">俄罗斯联邦</option><option value=\"RWA\">卢旺达</option><option value=\"SAU\">沙特阿拉伯</option><option value=\"SDN\">苏丹</option><option value=\"SEN\">塞内加尔</option><option value=\"SGP\">新加坡</option><option value=\"SGS\">南乔治亚岛和南桑德韦奇岛</option><option value=\"SHN\">圣赫勒拿</option><option value=\"SJM\">斯瓦尔巴群岛</option><option value=\"SLB\">所罗门群岛</option><option value=\"SLE\">塞拉利昂</option><option value=\"SLV\">萨尔瓦多</option><option value=\"SMR\">圣马力诺</option><option value=\"SOM\">索马里</option><option value=\"SPM\">圣皮埃尔和密克隆</option><option value=\"STP\">圣多美和普林西比</option><option value=\"SUR\">苏里南</option><option value=\"SVK\">斯洛伐克</option><option value=\"SVN\">斯洛文尼亚</option><option value=\"SWE\">瑞典</option><option value=\"SWZ\">斯威士兰</option><option value=\"SYC\">塞舌尔</option><option value=\"SYR\">叙利亚</option><option value=\"TCA\">特克斯科斯群岛</option><option value=\"TCD\">乍得</option><option value=\"TGO\">多哥</option><option value=\"THA\">泰国</option><option value=\"TJK\">塔吉克斯坦</option><option value=\"TKL\">托克劳</option><option value=\"TKM\">土库曼斯坦</option><option value=\"TMP\">东帝汶</option><option value=\"TON\">汤加</option><option value=\"TTO\">特立尼达和多巴哥</option><option value=\"TUN\">突尼斯</option><option value=\"TUR\">土耳其</option><option value=\"TUV\">图瓦卢</option><option value=\"TZA\">坦桑尼亚</option><option value=\"UGA\">乌干达</option><option value=\"UKR\">乌克兰</option><option value=\"UMI\">美属太平洋各群岛（包括：中途岛、约翰斯顿岛、豪兰岛、贝克岛和威克岛?</option><option value=\"URY\">乌拉圭</option><option value=\"USA\">美国</option><option value=\"UZB\">乌兹别克斯坦</option><option value=\"VAT\">梵蒂冈</option><option value=\"VCT\">圣文森特和格林纳丁斯</option><option value=\"VEN\">委内瑞拉</option><option value=\"VGB\">英属维尔京群岛</option><option value=\"VIR\">美属维尔京群岛</option><option value=\"VNM\">越南</option><option value=\"VUT\">瓦努阿图</option><option value=\"WLF\">瓦利斯和富图纳</option><option value=\"WSM\">萨摩亚</option><option value=\"YEM\">也门</option><option value=\"YUG\">南斯拉夫</option><option value=\"ZAF\">南非</option><option value=\"ZMB\">赞比亚</option><option value=\"ZWE\">津巴布韦</option></select> \n" +
                "\t\t\t\t\t\t\t\t</span>\n" +
                "\t\t\t\t\t\t\t\t<span id=\"inp_unitType\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t\t\t<select class=\"w_p80\" disabled=\"true\" name=\"_unitType\" onmouseover=\"variableDropDownBox_ProductConfig_Ration(this);\" id=\"_unitType\"><option value=\"100\">机关、团体</option><option value=\"110\">国家机关</option><option value=\"118\">军队(武警)</option><option value=\"119\">使领馆</option><option value=\"120\">党政机关</option><option value=\"130\">社会团体</option><option value=\"140\">基层群众自治组织</option><option value=\"200\">事业单位</option><option value=\"210\">卫生事业</option><option value=\"220\">体育事业</option><option value=\"230\">社会事业</option><option value=\"240\">教育事业</option><option value=\"250\">文化艺术业</option><option value=\"260\">广播电影电视业</option><option value=\"270\">科学研究业</option><option value=\"280\">综合技术服务业</option><option value=\"300\">企业单位</option><option value=\"310\">国有</option><option value=\"320\">集体</option><option value=\"330\">个体</option><option value=\"340\">私营</option><option value=\"350\">三资</option><option value=\"351\">中外合资</option><option value=\"352\">中外合作</option><option value=\"353\">外商独资</option><option value=\"360\">混合所有制</option><option value=\"900\">其他</option></select>\n" +
                "\t\t\t\t\t\t\t  </span>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t地址：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" id=\"_insuredAddress\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tname=\"_insuredAddress\" value=\"\"  maxlength=\"255\"/>\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t邮编：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=right3>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_postCode\" maxlength=\"6\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tid=\"_postCode\" value=\"\" />\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t\t固定电话：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" id=\"_phoneNumber\" value=\"\"  \n" +
                "\t\t\t\t\t\t\t\t\t   maxlength=\"12\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t</td>\t\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t    <span id=\"printAdd\">打印附加：\t</span>\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_appendPrintName\" id=\"_appendPrintName\"  readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tvalue=\"\" description=\"打印附加\" />\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t    <span id=\"printAdd\">集团代码：\t</span>\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"group_code\" id=\"group_code\"  readonly=\"readonly\" description=\"集团代码\" />\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t    审批状态：\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=right3>\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"hidden\" class=\"input_w w_p80\" name=\"_auditStatus\" maxlength=\"3\" id=\"_auditStatus\" />\n" +
                "\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_auditStatusDes\" maxlength=\"3\" id=\"_auditStatusDes\" readonly=\"readonly\" />\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t    <td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t    <span>版本号：\t</span>\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=right3>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_versionNo\" id=\"_versionNo\" required=\"true\" description=\"版本号\" value=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\">\n" +
                "\t\t\t\t\t\t\t    <span>电子邮箱：\t</span>\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=right3>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_email\" id=\"_email\" required=\"true\" description=\"电子邮箱\" value=\"\" readonly=\"readonly\" />\n" +
                "\t\t\t\t\t\t\t\t<img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id =\"decorationId\"  style =\"\"> </td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id =\"decorationValue\" style =\"\"> </td>\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"drivingLicenseNo\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t\t\t驾驶证号码：\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t\t<td class=\"right3\" id=\"drivingLicenseNoValue\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t\t<input type=\"text\" class=\"input_w w_p80\" name=\"_drivingLicenseNo\" id=\"_drivingLicenseNo\"  description=\"驾驶证号\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t\t\tmaxlength=\"20\" onblur=\"Insured.blurDrivingLicenseNo(this);\"/><img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<!-- Add by lujunfeng 20110811 添加初次上年违章次数和初次领证日期 begin -->\n" +
                "\t\t\t\t\t\t<tr id=\"disType\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t<td class=\"left4\" id=\"drivingCarType\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t    <span id=\"des_age\">准驾车型：\t</span>\t\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t            <td class=\"right3\" id=\"drivingCarTypeValue\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t      <input type=\"hidden\" name=\"_drivingCarType\" id=\"_drivingCarType\"\n" +
                "\t\t\t\t\t\t\t     required=\"true\" description=\"准驾车型\"/>\n" +
                "\t\t\t\t\t\t      <input type=\"text\" name=\"CarKindLicense\" id=\"CarKindLicense\" class=\"codecode1\" readonly=\"readonly\"\n" +
                "\t\t\t\t\t\t\t     ondblclick=\"code_CodeSelect(this,'CarKindLicense','-1,0','Y','riskCode=DAA');\"\n" +
                "\t\t\t\t\t\t\t     onchange=\"code_CodeChange(this,'CarKindLicense','-1,0','Y','riskCode=DAA');\"\n" +
                "\t\t\t\t\t\t\t     value=\"\" />\n" +
                "\t\t\t\t\t\t\t  <img src=\"/prpall/pages/image/imgMustInput.gif\" style=\"\"/>\n" +
                "\t\t\t\t            </td>\t\t\n" +
                "\t\t\t\t\t\t  <td class=\"left4\" id=\"causetroubleTimesType\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t\t\t上年违章次数：\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t  <td class=\"right3\" id=\"causetroubleTimesValue\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t <input type=\"text\" class=\"input_w w_p80\" name=\"_causetroubleTimes\" id=\"_causetroubleTimes\"  description=\"上年违章次数\"\n" +
                "\t\t\t\t\t\t\t\t    onblur=\"checkIsInteger(this);\" readonly=\"readonly\" maxlength=\"15\"/>\n" +
                "\t\t\t\t\t\t\t\t    <img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t  <td class=\"left4\" id=\"acceptLicenseDateType\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t\t\t初次领证日期：\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t  <td class=\"right3\" id=\"acceptLicenseDateValue\" style=\"display:none\">\n" +
                "\t\t\t\t\t\t\t <input type=\"text\" class=\"input_w w_p80\" name=\"_acceptLicenseDate\" id=\"_acceptLicenseDate\"  description=\"初次领证日期\" readonly=\"readonly\"/>\n" +
                "\t\t\t\t\t\t\t <img src=\"/prpall/pages/image/imgMustInput.gif\">\n" +
                "\t\t\t\t\t\t  </td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<!-- Add by LiChenhui 页面风格调整 20100722 begin -->\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td colspan=6 class=\"center1\">\n" +
                "                                <input type=\"hidden\" name=\"prpCmainCar.agreeDriverFlag\" id=\"prpCmainCar.agreeDriverFlag\" value=\"\" >\n" +
                "\t\t\t\t\t\t\t\t<input type=\"button\" value=\"确定\" name=\"insured_btn_Save\" class=\"button_ty\"\n" +
                "\t\t\t\t\t\t\t\t\tonClick=\"Insured.insured_Add('insertInsuredRow',this,'hidden_index_insured');\" disabled/>\n" +
                "\t\t\t\t\t\t\t\t<input type=\"hidden\" name=\"updateIndex\" id=\"updateIndex\" value=\"-1\"/>\n" +
                "\t\t\t\t\t\t\t\t<!-- modify by cj 20100805 暂时先注掉这两个按钮 start\n" +
                "\t\t\t\t\t\t\t\t<input type=\"button\" class=\"button_ty\" name=\"\" id=\"\" value=\"上传修改影像\" />\n" +
                "\t\t\t\t\t\t\t\t<input type=\"button\" class=\"button_ty\" name=\"\" id=\"\" value=\"查询影像\" />\n" +
                "\t\t\t\t\t\t\t\t-->\n" +
                "\t\t\t\t\t\t\t\t<input type=\"button\" value=\"重置\" name=\"insured_btn_Clear\"\n" +
                "\t\t\t\t\t\t\t\t\tclass=\"button_ty\" onClick=\"Insured.clearInsuredInputArea();\" disabled/>\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<!--  add xuzechao 20110811 客户评级 -->\n" +
                "\t\t\t\t\t\t<tr>\n" +
                "\t\t\t\t\t\t\t<td colspan=\"6\" width=\"100%\" align=\"right\"><input type=\"button\" value=\"客户评级\" name=\"showLevel\" class=\"button\"\n" +
                "\t\t\t\t\t\t\t\t\tonClick=\"Insured.queryCustomerLevel();\" />\n" +
                "\t\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\t<tr><td colspan=\"6\" id = \"modelList\"></td></tr>\n" +
                "\t\t\t\t\t</table>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";

        Map returnMap = new HashMap<>();
        Map lastResult = new HashMap<>();

        html = html.replaceAll("\r|\n|\t", "");
        Document doc = Jsoup.parse(html);

          /*  String LicenseOwner;// 车主姓名
            String CredentislasNum;//证件号码
            String IdType;//证件类型
            String InsuredName; //被保险人
            String PostedName;//投保人*/

        String role="";//角色
        String type="";//类型
        String name="";//名称
        String companyType="";//公司性质
        String IdCardNo="";//证件号码
        String IdCardType="";//证件类型
        String address="";//地址
        String email="";//邮箱
        String telNum="";//固定电话
        String mobilePhone="";//移动电话

        Elements trs = doc.getElementById("insertInsuredRow").select("tr");
        for(int i = 0;i<trs.size();i++) {
            if(trs.get(i).attributes().hasKey("id")){
                Elements tds = trs.get(i).select("td");
                Map returnResult = new HashMap<>();
                for (int j = 0; j < tds.size(); j++) {
                    Element td = tds.get(j);
                    if(j==0 ){
                        if(td.childNodeSize()==1){
                            role = td.childNode(0).attributes().get("value");
                            returnResult.put("role", role);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {//去除隐藏node
                                    role = td.childNode(k).attributes().get("value");
                                    role = role.replaceAll("\\s*", "");
                                    if (null != role && !role.equals("")) {
                                        returnResult.put("role", role);
                                        break;
                                    }
                                }
                                if(k==td.childNodeSize()-1){
                                    returnResult.put("role", "");
                                }
                            }
                        }
                    }
                    if(j==1 ){
                        if(td.childNodeSize()==1){
                            type = td.childNode(0).attributes().get("value");
                            returnResult.put("type", type);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    type = td.childNode(k).attributes().get("value");
                                    type = type.replaceAll("\\s*", "");
                                    if (null != type && !type.equals("")) {
                                        returnResult.put("type", type);
                                        break;
                                    }
                                }
                                if(k==td.childNodeSize()-1){
                                    returnResult.put("type", "");
                                }
                            }
                        }
                    }
                    if(j==2){
                        if(td.childNodeSize()==1){
                            name = td.childNode(0).attributes().get("value");
                            returnResult.put("name", name);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    name = td.childNode(k).attributes().get("value");
                                    name = name.replaceAll("\\s*", "");
                                    if (null != name && !name.equals("")) {
                                        returnResult.put("name", name);
                                        break;
                                    }

                                }
                                if(k==td.childNodeSize()-1){
                                    returnResult.put("name", "");
                                }
                            }
                        }
                    }
                    if(j==3){
                        if(td.childNodeSize()==1){
                            companyType = td.childNode(0).attributes().get("value");
                            returnResult.put("companyType", companyType);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    companyType = td.childNode(k).attributes().get("value");
                                    companyType = companyType.replaceAll("\\s*", "");
                                    if (null != companyType && !companyType.equals("")) {
                                        returnResult.put("companyType", companyType);
                                        break;
                                    }
                                }
                                if(k==td.childNodeSize()-1){
                                    returnResult.put("companyType", "");
                                }
                            }
                        }
                    }
                    if(j==4){
                        if(td.childNodeSize()==1){
                            IdCardType = td.childNode(0).attributes().get("value");
                            returnResult.put("IdCardType", IdCardType);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    IdCardType = td.childNode(k).attributes().get("value");
                                    IdCardType = IdCardType.replaceAll("\\s*", "");
                                    if (null != IdCardType && !IdCardType.equals("")) {
                                        returnResult.put("IdCardType", IdCardType);
                                        break;
                                    }
                                }
                                if(k==td.childNodeSize()-1){
                                    returnResult.put("IdCardType", "");
                                }
                            }
                        }
                    }
                    if(j==5){
                        if(td.childNodeSize()==1){
                            IdCardNo = td.childNode(0).attributes().get("value");
                            returnResult.put("IdCardNo", IdCardNo);
                        }else {
                            for (int k = 0; k < td.childNodeSize(); k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    IdCardNo = td.childNode(k).attributes().get("value");
                                    IdCardNo =  IdCardNo.replaceAll("\\s*", "");
                                    if (null != IdCardNo && !IdCardNo.equals("")) {
                                        returnResult.put("IdCardNo", IdCardNo);
                                        break;
                                    }
                                }
                                if (k == td.childNodeSize() - 1) {
                                    returnResult.put("IdCardNo", "");
                                }
                            }
                        }
                    }
                    if(j==6){
                        if(td.childNodeSize()==1){
                            address = td.childNode(0).attributes().get("value");
                            returnResult.put("address", address);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    address = td.childNode(k).attributes().get("value");
                                    address = address.replaceAll("\\s*", "");
                                    if (null != address && !address.equals("")) {
                                        returnResult.put("address", address);
                                        break;
                                    }
                                }
                                if (k == td.childNodeSize() - 1) {
                                    returnResult.put("address", "");
                                }
                            }
                        }
                    }
                    if(j==7){
                        if(td.childNodeSize()==1){
                            email = td.childNode(0).attributes().get("value");
                            returnResult.put("email", email);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    email = td.childNode(k).attributes().get("value");
                                    email =  email.replaceAll("\\s*", "");
                                    if (null != email && !email.equals("")) {
                                        returnResult.put("email", email);
                                        break;
                                    }
                                }
                                if (k == td.childNodeSize() - 1) {
                                    returnResult.put("email", "");
                                }
                            }
                        }
                    }
                    if(j==8){
                        if(td.childNodeSize()==1){
                            telNum = td.childNode(0).attributes().get("value");
                            returnResult.put("telNum", telNum);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    telNum = td.childNode(k).attributes().get("value");
                                    telNum = telNum.replaceAll("\\s*", "");
                                    if (null != telNum && !telNum.equals("")) {
                                        returnResult.put("telNum", telNum);
                                        break;
                                    }
                                }
                                if (k == td.childNodeSize() - 1) {
                                    returnResult.put("telNum", "");
                                }
                            }
                        }
                    }
                    if(j==9){
                        if(td.childNodeSize()==1){
                            mobilePhone = td.childNode(0).attributes().get("value");
                            returnResult.put("mobilePhone", mobilePhone);
                        }else {
                            for(int k=0 ;k<td.childNodeSize();k++) {
                                if (!td.childNode(k).attributes().hasKey("type")) {
                                    mobilePhone = td.childNode(k).attributes().get("value");
                                    mobilePhone = mobilePhone.replaceAll("\\s*", "");
                                    if (null != mobilePhone && !mobilePhone.equals("")) {
                                        returnResult.put("mobilePhone", mobilePhone);
                                        break;
                                    }
                                }
                                if (k == td.childNodeSize() - 1) {
                                    returnResult.put("mobilePhone", "");
                                }
                            }
                        }
                    }
                }
                lastResult.put(i,returnResult);
            }
        }

        returnMap.put("nextParams", null);
        returnMap.put("lastResult", lastResult);
        BaseCarInfoResponse carBaseInfoResponse = new BaseCarInfoResponse();
        Map lastResultCinsureMap = (Map) returnMap.get("lastResult");
        Iterator entries = lastResultCinsureMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Integer key = (Integer)entry.getKey();
             Map value = (Map)entry.getValue();
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
        System.out.println("carBaseInfoResponse = " + carBaseInfoResponse);

    }


    @Test
    public void testCitemKind(){



        Map returnMap = new HashMap<>();
        Map lastResult = new HashMap<>();
        returnMap.put("nextParams", null);
        File file=new File("d:/3.html");
        Document doc=null;
        try{
             doc = Jsoup.parse(file, "GB2312");
        }catch (Exception e){}


        //主险
        //初始化主险保险金额
        lastResult.put("CheSun",0);
        lastResult.put("DaoQiang",0);
        lastResult.put("SanZhe",0);
        lastResult.put("SiJi",0);
        lastResult.put("ChengKe",0);

        Elements itemKindMaintrs = doc.getElementById("itemKindMain").select("tbody").select("tr");
        for(int i = 0;i<itemKindMaintrs.size();i++) {
            Elements tds = itemKindMaintrs.get(i).select("td");
            if(tds.size()==32){//主险种以上保险的数据
               String insureName = tds.get(1).child(1).attributes().get("value");
                Double insureCost = Double.parseDouble(tds.get(4).childNode(1).childNode(1).attributes().get("value").toString());
                if(insureName.equals("机动车损失保险")){
                    System.out.println("insureName =" +insureName +" insureCost = "+insureCost+"\n");
                    lastResult.put("CheSun",insureCost);
                }
                if(insureName.equals("盗抢险")){
                    System.out.println("insureName =" +insureName +" insureCost = "+insureCost+"\n");
                    lastResult.put("DaoQiang",insureCost);
                }
                if(insureName.equals("第三者责任保险")){
                    System.out.println("insureName =" +insureName +" insureCost = "+insureCost+"\n");
                    lastResult.put("SanZhe",insureCost);
                }
                if(insureName.equals("车上人员责任险（司机）")){
                    System.out.println("insureName =" +insureName +" insureCost = "+insureCost+"\n");
                    lastResult.put("SiJi",insureCost);
                }
                if(insureName.equals("车上人员责任险（乘客）")){
                    System.out.println("insureName =" +insureName +" insureCost = "+insureCost+"\n");
                    lastResult.put("ChengKe",insureCost);
                }
            }
        }

        //附加险初始化
        lastResult.put("Boli",0);
        lastResult.put("ZiRan",0);
        lastResult.put("HuaHen",0);
        lastResult.put("SheShui",0);
        lastResult.put("CheDeng",0);
        Elements itemKindSubtrs = doc.getElementById("itemKindSub").select("tr");
        for(int i = 0;i<itemKindSubtrs.size();i++) {
            Elements tds = itemKindSubtrs.get(i).select("td");
            if(tds.size()==27){//主表中数据 玻璃险
                String insureName = tds.get(1).child(0).attributes().get("value");

                if(insureName.equals("玻璃单独破碎险")){
                    try{
                        Element elementBoliSelect = tds.get(1).child(1).child(0);
                        if(elementBoliSelect.tagName().equals("select")){
                            Elements BoliElements = elementBoliSelect.getAllElements();
                            for(int k=0;k<BoliElements.size();k++){
                                if(BoliElements.get(k).attributes().hasKey("selected")){
                                    String boliValue = BoliElements.get(k).attributes().get("value");
                                    if(boliValue.equals("10")||boliValue.equals("11")){//国产玻璃
                                        lastResult.put("Boli",1);
                                        System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                                    }
                                    else if(boliValue.equals("20")||boliValue.equals("21")){
                                        lastResult.put("Boli",2);
                                        System.out.println("insureName =" +insureName +" insureCost = "+2+"\n");
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception E){
                        logger.info("抓取机器人，【 PICC 解析车辆险种玻璃险失败】");
                    }
                }else if(insureName.equals("自燃损失险")){
                    Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                    lastResult.put("ZiRan",1);
                }else if(insureName.equals("车身划痕损失险")){
                    Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                    lastResult.put("HuaHen",1);
                }else if(insureName.equals("发动机特别损失险")){
                    Element elementBoliSelect = tds.get(3).child(0);//tds.get(3).child(0).attributes().get("value")
                    lastResult.put("SheShui",1);
                }
                //// TODO: 2016/5/18 1、车灯险  2、约定区域通行费用特约条款 3、法律费用特约条款 4、附加油污污染责任保险



            }
        }
        //不计免赔
        lastResult.put("BuJiMianCheSun",0);
        lastResult.put("BuJiMianSanZhe",0);
        lastResult.put("BuJiMianDaoQiang",0);
        lastResult.put("BuJiMianRenYuan",0);
        lastResult.put("BuJiMianFuJia",0);
        Elements itemKindSpecialtrs = doc.getElementById("itemKindSpecial").select("tbody").select("tr");
        for(int i = 0;i<itemKindSpecialtrs.size();i++) {
            Elements tds = itemKindSpecialtrs.get(i).select("td");
            if(tds.size()==27){//主表中数据
                String insureName = tds.get(1).child(0).attributes().get("value");

                if(insureName.equals("不计免赔率（车辆损失险）")){
                    lastResult.put("BuJiMianCheSun",1);
                    System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                }else if(insureName.equals("不计免赔率（三者险）")){
                    lastResult.put("BuJiMianSanZhe",1);
                    System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                }else if(insureName.equals("不计免赔率（机动车盗抢险）")){
                    lastResult.put("BuJiMianDaoQiang",1);
                    System.out.println("insureName =" +insureName +" insureCost = "+1+"\n");
                }else if(insureName.contains("不计免赔率（车上人员责任险")){
                    lastResult.put("BuJiMianRenYuan",1);
                    System.out.println(" BuJiMianRenYuan insureName =" +insureName +" insureCost = "+1+"\n");
                }else{
                    lastResult.put("BuJiMianFuJia",1);
                    System.out.println(" BuJiMianFuJia insureName =" +insureName +" insureCost = "+1+"\n");
                }
            }
        }
        returnMap.put("lastResult", lastResult);
    }
}
