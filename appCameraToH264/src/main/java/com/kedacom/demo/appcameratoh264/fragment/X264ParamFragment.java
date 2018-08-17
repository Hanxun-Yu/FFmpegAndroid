package com.kedacom.demo.appcameratoh264.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kedacom.demo.appcameratoh264.R;
import com.kedacom.demo.appcameratoh264.widget.pick.ProfilePickerDialogFragment;

/**
 * Created by yuhanxun
 * 2018/8/17
 * description:
 */
public class X264ParamFragment extends Fragment {

    private android.widget.LinearLayout parentpart1;
    private android.widget.LinearLayout parentpart2;
    private android.widget.LinearLayout parentpart3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_x264param, null);
        this.parentpart3 = (LinearLayout) view.findViewById(R.id.parent_part3);
        this.parentpart2 = (LinearLayout) view.findViewById(R.id.parent_part2);
        this.parentpart1 = (LinearLayout) view.findViewById(R.id.parent_part1);

        parentpart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfilePickerDialogFragment fragment =new ProfilePickerDialogFragment();
                fragment.show(getFragmentManager(),"ProfilePickerDialogFragment");
            }
        });
        return view;
    }

    public X264ParamFragment() {
    }

}
