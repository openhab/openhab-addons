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
package org.openhab.binding.wemo.internal.handler;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * {@link WemoBaseThingHandler} provides a base implementation for the
 * concrete WeMo handlers for each thing type.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoBaseThingHandler extends BaseThingHandler implements UpnpIOParticipant {

    protected @Nullable UpnpIOService service;
    protected WemoHttpCall wemoHttpCaller;
    protected String host = "";

    public WemoBaseThingHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing);
        this.service = upnpIOService;
        this.wemoHttpCaller = wemoHttpCaller;
    }

    @Override
    public void initialize() {
        // can be overridden by subclasses
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // can be overridden by subclasses
    }

    @Override
    public void onStatusChanged(boolean status) {
        // can be overridden by subclasses
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        // can be overridden by subclasses
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        // can be overridden by subclasses
    }

    @Override
    public @Nullable String getUDN() {
        return (String) this.getThing().getConfiguration().get(WemoBindingConstants.UDN);
    }

    protected boolean isUpnpDeviceRegistered() {
        UpnpIOService service = this.service;
        if (service != null) {
            return service.isRegistered(this);
        }
        return false;
    }

    protected String getHost() {
        String localHost = host;
        if (!localHost.isEmpty()) {
            return localHost;
        }
        UpnpIOService localService = service;
        if (localService != null) {
            URL descriptorURL = localService.getDescriptorURL(this);
            if (descriptorURL != null) {
                return descriptorURL.getHost();
            }
        }
        return "";
    }
}
