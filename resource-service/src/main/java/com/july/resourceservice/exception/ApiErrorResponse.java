package com.july.resourceservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {
    private final String errorMessage;
    private final String errorCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> details;
}
