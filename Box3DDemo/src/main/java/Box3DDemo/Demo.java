package Box3DDemo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

/**
 * This demo shows how to render 3-dimensional boxes with perspective.
 * It uses the joml-library for calculations.
 * 
 * The box vertices and therefore the idea to have the vertex positions and texture coordinates in a single vbo was taken from
 * the learnopengl.com tutorial <a href="https://learnopengl.com/Getting-started/Coordinate-Systems">Coordinate Systems</a>.
 * 
 * @author Joey de Vries
 * @author picatrix1899
 */
public class Demo
{
	// The initial width of the window.
	public static int WIDTH = 800;
	
	// The initial height of the window.
	public static int HEIGHT = 600;
	
	public static void main(String[] args) { new Demo().run(); }

	public void run()
	{
		/* ============
		 *  SETUP CODE
		 * ============ */
		
		/* ------------
		 *  GLFW Setup
		 * ------------ */
		
		// Creating an error callback to print occuring errors with GLFW to the console.
		GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.out);
		
		// Initializing GLFW.
		GLFW.glfwInit();
		
		// Setting the error callback.
		GLFW.glfwSetErrorCallback(errorCallback);
		
		/* --------------
		 *  Window Setup
		 * -------------- */
		
		// Defining important properties for the OpenGL context of the window.
		
		// Defining the minimum OpenGL version required to run.
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		
		// Defining the OpenGL profile. This is usually "CORE".
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		
		// Creating the window with the initial width and height and the title "Demo".
		long windowId = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Demo", 0, 0);
		
		// If the window id or window handle is "0" the creation of the window failed.
		if(windowId == 0)
		{
			System.err.println("Cannot create window.");
			System.exit(-1);
		}
			
		// Setting the OpenGL Context (contains all the resources like textures, shaders etc.) of the window as current to use.
		GLFW.glfwMakeContextCurrent(windowId);

		// Creating the OpenGL capabilities for this window.
		GL.createCapabilities();
		
		/* ===========
		 *  INIT CODE
		 * =========== */
		
		int boxVAOId = GL30.glGenVertexArrays();

		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(36 * 5);
		vertexBuffer.put(new float[] {
			        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
			         0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
			         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
			         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
			        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
			        -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

			        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
			         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
			         0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
			         0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
			        -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
			        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

			        -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
			        -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
			        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
			        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
			        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
			        -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

			         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
			         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
			         0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
			         0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
			         0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
			         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

			        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
			         0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
			         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
			         0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
			        -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
			        -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

			        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
			         0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
			         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
			         0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
			        -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
			        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f});
		vertexBuffer.flip();
		
		GL30.glBindVertexArray(boxVAOId);
		
		int boxVertexVBOId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, boxVertexVBOId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * 4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * 4, 3 * 4);
		
		MemoryUtil.memFree(vertexBuffer);

		int shaderProgramId = GL20.glCreateProgram();
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		
		GL20.glShaderSource(vertexShaderId, ""
				+ "#version 400\n"
				+ "in vec3 coords;\n"
				+ "in vec2 texCoords;\n"
				+ "out vec2 pass_texCoords;\n"
				+ "uniform mat4 T_projection;\n"
				+ "uniform mat4 T_view;\n"
				+ "uniform mat4 T_model;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	gl_Position = T_projection * T_view * T_model * vec4(coords, 1.0);\n"
				+ "	pass_texCoords = texCoords;\n"
				+ "}"
				);
		GL20.glCompileShader(vertexShaderId);
		
		if(GL20.glGetShaderi(vertexShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		int fragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(fragmentShaderId, ""
				+ "#version 400\n"
				+ "in vec2 pass_texCoords;\n"
				+ "layout(location=0) out vec4 out_Color;\n"
				+ "uniform sampler2D diffuse;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	out_Color = texture(diffuse, pass_texCoords);\n"
				+ "}"
				);

		GL20.glCompileShader(fragmentShaderId);

		if(GL20.glGetShaderi(fragmentShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(fragmentShaderId, 1000));
			System.exit(-1);
		}
		
		GL20.glAttachShader(shaderProgramId, vertexShaderId);
		GL20.glAttachShader(shaderProgramId, fragmentShaderId);
		
		GL20.glBindAttribLocation(shaderProgramId, 0, "coords");
		GL20.glBindAttribLocation(shaderProgramId, 1, "texCoords");
		
		GL20.glLinkProgram(shaderProgramId);
		
		GL20.glValidateProgram(shaderProgramId);
		
		int textureUniformLocation = GL20.glGetUniformLocation(shaderProgramId, "diffuse");
		int projectionMatrixUniformLocation = GL20.glGetUniformLocation(shaderProgramId, "T_projection");
		int viewMatrixUniformLocation = GL20.glGetUniformLocation(shaderProgramId, "T_view");
		int modelMatrixUniformLocation = GL20.glGetUniformLocation(shaderProgramId, "T_model");
		
		BufferedImage texture = null;
		try
		{
			texture = ImageIO.read(new File("test.png"));
		} catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		int textureWidth = texture.getWidth();
		int textureHeight = texture.getHeight();
		
		int[] pixels = new int[textureWidth * textureHeight];
		texture.getRGB(0, 0, textureWidth, textureHeight, pixels, 0, textureWidth);

		int textureId = GL11.glGenTextures();

		ByteBuffer buffer = MemoryUtil.memAlloc(textureWidth * textureHeight * 4);

		for(int y = 0; y < textureHeight; y++)
			for(int x = 0; x < textureWidth; x++)
			{
				int pixel = pixels[y * textureWidth + x];
				
				buffer.put((byte)((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte)((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte)(pixel & 0xFF)); // Blue component
				buffer.put((byte)((pixel >> 24) & 0xFF)); //Alpha component
			}

		buffer.flip();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, textureWidth, textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);		
		
		MemoryUtil.memFree(buffer);

		Vector3f[] cubePositions = new Vector3f[] {
		        new Vector3f( 0.0f,  0.0f,  0.0f),
		        new Vector3f( 2.0f,  5.0f, -15.0f),
		        new Vector3f(-1.5f, -2.2f, -2.5f),
		        new Vector3f(-3.8f, -2.0f, -12.3f),
		        new Vector3f( 2.4f, -0.4f, -3.5f),
		        new Vector3f(-1.7f,  3.0f, -7.5f),
		        new Vector3f( 1.3f, -2.0f, -2.5f),
		        new Vector3f( 1.5f,  2.0f, -2.5f),
		        new Vector3f( 1.5f,  0.2f, -1.5f),
		        new Vector3f(-1.3f,  1.0f, -1.5f)
		    };
		

		FloatBuffer matrixCarrierBuffer = MemoryUtil.memAllocFloat(16);
		
		Matrix4f projectionMatrix = new Matrix4f().setPerspective((float)Math.toRadians(45.0), (float)WIDTH / (float)HEIGHT, 0.1f, 100.0f);

		GL20.glUseProgram(shaderProgramId);
		
		matrixCarrierBuffer.clear();
		GL20.glUniformMatrix4fv(projectionMatrixUniformLocation, false, projectionMatrix.get(matrixCarrierBuffer));

		GL20.glUseProgram(0);
		

		
		/* ===========
		 *  MAIN LOOP
		 * =========== */
		
		boolean isRunning = true;
		while(isRunning)
		{
			// Poll all occuring events of the window. This includes keyboard inputs, mouse inputs and window close demands aswell. 
			GLFW.glfwPollEvents();
			
			// There are usually two buffers. one front and one back buffer. If you are rendering to the screen you are always rendering
			// to the back buffer. With this you are switching the place of both buffers to show the scene you just rendered. 
			GLFW.glfwSwapBuffers(windowId);
			
			// If the "ESC"-key got pressed the demo program should close.
			if(GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) isRunning = false;
			
			// If the window is demanded of closing by pressing the "X" icon the demo program should close.
			if(GLFW.glfwWindowShouldClose(windowId)) isRunning = false;
			
			GL11.glViewport(0, 0, WIDTH, HEIGHT);
			
			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			// Setting the default color of the pixels that are not affected by the rendered objects.
			GL11.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		
			/* =============
			 *  UPDATE CODE
			 * =============*/
			
			Matrix4f viewMatrix = new Matrix4f().translation(0.0f, 0.0f, -3.0f);

			/* ====================
			 * RENDER CODE
			 * ==================== */
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			// Binding the VAO that contains the vertex data of our triangle.
			GL30.glBindVertexArray(boxVAOId);
			
			// Enabling the position pointer "0" for streaming the vertex data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(0);
			
			// Enabling the position pointer "1" for streaming the texture coordinate data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(1);
			
			// Starting the shader.
			GL20.glUseProgram(shaderProgramId);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL32.GL_TEXTURE_2D, textureId);
			GL20.glUniform1i(textureUniformLocation, 0);
			
			matrixCarrierBuffer.clear();
			GL20.glUniformMatrix4fv(viewMatrixUniformLocation, false, viewMatrix.get(matrixCarrierBuffer));

			for(int i = 0; i < cubePositions.length; i++)
			{
				Matrix4f modelMatrix = new Matrix4f().translation(cubePositions[i]);
				
				matrixCarrierBuffer.clear();
				GL20.glUniformMatrix4fv(modelMatrixUniformLocation, false, modelMatrix.get(matrixCarrierBuffer));
					
				// Drawing the box.
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);
			}
				
			// Stopping the running shader.
			GL20.glUseProgram(0);
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		MemoryUtil.memFree(matrixCarrierBuffer);
		
		GL30.glDeleteVertexArrays(boxVAOId);
		GL30.glDeleteBuffers(boxVertexVBOId);
		GL20.glDeleteProgram(shaderProgramId);
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);
		GL11.glDeleteTextures(textureId);

		GLFW.glfwDestroyWindow(windowId);
	}
}