package com.megarobo.control.sample;

import android.os.Handler;
import android.os.SystemClock;

import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;

import com.megarobo.control.mqtt.MQTTCommandHelper;
import com.megarobo.control.mqtt.MQTTService;
import com.megarobo.control.sample.control.MySyntherizer;
import com.megarobo.control.utils.Cn2Spell;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.Utils;

public class MyTtsRecogListener extends MessageStatusRecogListener {
    private MySyntherizer mySyntherizer;

    public MyTtsRecogListener(Handler handler, MySyntherizer mySyntherizer) {
        super(handler);
        this.mySyntherizer = mySyntherizer;
    }

    // 识别成功
    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        reply(results[0]);
    }

    /**
     * 跳舞，向前走，向后走，左转，右转，向上，向下，回零位，恢复出厂设置，抓取，放开，停止
     * @param result
     */
    private void reply(String result) {
        //为空直接返回
        if(!Utils.isNotEmptyString(result)){
            return;
        }
        //获取首字母
        String headStr = Cn2Spell.getPinYinHeadChar(result);

        String pyStr = Cn2Spell.getPinYin(result);
        Logger.e("pystr",pyStr);

        mySyntherizer.setStereoVolume(1.0f,1.0f);

        if("xmxm".equals(headStr)){
            mySyntherizer.speak("你好，我在");
            return;
        }

        if(pyStr.contains("shangwu") || pyStr.contains("shangwudeshiyan") ||
                result.contains("上午") || result.contains("上午的实验")
                ){
            mySyntherizer.speak("上午的实验在10点半已经完成。");
            return;
        }

        if(pyStr.contains("jiexialai") || result.contains("接下来") || result.contains("三聚") || result.contains("分成")
                ){
            mySyntherizer.speak("好的，预计在20分钟内完成。");
            return;
        }


        /**
         * 主要控制识别
         */
        if("hl".equals(headStr) || "hn".equals(headStr) || "回零位".equals(result)
                || "hlw".equals(headStr) || "hnw".equals(headStr)){//"回零"
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().home());
            return;
        }

        if("tw".equals(headStr) || "跳舞".equals(result)){//"跳舞"
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().demo());
            return;
        }

        if(result.contains("持续向前") || "cxxq".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().frontContinue());
            return;
        }

        if(result.contains("前") || "xqz".equals(headStr) || pyStr.contains("qian")
                || "wq".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().front());
            return;
        }

        if(result.contains("持续向后") || "cxxh".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().backContinue());
            return;
        }

        if(result.contains("后") || "xhz".equals(headStr) || pyStr.contains("hou")
                || "wh".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().back());
            return;
        }

        if(result.contains("持续向左") || "cxxz".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().leftContinue());
            return;
        }

        if(result.contains("左") || "zz".equals(headStr) || pyStr.contains("zuo")
                || "wz".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().left());
            return;
        }

        if(result.contains("持续向右") || "cxxy".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().rightContinue());
            return;
        }

        if(result.contains("右") || "yz".equals(headStr) || pyStr.contains("you")
                || "wy".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().right());
            return;
        }

        if(result.contains("持续向上") || "cxxs".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().upContinue());
            return;
        }

        if(result.contains("上") || "xs".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().up());
            return;
        }

        if(result.contains("持续向下") || "cxxx".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().downContinue());
            return;
        }

        if(result.contains("下") || "xx".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().down());
            return;
        }

        if(result.contains("出厂设置") || "hfccsz".equals(headStr)
                || "ccsz".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().fold());
            return;
        }

        if(result.contains("抓取") || "zq".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().close());
            return;
        }

        if(result.contains("放开") || "fk".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().open());
            return;
        }

        if(result.contains("停止") || "tz".equals(headStr)){
            mySyntherizer.speak("好的");
            SystemClock.sleep(100);
            MQTTService.publish(MQTTCommandHelper.getInstance().stop());
            return;
        }

        mySyntherizer.speak("对不起，我没有听清，你能再说一遍吗？");


    }

    public void finish(){
        mySyntherizer.speak("完毕");
    }


}
