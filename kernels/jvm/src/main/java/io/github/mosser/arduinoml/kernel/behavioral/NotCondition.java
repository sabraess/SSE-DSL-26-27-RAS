package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;

/**
 * Négation : vraie quand la condition contenue est fausse.
 */
public class NotCondition extends UnaryCondition {

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
