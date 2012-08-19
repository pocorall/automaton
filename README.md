# A DFA/NFA library that is fast and easy to use

With pocorall.automaton, you can do:
* Search multiple strings super fast (see <a href="http://tusker.org/regex/regex_benchmark.html" target="_blank">benchmark</a>)

For a quick glance, check out our [test cases](https://github.com/pocorall/automaton/tree/master/src/test/java/net/pocorall/automaton) and [javadocs](http://pocorall.github.com/automaton/docs/api/).

The pocorall.automaton library is forked from <a href="http://www.brics.dk/automaton" target="_blank">dk.brics.automaton</a>. We extended the original code as following ways:

* [Allows state object](#state-object)
* [Downloadable from maven repository](#maven-artifact)
* [Test cases](https://github.com/pocorall/automaton/tree/master/src/test/java/net/pocorall/automaton)
* Hosted on github :-D

### State object

.. to be written..

### Maven artifact

Include these repository and dependency descriptions into your pom.

```
<repositories>
	<repository>
		<id>automaton-github</id>
		<url>https://raw.github.com/pocorall/automaton/master/mvn-repo</url>
	</repository>
</repositories>
```

```
<dependencies>
	<dependency>
		<groupId>net.pocorall</groupId>
		<artifactId>automaton</artifactId>
		<version>2.0</version>
	</dependency>
<dependencies>
```

### License

Both pocorall.automaton and original code(dk.brics.automaton) are available under BSD license.
