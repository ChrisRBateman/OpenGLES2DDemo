package org.cbateman.opengl;

import android.content.Context;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Clicking direction button changes direction of moon.
 */
@SuppressWarnings("WeakerAccess")
public class DirectionButton extends Button {

    private final float[] mTranslateMatrix = new float[16];
    private final float[] mIntermediate = new float[16];
    private final float[] mFinal = new float[16];

    public DirectionButton(Context context, int width, int height) {
        super();

        // Load direction texture.
        mTexId = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.direction));

        // Setup vertices data for direction image.
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
        Matrix.translateM(mTranslateMatrix, 0, (ratio - 0.15f), (-1.0f + 0.15f), 0.0f);

        final float[] vec1 = { verticesData[0], verticesData[1], verticesData[2], 1.0f };
        final float[] vec3 = { verticesData[10], verticesData[11], verticesData[12], 1.0f };
        final float[] res = new float[4];

        Matrix.multiplyMV(res, 0, mTranslateMatrix, 0, vec1, 0);
        mBounds.left = res[0];
        mBounds.top = res[1];

        Matrix.multiplyMV(res, 0, mTranslateMatrix, 0, vec3, 0);
        mBounds.right = res[0];
        mBounds.bottom = res[1];

        // Setup data after defining vertices and texture(s).
        setupData();

        Log.i(TAG, "DirectionButton constructed");
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
