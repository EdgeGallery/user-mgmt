package org.mec.auth.server.config.security;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.stereotype.Service;

@Service
public class AuthInMemoryAuthorizationCodeServices extends InMemoryAuthorizationCodeServices {
    @Autowired
    private HttpServletRequest request;

    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        String ssoSessionId = request.getSession(false).getId();
        request.getServletContext().setAttribute(code, ssoSessionId);
        super.store(code, authentication);
    }
}
