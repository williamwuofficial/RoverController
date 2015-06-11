# RoverController
Description

This project is to simply control a wheeled robot through an android application. 

Details

The wheeled robot in question is known as the ShieldBot (http://www.seeedstudio.com/wiki/Shield_Bot_V1.1) and communication to the robot is through a bluetooth module. During this project the module used was the RN-42 bluetooth module from sparkfun.com. 

To connect to the robot, please read through the documentation for the module. All that's required is to discover the bluetooth MAC address of the module. In the android code modify the current bluetooth address to the desired number. Then load on the arduino code, attaching the serial lines from the bluetooth module to the arduino. 

Make sure the code is loaded before the serial lines are attached as this could intefere in the programming 

Note:
Due to numerous issues. The code may be ported and redone in Android Studio. Currently, this code has been done in Eclipse using the outdated Ant build system. 



