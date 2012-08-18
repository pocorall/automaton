package net.pocorall.automaton;

import org.junit.Test;

import static net.pocorall.automaton.BasicAutomata.*;
import static org.junit.Assert.*;

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
		assertTrue(automaton.isEmpty());
	}

	@Test
	public void testMakeEmptyString() throws Exception {

	}

	@Test
	public void testMakeAnyString() throws Exception {

	}

	@Test
	public void testMakeAnyChar() throws Exception {

	}

	@Test
	public void testMakeChar() throws Exception {

	}

	@Test
	public void testMakeCharRange() throws Exception {

	}

	@Test
	public void testMakeCharSet() throws Exception {

	}

	@Test
	public void testMakeInterval() throws Exception {

	}

	@Test
	public void testMakeString() throws Exception {

	}

	@Test
	public void testMakeStringUnion() throws Exception {

	}

	@Test
	public void testMakeMaxInteger() throws Exception {

	}

	@Test
	public void testMakeMinInteger() throws Exception {

	}

	@Test
	public void testMakeTotalDigits() throws Exception {

	}

	@Test
	public void testMakeFractionDigits() throws Exception {

	}

	@Test
	public void testMakeIntegerValue() throws Exception {

	}

	@Test
	public void testMakeDecimalValue() throws Exception {

	}

	@Test
	public void testMakeStringMatcher() throws Exception {

	}
}
