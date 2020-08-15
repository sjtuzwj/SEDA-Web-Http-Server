package framework.Util.HttpUtil;

import java.util.HashMap;

public class RestfulParser {
    public static HashMap<String,String> parse(String url){
        HashMap<String,String> arg= new HashMap<>();
        String params[] = url.split("\\?");
        arg.put("path",params[0]);
        if(params.length>1){
            params = params[1].split("&");
            for(int i = 0; i< params.length; i++){
                String kv[] = params[i].split("=");
                arg.put(kv[0],kv[1]);
            }
        }
        return arg;
    }
}
