package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.mock.ContextMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static fr.lis.ikeyplus.rest.RestHandler.getParamOrThrow;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.DOT;
import static fr.lis.ikeyplus.utils.IkeyConfig.OutputFormat.TXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestHandlerTest {


    @Nested
    @DisplayName("getParamOrThrow")
    class GetParamOrThrowTest {

        private void doTest(String queryString, Consumer<Context> assertion) {
            ContextMock
                    .create(c -> c.getReq().queryString = queryString)
                    .execute(assertion);
        }

        @Test
        void should_extract_format() {
            doTest(
                    "format=DOT&keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isSameAs(DOT));
        }

        @Test
        void should_default_format() {
            doTest(
                    "keyRepresentation=flat",
                    ctx -> assertThat(getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isSameAs(TXT)
            );
        }

        @Test
        void should_throw_when_wrong_format() {
            doTest("format=NOOP&keyRepresentation=flat",
            ctx ->
                    assertThatThrownBy(() -> getParamOrThrow(ctx, "format", OutputFormat.class, TXT))
                            .isInstanceOf(BadRequestResponse.class)
            );
        }
    }

    @Nested
    @DisplayName("validateSddUrl")
    class ValidateSddUrlTest {

        @Test
        void should_validate_normal_http_url() {
            final var result = RestHandler.validateSddUrl("http://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_validate_normal_https_url() {
            final var result = RestHandler.validateSddUrl("https://google.com");
            assertThat(result).isNotNull();
        }

        @Test
        void should_not_validate_normal_ftp_url() {
            try {
                RestHandler.validateSddUrl("ftp://google.com");
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl protocol should be http or https");
            }
        }

        @Test
        void should_not_validate_null_url() {
            try {
                RestHandler.validateSddUrl(null);
                throw new RuntimeException();
            } catch (final Exception expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl cannot be null");
            }
        }

        @Test
        void should_not_validate_malformed_url() {
            try {
                RestHandler.validateSddUrl("../etc");
                throw new RuntimeException();
            } catch (final BadRequestResponse expected) {
                assertThat(expected.getMessage()).isEqualTo("sddUrl is malformed");
            }
        }
    }

}