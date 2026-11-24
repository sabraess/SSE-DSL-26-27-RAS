// Wiring code generated from an ArduinoML model
// Application name: multiStateAlarm

long debounce = 200;

enum STATE {ready, noise, light};
STATE currentState = ready;

boolean buttonBounceGuard = false;
long buttonLastDebounceTime = 0;

void setup(){
  pinMode(9, INPUT);  // button [Sensor]
  pinMode(12, OUTPUT); // led [Actuator]
  pinMode(11, OUTPUT); // buzzer [Actuator]
}

void loop() {
	switch(currentState){
		case ready:
			digitalWrite(12,LOW);
			digitalWrite(11,LOW);
			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
			if( digitalRead(9) == HIGH && buttonBounceGuard) {
				buttonLastDebounceTime = millis();
				currentState = noise;
			}
		break;
		case noise:
			digitalWrite(11,HIGH);
			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
			if( digitalRead(9) == HIGH && buttonBounceGuard) {
				buttonLastDebounceTime = millis();
				currentState = light;
			}
		break;
		case light:
			digitalWrite(11,LOW);
			digitalWrite(12,HIGH);
			buttonBounceGuard = millis() - buttonLastDebounceTime > debounce;
			if( digitalRead(9) == HIGH && buttonBounceGuard) {
				buttonLastDebounceTime = millis();
				currentState = ready;
			}
		break;
	}
}
