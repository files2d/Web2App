package com.csu.web2app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class MyCustomDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("标题")
                .setMessage("内容")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 在这里处理确认按钮的点击事件
                        dialog.dismiss(); // 关闭对话框
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 在这里处理取消按钮的点击事件
                        dialog.dismiss(); // 关闭对话框
                    }
                });
        //setCancelable(false);
        // 设置对话框不可取消
        return builder.create();
    }
}

