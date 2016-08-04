package org.cbateman.opengl;

import android.util.Log;

/**
 * TimeHelper class provides timing services for rendering frames in the
 * GLSurfaceView.Renderer class.
 */
public class TimeHelper {

    private static final String TAG = Constants.TAG;

    // Store start and previous times
    private long mStartTime = -1;
    private long mLastTime = -1;

    // Used for frame rate smoothing
    private static final float NANOS_PER_SECOND = 1000000000.0f;
    private static final int RECENT_TIME_DELTA_COUNT = 5;
    private float mRecentTimeDelta[] = new float[RECENT_TIME_DELTA_COUNT];
    private int mRecentTimeDeltaNext = -1;
    private float mDeltaSec = 0;

    public TimeHelper() {
        Log.i(TAG, "TimeHelper constructed");
    }

    /**
     * Initialize the class.
     */
    public void init() {
        mStartTime = System.nanoTime();
    }

    /**
     * Update timer for current frame
     */
    public void update() {
        long time = System.nanoTime();

        // Last time not established so skip calculations until next frame
        if (mLastTime == -1) {
            mDeltaSec = 0.0f;
            mLastTime = time;
            return;
        }

        float timeDeltaSeconds = (time - mLastTime) / NANOS_PER_SECOND;

        if (mRecentTimeDeltaNext < 0) {
            for (int i = 0; i < RECENT_TIME_DELTA_COUNT; i++) {
                mRecentTimeDelta[i] = timeDeltaSeconds;
            }
            mRecentTimeDeltaNext = 0;
        }

        mRecentTimeDelta[mRecentTimeDeltaNext] = timeDeltaSeconds;
        mRecentTimeDeltaNext = (mRecentTimeDeltaNext + 1) % RECENT_TIME_DELTA_COUNT;

        mDeltaSec = 0.0f;
        for (int i = 0; i < RECENT_TIME_DELTA_COUNT; i++) {
            mDeltaSec += mRecentTimeDelta[i];
        }

        mLastTime = time;
    }

    /**
     * Returns the accumulated time (in seconds) since initialization.
     * Call update() before calling this method.
     *
     * @return accumulated time (in seconds)
     */
    public float getAccumulatedTime() {
        return (mLastTime - mStartTime) / NANOS_PER_SECOND;
    }

    /**
     * Returns the delta time (in seconds) between update calls.
     * Call update() before calling this method.
     *
     * @return delta time (in seconds) between frame calls
     */
    public float getDeltaTime() {
        return mDeltaSec / RECENT_TIME_DELTA_COUNT;
    }
}
