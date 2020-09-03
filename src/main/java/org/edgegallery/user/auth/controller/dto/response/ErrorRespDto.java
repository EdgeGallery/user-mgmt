package org.edgegallery.user.auth.controller.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
