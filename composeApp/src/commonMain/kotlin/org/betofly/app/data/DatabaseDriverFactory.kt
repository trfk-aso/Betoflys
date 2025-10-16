package org.betofly.app.data

import app.cash.sqldelight.db.SqlDriver


interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}