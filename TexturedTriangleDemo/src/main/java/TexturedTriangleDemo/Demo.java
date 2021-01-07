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
import org.lwjgl.system.MemoryStack;
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
		
		// Generate the id for the virtual object called vertex array object (VAO) representing our triangle.
		// The VAO holds references to buffers containing vertex data like positions, texture coordinates, normals etc. but also
		// pointers to these informations, that will become important for seperatly sending the data to the vertex shader.
		int triangleVAOId = GL30.glGenVertexArrays();
		
		// Binding the VAO for setup.
		GL30.glBindVertexArray(triangleVAOId);
		
		// Generate an id for a buffer called vertex buffer object (VBO) that will be referenced by the VAO.
		// It will contain the vertex data.
		int triangleVertexVBOId = GL15.glGenBuffers();
		
		// Bind the VBO for setup.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, triangleVertexVBOId);
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			// Allocating a temporary float buffer and filling it with vertex data.
			// Here the data for a single vertex consists of 2 floats for position x, y and 2 floats for texture coordinates u and v.
			FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(3 * 4);
			vertexBuffer.put(new float[] {
				-0.5f, -0.5f, 0.0f, 0.0f,
				0.5f, -0.5f, 1.0f, 0.0f,
				0.0f, 0.5f, 0.5f, 1.0f});
			
			// Reset the insert/read position to 0 to allow reading from the beginning of the buffer.
			vertexBuffer.flip();
			
			// Put the vertex data safed in the temporary buffer into the VBO and telling
			// the VBO, that the buffered data will be "static" and therefore doesn't change.
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		}
		
		// Setting the pointer for position x, y in vertex data.
		// The stride is the amount of values per vertex in bytes.
		// In this case it's four float values á four bytes.
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
		
		// Setting the pointer for texture coordinates u and v in vertex data.
		// The stride is the amount of values per vertex in bytes.
		// In this case it's four float values á four bytes.
		// The pointer aka the offset is the amount of values before the current value range in bytes.
		// In this case the previous values where the positions consisting of two floats á four bytes.
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
		
		// Generate an id for the shader program.
		int shaderProgramId = GL20.glCreateProgram();
		
		// Generate an id for the vertex shader.
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		// Pass vertex shader source to the shader.
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
		
		// Compile the vertex shader and check for compilation errors.
		GL20.glCompileShader(vertexShaderId);
		if(GL20.glGetShaderi(vertexShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Vertex shader compilation error.");
			System.err.println(GL20.glGetShaderInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Generate an id for the fragment shader.
		int fragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		// Pass fragment shader source to the shader.
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

		// Compile the fragment shader and check for compilation errors.
		GL20.glCompileShader(fragmentShaderId);
		if(GL20.glGetShaderi(fragmentShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Fragment shader compilation error.");
			System.err.println(GL20.glGetShaderInfoLog(fragmentShaderId, 1000));
			System.exit(-1);
		}
		
		// Attach the vertex and fragment shader to the shader program.
		GL20.glAttachShader(shaderProgramId, vertexShaderId);
		GL20.glAttachShader(shaderProgramId, fragmentShaderId);
		
		// Bind the VAO pointer for vertex positions to the shader input variable "coords".
		GL20.glBindAttribLocation(shaderProgramId, 0, "coords");
		
		// Bind the VAO pointer for vertex texture coordinates to the shader input variable "texCoords".
		GL20.glBindAttribLocation(shaderProgramId, 1, "texCoords");
		
		// Link the shader program together and check for linking errors.
		GL20.glLinkProgram(shaderProgramId);
		if(GL20.glGetProgrami(shaderProgramId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Shader linking error.");
			System.err.println(GL20.glGetProgramInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Validate the shader program and check for validation errors.
		GL20.glValidateProgram(shaderProgramId);
		if(GL20.glGetProgrami(shaderProgramId, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Shader validation error.");
			System.err.println(GL20.glGetProgramInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Retrieve the locations for the uniform variables in the shader program to set their values later.
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
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

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

			/* =============
			 *  UPDATE CODE
			 * =============*/
			
			// For this demo there is no update code.
			
			/* ====================
			 * RENDER CODE
			 * ==================== */
			
			// Set the viewport for rendering to the dimensions of the window.
			// This is not neccessary as long as the only framebuffer been rendered to is the
			// framebuffer of the window itself. The moment you are rendering to multiple framebuffers
			// you have to set the viewport to the corresponding dimensions of the framebuffer you're gonna
			// rendering to.
			GL11.glViewport(0, 0, WIDTH, HEIGHT);
			
			// Setting the default color of the pixels that are not affected by the rendered objects.
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			
			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
			
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
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
			
			// Stopping the running shader.
			GL20.glUseProgram(0);
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		GL30.glDeleteVertexArrays(triangleVAOId);
		GL30.glDeleteBuffers(triangleVertexVBOId);
		
		// Deleting the shader program and the vertex and fragment shader.
		// Deleting the shader program does not automatically delete the attached shaders,
		// as they can be attached to multiple shader programs.
		GL20.glDeleteProgram(shaderProgramId);
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);
		
		GL11.glDeleteTextures(textureId);

		GLFW.glfwDestroyWindow(windowId);
	}
}
