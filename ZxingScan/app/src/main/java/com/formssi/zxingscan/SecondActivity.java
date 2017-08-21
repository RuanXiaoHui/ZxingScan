package com.formssi.zxingscan;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.formssi.zxinglib.activity.CaptureFragment;
import com.formssi.zxinglib.utils.CodeUtils;

/**
 * 定制化显示扫描界面
 */
public class SecondActivity extends BaseActivity implements View.OnClickListener {

    private CaptureFragment captureFragment;
    private RelativeLayout rltbottomBar;
    private RelativeLayout tlToolBar;
    private Button btnScanner;
    private Button btnStop;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        //测量底部以及bar布局的高度,现在不需要在这里面测量，已经放在了基类，只需传顶部bar以及顶部toolbar的View，默认为null也就是没有
        rltbottomBar= (RelativeLayout) findViewById(R.id.rltBar);
        tlToolBar= (RelativeLayout) findViewById(R.id.tlToolBar);

        captureFragment = new CaptureFragment();
        captureFragment.setMarginViewHeight(rltbottomBar,tlToolBar);
        // 为二维码扫描界面设置定制化界面my_camera
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
        initView();
    }

    private void initView() {
        btnScanner= (Button) findViewById(R.id.btnScanner);
        btnStop= (Button) findViewById(R.id.btnStop);
        ivBack= (ImageView) findViewById(R.id.ivBack);
        btnScanner.setEnabled(false);
        btnStop.setEnabled(true);
        btnScanner.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    /**
     * 二维码扫描解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {

            //如果你想一直扫描，那么你这个地方必须要close一下，然后在start一下
            //如果需要返回去必须要调用close，也就是不管怎么样都要调用close
            //实现一直扫的逻辑，就跟下面那个按钮的操作是一样的。按钮点击一下扫一下
            //如果不想一直扫，就result一下返回去
            captureFragment.CloseCameraDriver();

            Toast.makeText(getApplicationContext(), ""+result, Toast.LENGTH_SHORT).show();
            btnScanner.setEnabled(true);
            btnStop.setEnabled(false);
        }

        @Override
        public void onAnalyzeFailed() {
            captureFragment.CloseCameraDriver();
            Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnScanner:
                btnScanner.setEnabled(false);
                btnStop.setEnabled(true);
                captureFragment.StartCameraScanner();
                break;
            case R.id.btnStop:
                btnScanner.setEnabled(true);
                btnStop.setEnabled(false);
                captureFragment.CloseCameraDriver();
                break;
            case R.id.ivBack:
                finish();
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        analyzeCallback=null;
        captureFragment=null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        btnScanner.setEnabled(false);
        btnStop.setEnabled(true);
    }
}
