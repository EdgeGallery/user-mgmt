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

package org.edgegallery.user.auth.external.iam;

import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.utils.Consts;
import org.springframework.util.StringUtils;

public final class ExternalUserUtil {

    private ExternalUserUtil() {}

    /**
     * judge is external user.
     *
     * @param userInfo user info
     * @return true if external user, otherwise false
     */
    public static boolean isExternalUser(String userInfo) {
        if (StringUtils.isEmpty(userInfo)) {
            return true;
        }

        return userInfo.startsWith(Consts.EXTERNAL_USER_PREFIX);
    }

    /**
     * convert user role.
     *
     * @param userRole user role
     * @return EnumRole
     */
    public static EnumRole convertUserRole(String userRole) {
        if (StringUtils.isEmpty(userRole)) {
            return EnumRole.TENANT;
        }

        try {
            return EnumRole.valueOf(userRole);
        } catch (IllegalArgumentException iae) {
            return EnumRole.TENANT;
        }
    }
}
