package org.cbateman.opengl;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Renders a background image of stars.
 */
public class StarsImage extends Image {

    /**
     * StarsImage constructor.
     *
     * @param context interface to resources
     */
    public StarsImage(Context context) {
        super();

        mTexId = GraphicUtils.loadTexture(context.getResources().openRawResource(R.raw.stars));

        // Setup vertices data for stars.
        final float[] verticesData = {
            -1.0f, 1.0f, 0.0f, // Position 0
            0.0f, 0.0f, // TexCoord 0
            -1.0f, -1.0f, 0.0f, // Position 1
            0.0f, 1.0f, // TexCoord 1
            1.0f, -1.0f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 2
            1.0f, 1.0f, 0.0f, // Position 3
            1.0f, 0.0f // TexCoord 3
        };

        mVertices = ByteBuffer.allocateDirect(verticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(verticesData).position(0);

        Log.i(TAG, "StarsImage constructed");
    }
}
