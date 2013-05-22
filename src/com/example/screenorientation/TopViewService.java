package com.example.screenorientation;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.example.screenorientation.TopViewButton.OnTopViewButtonClickListener;

public class TopViewService extends Service
{
    private static final String TAG = TopViewService.class.getSimpleName();
    private TopViewButton mMainButton;
    
    private WindowManager mWindowManager;
    
    //private TopViewButton mPortraitButton;
    //private TopViewButton mLandscapeButton;
    //private TopViewButton mAutoRotateButton;
    //private TopViewButton mDisableButton;
    
    //private ForceRotateManager mForceRotateManager;
    //private TopViewMenuGroup mMenuGroup;
    
    private int mAutoRotateState = 1;
    
    private static Handler mUiHandler= new Handler();
    
    private static final Uri AUTO_ROTATE_SETTING_URI = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION);
    
    private ContentObserver mAutoRotateObserver = new ContentObserver(mUiHandler)
    {
        @Override
        public void onChange(boolean selfChange)
        {
            try {
                mAutoRotateState = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (mAutoRotateState == 1) {
                mMainButton.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
            } else if (mAutoRotateState == 0) {
                mMainButton.setImageDrawable(getResources().getDrawable(R.drawable.lock));
            }
            super.onChange(selfChange);
        }
    };
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        //mForceRotateManager = ForceRotateManager.getInstance(this);
        
        mMainButton = new TopViewButton(this);
        //mMainButton.setText(R.string.menu);
        mMainButton.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
        mMainButton.setScaleType(ScaleType.FIT_CENTER);
        mMainButton.setBackground(getResources().getDrawable(R.drawable.button_background));
        
        mMainButton.setWidth(this.getResources().getDimension(R.dimen.button_launcher_width));
        mMainButton.setHeight(this.getResources().getDimension(R.dimen.button_launcher_height));
        
        try {
            mAutoRotateState = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (mAutoRotateState == 1) {
            mMainButton.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
        } else if (mAutoRotateState == 0) {
            mMainButton.setImageDrawable(getResources().getDrawable(R.drawable.lock));
        }
        
        mMainButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mAutoRotateState = (mAutoRotateState + 1) % 2;
                if (mAutoRotateState == 0) {
                    mMainButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    setAutoOrientationEnabled(getContentResolver(), false);
                    int rotation = mWindowManager.getDefaultDisplay().getRotation();
                    Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, rotation);
                } else if (mAutoRotateState == 1) {
                    setAutoOrientationEnabled(getContentResolver(), true);
                }
            }
        });
        
        mMainButton.setDragEnabled(true);
        
        /*
        mMainButton.setOnTopViewButtonDragListener(new OnTopViewButtonDragListener()
        {
            @Override
            public void onDrag(TopViewButton buttonView, MotionEvent e)
            {
            }
        });
        */
        
        mMainButton.setOnConfigurationChangedListener(new TopViewButton.OnConfigurationChangedListener()
        {
            @Override
            public void onConfigurationChagned(Configuration newConfig)
            {
                Display display = mWindowManager.getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);
                
                WindowManager.LayoutParams mainParams = (LayoutParams)mMainButton.getLayoutParams();
                
                if (mainParams.x != 0) {
                    mainParams.x = displaySize.x - mainParams.width;
                }
                
                float ratio = (mainParams.y + mainParams.height) / (float)displaySize.x;
                mainParams.y = (int)Math.floor((ratio * displaySize.y) + 0.5f) - mainParams.height;
                if (mainParams.y  < getStatusBarHeight()) {
                    //mainParams.y = displaySize.y - mainParams.height;
                    mainParams.y  = getStatusBarHeight();
                }
                
                Log.d(TAG, "screen height: " + displaySize.y);
                Log.d(TAG, "y: " + (mainParams.y + mainParams.height));
                
                mWindowManager.updateViewLayout(mMainButton, mainParams);
            }
        });
        
        mMainButton.show();
        /*
        mLandscapeButton = new TopViewButton(this);
        mLandscapeButton.setText(R.string.landscape);
        mLandscapeButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mLandscapeButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        */
        
        /*
        mPortraitButton = new TopViewButton(this);
        mPortraitButton.setText(R.string.portrait);
        mPortraitButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mPortraitButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        */
        
        /*
        mDisableButton = new TopViewButton(this);
        mDisableButton.setText(R.string.disable);
        mDisableButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mDisableButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        mDisableButton.setBackground(getResources().getDrawable(R.drawable.lock));
        
        mAutoRotateButton = new TopViewButton(this);
        mAutoRotateButton.setText(R.string.auto_rotate);
        mAutoRotateButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mAutoRotateButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        mAutoRotateButton.setBackground(this.getResources().getDrawable(R.drawable.unlock));
        */
        //mMenuGroup = new TopViewMenuGroup(mLauncherButton);
        /*
        mMenuGroup.addSubMenu(mAutoRotateButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.reset();
                setAutoOrientationEnabled(getContentResolver(), true);
                mLauncherButton.setText(R.string.auto_rotate);
                mLauncherButton.setBackground(getResources().getDrawable(R.drawable.unlock));
            }
        });
        
        mMenuGroup.addSubMenu(mDisableButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.reset();
                setAutoOrientationEnabled(getContentResolver(), false);
                //Configuration config = TopViewService.this.getResources().getConfiguration();
                int rotation = mWindowManager.getDefaultDisplay().getRotation();
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, rotation);
                mLauncherButton.setText(R.string.disable);
                mLauncherButton.setBackground(getResources().getDrawable(R.drawable.lock));
            }
        });
        */
        /*
        mMenuGroup.addSubMenu(mPortraitButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_PORTRAIT);
                mLauncherButton.setText(R.string.portrait);
            }
        });
        
        mMenuGroup.addSubMenu(mLandscapeButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_LANDSCAPE);
                mLauncherButton.setText(R.string.landscape);
            }
        });
        */
        
        this.getContentResolver().registerContentObserver(AUTO_ROTATE_SETTING_URI, false, mAutoRotateObserver);
        
        startForeground(4321, new Notification());
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
    
    private int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = mMainButton.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mMainButton.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    @Override
    public void onDestroy()
    {
        this.getContentResolver().unregisterContentObserver(mAutoRotateObserver);
        super.onDestroy();
    }
}
