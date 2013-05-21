package com.example.screenorientation;

import java.util.ArrayList;

import android.app.Service;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.example.screenorientation.TopViewButton.OnTopViewButtonClickListener;
import com.example.screenorientation.TopViewButton.OnTopViewButtonDragListener;

public class TopViewMenuGroup
{
    private WindowManager mWindowManager;
    
    private TopViewButton mMainButton;
    private ArrayList<TopViewButton> mSubMenuButtons = new ArrayList<TopViewButton>();
    
    private boolean mIsMenuShown = false;
    private boolean mShouldUpdateMenuPosition = true;
    
    public interface OnSubMenuItemClickListener
    {
        public void onClick(TopViewButton buttonView);
    };
    
    public TopViewMenuGroup(TopViewButton mainButton)
    {
        mWindowManager = (WindowManager)mainButton.getContext().getSystemService(Service.WINDOW_SERVICE);
        
        mMainButton = mainButton;
        mMainButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                mIsMenuShown = !mIsMenuShown;
                Log.d("roger_tag", "show: " + mIsMenuShown);
                if (mIsMenuShown) {
                    showMenu(true);
                } else {
                    showMenu(false);
                }
            }
        });
        //mMainButton.setBackground(mMainButton.getResources().getDrawable(R.drawable.menu));
        
        mMainButton.setDragEnabled(true);
        
        mMainButton.setOnTopViewButtonDragListener(new OnTopViewButtonDragListener()
        {
            @Override
            public void onDrag(TopViewButton buttonView, MotionEvent e)
            {
                mShouldUpdateMenuPosition = true;
                if (mIsMenuShown) {
                    showMenu(false);
                }
            }
        });
        
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

                
                
                Log.d("roger_tag", "screen height: " + displaySize.y);
                Log.d("roger_tag", "y: " + (mainParams.y + mainParams.height));
                
                mWindowManager.updateViewLayout(mMainButton, mainParams);
                
                mShouldUpdateMenuPosition = true;
                if (mIsMenuShown) {
                    showMenu(false);
                }
            }
        });
        
        mMainButton.show();
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
    
    public void addSubMenu(TopViewButton subMenuButton, final OnSubMenuItemClickListener listener)
    {
        subMenuButton.setOnTopViewButtonClickListener(new OnTopViewButtonClickListener()
        {
            @Override
            public void onClick(TopViewButton buttonView)
            {
                listener.onClick(buttonView);
                showMenu(false);
            }
        });
        subMenuButton.setDragEnabled(false);
        mSubMenuButtons.add(subMenuButton);
    }
    
    private void showMenu(boolean isShowMenu)
    {
        mIsMenuShown = isShowMenu;
        
        if (isShowMenu && mShouldUpdateMenuPosition) {
            Point subMenuSize = new Point();
            for (TopViewButton button : mSubMenuButtons) {
                subMenuSize.x = Math.max(subMenuSize.x, button.getTopViewWidth());
                subMenuSize.y += button.getTopViewHeight();
            }
            
            Display display = mWindowManager.getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            
            WindowManager.LayoutParams mainParams = (LayoutParams)mMainButton.getLayoutParams();
            
            int originX;
            int originY;
            if (mainParams.x <= displaySize.x / 2f) {
                originX = mainParams.x;
            } else {
                originX = (mainParams.x + mainParams.width) - subMenuSize.x;
            }
            
            if (mainParams.y + mMainButton.getTopViewHeight() + subMenuSize.y < displaySize.y) {
                originY = mainParams.y + mMainButton.getTopViewHeight();
            } else {
                originY = mainParams.y - subMenuSize.y;
            }
            
            for (TopViewButton button : mSubMenuButtons) {
                button.moveTo(originX, originY);
                originY += button.getTopViewHeight();
            }
            
            mShouldUpdateMenuPosition = false;
        }
        
        if (isShowMenu) {
            for (TopViewButton button : mSubMenuButtons) {
                button.show();
            }
        } else {
            for (TopViewButton button : mSubMenuButtons) {
                button.hide();
            }
        }
    }
}
