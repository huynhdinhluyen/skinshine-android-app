package com.example.skinshine.ui.analyse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.skinshine.R;

public class SkinResultFragment extends Fragment {

    private static final String ARG_SKIN_TYPE = "skin_type";
    private static final String ARG_TREATMENT = "treatment_schedule";

    public static SkinResultFragment newInstance(String skinType, String treatmentSchedule) {
        SkinResultFragment fragment = new SkinResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SKIN_TYPE, skinType);
        args.putString(ARG_TREATMENT, treatmentSchedule);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_skin_result, container, false);

        TextView skinTypeText = root.findViewById(R.id.text_skin_type);
        TextView treatmentText = root.findViewById(R.id.text_treatment_schedule);

        Bundle args = getArguments();
        if (args != null) {
            String skinType = args.getString(ARG_SKIN_TYPE, "Không xác định");
            String treatment = args.getString(ARG_TREATMENT, "Không có lộ trình phù hợp.");
            skinTypeText.setText("Skin Type: " + skinType);
            treatmentText.setText(treatment);
        }

        return root;
    }
}