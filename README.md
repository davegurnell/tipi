# *Tipi* Templating Engine

Copyright 2012 [Dave Gurnell] and [Chris Ross].

*Tipi* is a tiny templating engine written in Scala. It lets you define templates to avoid needless repetition and redundancy in any type of text file. Its primary intended use is the static generation of HTML pages.

**Tipi is currently in early alpha. Everything may be subject to change.**

## Overview

Tipi's syntax is based on [Mustache]. However, its engine is a little more powerful.

Mustache requires you to invoke your templates from a programming language such as Javascript. All your template data has to be defined in code - you can't blend data and templates.

Tipi allows you to do everything you can do with Mustache, but it can also be used independently of the host programming language. You can define templates, define data, and invoke the templates with the data, all from within a single Tipi file.

Here's an example:

    {{# def cat name knownFor }}
      <li>{{ name }}, best known for {{ knownFor }}</li>
    {{/ def }]

    <p>Notable Internet felines:</p>

    <ul>
    {{ cat "Long Cat"     "being long" }}
    {{ cat "Keyboard Cat" "playing a fine tune" }}
    {{ cat "Nyan Cat"     "singing, being half Pop Tart" }}
    {{ cat "Nonono Cat"   "negativity" }}
    </ul>

This file uses a special tag, `def`, to define a template called `cat`. it then invokes `cat` four times to produce the bullet points in the list. The output is as follows:

    <p>Notable Internet felines:</p>

    <ul>
      <li>Long Cat, best known for being long</li>
      <li>Keyboard Cat, best known for playing a fine tune</li>
      <li>Nyan Cat, best known for singing, being half Pop Tart</li>
      <li>Nonono Cat, best known for negativity</li>
    </ul>

The `def` tag itself doesn't produce any output. However, when Tipi processes it, it stores the `cat` away for later use. This causes the `cat` tags later on to produce the correct templated output.

Programmers will recognise these semantics straight away. Tipi is actually a very simple programming language, supporting function definition and invocation (with static binding and lexical scoping semantics). The `def` tag is simply a predefined function that has a side-effect of registering a template for later use.

As a way of illustrating this, here is a Javascript fragment that is semantically equivalent to the above document:

    function cat(name, knownFor) {
      return "<li>" + name + ", best known for " + knownFor + "</li>";
    }

    function main() {
      return
        "<ul>" +
        cat("Long Cat",     "being long") +
        cat("Keyboard Cat", "playing a fine tune") +
        cat("Nyan Cat",     "singing, being half Pop Tart") +
        cat("Nonono Cat",   "negativity") +
        "</ul>";
    }

    document.write(main());

## Writing template files

### Tags and arguments

Tipi syntax involves three types of *tag*:

 - `{{# openingTags }}` denote the beginning of a block of content - they must be paired with a closing tag
 - `{{/ closingTags }}` denote the end of a block - they must be paired with an opening tag
 - `{{ singletonTags }}` appear on their own - they are equivalent to an opening tag immediately followed by a corresponding closing tag

Opening and singleton tags optionally take a list of arguments. Closing tags may not take arguments. Here is an example:

    {{# person "Dave" "http://untyped.com" }}
      {{# occupation }}Software Developer{{/ occupation }]
      {{# hobbies }}Music, running{{/ hobbies }}
    {{/ person }}

### Defining templates

By default, Tipi recognises only three built-in templates: `def`, `bind`, and `this`.

`def` is used to define other templates. You can define simple templates in argument style:

    {{ def food "Lasagne" }}

or more complex templates in block style:

    {{# def person name url }}
      {{ name }} has a web site at {{ url }}
    {{/ def }}

The two forms are semantically similar. Think of them as function definitions in a regular programming language:

    var food = function() {
      return "Lasagne"
    }

    var person = function(name, url) {
      return name + " has a web site at " + url;
    }

### Invoking templates

Once you have defined a template using `def`, you can *invoke* it by writing its name as a tag:

    {{ food }} // ==> "Lasagne"

    {{ person "Dave" "boxandarrow.com" }} // ==> "Dave has a web site at boxandarrow.com"

### Passing blocks using `this`

For more verbose invocations, you can pass a block of text as an argument using the `this` built-in:

    {{# def center}}
      <p style="text-align: center">
        {{ this }}
      </p>
    {{/ def }}

    {{# center }}
      Lots of text...
    {{/ center }}

Tags in the argument are expanded *before* it is passed to the template. This makes the semantics similar to regular programming languages. For example, the following templates and function calls are semantically similar:

    {{#x}}{{y}}{{/x}}

    x(y())

### Passing named blocks using `bind`

You can pass multiple named blocks as argument using the `bind` built-in:

    {{# def page }}
      <p class="sidebar">{{ sidebar }}</p>
      <p class="article">{{ article }}</p>
    {{/ def }}

    {{# page }}
      {{# bind sidebar }}
        Long sidebar definition goes here ...
      {{/ bind }}
      {{# bind article }}
        Long article definition goes here ...
      {{/ bind }}
    {{/ center }}

This is similar to named arguments in Scala function calls. Note, however, that the arguments are not declared in the opening tag of the template.

As with `this`-style arguments, `bind` blocks are expanded before they are passed to the template. For example, the following templates and function call pseudo-code are semantically similar:

    {{#x}}
      {{#bind a}}{{y}}{{/bind}}
      {{#bind b}}{{z}}{{/bind}}
    {{/x}}

    x(
      a = y(),
      z = b()
    )

While it is possible to mix normal, `this` and `bind` arguments, we recommend you stick to one kind of argument for each template you write. Otherwise things can become confusing.

## Running Tipi from Scala

First, create a `Tipi` object:

    import tipi.core.Tipi

    val tipi = new Tipi()

There are optional arguments to specify alternatives to the regular syntax:

    val tipi = new Tipi(
      simpleTagStart = "[:", // normally "{{"
      simpleTagEnd   = ":]", // normally "}}"
      blockStart     = "^",  // normally "#"
      blockEnd       = "v"   // normally "/"
    )

You can also specify the global environment used in the *expand* phase of compilation (see below), allowing you to pre-load templates written in Scala:

    val customEnv = // ...define your environment here...

    val tipi = new Tipi(
      globalEnv = customEnv
    )

Take a look at `Env.scala` and `Transform.scala` to see how to do this.

## How it works

### Parsing

Tipi parses these tags into a DOM tree, where each branch is a block and each leaf is a text node. For example, the document:

    {{# person "Dave" "http://untyped.com" }}
      {{# occupation }}Software Developer{{/ occupation }]
      {{# hobbies }}Music, running{{/ hobbies }}
    {{/ person }}

would be parsed as follows:

 - block "person" "Dave" "http://untyped.com"
    - block "occupation"
       - text "Software Developer"
    - block "hobbies"
       - text "Music, running"

### Evaluation

Once Tipi has parsed the document, it *expands* the DOM tree, invoking all the templates it can to produce the final document.

Expansion involves a pre-order walk of the tree within the context of an *environment* object. The environment stores any templates and template data that Tipi might need to process tags in the document:

 - Whenever Tipi encounters a *text* node, it echoes it straight to
   the output.

 - Whenever Tipi encounters a *block* node, it tries to find a template of
   the same name.

   If Tipi finds a matching template, it invokes it to expand the node,
   and then starts walking the resulting tree. If there isn't a matching
   template, Tipi leaves the block as-is and starts walking its children.

   In addition to expanding part of the tree, templates are also able to add
   new items to the environment. These new items are then available for Tipi
   to use when it is expanding later blocks in the tree.

### Rendering

After expansion, Tipi *renders* the final DOM tree by removing any remaining tags and returning the remaining text content.

[Dave Gurnell]: http://boxandarrow.com
[Chris Ross]: http://darkrock.co.uk
[Mustache]: http://mustache.github.com
