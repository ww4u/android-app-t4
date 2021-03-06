package com.megarobo.control.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.megarobo.control.R;
import com.megarobo.control.utils.Utils;

public class RightControl extends View {

        private Paint paint;
        private int btnX;
        private int btnY;
        //内外圆半径
        private int radiusBack;
        private int radiusPre;
        //外圆圆心
        private float circleX;
        private float circleY;

        public RightControl(Context context) {
            this(context,null);
        }

        public RightControl(Context context, AttributeSet attrs) {
            this(context, attrs, 0 );
        }

        public RightControl(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.TRANSPARENT);
            Bitmap bitmap =BitmapFactory.decodeResource(getResources(), R.drawable.bg_control_nor);
            Bitmap bitmapCenter =BitmapFactory.decodeResource(getResources(), R.drawable.ico_anchor);
            Bitmap bitmapUp =BitmapFactory.decodeResource(getResources(), R.drawable.ico_direction_nor);

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float bmpWidth = (float) (bitmap.getWidth());
            float bmpHeight = (float) (bitmap.getHeight());
            float ratiow = width / bmpWidth;
            float ratioh = height / bmpHeight;
            float ratio = ratiow < ratioh ? ratiow : ratioh;
            float moveX = ((width-bmpWidth*ratio) / 2.0f);
            float moveY = ((height-bmpHeight*ratio) / 2.0f);
            canvas.translate(moveX, moveY);
            canvas.scale(ratio, ratio);
            canvas.drawBitmap(bitmap,0,0,null);
            canvas.drawBitmap(bitmapCenter,320,300,null);
            canvas.drawBitmap(bitmapUp,330,50,null);


            //设置背景图的半径
            radiusBack = getWidth() / 3;
            //设置小圆点的半径
            radiusPre = getWidth() / 8;
            //圆心
            circleX = getWidth() / 2;
            circleY = getHeight() / 2;

            if (paint == null) {
                paint = new Paint();
            }
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(0x7f111111);
//            canvas.drawCircle(circleX,circleY,(float) radiusBack,paint);
//            if (btnX <= 0 && btnY <= 0){
//                paint.setColor(0xFFF10159);
//                canvas.drawCircle(circleX,circleY,(float) radiusPre,paint);
//                canvas.save();
//                return;
//            }
//            paint.setColor(0xFF744041);
//            canvas.drawCircle(btnX,btnY,(float) radiusPre,paint);
            canvas.save();
        }

    public int getBtnX() {
        return btnX;
    }

    public void setBtnX(int btnX) {
        this.btnX = btnX;
    }

    public int getBtnY() {
        return btnY;
    }

    public void setBtnY(int btnY) {
        this.btnY = btnY;
    }

    public float getCircleX() {
        return circleX;
    }

    public void setCircleX(float circleX) {
        this.circleX = circleX;
    }

    public float getCircleY() {
        return circleY;
    }

    public void setCircleY(float circleY) {
        this.circleY = circleY;
    }

    /**
     * 如果设置了onTouchListener，必须onTouchListener onTouch方法返回false下面方法才能执行
     * @param event
     * @return
     */
    @Override
        public boolean onTouchEvent(MotionEvent event) {
//            switch (event.getAction()){
//                case MotionEvent.ACTION_MOVE:
//                    System.out.println(event.getX() + " " + event.getY());
//                    btnX = (int) event.getX();
//                    btnY = (int) event.getY();
//                    if ((btnY - circleY)*(btnY - circleY) + (btnX - circleX)*(btnX - circleX) >= radiusBack * radiusBack){
//                        changeBtnLocation();
//                    }
//                    this.invalidate();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    System.out.println(event.getX() + " " + event.getY() + "TAG");
//                    //发广播
//                    sendAction(btnX,btnY);
////                    btnX = 0;
////                    btnY = 0;
//                    this.invalidate();
//                    break;
//            }

            return true;
        }


    /**
     * 建立以圆心为原点的正向坐标系
     */
    public int getAngle(){
        float realX,realY;
        realX = btnX-circleX;
        realY = circleY-btnY;
        return Utils.getAngle(0,0,realX,realY);
    }

    /**
     * 这里会用到数学知识
     */
    public void changeBtnLocation() {
        //相似比
        double similarity = Math.sqrt((radiusBack * radiusBack) / ((btnY - circleY)*(btnY - circleY) + (btnX - circleX)*(btnX - circleX)));
        btnX = (int) ((btnX - circleX)*similarity + circleX);
        btnY = (int) ((btnY - circleY)*similarity + circleY);


    }

    public boolean isRevoke(){
        return (btnY - circleY)*(btnY - circleY) + (btnX - circleX)*(btnX - circleX) >= radiusBack * radiusBack;
    }

}
