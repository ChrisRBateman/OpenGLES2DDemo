package org.cbateman.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Base class for all images.
 */
public abstract class Image {

    protected static final String TAG = Constants.TAG;

    protected int mTexId;
    protected int mProgram;
    protected int mPositionLocation;
    protected int mTexCoordLocation;
    protected int mSamplerLocation;
    protected int mMVPMatrixLocation;
    protected FloatBuffer mVertices;
    protected ShortBuffer mIndices;

    public Image() {
        final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main() {" +
            "    gl_Position = uMVPMatrix * aPosition;" +
            "    vTexCoord = aTexCoord;" +
            "}";

        final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "    gl_FragColor = texture2D(sTexture, vTexCoord);" +
            "}";
        // Create program from shaders
        mProgram = GraphicUtils.loadProgram(vertexShaderCode, fragmentShaderCode);

        // Get locations
        mPositionLocation = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexCoordLocation = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mSamplerLocation = GLES20.glGetUniformLocation(mProgram, "sTexture");
        mMVPMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        final short[] indicesData = {
                0, 1, 2, 0, 2, 3
        };
        mIndices = ByteBuffer.allocateDirect(indicesData.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(indicesData).position(0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this image.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this image
     */
    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        // Load the vertex position
        mVertices.position(0);
        GLES20.glVertexAttribPointer(mPositionLocation, 3, GLES20.GL_FLOAT,
                false,
                5 * 4, mVertices);
        // Load the texture coordinate
        mVertices.position(3);
        GLES20.glVertexAttribPointer(mTexCoordLocation, 2, GLES20.GL_FLOAT,
                false,
                5 * 4,
                mVertices);

        GLES20.glEnableVertexAttribArray(mPositionLocation);
        GLES20.glEnableVertexAttribArray(mTexCoordLocation);

        // Bind the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexId);

        // Set the sampler texture unit to 0
        GLES20.glUniform1i(mSamplerLocation, 0);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixLocation, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionLocation);
        GLES20.glDisableVertexAttribArray(mTexCoordLocation);
    }
}
