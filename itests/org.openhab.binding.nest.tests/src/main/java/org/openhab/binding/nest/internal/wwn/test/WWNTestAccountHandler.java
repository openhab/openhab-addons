/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.test;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.BINDING_ID;

import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.nest.internal.wwn.exceptions.InvalidWWNAccessTokenException;
import org.openhab.binding.nest.internal.wwn.handler.WWNAccountHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNRedirectUrlSupplier;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * The {@link WWNTestAccountHandler} is a {@link WWNAccountHandler} modified for testing. Using the
 * {@link NestTestRedirectUrlSupplier} it will always connect to same provided {@link #redirectUrl}.
 *
 * @author Wouter Born - Initial contribution
 */
public class WWNTestAccountHandler extends WWNAccountHandler {

    class NestTestRedirectUrlSupplier extends WWNRedirectUrlSupplier {

        NestTestRedirectUrlSupplier(Properties httpHeaders) {
            super(httpHeaders);
            this.cachedUrl = redirectUrl;
        }

        @Override
        public void resetCache() {
            // Skip resetting the URL so the test server keeps being used
        }
    }

    public static final ThingTypeUID THING_TYPE_TEST_BRIDGE = new ThingTypeUID(BINDING_ID, "wwn_test_account");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_TEST_BRIDGE);

    private String redirectUrl;

    public WWNTestAccountHandler(Bridge bridge, ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory,
            String redirectUrl) {
        super(bridge, clientBuilder, eventSourceFactory);
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected WWNRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidWWNAccessTokenException {
        return new NestTestRedirectUrlSupplier(getHttpHeaders());
    }
}
