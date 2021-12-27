package com.dataintegration.core.binders

case class IntegrationConf(
                            clusterList: List[Cluster],
                            featureList: List[Feature],
                            fileStoreList: Map[String, List[FileStore]],
                            properties: Properties
                          )
