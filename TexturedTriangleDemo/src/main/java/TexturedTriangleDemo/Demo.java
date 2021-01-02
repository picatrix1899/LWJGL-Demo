package TexturedTriangleDemo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

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
		
		int triangleVAOId = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(triangleVAOId);

		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(3 * 2);
		vertexBuffer.put(new float[] {-0.5f, -0.5f, 0.5f, -0.5f, 0.0f,  0.5f});
		vertexBuffer.flip();
		
		GL30.glBindVertexArray(triangleVAOId);
		int triangleVertexVBOId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, triangleVertexVBOId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

		MemoryUtil.memFree(vertexBuffer);
		
		FloatBuffer texCoordBuffer = MemoryUtil.memAllocFloat(3 * 2);
		texCoordBuffer.put(new float[] {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f});
		texCoordBuffer.flip();
		
		GL30.glBindVertexArray(triangleVAOId);
		int triangleTexCoordVBOId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, triangleTexCoordVBOId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		
		MemoryUtil.memFree(texCoordBuffer);
		
		int shaderProgramId = GL20.glCreateProgram();
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		
		GL20.glShaderSource(vertexShaderId, ""
				+ "#version 400\n"
				+ "in vec2 coords;\n"
				+ "in vec2 texCoords;\n"
				+ "out vec2 pass_texCoords;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	gl_Position = vec4(vec3(coords, 1.0), 1.0);\n"
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
			
			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
			
			// Setting the default color of the pixels that are not affected by the rendered objects.
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
			/* =============
			 *  UPDATE CODE
			 * =============*/
			
			// For this demo there is no update code.
			
			/* ====================
			 * RENDER CODE
			 * ==================== */
			
			// Binding the VAO that contains the vertex data of our triangle.
			GL30.glBindVertexArray(triangleVAOId);
			
			// Enabling the position pointer "0" for streaming the vertex data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(0);
			
			// Enabling the position pointer "1" for streaming the texture coordinate data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(1);
			
			// Starting the shader.
			GL20.glUseProgram(shaderProgramId);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL32.GL_TEXTURE_2D, textureId);
			GL20.glUniform1i(textureUniformLocation, 0);
			
			// Drawing the triangle.
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 3);
			
			// Stopping the running shader.
			GL20.glUseProgram(0);
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		GL30.glDeleteVertexArrays(triangleVAOId);
		GL30.glDeleteBuffers(triangleVertexVBOId);
		GL30.glDeleteBuffers(triangleTexCoordVBOId);
		GL20.glDeleteProgram(shaderProgramId);
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);
		GL11.glDeleteTextures(textureId);

		GLFW.glfwDestroyWindow(windowId);
	}
}
