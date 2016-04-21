package org.cbateman.opengl;

import android.graphics.RectF;
import android.opengl.GLU;
import android.opengl.Matrix;

/**
 * Button class provides support for user input.
 */
public abstract class Button extends Image {

    protected RectF mBounds = new RectF();

    public Button() {
        super();
    }

    /**
     * Returns true if touch event handled.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param pMatrix projection matrix
     * @param viewport screen dimensions
     * @return true if button handles touch; otherwise false
     */
    public boolean handleTouch(float x, float y, float[] pMatrix, int[] viewport) {
        float[] output = { 0, 0, 0, 0 };
        float newY = (float)viewport[3] - y;
        final float[] mMatrix = new float[16];

        Matrix.setIdentityM(mMatrix, 0);
        GLU.gluUnProject(x, newY, 1, mMatrix, 0, pMatrix, 0, viewport, 0, output, 0);

        output[0] /= output[3];
        output[1] /= output[3];
        output[2] /= output[3];
        output[3] = 1;

        return GraphicUtils.pointInRect(output[0], output[1], mBounds);
    }
}
