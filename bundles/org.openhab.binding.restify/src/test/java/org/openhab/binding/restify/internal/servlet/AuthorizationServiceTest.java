/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.restify.internal.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.restify.internal.RestifyBinding;
import org.openhab.binding.restify.internal.RestifyBindingConfig;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    @Mock
    private RestifyBinding restifyBinding;

    @InjectMocks
    private AuthorizationService sut;

    private static String basicHeader(String username, String password) {
        var credentials = "%s:%s".formatted(username, password);
        var encoded = Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
        return Authorization.BASIC_PREFIX + encoded;
    }

    private static Stream<Arguments> validRequiredAuthorizations() {
        return Stream.of(Arguments.of(new Authorization.Basic("john", "secret"), basicHeader("john", "secret")),
                Arguments.of(new Authorization.Bearer("token-123"), Authorization.BEARER_PREFIX + "token-123"));
    }

    @ParameterizedTest(name = "accepts matching required authorization [{index}]")
    @MethodSource("validRequiredAuthorizations")
    void authorizeAcceptsMatchingProvidedAuthorization(Authorization required, String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);

        // When / Then
        assertThatCode(() -> sut.authorize(required, provided)).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    private static Stream<Arguments> invalidBasicHeaders() {
        return Stream.of(Arguments.of("Bearer token"), Arguments.of("Basic not-base64!!!"),
                Arguments.of(Authorization.BASIC_PREFIX + Base64.getEncoder().encodeToString("john".getBytes(UTF_8))),
                Arguments.of(basicHeader("john", "wrong")));
    }

    @ParameterizedTest(name = "rejects invalid basic header [{index}]")
    @MethodSource("invalidBasicHeaders")
    void authorizeRejectsInvalidBasicAuthorization(String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Basic("john", "secret");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-username-or-password");
        verify(restifyBinding, times(1)).getConfig();
    }

    private static Stream<Arguments> invalidBearerHeaders() {
        return Stream.of(Arguments.of("Basic abc"), Arguments.of(Authorization.BEARER_PREFIX + "wrong-token"));
    }

    @ParameterizedTest(name = "rejects invalid bearer header [{index}]")
    @MethodSource("invalidBearerHeaders")
    void authorizeRejectsInvalidBearerAuthorization(String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Bearer("token-123");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-token");
        verify(restifyBinding, times(1)).getConfig();
    }

    @ParameterizedTest(name = "fails when header missing for required authorization [{index}]")
    @MethodSource("validRequiredAuthorizations")
    void authorizeFailsWhenProvidedHeaderMissing(Authorization required, @SuppressWarnings("unused") String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, null)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.required");
        verify(restifyBinding, times(1)).getConfig();
    }

    private static Stream<Arguments> validDefaultAuthorizations() {
        return Stream.of(
                Arguments.of(new RestifyBindingConfig(false, "john:secret", null), basicHeader("john", "secret")),
                Arguments.of(new RestifyBindingConfig(false, null, "token-123"),
                        Authorization.BEARER_PREFIX + "token-123"));
    }

    @ParameterizedTest(name = "uses configured default authorization [{index}]")
    @MethodSource("validDefaultAuthorizations")
    void authorizeUsesBindingDefaultWhenEndpointAuthorizationMissing(RestifyBindingConfig config, String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, provided)).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    private static Stream<Arguments> missingEffectiveAuthorizationCases() {
        return Stream.of(Arguments.of(new RestifyBindingConfig(true, null, null), null),
                Arguments.of(new RestifyBindingConfig(true, null, null), "Digest something"),
                Arguments.of(new RestifyBindingConfig(true, "invalid-format", null), basicHeader("john", "secret")),
                Arguments.of(new RestifyBindingConfig(true, null, null), Authorization.BEARER_PREFIX + "token-123"));
    }

    @ParameterizedTest(name = "fails when no effective auth and enforcement enabled [{index}]")
    @MethodSource("missingEffectiveAuthorizationCases")
    void authorizeFailsWhenEffectiveAuthorizationMissingAndEnforced(RestifyBindingConfig config, String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatThrownBy(() -> sut.authorize(null, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.missing-config-or-disable-enforce");
        verify(restifyBinding, times(1)).getConfig();
    }

    @ParameterizedTest(name = "allows when no effective auth and enforcement disabled [{index}]")
    @MethodSource("missingEffectiveAuthorizationCases")
    void authorizeAllowsWhenEffectiveAuthorizationMissingAndNotEnforced(RestifyBindingConfig config, String provided) {
        // Given
        var nonEnforcedConfig = new RestifyBindingConfig(false, config.defaultBasic(), config.defaultBearer());
        when(restifyBinding.getConfig()).thenReturn(nonEnforcedConfig);

        // When / Then
        assertThatCode(() -> sut.authorize(null, provided)).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }
}
