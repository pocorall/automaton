package net.pocorall.automaton;

import org.junit.Test;

import java.util.IdentityHashMap;

import static org.junit.Assert.assertEquals;

public class RunAutomatonTest {
	static public class AutomatonUtil {
		public static State build(Object... input) {
			final StringUnionOperations builder = new StringUnionOperations();

			for (Object i : input) {
				builder.add(i, (CharSequence) i);
			}

			return builder.complete().toState();
		}

		public static LinkedAutomaton makeStringUnion(String... strings) {
			if (strings.length == 0)
				return BasicAutomata.makeEmpty();
			LinkedAutomaton a = new LinkedAutomaton();
			a.setInitialState(build(strings));
			a.setDeterministic(true);
			a.reduce();
			a.recomputeHashCode();
			return a;
		}
	}

	@Test
	public void testSplit() {
		LinkedAutomaton a = AutomatonUtil.makeStringUnion("a", "hi", "ab", "there!");
		AutomatonMatcher matcher = new RunAutomaton(a).newMatcher("wiesaamfijabiemfeiaymfqi");
		Object aObj = matcher.find();
		assertEquals("a", matcher.group());
		assertEquals("a", aObj);
		assertEquals("wies", matcher.token());
		while (aObj != null) {
			System.out.println(matcher.token() + "::" + matcher.group() + "::" + aObj);
			aObj = matcher.find();
		}
		System.out.println(matcher.token());
	}
}