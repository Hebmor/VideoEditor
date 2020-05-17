#extension GL_OES_EGL_image_external : require
precision mediump float;     // highp here doesn't seem to matter
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform float brightness;
uniform float contrast;
void main() {
    vec4 color = texture2D(sTexture, vTextureCoord);
    color = vec4(color.rgb + brightness,1.0);
    color = vec4(((color.rgb - vec3(0.5)) * contrast) + vec3(0.5),1.0);
    gl_FragColor = color;
}
