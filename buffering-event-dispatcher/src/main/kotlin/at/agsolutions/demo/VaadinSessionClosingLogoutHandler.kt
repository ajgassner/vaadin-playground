package at.agsolutions.demo

import com.vaadin.flow.server.VaadinSession
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class VaadinSessionClosingLogoutHandler : LogoutHandler {

    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication?) {
        logger().debug("closing all vaadin sessions")
        VaadinSession.getAllSessions(request.session).forEach { vaadinSession ->
            logger().debug("closing vaadin session $vaadinSession")
            vaadinSession.lock()
            try {
                vaadinSession.close()
            } finally {
                vaadinSession.unlock()
            }
        }
    }
}
