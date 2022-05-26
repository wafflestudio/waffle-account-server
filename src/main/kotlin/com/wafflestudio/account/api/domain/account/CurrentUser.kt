package com.wafflestudio.account.api.domain.account

import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class CurrentUser(
    var id: Long = 0,
    val token: String,
) : Authentication {
    override fun getName() = id.toString()
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(""))
    override fun getCredentials() {}
    override fun getDetails() {}
    override fun getPrincipal() {}
    override fun isAuthenticated() = true
    override fun setAuthenticated(isAuthenticated: Boolean) {}
}
