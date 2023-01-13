/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.HostNameSource;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaType;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.system.SystemConfig;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class FreeDeviceHandler2Del extends HostHandler {
    private final Logger logger = LoggerFactory.getLogger(FreeDeviceHandler2Del.class);
    private final AudioHTTPServer audioHTTPServer;
    private final BundleContext bundleContext;

    private @Nullable ServiceRegistration<AudioSink> reg;
    private String ohIP;

    public FreeDeviceHandler2Del(Thing thing, AudioHTTPServer audioHTTPServer, String ipAddress,
            BundleContext bundleContext) {
        super(thing);
        this.audioHTTPServer = audioHTTPServer;
        this.ohIP = ipAddress;
        this.bundleContext = bundleContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        Map<String, String> properties = editProperties();
        String upnpName = properties.get(HostNameSource.UPNP.name());
        if (upnpName != null && Boolean.parseBoolean(properties.get(MediaType.AUDIO.name())) && reg == null) {
            reg = (ServiceRegistration<AudioSink>) bundleContext.registerService(AudioSink.class.getName(),
                    new AirMediaSink(this, audioHTTPServer, ohIP, bundleContext, "", upnpName), new Hashtable<>());
        }
    }

    @Override
    public void dispose() {
        ServiceRegistration<AudioSink> localReg = reg;
        if (localReg != null) {
            localReg.unregister();
        }
        super.dispose();
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            // fetchSystemConfig();
        }
    }

    protected abstract Optional<SystemConfig> getDeviceConfig() throws FreeboxException;

    protected abstract void internalCallReboot() throws FreeboxException;

    public void reboot() {
        try {
            internalCallReboot();
            triggerChannel(new ChannelUID(getThing().getUID(), SYS_INFO, BOX_EVENT), "reboot_requested");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "System rebooting...");
            stopRefreshJob();
            scheduler.schedule(this::initialize, 30, TimeUnit.SECONDS);
        } catch (FreeboxException e) {
            logger.warn("Error rebooting device : {}", e.getMessage());
        }
    }
}
