package com.megarobo.control.blockly.bean;



public class SleepCode extends Code{


    public SleepCode(String code){
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
