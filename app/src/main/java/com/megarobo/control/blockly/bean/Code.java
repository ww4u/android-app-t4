package com.megarobo.control.blockly.bean;

/**
 * 所有blockly块对应的code基类
 */
public abstract class Code {

    private String blockId;

    private int blankCount;

    private String content;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public int getBlankCount() {
        return blankCount;
    }

    public void setBlankCount(int blankCount) {
        this.blankCount = blankCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public abstract String execute();

    public abstract boolean check();

    public String getRealCode(){
        return getContent();
    }

}
