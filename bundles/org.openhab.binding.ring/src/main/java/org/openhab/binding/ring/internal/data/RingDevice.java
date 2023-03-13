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
package org.openhab.binding.ring.internal.data;

import org.openhab.binding.ring.handler.RingDeviceHandler;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 */
public interface RingDevice {

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
    public RingDeviceRegistry.Status getRegistrationStatus();

    /**
     * Set the registration status.
     *
     * @param registrationStatus
     */
    public void setRegistrationStatus(RingDeviceRegistry.Status registrationStatus);

    /**
     * Get the linked Ring account.
     *
     * @return the account.
     */
    public RingAccount getRingAccount();

    /**
     * Set the linked Ring account.
     *
     * @param ringAccount
     */
    public void setRingAccount(RingAccount ringAccount);

    /**
     * Get the linked Ring Device Handler.
     *
     * @return the handler.
     */
    public RingDeviceHandler getRingDeviceHandler();

    /**
     * Set the linked Ring Device Handler.
     *
     * @param ringDeviceHandler the handler.
     */
    public void setRingDeviceHandler(RingDeviceHandler ringDeviceHandler);
}
