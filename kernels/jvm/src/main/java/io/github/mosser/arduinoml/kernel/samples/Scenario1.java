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

public class Scenario1 {
	public static void main(String[] args) {
		// Étape 2 : le matériel
		Sensor button = new Sensor();   button.setName("button"); button.setPin(9);
		Actuator led = new Actuator();  led.setName("led");       led.setPin(12);
		Actuator buzzer = new Actuator(); buzzer.setName("buzzer"); buzzer.setPin(11);

		// Étape 3 : les états
		State off = new State(); off.setName("off");
		State on  = new State(); on.setName("on");

		// Étape 4 : actions de off
		Action a1 = new Action(); a1.setActuator(led);    a1.setValue(SIGNAL.LOW);
		Action a2 = new Action(); a2.setActuator(buzzer); a2.setValue(SIGNAL.LOW);
		off.setActions(Arrays.asList(a1, a2));

		// Étape 6 : actions de on
		Action a3 = new Action(); a3.setActuator(led);    a3.setValue(SIGNAL.HIGH);
		Action a4 = new Action(); a4.setActuator(buzzer); a4.setValue(SIGNAL.HIGH);
		on.setActions(Arrays.asList(a3, a4));

		// Étapes 5 et 6 : transitions
		SignalTransition t1 = new SignalTransition();
		t1.setSensor(button); t1.setValue(SIGNAL.HIGH); t1.setNext(on);
		off.setTransition(t1);
		SignalTransition t2 = new SignalTransition();
		t2.setSensor(button); t2.setValue(SIGNAL.LOW); t2.setNext(off);
		on.setTransition(t2);

		// Étapes 1 et 3 : l'App
		App alarme = new App();
		alarme.setName("alarme");
		alarme.setBricks(Arrays.asList(button, led, buzzer));
		alarme.setStates(Arrays.asList(off, on));
		alarme.setInitial(off);

		// Génération du code Arduino
		Visitor codeGenerator = new ToWiring();
		alarme.accept(codeGenerator);
		System.out.println(codeGenerator.getResult());
	}
}