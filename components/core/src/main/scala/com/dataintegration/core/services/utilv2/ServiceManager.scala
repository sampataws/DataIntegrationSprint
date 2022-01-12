package com.dataintegration.core.services.utilv2

import com.dataintegration.core.services.util.{ServiceConfig, ServiceResult}
import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils, Status}
import zio.{Task, ZIO}

trait ServiceManager[T <: ServiceConfig, S] extends ApplicationLogger {

  trait ServiceBackend {
    def startService: ZIO[Any, Throwable, List[ServiceResult[T,S]]]

    def stopService[R](upServices: ServiceResult[T,S]): ZIO[Any, Throwable, List[R]]

    def getServiceStatus[R](upServices: ServiceResult[T,S]): ZIO[Any, Throwable, List[R]]
  }

  object Apis {
    def startService: ZIO[ServiceBackend, Throwable, List[ServiceResult[T,S]]] =
      ZIO.serviceWithZIO[ServiceBackend](_.startService)

    def stopService[R](upServices: ServiceResult[T,S]): ZIO[ServiceBackend, Throwable, List[R]] =
      ZIO.serviceWithZIO[ServiceBackend](_.stopService(upServices))

    def getServiceStatus[R](upServices: ServiceResult[T,S]): ZIO[ServiceBackend, Throwable, List[R]] =
      ZIO.serviceWithZIO[ServiceBackend](_.getServiceStatus(upServices))

  }

  def serviceBuilder(task: T => Task[T],
                     listOfResources: List[T],
                     failureType: FailureType,
                     parallelism: Int): ZIO[Any, Throwable, List[T]] = for {
    result <- ZIO.foreachPar(listOfResources)(task).withParallelism(parallelism)
    _ = ApplicationUtils.prettyPrintCaseClass(result, logger)
    _ <- validateServiceTask(result, failureType)
  } yield result


  sealed trait FailureType

  /**
   * Should prevent app crash if at least one service is up and running
   * like 5 cluster at least one should be up
   */
  case object FailSafe extends FailureType

  /**
   * Should fail app even if one task fails
   * Like uploading jars/files
   */
  case object FailFast extends FailureType

  /**
   * Never fails
   */
  case object FailSecure extends FailureType

  def validateServiceTask(service: List[ServiceConfig], failType: FailureType): Task[Boolean] = Task {
    val markStatusBoolean = (service: ServiceConfig) => service.getStatus match {
      case Status.Pending => true
      case Status.Running => true
      case Status.Success => true
      case Status.Failed => false
    }

    val markFailureType = (serviceStatusBoolean: List[Boolean]) => failType match {
      case FailSafe => serviceStatusBoolean.reduce(_ || _)
      case FailFast => serviceStatusBoolean.reduce(_ && _)
      case FailSecure => true
    }

    val accumulatedStatus: Boolean = markFailureType(service.map(markStatusBoolean))
    val failureMessage = service.filter(_.getStatus == Status.Failed).map(_.getLoggingInfo).mkString(", ") + ": ended with failed status"

    if (accumulatedStatus) true else throw new RuntimeException(failureMessage)

  }

}
