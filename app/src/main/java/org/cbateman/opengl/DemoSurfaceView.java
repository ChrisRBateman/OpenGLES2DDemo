package org.cbateman.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Extends GLSurfaceView to handle touch events.
 */
public class DemoSurfaceView extends GLSurfaceView {

    private static final String TAG = Constants.TAG;

    private DemoRenderer mRenderer;

    public DemoSurfaceView(Context context) {
        super(context);

        init();
    }

    public DemoSurfaceView(Context context, AttributeSet attrs) {
        super (context, attrs);

        init();
    }

    /**
     * Sets the user's renderer.
     *
     * @param renderer the DemoRenderer
     */
    public void setRenderer(DemoRenderer renderer) {
        mRenderer = renderer;
        super.setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final MotionEvent event = MotionEvent.obtain(e);

        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.onTouchEvent(event);
            }
        });

        return true;
    }

    public void cleanUp() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.cleanUp();
            }
        });
    }

    /**
     * Initialize surface view.
     */
    private void init() {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        Log.i(TAG, "DemoSurfaceView initialized");
    }
}
