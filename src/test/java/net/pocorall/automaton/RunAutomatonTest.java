package net.pocorall.automaton;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class RunAutomatonTest {
	static public class PatternMatcher {
        private final StringUnionOperations builder = new StringUnionOperations();

        public PatternMatcher add(String key, Object value) {
            builder.add(value, key);
            return this;
        }

        public RunAutomaton build() {
            DefaultAutomaton a = new DefaultAutomaton();
            a.setInitialState(builder.complete());
            a.setDeterministic(true);
            a.reduce();
            a.recomputeHashCode();
            return new RunAutomaton(a);
        }
	}

	@Test
	public void testSplit() {
        RunAutomaton patterns = new PatternMatcher().add("a", new Date()).add("ab", 32).add("hi", "smile").add("there!", 3.141592).build();
        System.out.println(patterns.toString());
        String[] texts = new String[] {"wiesaamfijabiemfeiaymfqthere!i", "irmvoejrijaijrigmmrigjhiiej"};

        for(String text: texts) {
            RunAutomatonMatcher matcher = patterns.newMatcher(text);
            Object aObj = matcher.find();
            while (aObj != null) {
                System.out.println(matcher.token() + "::" + matcher.group() + "::" + aObj);
                aObj = matcher.find();
            }
            System.out.println(matcher.token());
        }
	}
}