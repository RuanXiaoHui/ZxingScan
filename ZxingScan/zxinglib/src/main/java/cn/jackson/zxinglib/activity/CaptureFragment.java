package cn.jackson.zxinglib.activity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import cn.jackson.zxinglib.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.io.IOException;
import java.util.Vector;
import cn.jackson.zxinglib.camera.CameraManager;
import cn.jackson.zxinglib.decoding.CaptureActivityHandler;
import cn.jackson.zxinglib.decoding.InactivityTimer;
import cn.jackson.zxinglib.utils.CodeUtils;
import cn.jackson.zxinglib.utils.DisplayUtil;
import cn.jackson.zxinglib.view.ViewfinderView;

/**
 * 自定义实现的扫描Fragment
 */
public class CaptureFragment extends Fragment implements SurfaceHolder.Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private CodeUtils.AnalyzeCallback analyzeCallback;
    private Camera camera;
    private static final long VIBRATE_DURATION = 200L;
    private int BOTTOM_HEIGHT;
    private int TOP_HEIGHT;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getActivity().getApplicationContext());
        hasSurface = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = null;
        if (bundle != null) {
            int layoutId = bundle.getInt(CodeUtils.LAYOUT_ID);
            if (layoutId != -1) {
                view = inflater.inflate(layoutId, null);
            }
        }
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_capture, null);
        }

        viewfinderView = (ViewfinderView) view.findViewById(R.id.viewfinder_view);

        surfaceView = (SurfaceView) view.findViewById(R.id.preview_view);
       if (BOTTOM_HEIGHT!=0&&BOTTOM_HEIGHT>0){
           viewfinderView.setMarginBottom(BOTTOM_HEIGHT);
       }
        if (TOP_HEIGHT!=0&&TOP_HEIGHT>0){
            viewfinderView.setMarginTop(TOP_HEIGHT);
        }
        surfaceHolder = surfaceView.getHolder();
        return view;
    }

    /***
     * 获取底部以及顶部的高度，进行适配
     * @param bottom
     * @param top
     */
   public void  setMarginViewHeight(View bottom,View top){
        if (bottom!=null){
             BOTTOM_HEIGHT= DisplayUtil.setViewHeight(bottom);
        }
        if (top!=null){
             TOP_HEIGHT= DisplayUtil.setViewHeight(top);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        StartCameraScanner();
    }

    public  void StartCameraScanner() {
        inactivityTimer = new InactivityTimer(this.getActivity());
        if (!hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        AudioManager audioService = (AudioManager) getActivity().getSystemService(getActivity().AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        CloseCameraDriver();
        CameraManager.get().closeDriver();
        if (inactivityTimer!=null){
            inactivityTimer.cleanActivity();
        }
        if (hasSurface) {
            surfaceClose();
        }
    }


    public void surfaceClose() {
        hasSurface = true;
        if (camera != null) {
            if (camera != null && CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewfinderView=null;
    }

    public  void CloseCameraDriver() {

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
            viewfinderView.ScanLightResetStop();
        }
        if (inactivityTimer!=null){
            inactivityTimer.cleanActivity();
            inactivityTimer.shutdown();
        }
    }

    /**
     * Handler scan result
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeFailed();
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeSuccess(barcode, result.getText());
            }
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            viewfinderView.ScanLightResetStart();
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            if (callBack != null) {
                callBack.callBack(e);
            }
            return;
        }
        if (callBack != null) {
            callBack.callBack(null);
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet, viewfinderView);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (hasSurface) {
            hasSurface = false;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceClose();
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public CodeUtils.AnalyzeCallback getAnalyzeCallback() {
        return analyzeCallback;
    }

    public void setAnalyzeCallback(CodeUtils.AnalyzeCallback analyzeCallback) {
        this.analyzeCallback = analyzeCallback;
    }

    @Nullable
    CameraInitCallBack callBack;

    /**
     * Set callback for Camera check whether Camera init success or not.
     */
    public void setCameraInitCallBack(CameraInitCallBack callBack) {
        this.callBack = callBack;
    }

    interface CameraInitCallBack {
        /**
         * Callback for Camera init result.
         * @param e If is's null,means success.otherwise Camera init failed with the Exception.
         */
        void callBack(Exception e);
    }
}
