package com.las.workout.exception.advice

import com.las.workout.exception.BaseHttpException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.*

@RestControllerAdvice
class GlobalHttpExceptionsAdvice {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BaseHttpException::class)
    fun handleBusinessExceptions(@AuthenticationPrincipal user: User?, ex: BaseHttpException): Mono<ResponseEntity<Any>> {
        return ReactiveRequestContextHolder.request.map { request ->
            val ip = if (request.isPresent) request.get().ip() else null
            val ua = if (request.isPresent) request.get().ua() else null
            val path = if (request.isPresent) request.get().path else null
            log.warn("BEX_ERR({}, {})->[{}]->[{}] - [{}]. UA={}", user?.username, ip, path, ex::class.simpleName, ex.message, ua)
            ResponseEntity(ex.data(), ex.status)
        }

    }

    @ExceptionHandler(Throwable::class)
    fun handleExceptions(@AuthenticationPrincipal user: User?, ex: Throwable): Mono<ResponseEntity<Any>> {
        return ReactiveRequestContextHolder.request.map { request ->
            val ip = if (request.isPresent) request.get().ip() else null
            val ua = if (request.isPresent) request.get().ua() else null
            val path = if (request.isPresent) request.get().path else null
            if (ex is AccessDeniedException)
                log.warn("API_ERR({}, {})->[{}]->[{}] - [{}]. UA={}", user?.username, ip, path, ex::class.simpleName, ex, ua)
            else
                log.error("API_ERR({}, {})->[{}]->[{}] - [{}]. UA={}", user?.username, ip, path, ex::class.simpleName, ex, ua)
            throw ex
        }
    }

}

object ReactiveRequestContextHolder {
    val CONTEXT_KEY = ServerHttpRequest::class.java

    val request: Mono<Optional<ServerHttpRequest>>
        get() = Mono.deferContextual { ctx ->
            Mono.just(ctx.getOrEmpty(CONTEXT_KEY))
        }
}

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveRequestContextFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .contextWrite { ctx: Context ->
                ctx.put(ReactiveRequestContextHolder.CONTEXT_KEY, exchange.request)
            }
    }
}

/**
 * Get requester ip
 */
fun ServerHttpRequest.ip(): String? =
    when {
        this.headers.containsKey("cf-connecting-ip") -> {
            this.headers["cf-connecting-ip"]?.firstOrNull()
        }
        this.headers.containsKey("x-forwarded-for") -> {
            this.headers["x-forwarded-for"]?.firstOrNull()
        }
        else -> null
    }

/**
 * Get request user-agent header
 */
fun ServerHttpRequest.ua(): String? = this.headers["user-agent"]?.firstOrNull()