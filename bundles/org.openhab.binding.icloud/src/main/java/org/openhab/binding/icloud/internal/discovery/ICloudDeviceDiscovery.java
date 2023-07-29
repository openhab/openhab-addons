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
package org.openhab.binding.icloud.internal.discovery;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.handler.ICloudAccountBridgeHandler;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation;
import org.openhab.binding.icloud.internal.utilities.ICloudTextTranslator;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each icloud device
 * found in the data received from {@link ICloudAccountBridgeHandler}.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
@NonNullByDefault
public class ICloudDeviceDiscovery extends AbstractDiscoveryService implements ICloudDeviceInformationListener {
    private final Logger logger = LoggerFactory.getLogger(ICloudDeviceDiscovery.class);
    private static final int TIMEOUT = 10;
    private ThingUID bridgeUID;
    private ICloudAccountBridgeHandler handler;
    private ICloudTextTranslator translatorService;

    public ICloudDeviceDiscovery(ICloudAccountBridgeHandler bridgeHandler, Bundle bundle,
            TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);

        this.handler = bridgeHandler;
        this.bridgeUID = bridgeHandler.getThing().getUID();
        this.translatorService = new ICloudTextTranslator(bundle, i18nProvider, localeProvider);
    }

    @Override
    public void deviceInformationUpdate(List<ICloudDeviceInformation> deviceInformationList) {
        for (ICloudDeviceInformation deviceInformationRecord : deviceInformationList) {

            String deviceTypeName = deviceInformationRecord.getDeviceDisplayName();
            String deviceOwnerName = deviceInformationRecord.getName();

            String thingLabel = deviceOwnerName + " (" + deviceTypeName + ")";
            String deviceId = deviceInformationRecord.getId();
            String deviceIdHash = Integer.toHexString(deviceId.hashCode());

            logger.debug("iCloud device discovery for [{}]", deviceInformationRecord.getDeviceDisplayName());

            ThingUID uid = new ThingUID(THING_TYPE_ICLOUDDEVICE, bridgeUID, deviceIdHash);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withProperty(DEVICE_PROPERTY_ID, deviceId)
                    .withProperty(translatorService.getText(DEVICE_PROPERTY_ID_LABEL), deviceId)
                    .withProperty(translatorService.getText(DEVICE_PROPERTY_OWNER_LABEL), deviceOwnerName)
                    .withRepresentationProperty(DEVICE_PROPERTY_ID).withLabel(thingLabel).build();

            logger.debug("Device [{}, {}] found.", deviceIdHash, deviceId);

            thingDiscovered(result);

        }
    }

    @Override
    protected void startScan() {
    }

    public void activate() {
        handler.registerListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        handler.unregisterListener(this);
    }
}
