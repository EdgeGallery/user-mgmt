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

package org.edgegallery.user.auth.controller.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
public abstract class TenantBasicRespDto {

    @ApiModelProperty(required = true, example = "TestUser1")
    private String username;

    @ApiModelProperty(required = true, example = "hauwei")
    private String company;

    @ApiModelProperty(required = true, example = "male")
    private String gender;

    @ApiModelProperty(required = true, example = "15533449966")
    private String telephone;

    private boolean isAllowed;

    /**
     * check basic data by trim.
     */
    public void trim() {
        this.username = StringUtils.trimWhitespace(this.username);
        this.company = StringUtils.trimWhitespace(this.company);
        this.gender =  StringUtils.trimWhitespace(this.gender);
        this.telephone = StringUtils.trimWhitespace(this.telephone);
    }
}
