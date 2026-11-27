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
package org.openhab.binding.httphook.internal.servlet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Hashtable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.httphook.internal.HttpHookBinding;
import org.openhab.binding.httphook.internal.HttpHookBindingConfig;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Martin Grześlowski - Test inherited authentication policy and configuration updates
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    @Mock
    private HttpHookBinding httpHookBinding;

    @InjectMocks
    private AuthorizationService sut;

    @Test
    void authorizeAcceptsMatchingRequiredBasicAuthorization() {
        // Given
        var required = new Authorization.Basic("john", "secret");
        var provided = basicHeader("john", "secret");

        // When / Then
        assertThatCode(() -> sut.authorize(required, provided)).doesNotThrowAnyException();
    }

    @Test
    void authorizeAcceptsMatchingRequiredBearerAuthorization() {
        // Given
        var required = new Authorization.Bearer("token-123");
        var provided = Authorization.BEARER_PREFIX + "token-123";

        // When / Then
        assertThatCode(() -> sut.authorize(required, provided)).doesNotThrowAnyException();
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
        // When / Then
        assertThatThrownBy(() -> sut.authorize(new Authorization.Basic("john", "secret"), null))
                .isInstanceOf(AuthorizationException.class).hasMessage("servlet.error.authorization.required");
    }

    @Test
    void authorizeUsesDefaultBasicWhenEndpointAuthorizationMissing() {
        // Given
        var config = new HttpHookBindingConfig(false, "john:secret", null);
        when(httpHookBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, basicHeader("john", "secret"))).doesNotThrowAnyException();
    }

    @Test
    void authorizeUsesDefaultBearerWhenEndpointAuthorizationMissing() {
        // Given
        var config = new HttpHookBindingConfig(false, null, "token-123");
        when(httpHookBinding.getConfig()).thenReturn(config);

        // When / Then
        assertThatCode(() -> sut.authorize(null, Authorization.BEARER_PREFIX + "token-123")).doesNotThrowAnyException();
    }

    @Test
    void authorizeRequiresCredentialsWhenDefaultsAreConfiguredEvenIfEnforcementIsDisabled() {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, "john:secret", null));

        assertAuthorizationFailure(sut, null, "servlet.error.authorization.required");
        assertAuthorizationFailure(sut, "Digest something", "servlet.error.authorization.required");
        assertAuthorizationFailure(sut, "Bearer something", "servlet.error.authorization.required");
    }

    @Test
    void authorizeAcceptsEitherConfiguredSchemeCaseInsensitively() {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, "john:secret", "token-123"));

        var basic = basicHeader("john", "secret").replace(Authorization.BASIC_PREFIX, "bAsIc ");
        assertThatCode(() -> sut.authorize(null, basic)).doesNotThrowAnyException();
        assertThatCode(() -> sut.authorize(null, "bEaReR token-123")).doesNotThrowAnyException();
    }

    @Test
    void authorizeFailsClosedWhenDefaultBasicIsMalformed() {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, "invalid-format", null));

        assertAuthorizationFailure(sut, null, "servlet.error.authorization.required");
        assertAuthorizationFailure(sut, basicHeader("john", "secret"),
                "servlet.error.authorization.invalid-username-or-password");
    }

    @Test
    void authorizeAllowsAnonymousAccessOnlyAfterExplicitOptIn() {
        when(httpHookBinding.getConfig()).thenReturn(new HttpHookBindingConfig(false, null, null));

        assertThatCode(() -> sut.authorize(null, null)).doesNotThrowAnyException();
    }

    @Test
    void authorizeUsesLatestConfigurationAcrossStartupAndDeletion() {
        var binding = new HttpHookBinding();
        var service = new AuthorizationService(binding);
        var properties = new Hashtable<String, Object>();
        properties.put("defaultBearer", "token-123");
        assertAuthorizationFailure(service, null, "servlet.error.authorization.not-initialized");

        binding.updated(properties);
        assertThatCode(() -> service.authorize(null, "Bearer token-123")).doesNotThrowAnyException();
        assertAuthorizationFailure(service, null, "servlet.error.authorization.required");

        binding.updated(null);
        assertAuthorizationFailure(service, "Bearer token-123",
                "servlet.error.authorization.missing-config-or-disable-enforce");
        assertAuthorizationFailure(service, null, "servlet.error.authorization.missing-config-or-disable-enforce");

        properties.put("defaultBearer", "new-token");
        binding.updated(properties);
        assertThatCode(() -> service.authorize(null, "Bearer new-token")).doesNotThrowAnyException();
        assertAuthorizationFailure(service, "Bearer token-123", "servlet.error.authorization.invalid-token");
    }

    @Test
    void authorizationStringsOmitCredentials() {
        assertThat(new Authorization.Basic("john", "secret").toString()).isEqualTo("Basic[]");
        assertThat(new Authorization.Bearer("token-123").toString()).isEqualTo("Bearer[]");
    }

    private void assertBasicAuthFailure(String provided) {
        // Given
        var required = new Authorization.Basic("john", "secret");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-username-or-password");
    }

    private void assertBearerAuthFailure(String provided) {
        // Given
        var required = new Authorization.Bearer("token-123");

        // When / Then
        assertThatThrownBy(() -> sut.authorize(required, provided)).isInstanceOf(AuthorizationException.class)
                .hasMessage("servlet.error.authorization.invalid-token");
    }

    private static void assertAuthorizationFailure(AuthorizationService service, String provided, String key) {
        var error = assertThrows(AuthorizationException.class, () -> service.authorize(null, provided));
        assertThat(error.getMessage()).isEqualTo(key);
    }

    private static String basicHeader(String username, String password) {
        var credentials = "%s:%s".formatted(username, password);
        var encoded = Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
        return Authorization.BASIC_PREFIX + encoded;
    }
}
