package framework.Util.HttpUtil;

public class Response {
    public ResponseType responseType;
    public String content;
    public Response(ResponseType rt, String c){
        responseType = rt;
        content = c;
    }
}
