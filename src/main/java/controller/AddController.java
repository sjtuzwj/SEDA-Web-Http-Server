package controller;

import framework.HttpType;

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
        return String.valueOf(Double.parseDouble(left)+Double.parseDouble(right));
    }
}
