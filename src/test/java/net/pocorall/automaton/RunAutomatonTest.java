package net.pocorall.automaton;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RunAutomatonTest {
	static public class AutomatonUtil {
		public static State build(String... input) {
			final StringUnionOperations builder = new StringUnionOperations();

			for (String i : input) {
				builder.add(i, i);
			}

			return builder.complete();
		}

		public static SingletonAutomaton makeStringUnion(String... strings) {
			if (strings.length == 0)
				return BasicAutomataFactory.makeEmpty();
			SingletonAutomaton a = new SingletonAutomaton();
			a.setInitialState(build(strings));
			a.setDeterministic(true);
			a.reduce();
			a.recomputeHashCode();
			return a;
		}
	}

	@Test
	public void testSplit() {
		SingletonAutomaton a = AutomatonUtil.makeStringUnion("a", "ab", "hi", "there!");
		System.out.println(a.toString());
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