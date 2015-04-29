### *Macroid* — a Scala GUI DSL for Android [![Build Status](https://travis-ci.org/macroid/macroid.png?branch=master)](https://travis-ci.org/macroid/macroid) [![Codacy Badge](https://www.codacy.com/project/badge/fb3d939567d04686bfb23da3a22b9de9)](https://www.codacy.com/public/nickstanch/macroid) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Macroid-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1529)

[![Join the chat at https://gitter.im/macroid/macroid](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/macroid/macroid?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

*Macroid* is the most badass modular functional user interface creation language for Android, implemented with Scala macros.
[Some people say](http://www.urbandictionary.com/define.php?term=macroid) it’s also a dead noodle.

Striving to be focused on one thing (GUI), *Macroid* promotes composability and high-level abstractions.

Prerequisites: Scala `2.10.x` or `2.11.x`, Android `API 9+`.

Latest version: `2.0.0-M4` ([installation](http://macroid.github.io/Installation.html) | [changelog](http://macroid.github.io/Changelog.html)).

License: [MIT](http://opensource.org/licenses/MIT).

* [What does it look like](http://macroid.github.io/Tutorial.html)
* [How is it different from...](http://macroid.github.io/Differences.html)
* [Detailed guide](http://macroid.github.io/Guide.html)
* [Mailing list](https://groups.google.com/forum/#!forum/macroid)

For more info head to http://macroid.github.io!

#### Contributing

All contributions are welcome (and encouraged)!

##### Commit messages

*Macroid*’s commit message structure is [inspired by the Spray project](http://spray.io/project-info/contributing/#git-commit-messages). The message has the following format:
```
[=|+|!] [core|viewable|akka|docs|all]: <Actual message>.
```
* `=` means there are no API changes
* `+` means added funtionality
* `!` means breaking changes (source or binary)

Example:
```
! core: Receive UI actions in mapUi & co (fix #48)

mapUi, flatMapUi, ... now operate on UI actions, rather than simple thunks.
For example, the new type signature for mapUi is (A ⇒ Ui[B]) ⇒ Future[B].
```
Following this convention greatly simplifies writing the changelogs.

##### Documentation

Although this is not crucial, updating the docs under `macroid-docs` together with the code changes might save some time in the future, and thus is highly appreciated. It can be done in the same commit.
