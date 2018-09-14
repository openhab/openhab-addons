/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for new DSMR Meters on a active DSMR bridge.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
public class DSMRMeterDiscoveryService extends DSMRDiscoveryService implements P1TelegramListener {

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDiscoveryService.class);

    /**
     * The {@link DSMRBridgeHandler} instance
     */
    private final DSMRBridgeHandler dsmrBridgeHandler;

    /**
     * Constructs a new {@link DSMRMeterDiscoveryService} attached to the give bridge handler.
     *
     * @param dsmrBridgeHandler The bridge handler this discovery service is attached to
     */
    public DSMRMeterDiscoveryService(DSMRBridgeHandler dsmrBridgeHandler) {
        this.dsmrBridgeHandler = dsmrBridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Start discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(true);
        dsmrBridgeHandler.registerDSMRMeterListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(false);
        super.stopScan();
        dsmrBridgeHandler.unregisterDSMRMeterListener(this);
    }

    @Override
    public void telegramReceived(P1Telegram telegram) {
        if (logger.isDebugEnabled()) {
            logger.debug("Detect meters from #{} objects", telegram.getCosemObjects().size());
        }
        meterDetector.detectMeters(telegram).forEach(m -> meterDiscovered(m, dsmrBridgeHandler.getThing().getUID()));
    }

    public void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void unsetLocaleProvider() {
        this.localeProvider = null;
    }

    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetTranslationProvider() {
        this.i18nProvider = null;
    }
}
