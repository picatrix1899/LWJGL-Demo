package ColoredTriangleDemo;

import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
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
		
		int shaderProgramId = GL20.glCreateProgram();
		
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		GL20.glShaderSource(vertexShaderId, ""
				+ "#version 400\n"
				+ "in vec2 coords;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	gl_Position = vec4(vec3(coords, 1.0), 1.0);\n"
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
				+ "layout(location=0) out vec4 out_Color;\n"
				+ "uniform vec3 color;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	out_Color = vec4(color,1);\n"
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
		
		GL20.glLinkProgram(shaderProgramId);
		
		GL20.glValidateProgram(shaderProgramId);
		
		int colorUniformLocation = GL20.glGetUniformLocation(shaderProgramId, "color");
		
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
			
			/* =============
			 *  RENDER CODE
			 * =============*/
			
			// Binding the VAO that contains the vertex data of our triangle.
			GL30.glBindVertexArray(triangleVAOId);
			
			// Enabling the position pointer "0" for streaming the vertex data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(0);

			// Starting the shader.
			GL20.glUseProgram(shaderProgramId);
			
			// Setting the color uniform and therefore the color of the triangle.
			GL20.glUniform3f(colorUniformLocation, 1.0f, 0.0f, 0.0f);
			
			// Drawing the triangle.
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 3);
			
			// Stopping the running shader.
			GL20.glUseProgram(0);
		}
		
		/* ================
		 *  FREE RESOURCES
		 * ================ */
		
		GL30.glDeleteVertexArrays(triangleVAOId);
		GL30.glDeleteBuffers(triangleVertexVBOId);
		GL20.glDeleteProgram(shaderProgramId);
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);

		GLFW.glfwDestroyWindow(windowId);
	}
}
