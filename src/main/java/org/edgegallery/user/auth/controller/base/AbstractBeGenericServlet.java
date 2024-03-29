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

package org.edgegallery.user.auth.controller.base;

import fj.data.Either;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.springframework.http.ResponseEntity;

public abstract class AbstractBeGenericServlet {

    protected <T> ResponseEntity<Object> buildResponse(Either<T, FormatRespDto> either) {
        return either.isRight() ? buildErrorResponse(either.right().value()) : ResponseEntity.ok(either.left().value());
    }

    protected <T> ResponseEntity<Object> buildCreatedResponse(Either<T, FormatRespDto> either) {
        return either.isRight() ? buildErrorResponse(either.right().value())
            : ResponseEntity.status(Status.CREATED.getStatusCode()).body(either.left().value());
    }

    private ResponseEntity<Object> buildErrorResponse(FormatRespDto formatRespDto) {
        return ResponseEntity.status(formatRespDto.getErrStatus().getStatusCode())
            .body(formatRespDto.getErrorRespDto());
    }

}
