package org.cbateman.opengl;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Renders the earth image.
 */
public class EarthImage extends Image {

    /**
     * EarthImage constructor.
     *
     * @param context interface to resources
     */
    public EarthImage(Context context) {
        mTexId = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.earth));

        // Setup vertices data for earth.
        final float[] verticesData = {
            -0.3f, 0.3f, 0.0f, // Position 0
            0.0f, 0.0f, // TexCoord 0
            -0.3f, -0.3f, 0.0f, // Position 1
            0.0f, 1.0f, // TexCoord 1
            0.3f, -0.3f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 2
            0.3f, 0.3f, 0.0f, // Position 3
            1.0f, 0.0f // TexCoord 3
        };

        mVertices = ByteBuffer.allocateDirect(verticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(verticesData).position(0);

        Log.i(TAG, "EarthImage constructed");
    }
}
