/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2012 Anders Moeller and Sung-Ho Lee
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
 * @author Sung-Ho Lee
 */
public class DefaultAutomaton extends LinkedAutomaton {

	static final long serialVersionUID = 10001;


	/**
	 * Singleton string. Null if not applicable.
	 */
	String singleton;

	/**
	 * Constructs a new automaton that accepts the empty language.
	 * Using this constructor, automata can be constructed manually from
	 * {@link State} and {@link Transition} objects.
	 *
	 * @see #setInitialState(State)
	 * @see State
	 * @see Transition
	 */
	public DefaultAutomaton() {
		super();
		singleton = null;
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
		super.setInitialState(s);
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
		super.reduce();
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


	/**
	 * Removes transitions to dead states and calls {@link #reduce()} and {@link #clearHashCode()}.
	 * (A state is "dead" if no accept state is reachable from it.)
	 */
	public void removeDeadTransitions() {
		clearHashCode();
		if (isSingleton())
			return;
		super.removeDeadTransitions();
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
		return super.getNumberOfStates();
	}

	/**
	 * Returns the number of transitions in this automaton. This number is counted
	 * as the total number of edges, where one edge may be a character interval.
	 */
	public int getNumberOfTransitions() {
		if (isSingleton())
			return singleton.length();
		return super.getNumberOfTransitions();
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
		if (!(obj instanceof DefaultAutomaton))
			return false;
		DefaultAutomaton a = (DefaultAutomaton) obj;
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
	 * Returns a clone of this automaton, expands if singleton.
	 */
	DefaultAutomaton cloneExpanded() {
		DefaultAutomaton a = clone();
		a.expandSingleton();
		return a;
	}

	/**
	 * Returns a clone of this automaton unless <code>allow_mutation</code> is set, expands if singleton.
	 */
	DefaultAutomaton cloneExpandedIfRequired() {
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
	public DefaultAutomaton clone() {
		return (DefaultAutomaton) super.clone();
	}

	/**
	 * Returns a clone of this automaton, or this automaton itself if <code>allow_mutation</code> flag is set.
	 */
	DefaultAutomaton cloneIfRequired() {
		if (allow_mutation)
			return this;
		else
			return clone();
	}


	/**
	 * See {@link BasicOperations#concatenate(DefaultAutomaton, DefaultAutomaton)}.
	 */
	public DefaultAutomaton concatenate(DefaultAutomaton a) {
		return BasicOperations.concatenate(this, a);
	}


	/**
	 * Returns an automaton that accepts the union of the empty string and the
	 * language of the given automaton.
	 * <p/>
	 * Complexity: linear in number of states.
	 */
	public DefaultAutomaton optional() {
		DefaultAutomaton a = cloneExpandedIfRequired();
		State s = new State();
		s.addEpsilon(a.initial);
		s.accept = true;
		a.initial = s;
		a.deterministic = false;
		a.clearHashCode();
		a.checkMinimizeAlways();
		return a;
	}

	/**
	 * See {@link BasicOperations#repeat(DefaultAutomaton)}.
	 */
	public DefaultAutomaton repeat() {
		return BasicOperations.repeat(this);
	}

	/**
	 * See {@link BasicOperations#complement(DefaultAutomaton)}.
	 */
	public DefaultAutomaton complement() {
		return BasicOperations.complement(this);
	}

	/**
	 * See {@link BasicOperations#minus(DefaultAutomaton, DefaultAutomaton)}.
	 */
	public DefaultAutomaton minus(DefaultAutomaton a) {
		return BasicOperations.minus(this, a);
	}

	/**
	 * See {@link BasicOperations#intersection(DefaultAutomaton, DefaultAutomaton)}.
	 */
	public DefaultAutomaton intersection(DefaultAutomaton a) {
		return BasicOperations.intersection(this, a);
	}

	/**
	 * See {@link BasicOperations#subsetOf(DefaultAutomaton, DefaultAutomaton)}.
	 */
	public boolean subsetOf(DefaultAutomaton a) {
		return BasicOperations.subsetOf(this, a);
	}

	/**
	 * See {@link BasicOperations#union(DefaultAutomaton, DefaultAutomaton)}.
	 */
	@Deprecated
	public DefaultAutomaton union(DefaultAutomaton a) {
		return BasicOperations.union(this, a);
	}


	/**
	 * See {@link BasicOperations#isEmptyString(DefaultAutomaton)}.
	 */
	@Deprecated
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
		return super.run(s);
	}

	/**
	 * Minimizes (and determinizes if not already deterministic) the given automaton.
	 *
	 * @see LinkedAutomaton#setMinimization(int)
	 */
	public DefaultAutomaton minimize() {
		if (!isSingleton()) {
			super.minimize();
		}
		recomputeHashCode();
		return this;
	}

	/**
	 * See {@link SpecialOperations#subst(DefaultAutomaton, Map)}.
	 */
	public DefaultAutomaton subst(Map<Character, Set<Character>> map) {
		return SpecialOperations.subst(this, map);
	}
}
