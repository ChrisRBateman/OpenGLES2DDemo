package org.cbateman.opengl.text.gltext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class Vertices {

	//--Constants--//
	final static int POSITION_CNT_2D = 2;              // Number of Components in Vertex Position for 2D
	final static int POSITION_CNT_3D = 3;              // Number of Components in Vertex Position for 3D
	final static int COLOR_CNT = 4;                    // Number of Components in Vertex Color
	final static int TEXCOORD_CNT = 2;                 // Number of Components in Vertex Texture Coords
	final static int NORMAL_CNT = 3;                   // Number of Components in Vertex Normal
	private static final int MVP_MATRIX_INDEX_CNT = 1; // Number of Components in MVP matrix index

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;
	
	final static int INDEX_SIZE = Short.SIZE / 8;      // Index Byte Size (Short.SIZE = bits)
	
	private static final String TAG = "Vertices";

	//--Members--//
	// NOTE: all members are constant, and initialized in constructor!
	public final int positionCnt;                      // Number of Position Components (2=2D, 3=3D)
	public final int vertexStride;                     // Vertex Stride (Element Size of a Single Vertex)
	public final int vertexSize;                       // Bytesize of a Single Vertex
	final IntBuffer vertices;                          // Vertex Buffer
	final ShortBuffer indices;                         // Index Buffer
	public int numVertices;                            // Number of Vertices in Buffer
	public int numIndices;                             // Number of Indices in Buffer
	final int[] tmpBuffer;                             // Temp Buffer for Vertex Conversion
	private int mTextureCoordinateHandle;
	private int mPositionHandle;
	private int mMVPIndexHandle;

    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

	//--Constructor--//
	// D: create the vertices/indices as specified (for 2d/3d)
	// A: maxVertices - maximum vertices allowed in buffer
	//    maxIndices - maximum indices allowed in buffer
	public Vertices(int maxVertices, int maxIndices) {
		//      this.gl = gl;                                   // Save GL Instance
		this.positionCnt = POSITION_CNT_2D;             // Set Position Component Count
		this.vertexStride = this.positionCnt + TEXCOORD_CNT + MVP_MATRIX_INDEX_CNT;  // Calculate Vertex Stride
		this.vertexSize = this.vertexStride * 4;        // Calculate Vertex Byte Size

		ByteBuffer buffer = ByteBuffer.allocateDirect(maxVertices * vertexSize);  // Allocate Buffer for Vertices (Max)
		buffer.order(ByteOrder.nativeOrder());        // Set Native Byte Order
		this.vertices = buffer.asIntBuffer();           // Save Vertex Buffer

		if (maxIndices > 0)  {                        // IF Indices Required
			buffer = ByteBuffer.allocateDirect(maxIndices * INDEX_SIZE);  // Allocate Buffer for Indices (MAX)
			buffer.order(ByteOrder.nativeOrder());     // Set Native Byte Order
			this.indices = buffer.asShortBuffer();       // Save Index Buffer
		}
		else                                            // ELSE Indices Not Required
			indices = null;                              // No Index Buffer

		numVertices = 0;                                // Zero Vertices in Buffer
		numIndices = 0;                                 // Zero Indices in Buffer

		this.tmpBuffer = new int[maxVertices * vertexSize / 4];  // Create Temp Buffer

		// initialize the shader attribute handles
		mTextureCoordinateHandle = AttribVariable.A_TexCoordinate.getHandle();
		mMVPIndexHandle = AttribVariable.A_MVPMatrixIndex.getHandle();
		mPositionHandle = AttribVariable.A_Position.getHandle();
	}

	//--Set Vertices--//
	// D: set the specified vertices in the vertex buffer
	//    NOTE: optimized to use integer buffer!
	// A: vertices - array of vertices (floats) to set
	//    offset - offset to first vertex in array
	//    length - number of floats in the vertex array (total)
	//             for easy setting use: vtx_cnt * (this.vertexSize / 4)
	// R: [none]
	public void setVertices(float[] vertices, int offset, int length) {
		this.vertices.clear();                              // Remove Existing Vertices
		int last = offset + length;                         // Calculate Last Element
		for (int i = offset, j = 0; i < last; i++, j++)     // FOR Each Specified Vertex
			tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);  // Set Vertex as Raw Integer Bits in Buffer
		this.vertices.put(tmpBuffer, 0, length);            // Set New Vertices
		this.vertices.flip();                               // Flip Vertex Buffer
		this.numVertices = length / this.vertexStride;      // Save Number of Vertices
	}

	//--Set Indices--//
	// D: set the specified indices in the index buffer
	// A: indices - array of indices (shorts) to set
	//    offset - offset to first index in array
	//    length - number of indices in array (from offset)
	// R: [none]
	public void setIndices(short[] indices, int offset, int length) {
		this.indices.clear();                           // Clear Existing Indices
		this.indices.put(indices, offset, length);      // Set New Indices
		this.indices.flip();                            // Flip Index Buffer
		this.numIndices = length;                       // Save Number of Indices
	}

    /**
     * Set up vertex and index buffer objects.
     */
	public void setupData() {
        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * BYTES_PER_FLOAT,
                    null, GLES20.GL_DYNAMIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * BYTES_PER_SHORT,
                    indices, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    public void cleanUp() {
        if (vbo[0] > 0) {
            GLES20.glDeleteBuffers(vbo.length, vbo, 0);
            vbo[0] = 0;
        }

        if (ibo[0] > 0) {
            GLES20.glDeleteBuffers(ibo.length, ibo, 0);
            ibo[0] = 0;
        }
    }

	//--Bind--//
	// D: perform all required binding/state changes before rendering batches.
	//    USAGE: call once before calling draw() multiple times for this buffer.
	// A: [none]
	// R: [none]
	public void bind() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

        // vertices could change every frame so update the gpu memory.
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, numVertices * vertexSize, vertices);

        // bind vertex position pointer
        GLES20.glVertexAttribPointer(mPositionHandle,
                positionCnt, GLES20.GL_FLOAT, false, vertexSize, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // bind texture position pointer
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
                TEXCOORD_CNT, GLES20.GL_FLOAT, false, vertexSize, positionCnt * 4);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // bind MVP Matrix index position handle
        GLES20.glVertexAttribPointer(mMVPIndexHandle,
                MVP_MATRIX_INDEX_CNT, GLES20.GL_FLOAT, false, vertexSize, (positionCnt + TEXCOORD_CNT) * 4);
        GLES20.glEnableVertexAttribArray(mMVPIndexHandle);
	}

	//--Draw--//
	// D: draw the currently bound vertices in the vertex/index buffers
	//    USAGE: can only be called after calling bind() for this buffer.
	// A: primitiveType - the type of primitive to draw
	//    offset - the offset in the vertex/index buffer to start at
	//    numVertices - the number of vertices (indices) to draw
	// R: [none]
	public void draw(int primitiveType, int offset, int numVertices) {
		if (indices != null) {                          // IF Indices Exist
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(primitiveType, numVertices, GLES20.GL_UNSIGNED_SHORT, offset);
		}
		else {                                          // ELSE No Indices Exist
			//draw direct
			GLES20.glDrawArrays(primitiveType, offset, numVertices);
		}
	}

	//--Unbind--//
	// D: clear binding states when done rendering batches.
	//    USAGE: call once before calling draw() multiple times for this buffer.
	// A: [none]
	// R: [none]
	public void unbind() {
		GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
