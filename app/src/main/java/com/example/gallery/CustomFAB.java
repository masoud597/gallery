package com.example.gallery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomFAB extends LinearLayout {
    private FloatingActionButton btnOne, btnTwo, btnOpen;
    private int selectedColumn = 2;
    private onSelectedColumnChange selectedColumnListener;

    public interface onSelectedColumnChange {
        void onSelectedColumnChanged(int newColumn);
    }
    public void setOnSelectedColumnChangeListener(onSelectedColumnChange listener) {
        this.selectedColumnListener = listener;
    }
    public void setSelectedColumn(int selectedColumn) {
        if (this.selectedColumn != selectedColumn) {
            this.selectedColumn = selectedColumn;
            if (selectedColumnListener != null) {
                selectedColumnListener.onSelectedColumnChanged(selectedColumn);
            }
        }
    }


    public CustomFAB(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }
    public CustomFAB(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setContainerBackground();
        inflateButtons();
        setupButtons();
    }
    private void setContainerBackground() {
        GradientDrawable containerBackground = new GradientDrawable();
        containerBackground.setColor(Color.WHITE);
        containerBackground.setCornerRadius(40f);
        containerBackground.setStroke(5, Color.LTGRAY);
        setPadding(3,3,3,3);
        setBackground(containerBackground);
    }
    private void inflateButtons() {
        btnOne = new FloatingActionButton(getContext());
        btnTwo = new FloatingActionButton(getContext());
        btnOpen = new FloatingActionButton(getContext());

        addView(btnOpen);
        addView(btnOne);
        addView(btnTwo);
    }
    private void setupButtons() {
        btnOne.setImageResource(R.drawable.one);
        btnTwo.setImageResource(R.drawable.two);
        btnOpen.setImageResource(R.drawable.add);

        btnOpen.setVisibility(GONE);

        btnOne.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnTwo.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        btnOpen.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        btnOne.setImageTintList(null);
        btnTwo.setImageTintList(null);
        btnOpen.setImageTintList(null);

        btnOne.setOutlineProvider(null);
        btnTwo.setOutlineProvider(null);
        btnOpen.setOutlineProvider(null);

        btnOne.setOnClickListener(v -> {
            setSelectedColumn(1);
            btnOne.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            btnTwo.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        });
        btnTwo.setOnClickListener(v -> {
            setSelectedColumn(2);
            btnTwo.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            btnOne.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        });
    }
    public void setOpenButtonOnClickListener(Runnable action) {
        btnOpen.setOnClickListener(v -> action.run());
    }
}
