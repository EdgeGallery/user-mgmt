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

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.user.auth.config.ServiceConfig;
import org.edgegallery.user.auth.config.validate.CheckParamsGenericUtils;
import org.edgegallery.user.auth.config.validate.IStringTrim;
import org.springframework.util.StringUtils;

@Setter
@Getter
public abstract class TenantBasicReqDto extends CheckParamsGenericUtils implements IStringTrim {

    @ApiModelProperty(required = true, example = "TestUser1")
    @Pattern(regexp = ServiceConfig.PATTERN_USERNAME)
    private String username;

    @ApiModelProperty(required = true, example = "huawei")
    private String company;

    @ApiModelProperty(required = true, example = "male")
    private String gender;

    @ApiModelProperty(example = "15533449966")
    private String telephone;

    @ApiModelProperty(example = "test@edgegallery.org")
    private String mailAddress;

    private boolean isAllowed = true;

    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.username = StringUtils.trimWhitespace(this.username);
        this.company = StringUtils.trimWhitespace(this.company);
        this.gender = StringUtils.trimWhitespace(this.gender);
        this.telephone =  StringUtils.trimWhitespace(this.telephone);
        this.mailAddress =  StringUtils.trimWhitespace(this.mailAddress);
    }
}
