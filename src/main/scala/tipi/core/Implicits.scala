package tipi.core

trait TransformImplicits {
  implicit def stringToId(name: String): Id = Id(name)
  implicit def stringAndTransformToIdAndTransform(pair: (String, Transform)): (Id, Transform) = Id(pair._1) -> pair._2

  implicit def docToTransform(doc: Doc): Transform = Transform.Constant(doc)
  implicit def idAndDocToIdAndTransform(pair: (Id, Doc)): (Id, Transform) = pair._1 -> Transform.Constant(pair._2)
  implicit def stringAndDocToIdAndTransform(pair: (String, Doc)): (Id, Transform) = Id(pair._1) -> Transform.Constant(pair._2)

  implicit def docFunctionToTransform(func: Function1[Doc, Doc]): Transform = Transform.Simple(func)
  implicit def idAndDocFunctionToIdAndTransform(pair: (Id, Function1[Doc, Doc])): (Id, Transform) = pair._1 -> Transform.Simple(pair._2)
  implicit def stringAndDocFunctionToIdAndTransform(pair: (String, Function1[Doc, Doc])): (Id, Transform) = Id(pair._1) -> Transform.Simple(pair._2)

  implicit def fullFunctionToTransform(func: Function1[(Env, Doc), (Env, Doc)]): Transform = Transform.Full(func)
  implicit def idAndFullFunctionToIdAndTransform(pair: (Id, Function1[(Env, Doc), (Env, Doc)])): (Id, Transform) = pair._1 -> Transform.Full(pair._2)
  implicit def stringAndFullFunctionToIdAndTransform(pair: (String, Function1[(Env, Doc), (Env, Doc)])): (Id, Transform) = Id(pair._1) -> Transform.Full(pair._2)
}

trait Implicits extends TransformImplicits

object Implicits extends Implicits