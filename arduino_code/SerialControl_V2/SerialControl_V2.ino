#include <Shieldbot.h>	

int debugLED = 13;

String buffer;
char message[10]; // Format = {M 100 100\0}

Shieldbot shieldbot = Shieldbot();


void setup(){
  shieldbot.setMaxSpeed(70,70);
  
  pinMode(debugLED, OUTPUT);
  
  Serial.begin(115200);
  Serial.setTimeout(1000);
}

void loop () {
  if (Serial.available()>0) {
    digitalWrite(debugLED, LOW);
    buffer = Serial.readStringUntil('\n');
    buffer.toCharArray(message, 12);
    shieldbotCMD(message);
    delay(250);
  } else {
    shieldbot.fastStop();
    digitalWrite(debugLED, HIGH);
    delay(50);
  }   
 
}

void shieldbotCMD(char* message){
  if (message[0] == 'M'){
     char* token;
     char* left_speed;
     char* right_speed;
     
     token = strtok(message, " ");
     left_speed = strtok(NULL, " ");
     right_speed = strtok(NULL, " ");

     shieldbot.drive(atoi(left_speed), atoi(right_speed));
  }
  return;
}
