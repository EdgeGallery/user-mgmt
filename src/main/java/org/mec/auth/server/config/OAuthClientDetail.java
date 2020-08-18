package org.mec.auth.server.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthClientDetail {
    private String clientId;

    private String clientSecret;

    private String clientUrl;
}
