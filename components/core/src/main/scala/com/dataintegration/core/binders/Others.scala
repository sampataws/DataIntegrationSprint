package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.log.audit.TableDefinition.LogScenarios

object Others {

  case class AssertScenario(name: String, assertionType: String)

  case class ScenarioConfig(
                             featureName: String,
                             description: String,
                             fileDependencies: List[FileStoreConfig],
                             assertions: List[AssertScenario]) {

    def getLoggingService: Seq[LogScenarios] = assertions.map { self =>
      LogScenarios(
        scenarioId = UUID.randomUUID().toString,
        scenarioName = self.name,
        scenarioDesc = description,
        assertion = self.assertionType)
    }
  }

}
