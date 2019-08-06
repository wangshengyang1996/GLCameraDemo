attribute vec4 attr_position;
attribute vec2 attr_tc;
varying vec2 tc;
void main() {
    gl_Position = attr_position;
    tc = attr_tc;
}