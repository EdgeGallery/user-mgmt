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

package org.edgegallery.user.auth.config;

public class DescriptionConfig {

    private DescriptionConfig() {
    }

    public static final String REGISTER_MSG = "The API can receive register user request. If register success, it will "
        + "return status 201. If username or telephone has existed, or verification code is error, it will reutrn "
        + "status 403. If database connection has exception, it will return status 500. If register failed, it "
        + "will return status 400.";

    public static final String RETRIEVE_PASSWORD_MSG = "The API can receive the retrieve password request. If retrieve "
        + "success, it will return status 200. If telephone do not exist or the verification code is error, it "
        + "will return status 403. If database connection has exception, it will return status 500. If retrieve "
        + "failed, it will return status 400.";

    public static final String VERIFICATION_MSG = "The API can receive the send verification code request. If send "
        + "verification code success, it will return status 200. If send verification code error, it will return "
        + "status 417.";

    public static final String UNIQUENESS_MSG = "The API can receive the unique verify request for telephone or "
        + "username. If the request param is unique, it will return status 200, otherwise it will return status "
        + "400.";

    public static final String LOGOUT_MSG = "The API can receive the logout request, If logout successful, it will"
        + " return status 200, otherwise it will return status 500.";
}

