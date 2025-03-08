/*************************************************************************

 Copyright 2025 爅峫

 This file is part of KeyFinder.

 KeyFinder is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KeyFinder is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KeyFinder.  If not, see <http://www.gnu.org/licenses/>.

 *************************************************************************/
package moye.sine.task.dialog.base;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import moye.sine.task.R;

public class BaseDialogFragment extends DialogFragment {
    private static final int ENTER_DURATION = 300; // 进入动画时长调整为300ms
    private static final int EXIT_DURATION = 250;  // 退出动画时长调整为250ms
    public Context oldContext;
    protected View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_SineTask_Dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        Animation enterAnim = createSlideAnimation(true);
        view.startAnimation(enterAnim);
        super.onViewCreated(view, savedInstanceState);
    }

    public void close() {
        Animation exitAnim = createSlideAnimation(false);
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BaseDialogFragment.this.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(exitAnim);
    }

    private Animation createSlideAnimation(boolean isEnter) {
        float fromY = isEnter ? 1.0f : 0.0f;
        float toY = isEnter ? 0.0f : 1.0f;
        TranslateAnimation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, fromY,
                Animation.RELATIVE_TO_SELF, toY
        );
        anim.setDuration(isEnter ? ENTER_DURATION : EXIT_DURATION);
        anim.setInterpolator(isEnter ?
                new DecelerateInterpolator() : // 进入使用减速插值
                new AccelerateInterpolator());  // 退出使用加速插值
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        return anim;
    }
}