package com.megarobo.control.blockly.bean;



public class MoveCode extends Code{


    public MoveCode(String code){
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
