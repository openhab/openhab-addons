/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.discovery;

import static org.openhab.binding.icloud.ICloudBindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.icloud.handler.ICloudAccountBridgeHandler;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.json.response.ICloudDeviceInformation;
import org.openhab.binding.icloud.internal.utilities.ICloudTextTranslator;
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
        if (deviceInformationList != null) {
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
