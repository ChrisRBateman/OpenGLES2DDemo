package org.cbateman.opengl;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * MainActivity class. The starting activity for the 2D demo.
 */
public class MainActivity extends Activity {

    private static final String TAG = Constants.TAG;

    private DemoSurfaceView mDemoSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDemoSurfaceView = new DemoSurfaceView(this);
        DemoRenderer demoRenderer = new DemoRenderer(this);

        mDemoSurfaceView.setRenderer(demoRenderer);
        setContentView(mDemoSurfaceView);

        Log.i(TAG, "MainActivity created");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDemoSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDemoSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDemoSurfaceView.cleanUp();

        Log.i(TAG, "MainActivity destroyed");
    }
}
