package at.agsolutions.demo

import com.vaadin.flow.server.HandlerHelper
import com.vaadin.flow.shared.ApplicationConstants
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import javax.servlet.http.HttpServletRequest

object SecurityUtils {

    val user: UserDetails?
        get() = SecurityContextHolder.getContext().authentication?.principal as? UserDetails

    fun isFrameworkInternalRequest(request: HttpServletRequest): Boolean {
        val parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER)
        return parameterValue != null && HandlerHelper.RequestType.values().any { r -> r.identifier == parameterValue }
    }
}
