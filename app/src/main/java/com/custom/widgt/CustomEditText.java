package com.custom.widgt;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class CustomEditText extends AppCompatEditText {
    
    private Drawable dRight;
    public Drawable getdRight() {
        return dRight;
    }
    public void setdRight(Drawable dRight) {
        this.dRight = dRight;
    }

    private Rect rBounds;
    private DrawableClickListener drawableClicklistener;

    public DrawableClickListener getDrawableClicklistener() {
        return drawableClicklistener;
    }
    public void setDrawableClicklistener(DrawableClickListener drawableClicklistener) {
        this.drawableClicklistener = drawableClicklistener;
    }
    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomEditText(Context context) {
        super(context);
    }

    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom)
    {
        if(right !=null)
        {
            dRight = right;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_UP && dRight!=null)
        {
            rBounds = dRight.getBounds();
            final int x = (int)event.getX();
            final int y = (int)event.getY();
            int w=this.getWidth();
            //System.out.println("x:/y: "+x+"/"+y);
            //System.out.println("bounds: "+bounds.left+"/"+bounds.right+"/"+bounds.top+"/"+bounds.bottom);
            //check to make sure the touch event was within the bounds of the drawable
            if(( x >= w-rBounds.width()-this.getPaddingRight()) && (x <= w) )
            {
                if(drawableClicklistener != null){
                    drawableClicklistener.onClick();
                }
                event.setAction(MotionEvent.ACTION_CANCEL);//use this to prevent the keyboard from coming up
            }
        }
        return super.onTouchEvent(event);
    }
    
    public interface DrawableClickListener{
        public void onClick();
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        dRight = null;
        rBounds = null;
        super.finalize();
    }
}
