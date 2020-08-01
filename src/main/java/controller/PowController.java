package controller;

import framework.HttpType;

public class PowController {
    private PowController() {
    }
    private static class SingletonInner {
        private static PowController PowController = new PowController();
    }
    public static PowController getInstance() {
        return SingletonInner.PowController;
    }

    public String Pow(String base,String index){
        if(base ==null||index==null)
            throw new NullPointerException();
        return String.valueOf(Math.pow(Double.parseDouble(base),Double.parseDouble(index)));
    }
}
