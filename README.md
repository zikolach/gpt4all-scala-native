Scala Native and Gpt4All example
================================

- [Gpt4All](https://github.com/nomic-ai/gpt4all)
- [Scala Native](https://scala-native.org)

To run it:

- install Gpt4All application from https://gpt4all.io/index.html
- load one of available models from the application or manually from the site above
- make `models` symlink a folder with downloaded model files (see Settings/Application/Download Path)
- change hardcoded paths (and if needed model name) in `Main.scala` and `build.sbt` files
- run `sbt run`
