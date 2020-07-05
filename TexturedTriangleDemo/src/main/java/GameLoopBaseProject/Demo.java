package GameLoopBaseProject;

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

		FloatBuffer buff1 = MemoryUtil.memAllocFloat(3 * 3);
		buff1.put(new float[] {-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f,  0.5f, 0.0f});
		buff1.flip();
		
		GL30.glBindVertexArray(scrPlaneVAOId);
		int scrPlaneVBO1Id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scrPlaneVBO1Id);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buff1, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

		MemoryUtil.memFree(buff1);
		
		FloatBuffer buff2 = MemoryUtil.memAllocFloat(3 * 2);
		buff2.put(new float[] {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f});
		buff2.flip();
		
		GL30.glBindVertexArray(scrPlaneVAOId);
		int scrPlaneVBO2Id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scrPlaneVBO2Id);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buff2, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		
		MemoryUtil.memFree(buff2);
		
		int msShader_prgId = GL20.glCreateProgram();
		int msShader_vsId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		int msShader_fsId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(msShader_vsId, ""
				+ "#version 400\n"
				+ "in vec3 coords;\n"
				+ "in vec2 texCoords;\n"
				+ "out vec2 pass_texCoords;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	gl_Position = vec4(coords, 1.0);\n"
				+ "	pass_texCoords = texCoords;\n"
				+ "}"
				);
		
		GL20.glShaderSource(msShader_fsId, ""
				+ "#version 400\n"
				+ "in vec2 pass_texCoords;\n"
				+ "layout(location=0) out vec4 out_Color;\n"
				+ "uniform sampler2D diffuse;\n"
				+ "void main()\n"
				+ "{\n"
				+ "	out_Color = texture(diffuse, pass_texCoords);\n"
				+ "}"
				);
		
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
		GL20.glBindAttribLocation(msShader_prgId, 1, "texCoords");
		
		GL20.glLinkProgram(msShader_prgId);
		
		GL20.glValidateProgram(msShader_prgId);
		
		int textureUniformLocation = GL20.glGetUniformLocation(msShader_prgId, "diffuse");
		
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
			GL20.glEnableVertexAttribArray(1);
			
			GL20.glUseProgram(msShader_prgId);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL32.GL_TEXTURE_2D, textureId);
			GL20.glUniform1i(textureUniformLocation, 0);
			
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 3);
			
			/* ==================== */
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		GL30.glDeleteVertexArrays(scrPlaneVAOId);
		GL30.glDeleteBuffers(scrPlaneVBO1Id);
		GL30.glDeleteBuffers(scrPlaneVBO2Id);
		GL20.glDeleteProgram(msShader_prgId);
		GL20.glDeleteShader(msShader_vsId);
		GL20.glDeleteShader(msShader_fsId);
		GL11.glDeleteTextures(textureId);
		
		
		/* ===================== */
		
		
		GLFW.glfwDestroyWindow(windowId);
	}
}
