/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.mec.auth.server.service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mec.auth.server.config.SmsConfig;
import org.mec.auth.server.utils.HttpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HwCloudVerification {

    private static final String WSSE_HEADER_FORMAT = "UsernameToken Username=\"%s\",PasswordDigest=\"%s\","
        + "Nonce=\"%s\",Created=\"%s\"";
    private static final String AUTH_HEADER_VALUE = "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"";

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private HttpsUtil httpsUtil;

    /**
     * telephone send verification code.
     *
     * @param telephone telephone
     * @param verificationCode verification code
     * @return
     */
    public Boolean sendVerificationCode(String telephone, String verificationCode) {
        String receiver = "+86" + telephone;
        String templateParas = "[\"" + verificationCode + "\"]";
        String wsseHeader = buildWsseHeader(smsConfig.getAppKey(), smsConfig.getAppSecret());
        Map<String, String> header = new HashMap<>();
        header.put(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        header.put(HttpHeaders.AUTHORIZATION, AUTH_HEADER_VALUE);
        header.put("X-WSSE", wsseHeader);
        String body = buildRequestBody(smsConfig.getSender(), receiver, smsConfig.getTemplateId(), templateParas,
            smsConfig.getStatusCallBack(), smsConfig.getSignature());
        return httpsUtil.httpsPost(smsConfig.getUrl(), header, body);
    }

    static String buildRequestBody(String sender, String receiver, String templateId, String templateParas,
        String statusCallbackUrl, String signature) {
        List<NameValuePair> keyValues = new ArrayList<NameValuePair>();
        keyValues.add(new BasicNameValuePair("from", sender));
        keyValues.add(new BasicNameValuePair("to", receiver));
        keyValues.add(new BasicNameValuePair("templateId", templateId));
        if (null != templateParas && !templateParas.isEmpty()) {
            keyValues.add(new BasicNameValuePair("templateParas", templateParas));
        }
        if (null != statusCallbackUrl && !statusCallbackUrl.isEmpty()) {
            keyValues.add(new BasicNameValuePair("statusCallback", statusCallbackUrl));
        }
        if (null != signature && !signature.isEmpty()) {
            keyValues.add(new BasicNameValuePair("signature", signature));
        }
        return URLEncodedUtils.format(keyValues, StandardCharsets.UTF_8);
    }

    /**
     * build X-WSSE.
     *
     * @param appKey appKey
     * @param appSecret appSecret
     * @return
     **/
    static String buildWsseHeader(String appKey, String appSecret) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String time = sdf.format(new Date()); //Created
        String nonce = UUID.randomUUID().toString().replace("-", ""); //Nonce
        byte[] passwordDigest = DigestUtils.sha256(nonce + time + appSecret);
        String hexDigest = Hex.encodeHexString(passwordDigest);
        String passwordDigestBase64Str = Base64.getEncoder()
                .encodeToString(hexDigest.getBytes(StandardCharsets.UTF_8));
        return String.format(WSSE_HEADER_FORMAT, appKey, passwordDigestBase64Str, nonce, time);
    }

}
