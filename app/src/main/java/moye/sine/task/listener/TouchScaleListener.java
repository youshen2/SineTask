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
package moye.sine.task.listener;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class TouchScaleListener implements View.OnTouchListener {
    private static final float SCALE_REDUCE_FACTOR = 0.9f;
    private float mOriginalScaleX = 1.0f;
    private float mOriginalScaleY = 1.0f;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                float targetScaleX = mOriginalScaleX * SCALE_REDUCE_FACTOR;
                float targetScaleY = mOriginalScaleY * SCALE_REDUCE_FACTOR;

                view.animate()
                        .scaleX(targetScaleX)
                        .scaleY(targetScaleY)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(170)
                        .start();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                view.animate()
                        .scaleX(mOriginalScaleX)
                        .scaleY(mOriginalScaleY)
                        .setDuration(170)
                        .start();
                break;
        }
        return false;
    }
}
