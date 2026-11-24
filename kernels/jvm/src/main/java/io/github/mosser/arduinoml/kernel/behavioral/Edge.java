package io.github.mosser.arduinoml.kernel.behavioral;

public enum Edge {
	RISING,   // passage de LOW à HIGH : le bouton vient d'être appuyé
	FALLING   // passage de HIGH à LOW : le bouton vient d'être relâché
}
