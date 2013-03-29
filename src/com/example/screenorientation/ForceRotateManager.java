package com.example.screenorientation;

import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class ForceRotateManager
{
    public static int SCREEN_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static int SCREEN_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    
    private static ForceRotateManager mInstance;
    private WindowManager mWindowManager;
    
    private LinearLayout mForceRotateOverlay;
    WindowManager.LayoutParams mOverlayParams;
    
    public static ForceRotateManager getInstance(Context context)
    {
        if (mInstance == null) {
            mInstance = new ForceRotateManager(context);
        }
        return mInstance;
    }
    
    public void rotateScreen(final int orientation)
    {
        mOverlayParams.screenOrientation = orientation;
        mWindowManager.updateViewLayout(mForceRotateOverlay, mOverlayParams);
        mForceRotateOverlay.setVisibility(View.VISIBLE);
    }
    
    public void reset()
    {
        mForceRotateOverlay.setVisibility(View.GONE);
    }
    
    private ForceRotateManager(Context context)
    {
        mForceRotateOverlay = new LinearLayout(context);
        mForceRotateOverlay.setClickable(false);
        mForceRotateOverlay.setFocusable(false);
        mForceRotateOverlay.setFocusableInTouchMode(false);
        mForceRotateOverlay.setLongClickable(false);
        
        mOverlayParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);
        
        mWindowManager = (WindowManager)context.getSystemService(Service.WINDOW_SERVICE);
        mWindowManager.addView(mForceRotateOverlay, mOverlayParams);
        mForceRotateOverlay.setVisibility(View.GONE);
    }
}
