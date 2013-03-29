package com.example.screenorientation;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import com.example.screenorientation.TopViewButton.OnTopViewButtonClickListener;
import com.example.screenorientation.TopViewButton.OnTopViewButtonDragListener;

public class TopViewService extends Service
{
    private static final int STATE_AUTO = 0;
    private static final int STATE_DISALBE = 1;
    private static final int STATE_FORCE_PORTRAIT = 2;
    private static final int STATE_FORCE_LANDSCAPE = 3;
    
    private int mState;
    
    private TopViewButton mLauncherButton;
    
    private TopViewButton mPortraitButton;
    private TopViewButton mLandscapeButton;
    private TopViewButton mAutoRotateButton;
    private TopViewButton mDisableButton;
    
    private LinearLayout mForceRotateOverlay;
    
    private WindowManager mWindowManager;
    
    private boolean mIsMenuShown = false;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mWindowManager = (WindowManager)TopViewService.this.getSystemService(Service.WINDOW_SERVICE);
        
        mForceRotateOverlay = new LinearLayout(TopViewService.this);
        mForceRotateOverlay.setClickable(false);
        mForceRotateOverlay.setFocusable(false);
        mForceRotateOverlay.setFocusableInTouchMode(false);
        mForceRotateOverlay.setLongClickable(false);

        WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);

        mWindowManager.addView(mForceRotateOverlay, param);
        mForceRotateOverlay.setVisibility(View.GONE);
        
        mLauncherButton = new TopViewButton(this);
        mLauncherButton.setText(R.string.menu);
        //mLauncherButton.setLayoutGravity(Gravity.LEFT | Gravity.BOTTOM);
        mLauncherButton.setWidth(this.getResources().getDimension(R.dimen.button_launcher_width));
        mLauncherButton.setHeight(this.getResources().getDimension(R.dimen.button_launcher_height));
        mLauncherButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
        //mLauncherButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView/*View v*/)
            {
                mIsMenuShown = !mIsMenuShown;
                if (mIsMenuShown) {
                    showMenu(true);
                } else {
                    showMenu(false);
                }
            }
        });
        
        mLauncherButton.setOnTopViewButtonDragListener(new OnTopViewButtonDragListener()
        {
            @Override
            public void onDrag(TopViewButton buttonView, MotionEvent e)
            {
                showMenu(false);
            }
            
        });
        
        mLauncherButton.show();
        
        startForeground(4321, new Notification());
    }
    
    private void showMenu(boolean isShown)
    {
        mIsMenuShown = isShown;
        if (mLandscapeButton == null) {
            mLandscapeButton = new TopViewButton(this);
            mLandscapeButton.setText(R.string.landscape);
            //mLandscapeButton.setLayoutGravity(Gravity.LEFT | Gravity.BOTTOM);
            mLandscapeButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
            mLandscapeButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
            mLandscapeButton.setTopButtonX(0);
            mLandscapeButton.setTopButtonY((int)(this.getResources().getDimension(R.dimen.button_launcher_height)));
            mLandscapeButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
            {
                @Override
                public void onClick(TopViewButton buttonView)
                {
                    WindowManager.LayoutParams params = (LayoutParams)mForceRotateOverlay.getLayoutParams();
                    params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    mWindowManager.updateViewLayout(mForceRotateOverlay, params);
                    mForceRotateOverlay.setVisibility(View.VISIBLE);
                    mState = STATE_FORCE_LANDSCAPE;
                    mLauncherButton.setText(R.string.landscape);
                    mIsMenuShown = false;
                    showMenu(mIsMenuShown);
                }
            });
        }
        
        if (mPortraitButton == null) {
            mPortraitButton = new TopViewButton(this);
            mPortraitButton.setText(R.string.portrait);
            //mPortraitButton.setLayoutGravity(Gravity.LEFT | Gravity.BOTTOM);
            mPortraitButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
            mPortraitButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
            mPortraitButton.setTopButtonX(0);
            mPortraitButton.setTopButtonY((int)(this.getResources().getDimension(R.dimen.button_launcher_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height)));
            mPortraitButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
            {
                @Override
                public void onClick(TopViewButton buttonView)
                {
                    WindowManager.LayoutParams params = (LayoutParams)mForceRotateOverlay.getLayoutParams();
                    params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    mWindowManager.updateViewLayout(mForceRotateOverlay, params);
                    mForceRotateOverlay.setVisibility(View.VISIBLE);
                    mState = STATE_FORCE_PORTRAIT;
                    mLauncherButton.setText(R.string.portrait);
                    mIsMenuShown = false;
                    showMenu(mIsMenuShown);
                }
            });
        }
        
        if (mDisableButton == null) {
            mDisableButton = new TopViewButton(this);
            mDisableButton.setText(R.string.disable);
            //mDisableButton.setLayoutGravity(Gravity.LEFT | Gravity.BOTTOM);
            mDisableButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
            mDisableButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
            mDisableButton.setTopButtonX(0);
            mDisableButton.setTopButtonY((int)(this.getResources().getDimension(R.dimen.button_launcher_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height)));
            mDisableButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
            {
                @Override
                public void onClick(TopViewButton buttonView)
                {
                    mForceRotateOverlay.setVisibility(View.GONE);
                    setAutoOrientationEnabled(getContentResolver(), false);
                    Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    mState = STATE_DISALBE;
                    mLauncherButton.setText(R.string.disable);
                    mIsMenuShown = false;
                    showMenu(mIsMenuShown);
                }
            });
        }
        
        if (mAutoRotateButton == null) {
            mAutoRotateButton = new TopViewButton(this);
            mAutoRotateButton.setText(R.string.auto_rotate);
            //mAutoRotateButton.setLayoutGravity(Gravity.LEFT | Gravity.BOTTOM);
            mAutoRotateButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
            mAutoRotateButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
            mAutoRotateButton.setTopButtonX(0);
            mAutoRotateButton.setTopButtonY((int)(this.getResources().getDimension(R.dimen.button_launcher_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height) + 
                    this.getResources().getDimension(R.dimen.button_option_height)));
            mAutoRotateButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
            {
                @Override
                public void onClick(TopViewButton buttonView)
                {
                    mForceRotateOverlay.setVisibility(View.GONE);
                    setAutoOrientationEnabled(getContentResolver(), true);
                    mState = STATE_AUTO;
                    mLauncherButton.setText(R.string.auto_rotate);
                    mIsMenuShown = false;
                    showMenu(mIsMenuShown);
                }
            });
        }
        
        if (isShown) {
            mPortraitButton.show();
            mLandscapeButton.show();
            mDisableButton.show();
            mAutoRotateButton.show();
        } else {
            mPortraitButton.hide();
            mLandscapeButton.hide();
            mDisableButton.hide();
            mAutoRotateButton.hide();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    private void setAutoOrientationEnabled(ContentResolver resolver, boolean enabled)
    {
        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }
}
