package com.huanli233.weichatpro2.ui.widget.scalablecontainer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.huanli233.weichatpro2.ui.utils.SpringAnimationUtils;
import com.huanli233.weichatpro2.ui.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import moye.sine.task.R;
import moye.sine.task.utils.ToolsUtil;

public class AppNestedScrollView extends NestedScrollView {
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO = 1.5f;
    public static final int OVER_SCROLLING_STATE = 1;
    public static final int OVER_SCROLL_FLING_CHECKING = 3;
    public static final int OVER_SCROLL_FLING_ING = 4;
    public static final int OVER_SCROLL_STATE_BACKING = 2;
    public static final int OVER_SCROLL_STATE_IDLE = 0;
    public static final float RESET_SCALE = 1.0f;
    public static final float START_SCALE = 0.8f;
    public static final int TYPE_DRAG_OVER_BACK = 1;
    public static final int TYPE_FLING_BACK = 0;
    private static final int DRAG_SIDE_END = 2;
    private static final int DRAG_SIDE_START = 1;
    private static final String TAG = "AppScrollView";
    private final DynamicAnimation.OnAnimationEndListener animationEndListener;
    private SpringAnimation anim;
    private List<View> animScaleViews = new ArrayList<>();
    private boolean enableEnd;
    private boolean enableStart;
    private int flingOverScrollState;
    private float flingVelocityY;
    private boolean isAnimScale;
    private long lastTrackTime;
    private int lastY;
    private int overScrollState;
    private int startDragSide;
    private int startPointId;

    private int ansv_anim_scale_list;
    private int skipOverScrollId;

    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private ValueAnimator mResetAnimator;
    private OnDragCloseListener mDragCloseListener;

    public AppNestedScrollView(Context context) {
        this(context, null);
    }

    public AppNestedScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppNestedScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.overScrollState = 0;
        this.flingOverScrollState = 0;
        this.enableStart = true;
        this.enableEnd = true;
        this.animationEndListener = new DynamicAnimation.OnAnimationEndListener() { // from class: com.xtc.ui.widget.scalablecontainer.AppNestedScrollView.2
            @Override // android.support.animation.DynamicAnimation.OnAnimationEndListener
            public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                AppNestedScrollView.this.overScrollState = 0;
                AppNestedScrollView.this.flingOverScrollState = 0;
            }
        };
        setOverScrollMode(2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppNestedScrollView, 0, 0);
        this.isAnimScale = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_ansv_anim_scale, false);
        this.enableStart = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_ansv_enable_start, true);
        this.enableEnd = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_ansv_enable_end, true);
        this.ansv_anim_scale_list = obtainStyledAttributes.getResourceId(R.styleable.AppNestedScrollView_ansv_anim_scale_list, 0);
        this.skipOverScrollId = obtainStyledAttributes.getResourceId(R.styleable.AppNestedScrollView_ansv_skip_overscroll, 0);
        obtainStyledAttributes.recycle();
    }

    @Override // android.support.v4.widget.NestedScrollView, android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        if (this.isAnimScale) {
            if (this.ansv_anim_scale_list == 0) {
                View childAt = getChildAt(0);
                if (childAt instanceof ViewGroup) {
                    setAnimScaleViews(collectChildren((ViewGroup) childAt));
                }
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.isAnimScale && this.ansv_anim_scale_list != 0) {
            View animList = findViewById(this.ansv_anim_scale_list);
            if (animList != null && animList instanceof ViewGroup) {
                setAnimScaleViews(collectChildren((ViewGroup) animList));
            }
        }
    }

    public void setAnimScale(boolean z) {
        this.isAnimScale = z;
    }

    public void setAnimScaleViews(List<View> list) {
        this.animScaleViews = list;
        post(AppNestedScrollView.this::scaleVerticalChildView);
    }

    public boolean isEnableStart() {
        return this.enableStart;
    }

    public void setEnableStart(boolean z) {
        this.enableStart = z;
    }

    public boolean isEnableEnd() {
        return this.enableEnd;
    }

    public void setEnableEnd(boolean z) {
        this.enableEnd = z;
    }

    private boolean hasChild() {
        return getChildCount() != 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override
    // android.support.v4.widget.NestedScrollView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!this.isAnimScale || this.animScaleViews == null) {
            return;
        }
        scaleVerticalChildView();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.widget.NestedScrollView, android.view.View
    public void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        doScrollChanged(i2);
    }

    private void doScrollChanged(int i) {
        if (hasChild()) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = currentTimeMillis - this.lastTrackTime;
            int scrollY = getScrollY();
            int i2 = this.lastY;
            if (scrollY != i2 && j > 0) {
                this.flingVelocityY = ((scrollY - i2) * 1000.0f) / ((float) j);
                this.lastY = scrollY;
                this.lastTrackTime = currentTimeMillis;
            }
            if (this.flingOverScrollState == 3) {
                float translationY = getChildAt(0).getTranslationY();
                boolean isInAbsoluteStart = ViewUtils.isInAbsoluteStart(this, 1);
                boolean isInAbsoluteEnd = ViewUtils.isInAbsoluteEnd(this, 1);
                if ((isInAbsoluteStart && this.enableStart && this.flingVelocityY < 0.0f) || (isInAbsoluteEnd && this.enableEnd && this.flingVelocityY > 0.0f)) {
                    this.flingOverScrollState = 4;
                    createAnimIfNeed(0);
                    this.anim.setStartVelocity(((-this.flingVelocityY)) / 2.0f);
                    this.anim.animateToFinalPosition(0.0f);
                } else if ((isInAbsoluteStart || isInAbsoluteEnd) && translationY != 0.0f) {
                    this.flingOverScrollState = 4;
                    createAnimIfNeed(0);
                    this.anim.animateToFinalPosition(0.0f);
                }
            }
            if (!this.isAnimScale || this.animScaleViews == null) {
                return;
            }
            scaleVerticalChildView();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    private void scaleVerticalChildView() {
        int scrollY = getScrollY();
        int measuredHeight = getMeasuredHeight();

        for (View child : this.animScaleViews) {
            if (child.getVisibility() == View.VISIBLE) {
                int top = child.getTop();
                int bottom = child.getBottom();
                int height = child.getHeight();

                // 计算子元素的可见部分
                int visibleTop = Math.max(top, scrollY);
                int visibleBottom = Math.min(bottom, scrollY + measuredHeight);
                int visibleHeight = visibleBottom - visibleTop;

                // 如果子元素不可见，跳过
                if (visibleHeight <= 0) {
                    continue;
                }

                // 计算缩放比例
                float visibleRatio = (float) visibleHeight / (float) height;
                float scaleFactor = 1.0f;

                // 判断是否从底部进入
                if (top > scrollY + ToolsUtil.dp2px(this.getContext(), 20)) { // 子元素的底部大于 RecyclerView 的可见区域顶部
                    scaleFactor = 0.85f + (visibleRatio * 0.15f); // 根据可见比例动态调整缩放比例
                }

                // 设置缩放中心点
                child.setPivotX(child.getWidth() / 2.0f);
                child.setPivotY(child.getHeight() / 2.0f);

                // 应用缩放比例
                child.setScaleX(scaleFactor);
                child.setScaleY(scaleFactor);
            }
        }
    }


    /* JADX WARN: Code restructure failed: missing block: B:14:0x002f, code lost:
        if (r2 != 3) goto L15;
     */
    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (hasChild()) {
            View childAt = getChildAt(0);
            int action = motionEvent.getAction();
            float translationY = childAt.getTranslationY();
            SpringAnimation springAnimation = this.anim;
            if (springAnimation != null && springAnimation.isRunning()) {
                this.anim.cancel();
            }
            if (action != 1) {
                if (action == 2) {
                    if (motionEvent.getHistorySize() != 0) {
                        float y = motionEvent.getY(0) - motionEvent.getHistoricalY(0, 0);
                        if (Math.abs(y) >= Math.abs(motionEvent.getX(0) - motionEvent.getHistoricalX(0, 0))) {
                            int i = y > 0.0f ? 1 : 2;
                            boolean z = ViewUtils.isInAbsoluteStart(this, 1) && this.enableStart;
                            boolean z2 = ViewUtils.isInAbsoluteEnd(this, 1) && this.enableEnd;
                            if (this.overScrollState == 0) {
                                if ((i == 1 && z) || (i == 2 && z2)) {
                                    this.startPointId = motionEvent.getPointerId(0);
                                    this.startDragSide = i;
                                    this.overScrollState = 1;
                                }
                            }
                            if (this.overScrollState == 1) {
                                if (this.startPointId != motionEvent.getPointerId(0)) {
                                    finishOverScroll();
                                } else {
                                    float f = translationY + (y / 1.5f);
                                    int i2 = this.startDragSide;
                                    if (i != i2 && ((i2 == 1 && f <= 0.0f) || (this.startDragSide == 2 && f > 0.0f))) {
                                        this.overScrollState = 0;
                                    } else {
                                        ViewParent parent = getParent();
                                        if (parent != null) {
                                            parent.requestDisallowInterceptTouchEvent(true);
                                        }
                                        childAt.setTranslationY(f);
                                        // 调整排除元素的translationY
                                        adjustExcludedViewsTranslationY(f);
                                    }
                                }
                            }
                        }
                    }
                } else if (action == 3) {
                    int i3 = this.overScrollState;
                    if (i3 != 2) {
                        if (i3 == 1) {
                            finishOverScroll();
                        } else if (this.flingOverScrollState == 0) {
                            this.flingVelocityY = 0.0f;
                            this.lastY = getScrollY();
                            this.flingOverScrollState = 3;
                            this.lastTrackTime = System.currentTimeMillis();
                            doScrollChanged(this.lastY);
                        }
                    }
                }
                return super.dispatchTouchEvent(motionEvent);
            }
            int i3 = this.overScrollState;
            if (i3 != 2) {
                if (i3 == 1) {
                    finishOverScroll();
                } else if (this.flingOverScrollState == 0) {
                    this.flingVelocityY = 0.0f;
                    this.lastY = getScrollY();
                    this.flingOverScrollState = 3;
                    this.lastTrackTime = System.currentTimeMillis();
                    doScrollChanged(this.lastY);
                }
            }
            return super.dispatchTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void finishOverScroll() {
        this.overScrollState = 2;
        createAnimIfNeed(1);
        this.anim.setStartVelocity(0.0f).animateToFinalPosition(0.0f);
    }

    private void createAnimIfNeed(int i) {
        if (this.anim == null) {
            View childAt = getChildAt(0);
            this.anim = new SpringAnimation(childAt, SpringAnimationUtils.FLOAT_PROPERTY_TRANSLATION_Y).setSpring(new SpringForce().setDampingRatio(1.0f).setStiffness(115.0f));
            this.anim.addEndListener(this.animationEndListener);
            // 添加更新监听器，同步排除元素的translationY
            this.anim.addUpdateListener((animation, value, velocity) -> {
                adjustExcludedViewsTranslationY((Float) value);
            });
        }
        SpringForce spring = this.anim.getSpring();
        if (i == 0) {
            spring.setStiffness(115.0f);
        }
        if (i == 1) {
            spring.setStiffness(200.0f);
        }
    }

    private List<View> collectChildren(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        ArrayList<View> arrayList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof LinearLayout) {  // 如果子元素是 LinearLayout，可以递归收集
                arrayList.addAll(collectChildren((ViewGroup) child));
            } else {
                arrayList.add(child);  // 添加非 LinearLayout 的子元素
            }
        }
        return arrayList;
    }

    private void adjustExcludedViewsTranslationY(float translationY) {
        if (skipOverScrollId == 0) {
            return;
        }
        View container = getChildAt(0);
        if (container instanceof ViewGroup) {
            View excludedView = ((ViewGroup) container).findViewById(skipOverScrollId);
            if (excludedView != null) {
                excludedView.setTranslationY(-translationY);
            }
        }
    }

    public void setOnDragCloseListener(OnDragCloseListener listener) {
        mDragCloseListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragCloseListener != null) {
            handleDragCloseTouch(ev);
        }
        return super.onTouchEvent(ev);
    }

    private void handleDragCloseTouch(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (getScrollY() == 0) {
                    mInitialMotionY = y;
                    mIsBeingDragged = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mInitialMotionY > 0 && !mIsBeingDragged) {
                    final float yDiff = y - mInitialMotionY;
                    if (yDiff > getTouchSlop()) {
                        mIsBeingDragged = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (mIsBeingDragged) {
                    float translationY = y - mInitialMotionY;
                    mDragCloseListener.onDrag(translationY);
                    return;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    VelocityTracker velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000);

                    float velocityY = velocityTracker.getYVelocity();
                    mDragCloseListener.onRelease(velocityY);

                    velocityTracker.recycle();
                    resetTouch();
                }
                mInitialMotionY = 0;
                break;
        }
    }

    private void resetTouch() {
        mIsBeingDragged = false;
        getParent().requestDisallowInterceptTouchEvent(false);
    }

    private int getTouchSlop() {
        return ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public interface OnDragCloseListener {
        void onDrag(float translationY);

        void onRelease(float velocityY);
    }
}