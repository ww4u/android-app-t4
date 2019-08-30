package com.megarobo.control.blockly.bean;

import java.util.List;

public class ForCode extends Code{


    /**
     * 循环代码体
     */
    private List<Code> codeList;

    /**
     * 计数器
     */
    private int count;


    public List<Code> getCodeList() {
        return codeList;
    }

    public void setCodeList(List<Code> codeList) {
        this.codeList = codeList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ForCode(){

    }

    public ForCode(List<Code> codeList){
        this.codeList = codeList;
    }

    @Override
    public String execute() {
        StringBuffer sb = new StringBuffer();
        while (count >0){
            for (Code code:codeList) {
                sb.append(code.execute()+"\n");
            }
            count--;
        }
        return sb.toString();
    }

    @Override
    public boolean check() {
        return true;
    }

}
