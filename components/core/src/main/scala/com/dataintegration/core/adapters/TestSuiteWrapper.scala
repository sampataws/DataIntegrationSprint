package com.dataintegration.core.adapters

trait TestSuiteWrapper {

  def preStep(args: Array[String]) : Unit
  def transformationStep(args: Array[String]) : Unit
  def postStep(args: Array[String]) : Unit

  def main(args: Array[String]): Unit = {
    preStep(args)
    transformationStep(args)
    postStep(args)
  }

}
