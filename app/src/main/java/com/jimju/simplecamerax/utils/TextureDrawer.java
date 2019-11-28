package com.jimju.simplecamerax.utils;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class TextureDrawer {
    public FloatBuffer buffer;
    private int mOESTextureId = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;
    public int shaderProgram = -1;

    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;


    private float[] vertexData = new float[]{
            1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f,
            -1f, -1f, 0f, 0f,
            1f, 1f, 1f, 1f,
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f};

    private String POSITION_ATTRIBUTE = "aPosition";
    private String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    private String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    private String  TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    private String VERTEX_SHADER = "attribute vec4 aPosition;\n" +
            "uniform mat4 uTextureMatrix;\n" +
            "attribute vec4 aTextureCoordinate;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main()\n" +
            "{\n" +
            "  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;\n" +
            "  gl_Position = aPosition;\n" +
            "}";

    private String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES uTextureSampler;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main()\n" +
            "{\n" +
            "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +
            "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +
            "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +
            "}\n";


    public static int createOESTextureObject() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }


    public TextureDrawer(int OESTextureId) {
        this.mOESTextureId = OESTextureId;
        buffer = createBuffer(vertexData);
        vertexShader = loadShader(GL_VERTEX_SHADER,VERTEX_SHADER);
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        shaderProgram = linkProgram(vertexShader, fragmentShader);
    }

    private FloatBuffer createBuffer(float[] vertexData){
        FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(vertexData,0,vertexData.length).position(0);
        return buffer;
    }

    //shader:着色程序
    private int loadShader(int type,String shaderSource){
        int shader = glCreateShader(type);
        if (shader == 0){
            throw new RuntimeException("Create Shader Failed!" + glGetError());
        }
        glShaderSource(shader,shaderSource);
        glCompileShader(shader);
        return shader;
    }

    private int linkProgram(int verShader,int fragShader){
        int program = glCreateProgram();
        if (program == 0){
            throw new RuntimeException("Create Program Failed!" + glGetError());
        }
        glAttachShader(program,verShader);
        glAttachShader(program,fragShader);
        glLinkProgram(program);
        glUseProgram(program);
        return program;
    }

    public void drawTexture(float[] transformMatrix){
        aPositionLocation = glGetAttribLocation(shaderProgram,POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(shaderProgram,TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(shaderProgram,TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(shaderProgram,TEXTURE_SAMPLER_UNIFORM);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mOESTextureId);
        glUniform1i(uTextureSamplerLocation,0);
        glUniformMatrix4fv(uTextureMatrixLocation,1,false,transformMatrix,0);
        if (buffer != null){
            buffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation,2,GL_FLOAT,false,16,buffer);

            buffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation,2,GL_FLOAT,false,16,buffer);
            glDrawArrays(GL_TRIANGLES,0,6);
        }
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public int getShaderProgram() {
        return shaderProgram;
    }
}
