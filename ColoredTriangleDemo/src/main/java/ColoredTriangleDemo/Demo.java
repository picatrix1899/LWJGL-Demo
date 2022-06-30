package ColoredTriangleDemo;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

/**
 * This demo shows how to render a 2d triangle with a in the fragment shader predefined color.
 * 
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
		glfwInit();
		
		// Setting the error callback.
		glfwSetErrorCallback(errorCallback);
		
		/* --------------
		 *  Window Setup
		 * -------------- */
		
		// Defining important properties for the OpenGL context of the window.
		
		// Defining the minimum OpenGL version required to run.
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		
		// Defining the OpenGL profile. This is usually "CORE".
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		
		// Creating the window with the initial width and height and the title "Demo".
		long windowId = glfwCreateWindow(WIDTH, HEIGHT, "Demo", 0, 0);
		
		// If the window id or window handle is "0" the creation of the window failed.
		if(windowId == 0)
		{
			System.err.println("Cannot create window.");
			System.exit(-1);
		}
		
		// Setting the OpenGL Context (contains all the resources like textures, shaders etc.) of the window as current to use.
		glfwMakeContextCurrent(windowId);
		
		// Creating the OpenGL capabilities for this window.
		GL.createCapabilities();
		
		/* ===========
		 *  INIT CODE
		 * =========== */
		
		// Generate the id for the virtual object called vertex array object (VAO) representing our triangle.
		// The VAO holds references to buffers containing vertex data like positions, texture coordinates, normals etc. but also
		// pointers to these informations, that will become important for seperatly sending the data to the vertex shader.
		int triangleVAOId = glCreateVertexArrays();
		
		// Generate an id for a buffer called vertex buffer object (VBO) that will be referenced by the VAO.
		// It will contain the vertex data.
		int triangleVertexVBOId = glCreateBuffers();
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			// Allocating a temporary float buffer and filling it with vertex data.
			// Here the data for a single vertex consists of 2 floats for position x, y.
			FloatBuffer vertexBuffer = stack.mallocFloat(3 * 2);
			vertexBuffer.put(new float[] {
				-0.5f, -0.5f,
				0.5f, -0.5f,
				0.0f,  0.5f});
			
			// Reset the insert/read position to 0 to allow reading from the beginning of the buffer.
			vertexBuffer.flip();
			
			// Put the vertex data safed in the temporary buffer into the VBO and telling
			// the VBO, that the buffered data will be "static" and therefore doesn't change.
			glNamedBufferData(triangleVertexVBOId, vertexBuffer, GL_STATIC_DRAW);
		}
		
		// Binding the VAO for setup.
		glBindVertexArray(triangleVAOId);
		
		// Bind the VBO for setup.
		glBindBuffer(GL_ARRAY_BUFFER, triangleVertexVBOId);
		
		glBindVertexBuffer(0, triangleVertexVBOId, 0, 2 * 4);
		
		// Enabling the position pointer "0" for streaming the vertex data of the triangle to gpu.
		glEnableVertexAttribArray(0);
		
		glVertexArrayAttribFormat(triangleVAOId, 0, 2, GL_FLOAT, false, 0);
		glVertexArrayAttribBinding(triangleVAOId, 0, 0);
		
		// Generate an id for the shader program.
		int shaderProgramId = glCreateProgram();
		
		// Generate an id for the vertex shader.
		int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
		
		// Pass vertex shader source to the shader.
		glShaderSource(vertexShaderId, ""
			+ "#version 400\n"
			+ "in vec2 coords;\n"
			+ "void main()\n"
			+ "{\n"
			+ "	gl_Position = vec4(vec3(coords, 1.0), 1.0);\n"
			+ "}"
			);
		
		// Compile the vertex shader and check for compilation errors.
		glCompileShader(vertexShaderId);
		if(glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE)
		{
			System.err.println(glGetShaderInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Generate an id for the fragment shader.
		int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
		
		// Pass fragment shader source to the shader.
		glShaderSource(fragmentShaderId, ""
			+ "#version 400\n"
			+ "layout(location=0) out vec4 out_Color;\n"
			+ "void main()\n"
			+ "{\n"
			+ " vec3 color = vec3(1.0, 0.0, 0.0);\n"
			+ "	out_Color = vec4(color, 1.0);\n"
			+ "}"
			);
		
		// Compile the fragment shader and check for compilation errors.
		glCompileShader(fragmentShaderId);
		if(glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE)
		{
			System.err.println(glGetShaderInfoLog(fragmentShaderId, 1000));
			System.exit(-1);
		}
		
		// Attach the vertex and fragment shader to the shader program.
		glAttachShader(shaderProgramId, vertexShaderId);
		glAttachShader(shaderProgramId, fragmentShaderId);
		
		// Bind the VAO pointer for vertex positions to the shader input variable "coords".
		glBindAttribLocation(shaderProgramId, 0, "coords");
		
		// Link the shader program together and check for linking errors.
		glLinkProgram(shaderProgramId);
		if(glGetProgrami(shaderProgramId, GL_LINK_STATUS) == GL_FALSE)
		{
			System.err.println(glGetProgramInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Validate the shader program and check for validation errors.
		glValidateProgram(shaderProgramId);
		if(glGetProgrami(shaderProgramId, GL_VALIDATE_STATUS) == GL_FALSE)
		{
			System.err.println(glGetProgramInfoLog(vertexShaderId, 1000));
			System.exit(-1);
		}
		
		/* ===========
		 *  MAIN LOOP
		 * =========== */
		
		boolean isRunning = true;
		while(isRunning)
		{
			// Poll all occuring events of the window. This includes keyboard inputs, mouse inputs and window close demands aswell. 
			glfwPollEvents();
			
			// There are usually two buffers. one front and one back buffer. If you are rendering to the screen you are always rendering
			// to the back buffer. With this you are switching the place of both buffers to show the scene you just rendered. 
			glfwSwapBuffers(windowId);
			
			// If the "ESC"-key got pressed the demo program should close.
			if(glfwGetKey(windowId, GLFW_KEY_ESCAPE) == GLFW_PRESS) isRunning = false;
			
			// If the window is demanded of closing by pressing the "X" icon the demo program should close.
			if(glfwWindowShouldClose(windowId)) isRunning = false;
			
			/* =============
			 *  UPDATE CODE
			 * =============*/
			
			// For this demo there is no update code.
			
			/* =============
			 *  RENDER CODE
			 * =============*/
			
			// Set the viewport for rendering to the dimensions of the window.
			// This is not neccessary as long as the only framebuffer been rendered to is the
			// framebuffer of the window itself. The moment you are rendering to multiple framebuffers
			// you have to set the viewport to the corresponding dimensions of the framebuffer you're gonna
			// rendering to.
			glViewport(0, 0, WIDTH, HEIGHT);
			
			// Setting the default color of the pixels that are not affected by the rendered objects.
			glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			
			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			
			// Binding the VAO that contains the vertex data of our triangle.
			glBindVertexArray(triangleVAOId);
			
			// Starting the shader.
			glUseProgram(shaderProgramId);
			
			// Drawing the triangle.
			glDrawArrays(GL_TRIANGLES, 0, 3);
			
			// Stopping the running shader.
			glUseProgram(0);
		}
		
		/* ================
		 *  FREE RESOURCES
		 * ================ */
		
		glDeleteVertexArrays(triangleVAOId);
		glDeleteBuffers(triangleVertexVBOId);
		
		// Deleting the shader program and the vertex and fragment shader.
		// Deleting the shader program does not automatically delete the attached shaders,
		// as they can be attached to multiple shader programs.
		glDeleteProgram(shaderProgramId);
		glDeleteShader(vertexShaderId);
		glDeleteShader(fragmentShaderId);
		
		glfwDestroyWindow(windowId);
		glfwTerminate();
	}
}