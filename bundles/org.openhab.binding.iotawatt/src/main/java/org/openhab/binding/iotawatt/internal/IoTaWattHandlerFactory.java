/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iotawatt.internal;

import static org.openhab.binding.iotawatt.internal.IoTaWattBindingConstants.THING_TYPE_IOTAWATT;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClient;
import org.openhab.binding.iotawatt.internal.handler.FetchDataServiceProvider;
import org.openhab.binding.iotawatt.internal.handler.HttpClientProvider;
import org.openhab.binding.iotawatt.internal.handler.IoTaWattClientProvider;
import org.openhab.binding.iotawatt.internal.handler.IoTaWattHandler;
import org.openhab.binding.iotawatt.internal.service.DeviceHandlerCallback;
import org.openhab.binding.iotawatt.internal.service.FetchDataService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.gson.Gson;

/**
 * The {@link IoTaWattHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.iotawatt", service = ThingHandlerFactory.class)
public class IoTaWattHandlerFactory extends BaseThingHandlerFactory
        implements HttpClientProvider, IoTaWattClientProvider, FetchDataServiceProvider {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_IOTAWATT);

    private final HttpClient insecureClient;
    private final Gson gson = new Gson();

    /**
     * Creates a IoTaWattHandlerFactory
     */
    public IoTaWattHandlerFactory() {
        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_IOTAWATT.equals(thingTypeUID)) {
            return new IoTaWattHandler(thing, this, this);
        }

        return null;
    }

    @Override
    public HttpClient getInsecureClient() {
        return insecureClient;
    }

    @Override
    public IoTaWattClient getIoTaWattClient(String hostname, long requestTimeout) {
        return new IoTaWattClient(hostname, requestTimeout, insecureClient, gson);
    }

    @Override
    public FetchDataService getFetchDataService(DeviceHandlerCallback deviceHandlerCallback) {
        return new FetchDataService(deviceHandlerCallback);
    }

    @Deactivate
    public void deactivate() {
        insecureClient.destroy();
    }
}
