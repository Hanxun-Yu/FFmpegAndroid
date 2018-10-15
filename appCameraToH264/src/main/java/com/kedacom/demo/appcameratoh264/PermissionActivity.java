package com.kedacom.demo.appcameratoh264;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

/**
 * Created by yuhanxun
 * 2018/10/10
 * description:
 */
public class PermissionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionBiz();
    }

    private void requestPermission() {
        // 类似 startActivityForResult()中的REQUEST_CODE
        // 权限列表,将要申请的权限以数组的形式提交。
        // 系统会依次进行弹窗提示。
        // 注意：如果AndroidManifest.xml中没有进行权限声明，这里配置了也是无效的，不会有弹窗提示。
        ActivityCompat.requestPermissions(this,
                permissions,
                REQUEST_CODE);
    }


    boolean hasOnstop = false;
    @Override
    protected void onStop() {
        super.onStop();
        Logger.e("onStop");
        hasOnstop = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.e("onResume hasOnstop:"+hasOnstop);

        if(hasOnstop) {
            hasOnstop = false;
            permissionBiz();
        }
    }

    private void permissionBiz() {
        if(checkPermission())
            startNext();
        else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        //如果返回true表示已经授权了
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//                && checkSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT) == PackageManager.PERMISSION_GRANTED
                ) {
            return true;
        } else {

            return false;
        }

    }

    final int REQUEST_CODE = 99;
    String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.CAPTURE_AUDIO_OUTPUT
    };


    private void startNext() {
        Toast.makeText(PermissionActivity.this, "OK", Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String _permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                Logger.d(_permissions);
                Logger.d(grantResults);

                boolean hasNoPrompt = false;
                for(int i=0;i<_permissions.length;i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                _permissions[i])) {
                            // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。

                        } else {
                            Toast.makeText(PermissionActivity.this, "选择了不再提示:" + _permissions[i] +
                                    "\n请进设置手动打开", Toast.LENGTH_SHORT).show();
                            hasNoPrompt = true;
                            break;
                        }
                    }
                }
                Logger.d("hasNoPrompt:"+hasNoPrompt);

                if(!hasNoPrompt) {
                    permissionBiz();
                } else {
                    gotoSetting();
                }
            }
        }
    }

    private void gotoSetting() {
        String sdk = android.os.Build.VERSION.SDK; // SDK号

        String model = android.os.Build.MODEL; // 手机型号

        String release = android.os.Build.VERSION.RELEASE; // android系统版本号
        String brand = Build.BRAND;//手机厂商
        if (TextUtils.equals(brand.toLowerCase(), "redmi") || TextUtils.equals(brand.toLowerCase(), "xiaomi")) {
            gotoMiuiPermission();//小米
        } else if (TextUtils.equals(brand.toLowerCase(), "meizu")) {
            gotoMeizuPermission();
        } else if (TextUtils.equals(brand.toLowerCase(), "huawei") || TextUtils.equals(brand.toLowerCase(), "honor")) {
            gotoHuaweiPermission();
        } else {
            startActivity(getAppDetailSettingIntent());
        }

    }

    /**
     * 跳转到miui的权限管理页面
     */
    private void gotoMiuiPermission() {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname",getPackageName());
            startActivity(localIntent);
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", getPackageName());
                startActivity(localIntent);
            } catch (Exception e1) { // 否则跳转到应用详情
                startActivity(getAppDetailSettingIntent());
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }
    }

    /**
     * 华为的权限管理页面
     */
    private void gotoHuaweiPermission() {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }

    }

    /**
     * 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

}
