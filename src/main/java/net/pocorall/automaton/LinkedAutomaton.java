package net.pocorall.automaton;

import java.io.Serializable;
import java.util.Set;

abstract public class LinkedAutomaton implements Serializable, Cloneable, Automaton {

	static final long serialVersionUID = 10001;

	/**
	 * Minimize using Huffman's O(n<sup>2</sup>) algorithm.
	 * This is the standard text-book algorithm.
	 *
	 * @see #setMinimization(int)
	 */
	public static final int MINIMIZE_HUFFMAN = 0;
	/**
	 * Minimize using Brzozowski's O(2<sup>n</sup>) algorithm.
	 * This algorithm uses the reverse-determinize-reverse-determinize trick, which has a bad
	 * worst-case behavior but often works very well in practice
	 * (even better than Hopcroft's!).
	 *
	 * @see #setMinimization(int)
	 */
	public static final int MINIMIZE_BRZOZOWSKI = 1;
	/**
	 * Minimize using Hopcroft's O(n log n) algorithm.
	 * This is regarded as one of the most generally efficient algorithms that exist.
	 *
	 * @see #setMinimization(int)
	 */
	public static final int MINIMIZE_HOPCROFT = 2;
	/**
	 * Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT</code>).
	 */
	static int minimization = MINIMIZE_HOPCROFT;
	/**
	 * Minimize always flag.
	 */
	static boolean minimize_always = false;
	/**
	 * Selects whether operations may modify the input automata (default: <code>false</code>).
	 */
	static boolean allow_mutation = false;
	/**
	 * Caches the <code>isDebug</code> state.
	 */
	static Boolean is_debug = null;

	/**
	 * Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT</code>).
	 *
	 * @param algorithm minimization algorithm
	 */
	static public void setMinimization(int algorithm) {
		minimization = algorithm;
	}

	/**
	 * Sets or resets minimize always flag.
	 * If this flag is set, then {@link #minimize()} will automatically
	 * be invoked after all operations that otherwise may produce non-minimal automata.
	 * By default, the flag is not set.
	 *
	 * @param flag if true, the flag is set
	 */
	static public void setMinimizeAlways(boolean flag) {
		minimize_always = flag;
	}

	/**
	 * Sets or resets allow mutate flag.
	 * If this flag is set, then all automata operations may modify automata given as input;
	 * otherwise, operations will always leave input automata languages unmodified.
	 * By default, the flag is not set.
	 *
	 * @param flag if true, the flag is set
	 * @return previous value of the flag
	 */
	static public boolean setAllowMutate(boolean flag) {
		boolean b = allow_mutation;
		allow_mutation = flag;
		return b;
	}

	/**
	 * Returns the state of the allow mutate flag.
	 * If this flag is set, then all automata operations may modify automata given as input;
	 * otherwise, operations will always leave input automata languages unmodified.
	 * By default, the flag is not set.
	 *
	 * @return current value of the flag
	 */
	static boolean getAllowMutate() {
		return allow_mutation;
	}

	/**
	 * Assigns consecutive numbers to the given states.
	 */
	static void setStateNumbers(Set<State> states) {
		int number = 0;
		for (State s : states)
			s.number = number++;
	}

	/**
	 * Returns a sorted array of transitions for each state (and sets state numbers).
	 */
	static Transition[][] getSortedTransitions(Set<State> states) {
		setStateNumbers(states);
		Transition[][] transitions = new Transition[states.size()][];
		for (State s : states)
			transitions[s.number] = s.getSortedTransitionArray(false);
		return transitions;
	}
}
