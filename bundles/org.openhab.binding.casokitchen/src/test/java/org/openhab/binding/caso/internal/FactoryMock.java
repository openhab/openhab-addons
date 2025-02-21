/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.caso.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.casokitchen.internal.CasoKitchenHandlerFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * {@link FactoryMock} for creating unit test handlers
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FactoryMock extends CasoKitchenHandlerFactory {

    public FactoryMock(HttpClientFactory httpFactory, final TimeZoneProvider tzp) {
        super(httpFactory, tzp);
    }

    @Override
    public @Nullable ThingHandler createHandler(Thing thing) {
        return super.createHandler(thing);
    }
}
