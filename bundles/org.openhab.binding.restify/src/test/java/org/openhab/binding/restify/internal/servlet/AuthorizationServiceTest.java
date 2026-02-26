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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void authorizeAcceptsMatchingRequiredBasicAuthorization() {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Basic("john", "secret");
        var provided = basicHeader("john", "secret");

        // When / Then
        assertThatCode(() -> sut.authorize(required, provided)).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    @Test
    void authorizeAcceptsMatchingRequiredBearerAuthorization() {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Bearer("token-123");
        var provided = Authorization.BEARER_PREFIX + "token-123";

        // When / Then
        assertThatCode(() -> sut.authorize(required, provided)).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    @Test
    void authorizeRejectsBasicAuthorizationWithWrongScheme() {
        assertBasicAuthFailure("Bearer token");
    }

    @Test
    void authorizeRejectsBasicAuthorizationWithInvalidBase64() {
        assertBasicAuthFailure("Basic not-base64!!!");
    }

    @Test
    void authorizeRejectsBasicAuthorizationWithoutCredentialsSeparator() {
        var encodedWithoutSeparator = Base64.getEncoder().encodeToString("john".getBytes(UTF_8));
        assertBasicAuthFailure(Authorization.BASIC_PREFIX + encodedWithoutSeparator);
    }

    @Test
    void authorizeRejectsBasicAuthorizationWithWrongPassword() {
        assertBasicAuthFailure(basicHeader("john", "wrong"));
    }

    @Test
    void authorizeRejectsBearerAuthorizationWithWrongScheme() {
        assertBearerAuthFailure("Basic abc");
    }

    @Test
    void authorizeRejectsBearerAuthorizationWithWrongToken() {
        assertBearerAuthFailure(Authorization.BEARER_PREFIX + "wrong-token");
    }

    @Test
    void authorizeFailsWhenRequiredAuthorizationExistsButHeaderMissing() {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);

        // When / Then
        assertThatThrownBy(() -> sut.authorize(new Authorization.Basic("john", "secret"), null))
                .isInstanceOf(AuthorizationException.class).hasMessage("servlet.error.authorization.required");
        verify(restifyBinding, times(1)).getConfig();
    }

    @Test
    void authorizeUsesDefaultBasicWhenEndpointAuthorizationMissing() {
        // Given
        var config = new RestifyBindingConfig(false, "john:secret", null);
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, basicHeader("john", "secret"))).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    @Test
    void authorizeUsesDefaultBearerWhenEndpointAuthorizationMissing() {
        // Given
        var config = new RestifyBindingConfig(false, null, "token-123");
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, Authorization.BEARER_PREFIX + "token-123")).doesNotThrowAnyException();
        verify(restifyBinding, times(1)).getConfig();
    }

    @Test
    void authorizeFailsWhenEffectiveAuthorizationMissingAndEnforced() {
        assertMissingEffectiveAuthorizationEnforced(new RestifyBindingConfig(true, null, null), null);
        assertMissingEffectiveAuthorizationEnforced(new RestifyBindingConfig(true, null, null), "Digest something");
        assertMissingEffectiveAuthorizationEnforced(new RestifyBindingConfig(true, "invalid-format", null),
                basicHeader("john", "secret"));
        assertMissingEffectiveAuthorizationEnforced(new RestifyBindingConfig(true, null, null),
                Authorization.BEARER_PREFIX + "token-123");
    }

    @Test
    void authorizeAllowsWhenEffectiveAuthorizationMissingAndNotEnforced() {
        assertMissingEffectiveAuthorizationNotEnforced(new RestifyBindingConfig(false, null, null), null);
        assertMissingEffectiveAuthorizationNotEnforced(new RestifyBindingConfig(false, null, null), "Digest something");
        assertMissingEffectiveAuthorizationNotEnforced(new RestifyBindingConfig(false, "invalid-format", null),
                basicHeader("john", "secret"));
        assertMissingEffectiveAuthorizationNotEnforced(new RestifyBindingConfig(false, null, null),
                Authorization.BEARER_PREFIX + "token-123");
    }

    private void assertBasicAuthFailure(String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Basic("john", "secret");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-username-or-password");
        verify(restifyBinding, times(1)).getConfig();
    }

    private void assertBearerAuthFailure(String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(RestifyBindingConfig.DEFAULT);
        var required = new Authorization.Bearer("token-123");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-token");
        verify(restifyBinding, times(1)).getConfig();
    }

    private void assertMissingEffectiveAuthorizationEnforced(RestifyBindingConfig config, String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatThrownBy(() -> sut.authorize(null, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.missing-config-or-disable-enforce");
    }

    private void assertMissingEffectiveAuthorizationNotEnforced(RestifyBindingConfig config, String provided) {
        // Given
        when(restifyBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, provided)).doesNotThrowAnyException();
    }

    private static String basicHeader(String username, String password) {
        var credentials = "%s:%s".formatted(username, password);
        var encoded = Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
        return Authorization.BASIC_PREFIX + encoded;
    }
}
