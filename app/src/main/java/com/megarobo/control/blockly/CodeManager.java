package com.megarobo.control.blockly;

import com.megarobo.control.blockly.bean.Code;
import com.megarobo.control.blockly.bean.ForCode;
import com.megarobo.control.blockly.bean.GetIOCode;
import com.megarobo.control.blockly.bean.HandCloseCode;
import com.megarobo.control.blockly.bean.HandOpenCode;
import com.megarobo.control.blockly.bean.MoveCode;
import com.megarobo.control.blockly.bean.SetIOCode;
import com.megarobo.control.blockly.bean.SleepCode;
import com.megarobo.control.blockly.bean.WristCode;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * blockly生成Python代码的管理类
 *
 * 1.包括初始化（Code String转化）
 *
 * 2.播放，生成robo端执行的python代码
 *
 * 3.单步执行，按顺序呢返回生成代码
 *
 */
public class CodeManager {

    private String code;
    private HashMap<Integer,Code> codeBeanHashMap;

    private List<Code> codeList;

    BlockingQueue<Code> queue = new LinkedBlockingQueue<>(10000);


    public CodeManager(String code){
        this.code = code;
    }

    /**
     * 对code进行处理，得到一个包含blockly ID，代码行号，代码的对象
     */
    public void init(){
        codeBeanHashMap = new HashMap<>();
        processCode();
    }

    /**
     * 对code进行处理，生成执行列表
     */
    private void processCode(){
        //因为code的格式是按照"\n"生成的，生产方式是自己定义的，在python_compressed.js中可以查看
        String codeStr[] = code.split("\n");
        //对每一行code进行处理

        codeList = new ArrayList<>();

        for(String subCode : codeStr){
            Logger.e("....",subCode+" blank"+countblanks(subCode));
            int blankCount = countblanks(subCode);
            if(Utils.isNotEmptyString(subCode) && subCode.contains("pass")){
                continue;
            }
            //得到blockId，blockId是一个由32位的字符组成的字符串
            String blockId = subCode.substring(subCode.lastIndexOf(')')+1,
                    subCode.lastIndexOf(')')+37);
            Code code = null;

            if(subCode.trim().startsWith("for")){ //forCode,解析成forCode
                code = new ForCode();
                int count = Integer.parseInt(subCode.substring(subCode.lastIndexOf('(')+1,
                        subCode.lastIndexOf(')')));
                ((ForCode) code).setCount(count);
            }
            if(subCode.trim().startsWith("move")){ //moveCode，解析成moveCode
                code = new MoveCode(subCode);
            }
            if(subCode.trim().startsWith("pinch(0)")){ //handCode,解析成handCode
                code = new HandOpenCode(subCode);
            }
            if(subCode.trim().startsWith("pinch") && !subCode.trim().startsWith("pinch(0)")){ //handCode,解析成handCode
                code = new HandCloseCode(subCode);
            }

            if(subCode.trim().startsWith("wrist")){ //wristCode，解析成wristCode
                code = new WristCode(subCode);
            }

            if(subCode.trim().startsWith("sleep")){ //
                code = new SleepCode(subCode);
            }

            if(subCode.trim().startsWith("waituntil")){ //
                code = new SetIOCode(subCode);
            }

            if(subCode.trim().startsWith("syncdo")){ //
                code = new GetIOCode(subCode);
            }

            if(code != null){
                //设置空格数,blockId
                code.setBlankCount(blankCount);
                code.setBlockId(blockId);
                code.setContent(subCode.replace(blockId,""));
                codeList.add(code);
            }
        }
        execute(codeList);
        Logger.e("queue",queue.size()+"");
    }


    private void execute(List<Code> codeList){

        for(int i=0;i<codeList.size();i++){

            Code code = codeList.get(i);
            if(code instanceof ForCode){

                ForCode forCode = (ForCode) code;
                //考虑到单步执行不会很多次，如果有超过100次的，变成100次
                int times = forCode.getCount()>100? 100 : forCode.getCount();
                if(times < 1){
                    return;
                }
                for(int j=0;j<times-1;j++){
                    List<Code> forBlock = getSubCodeList(
                            codeList.subList(i+1,codeList.size()),forCode);

                    execute(forBlock);
                }

            }else {
                Logger.e("content",code.getContent()+"");
                queue.add(code);
            }

        }

    }


    private List<Code> getSubCodeList(List<Code> codeList, Code code) {
        List<Code> subCodeList = new ArrayList<>();

        int i = 0;
        while(i<codeList.size() && codeList.get(i).getBlankCount() > code.getBlankCount()){
            subCodeList.add(codeList.get(i));
            if(codeList.get(i).getBlankCount() == code.getBlankCount()){
                return subCodeList;
            }
            i++;
        }
        return subCodeList;
    }

    public static int countblanks(String s){
        int i = 0 ;
        int count = 0;
        while(i < s.length() && s.charAt(i) == ' '){
            if(s.charAt(i) == ' '){
                count++;
            }
            i++;
        }
        return count;
    }

    /**
     *
     * @return 所有执行python代码
     */
    public String execute(){
        return "";
    }

    /**
     * 返回下一句代码
     */
    public Code step(){

        return queue.poll();
    }


    public String playAll() {
        for(Code subCode : codeList){
            if (subCode instanceof SetIOCode || subCode instanceof GetIOCode) {
                code = code.replace(subCode.getContent(),subCode.getRealCode());
            }
            code = code.replace(subCode.getBlockId().trim(),"");

        }
        return code;
    }

    public Code check(){
        for(Code subCode : codeList){
            if(subCode.check())
                continue;
            else
                return subCode;
        }
        return null;
    }
}
