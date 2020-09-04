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

package org.edgegallery.user.auth.utils;

import fj.data.Either;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

public class ValidatorUtil {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidatorUtil() {}

    /**
     * check parameters.
     * @param obj obj
     * @param <T> T
     * @return
     */
    public static <T> Either<Boolean,String> validate(T obj) {
        Set<ConstraintViolation<T>> set = validator.validate(obj);
        if (set == null || set.isEmpty()) {
            return Either.left(true);
        }
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<T> violation : set) {
            sb.append(violation.getPropertyPath().toString());
            sb.append(":");
            sb.append(violation.getMessage()).append(". ");
        }
        return Either.right(sb.toString());
    }

}
