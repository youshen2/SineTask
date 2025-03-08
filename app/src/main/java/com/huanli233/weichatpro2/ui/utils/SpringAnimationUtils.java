package com.huanli233.weichatpro2.ui.utils;

import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;

public class SpringAnimationUtils {
    public static final FloatPropertyCompat<View> FLOAT_PROPERTY_TRANSLATION_Y = new FloatPropertyCompat<View>("translationY") {

        @Override // android.support.animation.FloatPropertyCompat
        public float getValue(View view) {
            return view.getTranslationY();
        }

        @Override // android.support.animation.FloatPropertyCompat
        public void setValue(View view, float f) {
            view.setTranslationY(f);
        }
    };
}