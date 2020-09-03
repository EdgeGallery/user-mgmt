package org.edgegallery.user.auth.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthClientDetail {
    private String clientId;

    private String clientSecret;

    private String clientUrl;
}
