/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.pocorall.automaton;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Finite-state automaton with regular expression operations.
 * <p/>
 * Class invariants:
 * <ul>
 * <li> An automaton is either represented explicitly (with {@link State} and {@link Transition} objects)
 * or with a singleton string (see {@link #getSingleton()} and {@link #expandSingleton()}) in case the automaton is known to accept exactly one string.
 * (Implicitly, all states and transitions of an automaton are reachable from its initial state.)
 * <li> Automata are always reduced (see {@link #reduce()})
 * and have no transitions to dead states (see {@link #removeDeadTransitions()}).
 * <li> If an automaton is nondeterministic, then {@link #isDeterministic()} returns false (but
 * the converse is not required).
 * <li> Automata provided as input to operations are generally assumed to be disjoint.
 * </ul>
 * <p/>
 * If the states or transitions are manipulated manually, the {@link #restoreInvariant()}
 * and {@link #setDeterministic(boolean)} methods should be used afterwards to restore
 * representation invariants that are assumed by the built-in automata operations.
 *
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class LinkedAutomaton implements Serializable, Cloneable, Automaton {

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
	 * Initial state of this automaton.
	 */
	State initial;

	/**
	 * If true, then this automaton is definitely deterministic
	 * (i.e., there are no choices for any run, but a run may crash).
	 */
	boolean deterministic;

	/**
	 * Extra data associated with this automaton.
	 */
	transient Object info;

	/**
	 * Hash code. Recomputed by {@link #minimize()}.
	 */
	int hash_code;

	/**
	 * Singleton string. Null if not applicable.
	 */
	String singleton;

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
		singleton = null;
	}

	boolean isDebug() {
		if (is_debug == null)
			is_debug = Boolean.valueOf(System.getProperty("dk.brics.automaton.debug") != null);
		return is_debug.booleanValue();
	}

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

	void checkMinimizeAlways() {
		if (minimize_always)
			minimize();
	}

	boolean isSingleton() {
		return singleton != null;
	}

	/**
	 * Returns the singleton string for this automaton.
	 * An automaton that accepts exactly one string <i>may</i> be represented
	 * in singleton mode. In that case, this method may be used to obtain the string.
	 *
	 * @return string, null if this automaton is not in singleton mode.
	 */
	public String getSingleton() {
		return singleton;
	}

	/**
	 * Sets initial state.
	 *
	 * @param s state
	 */
	public void setInitialState(State s) {
		initial = s;
		singleton = null;
	}

	/**
	 * Gets initial state.
	 *
	 * @return state
	 */
	public State getInitialState() {
		expandSingleton();
		return initial;
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

	/**
	 * Associates extra information with this automaton.
	 *
	 * @param info extra information
	 */
	public void setInfo(Object info) {
		this.info = info;
	}

	/**
	 * Returns extra information associated with this automaton.
	 *
	 * @return extra information
	 * @see #setInfo(Object)
	 */
	public Object getInfo() {
		return info;
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
	 * Assigns consecutive numbers to the given states.
	 */
	static void setStateNumbers(Set<State> states) {
		int number = 0;
		for (State s : states)
			s.number = number++;
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
	 * Restores representation invariant.
	 * This method must be invoked before any built-in automata operation is performed
	 * if automaton states or transitions are manipulated manually.
	 *
	 * @see #setDeterministic(boolean)
	 */
	public void restoreInvariant() {
		removeDeadTransitions();
	}

	/**
	 * Reduces this automaton.
	 * An automaton is "reduced" by combining overlapping and adjacent edge intervals with same destination.
	 */
	public void reduce() {
		if (isSingleton())
			return;
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
	 * Returns the set of live states. A state is "live" if an accept state is reachable from it.
	 *
	 * @return set of {@link State} objects
	 */
	public Set<State> getLiveStates() {
		expandSingleton();
		return getLiveStates(getStates());
	}

	private Set<State> getLiveStates(Set<State> states) {
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
	 * Removes transitions to dead states and calls {@link #reduce()} and {@link #clearHashCode()}.
	 * (A state is "dead" if no accept state is reachable from it.)
	 */
	public void removeDeadTransitions() {
		clearHashCode();
		if (isSingleton())
			return;
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
	 * Returns a sorted array of transitions for each state (and sets state numbers).
	 */
	static Transition[][] getSortedTransitions(Set<State> states) {
		setStateNumbers(states);
		Transition[][] transitions = new Transition[states.size()][];
		for (State s : states)
			transitions[s.number] = s.getSortedTransitionArray(false);
		return transitions;
	}

	/**
	 * Expands singleton representation to normal representation.
	 * Does nothing if not in singleton representation.
	 */
	public void expandSingleton() {
		if (isSingleton()) {
			State p = new State();
			initial = p;
			for (int i = 0; i < singleton.length(); i++) {
				State q = new State();
				p.transitions.add(new Transition(singleton.charAt(i), q));
				p = q;
			}
			p.accept = true;
			deterministic = true;
			singleton = null;
		}
	}

	/**
	 * Returns the number of states in this automaton.
	 */
	public int getNumberOfStates() {
		if (isSingleton())
			return singleton.length() + 1;
		return getStates().size();
	}

	/**
	 * Returns the number of transitions in this automaton. This number is counted
	 * as the total number of edges, where one edge may be a character interval.
	 */
	public int getNumberOfTransitions() {
		if (isSingleton())
			return singleton.length();
		int c = 0;
		for (State s : getStates())
			c += s.transitions.size();
		return c;
	}

	/**
	 * Returns true if the language of this automaton is equal to the language
	 * of the given automaton. Implemented using <code>hashCode</code> and
	 * <code>subsetOf</code>.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof LinkedAutomaton))
			return false;
		LinkedAutomaton a = (LinkedAutomaton) obj;
		if (isSingleton() && a.isSingleton())
			return singleton.equals(a.singleton);
		return hashCode() == a.hashCode() && subsetOf(a) && a.subsetOf(this);
	}

	/**
	 * Returns hash code for this automaton. The hash code is based on the
	 * number of states and transitions in the minimized automaton.
	 * Invoking this method may involve minimizing the automaton.
	 */
	@Override
	public int hashCode() {
		if (hash_code == 0)
			minimize();
		return hash_code;
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
	 * Must be invoked when the stored hash code may no longer be valid.
	 */
	void clearHashCode() {
		hash_code = 0;
	}

	/**
	 * Returns a string representation of this automaton.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (isSingleton()) {
			b.append("singleton: ");
			for (char c : singleton.toCharArray())
				Transition.appendCharString(c, b);
			b.append("\n");
		} else {
			Set<State> states = getStates();
			setStateNumbers(states);
			b.append("initial state: ").append(initial.number).append("\n");
			for (State s : states)
				b.append(s.toString());
		}
		return b.toString();
	}

	/**
	 * Returns <a href="http://www.research.att.com/sw/tools/graphviz/" target="_top">Graphviz Dot</a>
	 * representation of this automaton.
	 */
	public String toDot() {
		StringBuilder b = new StringBuilder("digraph LinkedAutomaton {\n");
		b.append("  rankdir = LR;\n");
		Set<State> states = getStates();
		setStateNumbers(states);
		for (State s : states) {
			b.append("  ").append(s.number);
			if (s.accept != null)
				b.append(" [shape=doublecircle,label=\"\"];\n");
			else
				b.append(" [shape=circle,label=\"\"];\n");
			if (s == initial) {
				b.append("  initial [shape=plaintext,label=\"\"];\n");
				b.append("  initial -> ").append(s.number).append("\n");
			}
			for (Transition t : s.transitions) {
				b.append("  ").append(s.number);
				t.appendDot(b);
			}
		}
		return b.append("}\n").toString();
	}

	/**
	 * Returns a clone of this automaton, expands if singleton.
	 */
	LinkedAutomaton cloneExpanded() {
		LinkedAutomaton a = clone();
		a.expandSingleton();
		return a;
	}

	/**
	 * Returns a clone of this automaton unless <code>allow_mutation</code> is set, expands if singleton.
	 */
	LinkedAutomaton cloneExpandedIfRequired() {
		if (allow_mutation) {
			expandSingleton();
			return this;
		} else
			return cloneExpanded();
	}

	/**
	 * Returns a clone of this automaton.
	 */
	@Override
	public LinkedAutomaton clone() {
		try {
			LinkedAutomaton a = (LinkedAutomaton) super.clone();
			if (!isSingleton()) {
				HashMap<State, State> m = new HashMap<State, State>();
				Set<State> states = getStates();
				for (State s : states)
					m.put(s, new State());
				for (State s : states) {
					State p = m.get(s);
					p.accept = s.accept;
					if (s == initial)
						a.initial = p;
					for (Transition t : s.transitions)
						p.transitions.add(new Transition(t.min, t.max, m.get(t.to)));
				}
			}
			return a;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a clone of this automaton, or this automaton itself if <code>allow_mutation</code> flag is set.
	 */
	LinkedAutomaton cloneIfRequired() {
		if (allow_mutation)
			return this;
		else
			return clone();
	}

	/**
	 * Retrieves a serialized <code>LinkedAutomaton</code> located by a URL.
	 *
	 * @param url URL of serialized automaton
	 * @throws IOException            if input/output related exception occurs
	 * @throws OptionalDataException  if the data is not a serialized object
	 * @throws InvalidClassException  if the class serial number does not match
	 * @throws ClassCastException     if the data is not a serialized <code>LinkedAutomaton</code>
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static LinkedAutomaton load(URL url) throws IOException, OptionalDataException, ClassCastException,
		ClassNotFoundException, InvalidClassException {
		return load(url.openStream());
	}

	/**
	 * Retrieves a serialized <code>LinkedAutomaton</code> from a stream.
	 *
	 * @param stream input stream with serialized automaton
	 * @throws IOException            if input/output related exception occurs
	 * @throws OptionalDataException  if the data is not a serialized object
	 * @throws InvalidClassException  if the class serial number does not match
	 * @throws ClassCastException     if the data is not a serialized <code>LinkedAutomaton</code>
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static LinkedAutomaton load(InputStream stream) throws IOException, OptionalDataException, ClassCastException,
		ClassNotFoundException, InvalidClassException {
		ObjectInputStream s = new ObjectInputStream(stream);
		return (LinkedAutomaton) s.readObject();
	}

	/**
	 * Writes this <code>LinkedAutomaton</code> to the given stream.
	 *
	 * @param stream output stream for serialized automaton
	 * @throws IOException if input/output related exception occurs
	 */
	public void store(OutputStream stream) throws IOException {
		ObjectOutputStream s = new ObjectOutputStream(stream);
		s.writeObject(this);
		s.flush();
	}

	/**
	 * See {@link BasicAutomata#makeChar(char)}.
	 */
	public static LinkedAutomaton makeChar(char c) {
		return BasicAutomata.makeChar(c);
	}

	/**
	 * See {@link BasicAutomata#makeCharRange(char, char)}.
	 */
	public static LinkedAutomaton makeCharRange(char min, char max) {
		return BasicAutomata.makeCharRange(min, max);
	}


	/**
	 * See {@link BasicOperations#concatenate(LinkedAutomaton, LinkedAutomaton)}.
	 */
	public LinkedAutomaton concatenate(LinkedAutomaton a) {
		return BasicOperations.concatenate(this, a);
	}


	/**
	 * See {@link BasicOperations#optional(LinkedAutomaton)}.
	 */
	public LinkedAutomaton optional() {
		return BasicOperations.optional(this);
	}

	/**
	 * See {@link BasicOperations#repeat(LinkedAutomaton)}.
	 */
	public LinkedAutomaton repeat() {
		return BasicOperations.repeat(this);
	}

	/**
	 * See {@link BasicOperations#repeat(LinkedAutomaton, int)}.
	 */
	public LinkedAutomaton repeat(int min) {
		return BasicOperations.repeat(this, min);
	}

	/**
	 * See {@link BasicOperations#repeat(LinkedAutomaton, int, int)}.
	 */
	public LinkedAutomaton repeat(int min, int max) {
		return BasicOperations.repeat(this, min, max);
	}

	/**
	 * See {@link BasicOperations#complement(LinkedAutomaton)}.
	 */
	public LinkedAutomaton complement() {
		return BasicOperations.complement(this);
	}

	/**
	 * See {@link BasicOperations#minus(LinkedAutomaton, LinkedAutomaton)}.
	 */
	public LinkedAutomaton minus(LinkedAutomaton a) {
		return BasicOperations.minus(this, a);
	}

	/**
	 * See {@link BasicOperations#intersection(LinkedAutomaton, LinkedAutomaton)}.
	 */
	public LinkedAutomaton intersection(LinkedAutomaton a) {
		return BasicOperations.intersection(this, a);
	}

	/**
	 * See {@link BasicOperations#subsetOf(LinkedAutomaton, LinkedAutomaton)}.
	 */
	public boolean subsetOf(LinkedAutomaton a) {
		return BasicOperations.subsetOf(this, a);
	}

	/**
	 * See {@link BasicOperations#union(LinkedAutomaton, LinkedAutomaton)}.
	 */
	public LinkedAutomaton union(LinkedAutomaton a) {
		return BasicOperations.union(this, a);
	}

	/**
	 * See {@link BasicOperations#union(Collection)}.
	 */
	static public LinkedAutomaton union(Collection<LinkedAutomaton> l) {
		return BasicOperations.union(l);
	}

	/**
	 * See {@link BasicOperations#determinize(LinkedAutomaton)}.
	 */
	public void determinize() {
		BasicOperations.determinize(this);
	}

	/**
	 * See {@link BasicOperations#addEpsilons(LinkedAutomaton, Collection)}.
	 */
	public void addEpsilons(Collection<StatePair> pairs) {
		BasicOperations.addEpsilons(this, pairs);
	}

	/**
	 * See {@link BasicOperations#isEmptyString(LinkedAutomaton)}.
	 */
	public boolean isEmptyString() {
		return BasicOperations.isEmptyString(this);
	}

	/**
	 * Returns true if the given automaton accepts no strings.
	 */
	public boolean isEmpty() {
		if (isSingleton())
			return false;
		return initial.accept == null && initial.transitions.isEmpty();
	}

	/**
	 * Returns true if the given string is accepted by the automaton.
	 * <p/>
	 * Complexity: linear in the length of the string.
	 * <p/>
	 * <b>Note:</b> for full performance, use the {@link RunAutomaton} class.
	 */
	public Object run(String s) {
		if (isSingleton())
			return s.equals(singleton);
		if (deterministic) {
			State p = initial;
			for (int i = 0; i < s.length(); i++) {
				State q = p.step(s.charAt(i));
				if (q == null)
					return false;
				p = q;
			}
			return p.accept;
		} else {
			Set<State> states = getStates();
			LinkedAutomaton.setStateNumbers(states);
			LinkedList<State> pp = new LinkedList<State>();
			LinkedList<State> pp_other = new LinkedList<State>();
			BitSet bb = new BitSet(states.size());
			BitSet bb_other = new BitSet(states.size());
			pp.add(initial);
			ArrayList<State> dest = new ArrayList<State>();
			Object accept = initial.accept;
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				accept = false;
				pp_other.clear();
				bb_other.clear();
				for (State p : pp) {
					dest.clear();
					p.step(c, dest);
					for (State q : dest) {
						if (q.accept != null)
							accept = q.accept;
						if (!bb_other.get(q.number)) {
							bb_other.set(q.number);
							pp_other.add(q);
						}
					}
				}
				LinkedList<State> tp = pp;
				pp = pp_other;
				pp_other = tp;
				BitSet tb = bb;
				bb = bb_other;
				bb_other = tb;
			}
			return accept;
		}
	}

	/**
	 * Minimizes (and determinizes if not already deterministic) the given automaton.
	 *
	 * @see LinkedAutomaton#setMinimization(int)
	 */
	public LinkedAutomaton minimize() {
		if (!isSingleton()) {
			switch (LinkedAutomaton.minimization) {
				case LinkedAutomaton.MINIMIZE_HUFFMAN:
					MinimizationOperations.minimizeHuffman(this);
					break;
				case LinkedAutomaton.MINIMIZE_BRZOZOWSKI:
					MinimizationOperations.minimizeBrzozowski(this);
					break;
				default:
					MinimizationOperations.minimizeHopcroft(this);
			}
		}
		recomputeHashCode();
		return this;
	}

	/**
	 * See {@link SpecialOperations#subst(LinkedAutomaton, Map)}.
	 */
	public LinkedAutomaton subst(Map<Character, Set<Character>> map) {
		return SpecialOperations.subst(this, map);
	}
}
