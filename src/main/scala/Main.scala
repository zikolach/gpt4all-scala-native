import api.*

import scala.util.Using

object Main {

  def main(args: Array[String]): Unit = {
    val prompt =
      """### Instruction:
        |The prompt below is a question to answer, a task to complete, or a conversation to respond to; decide which and write an appropriate response.
        |### Prompt:
        |What is the meaning of life?
        |### Response:
        |""".stripMargin
    val libraryPath = "/Applications/gpt4all/lib"
    val modelPath = "models/orca-mini-3b-gguf2-q4_0.gguf"
    val result = Using(LLModel(modelPath, libraryPath)) { model =>
      println(s"Model ${model.modelName} loaded")
      model.setThreadCount(8)
      println(s"Set thread count")
      model.generate(prompt, streamToStdOut = true)
    }
    result.foreach(x => println("\nRESULT: " + x))
  }
}
