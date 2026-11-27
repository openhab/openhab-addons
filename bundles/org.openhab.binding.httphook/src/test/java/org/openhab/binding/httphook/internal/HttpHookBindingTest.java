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
package org.openhab.binding.httphook.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Hashtable;

import org.junit.jupiter.api.Test;

/**
 * @author Martin Grzeslowski - Initial contribution
 * @author Martin Grześlowski - Test safe configuration defaults and diagnostics
 */
class HttpHookBindingTest {
    private final HttpHookBinding sut = new HttpHookBinding();

    @Test
    void getConfigIsUninitializedBeforeFirstCallback() {
        assertThat(sut.getConfig()).isNull();
    }

    @Test
    void updatedWithNullPropertiesClearsCredentials() {
        // Given
        sut.updated(validProperties());

        // When
        sut.updated(null);

        // Then
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(true, null, null));
    }

    @Test
    void updatedWithEmptyPropertiesClearsCredentials() {
        // Given
        sut.updated(validProperties());

        // When
        sut.updated(new Hashtable<>());

        // Then
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(true, null, null));
    }

    @Test
    void updatedParsesBooleanAndStringValues() {
        // Given
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", "true");
        properties.put("defaultBasic", "john:secret");
        properties.put("defaultBearer", "token-123");

        // When
        sut.updated(properties);

        // Then
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(true, "john:secret", "token-123"));
    }

    @Test
    void updatedUsesDefaultBooleanWhenEnforceAuthenticationHasUnsupportedType() {
        // Given
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", 123);
        properties.put("defaultBasic", "");
        properties.put("defaultBearer", "");

        // When
        sut.updated(properties);

        // Then
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(true, null, null));
        properties.put("enforceAuthentication", "typo");
        sut.updated(properties);
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(true, null, null));
    }

    @Test
    void updatedOnlyAllowsAnonymousAccessWhenExplicitlyConfigured() {
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", "false");
        sut.updated(properties);
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(false, null, null));
    }

    @Test
    void configurationStringOmitsCredentials() {
        assertThat(new HttpHookBindingConfig(true, "john:secret", "token-123").toString())
                .isEqualTo("HttpHookBindingConfig[enforceAuthentication=true]");
    }

    @Test
    void updatedTrimsStringPropertiesAndConvertsBlankToNull() {
        // Given
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", false);
        properties.put("defaultBasic", "  john:secret  ");
        properties.put("defaultBearer", "   ");

        // When
        sut.updated(properties);

        // Then
        assertThat(sut.getConfig()).isEqualTo(new HttpHookBindingConfig(false, "john:secret", null));
    }

    private static Hashtable<String, Object> validProperties() {
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", true);
        properties.put("defaultBasic", "john:secret");
        properties.put("defaultBearer", "token-123");
        return properties;
    }
}
