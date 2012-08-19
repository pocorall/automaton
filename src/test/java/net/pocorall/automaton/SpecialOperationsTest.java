package net.pocorall.automaton;

import org.junit.Test;

import static net.pocorall.automaton.BasicAutomataFactory.*;
import static net.pocorall.automaton.SpecialOperations.*;
import static org.junit.Assert.*;

public class SpecialOperationsTest {
	@Test
	public void testReverse() throws Exception {
		DefaultAutomaton automaton = makeCharRange('b', 'd');
		reverse(automaton);
		assertEquals(Boolean.TRUE, automaton.run("b"));
		assertEquals(Boolean.FALSE, automaton.run("aa"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testOverlap() throws Exception {
		// implement
	}

	@Test
	public void testSingleChars() throws Exception {
		DefaultAutomaton automaton = singleChars(makeString("bad dream"));
		assertEquals(Boolean.TRUE, automaton.run("b"));
		assertEquals(Boolean.FALSE, automaton.run("aa"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testTrim() throws Exception {
		DefaultAutomaton automaton = trim(makeString("to_be"), "- ", '_');
		assertEquals(Boolean.TRUE, automaton.run("to be"));
		assertEquals(Boolean.TRUE, automaton.run("  to-be  "));
		assertEquals(Boolean.FALSE, automaton.run("two be"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testCompress() throws Exception {
		DefaultAutomaton automaton = compress(makeString("hello * world"), "new", '*');
		assertEquals(Boolean.TRUE, automaton.run("hello * world"));
		assertEquals(Boolean.TRUE, automaton.run("hello new world"));
		assertEquals(Boolean.FALSE, automaton.run("hello NEW world"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testSubst() throws Exception {
		DefaultAutomaton automaton = subst(makeString("hello * world"), '*', "new");
		assertEquals(Boolean.FALSE, automaton.run("hello * world"));
		assertEquals(Boolean.TRUE, automaton.run("hello new world"));
		assertEquals(Boolean.FALSE, automaton.run("hello NEW world"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}


	@Test
	public void testHomomorph() throws Exception {
		// implement
	}

	@Test
	public void testProjectChars() throws Exception {
		// implement
	}

	@Test
	public void testIsFinite() throws Exception {
		assertTrue(isFinite(makeString("hello * world")));
		assertFalse(isFinite(trim(makeString("to_be"), "- ", '_')));
	}

	@Test
	public void testGetStrings() throws Exception {
		assertEquals(0, getStrings(makeString("hello * world"), 3).size());
		assertEquals(2, getStrings(makeStringUnion("hello * world", "abc", "defg", "def"), 3).size());
	}

	@Test
	public void testGetFiniteStrings() throws Exception {
		assertEquals(4, getFiniteStrings(makeStringUnion("hello * world", "abc", "defg", "def"), 5).size());
	}


	@Test
	public void testGetCommonPrefix() throws Exception {
		assertEquals("", getCommonPrefix(makeStringUnion("hello * world", "abc", "defg", "def")));
		assertEquals("al", getCommonPrefix(makeStringUnion("allow", "ale", "algorithm", "aladin")));
	}

	@Test
	public void testPrefixClose() throws Exception {
		DefaultAutomaton automaton = makeStringUnion("hello * world", "abc", "defg", "def");
		prefixClose(automaton);
		assertEquals(Boolean.TRUE, automaton.run("hell"));
	}

	@Test
	public void testHexCases() throws Exception {
		DefaultAutomaton automaton = makeString("hello * world");
		automaton = hexCases(automaton);
		assertEquals(Boolean.TRUE, automaton.run("hello * world"));
		assertEquals(Boolean.TRUE, automaton.run("hEllo * worlD"));
		assertEquals(Boolean.FALSE, automaton.run("hellO * worlD"));
	}

	@Test
	public void testReplaceWhitespace() throws Exception {
		DefaultAutomaton automaton = makeString("hello * world");
		automaton = hexCases(automaton);
		automaton = replaceWhitespace(automaton);
		assertEquals(Boolean.TRUE, automaton.run("hello\n*\tworld"));
		assertEquals(Boolean.TRUE, automaton.run("hEllo * worlD"));
		assertEquals(Boolean.FALSE, automaton.run("hellO * worlD"));
	}
}
