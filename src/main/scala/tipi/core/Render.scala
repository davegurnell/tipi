package tipi.core

object Render {

  def apply(in: (Env, Doc)): String = {
    apply(in._2)
  }

  def apply(doc: Doc): String = {
    doc match {
      case Block(_, _, body) => Render(body)
      case Range(children)   => children.map(Render.apply _).foldLeft("")(_+_)
      case Text(value)       => value
    }
  }

}