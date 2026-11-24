package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;

/**
 * Condition (abstraite) : ce qui doit être vrai pour qu'une transition se déclenche.
 * Patron Composite : une condition est soit simple (SignalCondition),
 * soit une combinaison d'autres conditions (CompositeCondition).
 */
public abstract class Condition implements Visitable {
}
