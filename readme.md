#### 使用多种方式实现圆形预览
- 使用Camera API和Camera2 API并选择最接近正方形的预览尺寸

- 使用Camera API并为其动态添加一层父控件，达到正方形预览的效果

- 使用Camera API获取预览数据，使用OpenGL的方式进行显示

- 左侧使用1088x1088的分辨率，TextureView和GLSurfaceView的显示效果相同，均为正方形；</br>
右侧使用1920x1080的分辨率，TextureView显示效果为长方形，GLSurfaceView显示效果为TextureView显示内容中心的正方形部分

| ![TextureView + GLSurfaceView，1088x1088](https://github.com/wangshengyang1996/GLCameraDemo/blob/master/PreviewSize_1088x1088.gif) | ![TextureView + GLSurfaceView，1920x1080](https://github.com/wangshengyang1996/GLCameraDemo/blob/master/PreviewSize_1920x1080.gif) 
|:-|:-|
