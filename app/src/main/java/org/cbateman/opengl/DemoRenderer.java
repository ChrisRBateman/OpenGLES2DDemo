package org.cbateman.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import org.cbateman.opengl.text.gltext.GLText;

import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer class (registered with GLSurfaceView) that is responsible for making OpenGL
 * calls to render a frame.
 */
public class DemoRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = Constants.TAG;

    private Context mContext;
    private int mWidth = 240, mHeight = 320;

    private EarthImage mEarthImage;
    private MoonImage mMoonImage;
    private StarsImage mStarsImage;
    private DirectionButton mDirectionButton;
    private PlayButton mPlayButton;
    private SpeedButton mSpeedButton;

    // mPVMatrix is an abbreviation for "Projection View Matrix"
    private final float[] mPVMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mScratch = new float[16];

    private final TimeHelper mTimeHelper = new TimeHelper();

    private GLText mGLText;
    private final StringBuilder mSPFBuffer = new StringBuilder();
    private final DecimalFormat mSPFFormat = new DecimalFormat("0.####");
    private final float[] mTextPVMatrix = new float[16];

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

                } else if (mPlayButton.handleTouch(e.getX(), e.getY(), mProjectionMatrix, viewport)) {
                    togglePlay();

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
        mEarthImage.cleanup();
        mMoonImage.cleanup();
        mStarsImage.cleanup();

        mDirectionButton.cleanup();
        mPlayButton.cleanup();
        mSpeedButton.cleanup();

        mGLText.cleanUp();
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

        // Enable blending for alpha channel images
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
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
        Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        mEarthImage = new EarthImage(mContext);
        mMoonImage = new MoonImage(mContext);
        mStarsImage = new StarsImage(mContext);

        mDirectionButton = new DirectionButton(mContext, width, height);
        mPlayButton = new PlayButton(mContext, width, height);
        mSpeedButton = new SpeedButton(mContext, width, height);

        mTimeHelper.init();

        mGLText = new GLText(mContext.getAssets());
        mGLText.load("Roboto-Regular.ttf", 28, 2, 2);
        mSPFBuffer.setLength(0);

        // Set up a separate projection view for the text
        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        if (width > height) {
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        }
        else {
            Matrix.frustumM(projectionMatrix, 0, -1, 1, -1/ratio, 1/ratio, 1, 10);
        }
        float ext = Math.min(width, height) / 2;
        Matrix.orthoM(viewMatrix, 0, -ext, ext, -ext, ext, 0.1f, 100f);

        // Calculate the projection and view transformation for text
        Matrix.multiplyMM(mTextPVMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    /**
     * Called to draw the current frame.
     *
     * @param gl the GL interface
     */
    public void onDrawFrame(GL10 gl) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Update TimeHelper before calling any other methods from TimeHelper
        mTimeHelper.update();

        // Draw stars image
        mStarsImage.draw(mPVMatrix);

        float deltaTime = mTimeHelper.getDeltaTime();

        // Update moon position
        mMoonImage.update(deltaTime);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mMoonImage.getX(), mMoonImage.getY(), 0.0f);
        Matrix.multiplyMM(mScratch, 0, mPVMatrix, 0, mModelMatrix, 0);

        // Draw earth/moon images
        if (mMoonImage.getZOrder() > 0) {
            mEarthImage.draw(mPVMatrix);
            mMoonImage.draw(mScratch);
        } else {
            mMoonImage.draw(mScratch);
            mEarthImage.draw(mPVMatrix);
        }

        // Setup the button animation
        float elapsedTime = mTimeHelper.getAccumulatedTime();
        float scaleValue = getButtonAnimationScaleValue(elapsedTime);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, scaleValue, scaleValue, 1.0f);

        // Draw direction button
        mDirectionButton.draw(mPVMatrix, mModelMatrix);

        // Draw play button
        mPlayButton.draw(mPVMatrix, mModelMatrix);

        // Draw speed button
        mSpeedButton.draw(mPVMatrix, mModelMatrix);

        mSPFBuffer.setLength(0);
        mSPFBuffer.append(mSPFFormat.format(deltaTime));
        mSPFBuffer.append(" s/f");

        mGLText.begin(1.0f, 1.0f, 1.0f, 1.0f, mTextPVMatrix);
        mGLText.draw(mSPFBuffer.toString(), -mWidth / 2 + 2, mHeight / 2 - mGLText.getCharHeight());
        mGLText.end();
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
     * Play/pause moon animation.
     */
    private void togglePlay() {
        if (mMoonImage != null) {
            mMoonImage.setAnimationOn(!mMoonImage.isAnimating());
            if (mMoonImage.isAnimating()) {
                mPlayButton.setCurrentImage(1);
            } else {
                mPlayButton.setCurrentImage(0);
            }
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
