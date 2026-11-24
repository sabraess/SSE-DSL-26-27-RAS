package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitor;

import java.util.List;

/**
 * Condition binaire : combine exactement 2 conditions avec AND ou OR.
 * Pour en combiner plus, on imbrique : a AND b AND c = AND(a, AND(b, c)).
 */
public class BinaryCondition extends Condition {

	private LogicalOperator operator;
	private List<Condition> conditions;

	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		if (conditions == null || conditions.size() != 2) {
			throw new IllegalArgumentException("Une BinaryCondition contient exactement 2 conditions");
		}
		this.conditions = conditions;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
