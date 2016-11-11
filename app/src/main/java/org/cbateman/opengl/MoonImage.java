package org.cbateman.opengl;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Renders the moon image.
 */
public class MoonImage extends Image {

    public static final float MIN_SPEED = 0.20f;

    private static final float MAX_X = 0.4f;
    private static final float MIN_X = -0.4f;

    private float x = 0.4f;
    private float y = -0.4f;
    private int directionX = 1;
    private int directionY = -1;
    private float speed = MIN_SPEED;
    private int zOrder = 1;
    private boolean isAnimating = false;

    /**
     * MoonImage constructor. Classes that extend Image just need to create a texture and setup
     * vertices data.
     *
     * @param context interface to resources
     */
    public MoonImage(Context context) {
        super();

        // Load moon texture.
        mTexId = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.moon));

        // Setup vertices data for moon image.
        final float[] verticesData = {
            -0.1f, 0.1f, 0.0f, // Position 0
            0.0f, 0.0f, // TexCoord 0
            -0.1f, -0.1f, 0.0f, // Position 1
            0.0f, 1.0f, // TexCoord 1
            0.1f, -0.1f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 2
            0.1f, 0.1f, 0.0f, // Position 3
            1.0f, 0.0f // TexCoord 3
        };

        mVertices = ByteBuffer.allocateDirect(verticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(verticesData).position(0);

        // Setup data after defining vertices and texture(s).
        setupData();

        Log.i(TAG, "MoonImage constructed");
    }

    /**
     * Update image location.
     *
     * @param timeDeltaSeconds time delta
     */
    public void update(float timeDeltaSeconds) {
        if (isAnimating) {
            x = x + (directionX * speed * timeDeltaSeconds);
            y = y + (directionY * speed * timeDeltaSeconds);

            if ((x > MAX_X) || (x < MIN_X)) {
                directionX = -directionX;
                directionY = -directionY;
                zOrder = -zOrder;
                x = Math.max(-0.4f, Math.min(x, 0.4f));
                y = Math.max(-0.4f, Math.min(y, 0.4f));
            }
        }
    }

    /**
     * Returns the desired z order.
     *
     * @return desired z order
     */
    public int getZOrder() {
        return zOrder;
    }

    /**
     * Returns the speed.
     *
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Returns x coordinate.
     *
     * @return x coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Returns y coordinate.
     *
     * @return y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Change moon direction.
     */
    public void changeDirection() {
        if ((MIN_X <= x) && (x <= MAX_X)) {
            directionX = -directionX;
            directionY = -directionY;
        }
    }

    /**
     * Change moon speed.
     */
    public void changeSpeed() {
        if ((MIN_X <= x) && (x <= MAX_X))
            if (speed == MIN_SPEED) {
                speed = 2 * MIN_SPEED;
            } else if (speed == 2 * MIN_SPEED) {
                speed = 3 * MIN_SPEED;
            } else {
                speed = MIN_SPEED;
        }
    }

    /**
     * Returns state of animation.
     *
     * @return state of animation
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Turn animation on/off
     * .
     * @param setOn animation on/off flag
     */
    public void setAnimationOn(boolean setOn) {
        isAnimating = setOn;
    }
}
