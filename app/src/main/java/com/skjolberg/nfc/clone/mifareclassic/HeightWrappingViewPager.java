package com.skjolberg.nfc.clone.mifareclassic;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class HeightWrappingViewPager extends ViewPager {

    public HeightWrappingViewPager(Context context) {
        super(context);
    }

    public HeightWrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST;

        if(wrapHeight) {
            /**
             * The first super.onMeasure call made the pager take up all the 
             * available height. Since we really wanted to wrap it, we need 
             * to remeasure it. Luckily, after that call the first child is 
             * now available. So, we take the height from it. 
             */

            int width = getMeasuredWidth();
            int height = getMeasuredHeight();

            // Use the previously measured width but simplify the calculations
            //widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

            /* If the pager actually has any children, take the first child's 
             * height and call that our own */ 
            if(getChildCount() > 0) {
                View firstChild = getChildAt(0);

                /* The child was previously measured with exactly the full height.
                 * Allow it to wrap this time around. */
                firstChild.measure(widthMeasureSpec, 
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));

                height = firstChild.getMeasuredHeight();
                width = firstChild.getMeasuredHeight();
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
