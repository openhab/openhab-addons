/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.test;

import static org.openhab.binding.nest.NestBindingConstants.BINDING_ID;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.handler.NestRedirectUrlSupplier;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;

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

    public final static ThingTypeUID THING_TYPE_TEST_BRIDGE = new ThingTypeUID(BINDING_ID, "test_account");
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_TEST_BRIDGE);

    private String redirectUrl;

    public NestTestBridgeHandler(Bridge bridge, String redirectUrl) {
        super(bridge);
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected NestRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidAccessTokenException {
        return new NestTestRedirectUrlSupplier(getHttpHeaders());
    }

}
