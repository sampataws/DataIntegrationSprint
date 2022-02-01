package backup

object LayerDownTest extends App {

  case class Hello() {
    println("YEAHH")

    def print = println("hello")
  }

  Hello().print
  //  object ComputeContract extends ComputeContract[String] {
  //    override def createClient(endpoint: String): Task[String] = Task("ComputeContract Started").debug(debugStr)
  //    override def destroyClient(client: String): URIO[Any, Unit] = Task("ComputeContract Ended").debug(debugStr).unit.orDie
  //    override def createService(client: String, data: ComputeConfig): ComputeConfig = {
  //      logger.info(s"Cluster create started $client")
  //      data.copy(status = Status.Running)
  //    }
  //
  //    override def destroyService(client: String, data: ComputeConfig): ComputeConfig = {
  //      logger.info(s"Cluster create done $client")
  //      data.copy(status = Status.Success)
  //    }
  //    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
  //      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer
  //
  //    //override val manager: ComputeManager[String] = new ComputeManager[String]
  //    //override val api: ComputeApi[String] = new ComputeApi[String]
  //    override val live: ULayer[ComputeContract.type] = ZLayer.succeed(this)
  //  }
  //
  //  val dep =
  //    (configLayer ++ ComputeContract.liveClient("") ++ ZLayer.succeed(ComputeContract.api) ++ ComputeContract.live) >>> ComputeContract.manager.live
  //
  //
  //
  //  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
  //    ComputeContract.manager.Apis.startService.provideLayer(dep)
}
