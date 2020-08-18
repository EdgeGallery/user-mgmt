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

package org.mec.auth.server.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.mec.auth.server.config.ServiceConfig;
import org.mec.auth.server.config.validate.CheckParamsGenericUtils;
import org.mec.auth.server.config.validate.IStringTrim;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class RetrievePasswordReqDto extends CheckParamsGenericUtils implements IStringTrim {

    @SerializedName("telephone")
    @JsonProperty("telephone")
    @ApiModelProperty(required = true, example = "15191881309")
    @Pattern(regexp = ServiceConfig.PATTERN_TELEPHONE)
    private String telephone;

    @SerializedName("newPassword")
    @JsonProperty("newPassword")
    @ApiModelProperty(required = true, example = "TestPassword1")
    @Pattern(regexp = ServiceConfig.PATTERN_PASSWORD)
    private String newPassword;

    @SerializedName("verificationCode")
    @JsonProperty("verificationCode")
    @ApiModelProperty(required = true, example = "123456")
    @Pattern(regexp = ServiceConfig.PATTERN_VERIFICATION_CODE)
    private String verificationCode;


    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.telephone = StringUtils.trimWhitespace(this.telephone);
        this.newPassword = StringUtils.trimWhitespace(this.newPassword);
        this.verificationCode = StringUtils.trimWhitespace(this.verificationCode);
    }
}
