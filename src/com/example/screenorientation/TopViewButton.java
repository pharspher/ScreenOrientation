package com.example.screenorientation;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

public class TopViewButton extends ImageButton
{
    private static final String TAG = "TopViewButton";
    
    private OnTopViewButtonClickListener mOnClickListener;
    private OnTopViewButtonDragListener mOnDragListener;
    
    private boolean mIsPressedDown = false;
    private boolean mIsDragging = false;
    
    private WindowManager.LayoutParams mWindowParams;
    
    private Context mContext;
    private boolean mIsAttached = false;
    private boolean mIsLongPressed = false;
    
    private boolean mIsFlinging = false;
    private int mFlingTargetX = 0;
    private int mFlingTargetY = 0;
    
    private boolean mIsLongPressEnabled = false;
    private boolean mIsDragEnabled = false;
    
    private WindowManager mWindowManager;
    
    private int mWidth = 500;
    private int mHeight =150;
    
    private int mDragSlop;
    private int mInViewBoundSlop;
    
    private int mStatusBarHeight;
    
    private GestureDetector mGestureDetector;
    
    public interface OnConfigurationChangedListener
    {
        public void onConfigurationChagned(Configuration newConfig);
    }
    
    private OnConfigurationChangedListener mConfigurationChangedListener;
    
    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.OnGestureListener()
    {
        @Override
        public boolean onDown(MotionEvent event)
        {
            return false;
        }
        
        @Override
        public void onShowPress(MotionEvent e)
        {
            Log.i(TAG, "onShowPress()");
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            Log.i(TAG, "onSingleTapUp()");
            return false;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            //Log.i(TAG, "onScroll()");
            return false;
        }
        
        @Override
        public void onLongPress(MotionEvent e)
        {
            Log.i(TAG, "onLongPress()");
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            final int SWIPE_MIN_DISTANCE = 30;
            //final int SWIPE_MAX_OFF_PATH = 250;
            final int SWIPE_THRESHOLD_VELOCITY_X = 150;
            final int SWIPE_THRESHOLD_VELOCITY_Y = 1500;
            
            Log.i(TAG, "onFling(), vx: " + velocityX + ", vy: " + velocityY);
            mIsFlinging = true;
            
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && 
                Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                mFlingTargetY = mStatusBarHeight;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && 
                       Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY_Y) {
            
                Display display = mWindowManager.getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);
                mFlingTargetY = displaySize.y - TopViewButton.this.getHeight();
            } else {
                mFlingTargetY = mWindowParams.y;
            }
            
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY_X) {
                    mFlingTargetX = 0;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY_X) {
            
                Display display = mWindowManager.getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);
                mFlingTargetX = displaySize.x - TopViewButton.this.getWidth();
            } else {
                Display display = mWindowManager.getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);
                if (mWindowParams.x + getWidth() / 2f <= displaySize.x / 2f) {
                    //smoothMoveTo(0, mWindowParams.y);
                    mFlingTargetX = 0;
                } else {
                    //smoothMoveTo(displaySize.x - getWidth(), mWindowParams.y);
                    mFlingTargetX = displaySize.x - getWidth();
                }
            }
            Log.d(TAG, "onFling: 1");
            smoothMoveTo(mFlingTargetX, mFlingTargetY);
            return false;
        }
    };
    
    public TopViewButton(Context context)
    {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager)mContext.getSystemService(Service.WINDOW_SERVICE);
        mGestureDetector = new GestureDetector(mContext, mOnGestureListener);
        
        mInViewBoundSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mDragSlop = 2 * mInViewBoundSlop;
        mStatusBarHeight = getStatusBarHeight();
        
        mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        
        //mWindowParams.type = 2002;  //type是关键，这里的2002表示系统级窗口，你也可以试试2003。
        mWindowParams.format = 1;
        /**
         *这里的flags也很关键
         *代码实际是wmParams.flags |= FLAG_NOT_FOCUSABLE;
         *40的由来是wmParams的默认属性（32）+ FLAG_NOT_FOCUSABLE（8）
         */
        mWindowParams.flags = 40;
        mWindowParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        
        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        //mWindowParams.gravity = Gravity.NO_GRAVITY;
        mWindowParams.width = mWidth;
        mWindowParams.height = mHeight;
        mWindowParams.x = 0;
        mWindowParams.y = mStatusBarHeight;
        
        this.setFocusable(true);
        restoreColor();
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig)
    {
        if (mConfigurationChangedListener != null) {
            mConfigurationChangedListener.onConfigurationChagned(newConfig);
        }
        super.onConfigurationChanged(newConfig);
    }
    
    public void setOnConfigurationChangedListener(OnConfigurationChangedListener listener)
    {
        mConfigurationChangedListener = listener;
    }
    
    public interface OnTopViewButtonClickListener
    {
        public void onClick(TopViewButton buttonView);
    }
    
    public interface OnTopViewButtonDragListener
    {
        public void onDrag(TopViewButton buttonView, MotionEvent e);
    }
    
    public void setOnTopViewButtonClickListener(OnTopViewButtonClickListener listener)
    {
        mOnClickListener = listener;
    }
    
    public void setOnTopViewButtonDragListener(OnTopViewButtonDragListener listener)
    {
        mOnDragListener = listener;
    }
    
    private int mTouchStartX;
    private int mTouchStartY;
    private int mTouchStartRawX;
    private int mTouchStartRawY;
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mIsDragEnabled) {
            mGestureDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mIsPressedDown = true;
            mTouchStartX = (int)event.getX();
            mTouchStartY = (int)event.getY();
            mTouchStartRawX = (int)event.getRawX();
            mTouchStartRawY = (int)event.getRawY();
            updateButtonDownEffect();
            if (mIsLongPressEnabled) {
                postCheckForLongClick();
            }
            return false;
        case MotionEvent.ACTION_MOVE:
            if (!mIsPressedDown) {
                mTouchStartX = (int)event.getX();
                mTouchStartY = (int)(event.getY());
                mTouchStartRawX = (int)event.getRawX();
                mTouchStartRawY = (int)event.getRawY();
                mIsPressedDown = true;
            }
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            if (!mIsDragging) {
                if (!withinViewBound(event.getX(), event.getY())) {
                    mIsPressedDown = false;
                    restoreColor();
                    if (mPendingCheckLongClickRunnable != null) {
                        removeCallbacks(mPendingCheckLongClickRunnable);
                    }
                }
                /*
                Log.d("roger_tag", "slop: " + mDragSlop);
                Log.d("roger_tag", "x_start: " + (mTouchStartX));
                Log.d("roger_tag", "y_start: " + (mTouchStartY));
                Log.d("roger_tag", "x: " + x);
                Log.d("roger_tag", "y: " + y);
                Log.d("roger_tag", "x_diff: " + (x - mTouchStartX));
                Log.d("roger_tag", "y_diff: " + (y - mTouchStartY));
                */
                if (mIsDragEnabled) {
                    if (Math.abs(x - mTouchStartRawX) > mDragSlop || Math.abs(y - mTouchStartRawY) > mDragSlop) {
                        mIsDragging = true;
                        mTouchStartX = (int)event.getX();
                        mTouchStartY = (int)(event.getY());
                        if (mOnDragListener != null) {
                            mOnDragListener.onDrag(this, event);
                        }
                    }
                }
            } else {
                mWindowParams.x = (int)(x - mTouchStartX);
                mWindowParams.y = (int)(y - mTouchStartY);
                if (mWindowParams.y <= mStatusBarHeight) {
                    mWindowParams.y = mStatusBarHeight;
                }
                mWindowManager.updateViewLayout(this, mWindowParams);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mIsPressedDown) {
                if (!mIsLongPressed) {
                    if (mPendingCheckLongClickRunnable != null) {
                        removeCallbacks(mPendingCheckLongClickRunnable);
                    }
                    if (!mIsDragging) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(this);
                        }
                    } else if (!mIsFlinging){
                        Display display = mWindowManager.getDefaultDisplay();
                        Point displaySize = new Point();
                        display.getSize(displaySize);
                        if (mWindowParams.x + this.getWidth() / 2f <= displaySize.x / 2f) {
                            Log.d(TAG, "ACTION_UP: 1");
                            smoothMoveTo(0, mWindowParams.y);
                        } else {
                            Log.d(TAG, "ACTION_UP: 2");
                            smoothMoveTo(displaySize.x - this.getWidth(), mWindowParams.y);
                        }
                    }
                }
                mIsPressedDown = false;
                restoreColor();
            }
            mIsDragging = false;
            mIsFlinging = false;
            mIsPressedDown = false;
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private boolean withinViewBound(float x, float y)
    {
        if (x >= 0 - mInViewBoundSlop && x < mWindowParams.width + mInViewBoundSlop && y >= 0 - mInViewBoundSlop && y < mWindowParams.height + mInViewBoundSlop) {
            return true;
        }
        return false;
    }
    
    public void setWidth(float w)
    {
        mWindowParams.width = (int)w;
        //mDragSlop = Math.max(mWindowParams.width, mWindowParams.height);
        mWidth = (int)w;
    }
    
    public void setHeight(float h)
    {
        mWindowParams.height = (int)h;
        //mDragSlop = Math.max(mWindowParams.width, mWindowParams.height);
        mHeight = (int)h;
    }
    
    public int getTopViewHeight()
    {
        return mWindowParams.height;
    }
    
    public int getTopViewWidth()
    {
        return mWindowParams.width;
    }
    
    public void setTopButtonX(int x)
    {
        mWindowParams.x = x;
        invalidate();
    }
    
    public void setTopButtonY(int y)
    {
        mWindowParams.y = y + mStatusBarHeight;
    }
    
    public void setLayoutGravity(int gravity)
    {
        mWindowParams.gravity = gravity;
    }
    
    @Override
    public LayoutParams getLayoutParams()
    {
        return mWindowParams;
    }
    
    public boolean isAttached()
    {
        return mIsAttached;
    }
    
    public void show()
    {
        if (!mIsAttached) {
            WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(this, mWindowParams);
            mIsAttached = true;
        } else {
            mWindowManager.updateViewLayout(this, mWindowParams);
            this.setVisibility(View.VISIBLE);
        }
    }
    
    public void hide()
    {
        if (!mIsAttached) {
            WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(this, mWindowParams);
            mIsAttached = true;
        }
        this.setVisibility(View.INVISIBLE);
    }
    
    public void setDragEnabled(boolean isDragEnabled)
    {
        mIsDragEnabled = isDragEnabled;
    }
    
    @Override
    public boolean performLongClick()
    {
        mIsDragging = true;
        return super.performLongClick();
    }
    
    class CheckForLongClickRunnable implements Runnable
    {
        @Override
        public void run()
        {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mIsLongPressed = true;
            performLongClick();
        }
    }
    
    private CheckForLongClickRunnable mPendingCheckLongClickRunnable;
    
    private void postCheckForLongClick()
    {
        mIsLongPressed = false;
        if (mPendingCheckLongClickRunnable == null) {
            mPendingCheckLongClickRunnable = new CheckForLongClickRunnable();
        }
        postDelayed(mPendingCheckLongClickRunnable, 500);
    }
    
    private int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    private void updateButtonDownEffect()
    {
        Drawable background = this.getBackground();
        if (background != null) {
            background.setColorFilter(Color.DKGRAY, Mode.MULTIPLY);
        }
        
        Drawable foreground = this.getDrawable();
        if (foreground != null) {
            foreground.setColorFilter(Color.DKGRAY, Mode.MULTIPLY);
        }
    }
    
    private void restoreColor()
    {
        Drawable background = this.getBackground();
        if (background != null) {
            background.clearColorFilter();
        }
        
        Drawable foreground = this.getDrawable();
        if (foreground != null) {
            foreground.clearColorFilter();
        }
        //this.getBackground().setColorFilter(Color.parseColor("#FFFF5555"), Mode.MULTIPLY);
    }
    
    public void moveTo(int x, int y)
    {
        if (mWindowParams.x != x || mWindowParams.y != y) {
            //int oldX = mWindowParams.x;
            //int oldY = mWindowParams.y;
            mWindowParams.x = x;
            mWindowParams.y = y;
            if (mWindowParams.y <= mStatusBarHeight) {
                mWindowParams.y = mStatusBarHeight;
            }
            if (mIsAttached) {
                mWindowManager.updateViewLayout(this, mWindowParams);
            } else {
                mWindowManager.addView(this, mWindowParams);
                mIsAttached = true;
            }
        }
    }
    
    public void moveBy(int x, int y)
    {
        moveTo(mWindowParams.x + x, mWindowParams.y + y);
    }
    
    public void smoothMoveTo(int x, int y)
    {
        smoothMoveBy(x - mWindowParams.x, y - mWindowParams.y);
    }
    
    public void smoothMoveBy(int dx, int dy)
    {
        Log.i(TAG, "smoothMoveBy: (" + dx + ", " + dy + ")");
        TopViewAnimator anim = new TopViewAnimator(dx, dy);
        anim.setInterpolater(new DecelerateInterpolator());
        anim.setDuration(100);
        anim.setRefreshRate(5);
        anim.start();
    }
    
    public class TopViewAnimator
    {
        public int mDx;
        public int mDy;
        
        public float mOriginX;
        public float mOriginY;
        
        public float mRemainderX;
        public float mRemainderY;
        
        public float mDistX;
        public float mDistY;
        public float mTargetX;
        public float mTargetY;
        
        public long mDuration = 300;
        public long mRefreshRate = 10;
        public int mFrame;
        public int mTotalFrame;
        public float mTimeStep;
        public float mTimeProgress;
        public float mDistProgress;
        
        public Handler mHandler = new Handler();
        
        public Interpolator mInterpolator = new LinearInterpolator();
        
        public Runnable mRunnable = new Runnable()
        {
            @Override
            public void run() {
                if (mFrame++ == mTotalFrame) {
                    return;
                }
                mTimeProgress += mTimeStep;
                mDistProgress = mInterpolator.getInterpolation(mTimeProgress);
                mDistX = mDx * mDistProgress + mRemainderX;
                mDistY = mDy * mDistProgress + mRemainderY;
                mTargetX = mOriginX + mDistX;
                mTargetY = mOriginY + mDistY;
                mWindowParams.x = (int)(mTargetX);
                mWindowParams.y = (int)(mTargetY);
                mRemainderX = mTargetX - mWindowParams.x;
                mRemainderY = mTargetY - mWindowParams.y;
                mWindowManager.updateViewLayout(TopViewButton.this, mWindowParams);
                mHandler.postDelayed(mRunnable, mRefreshRate);
            }
        };
        
        public TopViewAnimator(final int dx, final int dy)
        {
            mDx = dx;
            mDy = dy;
        }
        
        public void start()
        {
            mTotalFrame = (int)(mDuration / mRefreshRate);
            mTimeStep = 1 / (float)mTotalFrame;
            mOriginX = mWindowParams.x;
            mOriginY = mWindowParams.y;
            mHandler.postDelayed(mRunnable, mRefreshRate);
        }
        
        public void setDuration(final long duration)
        {
            mDuration = duration;
        }
        
        public void setInterpolater(final Interpolator interpolator)
        {
            if (interpolator != null) {
                mInterpolator = interpolator;
            }
        }
        
        public void setRefreshRate(final long rate)
        {
            mRefreshRate = rate;
        }
    }
}
