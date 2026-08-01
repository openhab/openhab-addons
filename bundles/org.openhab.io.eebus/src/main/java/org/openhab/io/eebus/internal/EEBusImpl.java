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
package org.openhab.io.eebus.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.io.eebus.EEBus;
import org.openhab.io.eebus.internal.cert.EEBusCertificateStorage;
import org.openmuc.jeebus.ship.api.ShipNodeConfiguration;
import org.openmuc.jeebus.shipspine.ShipCommunication;
import org.openmuc.jeebus.shipspine.ShipCommunication.ConnectClientsTo;
import org.openmuc.jeebus.spine.api.Device;
import org.openmuc.jeebus.spine.api.Entity;
import org.openmuc.jeebus.spine.xsd.v1.DeviceTypeEnumType;
import org.openmuc.jeebus.spine.xsd.v1.EntityTypeEnumType;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a local EEBus SHIP/SPINE node presenting openHAB as a Controllable System (CS), and wires
 * its LPC/LPP use cases to whichever items are tagged with {@code eebus="lpc"}/{@code "lpp"}
 * metadata (see {@link EEBusChangeListener}). Analogous in structure to
 * {@code org.openhab.io.homekit.internal.HomekitImpl}, simplified to a single SHIP node instance
 * (EEBus's LPC/LPP use cases are household-wide singleton limits, unlike HomeKit's
 * many-accessories model).
 *
 * @author openHAB EEBus Add-on Contributors - Initial contribution
 */
@Component(service = { EEBus.class }, configurationPid = EEBusSettings.CONFIG_PID, property = {
        Constants.SERVICE_PID + "=org.openhab.eebus" })
@ConfigurableService(category = "io", label = "EEBus Integration", description_uri = "io:eebus")
@NonNullByDefault
public class EEBusImpl implements EEBus, ReadyService.ReadyTracker {

    private static final int CERTIFICATE_VALIDITY_DAYS = 3650;
    private static final String IDENTITY_STORAGE_KEY = "org.openhab.io.eebus.identity";

    private final Logger logger = LoggerFactory.getLogger(EEBusImpl.class);

    private final StorageService storageService;
    private final ItemRegistry itemRegistry;
    private final MetadataRegistry metadataRegistry;
    private final EventPublisher eventPublisher;
    private final ReadyService readyService;

    private EEBusSettings settings;
    private boolean started;

    private @Nullable Device device;
    private @Nullable ShipCommunication shipCommunication;
    private @Nullable EEBusChangeListener changeListener;

    @Activate
    public EEBusImpl(@Reference StorageService storageService, @Reference ItemRegistry itemRegistry,
            @Reference MetadataRegistry metadataRegistry, @Reference EventPublisher eventPublisher,
            @Reference ReadyService readyService, Map<String, Object> properties) {
        this.storageService = storageService;
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.eventPublisher = eventPublisher;
        this.readyService = readyService;
        this.settings = new Configuration(properties).as(EEBusSettings.class);
        readyService.registerTracker(this, new ReadyMarkerFilter().withType(StartLevelService.STARTLEVEL_MARKER_TYPE)
                .withIdentifier(Integer.toString(StartLevelService.STARTLEVEL_STATES)));
    }

    @Modified
    protected synchronized void modified(Map<String, Object> properties) {
        EEBusSettings newSettings = new Configuration(properties).as(EEBusSettings.class);
        boolean restart = settings.requiresRestart(newSettings);
        settings = newSettings;
        if (restart && started) {
            logger.info("EEBus: network/identity settings changed, restarting SHIP node");
            stopNode();
            startNode();
        }
    }

    @Override
    public synchronized void onReadyMarkerAdded(ReadyMarker readyMarker) {
        started = true;
        startNode();
    }

    @Override
    public synchronized void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        started = false;
        stopNode();
    }

    @Deactivate
    protected synchronized void deactivate() {
        stopNode();
    }

    private void startNode() {
        try {
            Storage<String> certStorage = storageService.getStorage(EEBusCertificateStorage.class.getName(),
                    EEBusCertificateStorage.class.getClassLoader());
            EEBusCertificateStorage certificateStorage = new EEBusCertificateStorage(certStorage, IDENTITY_STORAGE_KEY,
                    "CN=" + settings.friendlyName, CERTIFICATE_VALIDITY_DAYS);

            DeviceTypeEnumType deviceType = DeviceTypeEnumType.valueOf(settings.deviceType);
            EntityTypeEnumType entityType = EntityTypeEnumType.valueOf(settings.entityType);
            ConnectClientsTo connectPolicy = ConnectClientsTo.valueOf(settings.connectPolicy);

            // See org.openhab.binding.eebus's ServiceNameSanitizer javadoc: the mDNS service
            // instance name gets echoed back as the TLS SNI value by at least some SHIP clients
            // connecting in, so it must be sanitized to a safe charset.
            ShipNodeConfiguration shipConfig = new ShipNodeConfiguration(Set.of(settings.bindAddress), settings.port,
                    settings.wssPath, true, settings.deviceId, settings.serviceDomain,
                    ServiceNameSanitizer.sanitize(settings.friendlyName), certificateStorage, "openhab-eebus",
                    CERTIFICATE_VALIDITY_DAYS);

            ShipCommunication communication = new ShipCommunication(shipConfig).withConnectClientsTo(connectPolicy)
                    .withAutoAcceptMode(settings.autoAcceptPairing);
            Set<String> trustedSkis = parseTrustedSkis(settings.trustedSkis);
            if (!trustedSkis.isEmpty()) {
                communication = communication.withTrustedSkis(trustedSkis);
            }
            this.shipCommunication = communication;

            Device newDevice = Device.getBuilder().withDeviceType(deviceType).withCommunication(communication)
                    .withId(settings.deviceId).withDiscoverDevices(false).addEntity().setType(entityType)
                    .applyToDevice().build();
            this.device = newDevice;

            Entity entity = newDevice.getEntities().iterator().next();
            this.changeListener = new EEBusChangeListener(itemRegistry, metadataRegistry, eventPublisher, entity);

            logger.info("EEBus: SHIP node started, own SKI: {}", communication.getOwnSki());
        } catch (RuntimeException e) {
            logger.warn("EEBus: failed to start SHIP node", e);
        }
    }

    private void stopNode() {
        EEBusChangeListener listener = this.changeListener;
        if (listener != null) {
            listener.stop();
            this.changeListener = null;
        }
        Device dev = this.device;
        if (dev != null) {
            dev.close();
            this.device = null;
        }
        this.shipCommunication = null;
    }

    private static Set<String> parseTrustedSkis(String trustedSkis) {
        if (trustedSkis.isBlank()) {
            return Set.of();
        }
        return Set.of(trustedSkis.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public @Nullable String getOwnSki() {
        ShipCommunication communication = this.shipCommunication;
        return communication == null ? null : communication.getOwnSki();
    }
}
