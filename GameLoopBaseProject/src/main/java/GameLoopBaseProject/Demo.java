package GameLoopBaseProject;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

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
		
		// TODO: Enter code here...
		
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
		
			/* ====================
			 * UPDATE CODE
			 * ==================== */

			// TODO: Enter code here...
			
			/* ====================
			 * RENDER CODE
			 * ==================== */
			
			// TODO: Enter code here...
		}
		
		/* ================
		 *  FREE RESOURCES
		 * ================ */
		
		// TODO: Enter code here...
		
		GLFW.glfwDestroyWindow(windowId);
	}
}
