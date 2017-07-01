/*
   Copyright (C) 2015 Preet Desai (preet.desai@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package dev.ks.platform.sdl;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import org.libsdl.app.SDLActivity;

public class KsActivity extends SDLActivity
{
    // ============================================================= //

    @Override
    protected String[] getLibraries() {
        return new String[] {
                "SDL2",
                "gnustl_shared",
                "ks_test"
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get Display Info
        getDisplayInfo();

        // Get Window Size
        mWindowWidthPx = 0;
        mWindowHeightPx = 0;
        addLayoutSizeChangeListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Get Display Rotation
        getDisplayRotation();

        // Get the Window size
        addLayoutSizeChangeListener();
    }

    // ============================================================= //

    private void addLayoutSizeChangeListener()
    {
        // For now, rely on SDL to get the Window size


//        // ref:
//        // http://stackoverflow.com/a/30469196
//        final ViewGroup rootLayout = mLayout;
//
//        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//
//                        int new_window_width_px = rootLayout.getWidth();
//                        int new_window_height_px = rootLayout.getHeight();
//
//                        if (new_window_width_px != mWindowWidthPx ||
//                                new_window_height_px != mWindowHeightPx) {
//                            if (Build.VERSION.SDK_INT >= 16) {
//                                rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                            } else {
//                                rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                            }
//
//                            mWindowWidthPx = rootLayout.getWidth();
//                            mWindowHeightPx = rootLayout.getHeight();
//
//                            jniOnWindowSizeChanged(
//                                    mWindowWidthPx,
//                                    mWindowHeightPx
//                            );
//                        }
//                    }
//                }
//        );
    }

    private void getDisplayInfo()
    {
        Display display = getWindowManager().getDefaultDisplay();

        if(Build.VERSION.SDK_INT >= 17) {
            mDisplayName = display.getName();

            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);

            mDisplayWidthPx = metrics.widthPixels;
            mDisplayHeightPx = metrics.heightPixels;

            mDisplayXDPI = metrics.xdpi;
            mDisplayYDPI = metrics.ydpi;
        }
        else {
            mDisplayName = Integer.toString(display.getDisplayId());

            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            // ref: http://stackoverflow.com/questions/2193457/...
            // ...is-there-a-way-to-determine-android-physical-screen-height-in-cm-or-inches
            // since SDK_INT = 1;
            mDisplayWidthPx = metrics.widthPixels;
            mDisplayHeightPx = metrics.heightPixels;

            // includes window decorations (statusbar bar/menu bar)
            if (Build.VERSION.SDK_INT >= 14)  {
                try  {
                    mDisplayWidthPx = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                    mDisplayHeightPx = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
                }
                catch (Exception ignored)
                { }
            }

            mDisplayXDPI = metrics.xdpi;
            mDisplayYDPI = metrics.ydpi;
        }

        getDisplayRotation();

        jniOnInitDisplayInfo(
                mDisplayName,
                mDisplayWidthPx,
                mDisplayHeightPx,
                mDisplayXDPI,
                mDisplayYDPI,
                mDisplayRotationCW);
    }

    private void getDisplayRotation()
    {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        final int rotation = rotationEnumToDegs(display.getRotation());
        int rotation_cw;
        if(rotation == 0) {
            rotation_cw = 0;
        }
        else {
            rotation_cw = 360-rotation;
        }

        if(rotation_cw != mDisplayRotationCW) {
            mDisplayRotationCW = rotation_cw;
            jniOnDisplayRotationChanged(mDisplayRotationCW);
        }
    }

    private int rotationEnumToDegs(int rotation_enum)
    {
        if(rotation_enum == Surface.ROTATION_0) {
            return 0;
        }
        else if(rotation_enum == Surface.ROTATION_90) {
            return 90;
        }
        else if(rotation_enum == Surface.ROTATION_180) {
            return 180;
        }

        return 270;
    }


    // ============================================================= //

    // Native Methods
    public native void jniOnInitDisplayInfo(String displayName,
                                            int displayWidthPx,
                                            int displayHeightPx,
                                            float displayXDPI,
                                            float displayYDPI,
                                            int displayRotationCW);

    public native void jniOnDisplayRotationChanged(int displayRotationCW);

//    public native void jniOnWindowSizeChanged(int windowWidthPx,
//                                              int windowHeightPx);

    // ============================================================= //

    // Display Info
    private String mDisplayName;
    private int mDisplayWidthPx;
    private int mDisplayHeightPx;
    private float mDisplayXDPI;
    private float mDisplayYDPI;
    private int mDisplayRotationCW; // 0,90,180,270

    // Window (root view) Info
    private int mWindowWidthPx;
    private int mWindowHeightPx;
}
