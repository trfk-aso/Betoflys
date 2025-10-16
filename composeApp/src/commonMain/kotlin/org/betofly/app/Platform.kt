package org.betofly.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform