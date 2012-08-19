# A DFA/NFA library that is fast and easy to use

With pocorall.automaton, you can do:
* Search multiple strings super fast (see <a href="http://tusker.org/regex/regex_benchmark.html" target="_blank">benchmark</a>)

For a quick glance, check out our [test cases](https://github.com/pocorall/automaton/tree/master/src/test/java/net/pocorall/automaton) and [javadocs](http://pocorall.github.com/automaton/docs/api/).

### [Download latest version(2.0)](https://github.com/pocorall/automaton/raw/master/mvn-repo/net/pocorall/automaton/2.0/automaton-2.0.jar) or [include maven artifact](#maven-artifact)

The pocorall.automaton library is forked from <a href="http://www.brics.dk/automaton" target="_blank">dk.brics.automaton</a>. We extended the original code as following ways:

* [Allows state object](#state-object)
* [Downloadable from maven repository](#maven-artifact)
* [Test cases](https://github.com/pocorall/automaton/tree/master/src/test/java/net/pocorall/automaton)
* Hosted on github :-D

### State object

For each state, conventional state machine has a boolean property that specifies it is acceptable or not. However, in many contexts, we need to bound an object which describes extra informations about that state. In this library, a state object is received for every matching:

```
AutomatonMatcher matcher = ...;
Object stateObj = matcher.find();
```

The find() method returns null when no matches are found.


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

### Contributors

This library is maintained by Sung-Ho Lee

Original code is developed by Anders Møller at Aarhus University, with contributions, suggestions and bug reports from Alexandar Bakic, Jodi Moran, Brandon Lee, David Lutterkort, John Gibson, Alex Meyer, Daniel Lowe, Harald Zauner, Dawid Weiss, Robert Muir, Hans-Martin Adorf, Dale Richardson, Yannick Versley, and Gustaf Lundh. 