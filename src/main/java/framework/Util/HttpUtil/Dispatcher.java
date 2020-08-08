package framework.Util.HttpUtil;

import controller.AddController;
import controller.PowController;
import framework.Util.HttpUtil.RequestType;
import framework.Util.HttpUtil.Response;
import framework.Util.HttpUtil.StaticPage;

import java.util.HashMap;

import static framework.Util.HttpUtil.ResponseType.HTML;
import static framework.Util.HttpUtil.ResponseType.JSON;

public class Dispatcher {
    public static Response dispatch(RequestType type, HashMap<String,String> params){
        if(type == RequestType.GET)
            try{
        switch(params.get("path")){
            case "/pow":
                return new Response(JSON,PowController.getInstance().Pow(params.get("base"),params.get("index")));
            case "/add":
                return new Response(JSON,AddController.getInstance().Add(params.get("left"),params.get("right")));
            case "/":
                return new Response(JSON,"{homepage:1}");
            default:
                return new Response(HTML, StaticPage.getInstance().get("404"));
        } }catch (NullPointerException e){
                System.out.println("Here");
                return new Response(HTML, StaticPage.getInstance().get("400"));
            }
        else
            return new Response(HTML, StaticPage.getInstance().get("405"));
    }
}
