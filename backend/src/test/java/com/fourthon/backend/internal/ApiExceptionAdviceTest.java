package com.fourthon.backend.internal;

import com.fourthon.backend.common.exception.CodedException;
import com.fourthon.backend.internal.config.ApiExceptionAdvice;
import com.fourthon.backend.internal.dto.ApiErrorResponse;
import com.fourthon.backend.recommendation.domain.GenreTab;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionAdviceTest {

    private final ApiExceptionAdvice apiExceptionAdvice = new ApiExceptionAdvice();

    @Test
    void 코드가_있는_예외는_상태와_코드와_메시지를_그대로_돌려준다() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleCoded(
                new CodedException(HttpStatus.CONFLICT, "TASTE_REPORT_EXISTS", "이미 취향 리포트가 있습니다"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(new ApiErrorResponse("TASTE_REPORT_EXISTS", "이미 취향 리포트가 있습니다"));
    }

    @Test
    void 요청_본문_검증_실패는_필드별_한국어_메시지로_알려준다() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "genres", null, false, new String[]{"NotEmpty"}, null, "must not be empty"));
        bindingResult.addError(new FieldError("request", "freeText", null, false, new String[]{"NotBlank"}, null, "must not be blank"));

        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleInvalidBody(
                new MethodArgumentNotValidException(parameter(), bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_INPUT");
        assertThat(response.getBody().message()).isEqualTo("genres: 하나 이상 있어야 합니다, freeText: 비어 있을 수 없습니다");
    }

    @Test
    void 잘못된_enum_값은_가능한_값을_함께_알려준다() throws NoSuchMethodException {
        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("DRAMA", GenreTab.class, "genre", parameter(), null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_PARAMETER");
        assertThat(response.getBody().message()).isEqualTo("genre 값이 올바르지 않습니다: DRAMA (가능한 값: ALL, MUSICAL, PLAY, ETC)");
    }

    @Test
    void 필수_파라미터가_없으면_이름을_알려준다() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleMissingParameter(
                new MissingServletRequestParameterException("performanceIds", "List"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(new ApiErrorResponse("MISSING_PARAMETER", "필수 값이 없습니다: performanceIds"));
    }

    @Test
    void 상태가_정해진_예외는_그_상태와_메시지를_돌려준다() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleUnexpected(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ApiErrorResponse("NOT_FOUND", "공연을 찾을 수 없습니다"));
    }

    @Test
    void 예상하지_못한_예외는_내부_정보_없이_500으로_알려준다() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionAdvice.handleUnexpected(
                new IllegalStateException("DB 연결 정보 노출 금지"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("DB");
    }

    private MethodParameter parameter() throws NoSuchMethodException {
        return new MethodParameter(ApiExceptionAdviceTest.class.getDeclaredMethod("target", GenreTab.class), 0);
    }

    private void target(GenreTab genre) {
    }
}
