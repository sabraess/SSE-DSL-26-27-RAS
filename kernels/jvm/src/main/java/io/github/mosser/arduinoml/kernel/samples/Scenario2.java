package io.github.mosser.arduinoml.kernel.samples;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.Action;
import io.github.mosser.arduinoml.kernel.behavioral.BinaryCondition;
import io.github.mosser.arduinoml.kernel.behavioral.LogicalOperator;
import io.github.mosser.arduinoml.kernel.behavioral.SignalCondition;
import io.github.mosser.arduinoml.kernel.behavioral.State;
import io.github.mosser.arduinoml.kernel.behavioral.Transition;
import io.github.mosser.arduinoml.kernel.generator.ToWiring;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.Arrays;

/**
 * Scénario 2 : Dual-check alarm.
 * Le buzzer sonne si et seulement si les deux boutons sont appuyés en même temps.
 * Relâcher au moins un bouton arrête le son.
 */
public class Scenario2 {
	public static void main(String[] args) {
		// Le matériel
		Sensor b1 = new Sensor();         b1.setName("b1");         b1.setPin(8);
		Sensor b2 = new Sensor();         b2.setName("b2");         b2.setPin(9);
		Actuator buzzer = new Actuator(); buzzer.setName("buzzer"); buzzer.setPin(11);

		// Les états
		State off = new State(); off.setName("off");
		State on  = new State(); on.setName("on");

		// Actions
		Action buzzerOff = new Action(); buzzerOff.setActuator(buzzer); buzzerOff.setValue(SIGNAL.LOW);
		off.setActions(Arrays.asList(buzzerOff));
		Action buzzerOn = new Action();  buzzerOn.setActuator(buzzer);  buzzerOn.setValue(SIGNAL.HIGH);
		on.setActions(Arrays.asList(buzzerOn));

		// Transition off -> on : b1 = HIGH ET b2 = HIGH  (BinaryCondition AND)
		SignalCondition b1Pressed = new SignalCondition(); b1Pressed.setSensor(b1); b1Pressed.setValue(SIGNAL.HIGH);
		SignalCondition b2Pressed = new SignalCondition(); b2Pressed.setSensor(b2); b2Pressed.setValue(SIGNAL.HIGH);
		BinaryCondition bothPressed = new BinaryCondition();
		bothPressed.setOperator(LogicalOperator.AND);
		bothPressed.setConditions(Arrays.asList(b1Pressed, b2Pressed));

		Transition t1 = new Transition(); t1.setCondition(bothPressed); t1.setNext(on);
		off.setTransitions(Arrays.asList(t1));

		// Transitions on -> off : deux transitions, une par bouton relâché
		SignalCondition b1Released = new SignalCondition(); b1Released.setSensor(b1); b1Released.setValue(SIGNAL.LOW);
		SignalCondition b2Released = new SignalCondition(); b2Released.setSensor(b2); b2Released.setValue(SIGNAL.LOW);

		Transition t2 = new Transition(); t2.setCondition(b1Released); t2.setNext(off);
		Transition t3 = new Transition(); t3.setCondition(b2Released); t3.setNext(off);
		on.setTransitions(Arrays.asList(t2, t3));

		// L'App
		App alarme = new App();
		alarme.setName("dual-check alarm");
		alarme.setBricks(Arrays.asList(b1, b2, buzzer));
		alarme.setStates(Arrays.asList(off, on));
		alarme.setInitial(off);

		// Génération du code Arduino
		Visitor codeGenerator = new ToWiring();
		alarme.accept(codeGenerator);
		System.out.println(codeGenerator.getResult());
	}
}
