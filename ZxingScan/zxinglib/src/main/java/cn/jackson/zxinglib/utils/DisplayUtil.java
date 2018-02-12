package cn.jackson.zxinglib.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class DisplayUtil{

    @SuppressWarnings("deprecation")
    public static Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(screenResolution);
        } else {
            screenResolution.set(display.getWidth(), display.getHeight());
        }
        return screenResolution;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /****
     * 测量包裹控件的大小，当设置底部布局，以及顶部ToolBar的时候进行调用
     * @param views
     * @return
     */
    public static int setViewHeight(View views) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        views.measure(spec, spec);
        int measuredWidthTicketNum = views.getMeasuredHeight();
        return measuredWidthTicketNum;
    }

}
