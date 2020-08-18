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

package org.mec.auth.server.config.validate;

import fj.data.Either;
import java.util.Objects;
import javax.ws.rs.core.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mec.auth.server.utils.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ValidateAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateAspect.class);

    /**
     * AOP around function.
     * @param joinPoint joinPoint
     */
    @Around("@annotation(org.mec.auth.server.config.validate.annotation.ParameterValidate)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Either<Boolean,FormatRespDto> result = validate(joinPoint.getArgs());
        if (result.isRight()) {
            LOGGER.error("method={} invoke fail. error={}",
                    joinPoint.getSignature(),result.right().value().toString());
            return result;
        }
        return joinPoint.proceed();
    }

    private static Either<Boolean,FormatRespDto> validate(Object[] args) {
        Either<Boolean,FormatRespDto> result = Either.left(true);
        if (args == null || args.length == 0) {
            return result;
        }
        for (Object arg : args) {
            if (Objects.isNull(arg)) {
                continue;
            }
            if (arg instanceof IStringTrim) {
                ((IStringTrim)arg).stringTrim();
            }
            Either<Boolean,String> validateResult = ValidatorUtil.validate(arg);
            if (validateResult.isRight()) {
                return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST,validateResult.right().value()));
            }
            if (arg instanceof ICheckParams) {
                Either<Boolean, FormatRespDto> dataCheckResult = ((ICheckParams)arg).checkData();
                if (dataCheckResult.isRight()) {
                    return dataCheckResult;
                }
            }
        }
        return result;
    }
}
