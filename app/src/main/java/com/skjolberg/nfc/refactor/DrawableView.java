/***************************************************************************
 *
 * This file is part of the 'NDEF Tools for Android' project at
 * http://code.google.com/p/ndef-tools-for-android/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************/
package com.skjolberg.nfc.refactor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.view.View;

public class DrawableView extends View {
	
    private Context mContext;

    public DrawableView(Context context) {
        super(context);
        mContext = context;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //Paint Background
        Paint background = new Paint();
        background.setColor(getResources().getColor(0xFF000000));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        //Set vars for Arrow Paint
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(0xFF000000));
        
        
        
        
        
        /*
        arrLength = radius - 10;

        if(centerX < centerY)
            radius = centerX - margin;
        else 
            radius = centerY - margin;

        //Draw Shaft
       
        //Draw ArrowHead
            //This is where I'm confused

     // create and draw triangles
        // use a Path object to store the 3 line segments
        // use .offset to draw in many locations
        // note: this triangle is not centered at 0,0
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.RED);
        Path path = new Path();
        path.moveTo(0, -10);
        path.lineTo(5, 0);
        path.lineTo(-5, 0);
        path.close();
        path.offset(10, 40);
        canvas.drawPath(path, paint);
        path.offset(50, 100);
        canvas.drawPath(path, paint);
        // offset is cumlative
        // next draw displaces 50,100 from previous
        path.offset(50, 100);
        
        */
        Path path = getArrowPath(getWidth() / 2, getHeight() / 2, getWidth(), 0, 100);
        
        canvas.drawPath(path, paint);
    }

    private Path getArrowPath(int centerX, int centerY,int width, int angle, int radius)
    {

    	Path path = new Path();
    	path.setFillType(FillType.EVEN_ODD);
    	int x, y, sx,sy;
    	double rdeg1 = (Math.PI * (90 - angle)) / 180;
    	//fill the base line first
    	int iradius = width/2;
    	sx = (int) (centerX + (iradius * Math.cos(rdeg1)));
    	sy = (int) (centerY + (iradius * Math.sin(rdeg1)));
    	path.moveTo(sx, sy);
    	double rdeg2 = (Math.PI * (270-angle)) / 180;
    	x = (int) (centerX + (iradius * Math.cos(rdeg2)));
    	y = (int) (centerY + (iradius * Math.sin(rdeg2)));
    	path.lineTo(x, y);
    	double rdeg = (Math.PI * angle) / 180;
    	//fill the arrow line 1
    	x = (int) (centerX + (radius * Math.cos(rdeg)));
    	y = (int) (centerY + (radius * Math.sin(rdeg)));
    	path.lineTo(x, y);
    	//fill the arrow line 2
    	path.moveTo(sx, sy);
    	path.close();
    	return path;
    }
}