/*
 *  Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.user.auth;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.edgegallery.user.auth", exclude = {SecurityAutoConfiguration.class})
@MapperScan(basePackages = {"org.edgegallery.user.auth.db.mapper"})
@EnableScheduling
@EnableServiceComb
public class MainServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainServer.class);

    /**
     * Main.
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {

        // do not check host name
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    LOGGER.info("checks client trusted");
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    LOGGER.info("checks server trusted");
                }
            }
        };
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        SpringApplication.run(MainServer.class, args);
    }
}
