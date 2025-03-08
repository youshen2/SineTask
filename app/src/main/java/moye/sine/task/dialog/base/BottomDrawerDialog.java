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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanli233.weichatpro2.ui.widget.scalablecontainer.AppNestedScrollView;

import moye.sine.task.R;

public class BottomDrawerDialog extends BaseDialogFragment {
    private static final int MIN_FLING_VELOCITY = 1000;
    private static final float MIN_CLOSE_TRANSLATION = 0.15f; // 关闭阈值（百分比）
    private AppNestedScrollView mScrollView;
    private float mCurrentTranslation = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View root) {
        mScrollView = root.findViewById(R.id.scrollView);
        mScrollView.setOnDragCloseListener(new AppNestedScrollView.OnDragCloseListener() {
            @Override
            public void onDrag(float translationY) {
                handleDrag(translationY);
            }

            @Override
            public void onRelease(float velocityY) {
                handleRelease(velocityY);
            }
        });
    }

    private void handleDrag(float deltaY) {
        // 限制拖动范围
        float newTranslation = mCurrentTranslation + deltaY;
        newTranslation = Math.max(0, Math.min(newTranslation, view.getHeight()));

        // 使用属性动画平滑过渡
        view.animate()
                .translationY(newTranslation)
                .setDuration(0) // 立即生效
                .start();

        mCurrentTranslation = newTranslation;

        // 更新背景透明度
        updateBackgroundAlpha(newTranslation);
    }

    private void handleRelease(float velocityY) {
        final float screenHeight = getResources().getDisplayMetrics().heightPixels;
        final float closeThreshold = view.getHeight() * MIN_CLOSE_TRANSLATION;
        final boolean fastEnough = velocityY > MIN_FLING_VELOCITY;
        final boolean farEnough = mCurrentTranslation > closeThreshold;
        final boolean shouldClose = fastEnough || farEnough;

        if (shouldClose) {
            close();
        } else {
            resetPosition();
        }
    }

    private void updateBackgroundAlpha(float translationY) {
        float progress = translationY / view.getHeight();
        int alpha = (int) (0x80 * (1 - progress)); // 原始alpha为0x80（128）
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.argb(alpha, 0, 0, 0))
            );
        }
    }

    private void resetPosition() {
        view.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 恢复背景
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.parseColor("#80000000"))
            );
        }
    }
}