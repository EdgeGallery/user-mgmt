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
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.user.auth.config.validate.CheckParamsGenericUtils;
import org.edgegallery.user.auth.config.validate.IStringTrim;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Setter
@Getter
public class QueryUserReqDto extends CheckParamsGenericUtils implements IStringTrim {
    private String username;

    private String mailAddress;

    private String telephone;

    @ApiModelProperty(example = "ADMIN")
    @Pattern(regexp = "ALL|ADMIN|TENANT|GUEST")
    private String role;

    @Min(value = -1)
    @Max(value = 1)
    private int status;

    @NotNull
    @Valid
    private QueryUserCtrlDto queryCtrl;

    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.username = StringUtils.trimWhitespace(this.username);
        this.mailAddress =  StringUtils.trimWhitespace(this.mailAddress);
        this.telephone =  StringUtils.trimWhitespace(this.telephone);
        this.role =  StringUtils.trimWhitespace(this.role);
        this.queryCtrl.stringTrim();
    }

    /**
     * correct req content
     */
    public void correct() {
        queryCtrl.correct();
    }
}
