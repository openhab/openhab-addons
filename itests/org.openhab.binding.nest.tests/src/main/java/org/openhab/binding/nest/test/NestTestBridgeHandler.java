/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nest.test;

import static org.openhab.binding.nest.internal.NestBindingConstants.BINDING_ID;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.openhab.binding.nest.internal.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.handler.NestRedirectUrlSupplier;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * The {@link NestTestBridgeHandler} is a {@link NestBridgeHandler} modified for testing. Using the
 * {@link NestTestRedirectUrlSupplier} it will always connect to same provided {@link #redirectUrl}.
 *
 * @author Wouter Born - Increase test coverage
 */
public class NestTestBridgeHandler extends NestBridgeHandler {

    class NestTestRedirectUrlSupplier extends NestRedirectUrlSupplier {

        NestTestRedirectUrlSupplier(Properties httpHeaders) {
            super(httpHeaders);
            this.cachedUrl = redirectUrl;
        }

        @Override
        public void resetCache() {
            // Skip resetting the URL so the test server keeps being used
        }
    }

    public static final ThingTypeUID THING_TYPE_TEST_BRIDGE = new ThingTypeUID(BINDING_ID, "test_account");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_TEST_BRIDGE);

    private String redirectUrl;

    public NestTestBridgeHandler(Bridge bridge, ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory,
            String redirectUrl) {
        super(bridge, clientBuilder, eventSourceFactory);
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected NestRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidAccessTokenException {
        return new NestTestRedirectUrlSupplier(getHttpHeaders());
    }
}
