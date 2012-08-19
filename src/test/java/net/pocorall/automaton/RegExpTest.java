package net.pocorall.automaton;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegExpTest {

	private void testTrues(RegExp regExp, String stringRep, String[] falses, String... strings) {
		Automaton a = regExp.toAutomaton();
//		System.out.println(regExp);
		assertEquals(regExp.toString(), stringRep);
		for (String string : strings) {
			assertEquals(Boolean.TRUE, a.run(string));
		}
		for (String neg : falses) {
			assertEquals(Boolean.FALSE, a.run(neg));
		}
	}

	@Test
	public void testRegExp() throws Exception {
		testTrues(new RegExp("f[A-Z]+q"), "\\f([\\A-\\Z]){1,}\\q", new String[]{}, "fAq");

		testTrues(RegExp.makeAnyString(), "@", new String[]{}, "automaton", "anything");

		testTrues(RegExp.makeString("SomeThIng"), "\"SomeThIng\"", new String[]{"anything"}, "SomeThIng");

		testTrues(RegExp.makeRepeat(new RegExp("some")), "(\"some\")*",
			new String[]{"any", "some some"}, "somesome", "");
	}

}
