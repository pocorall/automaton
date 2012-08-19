package net.pocorall.automaton;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;

/**
 * Operations for building minimal deterministic automata from sets of strings.
 * The algorithm requires sorted input data, but is very fast (nearly linear with the input size).
 *
 * @author Dawid Weiss and Sung-Ho Lee
 */
final public class StringUnionOperations {

	/**
	 * Lexicographic order of input sequences.
	 */
	public final static Comparator<CharSequence> LEXICOGRAPHIC_ORDER = new Comparator<CharSequence>() {
		public int compare(CharSequence s1, CharSequence s2) {
			final int lens1 = s1.length();
			final int lens2 = s2.length();
			final int max = Math.min(lens1, lens2);

			for (int i = 0; i < max; i++) {
				final char c1 = s1.charAt(i);
				final char c2 = s2.charAt(i);
				if (c1 != c2)
					return c1 - c2;
			}
			return lens1 - lens2;
		}
	};

	/**
	 * State with <code>char</code> labels on transitions.
	 */
	private final static class CharState {

		/**
		 * An empty set of labels.
		 */
		private final static char[] NO_LABELS = new char[0];

		/**
		 * An empty set of states.
		 */
		private final static CharState[] NO_STATES = new CharState[0];

		/**
		 * Labels of outgoing transitions. Indexed identically to {@link #charStates}.
		 * Labels must be sorted lexicographically.
		 */
		char[] labels = NO_LABELS;

		/**
		 * States reachable from outgoing transitions. Indexed identically to
		 * {@link #labels}.
		 */
		CharState[] charStates = NO_STATES;

		/**
		 * <code>true</code> if this state corresponds to the end of at least one
		 * input sequence.
		 */
		Object is_final;

		/**
		 * Returns the target state of a transition leaving this state and labeled
		 * with <code>label</code>. If no such transition exists, returns
		 * <code>null</code>.
		 */
		public CharState getState(char label) {
			final int index = Arrays.binarySearch(labels, label);
			return index >= 0 ? charStates[index] : null;
		}

		/**
		 * Returns an array of outgoing transition labels. The array is sorted in
		 * lexicographic order and indexes correspond to states returned from
		 * {@link #getCharStates()}.
		 */
		public char[] getTransitionLabels() {
			return this.labels;
		}

		/**
		 * Returns an array of outgoing transitions from this state. The returned
		 * array must not be changed.
		 */
		public CharState[] getCharStates() {
			return this.charStates;
		}

		/**
		 * Two states are equal if:
		 * <ul>
		 * <li>they have an identical number of outgoing transitions, labeled with
		 * the same labels</li>
		 * <li>corresponding outgoing transitions lead to the same states (to states
		 * with an identical right-language).
		 * </ul>
		 */
		@Override
		public boolean equals(Object obj) {
			final CharState other = (CharState) obj;
			return is_final == other.is_final
				&& Arrays.equals(this.labels, other.labels)
				&& referenceEquals(this.charStates, other.charStates);
		}

		/**
		 * Return <code>true</code> if this state has any children (outgoing
		 * transitions).
		 */
		public boolean hasChildren() {
			return labels.length > 0;
		}

		/**
		 * Is this state a final state in the automaton?
		 */
		public Object isFinal() {
			return is_final;
		}

		/**
		 * Compute the hash code of the <i>current</i> status of this state.
		 */
		@Override
		public int hashCode() {
			int hash = is_final != null ? 1 : 0;

			hash ^= hash * 31 + this.labels.length;
			for (char c : this.labels)
				hash ^= hash * 31 + c;

			/*
			 * Compare the right-language of this state using reference-identity of
			 * outgoing states. This is possible because states are interned (stored
			 * in registry) and traversed in post-order, so any outgoing transitions
			 * are already interned.
			 */
			for (CharState s : this.charStates) {
				hash ^= System.identityHashCode(s);
			}

			return hash;
		}

		/**
		 * Create a new outgoing transition labeled <code>label</code> and return
		 * the newly created target state for this transition.
		 */
		CharState newState(char label) {
			assert Arrays.binarySearch(labels, label) < 0 : "State already has transition labeled: "
				+ label;

			labels = copyOf(labels, labels.length + 1);
			charStates = copyOf(charStates, charStates.length + 1);

			labels[labels.length - 1] = label;
			return charStates[charStates.length - 1] = new CharState();
		}

		/**
		 * Return the most recent transitions's target state.
		 */
		CharState lastChild() {
			assert hasChildren() : "No outgoing transitions.";
			return charStates[charStates.length - 1];
		}

		/**
		 * Return the associated state if the most recent transition
		 * is labeled with <code>label</code>.
		 */
		CharState lastChild(char label) {
			final int index = labels.length - 1;
			CharState s = null;
			if (index >= 0 && labels[index] == label) {
				s = charStates[index];
			}
			assert s == getState(label);
			return s;
		}

		/**
		 * Replace the last added outgoing transition's target state with the given
		 * state.
		 */
		void replaceLastChild(CharState charState) {
			assert hasChildren() : "No outgoing transitions.";
			charStates[charStates.length - 1] = charState;
		}

		public net.pocorall.automaton.State toState() {
			return convert(this, new IdentityHashMap<CharState, net.pocorall.automaton.State>());
		}

		/**
		 * Internal recursive traversal for conversion.
		 */
		private static net.pocorall.automaton.State convert(CharState s,
															IdentityHashMap<CharState, net.pocorall.automaton.State> visited) {
			net.pocorall.automaton.State converted = visited.get(s);
			if (converted != null)
				return converted;

			converted = new net.pocorall.automaton.State();
			converted.setAccept(s.is_final);

			visited.put(s, converted);
			int i = 0;
			char[] labels = s.labels;
			for (CharState target : s.charStates) {
				converted.addTransition(new Transition(labels[i++], convert(target, visited)));
			}

			return converted;
		}

		/**
		 * JDK1.5-replacement of {@link Arrays#copyOf(char[], int)}
		 */
		private static char[] copyOf(char[] original, int newLength) {
			char[] copy = new char[newLength];
			System.arraycopy(original, 0, copy, 0, Math.min(original.length,
				newLength));
			return copy;
		}

		/**
		 * JDK1.5-replacement of {@link Arrays#copyOf(char[], int)}
		 */
		public static CharState[] copyOf(CharState[] original, int newLength) {
			CharState[] copy = new CharState[newLength];
			System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
			return copy;
		}

		/**
		 * Compare two lists of objects for reference-equality.
		 */
		private static boolean referenceEquals(Object[] a1, Object[] a2) {
			if (a1.length != a2.length)
				return false;

			for (int i = 0; i < a1.length; i++)
				if (a1[i] != a2[i])
					return false;

			return true;
		}
	}

	/**
	 * "register" for state interning.
	 */
	private HashMap<CharState, CharState> register = new HashMap<CharState, CharState>();

	/**
	 * Root automaton state.
	 */
	private CharState root = new CharState();

	/**
	 * Previous sequence added to the automaton in {@link #add(Object, CharSequence)}.
	 */
	private StringBuilder previous;

	public void addAll(Object acceptObj, CharSequence... sequences) {
		for (CharSequence sequence : sequences) {
			add(acceptObj, sequence);
		}
	}

	/**
	 * Add another character sequence to this automaton. The sequence must be
	 * lexicographically larger or equal compared to any previous sequences
	 * added to this automaton (the input must be sorted).
	 */
	public void add(Object acceptObj, CharSequence current) {
		assert register != null : "DefaultAutomaton already built.";
		assert current.length() > 0 : "Input sequences must not be empty.";
		assert previous == null || LEXICOGRAPHIC_ORDER.compare(previous, current) <= 0 :
			"Input must be sorted: " + previous + " >= " + current;
		assert setPrevious(current);

		// Descend in the automaton (find matching prefix). 
		int pos = 0, max = current.length();
		CharState next, charState = root;
		while (pos < max && (next = charState.lastChild(current.charAt(pos))) != null) {
			charState = next;
			pos++;
		}

		if (charState.hasChildren())
			replaceOrRegister(charState);

		addSuffix(charState, current, pos, acceptObj);
	}

	/**
	 * Finalize the automaton and return the root state. No more strings can be
	 * added to the builder after this call.
	 *
	 * @return Root automaton state.
	 */
	public State complete() {
		if (this.register == null)
			throw new IllegalStateException();

		if (root.hasChildren())
			replaceOrRegister(root);

		register = null;
		return root.toState();
	}


	/**
	 * Build a minimal, deterministic automaton from a sorted list of strings.
	 */
	public static net.pocorall.automaton.State build(CharSequence[] input) {
		final StringUnionOperations builder = new StringUnionOperations();

		for (CharSequence chs : input)
			builder.add(Boolean.TRUE, chs);

		return builder.complete();
	}

	/**
	 * Copy <code>current</code> into an internal buffer.
	 */
	private boolean setPrevious(CharSequence current) {
		if (previous == null)
			previous = new StringBuilder();

		previous.setLength(0);
		previous.append(current);

		return true;
	}

	/**
	 * Replace last child of <code>state</code> with an already registered
	 * state or register the last child state.
	 */
	private void replaceOrRegister(CharState charState) {
		final CharState child = charState.lastChild();

		if (child.hasChildren())
			replaceOrRegister(child);

		final CharState registered = register.get(child);
		if (registered != null) {
			charState.replaceLastChild(registered);
		} else {
			register.put(child, child);
		}
	}

	/**
	 * Add a suffix of <code>current</code> starting at <code>fromIndex</code>
	 * (inclusive) to state <code>state</code>.
	 */
	private void addSuffix(CharState charState, CharSequence current, int fromIndex, Object acceptObj) {
		final int len = current.length();
		for (int i = fromIndex; i < len; i++) {
			charState = charState.newState(current.charAt(i));
		}
		charState.is_final = acceptObj;
	}
}
