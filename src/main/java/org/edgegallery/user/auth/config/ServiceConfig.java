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

public class ServiceConfig {

    private ServiceConfig() {
    }

    public static final String PATTERN_VERIFICATION_CODE = "^\\d{6,6}$";

    public static final String PATTERN_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    public static final String PATTERN_USERPW =
        "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+`\\-={}:\";'<>?,./]).{6,18}$";

    public static final String PATTERN_TELEPHONE = "^1[34578]\\d{9}$";

    public static final String PATTERN_MAILADDRESS
        = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
}
