package org.edgegallery.user.auth.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFailureListener.class);

    @Autowired
    private MecUserDetailsService mecUserDetailsService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        if (event.getException().getClass().equals(UsernameNotFoundException.class)) {
            return;
        }
        String userId = event.getAuthentication().getName();
        mecUserDetailsService.addFailedCount(userId);
    }
}
