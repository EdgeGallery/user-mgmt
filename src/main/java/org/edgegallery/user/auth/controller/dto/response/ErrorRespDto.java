/*
 *  Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.user.auth.utils.ErrorEnum;

@Setter
@Getter
@ToString
public class ErrorRespDto {
    private int code;
    private String message;
    private String detail;

    /**
     * construct.
     *
     * @param returnCode int code
     * @param message msg
     * @param detail detail
     */
    public ErrorRespDto(int returnCode, String message, String detail) {
        this.code = returnCode;
        this.message = message;
        this.detail = detail;
    }

    /**
     * build from error enum define.
     *
     * @param errorEnum error define
     * @return ErrorRespDto
     */
    public static ErrorRespDto build(ErrorEnum errorEnum) {
        return new ErrorRespDto(errorEnum.code(), errorEnum.message(), errorEnum.detail());
    }
}
