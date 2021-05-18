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

package org.edgegallery.user.auth.utils;

public enum ErrorEnum {
    DATABASE_EXCEPTION(100000001, "Database Exception", "Database Exception"),
    USER_NOT_FOUND(100000002, "User not exist", "User not exist"),
    MOBILEPHONE_NOT_FOUND(100000003, "mobile phone not exist", "mobile phone not exist"),
    MAILADDR_NOT_FOUND(100000004, "email address not exist", "email address not exist"),
    PASSWORD_INCORRECT(100000005, "password incorrect", "password incorrect"),
    VERIFY_CODE_ERROR(100000006, "verification code incorrect", "verification code incorrect");

    private int code;
    private String message;
    private String detail;

    ErrorEnum(int code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public String detail() {
        return this.detail;
    }
}
