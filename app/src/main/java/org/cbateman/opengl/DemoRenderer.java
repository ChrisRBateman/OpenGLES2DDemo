package org.cbateman.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer class (registered with GLSurfaceView) that is responsible for making OpenGL
 * calls to render a frame.
 */
public class DemoRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = Constants.TAG;

    private Context mContext;
    private long mLastTime;
    private long mStartTime = -1;
    private int mWidth = 240, mHeight = 320;

    private EarthImage mEarthImage;
    private MoonImage mMoonImage;
    private StarsImage mStarsImage;
    private DirectionButton mDirectionButton;
    private SpeedButton mSpeedButton;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mScratch = new float[16];

    /**
     * FreeCellRenderer constructor.
     *
     * @param context the Context object
     */
    public DemoRenderer(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        mContext = context;

        Log.i(TAG, "DemoRenderer constructed");
    }

    /**
     * Touch event callback.
     *
     * @param e the MotionEvent object
     */
    public void onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int viewport[] = { 0, 0, mWidth, mHeight };
                if (mDirectionButton.handleTouch(e.getX(), e.getY(), mProjectionMatrix, viewport)) {
                    changeDirection();
                } else if (mSpeedButton.handleTouch(e.getX(), e.getY(), mProjectionMatrix, viewport)) {
                    changeSpeed();
                }
                break;
        }
    }

    /**
     * Clean up any resources used by renderer.
     */
    public void cleanUp() {

    }

    // GLSurfaceView.Renderer ----------------------------------------------------------------------

    /**
     * Called when the surface is created or recreated.
     *
     * @param gl the GL interface
     * @param config the EGLConfig of the created surface
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Called when the surface changed size.
     *
     * @param gl the GL interface
     * @param width the surface width
     * @param height the surface height
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // Set the projection matrix
        Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        mEarthImage = new EarthImage(mContext);
        mMoonImage = new MoonImage(mContext);
        mStarsImage = new StarsImage(mContext);
        mDirectionButton = new DirectionButton(mContext, width, height);
        mSpeedButton = new SpeedButton(mContext, width, height);
    }

    /**
     * Called to draw the current frame.
     *
     * @param gl the GL interface
     */
    public void onDrawFrame(GL10 gl) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mStartTime == -1) {
            mStartTime = SystemClock.uptimeMillis();
        }

        // Track time between frames
        final long time = SystemClock.uptimeMillis();
        final long timeDelta = time - mLastTime;
        final float timeDeltaSeconds = mLastTime > 0.0f ? timeDelta / 1000.0f : 0.0f;
        mLastTime = time;

        // Draw stars image
        mStarsImage.draw(mMVPMatrix);

        // Update moon position
        mMoonImage.update(timeDeltaSeconds);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mMoonImage.getX(), mMoonImage.getY(), 0.0f);
        Matrix.multiplyMM(mScratch, 0, mMVPMatrix, 0, mModelMatrix, 0);

        // Enable blending for alpha channel images
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        // Draw earth/moon images
        if (mMoonImage.getZOrder() > 0) {
            mEarthImage.draw(mMVPMatrix);
            mMoonImage.draw(mScratch);
        } else {
            mMoonImage.draw(mScratch);
            mEarthImage.draw(mMVPMatrix);
        }

        // Setup the button animation
        float elapsedTime = (time - mStartTime) / 1000.0f;
        float scaleValue = getButtonAnimationScaleValue(elapsedTime);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, scaleValue, scaleValue, 1.0f);

        // Draw direction button
        mDirectionButton.draw(mMVPMatrix, mModelMatrix);

        // Draw speed button
        mSpeedButton.draw(mMVPMatrix, mModelMatrix);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    // Private methods -----------------------------------------------------------------------------

    /**
     * Change moon direction.
     */
    private void changeDirection() {
        if (mMoonImage != null) {
            mMoonImage.changeDirection();
        }
    }

    /**
     * Change moon speed.
     */
    private void changeSpeed() {
        if (mMoonImage != null) {
            mMoonImage.changeSpeed();

            if (mMoonImage.getSpeed() == MoonImage.MIN_SPEED) {
                mSpeedButton.setCurrentImage(0);
            } else if (mMoonImage.getSpeed() == 2 * MoonImage.MIN_SPEED) {
                mSpeedButton.setCurrentImage(1);
            } else {
                mSpeedButton.setCurrentImage(2);
            }
        }
    }

    /**
     * Returns the current scale value.
     *
     * @param timeDeltaSeconds the time interval
     * @return the scale value
     */
    private float getButtonAnimationScaleValue(float timeDeltaSeconds) {
        float min = 1.0f - 0.04f;
        float max = 1.0f + 0.04f;
        float period = 0.5f;
        float phase = 0.0f;

        float amplitude = max - min;
        return (float)(min + amplitude *
                Math.sin(((timeDeltaSeconds / period) + phase) * 2 * Math.PI));
    }
}
