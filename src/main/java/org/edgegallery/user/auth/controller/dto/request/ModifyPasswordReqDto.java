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

package org.edgegallery.user.auth.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.user.auth.config.ServiceConfig;
import org.edgegallery.user.auth.config.validate.CheckParamsGenericUtils;
import org.edgegallery.user.auth.config.validate.IStringTrim;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class ModifyPasswordReqDto extends CheckParamsGenericUtils implements IStringTrim {
    @SerializedName("type")
    @JsonProperty("type")
    @Min(value = 1)
    @Max(value = 2)
    private int type = 2;

    @SerializedName("newPassword")
    @JsonProperty("newPassword")
    @ApiModelProperty(required = true, example = "TestPassword1")
    @Pattern(regexp = ServiceConfig.PATTERN_PASSWORD)
    private String newPassword;

    @SerializedName("userId")
    @JsonProperty("userId")
    private String userId;

    @SerializedName("oldPassword")
    @JsonProperty("oldPassword")
    @ApiModelProperty(example = "TestOldPassword#321")
    private String oldPassword;

    @SerializedName("telephone")
    @JsonProperty("telephone")
    @ApiModelProperty(example = "15191881309")
    @Pattern(regexp = ServiceConfig.PATTERN_TELEPHONE)
    private String telephone;

    @SerializedName("mailAddress")
    @JsonProperty("mailAddress")
    @ApiModelProperty(example = "test@edgegallery.org")
    @Pattern(regexp = ServiceConfig.PATTERN_MAILADDRESS)
    private String mailAddress;

    @SerializedName("verificationCode")
    @JsonProperty("verificationCode")
    @ApiModelProperty(required = true, example = "123456")
    @Pattern(regexp = ServiceConfig.PATTERN_VERIFICATION_CODE)
    private String verificationCode;

    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.newPassword = StringUtils.trimWhitespace(this.newPassword);

        this.userId = StringUtils.trimWhitespace(this.userId);
        this.oldPassword = StringUtils.trimWhitespace(this.oldPassword);

        this.telephone = StringUtils.trimWhitespace(this.telephone);
        this.mailAddress = StringUtils.trimWhitespace(this.mailAddress);
        this.verificationCode = StringUtils.trimWhitespace(this.verificationCode);
    }

    /**
     * judge if the request is retrieve password type
     *
     * @return true if the request is retrieve password type, otherwise false
     */
    public boolean isRetrieveType() {
        return this.type == 2;
    }
}
