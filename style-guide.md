HypeDyn Scala Style Guide
========

This file contains style guidelines for writing Scala code that is to be used in HypeDyn. The main goal for this style
guide is to ensure readability and clarity in the code in HypeDyn. This guide is adapted from the [official Scala style
guide](http://docs.scala-lang.org/style/) and the 
[PayPal Scala Style Guidelines](https://github.com/paypal/scala-style-guide).

Some of these style guidelines are already applied by the IntelliJ Scala Plugin.

# Whitespace

* Use 2 spaces for indentation
* Add a newline to the end of every file

# Line Length

* Use a maximum line length of 120 characters

# Names

## Functions, Classes and Variables

Use camel case for all function, class and variable names. Classes start with an upper case letter, and values and
functions start with a lower case. Here are some examples:

```scala
class MyClass {
  val myValue = 1
  def myFunction: Int = myValue
}
```

### Acronyms

Use camel case for acronyms, for example, `HttpUtil`. For consistency, this includes class names like `Ttl` and `Json`
as well as common terms such as `Db` and `Io`. Follow normal camel-casing rules.

```scala
val httpIoJsonToXmlParser = new HttpIoJsonToXmlParser()
```

## Package Objects

Package names should be all lowercase with no underscores.

File names for package objects must match the most specific name in the package.
For example, for the `com.paypal.mypackage` package object, the file
should be named `mypackage.scala` and the file should be structured like this:


```scala
package com.paypal

package object mypackage {
  //...
}
```

# Classes

## Public Methods

Limit the number of public methods in your class to 30.

## Case Classes

Do not use `new` to construct new instances of case classes.

# Imports

## Ordering

IntelliJ's code style configuration allows for automatic grouping of imports by namespace.
We use the following ordering, which you should add to your IntelliJ configuration
(found in Settings > Editor > Code Style > Scala):

```
java
___blank line___
scala
___blank line___
scalafx
___blank line___
all other imports
___blank line___
org.narrativeandplay.hypedyn
```

You should also regularly run IntelliJ's Optimize Imports (Code > Optimize Imports) against
your code before committing, to maintain import cleanliness.

## Location

Most `import`s in a file should go at the top, right below the package name. The
only time you should break that rule is if you import some or all names from
an `object` from inside a `class`. For example:

```scala
package mypackage

import a.b.c.d
import d.c.b.a

class MyClass {
  import Utils._
  import MyClassCompanion.A
}

object MyClassCompanion {
  case class A()
}

object Utils {
  case class Util1()
  case class Util2()
}
```

## Predef

Never `import` anything inside of Scala's
['Predef'](http://www.scala-lang.org/api/current/index.html#scala.Predef$)
object. It is automatically imported for you, so it's redundant to manually
`import`.

# Values

Use `val` by default. `var`s should be limited to local variables or `private` class variables. Their usage
should be well documented.

`null`s are not allowed anywhere in `core` or `api`. These modules make it a point to be completely
type safe, and `null`s are a hole in the type system that violates this invariant.
 
`null`s are to be used sparingly in `ui` and `default-story-viewer` when needed to interface with
Java code that requires a null for correct operation. When receiving potentially nullable values from Java code, 
wrap it in an `Option` to make the nullability of the received value obvious.

Do **not** return, or set a value to `null` unless it is absolutely required by the API, even in `ui` and
`default-story-viewer`.

`Any` should almost never be used or inferred for the type of any value. If there is a case where `Any` is returned,
it must be explicitly noted, with an accompanying explanation for its use.

Refrain from the use of `.asInstanceOf` to type cast values to the expected type. There are cases where this is
required (especially in interfacing with Java code), but do not use them otherwise.

## Modifiers

Optional variable modifiers should be declared in the following order: `override access (private|protected) final implicit lazy`.

For example,

```scala
private implicit lazy val someSetting = ...
```

## Type Annotations

Prefer type inference to explicit type annotations whenever possible, especially when the type is obvious from the
declaration.

Do
```scala
val lst = List.empty[Int]
```

Not
```scala
val lst: List[Int] = List.empty[Int]
```

In the same vein, use the `empty` methods of collections to get an empty collection.

For example, use `val lst = List.empty[Int]` instead of `val lst = Nil`.

Always put a space after `:` characters in type annotations, if an annotation is necessary.

# Functions

Rules to follow for all functions:

* Always put a space after `:` characters in function signatures.
* Always put a space after `,` in function signatures.

## Public Functions
All public functions and methods, including those inside `object`s must have:

* A return type
* Scaladoc including a function overview, information on parameters, and information on the return value. See the 
[Scaladoc](#scaladoc-comments-and-annotations) section for more details.

Here is a complete example of a function inside of an `object`.

```scala
object MyObject {
  /**
   * returns the static integer 123
   * @return the number 123
   */
  def myFunction: Int = {
    123
  }
}
```

### Parameter Lists

If a function has a parameter list with fewer than 70 characters, put all parameters on the same line:

```scala
def add(a: Int, b: Int): Int = {
  ...
}
```

If a function has long or complex parameter lists, follow these rules:

1. Put the first parameter on the same line as the function name.
2. Put the rest of the parameters each on a new line, aligned with the first parameter.
3. If the function has multiple parameter lists, align the opening parenthesis with the previous one and align
parameters the same as #2.

Example:

```scala
def lotsOfParams(aReallyLongParameterNameOne: Int,
                 aReallyLongParameterNameTwo: Int,
                 aReallyLongParameterNameThree: Int,
                 aReallyLongParameterNameFour: Int)
                (implicit adder: Adder,
                 reader: Reader): Int = {
  ...
}
```

For function names over 30 characters, try to shorten the name. If you can't,
start the parameter list on the next line and indent everything 2 spaces:

```scala
def aVeryLongMethodThatShouldHaveAShorterNameIfPossible(
  aParam: Int,
  anotherParam: Int,
  aThirdParam: Int)
 (implicit iParam: Foo,
  bar: Bar): String = {
  ...
}
```

In all cases, the function's return type still has to be written directly
following the last closing parenthesis.

#### Calling Functions

When calling functions with numerous arguments, place the first parameter on the
same line as the function and align the remaining parameters with the first:

```scala
fooBar(someVeryLongFieldName,
       andAnotherVeryLongFieldName,
       "this is a string",
       3.1415)
```

For functions with very long names, start the parameter list on the second line
and indent by 2 spaces:

```scala
aMuchLongerMethodNameThatShouldProbablyBeRefactored(
  aParam,
  anotherParam,
  aThirdParam)
```

For clarity, place closing parenthesis on a new line (in "dangling" style):

```scala
aLongMethodNameThatReturnsAFuture(
  aParam,
  anotherParam,
  aThirdParam
) map { res =>
  ...
}
```

When calling `.apply` methods, do not explicitly invoke `.apply`. This is a standard Scala convention, and it makes no
sense to explicitly invoke `.apply` as it conveys no information to the reader about the method call that is not
already obvious from conventions.

For example, use:

```scala
Option(1)
```

instead of:

```
Option.apply(1)
```

## Anonymous functions

Anonymous functions start on the same line as preceding code. Declarations
start with ` { ` (note the space before and after the `{`). Arguments are then
listed on the same line. A few more notes:

* Place the argument list after the opening `{`, i.e. `{ arg => ... }`, not `(arg) => { ... }`
* Do not use braces after the argument list, just start the function body on the next line.
* Prefer type inference for argument types. Annotate only if type inference is unable to determine the type (e.g. in the
case of implicit conversions)

Here's a complete example example:

```scala
Option(123) map { number =>
  println(s"the number plus one is: ${number + 1}")
}
```

When type inference is unavailable for argument type:

```scala
private lazy val exit = new MenuItem("Exit") {
  onAction = { actionEvent: ActionEvent =>
    Platform.exit()
  }
}
```

Use parentheses and an underscore for anonymous functions that are:

* single binary operations
* a single method invocation on the input
* two or fewer unary methods on the input

Examples:

```scala
val list = List("list", "of", "strings")
list filter (_.length > 2)
list filter (_ contains "i")
```

## Passing named functions

If the function takes a single argument, then arguments and underscores should be omitted.

For example:
```scala
Option(123) map println
```

is preferred over

```scala
Option(123) map (println(_))
```

## Calling functions

* Omit parentheses on arity-0 method invocations when the method has no side effects, keep them otherwise
* On arity-1 methods:
    * Use infix invocation when methods have no side effects, or when the method takes a function as a parameter
        * `names mkString ","`
        * `names foreach  { n => println(n) }`
        * `names map (_ + " Surname")`
    * In all other cases, use dot notation
        * Do not do `mutable.ArrayBuffer.empty[Int] append 1`
        * Instead, do `mutable.ArrayBuffer.empty[Int].append(1)`
* On methods of higher arity:
    * When the method has no side effects, use infix notation to invoke the method, leaving a space between the method
      name and the argument list
        * `objectName method (arg1, arg2)`
    * In all other cases, use dot notation.
    
When dealing with curried functions, due to how function application works, call the curried function using dot 
notation.

For example, call `fold` like so:

```
List(1, 2, 3).fold(0)(_ + _)
```

While the following is a valid way to invoke fold, avoid it:

```
(List(1, 2, 3) fold 0) (_ + _)
```

The brackets around `List(1, 2, 3) fold 0` are necessary. Omitting the brackets will result in a compile error, 
as in the following:

```
List(1, 2, 3) fold 0 (_ + _) // Compile error
```

##### Rule Exceptions

Java <=> Scala interoperation on primitives: Scala and Java booleans, for example, are not directly compatible, and
require an implicit conversion between one another. Normally this is handled automagically by the Scala compiler, but
the following code would reveal a compiler error:

```scala
Option(true) foreach (someMethodThatTakesAJavaBoolean)
```

A named parameter is *not* necessary to achieve this. An underscore should be used to resolve the implicit conversion.
To avoid confusion, please also add a note that a Scala <=> Java conversion is taking place:

```scala
Option(true) foreach (someMethodThatTakesAJavaBoolean(_))
```

# Logic Flows

In general, logic that handles a choice between two or more outcomes should prefer to use `match`.

## Match Statements

When you `match` on any type, follow these rules:

1. Pattern matching should be exhaustive and explicitly handle the failure/default case rather than relying on a runtime 
   `MatchError`. (This is specific to match blocks and not case statements in partial functions.) Case classes used in 
   pattern matching should extend a common `sealed trait` so that compiler warnings will be generated for inexhaustive 
   matching.
2. Indent all `case` statements at the same level, and put the `=>` one space to
   the right of the closing `)`
3. Short single line expressions should be on the same line as the `case`
4. Long single line and multi-line expressions should be on the line below the case, indented one level from the `case`.
5. Do not add extra newlines in between each case statement.
6. Filters on case statements should be on the same line if doing so will not make the line excessively long.

Here's a complete example:

```scala
Option(123) match {
  case Some(i) if i > 0 =>
    val intermediate = doWorkOn(i + 1)
    doMoreWorkOn(intermediate)
  case _ => 123
}
```

## Option

Flows with `Option` values should be constructed using the `match` keyword as follows.

```scala
def stuff(i: Int): Int = { ... }

Option(123) match {
  case None => 0
  case Some(number) => stuff(number)
}
```

The `.fold` operator should generally not be used. Simple, single-line patterns are acceptable for `.fold`, such as:

```scala
def stuff(i: Int): Int = { ... }

Option(123).fold(0)(stuff)
```

Similarly, simple patterns are acceptable for `map` with `getOrElse`, such as:

```scala
def stuff(i: Int): Int = { ... }

Option(123) map stuff getOrElse 0
```

You should enforce expected type signatures, as `match` does not guarantee consistent types between match outcomes
(e.g. the `None` case could return `Int`, while the `Some` case could return `String`).

Use `Option.empty` to obtain an empty `Option` for a declaration instead of `None`, i.e.
```scala
var intOption = Option.empty[Int]
```

instead of
```scala
var intOption: Option[Int] = None
```

This allows type inference to work so that the type of the declaration can be inferred, and reduces code clutter.
`None` should still be used in other cases.

`.get` is banned because it explicitly bypasses the point of using `Option` to make explicit the nullability
of the value in the `Option`. If a value must be retrieved from an `Option`, use `getOrElse`, or perform a
pattern match.

## For Comprehension

Scala has the ability to represent for-comprehensions with more than one generator (usually, more than one <- symbol). 
In such cases, there are two alternative syntaxes which may be used:

```
// wrong!
for (x <- board.rows; y <- board.files) 
  yield (x, y)

// right!
for {
  x <- board.rows
  y <- board.files
} yield (x, y)
```

While the latter style is more verbose, it is generally considered easier to read and more “scalable” (meaning that it 
does not become obfuscated as the complexity of the comprehension increases). You should prefer this form for all 
for-comprehensions of more than one generator. Comprehensions with only a single generator 
(e.g. for (i <- 0 to 10) yield i) should use the first form (parentheses rather than curly braces).

The exceptions to this rule are for-comprehensions which lack a yield clause. In such cases, the construct is actually 
a loop rather than a functional comprehension and it is usually more readable to string the generators together between 
parentheses rather than using the syntactically-confusing `} {` construct:

```
// wrong!
for {
  x <- board.rows
  y <- board.files
} {
  printf("(%d, %d)", x, y)
}

// right!
for (x <- board.rows; y <- board.files) {
  printf("(%d, %d)", x, y)
}
```

Finally, for comprehensions are preferred to chained calls to map, flatMap, and filter, as this can get difficult to 
read (this is one of the purposes of the enhanced for comprehension).

# Tests

TODO

# Scaladoc, Comments, and Annotations

All classes, objects, traits, and methods should be documented. Generally, follow the documentation guidelines
provided by the Scala Documentation Style Guide on [ScalaDoc](http://docs.scala-lang.org/style/scaladoc.html).

The only deviation from ScalaDoc style is the style of documentation blocks. Documentation blocks use JavaDoc
comment style instead of ScalaDoc style. I.e.,

```scala
/**
 *
 */
```
instead of
```scala
/**
  *
  */
```

Rules:

* Classes that are instantiated via methods in a companion object **must** include ScalaDoc documentation with a code example.
* Abstract classes *should* be documented with an example of their intended implementation.
* Implicit wrapper classes **must** include ScalaDoc documentation with a code example.
* Public, protected, and package-private methods **must** include ScalaDoc documentation.
* Private methods *should* be documented, however it is left to the discretion of the developer as to the level of documentation.
* All methods **must** include `@throws` annotations if they throw an exception in their normal operation.

Use your best judgment otherwise, and err toward more documentation rather than less.

# Further Reading

The Twitter Scala style guide ([Effective Scala](http://twitter.github.io/effectivescala/)) is to be used if some point
of style is not covered in this document. It also makes for good reading regardless, as it provides a good set of core
practices to follow when writing Scala.

Also of interest is the [official Scala style guide](http://docs.scala-lang.org/style/). It contains much of the
reasoning behind many of the guidelines provided here, and is to be used as reference if both this and the Twitter style
guide are ambiguous or silent on some point of style. 
