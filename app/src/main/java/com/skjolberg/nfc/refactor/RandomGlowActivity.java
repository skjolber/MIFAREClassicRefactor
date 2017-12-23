package com.skjolberg.nfc.refactor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RandomGlowActivity extends Activity {
	
	RelativeLayout contentView;
	int noOfRows = 8;
	int noOfCols = 5;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create a linear layout.
        contentView = new RelativeLayout(this);
        contentView.setBackgroundColor(Color.BLUE);
        setContentView(contentView);
        
        //TODO: Add rows and cols based on the screen dimensions

        for(int i=0; i<noOfRows; i++)
        	for(int j=0; j<noOfCols; j++) 
        	{
        		View tile = new View(this);
        		tile.setBackgroundColor(Color.argb(0x80, 0, 0, 0));
        		RelativeLayout.LayoutParams lp =
        				new RelativeLayout.LayoutParams(100,100);
        		lp.setMargins(j*100, i*100, 0, 0);
        		tile.setLayoutParams(lp
        				);
        		contentView.addView(tile); 
        	}
    }

    final Handler mHandler = new Handler();
    
    @Override
    public void onResume() {
    	super.onResume();
    	//Animate a tile at random.
    	final Runnable task = new Runnable() {
			
			@Override
			public void run() {
//				animateRandomChild();
				animateRandomTile();
				mHandler.postDelayed(this, 1000);
			}
		};
		mHandler.postDelayed(task, 1000);
    }
    
    private void animateRandomTile() {
    	int randomChild = (int) (Math.random() * noOfRows * noOfCols);
    	View tile = contentView.getChildAt(randomChild);
    	
    	AlphaAnimation alphaAnimation = new AlphaAnimation(tile.getAlpha(), 0);
    	alphaAnimation.setDuration(500);
    	alphaAnimation.setRepeatMode(Animation.REVERSE);
    	alphaAnimation.setRepeatCount(1);
    	alphaAnimation.setInterpolator(new DecelerateInterpolator());
    	tile.startAnimation(alphaAnimation);
    }
}