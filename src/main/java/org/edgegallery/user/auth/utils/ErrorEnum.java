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

/**
 * Error Define.
 */
public enum ErrorEnum {
    /**
     * no login user.
     */
    NO_LOGIN_USER(70000, "No login user."),
    /**
     * No permission.
     */
    NO_PERMISSION(70001, "No permission."),
    /**
     * Illegal parameter.
     */
    PARA_ILLEGAL(70002, "Illegal parameter."),
    /**
     * Database Exception.
     */
    DATABASE_EXCEPTION(70003, "Database Exception."),
    /**
     * The user does not exist.
     */
    USER_NOT_FOUND(70004, "The user does not exist."),
    /**
     * The mobile phone does not exist.
     */
    MOBILEPHONE_NOT_FOUND(70005, "The mobile phone does not exist."),
    /**
     * The email address does not exist.
     */
    MAILADDR_NOT_FOUND(70006, "The email address does not exist."),
    /**
     * The user name has been registered.
     */
    USERNAME_REGISTERED(70007, "The user name has been registered."),
    /**
     * The mobile phone number has been registered.
     */
    MOBILEPHONE_REGISTERED(70008, "The mobile phone number has been registered."),
    /**
     * The email address has been registered.
     */
    MAILADDR_REGISTERED(70009, "The email address has been registered."),
    /**
     * The password is incorrect.
     */
    PASSWORD_INCORRECT(70010, "The password is incorrect."),
    /**
     * The verification code is wrong or expired.
     */
    VERIFY_CODE_ERROR(70011, "The verification code is wrong or expired."),
    /**
     * Sms server connect failed.
     */
    SMS_CONNECT_FAILED(70012, "Sms server connect failed."),
    /**
     * Send verification code by sms failed.
     */
    SEND_VERIFYCODE_SMS_FAILED(70013, "Send verification code by sms failed."),
    /**
     * Send verification code by mail failed.
     */
    SEND_VERIFYCODE_MAIL_FAILED(70014, "Send verification code by mail failed."),
    /**
     * User register failed.
     */
    USER_REGISTER_FAILED(70015, "User register failed."),
    /**
     * Modify password failed.
     */
    MODIFY_PW_FAILED(70016, "Modify password failed."),
    /**
     * Login failed.
     */
    LOGIN_FAILED(70017, "Login failed."),

    /**
     * Unkown Error.
     */
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
