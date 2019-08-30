package com.megarobo.control.blockly.bean;



public class WristCode extends Code{


    public WristCode(String code){
        setContent(code);
    }


    @Override
    public String execute() {
        return getContent();
    }

    @Override
    public boolean check() {
        return true;
    }

}
