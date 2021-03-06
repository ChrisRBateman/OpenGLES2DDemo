OpenGLES2DDemo
==============

#### OpenGLES 2.0 2D Sprite Demo

Demo renders and animates 2D sprites using OpenGLES 2.0.   
Written in <b>Java</b> using Android Studio 3 and tested on Android 4.4.4, 5.1.1 and 7.0 devices.
I'm using vertex and index buffer objects to display the sprites.

<img src="screenshots/screen-one.png" width="171" height="284" title="Screen Shot 1">  <img src="screenshots/screen-two.png" width="171" height="284" title="Screen Shot 2">  <img src="screenshots/screen-three.png" width="171" height="284" title="Screen Shot 3">

There are 3 buttons on the bottom of the screen that control speed, play/pause and direction of moon sprite. 
The 3 buttons are also animated.

Added frame rate smoothing code from https://github.com/fadden/android-breakout.

Added the GLText class from https://github.com/d3alek/Texample2 to display frame rate text. 
I modified the code to use vertex and index buffer objects.
