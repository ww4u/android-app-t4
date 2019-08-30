package com.megarobo.control.blockly.bean;



public class HandCloseCode extends Code{


    public HandCloseCode(String code){
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
