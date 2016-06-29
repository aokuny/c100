import com.ihandy.quote_common.httpUtil.HttpsUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Created by tapingyang01 on 16-6-28.
 */
public class RequestModel {

    String url;
    String type;
    String viewInstanceId;
    String xml;
    boolean isNeedTime;
    String rav;
    String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getRav() {
        return rav;
    }

    public void setRav(String rav) {
        this.rav = rav;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getViewInstanceId() {
        return viewInstanceId;
    }

    public void setViewInstanceId(String viewInstanceId) {
        this.viewInstanceId = viewInstanceId;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public boolean isNeedTime() {
        return isNeedTime;
    }

    public void setNeedTime(boolean needTime) {
        isNeedTime = needTime;
    }
    private  String genXmlParam(){
        StringBuffer sb = new StringBuffer();
        if(StringUtils.isNotBlank(this.type)){
            sb.append("__type=");
            sb.append(this.type);
        }
        if(StringUtils.isNotBlank(this.viewInstanceId)){
            sb.append("&__viewInstanceId=");
            sb.append(this.viewInstanceId);
        }
        if(StringUtils.isNotBlank(this.xml)){
            sb.append("&__xml=");
            sb.append(this.xml);
        }
        if(this.isNeedTime){
            sb.append("&"+System.currentTimeMillis());
        }
        return sb.toString();
    }
    public String sendRequest(String cookieValue,String returnType){
        String responseData = StringUtils.EMPTY ;
        if(StringUtils.isNotBlank(this.url)) {
            String paramsData;
            if(StringUtils.isNotBlank(this.rav)) {
                paramsData = this.rav;
            }else{
                paramsData = this.genXmlParam();
            }
            if(StringUtils.isBlank(returnType)){
                returnType = "html";
            }
            responseData = HttpsUtil.sendPost(this.url, paramsData, cookieValue, "UTF-8").get(returnType);
            System.out.println("responseData_"+this.type+"_"+this.title+":" + responseData);
        }
        return responseData;
    }
}
