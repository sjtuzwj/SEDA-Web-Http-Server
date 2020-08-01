package framework;

import controller.AddController;
import controller.PowController;

import java.util.HashMap;

public class Dispatcher {
    public static String dispatch(HttpType type, HashMap<String,String> params){
        if(type == HttpType.GET)
            try{
        switch(params.get("path")){
            case "/pow":
                return PowController.getInstance().Pow(params.get("base"),params.get("index"));
            case "/add":
                return AddController.getInstance().Add(params.get("left"),params.get("right"));
            default:
                return "path not supported";
        } }catch (NullPointerException e){
                return "Parameter not found";
            }
        else
            return "operation not supported";
    }
}
