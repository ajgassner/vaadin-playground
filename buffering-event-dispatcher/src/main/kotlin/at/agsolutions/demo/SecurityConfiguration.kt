package at.agsolutions.demo

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.util.matcher.RequestMatcher

@EnableWebSecurity
@Configuration
class SecurityConfiguration(securityProperties: SecurityProperties, auth: AuthenticationManagerBuilder) {

    companion object {
        val PUBLIC_URLS = arrayOf(
                "/error",
                "/images/**",
                "/icons/**",
                "/manifest.webmanifest",
                "/offline.html",
                "/sw.js",
                "/frontend/**",
                "/frontend-es5/**",
                "/frontend-es6/**",
                "/VAADIN/**",
                "/vaadinServlet/PUSH*",
                "/vaadinServlet/*"
        )
    }

    init {
        auth.inMemoryAuthentication().withUser(securityProperties.user.name).password(securityProperties.user.password).roles("USER")
    }

    @Configuration
    @Order(1)
    class ApiWebSecurityConfigurerAdapter : WebSecurityConfigurerAdapter() {

        override fun configure(http: HttpSecurity) {
            http
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .requestMatchers()
                    .antMatchers("/api/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/api/**")
                    .permitAll()
                    .and()
                    .csrf()
                    .disable()
        }
    }

    @Configuration
    @Order(2)
    class VaadinWebSecurityConfigurerAdapter : WebSecurityConfigurerAdapter() {

        override fun configure(http: HttpSecurity) {
            http
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers(*PUBLIC_URLS)
                    .permitAll()
                    .requestMatchers(RequestMatcher { SecurityUtils.isFrameworkInternalRequest(it) })
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated()
                    .and()
                    .sessionManagement()
                    .sessionFixation()
                    .changeSessionId()
                    .and()
                    .formLogin()
                    .defaultSuccessUrl("/")
                    .and()
                    .logout()
                    .addLogoutHandler(VaadinSessionClosingLogoutHandler())
                    .and()

        }
    }
}
