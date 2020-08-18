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
public class UniqueReqDto extends CheckParamsGenericUtils implements IStringTrim {

    @ApiModelProperty(required = true, example = "zhangtest")
    @Pattern(regexp = ServiceConfig.PATTERN_USERNAME)
    private String username;

    @ApiModelProperty(required = true, example = "15191881203")
    @Pattern(regexp = ServiceConfig.PATTERN_TELEPHONE)
    private String telephone;

    @Override
    public void stringTrim() {
        this.username = StringUtils.trimWhitespace(username);
        this.telephone = StringUtils.trimWhitespace(telephone);
    }
}
