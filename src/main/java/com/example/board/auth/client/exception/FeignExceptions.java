package com.example.board.auth.client.exception;

import com.example.board.auth.commons.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeignExceptions {
    private final JsonMapper jsonMapper;

    public Optional<ApiResponse<Void>> extractErrorResponse(FeignException e) {
        return Optional.ofNullable(e.contentUTF8())
                .filter(jsonBody -> !jsonBody.isBlank())
                .flatMap(jsonBody -> {
                    try {
                        return Optional.of(jsonMapper.readValue(jsonBody, new TypeReference<ApiResponse<Void>>() {}));
                    } catch (JacksonException _) {
                        return Optional.empty();
                    }
                });
    }

    public boolean isRetryableStatus(int status) {
        return status == -1 || status == 429 || status == 502 || status == 503 || status == 504;
    }
}
