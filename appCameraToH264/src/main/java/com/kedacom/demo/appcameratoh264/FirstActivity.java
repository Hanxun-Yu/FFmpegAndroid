package com.kedacom.demo.appcameratoh264;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yuhanxun
 * 2018/10/10
 * description:
 */
public class FirstActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //do something
        //...wait a pic


        startNext();
    }


    private void startNext() {
        Intent intent = new Intent(this,PermissionActivity.class);
        startActivity(intent);
        finish();
    }
}
