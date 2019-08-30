package com.megarobo.control.blockly.bean;



public class HandOpenCode extends Code{


    public HandOpenCode(String code){
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
