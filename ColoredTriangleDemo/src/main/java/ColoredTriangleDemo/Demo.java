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
		
		int scrPlaneVAOId = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(scrPlaneVAOId);

		FloatBuffer buff1 = MemoryUtil.memAllocFloat(4 * 3);
		buff1.put(new float[] {-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f,  0.5f, 0.0f});

		buff1.flip();
		
		GL30.glBindVertexArray(scrPlaneVAOId);
		int scrPlaneVBOId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scrPlaneVBOId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buff1, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

		MemoryUtil.memFree(buff1);
		
		int msShader_prgId = GL20.glCreateProgram();
		int msShader_vsId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		int msShader_fsId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(msShader_vsId, """
				#version 400
				
				in vec3 coords;

				void main()
				{
					gl_Position = vec4(coords, 1.0);
				}
				""");
		
		GL20.glShaderSource(msShader_fsId, """
				#version 400

				layout(location=0) out vec4 out_Color;
				
				void main()
				{
					out_Color = vec4(1,0,0,1);
				}
				""");
		
		GL20.glCompileShader(msShader_vsId);
		GL20.glCompileShader(msShader_fsId);

		if(GL20.glGetShaderi(msShader_vsId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			try
			{
				throw new Exception(GL20.glGetShaderInfoLog(msShader_vsId, 500));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if(GL20.glGetShaderi(msShader_fsId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			try
			{
				throw new Exception(GL20.glGetShaderInfoLog(msShader_fsId, 500));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		GL20.glAttachShader(msShader_prgId, msShader_vsId);
		GL20.glAttachShader(msShader_prgId, msShader_fsId);
		
		GL20.glBindAttribLocation(msShader_prgId, 0, "coords");
		
		GL20.glLinkProgram(msShader_prgId);
		
		GL20.glValidateProgram(msShader_prgId);
		
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
			
			GL30.glBindVertexArray(scrPlaneVAOId);
			GL20.glEnableVertexAttribArray(0);

			GL20.glUseProgram(msShader_prgId);
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			
			/* ==================== */
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		GL30.glDeleteVertexArrays(scrPlaneVAOId);
		GL30.glDeleteBuffers(scrPlaneVBOId);
		GL20.glDeleteProgram(msShader_prgId);
		GL20.glDeleteShader(msShader_vsId);
		GL20.glDeleteShader(msShader_fsId);
		
		/* ===================== */
		
		
		GLFW.glfwDestroyWindow(windowId);
	}
}
