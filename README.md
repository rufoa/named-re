named-re
========

named-re adds support for **named capturing groups** to regexes in clojure, even when the underlying JRE does not support them directly (e.g. Java 6).

It does this by modifying the `clojure.core/re-` functions, the `clojure.string/replace` function, and the `#"regex"` reader macro.

[![Build Status](https://travis-ci.org/rufoa/named-re.png?branch=master)](https://travis-ci.org/rufoa/named-re)

## Installation ##

named-re is in Clojars. To use it in a Leiningen project, add it to your project.clj dependencies:

    [named-re "1.0.0"]

then require `named-re.core` in your code:

    (ns my.example
       (:require named-re.core))

## Syntax ##

named-re uses the same regex syntax as Java 7.

Named groups are denoted by `(?<groupname>...)`. Named backreferences use `\k<groupname>`. Named groups in replacement strings use `${groupname}`.

## Usage ##

named-re augments the normal `re-` functions in `clojure.core`.

If a pattern contains any named groups, then the match result will be a map rather than a vector. The keys in the map are the names of the groups. The `:0` entry is the entire match (like the 0th item in a match vector).

For example:

    (re-find
       #"(?<area>\d{3})-(?<prefix>\d{3})-(?<line>\d{4})"
       "555-123-4567")

gives

    {  :0      "555-123-4567",
       :line   "4567",
       :prefix "123",
       :area   "555" } 

Similarly, `re-seq` will return a list of maps:

    (re-seq
       #"(?<title>Mr|Mrs|Miss) (?<forename>\w+) (?<surname>\w+)"
       "Present were Mrs Jane Smith, Mr Joe Bloggs, and Miss Anne Example")

gives

    (  {:surname "Smith",   :title "Mrs",  :forename "Jane", :0 "Mrs Jane Smith"}
       {:surname "Bloggs",  :title "Mr",   :forename "Joe",  :0 "Mr Joe Bloggs"}
       {:surname "Example", :title "Miss", :forename "Anne", :0 "Miss Anne Example"} )

Replacements work as expected:

    (clojure.string/replace
       "by Alfred V. Aho, Monica S. Lam, Ravi Sethi, and Jeffrey D. Ullman"
       #"(?<first>[A-Z]\w+) (\w\. )*(?<last>[A-Z])\w+"
       "${first} ${last}.")

gives

    "by Alfred A., Monica L., Ravi S., and Jeffrey U."

If a pattern does not contain any named groups then these functions behave the normal way.

## Caveats ##

named-re does not use `java.util.regex.Pattern` and `java.util.regex.Matcher` objects. This means:

 - `(instance? java.util.regex.Pattern #"(?<foo>..)")` will return `false`
 - `(instance? java.util.regex.Matcher (re-matcher #"(?<foo>..)" "abcdef"))` will return `false`
 - Java interop cannot be used to invoke methods or access fields directly, e.g. `(.reset some-matcher)` will throw an exception

## License

Copyright © 2013 @rufoa

Distributed under the Eclipse Public License, the same as Clojure.