package com.dataintegration.core.services.job

object DivideJobsToClusters {

  @scala.annotation.tailrec
  def equallyDistribute[C, J](
                               primaryList: List[J],
                               distributionList: List[C],
                               accumulator: Map[J, C] = Map.empty[J, C]): Map[J, C] = {

    if (distributionList.isEmpty) throw new RuntimeException("Not enough clusters running to create spark job!")

    if (primaryList.isEmpty) accumulator
    else
      equallyDistribute(
        primaryList = primaryList.tail,
        distributionList = distributionList.tail :+ distributionList.head,
        accumulator = accumulator ++ Map(primaryList.head -> distributionList.head))
  }

}
