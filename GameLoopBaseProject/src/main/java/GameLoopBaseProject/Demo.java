package GameLoopBaseProject;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Demo
{

	public static int WIDTH = 800;
	public static int HEIGHT = 600;
	
	public static void main(String[] args)
	{
		new Demo();
	}

	public Demo()
	{
		GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.out);
		
		GLFW.glfwInit();
		
		GLFW.glfwSetErrorCallback(errorCallback);
		
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		
		long windowId = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Demo", 0, 0);
		
		if(windowId == 0)
		{
			System.exit(-1);
		}
		
		GLFW.glfwMakeContextCurrent(windowId);

		GL.createCapabilities();
		
		/* =========================
		 * INIT CODE
		 * ========================= */
		
		
		
		/* ========================= */
		
		GLFW.glfwShowWindow(windowId);
		
		boolean isRunning = true;
		while(isRunning)
		{
			GLFW.glfwPollEvents();
			GLFW.glfwSwapBuffers(windowId);
			
			if(GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) isRunning = false;
			if(GLFW.glfwWindowShouldClose(windowId)) isRunning = false;
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glClearColor(0, 0, 0, 1);
		
			/* ====================
			 * UPDATE CODE
			 * ==================== */

			
			
			
			/* ==================== */
			
			/* ====================
			 * RENDER CODE
			 * ==================== */
			
			/* ==================== */
		}
		
		/* =====================
		 * CLEANUP CODE
		 * ===================== */
		
		/* ===================== */
		
		GLFW.glfwDestroyWindow(windowId);
	}
	
}
