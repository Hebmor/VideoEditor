# VideoEditor
 FFMPEG-based video editor for Android
 
# RU
## Описание:

Видеоредактор для Android основанный на FFmpeg и внутренних классах Android ([MediaCodec](https://developer.android.com/reference/android/media/MediaCodec), 
[MediaExtractor](https://developer.android.com/reference/android/media/MediaExtractor), 
[MediaMuxer](https://developer.android.com/reference/android/media/MediaMuxer), 
[GLSurfaceView](https://developer.android.com/reference/android/opengl/GLSurfaceView)).

Для хранения информации приложения использован [Android Room (SQLite)](https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#0).

## Требования:
* Версия Android: 7.0 - 9.0 (10.0 - на данный момент не поддерживается!)
* OpenGL 2.0

## Это важно!

* Приложение в данный момент в разработке и может содержать лишний исходный код, нелогичности и плохой стиль кода. 
* Что-то может не работать или просто отсутствовать!
* По мере разработки все будет чиститься, к коду добавлены комментарии. 

> Для взаимодествия из Java с FFmpeg использован (https://github.com/tanersener/mobile-ffmpeg).

# EN
## Description:

Video editor for Android based on FFmpeg and Android internal classes ([MediaCodec](https://developer.android.com/reference/android/media/MediaCodec), 
[MediaExtractor](https://developer.android.com/reference/android/media/MediaExtractor), 
[MediaMuxer](https://developer.android.com/reference/android/media/MediaMuxer), 
[GLSurfaceView](https://developer.android.com/reference/android/opengl/GLSurfaceView)).

Applications are used to store information [Android Room (SQLite)](https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#0).

## Requirements:
* Android version: 7.0 - 9.0 (10.0 - currently not supported!)
* Opengl 2.0

## It is important!

* The application is currently under development and may contain redundant source code, inconsistencies and poor code style.
* Something may not work or just be absent!
* As you develop, everything will be cleaned, comments are added to the code.

> To interact with Java using FFmpeg (https://github.com/tanersener/mobile-ffmpeg).
