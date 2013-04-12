#include <Servo.h> 
// Signal received from Android
char signal[2];
// Lower servo
Servo base_servo;
// Upper servo
Servo arm_servo;
// An array that contains the value of rotation of each engine
int values_array[4] = { 0 , 90 , 90 , 0 };
// Base's potentiometer to scan rotation angle
int potentiometer = A0;
// Counter
int i;

// Assign to each engine a value to rotate
void assign_values(int *values_array,char signal[2]){
	switch (signal[0]){
		case '1':
			// Rotation angle scanned by potentiometer, it varies between 250 and 1015
			int rotation_angle = analogRead(potentiometer);
			Serial.println(rotation_angle);
			if ((signal[1]=='B') && (rotation_angle<1015))
				// Rotation in the forward direction
				values_array[0]=1;
                                
                                      
			else if ((signal[1]=='F') && (rotation_angle>250))
				// Rotation in the backward direction
				values_array[0]=-1;                       
			else
				// Hold the actual position
				values_array[0]=0;
			break;
		case '2' :
			if (signal[1]=='F' && values_array[1]<75)
				// Rotation in the forward direction (by 5 steps)
				values_array[1]+=5;
			else if (signal[1]=='B' && values_array[1]>35)
				// Rotation in the backward direction (by 5 steps)
				values_array[1]-=5;
			break;
		case '3':
			if ((signal[1]=='B') && (values_array[2]<150))
				// Rotation in the forward direction (by 5 stepss)
				values_array[2]+=5;
			else if ((signal[1]=='F') && (values_array[2]>70))
				// Rotation in the backward direction (by 5 steps)
				values_array[2]-=5;
			break;
		case '4':
			if (signal[1]=='F')
				// Close the hand
				values_array[3]=1;
			else if (signal[1]=='B')
				// Open the hand
				values_array[3]=-1;
			else
				values_array[3]=0;
			break;			
	}
}

// Rotating the engines according to their rotation values
void rotating_engines(int Engine_index, int value){
	switch (Engine_index){
		case 0 :
			if (value==1){
				// Rotation in the forward direction
				digitalWrite(2,HIGH);
				digitalWrite(3,LOW);
			} else if (value==-1) {
				// Rotation in the backward direction
				digitalWrite(2,LOW);
				digitalWrite(3,HIGH);
			} else {
				// No rotation
				digitalWrite(2,LOW);
				digitalWrite(3,LOW);
			}
			break;
		case 1:
			base_servo.write(value);
			break;
		case 2:
			arm_servo.write(value);
			break;
		case 3:
			if (value==1){
				// Open the hand
				digitalWrite(4,HIGH);
				digitalWrite(7,LOW);
			} else if (value==-1){
				// Close the hand
				digitalWrite(4,LOW);
				digitalWrite(7,HIGH);
			} else {
				// Do nothing
				digitalWrite(4,LOW);
				digitalWrite(7,LOW);
			}
			break;
	}
}

// Arduino setup
void setup(){
	Serial.begin(9600);
	// Pins' (2 to 12) initialisation
	for (i=0; i<12;i++){
		pinMode(i,OUTPUT);
		digitalWrite(i,LOW);
	}
	// Potentiometer pin's initialisation
	pinMode(potentiometer,INPUT);
	base_servo.attach(5);
	arm_servo.attach(6);	
}

// Arduino loop
void loop(){
	// Verify the existence of serial data
	if (Serial.available()>0){
		// Read the signal received from Android
		Serial.readBytes(signal,2);  
		// Assign to each engine a value to rotate
		assign_values(values_array,signal);
		Serial.println(signal[0]);
		Serial.println(signal[1]);
	}
	// Rotating the engines according to their rotation values
	for (i=0;i<4;i++)
		rotating_engines(i,values_array[i]); 
}

