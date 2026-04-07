package com.gyul.tododook.global.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }
}
