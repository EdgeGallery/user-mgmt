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
import org.edgegallery.user.auth.config.ServiceConfig;
import org.edgegallery.user.auth.config.validate.CheckParamsGenericUtils;
import org.edgegallery.user.auth.config.validate.IStringTrim;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Setter
@Getter
public class QueryUserCtrlDto extends CheckParamsGenericUtils implements IStringTrim {
    private static final String DEFAULT_SORTBY = "CREATETIME";

    private static final String DEFAULT_SORTORDER = "ASC";

    @Min(value = -1)
    private int offset;

    @Min(value = 0)
    @Max(value = 100)
    private int limit;

    @ApiModelProperty(example = "USERNAME")
    @Pattern(regexp = "|USERNAME|CREATETIME")
    private String sortBy;

    @ApiModelProperty(example = "ASC")
    @Pattern(regexp = "|ASC|DESC")
    private String sortOrder;

    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.sortBy = StringUtils.trimWhitespace(this.sortBy);
        this.sortOrder =  StringUtils.trimWhitespace(this.sortOrder);
    }

    /**
     * correct req content
     */
    public void correct() {
        if (validSort()) {
            return;
        }

        this.sortBy = DEFAULT_SORTBY;
        this.sortOrder = DEFAULT_SORTORDER;
    }

    private boolean validSort() {
        return !StringUtils.isEmpty(this.sortBy) && !StringUtils.isEmpty(this.sortOrder);
    }
}
