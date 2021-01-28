package io.techcode.streamy

import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import pureconfig._
import pureconfig.error.FailureReason

package object config {

  // Success behaviour reader
  implicit val successBehaviourReader: ConfigReader[SuccessBehaviour] = ConfigReader.fromString[SuccessBehaviour] {
    case "remove" => Right(SuccessBehaviour.Remove)
    case "skip" => Right(SuccessBehaviour.Skip)
    case _ => Left(new FailureReason {
      override def description: String = "Success behaviour must be either 'remove' or 'skip'"
    })
  }

  // Success behaviour reader
  implicit val errorBehaviourReader: ConfigReader[ErrorBehaviour] = ConfigReader.fromString[ErrorBehaviour] {
    case "discard" => Right(ErrorBehaviour.Discard)
    case "discard-and-report" => Right(ErrorBehaviour.DiscardAndReport)
    case "skip" => Right(ErrorBehaviour.Skip)
    case _ => Left(new FailureReason {
      override def description: String = "Error behaviour must be either 'discard' or 'discard-and-report' or 'skip'"
    })
  }

}
