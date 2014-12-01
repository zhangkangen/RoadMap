package com.example.roadmap.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.example.roadmap.R;

/**
 * Created by Administrator on 2014/10/27.
 */
public class GPSDialogFragment extends DialogFragment {

    public static GPSDialogFragment newInstance(String title){
        GPSDialogFragment fragment = new GPSDialogFragment();
        Bundle args = new Bundle();
        args.putString("title",title);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        return new AlertDialog.Builder(getActivity()).setTitle(title).setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);// 跳转到位置设置界面
                startActivityForResult(intent,
                        R.layout.activity_my);
            }
        }).setNegativeButton("取消",null).create();
    }
}
