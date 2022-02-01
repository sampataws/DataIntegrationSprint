package com.dataintegration.core.adapters

trait TestSuiteWrapper {

  def preStep(args: Array[String]) : Array[String]
  def transformationStep(args: Array[String]) : Unit
  def postStep(args: Array[String]) : Unit

  def main(args: Array[String]): Unit = {
    val updatedArgs = preStep(args)
    transformationStep(updatedArgs)
    postStep(updatedArgs)
  }

}
