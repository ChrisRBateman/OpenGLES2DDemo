package org.cbateman.opengl;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Clicking play button starts and stops moon.
 */
public class PlayButton extends Button {

    private int[] mTexIdArray = new int[3];
    private final float[] mTranslateMatrix = new float[16];
    private final float[] mIntermediate = new float[16];
    private final float[] mFinal = new float[16];

    public PlayButton(Context context, int width, int height) {
        super();

        // Load speed textures.
        mTexIdArray[0] = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.play));
        mTexIdArray[1] = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.pause));
        mTexId = mTexIdArray[0];

        // Setup vertices data for speed image.
        final float[] verticesData = {
            -0.07f, 0.07f, 0.0f, // Position 0
            0.0f, 0.0f, // TexCoord 0
            -0.07f, -0.07f, 0.0f, // Position 1
            0.0f, 1.0f, // TexCoord 1
            0.07f, -0.07f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 2
            0.07f, 0.07f, 0.0f, // Position 3
            1.0f, 0.0f // TexCoord 3
        };

        mVertices = ByteBuffer.allocateDirect(verticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(verticesData).position(0);

        float ratio = (float) width / height;
        Matrix.setIdentityM(mTranslateMatrix, 0);
        Matrix.translateM(mTranslateMatrix, 0, 0.0f, (-1.0f + 0.15f), 0.0f);

        final float[] vec1 = { verticesData[0], verticesData[1], verticesData[2], 1.0f };
        final float[] vec3 = { verticesData[10], verticesData[11], verticesData[12], 1.0f };
        final float[] res = new float[4];

        Matrix.multiplyMV(res, 0, mTranslateMatrix, 0, vec1, 0);
        mBounds.left = res[0];
        mBounds.top = res[1];

        Matrix.multiplyMV(res, 0, mTranslateMatrix, 0, vec3, 0);
        mBounds.right = res[0];
        mBounds.bottom = res[1];

        Log.i(TAG, "PlayButton constructed");
    }

    /**
     * Set the current image.
     *
     * @param index the index of image
     */
    public void setCurrentImage(int index) {
        if ((index >= 0) && (index < mTexIdArray.length)) {
            mTexId = mTexIdArray[index];
        }
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this image.
     *
     * @param mvpMatrix the Model View Project matrix in which to draw
     * this image
     * @param scaleMatrix the Scaling matrix
     */
    public void draw(float[] mvpMatrix, float[] scaleMatrix) {
        // Translate then scale
        Matrix.multiplyMM(mIntermediate, 0, mvpMatrix, 0, mTranslateMatrix, 0);
        Matrix.multiplyMM(mFinal, 0, mIntermediate, 0, scaleMatrix, 0);
        super.draw(mFinal);
    }
}
