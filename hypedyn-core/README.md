Core Module
===========

# Story Export Resources

Mostly for sanity reasons, the resources required for exporting a story are not directly developed in JS, CSS, and HTML.
Instead, the resources are developed in [CoffeeScript](http://coffeescript.org/), [Sass](http://sass-lang.com/), 
[Slim](http://slim-lang.com/), which compile into JS, CSS, and HTML respectively. All JS, CSS, and HTML files are
auto-generated, and any changes to them will be automatically overwritten by the compile process.

The package for the resources is `org.narrativeandplay.hypedyn.serialisation.export`, and contains the compiled HTML
files. The subpackages `javascripts` and `stylesheets` contain the compiled JS and CSS files respectively. The
subpackage `dev` mirrors this package layout, and contains the development resources.

## Compiling

Requirements:

* CoffeeScript
* Sass
* Slim

The projects' respective GitHub pages will contain the necessary instructions to install the compilers. The compilers
can be run via the commandline. Alternatively, for those using IntelliJ IDEA (or its derivatives like PyCharm, RubyMine,
etc.), the exported file watchers is provided for simplicity. The provided file watchers assume that the compilers are
available on your PATH, so they will error is this is not so.