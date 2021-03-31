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

package org.edgegallery.user.auth.controller.dto.request;

import fj.data.Either;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.user.auth.config.validate.CheckParamsGenericUtils;
import org.edgegallery.user.auth.config.validate.IStringTrim;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.utils.Consts;
import org.springframework.util.StringUtils;

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

    private String createTimeBegin;

    private String createTimeEnd;

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
        this.createTimeBegin =  StringUtils.trimWhitespace(this.createTimeBegin);
        this.createTimeEnd =  StringUtils.trimWhitespace(this.createTimeEnd);
        this.queryCtrl.stringTrim();
    }

    @Override
    protected Either<Boolean, FormatRespDto> checkDataFormat() {
        LocalDate beginDate = null;
        if (!StringUtils.isEmpty(createTimeBegin)) {
            try {
                beginDate = LocalDate.parse(createTimeBegin, Consts.DATE_TIME_FORMATTER);
            } catch (DateTimeParseException dtpe) {
                return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST, "begin time is invalid."));
            }
        }

        LocalDate endDate = null;
        if (!StringUtils.isEmpty(createTimeEnd)) {
            try {
                endDate = LocalDate.parse(createTimeEnd, Consts.DATE_TIME_FORMATTER);
            } catch (DateTimeParseException dtpe) {
                return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST, "end time is invalid."));
            }
        }

        if (beginDate != null && endDate != null && beginDate.isAfter(endDate)) {
            return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST, "begin time is after end time."));
        }

        if (endDate != null) {
            endDate = endDate.plus(Period.ofDays(1));
            this.createTimeEnd = endDate.format(Consts.DATE_TIME_FORMATTER);
        }

        return Either.left(true);
    }
}
