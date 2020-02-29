package com.jimes.lottary_lib.scratch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.jimes.lottary_lib.R;
import com.jimes.lottary_lib.utils.SizeUtils;

/**
 * 刮刮卡的效果
 */
public class ScratchView extends View {

    private Paint mOutterPain = null;

    private Path mPath;//记录用户滑动的轨迹

    private Canvas mCanvas;//画布

    private Bitmap mBitmap;//图片

    private int mLastX;

    private int mLastY;

    private float strokeWidth = 30f;//手势宽度
    private int textColor = Color.BLACK;//字体颜色

    private int foreColor;//阴影颜色

    private String mText;//文字

    private float textSize;//文字大小
    private Paint mBackPaint;
    //判断只改遮盖层区域是否消除达到阈值
    private volatile boolean mComplete = false;

    /**
     * 刮刮卡刮完调用的接口
     *  
     */
    public interface OnGuaGuaComplete {
        void complete();
    }

    private OnGuaGuaComplete mListener;

    /**
     * 设置监听时间处理
     *
     * @param mListener
     */
    public void setOnGuaGuaCompleteListener(OnGuaGuaComplete mListener) {
        this.mListener = mListener;
    }

    /**
     * 记录刮奖信息文本的宽和高
     */
    private Rect mTextBound;

    public ScratchView(Context context) {
        this(context, null);
    }


    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public ScratchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
// TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取页面的宽和高
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        //初始化我们的bitMap
        mBitmap = Bitmap.createBitmap(width > 0 ? width : 100, height > 0 ? height : 100, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        setOutPaint();
        setUpBackPaint();
        mCanvas.drawColor(foreColor);


    }

    /**
     * 设置我们绘制获奖信息的画笔属性
     *  
     */
    private void setUpBackPaint() {
        mBackPaint.setColor(textColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(textSize);
//获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    /**
     * 进行一些初始化操作
     */
    private void init(Context context, AttributeSet attrs) {
        if (null == attrs) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView);

        try {
            mText = typedArray.getString(R.styleable.ScratchView_text);
            textSize = typedArray.getDimension(R.styleable.ScratchView_textSize, SizeUtils.sp2px(context, 12));
            textColor = typedArray.getColor(R.styleable.ScratchView_textColor, Color.BLACK);
            strokeWidth = typedArray.getDimension(R.styleable.ScratchView_strokeWidth, SizeUtils.dip2px(context, 10));
            foreColor = typedArray.getColor(R.styleable.ScratchView_foreColor, Color.GRAY);
        } finally {
            typedArray.recycle();
        }

        mOutterPain = new Paint();
        mPath = new Path();

        mTextBound = new Rect();
        mBackPaint = new Paint();
    }

    /**
     * 设置绘制path画笔的一些属性
     */
    private void setOutPaint() {
        mOutterPain.setColor(Color.YELLOW);
        mOutterPain.setAntiAlias(true);
        mOutterPain.setDither(true);
        mOutterPain.setStrokeJoin(Paint.Join.ROUND);
        mOutterPain.setStrokeCap(Paint.Cap.ROUND);
        mOutterPain.setStyle(Paint.Style.STROKE);
        mOutterPain.setStrokeWidth(strokeWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN://按下时绘制路径
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);

                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);
                if (dx > 3 || dy > 3) {//移动小于3像素的时候不做改变
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
            default:
                break;
        }

        invalidate();

        return true;
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            float wipeArea = 0;
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];
            //获得Bitmap上所有的像素信息
            mBitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.e("zx", percent + "");
                if (percent > 50) {
                    mComplete = true;
                    postInvalidate();
                }
            }
        }
    };

    @Override//图形绘制
    protected void onDraw(Canvas canvas) {
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2, getHeight() / 2 + mTextBound.height() / 2, mBackPaint);
        if (mComplete) {
            if (mListener != null) {
                mListener.complete();
            }
        }
        if (!mComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }


    private void drawPath() {
        mOutterPain.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPain);
    }
}
