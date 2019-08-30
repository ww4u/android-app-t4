package com.megarobo.control.blockly.bean;


import com.megarobo.control.utils.Utils;

public class GetIOCode extends Code{


    public GetIOCode(String code){
        setContent(code);
    }


    @Override
    public String execute() {
        return getContent();
    }

    @Override
    public boolean check(){
        String content = getContent();

        String firstStr = content.substring(content.indexOf('(')+1,
                content.lastIndexOf(','));

        String secondStr = content.substring(content.indexOf(',')+1,
                content.lastIndexOf(')'));
        return Utils.isCorrectOutput(firstStr,secondStr);
    }

    @Override
    public String getRealCode() {
        String content = getContent();

        String firstStr = content.substring(content.indexOf('(')+1,
                content.lastIndexOf(','));

        String[] fStrings = firstStr.split("Y|y");
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("(");
        for (int i = 0; i < fStrings.length; i++) {
            if("".equals(fStrings[i].trim())) {
                continue;
            }
            strBuffer.append("'Y");
            strBuffer.append(fStrings[i]);
            strBuffer.append("',");
        }
        String firstString  = strBuffer.substring(0, strBuffer.length()-1);
        firstString += ")";


        String secondStr = content.substring(content.indexOf(',')+1,
                content.lastIndexOf(')'));

        char[] a = secondStr.toCharArray();
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("(");
        for(char c : a) {
            sBuffer.append(c);
            sBuffer.append(',');
        }
        String  secondString = sBuffer.substring(0, sBuffer.length()-1);
        secondString += ")";

        return"syncdo("+firstString+","+secondString+")";
    }

}
