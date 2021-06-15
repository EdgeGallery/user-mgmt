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
    NO_LOGIN_USER(70000, "No login user."),
    NO_PERMISSION(70001, "No permission."),
    PARA_ILLEGAL(70002, "Illegal parameter."),
    DATABASE_EXCEPTION(70003, "Database Exception"),
    USER_NOT_FOUND(70004, "User not exist"),
    MOBILEPHONE_NOT_FOUND(70005, "mobile phone not exist"),
    MAILADDR_NOT_FOUND(70006, "email address not exist"),
    USERNAME_EXIST(70007, "Username has existed"),
    MOBILEPHONE_EXIST(70008, "Telephone has existed"),
    MAILADDR_EXIST(70009, "MailAddress has existed"),
    PASSWORD_INCORRECT(70010, "password incorrect"),
    VERIFY_CODE_ERROR(70011, "verification code incorrect"),
    SMS_CONNECT_FAILED(70012, "sms server connect failed."),
    SEND_VERIFYCODE_SMS_FAILED(70013, "send verification code by sms failed."),
    SEND_VERIFYCODE_MAIL_FAILED(70014, "send verification code by mail failed."),
    USER_REGISTER_FAILED(70015, "User register failed."),
    MODIFY_PW_FAILED(70016, "Modify password failed."),

    UNKNOWN(79999, "Unkown Error.");

    private int code;
    private String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
