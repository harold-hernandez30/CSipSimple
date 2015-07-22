package com.septrivium.augeo.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Created by harold on 7/23/2015.
 */
public class BitmapUtils {

    public static Bitmap combineImage(Bitmap srcBitmap, Bitmap dstBitmap, PorterDuff.Mode porterDuffMode) {

        Bitmap.Config config = srcBitmap.getConfig();
        Bitmap newBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(srcBitmap, 0, 0, null);

        Paint paint = new Paint();
        PorterDuff.Mode selectedMode = porterDuffMode;

        paint.setXfermode(new PorterDuffXfermode(selectedMode));
        newCanvas.drawBitmap(dstBitmap,
                srcBitmap.getWidth() - dstBitmap.getWidth(),  //show image to the right
                0, //show image to the top
                paint);

        return newBitmap;
    }
}
