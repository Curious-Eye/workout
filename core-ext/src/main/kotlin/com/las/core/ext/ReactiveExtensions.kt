package com.las.core.ext

import reactor.core.publisher.Mono

fun <T> Mono<T>.errorIfEmpty(exception: Exception): Mono<T> =
    this.switchIfEmpty(Mono.error(exception))

fun <T, O> Mono<T>.zipWhenToPair(other: Mono<O>): Mono<Pair<T, O>> =
    this.zipWith(other) { t, o -> Pair(t, o) }
