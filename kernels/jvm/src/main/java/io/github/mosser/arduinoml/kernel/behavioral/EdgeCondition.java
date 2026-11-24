package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Sensor;

/**
 * Condition sur un front : vraie uniquement au moment où le capteur change de valeur
 * (contrairement à SignalCondition, qui est vraie tant que le capteur a la valeur).
 */
public class EdgeCondition extends Condition {

	private Sensor sensor;
	private Edge edge;

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
