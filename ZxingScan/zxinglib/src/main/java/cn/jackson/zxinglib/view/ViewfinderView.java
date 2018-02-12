/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.jackson.zxinglib.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import com.google.zxing.ResultPoint;
import java.util.Collection;
import java.util.HashSet;
import cn.jackson.zxinglib.R;
import cn.jackson.zxinglib.camera.CameraManager;
import cn.jackson.zxinglib.utils.DisplayUtil;

/**
 * 自定义组件实现,扫描功能
 */
public final class ViewfinderView extends View {

    //隔离多少秒进行刷新一下界面
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;
    private final Paint paint;
    //界面的背景
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    //星星闪闪
    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    //线的开始于结束
    public static int scannerStart = 0;
    public static int scannerEnd = 0;
    //绘制扫描线的画笔
    private Paint linePaint;
    //扫描线颜色
    private  int laserColor;
    //扫描线高度
    private  int SCANNER_LINE_HEIGHT ;
    //扫描线移动距离
    private  int   SCANNER_LINE_MOVE_DISTANCE;
    // 是否展示小圆点
    private boolean isCircle;
    // 扫描框边角颜色
    private int innercornercolor;
    // 扫描框边角长度
    private int innercornerlength;
    // 扫描框边角宽度
    private int innercornerwidth;
    //扫描线是否显示
    private boolean lineIsShow;
    //扫描线渐变线
    private RadialGradient radialGradient;

    //绘制扫描线外部的矩形
    private RectF rectF;

    private int screenWidthPx;

    //扫描区域提示文本
    private  String labelText;
    //扫描区域提示文本颜色
    private  int labelTextColor;
    //提示文本的size
    private  float labelTextSize;

    //设置文字画笔的颜色
    private TextPaint labTextPaint;

    //设置文本可以进行换行
    private StaticLayout mTipTextSl;

    //是否旋转了屏幕,外面进行调用控制
    public boolean isStart=true;
    //是否显示文案
    private boolean isShowText;

    //是否显示扫描线，点击开始暂停时候进行使用的
    private boolean isScanLineShow=true;


    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        linePaint=new Paint();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidthPx = dm.widthPixels;
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<>(5);
        linePaint.setAntiAlias(true);
        paint.setAntiAlias(true);
        linePaint.setAlpha(0xFF);

        rectF=new RectF();
        labTextPaint = new TextPaint();
        labTextPaint.setAntiAlias(true);
        initInnerRect(context,attrs);
    }

    /**
     * 初始化内部框的大小
     * @param context
     * @param attrs
     */
    private  void initInnerRect(Context context,AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);

        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.ViewfinderView_inner_margintop, -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGINTOP = (int) innerMarginTop;
        }

        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = (int) ta.getDimension(R.styleable.ViewfinderView_inner_width,
                screenWidthPx/ 2);

        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = (int) ta.getDimension(R.styleable.ViewfinderView_inner_height,
                screenWidthPx/ 2);


        // 扫描框边角颜色
        innercornercolor = ta.getColor(R.styleable.ViewfinderView_inner_corner_color, Color.parseColor("#45DDDD"));

        // 扫描框边角长度
        innercornerlength = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_length, 65);

        // 扫描框边角宽度
        innercornerwidth = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_width, 15);

        //扫描线的颜色
        laserColor = ta.getColor(R.styleable.ViewfinderView_inner_scan_line_color, 0x00FF00);

        //扫描线是否显示
        lineIsShow=ta.getBoolean(R.styleable.ViewfinderView_inner_scan_line_isShow,false);

        // 扫描速度
        SCANNER_LINE_MOVE_DISTANCE = (int)ta.getDimension(R.styleable.ViewfinderView_inner_scan_speed, 3);

        //是否展示小圆点
        isCircle = ta.getBoolean(R.styleable.ViewfinderView_inner_scan_iscircle, true);

        //扫描线的高度(建议不要超过6)
        SCANNER_LINE_HEIGHT=(int)ta.getDimension(R.styleable.ViewfinderView_inner_scan_line_height,3);

        //设置绘制文本的颜色
        labelTextColor=ta.getColor(R.styleable.ViewfinderView_label_text_color,0xFF0000);

        //设置扫描的文本
        labelText=ta.getString(R.styleable.ViewfinderView_label_text);
        if(labelText==null|| TextUtils.isEmpty(labelText)){
            isShowText=false;
        }else{
            isShowText=true;
        }

        //设置文本的大小
        labelTextSize= ta.getDimension(R.styleable.ViewfinderView_label_text_size,8);
        labTextPaint.setTextSize(labelTextSize);
        labTextPaint.setColor(labelTextColor);
        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        if(scannerStart == 0 || scannerEnd == 0||isStart) {
            scannerStart = frame.top;
            scannerEnd = frame.bottom;
            isStart=false;
        }
           int  width = canvas.getWidth();
           int  height = canvas.getHeight();

        // 绘制边框外部的内容
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        //是否绘制文本
        if (isShowText){
            drawTextInfo(canvas,frame);
        }

        if (resultBitmap != null) {
            // 在扫描矩形上绘制不透明的结果位图
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
             drawFrameBounds(canvas, frame);
            if (lineIsShow&&isScanLineShow){
                drawLaserScanner(canvas,frame);
            }else{
                if (lineIsShow) {
                    canvas.drawOval(rectF, linePaint);
                }
            }
            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentPossible) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                    }
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentLast) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                    }
                }
            }
            if (isScanLineShow){
                postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
            }
        }
    }
    /**
     * 绘制移动扫描线
     * @param canvas
     * @param frame
     */
    private void drawLaserScanner(Canvas canvas, Rect frame) {
         linePaint.setColor(laserColor);
         radialGradient = new RadialGradient(
                (float)(frame.left + frame.width() / 2),
                (float)(scannerStart + SCANNER_LINE_HEIGHT / 2),
                360f,
                laserColor,
                shadeColor(laserColor),
                Shader.TileMode.MIRROR);
        linePaint.setShader(radialGradient);
        if(scannerStart < scannerEnd) {
            rectF.set(frame.left + 2 * SCANNER_LINE_HEIGHT,
                    scannerStart, frame.right - 2 * SCANNER_LINE_HEIGHT,
                    scannerStart + SCANNER_LINE_HEIGHT);
            canvas.drawOval(rectF, linePaint);
            scannerStart += SCANNER_LINE_MOVE_DISTANCE;
        } else {
            scannerStart = frame.top;
        }
        linePaint.setShader(null);
        radialGradient=null;
    }

    /****
     * 处理颜色模糊
     * @param color
     * @return
     */
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "20"+hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    /**
     * 绘制取景框边框
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        paint.setColor(innercornercolor);
        paint.setStyle(Paint.Style.FILL);

        int corWidth = innercornerwidth;
        int corLength = innercornerlength;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, paint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, paint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, paint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, paint);
    }

    //绘制文本
    private void drawTextInfo(Canvas canvas, Rect frame) {
        paint.setColor(labelTextColor);
        paint.setTextSize(labelTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.save();
        mTipTextSl = new StaticLayout(labelText, labTextPaint, DisplayUtil.getScreenResolution(getContext()).x,
                Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
        canvas.translate(0,
                frame.bottom +mTipTextSl.getHeight());
        mTipTextSl.draw(canvas);
        canvas.restore();
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    /****
     * 获取底部高度
     * @param marginBottom
     */
    public void setMarginBottom(int marginBottom) {
        CameraManager.MARGINBOTTOM=marginBottom;
        invalidate();
    }
    /****
     * 获取顶部高度
     * @param marginTopHeight
     */
    public void setMarginTop(int marginTopHeight) {
        CameraManager.MARGINTOP=marginTopHeight;
        invalidate();
    }

    /***
     * 隐藏扫描线
     */
    public void ScanLightResetStop(){
        isScanLineShow=false;
    }
    /****
     * 显示扫描线
     */
    public void ScanLightResetStart(){
        isScanLineShow=true;
    }

}
