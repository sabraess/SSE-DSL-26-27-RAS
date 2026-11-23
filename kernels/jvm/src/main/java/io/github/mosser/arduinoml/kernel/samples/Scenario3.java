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

public class Scenario3 {

	public static void main(String[] args) {
		Sensor button = new Sensor();  
        button.setName("button"); 
        button.setPin(9);

		Actuator led = new Actuator(); 
        led.setName("led");
        led.setPin(12);

        // States
		State off = new State(); 
        off.setName("off");
		
        State on  = new State(); 
        on.setName("on");

		// Actions
		Action ledOff = new Action(); 
        ledOff.setActuator(led); 
        ledOff.setValue(SIGNAL.LOW);
		off.setActions(Arrays.asList(ledOff));

		Action ledOn = new Action(); 
        ledOn.setActuator(led); 
        ledOn.setValue(SIGNAL.HIGH);
        on.setActions(Arrays.asList(ledOn));

		// Transitions = HIGH dans les DEUX sens (!= scénar 1)
		SignalTransition offToOn = new SignalTransition();
		offToOn.setSensor(button); 
        offToOn.setValue(SIGNAL.HIGH); 
        offToOn.setNext(on);
		off.setTransition(offToOn);

		SignalTransition onToOff = new SignalTransition();
		onToOff.setSensor(button); 
        onToOff.setValue(SIGNAL.HIGH); 
        onToOff.setNext(off);
		on.setTransition(onToOff);

		// L'APp
		App app = new App();
		app.setName("switch");
		app.setBricks(Arrays.asList(button, led));
		app.setStates(Arrays.asList(off, on));
		app.setInitial(off);

		// gene code arduino
		Visitor codeGenerator = new ToWiring();
		app.accept(codeGenerator);
		System.out.println(codeGenerator.getResult());
	}
}