package com.las.core.ext

fun <T> List<T>?.thisOrEmpty(): List<T> = this ?: listOf()