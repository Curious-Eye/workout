package com.las.workout.jwt.service

import com.las.workout.system.data.entity.MetaInfoEntity
import com.las.workout.system.data.entity.MetaObjectType
import com.las.workout.system.data.repository.MetaInfoRepository
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwkSource : JWKSource<SecurityContext> {

    private lateinit var jwkSet: JWKSet

    @Autowired
    private lateinit var metaInfoRepository: MetaInfoRepository

    @Scheduled(fixedRate = Long.MAX_VALUE)
    fun initKeys() {
        jwkSet = metaInfoRepository.findById(MetaObjectType.JWKSET)
            .switchIfEmpty(Mono.defer {
                metaInfoRepository.save(
                    MetaInfoEntity(
                        id = MetaObjectType.JWKSET,
                        data = mapOf("keys" to JwkUtils.generateJWKSet().keys.map { it.toString() })
                    )
                )
            })
            .map { meta -> JWKSet((meta.data.values.first()!! as List<String>).map { RSAKey.parse(it) }) }
            .block()!!
    }

    override fun get(jwkSelector: JWKSelector, context: SecurityContext?): MutableList<JWK> {
        return jwkSelector.select(jwkSet)
    }

}