package com.example.screenorientation;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Surface;

import com.example.screenorientation.TopViewButton.OnTopViewButtonClickListener;
import com.example.screenorientation.TopViewButton.OnTopViewButtonDragListener;
import com.example.screenorientation.TopViewMenuGroup.OnSubMenuItemClickListener;

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
    
    private ForceRotateManager mForceRotateManager;
    private TopViewMenuGroup mMenuGroup;
    
    private boolean mIsMenuShown = false;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mForceRotateManager = ForceRotateManager.getInstance(this);
        
        mLauncherButton = new TopViewButton(this);
        mLauncherButton.setText(R.string.menu);
        mLauncherButton.setWidth(this.getResources().getDimension(R.dimen.button_launcher_width));
        mLauncherButton.setHeight(this.getResources().getDimension(R.dimen.button_launcher_height));
        
        mLandscapeButton = new TopViewButton(this);
        mLandscapeButton.setText(R.string.landscape);
        mLandscapeButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mLandscapeButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mPortraitButton = new TopViewButton(this);
        mPortraitButton.setText(R.string.portrait);
        mPortraitButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mPortraitButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mDisableButton = new TopViewButton(this);
        mDisableButton.setText(R.string.disable);
        mDisableButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mDisableButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mAutoRotateButton = new TopViewButton(this);
        mAutoRotateButton.setText(R.string.auto_rotate);
        mAutoRotateButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mAutoRotateButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mMenuGroup = new TopViewMenuGroup(mLauncherButton);
        mMenuGroup.addSubMenu(mLandscapeButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_LANDSCAPE);
                mLauncherButton.setText(R.string.landscape);
            }
        });
        
        mMenuGroup.addSubMenu(mPortraitButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_PORTRAIT);
                mLauncherButton.setText(R.string.portrait);
            }
        });
        
        mMenuGroup.addSubMenu(mDisableButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.reset();
                setAutoOrientationEnabled(getContentResolver(), false);
                //Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                mLauncherButton.setText(R.string.disable);
            }
        });
        
        mMenuGroup.addSubMenu(mAutoRotateButton, new OnSubMenuItemClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mForceRotateManager.reset();
                setAutoOrientationEnabled(getContentResolver(), true);
                mLauncherButton.setText(R.string.auto_rotate);
            }
        });
        
        startForeground(4321, new Notification());
    }
    
    private void showMenu(boolean isShown)
    {
        mIsMenuShown = isShown;
        if (mLandscapeButton == null) {
            mLandscapeButton = new TopViewButton(this);
            mLandscapeButton.setText(R.string.landscape);
            mLandscapeButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
            mLandscapeButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
            /*
            mLandscapeButton.setTopButtonX(0);
            mLandscapeButton.setTopButtonY((int)(this.getResources().getDimension(R.dimen.button_launcher_height)));
            mLandscapeButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
            {
                @Override
                public void onClick(TopViewButton buttonView)
                {
                    mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_LANDSCAPE);
                    mState = STATE_FORCE_LANDSCAPE;
                    mLauncherButton.setText(R.string.landscape);
                    mIsMenuShown = false;
                    showMenu(mIsMenuShown);
                }
            });
            */
        }
        
        if (mPortraitButton == null) {
            mPortraitButton = new TopViewButton(this);
            mPortraitButton.setText(R.string.portrait);
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
                    mForceRotateManager.rotateScreen(ForceRotateManager.SCREEN_PORTRAIT);
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
                    mForceRotateManager.reset();
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
                    mForceRotateManager.reset();
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
