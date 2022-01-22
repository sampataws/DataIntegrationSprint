package com.dataintegration.core.binders

object Others {

  case class AssertScenario(name: String, assertionType: String)

  case class ScenarioConfig(
                             featureName: String,
                             description: String,
                             fileDependencies: List[FileStoreConfig],
                             assertions: List[AssertScenario])

}
