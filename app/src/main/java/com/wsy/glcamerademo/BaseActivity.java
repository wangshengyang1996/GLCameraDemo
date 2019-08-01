package com.wsy.glcamerademo;

import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class BaseActivity extends AppCompatActivity {

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 是否全被同意
     */
    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        onRequestPermissionResult(requestCode, isAllGranted);
    }

    /**
     * 封装{@link #onRequestPermissionsResult(int, String[], int[])}，回传请求码和是否全部被同意
     *
     * @param requestCode  请求权限时的请求码
     * @param isAllGranted 是否全被同意
     */
    protected void onRequestPermissionResult(int requestCode, boolean isAllGranted) {

    }

    protected void showToast(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
