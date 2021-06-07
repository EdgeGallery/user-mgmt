/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.user.auth.utils;

import java.time.format.DateTimeFormatter;

public class Consts {

    private Consts() {
    }

    public static final String SUPER_ADMIN_NAME = "admin";

    public static final String GUEST_USER_NAME = "guest";

    public static final String GUEST_USER_PW = "guest";

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static final int FIRST_LOGIN_JUDGE_DAYCOUNT = 5 * 365;

    public static final long MILLIS_ONE_DAY = 1 * 24 * 3600 * 1000L;

    public static final class PW_MODIFY_SCENE {
        public static final int FIRSTLOGIN = 1;

        public static final int EXPIRED = 2;
    }
}

