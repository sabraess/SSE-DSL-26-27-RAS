package io.github.mosser.arduinoml.kernel.samples;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.Action;
import io.github.mosser.arduinoml.kernel.behavioral.EDGE;
import io.github.mosser.arduinoml.kernel.behavioral.EdgeCondition;
import io.github.mosser.arduinoml.kernel.behavioral.State;
import io.github.mosser.arduinoml.kernel.behavioral.Transition;
import io.github.mosser.arduinoml.kernel.generator.ToWiring;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.SIGNAL;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

import java.util.Arrays;

/**
 * Scénario 4 : Multi-state alarm.
 * Appui 1 : le buzzer sonne.
 * Appui 2 : le buzzer s'arrête et la LED s'allume.
 * Appui 3 : la LED s'éteint, prêt à recommencer.
 */
public class Scenario4 {

	public static void main(String[] args) {
		// Bricks
		Sensor button = new Sensor();
		button.setName("button");
		button.setPin(9);

		Actuator led = new Actuator();
		led.setName("led");
		led.setPin(12);

		Actuator buzzer = new Actuator();
		buzzer.setName("buzzer");
		buzzer.setPin(11);

		// States
		State ready = new State();
		ready.setName("ready");

		State buzzing = new State();
		buzzing.setName("buzzing");

		State lightOn = new State();
		lightOn.setName("lightOn");

		// Actions : on fixe les DEUX actionneurs dans chaque état
		ready.setActions(Arrays.asList(
				action(buzzer, SIGNAL.LOW),
				action(led, SIGNAL.LOW)));

		buzzing.setActions(Arrays.asList(
				action(buzzer, SIGNAL.HIGH),
				action(led, SIGNAL.LOW)));

		lightOn.setActions(Arrays.asList(
				action(buzzer, SIGNAL.LOW),
				action(led, SIGNAL.HIGH)));

		// Condition : front montant = "le bouton VIENT d'être appuyé"
		EdgeCondition pushed = new EdgeCondition();
		pushed.setSensor(button);
		pushed.setEdge(EDGE.RISING);

		// Transitions : ready -> buzzing -> lightOn -> ready
		ready.setTransitions(Arrays.asList(transition(pushed, buzzing)));
		buzzing.setTransitions(Arrays.asList(transition(pushed, lightOn)));
		lightOn.setTransitions(Arrays.asList(transition(pushed, ready)));

		// L'App
		App app = new App();
		app.setName("multi-state alarm");
		app.setBricks(Arrays.asList(button, led, buzzer));
		app.setStates(Arrays.asList(ready, buzzing, lightOn));
		app.setInitial(ready);

		// Génération du code Arduino
		Visitor codeGenerator = new ToWiring();
		app.accept(codeGenerator);
		System.out.println(codeGenerator.getResult());
	}

	// Petites fonctions utilitaires pour éviter de répéter 3 lignes à chaque fois
	private static Action action(Actuator actuator, SIGNAL value) {
		Action a = new Action();
		a.setActuator(actuator);
		a.setValue(value);
		return a;
	}

	private static Transition transition(EdgeCondition condition, State next) {
		Transition t = new Transition();
		t.setCondition(condition);
		t.setNext(next);
		return t;
	}
}