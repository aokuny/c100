package com.ihandy.rbquote;

/**
 * Created by fengwen on 2016/5/17.
 */
    import java.io.BufferedReader;
    import java.io.DataOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.net.URLConnection;
    import java.security.MessageDigest;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import org.apache.commons.io.IOUtils;
    import org.apache.commons.lang3.StringUtils;


    import net.sf.json.JSONObject;


public class TestHttpUtilbIHu {
        /**
         * 向指定URL发送GET方法的请求
         *
         * @param url
         *            发送请求的URL
         * @param param
         *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
         * @return URL 所代表远程资源的响应结果
         */
        public static String sendGet(String url, String param) {
            String result = "";
            BufferedReader in = null;
            try {
                String urlNameString = url + "?" + param;
                URL realUrl = new URL(urlNameString);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                connection.setRequestProperty("Host", "quote.zhonghe-bj.com:8085");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                // 建立实际的连接
                connection.connect();
                // 定义 BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                System.out.println("发送GET请求出现异常！" + e);
                e.printStackTrace();
            }
            // 使用finally块来关闭输入流
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return result;
        }

        /**
         * 向指定 URL 发送POST方法的请求
         *
         * @param url
         *            发送请求的 URL
         * @param param
         *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
         * @return 所代表远程资源的响应结果
         */
        public static String sendPost(String url, String param) {
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";
            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection conn = realUrl.openConnection();
                // 设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
                // 定义BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                System.out.println("发送 POST 请求出现异常！" + e);
                e.printStackTrace();
            }
            // 使用finally块来关闭输出流、输入流
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return result;
        }

        public static String doHttpPost(String xmlInfo, String URL) {
            System.out.println("发起的数据:" + xmlInfo);
            byte[] xmlData = xmlInfo.getBytes();
            InputStream instr = null;
            java.io.ByteArrayOutputStream out = null;
            try {
                URL url = new URL(URL);
                URLConnection urlCon = url.openConnection();
                urlCon.setDoOutput(true);
                urlCon.setDoInput(true);
                urlCon.setUseCaches(false);
                urlCon.setRequestProperty("Content-Type", "text/xml");
                urlCon.setRequestProperty("Content-length", String.valueOf(xmlData.length));
                System.out.println(String.valueOf(xmlData.length));
                DataOutputStream printout = new DataOutputStream(urlCon.getOutputStream());
                printout.write(xmlData);
                printout.flush();
                printout.close();
                instr = urlCon.getInputStream();
                byte[] bis = IOUtils.toByteArray(instr);
                String ResponseString = new String(bis, "UTF-8");
                if ((ResponseString == null) || ("".equals(ResponseString.trim()))) {
                    System.out.println("返回空");
                }
                System.out.println("返回数据为:" + ResponseString);
                return ResponseString;

            } catch (Exception e) {
                e.printStackTrace();
                return "0";
            } finally {
                try {
                    out.close();
                    instr.close();
                } catch (Exception ex) {
                    return "0";
                }
            }
        }


        /**
         * 向指定 URL 发送POST方法的请求
         *
         * @param url
         *            	发送请求的 URL
         * @param param
         *            	请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
         * @param sessionId
         * 				保持会话连接，
         * @return 所代表远程资源的响应结果
         */
        public static Map<String, String> sendPost(String url, String param, String sessionId) {
            Map<String, String> resultMap = new HashMap<String, String>();
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";
            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection conn = realUrl.openConnection();
                // 设置通用的请求属性
                //conn.setRequestProperty("Host", "quote.zhonghe-bj.com:8085");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setRequestProperty("Accept", "*/*");
                conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                if(StringUtils.isNotBlank(sessionId)){
                    conn.setRequestProperty("Cookie", sessionId);
                }
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
                // 定义BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String cookieValue = conn.getHeaderField("Set-Cookie");
                resultMap.put("cookieValue", cookieValue);
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                System.out.println("发送 POST 请求出现异常！" + e);
                e.printStackTrace();
            }
            // 使用finally块来关闭输出流、输入流
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            resultMap.put("result", result);
            return resultMap;
        }

        /**
         * 发送HTTP，JSON格式
         * @param url
         * @param jsonStr
         */
        public static String sendPostForJson(String url, String jsonStr) {
            StringBuffer sb = new StringBuffer("");
            try{
                //创建连接
                URL realUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                connection.connect();
                //POST请求
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(jsonStr.getBytes("UTF-8"));//这样可以处理中文乱码问题
                out.flush();
                out.close();
                //读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    sb.append(lines);
                }
                reader.close();
                // 断开连接
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        public static void main(String[] args) throws Exception {
            long start = System.currentTimeMillis();
            String key = "csdfse784";
            String agent = "3820";

//		String carNO = "京HD3639";
//		String EngineNo = "1026510";
//		String CarVin = "LHGRU5746F2026462";
//		String RegisterDate = "2015-04-03";
//		String MoldName = "缤智牌HG7180HAM5";
//		String IntentionCompany = "0";

//		String carNO = "京N3HU88";
//		String EngineNo = "8634342";
//		String CarVin = "LDCC13L25A0206503";
//		String RegisterDate = "2010-04-28";
//		String MoldName = "东风雪铁龙DC7165DTA轿车";
//		String IntentionCompany = "2";

            String carNO = "京P55M11";
            String EngineNo = "4LA4D8297";
            String CarVin = "LGXC16DF4A0169664";
            String RegisterDate = "2006-10-01";
            String MoldName = "比亚迪F3";
            String IntentionCompany = "2";
            //"ForceTotal":627.56,"TaxTotal":400

            //String carNO = "京PT3G98";
           // String carNO = "京N3HU88";
            //String carNO = "翼A723KG";
            //String carNO = "京QA5M75";
            //String carNO = "京KT3495";
            //String carNO = "京KV5909";
          // String carNO = "京QWZ015";
            //String carNO = "京Q8D075";
            // String carNO = "京KV5909";
            //String carNO="冀F03E65";
            //String carNO="京P55M11";
            //String carNO="冀A723KG";
            //String carNO="京QA5M75";
            //String carNO="吉JC1112";
            //String carNO="京QK92X9";
            //String carNO = "京MH0501";
            //String carNO = "京P2QZ85";


            String custKey = "e8370f8fb7d7d6ec7bcaa8b3b738e409";

            String param1 = "LicenseNo=" + carNO + "&CityCode=1&Agent=" + agent + "&IsPublic=0&CustKey=" + custKey;
            //String paramTest = "LicenseNo=京P55M11&CityCode=1&Agent=3820&IsPublic=0&CustKey=492f4a6c11781385e9cf08651e77e148&SecCode=6634bf16b66dddd263a824f79f9a7e3a";
            String secCode1 = MD5(param1 + key);
            param1 = param1 + "&SecCode=" + secCode1.toLowerCase();
           // String url1 = "http://i.91bihu.com/api/claim/GetCreditInfo";
            String url1 ="http://i.91bihu.com/api/CarInsurance/getreinfo";
            String result = sendGet(url1, param1);
            System.err.println(result);
            System.err.println("用时：" + String.valueOf(System.currentTimeMillis() - start));

            //上传
		String url = "http://i.91bihu.com/api/CarInsurance/PostPrecisePrice";
		//封装JSON参数
		Map<String, String> jsonObj = new HashMap<>();
		//报价信息
		jsonObj.put("IsSingleSubmit", "1");//IsSingleSubmit	是	Int	是否对单个保险公司核保，1=是，0=否
		jsonObj.put("IntentionCompany", IntentionCompany);//IntentionCompany	是	String	意向投保公司(-1:只报价不核保、0:平安、1:太平洋、2:人保)
		//车辆信息
		jsonObj.put("LicenseNo", carNO);//LicenseNo	是	String	车牌号
		jsonObj.put("EngineNo", EngineNo);//EngineNo	是	String	发动机号
		jsonObj.put("CarVin", CarVin);//CarVin	是	String	车架号
		jsonObj.put("RegisterDate", RegisterDate);//RegisterDate	是	string	注册日期
		jsonObj.put("MoldName", MoldName);//MoldName	是	String	品牌型号
		jsonObj.put("CarType", "0");//CarType	是	Int	车辆类型：0客车，1货车
		jsonObj.put("IsNewCar", "0");//IsNewCar	是	Int	是否新车（0：否；1：新车）
		jsonObj.put("CarUsedType", "0");//CarUsedType	是	string	使用性质（0营运、非营运）
		jsonObj.put("Citycode", "10");//Citycode	是	Int	城市Id（目前系统只支持北京地区）（北京:110000）
		//险种信息
		jsonObj.put("ForceTax", "1");//ForceTax	是	Int	交强险+车船税(1:报价交强车船，0：不报价交强车船)
		jsonObj.put("BoLi", "1");//BoLi	是	Double	玻璃单独破碎险，0-不投保，1国产，2进口
		jsonObj.put("CheDeng", "0");//CheDeng	是	Double	倒车镜、车灯单独损坏险，0-不投保，1-国产，2-进口
		jsonObj.put("SheShui", "1");//SheShui	是	Double	涉水行驶损失险，0-不投保，1投保
		jsonObj.put("HuaHen", "2000");//HuaHen	是	Double	车身划痕损失险，0-不投保，>0投保(具体金额)（2，000；5，000；10，000；20，000）
		jsonObj.put("SiJi", "10000");//SiJi	是	Double	车上人员责任险(司机) ，0-不投保，>0投保(具体金额）（10，000；20，000；30，000；40，000；50，000；100，000；200，000）
		jsonObj.put("ChengKe", "10000");//ChengKe	是	Double	车上人员责任险(乘客) ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；200，000）
		jsonObj.put("CheSun", "1");//CheSun	是	Double	机动车损失保险，0-不投保，>0投保
		jsonObj.put("DaoQiang", "1");//DaoQiang	是	Double	全车盗抢保险，0-不投保，>0投保
		jsonObj.put("SanZhe", "50000");//SanZhe	是	Double	第三者责任保险，0-不投保，>0投保(具体金额)（50，000；100，000；150，000；200，000；300，000；500，000；1，000，000；1，500，000）
		jsonObj.put("ZiRan", "1");//ZiRan	是	Double	自燃损失险，0-不投保，1投保
		jsonObj.put("BuJiMianCheSun", "1");//BuJiMianCheSun	是	Double	不计免赔险(车损) ，0-不投保，1投保
		jsonObj.put("BuJiMianDaoQiang", "1");//BuJiMianDaoQiang	是	Double	不计免赔险(盗抢) ，0-不投保，1投保
		jsonObj.put("BuJiMianFuJia", "1");//BuJiMianFuJia	是	Double	不计免赔险(附加险) ，0-不投保，1投保
		jsonObj.put("BuJiMianRenYuan", "1");//BuJiMianRenYuan	是	Double	不计免赔险(车上人员) ，0-不投保，1投保
		jsonObj.put("BuJiMianSanZhe", "1");//BuJiMianSanZhe	是	Double	不计免赔险(三者) ，0-不投保，1投保
           // 平台参数

		jsonObj.put("CustKey", custKey);
		jsonObj.put("Agent", agent);//Agent	是	Int	调用平台标识
		StringBuffer paramSb = new StringBuffer();
		for(String mapKey : jsonObj.keySet()){
			String value = jsonObj.get(mapKey);
			paramSb.append(mapKey + "=" + value + "&");
		}
		String secCode = MD5(paramSb.toString() + key).toLowerCase();
		paramSb.append("secCode=" + secCode);


		String result2 = sendGet(url, paramSb.toString());
		System.err.println(result2);

            //获取报价接口
		for(int i=0;i<4;i++){
			Thread.sleep(1000L);
			String param = "LicenseNo=" + carNO + "&IntentionCompany=" + IntentionCompany + "&Agent=" + agent + "&CustKey=" + custKey;
			String secCode2 = MD5(param + key);
			param = param + "&SecCode=" + secCode2.toLowerCase();
			String url2 = "http://i.91bihu.com/api/CarInsurance/GetPrecisePrice";
			String result1 = sendGet(url2, param);
			System.err.println(result1);
		}

		//获取核保信息
		String param = "LicenseNo=" + carNO + "&IntentionCompany=" + IntentionCompany + "&Agent=" + agent+"&CustKey=" + custKey;
		String secCode2 = MD5(param + key);
		param = param + "&SecCode=" + secCode2.toLowerCase();
		String url4 = "http://i.91bihu.com/api/CarInsurance/GetSubmitInfo";
		String result4 = sendGet(url4, param);
		System.err.println(result4);

            //获取出险信息
          /*  String param5 = "LicenseNo=" + carNO + "&Agent=" + agent + "&CustKey=" + custKey;
            String secCode5 = MD5(param5 + key);
            param5 = param5 + "&SecCode=" + secCode5.toLowerCase();
            String url5 = "http://i.91bihu.com/api/Claim/GetCreditInfo"; //出险信息
           // String url5 = "http://i.91bihu.com/api/CarInsurance/getreinfo"; //续保信息

            String result5 = sendGet(url5, param5);
            System.err.println(result5);*/

            System.err.println("用时：" + String.valueOf(System.currentTimeMillis() - start));
        }

        public static void main1(String[] args) throws Exception {

            String key = "csdfse784";
            //获取续保信息
//		String secCode = MD5("213520030193京P55M111100003820" + key);
//		System.err.println(secCode.toLowerCase());
//		String url = "http://m.91bihu.com/api/CommonInsuranceBusiness/GetReInsuranceInfo";
//		String param = "lastYearCompany=2&mobile=13520030193&useridentity=&carLicense=京P55M11&citycode=110000&agent=3820&secCode=" + secCode.toLowerCase();
//		String result = sendGet(url, param);
//		System.err.println(result);

            //报价(上传)
//		Map<String, String> map = new HashMap<String,String>();
//		map.put("registerDate", "2010-06-21");//注册日期
//		map.put("moldName", "比亚迪QCJ7150A6轿车");//品牌型号
//		map.put("engineNo", "4LA4D8297");//发动机号
//		map.put("carVin", "LGXC16DF4A0169664");//车架号
//		map.put("carLicense", "京P55M11");//车牌号
//		map.put("mobile", "13520030193");//手机号
//		map.put("intentionCompany", "0");//意向投保公司(-1:只报价、0:平安、1:太平洋、2:人保)
//		map.put("userIdentity", "");//用户标识(微信平台：openid；app平台：””)
//		map.put("carType", "0");//私家车/公用车（0：私家车；1：公用车）
//		map.put("isNewCar", "0");//是否新车（0：否；1：新车）
//		map.put("useType", "0");//营运/非营运（0：非营运；1：营运）
//		map.put("citycode", "110000");//城市Id（目前系统只支持北京地区）（北京:110000）
//		map.put("boli", "0");//玻璃单独破碎险，0-不投保，1国产，2进口
//		map.put("bujimianchesun", "0");//不计免赔险(车损) ，0-不投保，1投保
//		map.put("bujimiandaoqiang", "0");//不计免赔险(盗抢) ，0-不投保，1投保
//		map.put("bujimianfujia", "0");//不计免赔险(附加险) ，0-不投保，1投保
//		map.put("bujimianrenyuan", "0");//不计免赔险(车上人员) ，0-不投保，1投保
//		map.put("bujimiansanzhe", "0");//不计免赔险(三者) ，0-不投保，1投保
//		map.put("chedeng", "0");//倒车镜、车灯单独损坏险，0-不投保，1-国产，2-进口
//		map.put("sheshui", "0");//涉水行驶损失险，0-不投保，1投保
//		map.put("huahen", "0");//车身划痕损失险，0-不投保，>0投保(具体金额)（2，000；5，000；10，000；20，000）
//		map.put("siji", "10000.00");//车上人员责任险(司机) ，0-不投保，>0投保(具体金额）（10，000；20，000；30，000；40，000；50，000；100，000；200，000）
//		map.put("chengke", "10000");//车上人员责任险(乘客) ，0-不投保，>0投保(具体金额)（10，000；20，000；30，000；40，000；50，000；100，000；200，000）
//		map.put("chesun", "0");//机动车损失保险，0-不投保，>0投保
//		map.put("daoqiang", "0");//全车盗抢保险，0-不投保，>0投保
//		map.put("sanzhe", "50000");//第三者责任保险，0-不投保，>0投保(具体金额)（50，000；100，000；150，000；200，000；300，000；500，000；1，000，000；1，500，000）
//		map.put("ziran", "0");//自燃损失险，0-不投保，1投保
//		map.put("agent", "4820");
//		StringBuffer paramSb = new StringBuffer();
//		for(String mapKey : map.keySet()){
//			String value = map.get(mapKey);
//			paramSb.append(mapKey + "=" + value + "&");
//		}
//		String secCode = MD5("013520030193京P55M111100004820" + key);
//		System.err.println(secCode);
//		paramSb.append("secCode=" + secCode.toLowerCase());
//		String url = "http://m.91bihu.com/api/CommonInsuranceBusiness/GetPrecisePrice";
//		String result = sendGet(url, paramSb.toString());
//		System.err.println(result);

//		String key = "csdfse784";
//		//查询车辆车险信息
//		String url = "http://m.91bihu.com/api/CommonInsuranceBusiness/GetCreditInfoBy";
//		String secCode = MD5("京P55M113820" + key);
//		String param = "LicenseNo=京P55M11&agent=3820&secCode=" + secCode.toLowerCase();
//		String result = sendGet(url, param);
//		System.err.println(result);
//		System.err.println("用时：" + String.valueOf(System.currentTimeMillis() - start));
        }


        public final static String MD5(String pwd) {
            // 用于加密的字符
            char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
            try {
                // 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
                byte[] btInput = pwd.getBytes();

                // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
                MessageDigest mdInst = MessageDigest.getInstance("MD5");

                // MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
                mdInst.update(btInput);

                // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
                byte[] md = mdInst.digest();

                // 把密文转换成十六进制的字符串形式
                int j = md.length;
                char str[] = new char[j * 2];
                int k = 0;
                for (int i = 0; i < j; i++) { // i = 0
                    byte byte0 = md[i]; // 95
                    str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
                    str[k++] = md5String[byte0 & 0xf]; // F
                }

                // 返回经过加密后的字符串
                return new String(str);

            } catch (Exception e) {
                return null;
            }
        }

}
