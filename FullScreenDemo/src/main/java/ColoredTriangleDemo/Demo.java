package ColoredTriangleDemo;

import java.nio.FloatBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeType;

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
			// Here the data for a single vertex consists of 2 floats for position x, y
			FloatBuffer vertexBuffer = stack.mallocFloat(3 * 2);
			vertexBuffer.put(new float[] {
				-0.5f, -0.5f,
				0.5f, -0.5f,
				0.0f,  0.5f});
			
			// Reset the insert/read position to 0 to allow reading from the beginning of the buffer.
			vertexBuffer.flip();
			
			// Put the vertex data safed in the temporary buffer into the VBO and telling
			// the VBO, that the buffered data will be "static" and therefore doesn't change.
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		}
		
		// Setting the pointer for position x, y in vertex data.
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
		
		// Generate an id for the shader program.
		int shaderProgramId = GL20.glCreateProgram();
		
		// Generate an id for the vertex shader.
		int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		
		// Pass vertex shader source to the shader.
		GL20.glShaderSource(vertexShaderId, ""
			+ "#version 400\n"
			+ "in vec2 coords;\n"
			+ "void main()\n"
			+ "{\n"
			+ "	gl_Position = vec4(vec3(coords, 1.0), 1.0);\n"
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
			+ "layout(location=0) out vec4 out_Color;\n"
			+ "void main()\n"
			+ "{\n"
			+ "	out_Color = vec4(1.0, 0.0, 0.0, 1.0);\n"
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
		
		/* ===========
		 *  MAIN LOOP
		 * =========== */
		
		// The value of this variable states that the window is currently in fullscreen mode.
		boolean fullscreen = false;
		
		// These variables contain the position of the window in windowed mode.
		// They are used for switching back from fullscreen mode to windowed mode.
		int posX = 0;
		int posY = 0;
		
		// These variables contain the size of the window in windowed mode.
		// They are used for switching back from fullscreen mode to windowed mode.
		int width = 0;
		int height = 0;
		
		// The value of this variable states that the "Q"-Key is hold down.
		boolean isButtonHold = false;
		
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
			
			// If the "Q"-key got pressed, toggle between fullscreen and windowed.
			if(GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS && !isButtonHold)
			{
				if(fullscreen)
				{
					GLFW.glfwSetWindowMonitor(windowId, 0, posX, posY, width, height, GLFW.GLFW_DONT_CARE);
					
					GL11.glViewport(0, 0, width,  height);
				}
				else
				{
					int[] posXA = new int[1];
					int[] posYA = new int[1];
					
					GLFW.glfwGetWindowPos(windowId, posXA, posYA);

					posX = posXA[0];
					posY = posYA[0];
					
					int[] widthA = new int[1];
					int[] heightA = new int[1];
					
					GLFW.glfwGetWindowSize(windowId, widthA, heightA);
					
					width = widthA[0];
					height = heightA[0];
					
					long currentMonitor = glfwGetCurrentMonitor(windowId);
					
					GLFWVidMode mode = GLFW.glfwGetVideoMode(currentMonitor);
			        
			        int monitorWidth = mode.width();
			        int monitorHeight = mode.height();

					GLFW.glfwSetWindowMonitor(windowId, currentMonitor, 0, 0, monitorWidth, monitorHeight, GLFW.GLFW_DONT_CARE);
					
					GL11.glViewport(0, 0, monitorWidth,  monitorHeight);
				}
				
				fullscreen = !fullscreen;
				
				isButtonHold = true;
			}
			
			if(GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_Q) == GLFW.GLFW_RELEASE && isButtonHold)
			{
				isButtonHold = false;
			}
			
			/* =============
			 *  RENDER CODE
			 * =============*/
			
			// Setting the default color of the pixels that are not affected by the rendered objects.
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			
			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

			// Binding the VAO that contains the vertex data of our triangle.
			GL30.glBindVertexArray(triangleVAOId);
			
			// Enabling the position pointer "0" for streaming the vertex data of the triangle to gpu.
			GL20.glEnableVertexAttribArray(0);

			// Starting the shader.
			GL20.glUseProgram(shaderProgramId);
			
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
	
	/** Determines the current monitor that the specified window is being displayed on.
	 * If the monitor could not be determined, the primary monitor will be returned.
	 * 
	 * From
	 * <a href="https://stackoverflow.com/questions/21421074/how-to-create-a-full-screen-window-on-the-current-monitor-with-glfw">
	 * StackOverflow: How to create a full screen window on the current monitor with GLFW"
	 * </a><br>
	 * @param window The window to query
	 * @return The current monitor on which the window is being displayed, or the primary monitor if one could not be determined
	 * 
	 * @author <a href="https://stackoverflow.com/a/31526753/2398263">Shmo</a><br>
	 * @author Brian_Entei
	 * @author picatrix1899
	 */
	@NativeType("GLFWmonitor *")
	public long glfwGetCurrentMonitor(long window)
	{
	    int[] windowPosX = new int[1];
	    int[] windowPosY = new int[1];
	    
	    
	    GLFW.glfwGetWindowPos(window, windowPosX, windowPosY);
	    
	    int[] windowWidth = new int[1];
	    int[] windowHeight = new int[1];
	    
	    GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
	    
	    PointerBuffer monitors = GLFW.glfwGetMonitors();

	    long bestmonitor = GLFW.glfwGetPrimaryMonitor();
	    
	    while(monitors.hasRemaining())
	    {
	        long monitor = monitors.get();
	        
	        GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
	        
	        int monitorWidth = mode.width();
	        int monitorHeight = mode.height();
	        
	        int[] monitorPosX = new int[1];
	        int[] monitorPosY = new int[1];
	        
	        GLFW.glfwGetMonitorPos(monitor, monitorPosX, monitorPosY);
	        
	        int bestoverlap = 0;

	        int overlap = Math.max(0, Math.min(windowPosX[0] + windowWidth[0], monitorPosX[0] + monitorWidth) - Math.max(windowPosX[0], monitorPosX[0])) *
	                Math.max(0, Math.min(windowPosY[0] + windowHeight[0], monitorPosY[0] + monitorHeight) - Math.max(windowPosY[0], monitorPosY[0]));

	        if (bestoverlap < overlap) {
	            bestoverlap = overlap;
	            bestmonitor = monitor;
	        }
	    }

	    return bestmonitor;
	}
}
