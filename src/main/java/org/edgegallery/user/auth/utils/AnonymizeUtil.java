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

import org.apache.commons.lang3.StringUtils;

public final class AnonymizeUtil {

    private static final int MOBILE_PHONE_LEN = 11;

    private static final String MASK_SIGN = "****";

    private AnonymizeUtil() {}

    /**
     * anonymize mail address.
     *
     * @param phoneNum mobile phone number
     * @return anonymized phone number
     */
    public static String anonymizePhoneNum(String phoneNum) {
        if (StringUtils.isEmpty(phoneNum) || phoneNum.length() != MOBILE_PHONE_LEN) {
            return phoneNum;
        }
        return phoneNum.substring(0, 3).concat(MASK_SIGN).concat(phoneNum.substring(7));
    }

    /**
     * anonymize mail address.
     *
     * @param mailAddress email address
     * @return anonymized email address
     */
    public static String anonymizeMail(String mailAddress) {
        if (StringUtils.isEmpty(mailAddress)) {
            return mailAddress;
        }

        int pos = mailAddress.indexOf("@");
        if (pos <= 1) {
            return mailAddress;
        }

        return mailAddress.substring(0, 1).concat(MASK_SIGN).concat(mailAddress.substring(pos));
    }

    /**
     * check if anonymized.
     *
     * @param strValue check str
     * @return if anonymized
     */
    public static boolean isAnonymized(String strValue) {
        if (StringUtils.isEmpty(strValue)) {
            return false;
        }

        return strValue.indexOf(MASK_SIGN) >= 0;
    }
}
