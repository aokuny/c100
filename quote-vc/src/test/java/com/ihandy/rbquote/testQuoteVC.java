package com.ihandy.rbquote;

import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.service.IService;
import net.sf.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

    @Test
    public void testJsoupTabel(){
        String html ="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "  //for global use\n" +
                "  var contextRootPath = \"/prpall\";\n" +
                "var guangDongFlag = false;\n" +
                "var guangZhouFlag = false;\n" +
                "\n" +
                "</script>\n" +
                "<script type=\"text/javascript\" src=\"/prpall/pages/common/cb/GuangdongSys.js\"></script>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=GBK\">\n" +
                "<!--  \n" +
                "-->\n" +
                "<link href=\"/prpall/style/style_all.css\" type=\"text/css\" rel=\"stylesheet\" />\n" +
                "\n" +
                "<link href=\"/prpall/style/calendar.css\" type=\"text/css\" rel=\"stylesheet\" />\n" +
                "\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/prpall/style/Standard.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/prpall/style/tabview.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\"\n" +
                "\thref=\"/prpall/pages/js/build/reset-fonts-grids/reset-fonts-grids.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\"\n" +
                "\thref=\"/prpall/pages/js/build/resize/assets/skins/sam/resize.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\"\n" +
                "\thref=\"/prpall/pages/js/build/layout/assets/skins/sam/layout.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\"\n" +
                "\thref=\"/prpall/pages/js/build/button/assets/skins/sam/button.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/prpall/style/blackbird.css\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/prpall/pages/js/build/assets/skins/sam/skin.css\" />\n" +
                "\n" +
                "\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=GBK\"> \n" +
                "<script type=\"text/javascript\">\n" +
                "/** deleteNode */\n" +
                "function  delNode(){   \n" +
                "  var nodeId = \"loading\";\n" +
                "  try{   \n" +
                "\t  var div =document.getElementById(nodeId);  \n" +
                "\t  if(div !==null){\n" +
                "\t\t  document.body.removeChild(div);\n" +
                "\t\t  div=null;    \n" +
                " \t  }  \n" +
                "  } catch(e){   \n" +
                "  \t   alert(\"delete node \"+nodeId+\" error\");\n" +
                "  }   \n" +
                "}\n" +
                "//骞夸??规? add by huang \n" +
                "function d(obj){\n" +
                "\tvar result = document.getElementById(obj);\n" +
                "\tif(result){\n" +
                "\t\treturn result;\n" +
                "\t}\n" +
                "\tresults = document.getElementsByName(obj);\n" +
                "\tif(results && results.length>0){\n" +
                "\t\treturn results[0];\t\t\n" +
                "\t}\n" +
                "}\n" +
                "//delNode();//delete loading   \n" +
                "</script>        \n" +
                "<script language=\"vbscript\"   src=\"/prpall/common/js/urlencode.vbs\"></script>\n" +
                "<script language=\"javascript\" charset=\"UTF-8\" src=\"/prpall/requestCombo?/common/js/sinosoft.js&/common/js/Common.js\n" +
                "&/common/js/Application.js\n" +
                "&/common/js/prototype.js\n" +
                "&/common/dwr/engine-min.js\n" +
                "&/common/dwr/util-min.js\n" +
                "&/widgets/yui/yahoo-dom-event/v2.8.0/yahoo-dom-event.js\n" +
                "&/widgets/yui/connection/connection-min.js\n" +
                "&/widgets/yui/element/element-beta-min.js\n" +
                "&/widgets/yui/container/container-min.js\n" +
                "&/widgets/yui/datasource/datasource-beta-min.js\n" +
                "&/widgets/yui/datatable/datatable-beta-min.js\n" +
                "&/widgets/yui/yahoo-dom-event/element-min.js\n" +
                "&/widgets/yui/yahoo/json-min.js\n" +
                "&/pages/js/build/yuiloader/yuiloader-min.js\n" +
                "&/pages/js/build/button/button-min.js\n" +
                "&/pages/js/build/layout/layout-min.js\n" +
                "&/pages/js/build/event/event-min.js\n" +
                "&/pages/js/build/resize/resize-min.js\n" +
                "&/pages/js/build/animation/animation-min.js\n" +
                "&/pages/js/build/layout/layout-min.js\n" +
                "&/pages/js/build/editor/editor.js\n" +
                "&/common/js/CodeSelect.js\n" +
                "&/widgets/datatable_init.js\n" +
                "&/common/validdate/checkdata.js\n" +
                "&/common/js/MulLine.js\n" +
                "&/common/js/tabber.js\n" +
                "&/common/js/HashMap.js\"></script>\n" +
                "<script language='Javascript' src='/prpall/common/js/accessKey.js'></script>\n" +
                "<script language='Javascript' src='/prpall/dwr/interface/dwrInvokeDataAction.js'></script>\n" +
                "<script language='Javascript' src='/prpall/dwr/interface/qdwrInvokeDataAction.js'></script>\n" +
                "<script language='Javascript' src='/prpall/dwr/interface/dwrQueryRenewalInfoAction.js'></script>\n" +
                "\n" +
                "<script language=\"javascript\" src=\"/prpall/widgets/yui/autocomplete/autocomplete-min.js\"></script>\n" +
                "<script language=\"javascript\" src=\"/prpall/widgets/yui/tabview/tabview-min.js\"></script>\n" +
                "<script language=\"javascript\" src=\"/prpall/common/js/select_ui.js\"></script>\n" +
                "\n" +
                "<!--\n" +
                "<script language=\"javascript\" src=\"/prpall/common/blackbirdjs/blackbird.js\"></script>\n" +
                "-->\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "\tvar i18n = new Object();\n" +
                "\ti18n.navigator = new Object();\n" +
                "\ti18n.navigator.page=\"页\";\n" +
                "\ti18n.navigator.first=\"首页\";\n" +
                "\ti18n.navigator.last=\"尾页\";\n" +
                "\ti18n.navigator.prev=\"上页\";\n" +
                "\ti18n.navigator.next=\"下页\";\t\n" +
                "\ti18n.navigator.records=\"条记录\";\t\t\n" +
                "\ti18n.navigator.page=\"页\";\n" +
                "\ti18n.navigator.more=\"更多\";\n" +
                "\t\t\n" +
                "\ti18n.prompt = new Object();\n" +
                "\ti18n.prompt.ok=\"确定\";\t\n" +
                "\ti18n.prompt.cancel=\"取消\";\t\n" +
                "\t\n" +
                "\ti18n.errors = new Object();\t\n" +
                "\ti18n.errors.deletefail=\"删除失败\";\t\n" +
                "</script>\n" +
                "\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<title>理赔信息</title>\n" +
                "\t\t\n" +
                " \n" +
                "<div id=\"loading\"  style=\"z-index:9000;position:absolute;background:white url(/prpall/widgets/loading/images/block-bg.gif) repeat-x;border:3px solid #B2D0F7;color:#003366;width:180px;height:50px;left:45%;top:40%;padding:20px;font:bold 14px verdana,tahoma,helvetica; text-align:center;\">\n" +
                "\t<div style=\"font-size:8pt;background-image:url(/prpall/widgets/loading/images/loading.gif);background-repeat: no-repeat;padding-left:20px;\theight:18px;\ttext-align:left;\">\n" +
                "\t\t加载中...\n" +
                "\t</div>\n" +
                "</div>\n" +
                "<script type=\"text/javascript\">\n" +
                "/** deleteNode when page load*/\n" +
                "if (window.attachEvent) {   \n" +
                "   window.attachEvent(\"onload\", delNode);   \n" +
                "   window.attachEvent(\"onunload\", beforeWindowClose);   \n" +
                "} else if (window.addEventListener) {   \n" +
                "   window.addEventListener(\"load\", delNode, false);    \n" +
                "   window.addEventListener(\"unload\", beforeWindowClose, false);    \n" +
                "}\n" +
                "/** deleteNode */\n" +
                "function  delNode(){   \n" +
                "  var nodeId = \"loading\";\n" +
                "  try{   \n" +
                "\t  var div =document.getElementById(nodeId);  \n" +
                "\t  if(div !=null){\n" +
                "\t\t  document.body.removeChild(div);\n" +
                "\t\t  div=null;    \n" +
                " \t  }  \n" +
                "  } catch(e){   \n" +
                "  \t   alert(\"delete node \"+nodeId+\" error\");\n" +
                "  }   \n" +
                "}\n" +
                "function beforeWindowClose(){\n" +
                "\t//如果是转投保，则将转投保的车辆解锁\t\n" +
                "\tvar minusFlag = document.getElementById(\"minusFlag\");\n" +
                "\tvar editType = document.getElementById(\"editType\");\n" +
                "\tif(minusFlag==null){\n" +
                "\t\treturn;\n" +
                "\t}\n" +
                "\tif(minusFlag.value==\"1\" && (editType.value==\"NEW\" || editType.value==\"UPDATE\" || editType.value==\"RENEWAL\")){\n" +
                "\t\tvar contractNo = document.getElementById(\"prpBatchVehicle.id.contractNo\");\n" +
                "\t\tvar serialNo = document.getElementById(\"paramIndex\");\n" +
                "\t\tvar strUrl = \"/prpall/batch/unlockVehicle.do?\";\n" +
                "\t\tstrUrl += \"prpBatchMain.contractNo=\" + contractNo.value;\n" +
                "\t\tstrUrl += \"&serialNo=\"+serialNo.value;\n" +
                "\t\tvar callback = {\n" +
                "\t\t\tsuccess:function (res) {\n" +
                "\t\t\t\t\n" +
                "\t\t\t}, \n" +
                "\t\t\tfailure:function (res)  {\n" +
                "\t\t\t}\n" +
                "\t\t};\n" +
                "\t\tvar transaction = YAHOO.util.Connect.asyncRequest(\"POST\", strUrl, callback, null);\n" +
                "\t}\n" +
                "}\n" +
                "//监视当关闭投保单录入页面时触发windowClose方法\n" +
                "//YAHOO.util.Event.addListener(document.getElementById(\"fm\"), 'unload', beforeWindowClose);\n" +
                "//YAHOO.util.Event.addListener(document.getElementById(\"fm\"), 'beforeunload', beforeWindowUnload);\n" +
                "</script>\n" +
                "\n" +
                "\n" +
                "\t</head>\n" +
                "\t<body id=\"all_title\" style=\"width: 100%; overflow-x: hidden;\">\n" +
                "\t\t<table class=\"fix_table\" border=\"0\" id=\"insertUndwrtRow\"\n" +
                "\t\t\tstyle=\"width: 100%\" vtitle=\"MulLine\">\n" +
                "\t\t\t<thead>\n" +
                "\t\t\t\t<tr class=\"sort\">\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t报案号\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t立案号\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t出险日期\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t报案日期\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"10%\">\n" +
                "\t\t\t\t\t\t报案注销标志\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t立案日期\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"10%\">\n" +
                "\t\t\t\t\t\t立案注销标志\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t结案标志\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t责任赔款\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"9%\">\n" +
                "\t\t\t\t\t\t总赔付金额\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t\t<th width=\"8%\">\n" +
                "\t\t\t\t\t\t地址\n" +
                "\t\t\t\t\t</th>\n" +
                "\t\t\t\t</tr>\n" +
                "\t\t\t</thead>\n" +
                "\t\t\t<tbody>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t<tr align=\"center\">\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].registNo\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].registNo\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"RDAT201511000000094684\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].claimNo\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].claimNo\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"ADAT201511010761001142\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].damageDate\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].damageDate\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2015-06-14\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].reportDate\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].reportDate\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2015-06-14\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].registCancelFlag\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].registCancelFlag\"\n" +
                "\t\t\t\t\t\t\t\tclass=\"input_w w_p80\" value=\"0\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].claimDate\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].claimDate\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2015-06-14\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].claimCancelFlag\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].claimCancelFlag\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"0\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].endCaseDate\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].endCaseDate\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2015-06-14\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].sumEstiPaid\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].sumEstiPaid\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2000.00\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].sumPaid\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].sumPaid\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"2000.00\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t\t<td>\n" +
                "\t\t\t\t\t\t\t<input type=\"text\" name=\"caseInfoVoX[0].address\"\n" +
                "\t\t\t\t\t\t\t\tid=\"caseInfoVoX[0].address\" class=\"input_w w_p80\"\n" +
                "\t\t\t\t\t\t\t\tvalue=\"石景山区西山奥园\" />\n" +
                "\t\t\t\t\t\t</td>\n" +
                "\t\t\t\t\t</tr>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "  \t\t\t  \t\n" +
                "\t\t\t</tbody>\n" +
                "\t\t</table>\n" +
                "\t</body>\n" +
                "</html>";
        Document doc = Jsoup.parse(html);
        Elements trs = doc.getElementById("insertUndwrtRow").select("tr");
        for(int i = 1;i<trs.size();i++){
            Elements tds = trs.get(i).select("td");
            for(int j = 0;j<tds.size();j++){
                String text = tds.get(j).select("input").val();
                System.out.println(j+"---->"+trs.get(0).select("th").get(j).text() +"---->"+text+"\n");
            }
        }
    }
}
