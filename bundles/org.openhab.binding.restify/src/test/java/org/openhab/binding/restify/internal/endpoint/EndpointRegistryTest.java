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
package org.openhab.binding.restify.internal.endpoint;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openhab.binding.restify.internal.servlet.DispatcherServlet.Method.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.restify.internal.servlet.DispatcherServlet;
import org.openhab.binding.restify.internal.servlet.Response;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class EndpointRegistryTest {

    private static Endpoint endpoint() {
        return new Endpoint(null, new Response.JsonResponse(emptyMap()));
    }

    @ParameterizedTest(name = "register/find returns endpoint for matching method [{index}]")
    @EnumSource(DispatcherServlet.Method.class)
    void registerAndFindMatchingEndpoint(DispatcherServlet.Method method) throws RegistrationException {
        var registry = new EndpointRegistry();
        var endpoint = endpoint();

        registry.register("/status", method, endpoint);

        assertThat(registry.find("/status", method)).containsSame(endpoint);
    }

    @ParameterizedTest(name = "find is method/path specific [{index}]")
    @EnumSource(DispatcherServlet.Method.class)
    void findIsMethodAndPathSpecific(DispatcherServlet.Method method) throws RegistrationException {
        var registry = new EndpointRegistry();
        var endpoint = endpoint();

        registry.register("/status", method, endpoint);

        var differentMethod = method == GET ? POST : GET;
        assertThat(registry.find("/status", differentMethod)).isEmpty();
        assertThat(registry.find("/other", method)).isEmpty();
    }

    @ParameterizedTest(name = "register rejects duplicate key [{index}]")
    @EnumSource(DispatcherServlet.Method.class)
    void registerThrowsOnDuplicateKey(DispatcherServlet.Method method) throws RegistrationException {
        var registry = new EndpointRegistry();
        var endpoint = endpoint();

        registry.register("/status", method, endpoint);

        assertThatThrownBy(() -> registry.register("/status", method, endpoint))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Duplicate key found! key: %s:/status".formatted(method));
    }

    @ParameterizedTest(name = "unregister removes existing endpoint [{index}]")
    @EnumSource(DispatcherServlet.Method.class)
    void unregisterRemovesExistingEndpoint(DispatcherServlet.Method method) throws RegistrationException {
        var registry = new EndpointRegistry();
        var endpoint = endpoint();

        registry.register("/status", method, endpoint);

        registry.unregister("/status", method);

        assertThat(registry.find("/status", method)).isEmpty();
    }

    @ParameterizedTest(name = "unregister missing endpoint does not throw [{index}]")
    @EnumSource(DispatcherServlet.Method.class)
    void unregisterMissingEndpointDoesNotThrow(DispatcherServlet.Method method) {
        var registry = new EndpointRegistry();

        registry.unregister("/missing", method);

        assertThat(registry.find("/missing", method)).isEmpty();
    }
}
