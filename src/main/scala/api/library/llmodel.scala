//noinspection ScalaUnusedSymbol,ScalaWeakerAccess
package api.library

import scala.scalanative.unsafe.*

@link("llmodel")
@extern
object llmodel {
  def llmodel_set_implementation_search_path(path: CString): Unit = extern
  def llmodel_model_create2(
      model_path: CString,
      build_variant: CString,
      error: Ptr[CString]
  ): Ptr[Byte] = extern
  def llmodel_model_destroy(model: Ptr[Byte]): Unit = extern
  def llmodel_loadModel(model: Ptr[Byte], model_path: CString, n_ctx: CInt): CBool = extern
  def llmodel_isModelLoaded(model: Ptr[Byte]): CBool = extern

  def llmodel_setThreadCount(model: Ptr[Byte], n_threads: CInt): Unit = extern
  def llmodel_threadCount(model: Ptr[Byte]): CInt = extern

  type LLModelPromptContext = CStruct14[
    Ptr[CFloat], // logits
    CSSize, // logits_size
    Ptr[CInt], // tokens
    CSSize, // tokens_size
    CInt, // n_past
    CInt, // n_ctx
    CInt, // n_predict
    CInt, // top_k
    CFloat, // top_p
    CFloat, // temp
    CInt, // n_batch
    CFloat, // repeat_penalty
    CInt, // repeat_last_n
    CFloat // context_erase
  ]
  type LLModelPromptCallback = CFuncPtr1[CInt, CBool]
  type LLModelResponseCallback = CFuncPtr2[CInt, CString, CBool]
  type LLModelRecalculateCallback = CFuncPtr1[CBool, CBool]

  def llmodel_prompt(
      model: Ptr[Byte],
      prompt: CString,
      prompt_callback: LLModelPromptCallback,
      response_callback: LLModelResponseCallback,
      recalculate_callback: LLModelRecalculateCallback,
      ctx: Ptr[LLModelPromptContext]
  ): Unit = extern
}
