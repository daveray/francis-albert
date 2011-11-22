# francis-albert

A very simple functional ear trainer built to get familiar with [Overtone](http://overtone.github.com/). Inspired-by/copied-from the much more advanced [FET](http://www.miles.be/).

<img src="https://github.com/downloads/daveray/francis-albert/francis-albert.png" alt="Screenshot">

TODO:

* Learn the ways of overtone
* Other instruments
* Cadence tempo
* More cadences
* Configurable note selection. Full-chromatic is a little tough for beginners.

## Usage

First you have to install SuperCollider as described on the [Overtone](http://overtone.github.com/) site. Then:

    $ lein deps
    $ lein run

You'll hear a three-chord cadence and then a note. Guess the interval of the note using the buttons provided. Then you'll hear another cadence and another note. Forever.

## License

Copyright (C) 2011 Dave Ray

Distributed under the Eclipse Public License, the same as Clojure.
