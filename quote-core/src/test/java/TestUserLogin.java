import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhujiajia on 16/5/25.
 */
public class TestUserLogin {

    @Test
    public void TestDemo() {

        //获取session信息
        String url_login = "http://dm.axatp.com/login.do";
//        String cookieValue = HttpsUtil.sendGetForAxatp(url_login, null, "GBK").get("cookieValue");
//        System.out.println(cookieValue);

        //获取要邀请码标识
        StringBuffer param_login = new StringBuffer();
        param_login.append("memberName=jtl_bj&");
        param_login.append("flag=ajaxRecommendCode");
//        String login_two = HttpsUtil.sendPost(url_login, param_login.toString(), cookieValue, "GBK").get("html");
//        System.out.println(login_two);

        //获取验证码图片
        String url_randCode = "http://dm.axatp.com/getAdditionNo.do?type=login";
//        Map imgMap = HttpsUtil.getURLImgOutByte(url_randCode, cookieValue, "GBK");
//        System.out.println(html_randCode);

        //生成图片
//        String path = "../login/img/";
//        File f = new File("");
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//        String filePath = path + File.separator + "code_"+new Date().getTime()+"." + imgMap.get("type");
//        File imageFile = new File(filePath);
//        //创建输出流
//        try {
//            FileOutputStream outStream = null;
//            outStream = new FileOutputStream(imageFile);
//            //写入数据
//            outStream.write((byte[]) imgMap.get("byte"));
//            //关闭输出流
//            outStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //解析验证码
        String url_getCode = "http://192.168.4.117:8011/GetSeccode.aspx?ImgURL=http://dm.axatp.com/getAdditionNo.do?type=login";
//        String html_getCode = HttpsUtil.sendGetForAxatp(url_getCode, cookieValue, "GBK").get("html");
//        System.out.println(html_getCode);

        //用户登录
        StringBuffer paramSb = new StringBuffer();
        paramSb.append("memberName=jtl_bj&");
        paramSb.append("voucherNoArray=&");
        paramSb.append("voucherNoArrayLogin=&");
        paramSb.append("defaultAgentCode=1&");
        paramSb.append("isVIP=false&");
        paramSb.append("linkResource=&");
        paramSb.append("flag=login&");
        paramSb.append("memberName=jtl_bj&");
        paramSb.append("password=123456&");
        paramSb.append("showRecommendCode=1&");
        paramSb.append("isAgent=3212&");
        paramSb.append("checkRecommendCode=0&");
        paramSb.append("recommendCode=123&");
//        paramSb.append("randomCode=" + html_getCode + "&");
        String param = paramSb.toString();
        param = param.substring(0, param.length() - 1);
//        String coo="JSESSIONID=315C14F8FDD9A3B7407806A2CFA3F7D2";
//        String html_index = HttpsUtil.sendPost(url_login, param.toString(), cookieValue, "GBK").get("html");
//        System.out.println(html_index);


    }

}
