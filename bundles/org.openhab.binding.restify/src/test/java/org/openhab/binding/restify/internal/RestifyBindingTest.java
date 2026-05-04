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
package org.openhab.binding.restify.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Hashtable;

import org.junit.jupiter.api.Test;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
class RestifyBindingTest {
    private final RestifyBinding sut = new RestifyBinding();

    @Test
    void getConfigReturnsDefaultWhenNeverUpdated() {
        assertThat(sut.getConfig()).isEqualTo(RestifyBindingConfig.DEFAULT);
    }

    @Test
    void updatedWithNullPropertiesKeepsExistingConfig() {
        // Given
        sut.updated(validProperties());
        var before = sut.getConfig();

        // When
        sut.updated(null);

        // Then
        assertThat(sut.getConfig()).isEqualTo(before);
    }

    @Test
    void updatedWithEmptyPropertiesKeepsExistingConfig() {
        // Given
        sut.updated(validProperties());
        var before = sut.getConfig();

        // When
        sut.updated(new Hashtable<>());

        // Then
        assertThat(sut.getConfig()).isEqualTo(before);
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
        assertThat(sut.getConfig()).isEqualTo(new RestifyBindingConfig(true, "john:secret", "token-123"));
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
        assertThat(sut.getConfig().enforceAuthentication()).isFalse();
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
        assertThat(sut.getConfig()).isEqualTo(new RestifyBindingConfig(false, "john:secret", null));
    }

    private static Hashtable<String, Object> validProperties() {
        var properties = new Hashtable<String, Object>();
        properties.put("enforceAuthentication", true);
        properties.put("defaultBasic", "john:secret");
        properties.put("defaultBearer", "token-123");
        return properties;
    }
}
