# wong-hybrid

> **Work in progress.** This is an experiment in serving WebObjects and ng-objects from the same application, kept as small as possible so the mechanism is easy to read. The frameworks and libraries involved are under development, our goal is to simplify this process.

A minimal application that serves **WebObjects** (via [wonder-slim](https://github.com/undur/wonder-slim)) and [**ng-objects**](https://github.com/undur/ng-objects) side by side, from one Jetty server, on one port.

Two pages, one per framework, each with a click counter:

| URL | Served by | What it shows |
|-----|-----------|---------------|
| `/` | WebObjects | A `WOComponent` with a classic component action |
| `/ng/` | ng-objects | An `NGComponent` with an Ajax component action |

But the pages are not the point — the point is that both frameworks answer on the same port and that the whole wiring fits in one class.

## How it works

Both frameworks have Jetty-based adaptors ([wo-adaptor-jetty](https://github.com/undur/wo-adaptor-jetty) and ng-objects' own `ng-adaptor-jetty`), and both expose their request handling as a plain Jetty `Handler`. So the hybrid is a Jetty server with two handlers in a sequence.

The process is a regular WO application, started through `ERXApplication.main()`. Its `Application` class implements `WOAdaptorJetty.JettyServerProvider`, which lets it build the Jetty server that WO serves through instead of leaving that to the adaptor. It builds the server like this:

1. A `Server` with one HTTP connector on WO's port.
2. WO's handler, `WOJettyHandler`, which dispatches into the `ERXApplication` it lives in.
3. An `NGApplication` instance, created with `NGApplication.create()` rather than `run()` so ng-objects does not start an adaptor of its own, wrapped in ng-objects' `NGJettyHandler`.
4. A `Handler.Sequence` holding the two handlers, WO first.

A request then travels like this:

1. Jetty hands the request to the sequence.
2. The WO handler dispatches it through WO. If WO has an answer, that is the response.
3. If WO's route table has no route for the URL, its not-found handler marks the 404 as *unhandled*. The WO handler notices the mark, drops the response and returns `false`, which tells Jetty to try the next handler.
4. The ng-objects handler dispatches the request through `NGHybridApplication`'s routes.

So the rule is: WO answers what it knows about, ng-objects answers what is left. All of this is in `src/main/java/wong/Application.java`, with comments.

## Layout

```
pom.xml                                   Maven build (vermilingua: "woapplication" packaging and the run goal)
build.properties                          WO project descriptor (principal class, project name)
src/main/java/wong/Application.java       The WO application, and the Jetty server setup for both frameworks
src/main/java/wong/Session.java           The WO session
src/main/java/wong/components/WOPage.java
src/main/components/WOPage.wo/         The WO page served at /
src/main/java/wong/ng/NGHybridApplication.java   The ng-objects application and its routes
src/main/java/wong/ng/components/NGPage.java
src/main/resources/ng/app/components/NGPage.wo/  The ng-objects page served at /ng/
src/main/woresources/Properties           WO properties (adaptor, logging)
```

## Running it

You need Java 25 and Maven, nothing else: every dependency is a released version, so nothing has to be built from source. wonder-slim, WebObjects itself and [wo-adaptor-jetty 0.9.0](https://github.com/undur/wo-adaptor-jetty/releases/tag/v0.9.0) come from the WOCommunity repository, which the pom declares, and ng-objects and vermilingua come from Maven Central.

Build and run:

```
mvn vermilingua:run
```

This has vermilingua build the WO application bundle in `target/` and start it through the bundle's own launch script, on port 1200 by default. Arguments for the application go in `run.args`, for example `mvn vermilingua:run -Drun.args="-WOPort 1201"`. Then open <http://localhost:1200/> and follow the link to the ng-objects page. Ctrl-C stops it.

In Eclipse, import it with **File → Import → Existing Maven Projects** and launch `wong.Application` the way you would launch any WO application.