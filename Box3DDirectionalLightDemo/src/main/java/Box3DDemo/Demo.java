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
		
		// Generate the id for the virtual object called vertex array object (VAO) representing our box.
		// The VAO holds buffers containing vertex data like positions, texture coordinates, normals etc. but also
		// pointers to these informations that will become important for seperatly sending the data to the vertex shader.
		int boxVAOId = GL30.glGenVertexArrays();

		// Allocating a temporary float buffer and filling it with vertex data.
		// Here the data for a single vertex consists of 3 floats for position x, y and z,
		// 3 floats for normal x, y and z and 2 floats for texture coordinates u and v.
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(36 * 8);
		vertexBuffer.put(new float[] {
			-0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
	         0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  0.0f,
	         0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
	         0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
	        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  1.0f,
	        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,

	        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,
	         0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,
	         0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
	         0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
	        -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
	        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,

	        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
	        -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
	        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
	        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
	        -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
	        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

	         0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
	         0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
	         0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
	         0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
	         0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
	         0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

	        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,
	         0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  1.0f,
	         0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
	         0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
	        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  0.0f,
	        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,

	        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f,
	         0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  1.0f,
	         0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
	         0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
	        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,
	        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f});
		vertexBuffer.flip();
		
		// Binding the VAO for setup.
		GL30.glBindVertexArray(boxVAOId);
		
		// Generate an id for a buffer called vertex buffer object (VBO) that will be referenced by the VAO.
		// It will contain the vertex data.
		int boxVertexVBOId = GL15.glGenBuffers();
		
		// Bind the VBO for setup.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, boxVertexVBOId);
		
		// Put the vertex data safted in the temporary buffer into the VBO and telling
		// the VBO, that the buffered data will be "static" and therefore doesn't change.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		
		// Setting the pointer for position x, y and z in vertex data.
		// The stride is the amount of values per vertex in bytes.
		// In this case it's eight float values á four bytes.
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
		
		// Setting the pointer for normal x, y and z in vertex data.
		// The stride is the amount of values per vertex in bytes.
		// In this case it's eight float values á four bytes.
		// The pointer aka the offset is the amount of values before the current value range in bytes.
		// In this case the previous values was the position consisting of three floats á four bytes.
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
		
		// Setting the pointer for texture coordinates u and v in vertex data.
		// The stride is the amount of values per vertex in bytes.
		// In this case it's eight float values á four bytes.
		// The pointer aka the offset is the amount of values before the current value range in bytes.
		// In this case the previous values where the position and the normal consisting of six floats á four bytes.
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);
		
		// Unbinding the VAO after setup to not mess itself or other VAOs up.
		// This is not really necessary and should be seen as a safety messurement.
		GL30.glBindVertexArray(0);
		
		// deallocating the temporary buffer for the vertex data.
		MemoryUtil.memFree(vertexBuffer);

		// Generate an id for the shader program.
		int ambientLightShaderProgramId = GL20.glCreateProgram();
		
		// Generate an id for the vertex shader.
		int ambientLightVertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		
		// Pass vertex shader source to the shader.
		GL20.glShaderSource(ambientLightVertexShaderId, ""
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
		
		// Compile the vertex shader and check for errors.
		GL20.glCompileShader(ambientLightVertexShaderId);
		if(GL20.glGetShaderi(ambientLightVertexShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(ambientLightVertexShaderId, 1000));
			System.exit(-1);
		}
		
		// Generate an id for the fragment shader.
		int ambientLightFragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		// Pass fragment shader source to the shader.
		GL20.glShaderSource(ambientLightFragmentShaderId, ""
			+ "#version 400\n"
			+ "struct BaseLight {\n"
			+ " vec3 color;\n"
			+ " float intensity;\n"
			+ "};\n"
			+ "in vec2 pass_texCoords;\n"
			+ "layout(location=0) out vec4 out_Color;\n"
			+ "uniform BaseLight ambientLight;\n"
			+ "uniform sampler2D diffuse;\n"
			+ "void main()\n"
			+ "{\n"
			+ "	out_Color = texture(diffuse, pass_texCoords) * (vec4(ambientLight.color, 1.0) * ambientLight.intensity);\n"
			+ "}"
			);

		// Compile the fragment shader and check for errors.
		GL20.glCompileShader(ambientLightFragmentShaderId);
		if(GL20.glGetShaderi(ambientLightFragmentShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(ambientLightFragmentShaderId, 1000));
			System.exit(-1);
		}
		
		// Attach the vertex and fragment shader to the shader program.
		GL20.glAttachShader(ambientLightShaderProgramId, ambientLightVertexShaderId);
		GL20.glAttachShader(ambientLightShaderProgramId, ambientLightFragmentShaderId);
		
		// Bind the VAO pointer for vertex positions to the shader input variable "coords".
		GL20.glBindAttribLocation(ambientLightShaderProgramId, 0, "coords");
		
		// Bind the VAO pointer for vertex texture coordinates to the shader input variable "texCoords".
		GL20.glBindAttribLocation(ambientLightShaderProgramId, 2, "texCoords");
		
		// Link the shader program together.
		GL20.glLinkProgram(ambientLightShaderProgramId);
		
		// Validate the shader program.
		GL20.glValidateProgram(ambientLightShaderProgramId);
		
		// Retrieve the locations for the uniform variables in the shader program to set their values later.
		int ambientLightTextureUniformLocation = GL20.glGetUniformLocation(ambientLightShaderProgramId, "diffuse");
		int ambientLightProjectionMatrixUniformLocation = GL20.glGetUniformLocation(ambientLightShaderProgramId, "T_projection");
		int ambientLightViewMatrixUniformLocation = GL20.glGetUniformLocation(ambientLightShaderProgramId, "T_view");
		int ambientLightModelMatrixUniformLocation = GL20.glGetUniformLocation(ambientLightShaderProgramId, "T_model");
		int ambientLightColorUniformLocation =  GL20.glGetUniformLocation(ambientLightShaderProgramId, "ambientLight.color");
		int ambientLightIntensityUniformLocation =  GL20.glGetUniformLocation(ambientLightShaderProgramId, "ambientLight.intensity");
		
		// Generate an id for the shader program.
		int directionalLightShaderProgramId = GL20.glCreateProgram();
		
		// Generate an id for the vertex shader.
		int directionalLightVertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		
		// Pass vertex shader source to the shader.
		GL20.glShaderSource(directionalLightVertexShaderId, ""
			+ "#version 400\n"
			+ "struct Camera {\n"
			+ " vec3 position;\n"
			+ " mat4 T_view;\n"
			+ "};\n"
			+ "in vec3 coords;\n"
			+ "in vec2 texCoords;\n"
			+ "in vec3 normal;\n"
			+ "out vec2 pass_texCoords;\n"
			+ "out Camera pass_camera;\n"
			+ "out vec3 pass_normal;\n"
			+ "uniform mat4 T_projection;\n"
			+ "uniform mat4 T_model;\n"
			+ "uniform Camera camera;\n"
			+ "void main()\n"
			+ "{\n"
			+ "	gl_Position = T_projection * camera.T_view * T_model * vec4(coords, 1.0);\n"
			+ "	pass_texCoords = texCoords;\n"
			+ " pass_camera = camera;\n"
			+ " pass_normal = mat3(transpose(inverse(T_model))) * normal;\n"
			+ "}"
			);
				
		// Compile the vertex shader and check for errors.
		GL20.glCompileShader(directionalLightVertexShaderId);
		if(GL20.glGetShaderi(directionalLightVertexShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(directionalLightVertexShaderId, 1000));
			System.exit(-1);
		}
				
		// Generate an id for the fragment shader.
		int directionalLightFragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		// Pass fragment shader source to the shader.
		GL20.glShaderSource(directionalLightFragmentShaderId, ""
			+ "#version 400\n"
			+ "struct Camera {\n"
			+ " vec3 position;\n"
			+ " mat4 T_view;\n"
			+ "};\n"
			+ "struct BaseLight {\n"
			+ " vec3 color;\n"
			+ " float intensity;\n"
			+ "};\n"
			+ "struct DirectionalLight {\n"
			+ " BaseLight base;\n"
			+ " vec3 direction;\n"
			+ "};\n"
			+ "in vec2 pass_texCoords;\n"
			+ "in Camera pass_camera;\n"
			+ "in vec3 pass_normal;\n"
			+ "layout(location=0) out vec4 out_Color;\n"
			+ "uniform sampler2D diffuse;\n"
			+ "uniform DirectionalLight directionalLight;\n"
			+ "void main()\n"
			+ "{\n"
			+ " vec3 nrm = normalize(pass_normal);\n"
			+ " vec3 dirToLight = normalize(-directionalLight.direction);\n"
			+ " float diffuseFactor = max(dot(nrm, dirToLight), 0.0f);\n"
			+ "	out_Color = texture(diffuse, pass_texCoords) * (vec4(directionalLight.base.color, 1.0) * directionalLight.base.intensity * diffuseFactor);\n"
			+ "}"
			);

		// Compile the fragment shader and check for errors.
		GL20.glCompileShader(directionalLightFragmentShaderId);
		if(GL20.glGetShaderi(directionalLightFragmentShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println(GL20.glGetShaderInfoLog(directionalLightFragmentShaderId, 1000));
			System.exit(-1);
		}
				
		// Attach the vertex and fragment shader to the shader program.
		GL20.glAttachShader(directionalLightShaderProgramId, directionalLightVertexShaderId);
		GL20.glAttachShader(directionalLightShaderProgramId, directionalLightFragmentShaderId);
			
		// Bind the VAO pointer for vertex positions to the shader input variable "coords".
		GL20.glBindAttribLocation(directionalLightShaderProgramId, 0, "coords");
		
		// Bind the VAO pointer for vertex normals to the shader input variable "normal".
		GL20.glBindAttribLocation(directionalLightShaderProgramId, 1, "normal");
		
		// Bind the VAO pointer for vertex texture coordinates to the shader input variable "texCoords".
		GL20.glBindAttribLocation(directionalLightShaderProgramId, 2, "texCoords");
		
		// Link the shader program together.
		GL20.glLinkProgram(directionalLightShaderProgramId);
		
		// Validate the shader program.
		GL20.glValidateProgram(directionalLightShaderProgramId);
		
		// Retrieve the locations for the uniform variables in the shader program to set their values later.
		int directionalLightTextureUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "diffuse");
		int directionalLightProjectionMatrixUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "T_projection");
		int directionalLightCameraPositionUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "camera.position");
		int directionalLightCameraViewMatrixUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "camera.T_view");
		int directionalLightModelMatrixUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "T_model");
		int directionalLightColorUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "directionalLight.base.color");
		int directionalLightIntensityUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "directionalLight.base.intensity");
		int directionalLightDirectionUniformLocation = GL20.glGetUniformLocation(directionalLightShaderProgramId, "directionalLight.direction");
		
		// Read the pixel data of an image manually.
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
		
		int textureId = GL11.glGenTextures();
		
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

		Vector3f ambientLightColor = new Vector3f(1.0f, 1.0f, 1.0f);
		float ambientLightIntensity = 0.3f;

		Vector3f directionalLightColor = new Vector3f(1.0f, 1.0f, 1.0f);
		float directionalLightIntensity = 0.8f;
		Vector3f directionalLightDirection = new Vector3f(1.0f, -1.0f, 0.0f);
		
		GL20.glUseProgram(ambientLightShaderProgramId);
		
		matrixCarrierBuffer.clear();
		GL20.glUniformMatrix4fv(ambientLightProjectionMatrixUniformLocation, false, projectionMatrix.get(matrixCarrierBuffer));

		GL20.glUniform3f(ambientLightColorUniformLocation, ambientLightColor.x, ambientLightColor.y, ambientLightColor.z);
		GL20.glUniform1f(ambientLightIntensityUniformLocation, ambientLightIntensity);
		
		GL20.glUseProgram(directionalLightShaderProgramId);
		
		matrixCarrierBuffer.clear();
		GL20.glUniformMatrix4fv(directionalLightProjectionMatrixUniformLocation, false, projectionMatrix.get(matrixCarrierBuffer));

		GL20.glUniform3f(directionalLightColorUniformLocation, directionalLightColor.x, directionalLightColor.y, directionalLightColor.z);
		GL20.glUniform1f(directionalLightIntensityUniformLocation, directionalLightIntensity);
		GL20.glUniform3f(directionalLightDirectionUniformLocation, directionalLightDirection.x, directionalLightDirection.y, directionalLightDirection.z);
		
		GL20.glUseProgram(0);
		
		// Setting the default color of the pixels that are not affected by the rendered objects.
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
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
			
			Vector3f cameraPosition = new Vector3f(0.0f, 0.0f, 3.0f);
			Matrix4f viewMatrix = new Matrix4f().translation(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

			/* ====================
			 * RENDER CODE
			 * ==================== */

			// Clearing the pixeldata, the depthdata and the stencildata of the screen.
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LESS);
			
			// Binding the VAO that contains the vertex data of our triangle.
			GL30.glBindVertexArray(boxVAOId);
			
			// Enabling the position pointer "0" for passing the vertex positions to gpu.
			GL20.glEnableVertexAttribArray(0);
			
			// The ambient light shader does not use the normals.
			// However disabling the respective pointer is usually not neccessery.
			GL20.glDisableVertexAttribArray(1);
			
			// Enabling the position pointer "1" for passing the vertex texture coordinates to gpu.
			GL20.glEnableVertexAttribArray(2);
			
			// Starting the shader.
			GL20.glUseProgram(ambientLightShaderProgramId);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL32.GL_TEXTURE_2D, textureId);
			GL20.glUniform1i(ambientLightTextureUniformLocation, 0);
			
			matrixCarrierBuffer.clear();
			GL20.glUniformMatrix4fv(ambientLightViewMatrixUniformLocation, false, viewMatrix.get(matrixCarrierBuffer));

			for(int i = 0; i < cubePositions.length; i++)
			{
				Matrix4f modelMatrix = new Matrix4f().translation(cubePositions[i]);
				
				matrixCarrierBuffer.clear();
				GL20.glUniformMatrix4fv(ambientLightModelMatrixUniformLocation, false, modelMatrix.get(matrixCarrierBuffer));
					
				// Drawing the box.
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);
			}
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			
			// Enabling the position pointer "0" for passing the vertex positions to gpu.
			GL20.glEnableVertexAttribArray(0);
			
			// Enabling the position pointer "1" for passing the vertex normals to gpu.
			GL20.glEnableVertexAttribArray(1);
			
			// Enabling the position pointer "1" for passing the vertex texture coordinates to gpu.
			GL20.glEnableVertexAttribArray(2);
			
			// Starting the shader.
			GL20.glUseProgram(directionalLightShaderProgramId);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL32.GL_TEXTURE_2D, textureId);
			GL20.glUniform1i(directionalLightTextureUniformLocation, 0);
			
			matrixCarrierBuffer.clear();
			GL20.glUniform3f(directionalLightCameraPositionUniformLocation, cameraPosition.x, cameraPosition.y, cameraPosition.z);
			GL20.glUniformMatrix4fv(directionalLightCameraViewMatrixUniformLocation, false, viewMatrix.get(matrixCarrierBuffer));

			for(int i = 0; i < cubePositions.length; i++)
			{
				Matrix4f modelMatrix = new Matrix4f().translation(cubePositions[i]);
				
				matrixCarrierBuffer.clear();
				GL20.glUniformMatrix4fv(directionalLightModelMatrixUniformLocation, false, modelMatrix.get(matrixCarrierBuffer));
					
				// Drawing the box.
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36);
			}
			
			// Stopping the running shader.
			GL20.glUseProgram(0);
			
			GL11.glDisable(GL11.GL_BLEND);
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
		
		/* =====================
		 * FREE RESOURCES
		 * ===================== */
		
		MemoryUtil.memFree(matrixCarrierBuffer);
		
		GL30.glDeleteVertexArrays(boxVAOId);
		
		GL30.glDeleteBuffers(boxVertexVBOId);
		
		GL20.glDeleteProgram(ambientLightShaderProgramId);
		GL20.glDeleteShader(ambientLightVertexShaderId);
		GL20.glDeleteShader(ambientLightFragmentShaderId);
		
		GL20.glDeleteProgram(directionalLightShaderProgramId);
		GL20.glDeleteShader(directionalLightVertexShaderId);
		GL20.glDeleteShader(directionalLightFragmentShaderId);
		
		GL11.glDeleteTextures(textureId);

		GLFW.glfwDestroyWindow(windowId);
	}
}
