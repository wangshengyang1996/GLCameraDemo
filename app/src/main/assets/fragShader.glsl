varying vec2 tc;
uniform sampler2D ySampler;
uniform sampler2D uSampler;
uniform sampler2D vSampler;
const mat3 convertMat = mat3(1.0, 1.0, 1.0, 0, -0.344, 1.77, 1.403, -0.714, 0);
void main()
{
    vec3 yuv;
    yuv.x = texture2D(ySampler, tc).r;
    yuv.y = texture2D(uSampler, tc).r - 0.5;
    yuv.z = texture2D(vSampler, tc).r - 0.5;
    gl_FragColor = vec4(convertMat * yuv, 1.0);
}