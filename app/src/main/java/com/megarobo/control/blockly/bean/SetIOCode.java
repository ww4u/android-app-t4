package com.megarobo.control.blockly.bean;


import com.megarobo.control.utils.Utils;

public class SetIOCode extends Code{


    public SetIOCode(String code){
        setContent(code);
    }


    @Override
    public String execute() {
        return getContent();
    }

    @Override
    public boolean check(){
        String content = getContent();

        String firstStr = content.substring(content.indexOf("getdi(")+6,
                content.lastIndexOf(")=="));

        String secondStr = content.substring(content.indexOf("==[")+3,
                content.lastIndexOf(']'));
        return Utils.isCorrectIO(firstStr,secondStr);
    }

    @Override
    public String getRealCode() {
        String content = getContent();

        String firstStr = content.substring(content.indexOf("getdi(")+6,
                content.lastIndexOf(")=="));

        String[] fStrings = firstStr.split("X|x");
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < fStrings.length; i++) {
            if("".equals(fStrings[i].trim())) {
                continue;
            }
            strBuffer.append("'X");
            strBuffer.append(fStrings[i]);
            strBuffer.append("',");
        }
        String firstString  = strBuffer.substring(0, strBuffer.length()-1);


        String secondStr = content.substring(content.indexOf("==[")+3,
                content.lastIndexOf(']'));

        char[] a = secondStr.toCharArray();
        StringBuffer sBuffer = new StringBuffer();
        for(char c : a) {
            sBuffer.append(c);
            sBuffer.append(',');
        }
        String  secondString = sBuffer.substring(0, sBuffer.length()-1);

        return "waituntil( \"getdi("+firstString+")==["+secondString+"]\")";
    }
}
