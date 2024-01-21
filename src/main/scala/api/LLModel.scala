//noinspection ScalaUnusedSymbol
package api

import api.library.llmodel.LLModelPromptContext
import scala.collection.mutable
import scala.scalanative.unsafe.*

object LLModel {

  /**
   * Create model object
   *
   * @param modelPath   path to model file
   * @param libraryPath path to model library implementations
   */
  def apply(modelPath: String, libraryPath: String): LLModel = {
    Zone { implicit z =>
      val error: Ptr[CString] = stackalloc[CString]()
      val modelName = java.io.File(modelPath).getName
      val modelPathC: CString = toCString(modelPath)
      val buildVariantC: CString = toCString("auto")

      library.llmodel.llmodel_set_implementation_search_path(toCString(libraryPath))
      val modelPtr: Ptr[Byte] = library.llmodel.llmodel_model_create2(modelPathC, buildVariantC, error)

      if (modelPtr == null) {
        val errorMsg: String = fromCString(!error)
        // Handle error, e.g., throw an exception or print the error message
        throw new RuntimeException(s"Error creating LLModel: $errorMsg")
      }
      library.llmodel.llmodel_loadModel(modelPtr, modelPathC, n_ctx = 2048)

      if (!library.llmodel.llmodel_isModelLoaded(modelPtr)) {
        throw new RuntimeException(s"The model $modelPathC could not be loaded")
      }
      LLModel(modelPtr, modelName)
    }
  }

  private object GenerationConfig {
    class Builder {
      private val configToBuild = stackalloc[library.llmodel.LLModelPromptContext]()
      configToBuild._1 = stackalloc[CFloat](0) // logits
      configToBuild._2 = 0 // logits_size
      configToBuild._3 = stackalloc[CInt](0) // tokens
      configToBuild._4 = 0 // tokens_size
      configToBuild._5 = 0 // n_past
      configToBuild._6 = 1024 // n_ctx
      configToBuild._7 = 4096 // 128 // n_predict
      configToBuild._8 = 40 // top_k
      configToBuild._9 = 0.95 // top_p
      configToBuild._10 = 0.28 // temp
      configToBuild._11 = 8 // n_batch
      configToBuild._12 = 1.1 // repeat_penalty
      configToBuild._13 = 10 // repeat_last_n
      configToBuild._14 = 0.55 // context_erase

      def withNPast(n_past: Int): Builder = {
        configToBuild._5 = n_past
        this
      }

      def withNCtx(n_ctx: Int): Builder = {
        configToBuild._6 = n_ctx
        this
      }

      def withNPredict(n_predict: Int): Builder = {
        configToBuild._7 = n_predict
        this
      }

      def withTopK(top_k: Int): Builder = {
        configToBuild._8 = top_k
        this
      }

      def withTopP(top_p: Float): Builder = {
        configToBuild._9 = top_p
        this
      }

      def withTemp(temp: Float): Builder = {
        configToBuild._10 = temp
        this
      }

      def withNBatch(n_batch: Int): Builder = {
        configToBuild._11 = n_batch
        this
      }

      def withRepeatPenalty(repeat_penalty: Float): Builder = {
        configToBuild._12 = repeat_penalty
        this
      }

      def withRepeatLastN(repeat_last_n: Int): Builder = {
        configToBuild._13 = repeat_last_n
        this
      }

      def withContextErase(context_erase: Float): Builder = {
        configToBuild._14 = context_erase
        this
      }

      def build: Ptr[LLModelPromptContext] = configToBuild
    }
  }

  private val response: StringBuilder = StringBuilder()

  private def onResponse(token: CInt, resp: CString): CBool = {
    if (token == -1) throw new RuntimeException("Prompt too long")
    val out = fromCString(resp)
    response.append(out)
    true
  }

  private def onResponseStream(token: CInt, resp: CString): CBool = {
    if (token == -1) throw new RuntimeException("Prompt too long")
    val out = fromCString(resp)
    print(out)
    response.append(out)
    true
  }
  private def onRecalculate(isRecalculate: CBool): CBool = {
    isRecalculate
  }

  private def onPrompt(token: CInt): CBool = {
    true
  }

}

case class LLModel private (model: Ptr[Byte], modelName: String) extends AutoCloseable {
  import LLModel._

  def setThreadCount(threadCount: Int): Unit = {
    library.llmodel.llmodel_setThreadCount(model, threadCount)
  }

  def generate(prompt: String, streamToStdOut: Boolean = false): String = {
    Zone { implicit z =>
      library.llmodel.llmodel_prompt(
        model,
        toCString(prompt),
        onPrompt,
        if (streamToStdOut) onResponseStream else onResponse,
        onRecalculate,
        new GenerationConfig.Builder().build
      )
      val resp = response.toString()
      response.clear()
      resp
    }
  }

  override def close(): Unit =
    library.llmodel.llmodel_model_destroy(model)
}
