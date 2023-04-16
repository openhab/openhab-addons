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
package org.openhab.binding.rainsoft.internal.data;

import org.openhab.binding.rainsoft.handler.RainSoftDeviceHandler;
import org.openhab.binding.rainsoft.internal.RainSoftAccount;
import org.openhab.binding.rainsoft.internal.RainSoftDeviceRegistry;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Interface common to all RainSoft devices.
 *
 * @author Ben Rosenblum - Initial contribution
 */
public interface RainSoftDevice {

    /**
     * Get the device id.
     *
     * @return the device id.
     */
    public String getId();

    /**
     * Get the device device_id.
     *
     * @return the device device_id.
     */
    public String getDeviceId();

    /**
     * Get the device description.
     *
     * @return the device description.
     */
    public String getDescription();

    /**
     * Get the device firmware version.
     *
     * @return the device firmware version.
     */
    public String getFirmwareVersion();

    /**
     * Get the device time zone.
     *
     * @return the device time zone.
     */
    public String getTimeZone();

    /**
     * Get the device kind.
     *
     * @return the device kind.
     */
    public String getKind();

    /**
     * Get battery level
     *
     * @return battery level (%)
     */
    public Integer getBattery();

    /**
     * Get the DiscoveryResult object to identify the device as
     * discovered thing.
     *
     * @return the device as DiscoveryResult instance.
     */
    public DiscoveryResult getDiscoveryResult();

    /**
     * Get the registration status.
     *
     * @return
     */
    public RainSoftDeviceRegistry.Status getRegistrationStatus();

    /**
     * Set the registration status.
     *
     * @param registrationStatus
     */
    public void setRegistrationStatus(RainSoftDeviceRegistry.Status registrationStatus);

    /**
     * Get the linked RainSoft account.
     *
     * @return the account.
     */
    public RainSoftAccount getRainSoftAccount();

    /**
     * Set the linked RainSoft account.
     *
     * @param rainSoftAccount
     */
    public void setRainSoftAccount(RainSoftAccount rainSoftAccount);

    /**
     * Get the linked RainSoft Device Handler.
     *
     * @return the handler.
     */
    public RainSoftDeviceHandler getRainSoftDeviceHandler();

    /**
     * Set the linked RainSoft Device Handler.
     *
     * @param rainSoftDeviceHandler the handler.
     */
    public void setRainSoftDeviceHandler(RainSoftDeviceHandler rainSoftDeviceHandler);
}
