#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    color.r = color.r * 0.8;
    color.g = color.g * 0.59;
    color.b= color.b * 0.3;

    gl_FragColor = color;
}