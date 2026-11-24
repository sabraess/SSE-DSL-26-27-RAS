package io.github.mosser.arduinoml.kernel.generator;

import io.github.mosser.arduinoml.kernel.App;
import io.github.mosser.arduinoml.kernel.behavioral.*;
import io.github.mosser.arduinoml.kernel.structural.*;

/**
 * Visitor that generates Wiring (Arduino) code from an ArduinoML model.
 *
 * Le code est produit en 3 passes sur les briques :
 *   GLOBALS : variables globales
 *   SETUP   : contenu de setup()
 *   READ    : lecture des capteurs au début de loop()
 */
public class ToWiring extends Visitor<StringBuffer> {
	enum PASS {GLOBALS, SETUP, READ, LOOP}

	public ToWiring() {
		this.result = new StringBuffer();
	}

	private void w(String s) {
		result.append(String.format("%s", s));
	}

	@Override
	public void visit(App app) {
		context.put("pass", PASS.GLOBALS);
		w("// Wiring code generated from an ArduinoML model\n");
		w(String.format("// Application name: %s\n", app.getName()) + "\n");

		w("unsigned long debounce = 200;        // delai minimal entre deux changements d'etat\n");
		w("unsigned long lastDebounceTime = 0;\n");
		w("\nenum STATE {");
		String sep = "";
		for (State state : app.getStates()) {
			w(sep);
			state.accept(this);
			sep = ", ";
		}
		w("};\n");
		if (app.getInitial() != null) {
			w("STATE currentState = " + app.getInitial().getName() + ";\n");
		}
		for (Brick brick : app.getBricks()) {
			brick.accept(this);
		}

		context.put("pass", PASS.SETUP);
		w("\nvoid setup(){\n");
		for (Brick brick : app.getBricks()) {
			brick.accept(this);
		}
		w("}\n");

		w("\nvoid loop() {\n");
		context.put("pass", PASS.READ);
		for (Brick brick : app.getBricks()) {
			brick.accept(this);
		}

		context.put("pass", PASS.LOOP);
		w("\n\tswitch(currentState){\n");
		for (State state : app.getStates()) {
			state.accept(this);
		}
		w("\t}\n" +
				"}\n");
	}

	@Override
	public void visit(Actuator actuator) {
		if (context.get("pass") == PASS.SETUP) {
			w(String.format("  pinMode(%d, OUTPUT); // %s [Actuator]\n", actuator.getPin(), actuator.getName()));
		}
	}

	@Override
	public void visit(Sensor sensor) {
		String n = sensor.getName();
		if (context.get("pass") == PASS.GLOBALS) {
			// valeur filtrée (sans rebond), valeur au tour précédent (pour les fronts)
			w(String.format("\nint %sState = LOW;\n", n));
			w(String.format("int %sPrev = LOW;\n", n));
			w(String.format("int %sRaw = LOW;\n", n));
			w(String.format("unsigned long %sLastChange = 0;\n", n));
		}
		if (context.get("pass") == PASS.SETUP) {
			w(String.format("  pinMode(%d, INPUT);  // %s [Sensor]\n", sensor.getPin(), n));
		}
		if (context.get("pass") == PASS.READ) {
			// anti-rebond : on n'accepte une nouvelle valeur que si elle est stable depuis 50 ms
			w(String.format("\t// lecture de %s (anti-rebond)\n", n));
			w(String.format("\t%sPrev = %sState;\n", n, n));
			w(String.format("\tint %sRead = digitalRead(%d);\n", n, sensor.getPin()));
			w(String.format("\tif (%sRead != %sRaw) { %sRaw = %sRead; %sLastChange = millis(); }\n", n, n, n, n, n));
			w(String.format("\tif (millis() - %sLastChange > 50) { %sState = %sRaw; }\n", n, n, n));
		}
	}

	@Override
	public void visit(State state) {
		if (context.get("pass") == PASS.GLOBALS) {
			w(state.getName());
			return;
		}
		if (context.get("pass") == PASS.LOOP) {
			w("\t\tcase " + state.getName() + ":\n");
			for (Action action : state.getActions()) {
				action.accept(this);
			}
			if (!state.getTransitions().isEmpty()) {
				w("\t\t\tif (millis() - lastDebounceTime > debounce) {\n");
				String keyword = "if";
				for (Transition transition : state.getTransitions()) {
					w("\t\t\t\t" + keyword + " (");
					transition.accept(this);
					keyword = "} else if";
				}
				w("\t\t\t\t}\n");
				w("\t\t\t}\n");
			}
			w("\t\tbreak;\n");
		}
	}

	@Override
	public void visit(Transition transition) {
		transition.getCondition().accept(this);   // écrit l'expression booléenne
		w(") {\n");
		w("\t\t\t\t\tlastDebounceTime = millis();\n");
		w("\t\t\t\t\tcurrentState = " + transition.getNext().getName() + ";\n");
	}

	@Override
	public void visit(SignalCondition condition) {
		// niveau : ex. buttonState == HIGH
		w(String.format("%sState == %s", condition.getSensor().getName(), condition.getValue()));
	}

	@Override
	public void visit(EdgeCondition condition) {
		// front : la valeur vient de changer entre le tour précédent et maintenant
		String n = condition.getSensor().getName();
		if (condition.getEdge() == Edge.RISING) {
			w(String.format("(%sState == HIGH && %sPrev == LOW)", n, n));
		} else {
			w(String.format("(%sState == LOW && %sPrev == HIGH)", n, n));
		}
	}

	@Override
	public void visit(BinaryCondition condition) {
		// ex. (b1State == HIGH && b2State == HIGH)
		String op = condition.getOperator() == LogicalOperator.AND ? " && " : " || ";
		w("(");
		condition.getConditions().get(0).accept(this);   // récursif : peut être composée
		w(op);
		condition.getConditions().get(1).accept(this);
		w(")");
	}

	@Override
	public void visit(NotCondition condition) {
		// ex. !(buttonState == HIGH)
		w("!(");
		condition.getCondition().accept(this);
		w(")");
	}

	@Override
	public void visit(Action action) {
		w(String.format("\t\t\tdigitalWrite(%d,%s);\n", action.getActuator().getPin(), action.getValue()));
	}

}
