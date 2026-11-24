package io.github.mosser.arduinoml.kernel.behavioral;

/**
 * Condition unaire (abstraite) : transforme exactement 1 condition.
 */
public abstract class UnaryCondition extends Condition {

	protected Condition condition;

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
}
