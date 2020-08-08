package controller;

import com.alibaba.fastjson.JSON;

public class AddController {
    private AddController() {
    }
    private static class SingletonInner {
        private static AddController AddController = new AddController();
    }
    public static AddController getInstance() {  return SingletonInner.AddController;}

    public String Add(String left,String right){
        if(left==null||right==null)
            throw new NullPointerException();
        return JSON.toJSONString(String.valueOf(Double.parseDouble(left)+Double.parseDouble(right)));
    }
}
