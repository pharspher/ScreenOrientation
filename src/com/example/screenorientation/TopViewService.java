package com.example.screenorientation;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;
import android.view.WindowManager;

import com.example.screenorientation.TopViewMenuGroup.OnSubMenuItemClickListener;

public class TopViewService extends Service
{
    private TopViewButton mLauncherButton;
    
    private WindowManager mWindowManager;
    
    //private TopViewButton mPortraitButton;
    //private TopViewButton mLandscapeButton;
    private TopViewButton mAutoRotateButton;
    private TopViewButton mDisableButton;
    
    private ForceRotateManager mForceRotateManager;
    private TopViewMenuGroup mMenuGroup;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        mForceRotateManager = ForceRotateManager.getInstance(this);
        
        mLauncherButton = new TopViewButton(this);
        mLauncherButton.setText(R.string.menu);
        mLauncherButton.setWidth(this.getResources().getDimension(R.dimen.button_launcher_width));
        mLauncherButton.setHeight(this.getResources().getDimension(R.dimen.button_launcher_height));
        
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
        
        mDisableButton = new TopViewButton(this);
        mDisableButton.setText(R.string.disable);
        mDisableButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mDisableButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mAutoRotateButton = new TopViewButton(this);
        mAutoRotateButton.setText(R.string.auto_rotate);
        mAutoRotateButton.setWidth(this.getResources().getDimension(R.dimen.button_option_width));
        mAutoRotateButton.setHeight(this.getResources().getDimension(R.dimen.button_option_height));
        
        mMenuGroup = new TopViewMenuGroup(mLauncherButton);
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
            }
        });
        
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
}
