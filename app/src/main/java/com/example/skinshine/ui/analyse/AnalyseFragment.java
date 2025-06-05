package com.example.skinshine.ui.analyse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.example.skinshine.R;

public class AnalyseFragment extends Fragment {

    private String[] questions = {
            "How does your skin feel a few hours after cleansing?",
            "How often do you experience shine on your face?",
            "Do you have visible dry or flaky patches?",
            "Is your skin easily irritated or red?",
            "How visible are your pores?"
    };

    private String[][] options = {
            {"Tight/dry", "Normal", "Oily", "Combination", "Sensitive"},
            {"Rarely", "Sometimes", "Often", "Only T-zone", "Never"},
            {"Often", "Rarely", "Never", "Sometimes", "Occasionally"},
            {"Very easily", "Sometimes", "Rarely", "Never", "Often"},
            {"Small", "Medium", "Large", "Large on T-zone", "Normal"}
    };

    // Initialize answers with -1 to indicate unanswered
    private int[] answers = {-1, -1, -1, -1, -1};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analyse, container, false);

        ViewGroup questionsContainer = root.findViewById(R.id.questions_container);

        // Get reference to result card to show/hide it
        CardView resultCard = root.findViewById(R.id.result_card);
        // Initially hide the result card
        resultCard.setVisibility(View.GONE);

        TextView resultText = root.findViewById(R.id.result_text);

        for (int i = 0; i < questions.length; i++) {
            View qView = inflater.inflate(R.layout.item_question, questionsContainer, false);
            TextView qText = qView.findViewById(R.id.question_text);
            qText.setText((i+1) + ". " + questions[i]);
            
            RadioGroup rg = qView.findViewById(R.id.options_group);
            
            for (int j = 0; j < options[i].length; j++) {
                RadioButton rb = new RadioButton(getContext());
                rb.setText(options[i][j]);
                // Generate unique IDs for each RadioButton
                rb.setId(View.generateViewId());
                rb.setTextSize(15);
                rb.setTag(j); // Store the answer value
                rg.addView(rb);
            }
            
            final int questionIndex = i;
            rg.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != -1) {
                    RadioButton selectedButton = group.findViewById(checkedId);
                    if (selectedButton != null) {
                        answers[questionIndex] = (int) selectedButton.getTag();
                    }
                }
            });
            
            questionsContainer.addView(qView);
        }

        Button submitBtn = root.findViewById(R.id.submit_button);

        submitBtn.setOnClickListener(v -> {
            boolean allAnswered = true;
            for (int ans : answers) {
                if (ans == -1) {
                    allAnswered = false;
                    break;
                }
            }
            
            if (!allAnswered) {
                Toast.makeText(getContext(), "Please answer all questions", Toast.LENGTH_SHORT).show();
                resultCard.setVisibility(View.GONE);
            } else {
                String skinType = calculateSkinType();
                resultText.setText("Your skin type: " + skinType);
                resultCard.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private String calculateSkinType() {
        int dry = 0, oily = 0, combination = 0, sensitive = 0, normal = 0;
        
        for (int ans : answers) {
            switch (ans) {
                case 0: dry++; break;
                case 1: normal++; break;
                case 2: oily++; break;
                case 3: combination++; break;
                case 4: sensitive++; break;
            }
        }
        
        int max = Math.max(Math.max(Math.max(Math.max(dry, oily), combination), sensitive), normal);
        
        if (max == dry) return "Dry";
        if (max == oily) return "Oily";
        if (max == combination) return "Combination";
        if (max == sensitive) return "Sensitive";
        return "Normal";
    }
}
