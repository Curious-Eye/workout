package com.las.core.ext

import reactor.core.publisher.Mono
import java.util.concurrent.Callable
import java.util.function.Function
import java.util.function.Predicate

fun <T> Mono<T>.errorIfEmpty(exception: Exception): Mono<T> =
    this.switchIfEmpty(Mono.error(exception))

fun <T, O> Mono<T>.zipWhenToPair(other: Function<T, Mono<out O>>): Mono<Pair<T, O>> =
    this.zipWhen(other) { t, o -> Pair(t, o) }

fun <T, O> Mono<T>.zipWithToPair(other: Mono<O>): Mono<Pair<T, O>> =
    this.zipWith(other) { t, o -> Pair(t, o) }

fun <T> Mono<T>.errorIf(err: Exception, predicate: Predicate<T>): Mono<T> =
    this.flatMap {
        if (predicate.test(it)) Mono.error(err)
        else Mono.just(it)
    }

fun <T> Mono<T>.flatNext(c: Callable<Mono<*>>): Mono<T> =
    this.flatMap {
        c.call()
            .thenReturn(it)
    }