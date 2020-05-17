#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform vec2 imageResolution;
uniform float size;

void main() {
    vec2 coordinates = vTextureCoord.xy;
    vec2 pixelSize = vec2(size / imageResolution.x,size / imageResolution.y);
    vec2 position = floor(coordinates / pixelSize) * pixelSize;
    gl_FragColor = texture2D(sTexture, position);
}