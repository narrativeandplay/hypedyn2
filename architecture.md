# Architecture

HypeDyn is split into 4 modules:

* `api` - contains the API that the other modules and plugins use to communicate
* `core` - the backend of the system, handling things such as data structure management, serialisation, etc.
* `ui` - contains the UI of the system, including the main entry point into the application
* `default-story-viewer` - contains an implementation of the story viewer, for story visualisation

## Overview

The main program flow is similar to how the HTTP request/response model works: the UI/plugins initiate requests for
actions to be done, the core responds with an acknowledgement of the request, including any information required to
carry out the action. The UI/plugins then issue update requests (in the sense that a data store is changed), the core
performs the update requests, then issues events that signal the completion of update requests.

There are exceptions to this flow (e.g. the story viewer issues node selection events, which are completion events
rather than requests), but by and large that flow is adhered to.

By using events as the communication channel between major components, there is a lot of freedom afforded to the
implementations, so long as the right events are issued/responded to.

## Backend Design

The design of the backend follows a simple principle: 
[Imperative shell, functional core](https://www.destroyallsoftware.com/talks/boundaries). All data structures in the
backend are immutable, so testing (whenever tests are added) the data structures are fairly trivial. The mutable state
needed to track program state is then corralled into the controllers. This means that there is only _one_ point of 
mutable state, and this makes state much easier to reason about.

## UI Design

The UI toolkit used in HypeDyn is JavaFX, more specifically, a Scala wrapper over JavaFX, called ScalaFX. JavaFX (and
consequently ScalaFX) has something called Observables, which track values which change over time. We can write
bindings that track some property of the value we want to use, and this binding would then change when the value
changes. I believe this is the concept of reactive programming, where instead of dealing with values, we deal with
_streams_ of values. There has been much attempt to make use of observables (in the form of Properties) as much as
possible in UI code, so that whenever updates are needed (e.g. in the node editor when the story updates) updating
is a simple affair.

The main issue with properties, is that properties are mutable objects. This makes state very difficult to reason about,
especially in the node editor, which deals with a huge amount of state. While care has been taken to try and not
change things unrelated to the component, it's hard to say whether it's entirely free from mutable state issues.

## API Design

The API is mostly a straightforward affair: it contains interfaces and non-inheritable classes for communication
between the backend and the UI. The more complex issue is the design of the rules and facts.

Facts are effectively designed as a union type: a fact is a number fact _or_ a boolean fact _or_ a string fact. The
implementation is fairly simple, but getting the UI to work with an set of _types_ rather than an enumeration of some
sort is not simple. This is the reason there exists a list of strings that maps each fact type to a string, so that
the types can be enumerated.

Rules were designed from the definition, i.e. _how_ were the different types of rules defined informed how the instances
of the rules were designed. Rule definitions (referring to actions and conditions as a whole) are almost like defining
a programming language within a programming language using strings. And instances of rules are simply strings chained
together appropriately for a given rule type. For obvious reasons, this way of defining rules is not the most sound,
but because of the complexity of the rules, this was a simple way of implementing them. This way also afforded the
ability of iterating over the fields of a definition simply, which allowed for auto-generation of the UI, which makes
future rule definitions simple.

A future work would be to find a way to consistently define rule types and fact types, not using strings, but using
the type system. The main issue to overcome is the problem of enumerating defined types, and enumerating over the
fields of a class definition without instantiating the class.
