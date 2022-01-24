package com.dataintegration.core.util

case class SqlCredentials(
                           driver: String = "com.mysql.cj.jdbc.Driver",
                           url: String = "jdbc:mysql://192.168.8.3:3306/dts?useSSL=false",
                           user: String = "root",
                           password: String = "admin")
