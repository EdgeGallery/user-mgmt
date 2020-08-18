package org.mec.auth.server.controller.dto.response;

import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class FormatRespDto {

    private Response.Status errStatus;
    private ErrorRespDto errorRespDto;

    public FormatRespDto(Response.Status status, String detail) {
        this.errStatus = status;
        this.errorRespDto = new ErrorRespDto(status.getStatusCode(), status.toString(), detail);
    }

    public FormatRespDto(Response.Status status) {
        this.errStatus = status;
        this.errorRespDto = new ErrorRespDto(status.getStatusCode(), status.toString(), null);
    }
}
