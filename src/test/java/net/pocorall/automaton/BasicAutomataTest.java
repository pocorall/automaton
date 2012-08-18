package net.pocorall.automaton;

import org.junit.Ignore;
import org.junit.Test;

import static net.pocorall.automaton.BasicAutomata.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: onsquare
 * Date: 12. 8. 19
 * Time: 오전 12:58
 * To change this template use File | Settings | File Templates.
 */
public class BasicAutomataTest {
	@Test
	public void testMakeEmpty() throws Exception {
		LinkedAutomaton automaton = makeEmpty();
		assertTrue(automaton.isDeterministic());
		assertFalse(automaton.isSingleton());
		assertTrue(automaton.isEmpty());
	}

	@Test
	public void testMakeEmptyString() throws Exception {
		LinkedAutomaton automaton = makeEmptyString();
		assertTrue(automaton.isDeterministic());
		assertTrue(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeAnyString() throws Exception {
		LinkedAutomaton automaton = makeAnyString();
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run("Any string can be accepted"));
		assertEquals(Boolean.TRUE, automaton.run("Anything!"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeAnyChar() throws Exception {
		LinkedAutomaton automaton = makeAnyChar();
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run("a"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeChar() throws Exception {
		LinkedAutomaton automaton = makeChar('t');
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("a"));
		assertEquals(Boolean.TRUE, automaton.run("t"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertTrue(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeCharRange() throws Exception {
		LinkedAutomaton automaton = makeCharRange('b', 'd');
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("a"));
		assertEquals(Boolean.TRUE, automaton.run("c"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeCharSet() throws Exception {
		LinkedAutomaton automaton = makeCharSet("bd");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("c"));
		assertEquals(Boolean.TRUE, automaton.run("b"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeInterval() throws Exception {
		LinkedAutomaton automaton = makeInterval(3, 534, 5);
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("999"));
		assertEquals(Boolean.FALSE, automaton.run("00001"));
		assertEquals(Boolean.FALSE, automaton.run("000101"));
		assertEquals(Boolean.TRUE, automaton.run("00101"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeString() throws Exception {
		LinkedAutomaton automaton = makeString("exactly the same");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("000101"));
		assertEquals(Boolean.TRUE, automaton.run("exactly the same"));
		assertEquals(Boolean.FALSE, automaton.run("Not a string"));
		assertTrue(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeStringUnion() throws Exception {
		LinkedAutomaton automaton = makeStringUnion("to be", "not to be");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("To be"));
		assertEquals(Boolean.TRUE, automaton.run("not to be"));
		assertEquals(Boolean.FALSE, automaton.run("that is the question"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeMaxInteger() throws Exception {
		LinkedAutomaton automaton = makeMaxInteger("386");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("-4"));
		assertEquals(Boolean.TRUE, automaton.run("2"));
		assertEquals(Boolean.TRUE, automaton.run("02"));
		assertEquals(Boolean.FALSE, automaton.run("0486"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	// bug found??
	@Test
	@Ignore
	public void testMakeMinInteger() throws Exception {
		LinkedAutomaton automaton = makeMinInteger("386");
		System.out.println(automaton.toString());
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.FALSE, automaton.run("-4"));
		assertEquals(Boolean.TRUE, automaton.run("0486"));
		assertEquals(Boolean.FALSE, automaton.run("2"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeTotalDigits() throws Exception {
		LinkedAutomaton automaton = makeTotalDigits(3);
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run(" -4"));
		assertEquals(Boolean.FALSE, automaton.run("2e3"));
		assertEquals(Boolean.TRUE, automaton.run("02"));
		assertEquals(Boolean.TRUE, automaton.run("0486 "));
		assertEquals(Boolean.FALSE, automaton.run("38829"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeFractionDigits() throws Exception {
		LinkedAutomaton automaton = makeFractionDigits(3);
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run(" -4.3"));
		assertEquals(Boolean.FALSE, automaton.run("2.1292"));
		assertEquals(Boolean.TRUE, automaton.run("02"));
		assertEquals(Boolean.TRUE, automaton.run("0486342434 "));
		assertEquals(Boolean.FALSE, automaton.run("38829.12923"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeIntegerValue() throws Exception {
		LinkedAutomaton automaton = makeIntegerValue("342");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run(" 00342 "));
		assertEquals(Boolean.FALSE, automaton.run("0+342"));
		assertEquals(Boolean.TRUE, automaton.run("+342  "));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeDecimalValue() throws Exception {
		LinkedAutomaton automaton = makeDecimalValue("342.02");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run(" 00342.0200 "));
		assertEquals(Boolean.FALSE, automaton.run("+342.002"));
		assertEquals(Boolean.TRUE, automaton.run("+342.02  "));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}

	@Test
	public void testMakeStringMatcher() throws Exception {
		LinkedAutomaton automaton = makeStringMatcher("to be");
		assertTrue(automaton.isDeterministic());
		assertEquals(Boolean.TRUE, automaton.run("To be or not to be"));
		assertNull(automaton.run("Two bees or three bees"));
		assertFalse(automaton.isSingleton());
		assertFalse(automaton.isEmpty());
	}
}
