package com.dataintegration.core.binders

case class Compute(
                    clusterName: String,
                    bucketName: String,
                    project: String,
                    region: String,
                    subnetUri: String,
                    endpoint: String,
                    imageVersion: String,
                    masterMachineTypeUri: String,
                    masterNumInstance: Int,
                    masterBootDiskSizeGB: Int,
                    workerMachineTypeUri: String,
                    workerNumInstance: Int,
                    workerBootDiskSizeGB: Int,
                    maxRetries: Int,
                    idleDeletionDurationSec: Int,
                    weightage: Int
                  ) extends ServiceConfig
