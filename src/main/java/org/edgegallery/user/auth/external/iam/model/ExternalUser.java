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

package org.edgegallery.user.auth.external.iam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.utils.Consts;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalUser {

    private static final int COMBINED_USERINFO_LEN = 5;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("mailAddress")
    private String mailAddress;

    @JsonProperty("userRole")
    private String userRole;

    /**
     * build combined user info.
     *
     * @return combined user info
     */
    public String build() {
        StringBuilder combinedUserInfo = new StringBuilder(Consts.EXTERNAL_USER_PREFIX)
            .append(Consts.ExternalUserType.EXTERNAL_IAM_USER).append(";")
            .append(getUserId()).append(";")
            .append(getUserName()).append(";")
            .append(getMailAddress()).append(";")
            .append(ExternalUserUtil.convertUserRole(getUserRole())).append(";");
        return combinedUserInfo.toString();
    }

    /**
     * parse from combined user info.
     *
     * @param combinedUserInfo
     */
    public void parse(String combinedUserInfo) {
        String[] combinedUserInfoArr = combinedUserInfo.split(";");
        if (combinedUserInfoArr.length < COMBINED_USERINFO_LEN) {
            return;
        }

        int index = 1;
        setUserId(combinedUserInfoArr[index++]);
        setUserName(combinedUserInfoArr[index++]);
        setMailAddress(combinedUserInfoArr[index++]);
        setUserRole(combinedUserInfoArr[index++]);
    }
}
