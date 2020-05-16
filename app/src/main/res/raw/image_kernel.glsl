#extension GL_OES_EGL_image_external : require
precision mediump float;     // highp here doesn't seem to matter
varying vec2 vTextureCoord;
uniform vec2 imageResolution;
uniform mat3 kernel;
uniform samplerExternalOES sTexture;

void main() {
    vec4 color = vec4(0);
    vec2 pos = vec2(vTextureCoord.x,vTextureCoord.y);
    vec2 onePixel = vec2(1, 1) / imageResolution;
    for(int i = 0; i < 3; i++) {
        for(int j = 0; j < 3; j++) {
            vec2 samplePos = pos + vec2(i - 1 , j - 1) * onePixel;
            vec4 sampleColor = texture2D(sTexture, samplePos);
            sampleColor *= kernel[i][j];
            color += sampleColor;
        }
    }
    color.a = 1.0;
    gl_FragColor = color;
}