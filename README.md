# francis-albert

A very simple functional ear trainer built to get familiar with [Overtone](http://overtone.github.com/). Inspired-by/copied-from the much more advanced [FET](http://www.miles.be/).

<img src="https://github.com/downloads/daveray/francis-albert/francis-albert.png" alt="Screenshot">

There's also a chord identification mode:

<img src="https://github.com/downloads/daveray/francis-albert/francis-albert-chords.png" alt="Screenshot">

TODO:

* Learn the ways of overtone
* Other instruments
* Cadence tempo
* More cadences
* Configurable note selection. Full-chromatic is a little tough for beginners.

## Usage
_At the moment this depends on the latest and greatest (as of 3/4/2011) version of Seesaw. There'll be a new release of that shortly._

First you have to install SuperCollider as described on the [Overtone](http://overtone.github.com/) site. Then:

    $ lein deps
    $ lein run

In the "Functional Intervals" tab you'll hear a three-chord cadence and then a note. Guess the interval of the note in the key established by the cadence using the buttons provided. Then you'll hear another cadence and another note. Forever.

In the "Chord Qualities" tab, Frank plays a single chord for you and you have to guess the quality of the chord, major, minor, etc. Make a guess, then you'll hear another chord, so on and so forth, forever. Fun!

## License

Copyright (C) 2012 Dave Ray

Distributed under the Eclipse Public License, the same as Clojure.
