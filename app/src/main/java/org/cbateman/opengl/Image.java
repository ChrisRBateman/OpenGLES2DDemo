package org.cbateman.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Base class for all images.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Image {

    protected static final String TAG = Constants.TAG;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    protected int mTexId;
    protected int mProgram;
    protected int mPositionLocation;
    protected int mTexCoordLocation;
    protected int mSamplerLocation;
    protected int mMVPMatrixLocation;
    protected FloatBuffer mVertices;
    protected ShortBuffer mIndices;

    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

    public Image() {
    }

    /**
     * Setup resources.
     */
    protected void setupData() {
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

        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertices.capacity() * BYTES_PER_FLOAT,
                    mVertices, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices.capacity() * BYTES_PER_SHORT,
                    mIndices, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this image.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this image
     */
    public void draw(float[] mvpMatrix) {
        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glUseProgram(mProgram);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

            GLES20.glVertexAttribPointer(mPositionLocation, 3, GLES20.GL_FLOAT, false, 5 * 4, 0);
            GLES20.glEnableVertexAttribArray(mPositionLocation);

            GLES20.glVertexAttribPointer(mTexCoordLocation, 2, GLES20.GL_FLOAT, false, 5 * 4, 12);
            GLES20.glEnableVertexAttribArray(mTexCoordLocation);

            // Bind the texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexId);

            // Set the sampler texture unit to 0
            GLES20.glUniform1i(mSamplerLocation, 0);

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixLocation, 1, false, mvpMatrix, 0);

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    public void cleanup() {
        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(vbo.length, vbo, 0);
            vbo[0] = 0;
        }

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
            ibo[0] = 0;
        }
    }
}
