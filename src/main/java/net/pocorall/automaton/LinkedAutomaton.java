package net.pocorall.automaton;

import java.io.Serializable;
import java.util.*;

abstract public class LinkedAutomaton implements Serializable, Cloneable, Automaton {

	/**
	 * Initial state of this automaton.
	 */
	State initial;

	/**
	 * If true, then this automaton is definitely deterministic
	 * (i.e., there are no choices for any run, but a run may crash).
	 */
	boolean deterministic;

	/**
	 * Hash code. Recomputed by {@link #minimize()}.
	 */
	int hash_code;


	/**
	 * Constructs a new automaton that accepts the empty language.
	 * Using this constructor, automata can be constructed manually from
	 * {@link State} and {@link Transition} objects.
	 *
	 * @see #setInitialState(State)
	 * @see State
	 * @see Transition
	 */
	public LinkedAutomaton() {
		initial = new State();
		deterministic = true;
	}

	/**
	 * Returns deterministic flag for this automaton.
	 *
	 * @return true if the automaton is definitely deterministic, false if the automaton
	 *         may be nondeterministic
	 */
	public boolean isDeterministic() {
		return deterministic;
	}

	/**
	 * Sets deterministic flag for this automaton.
	 * This method should (only) be used if automata are constructed manually.
	 *
	 * @param deterministic true if the automaton is definitely deterministic, false if the automaton
	 *                      may be nondeterministic
	 */
	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}


	boolean isDebug() {
		if (is_debug == null)
			is_debug = Boolean.valueOf(System.getProperty("dk.brics.automaton.debug") != null);
		return is_debug.booleanValue();
	}


	void checkMinimizeAlways() {
		if (minimize_always)
			minimize();
	}

	/**
	 * Returns sorted array of all interval start points.
	 */
	char[] getStartPoints() {
		Set<Character> pointset = new HashSet<Character>();
		for (State s : getStates()) {
			pointset.add(Character.MIN_VALUE);
			for (Transition t : s.transitions) {
				pointset.add(t.min);
				if (t.max < Character.MAX_VALUE)
					pointset.add((char) (t.max + 1));
			}
		}
		char[] points = new char[pointset.size()];
		int n = 0;
		for (Character m : pointset)
			points[n++] = m;
		Arrays.sort(points);
		return points;
	}

	/**
	 * Minimizes (and determinizes if not already deterministic) the given automaton.
	 *
	 * @see LinkedAutomaton#setMinimization(int)
	 */
	public LinkedAutomaton minimize() {
		switch (minimization) {
			case MINIMIZE_HUFFMAN:
				MinimizationOperations.minimizeHuffman(this);
				break;
			case MINIMIZE_BRZOZOWSKI:
				MinimizationOperations.minimizeBrzozowski(this);
				break;
			default:
				MinimizationOperations.minimizeHopcroft(this);
		}
		recomputeHashCode();
		return this;
	}

	/**
	 * Adds transitions to explicit crash state to ensure that transition function is total.
	 */
	void totalize() {
		State s = new State();
		s.transitions.add(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, s));
		for (State p : getStates()) {
			int maxi = Character.MIN_VALUE;
			for (Transition t : p.getSortedTransitions(false)) {
				if (t.min > maxi)
					p.transitions.add(new Transition((char) maxi, (char) (t.min - 1), s));
				if (t.max + 1 > maxi)
					maxi = t.max + 1;
			}
			if (maxi <= Character.MAX_VALUE)
				p.transitions.add(new Transition((char) maxi, Character.MAX_VALUE, s));
		}
	}

	/**
	 * Must be invoked when the stored hash code may no longer be valid.
	 */
	void clearHashCode() {
		hash_code = 0;
	}

	/**
	 * Removes transitions to dead states and calls {@link #reduce()} and {@link #clearHashCode()}.
	 * (A state is "dead" if no accept state is reachable from it.)
	 */
	public void removeDeadTransitions() {
		clearHashCode();
		Set<State> states = getStates();
		Set<State> live = getLiveStates(states);
		for (State s : states) {
			Set<Transition> st = s.transitions;
			s.resetTransitions();
			for (Transition t : st)
				if (live.contains(t.to))
					s.transitions.add(t);
		}
		reduce();
	}

	/**
	 * Reduces this automaton.
	 * An automaton is "reduced" by combining overlapping and adjacent edge intervals with same destination.
	 */
	public void reduce() {
		Set<State> states = getStates();
		setStateNumbers(states);
		for (State s : states) {
			List<Transition> st = s.getSortedTransitions(true);
			s.resetTransitions();
			State p = null;
			int min = -1, max = -1;
			for (Transition t : st) {
				if (p == t.to) {
					if (t.min <= max + 1) {
						if (t.max > max)
							max = t.max;
					} else {
						if (p != null)
							s.transitions.add(new Transition((char) min, (char) max, p));
						min = t.min;
						max = t.max;
					}
				} else {
					if (p != null)
						s.transitions.add(new Transition((char) min, (char) max, p));
					p = t.to;
					min = t.min;
					max = t.max;
				}
			}
			if (p != null)
				s.transitions.add(new Transition((char) min, (char) max, p));
		}
		clearHashCode();
	}

	protected Set<State> getLiveStates(Set<State> states) {
		HashMap<State, Set<State>> map = new HashMap<State, Set<State>>();
		for (State s : states)
			map.put(s, new HashSet<State>());
		for (State s : states)
			for (Transition t : s.transitions)
				map.get(t.to).add(s);
		Set<State> live = new HashSet<State>(getAcceptStates());
		LinkedList<State> worklist = new LinkedList<State>(live);
		while (worklist.size() > 0) {
			State s = worklist.removeFirst();
			for (State p : map.get(s))
				if (!live.contains(p)) {
					live.add(p);
					worklist.add(p);
				}
		}
		return live;
	}

	/**
	 * Returns the set of reachable accept states.
	 *
	 * @return set of {@link State} objects
	 */
	public Set<State> getAcceptStates() {
		expandSingleton();
		HashSet<State> accepts = new HashSet<State>();
		HashSet<State> visited = new HashSet<State>();
		LinkedList<State> worklist = new LinkedList<State>();
		worklist.add(initial);
		visited.add(initial);
		while (worklist.size() > 0) {
			State s = worklist.removeFirst();
			if (s.accept != null)
				accepts.add(s);
			for (Transition t : s.transitions)
				if (!visited.contains(t.to)) {
					visited.add(t.to);
					worklist.add(t.to);
				}
		}
		return accepts;
	}

	/**
	 * Recomputes the hash code.
	 * The automaton must be minimal when this operation is performed.
	 */
	public void recomputeHashCode() {
		hash_code = getNumberOfStates() * 3 + getNumberOfTransitions() * 2;
		if (hash_code == 0)
			hash_code = 1;
	}


	/**
	 * Returns the number of states in this automaton.
	 */
	public int getNumberOfStates() {
		return getStates().size();
	}

	/**
	 * Returns the number of transitions in this automaton. This number is counted
	 * as the total number of edges, where one edge may be a character interval.
	 */
	public int getNumberOfTransitions() {
		int c = 0;
		for (State s : getStates())
			c += s.transitions.size();
		return c;
	}

	/**
	 * Expands singleton representation to normal representation.
	 * Does nothing if not in singleton representation.
	 */
	public void expandSingleton() {
// does nothing
	}

	/**
	 * Returns the set of states that are reachable from the initial state.
	 *
	 * @return set of {@link State} objects
	 */
	public Set<State> getStates() {
		expandSingleton();
		Set<State> visited;
		if (isDebug())
			visited = new LinkedHashSet<State>();
		else
			visited = new HashSet<State>();
		LinkedList<State> worklist = new LinkedList<State>();
		worklist.add(initial);
		visited.add(initial);
		while (worklist.size() > 0) {
			State s = worklist.removeFirst();
			Collection<Transition> tr;
			if (isDebug())
				tr = s.getSortedTransitions(false);
			else
				tr = s.transitions;
			for (Transition t : tr)
				if (!visited.contains(t.to)) {
					visited.add(t.to);
					worklist.add(t.to);
				}
		}
		return visited;
	}

	// statics /////////////////////////////////////////////////////////
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
