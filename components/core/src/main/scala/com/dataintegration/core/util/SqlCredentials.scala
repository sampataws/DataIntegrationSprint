package com.dataintegration.core.util

case class SqlCredentials(
                           driver: String = System.getenv("db_driver"),
                           url: String = System.getenv("db_url"),
                           user: String = System.getenv("db_user"),
                           password: String = System.getenv("db_password"))
