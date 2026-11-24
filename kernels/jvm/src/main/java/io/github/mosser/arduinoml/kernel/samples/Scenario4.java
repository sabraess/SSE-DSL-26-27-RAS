package io.github.mosser.arduinoml.kernel.samples;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.Action;
import io.github.mosser.arduinoml.kernel.behavioral.SignalTransition;
import io.github.mosser.arduinoml.kernel.behavioral.State;
import io.github.mosser.arduinoml.kernel.generator.ToWiring;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.Arrays;

public class Scenario4 {

	public static void main(String[] args) {
		Sensor button   = new Sensor();
        button.setName("button");
        button.setPin(9);

		Actuator led = new Actuator();
        led.setName("led");
        led.setPin(12);
		
        Actuator buzzer = new Actuator();
        buzzer.setName("buzzer");
        buzzer.setPin(11);

		State ready = new State();
        ready.setName("ready");

		State noise = new State();
        noise.setName("noise");
		
        State light = new State(); 
        light.setName("light");

		// Actions
		// ready = tout éteint
		Action readyLed = new Action();
        readyLed.setActuator(led);
        readyLed.setValue(SIGNAL.LOW);
		
        Action readyBuzzer = new Action();
        readyBuzzer.setActuator(buzzer);
        readyBuzzer.setValue(SIGNAL.LOW);
		
        ready.setActions(Arrays.asList(readyLed, readyBuzzer));

		// noise = buzzer sonne
		Action noiseBuzzer = new Action();
        noiseBuzzer.setActuator(buzzer);
        noiseBuzzer.setValue(SIGNAL.HIGH);
		
        noise.setActions(Arrays.asList(noiseBuzzer));

		// light = couper buzzer ET allumer LED
		Action lightBuzzer = new Action();
        lightBuzzer.setActuator(buzzer);
        lightBuzzer.setValue(SIGNAL.LOW);

		Action lightLed = new Action();
        lightLed.setActuator(led);
        lightLed.setValue(SIGNAL.HIGH);

		light.setActions(Arrays.asList(lightBuzzer, lightLed));

		// Transitions (un appui fait avancer dans le cycle)
		SignalTransition readyToNoise = new SignalTransition();
		readyToNoise.setSensor(button);
        readyToNoise.setValue(SIGNAL.HIGH);
        readyToNoise.setNext(noise);
		ready.setTransition(readyToNoise);

		SignalTransition noiseToLight = new SignalTransition();
		noiseToLight.setSensor(button);
        noiseToLight.setValue(SIGNAL.HIGH);
        noiseToLight.setNext(light);
		noise.setTransition(noiseToLight);

		SignalTransition lightToReady = new SignalTransition();
		lightToReady.setSensor(button);
        lightToReady.setValue(SIGNAL.HIGH);
        lightToReady.setNext(ready);
		light.setTransition(lightToReady);

		// Étape 5 : l'application
		App app = new App();
		app.setName("multiStateAlarm");
		app.setBricks(Arrays.asList(button, led, buzzer));
		app.setStates(Arrays.asList(ready, noise, light));
		app.setInitial(ready);

		// arduino
		Visitor codeGenerator = new ToWiring();
		app.accept(codeGenerator);
		System.out.println(codeGenerator.getResult());
	}
}