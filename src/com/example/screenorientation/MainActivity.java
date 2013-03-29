package com.example.screenorientation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent();
        serviceIntent.setClassName("com.example.screenorientation", "com.example.screenorientation.TopViewService");
        this.startService(serviceIntent);
    }
}
