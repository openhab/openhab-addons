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

import org.json.simple.JSONObject;
import org.openhab.binding.rainsoft.handler.RainSoftDeviceHandler;
import org.openhab.binding.rainsoft.internal.ApiConstants;
import org.openhab.binding.rainsoft.internal.RainSoftAccount;
import org.openhab.binding.rainsoft.internal.RainSoftDeviceRegistry;

/**
 * Interface common to all RainSoft devices.
 *
 * @author Ben Rosenblum - Initial contribution
 */

public abstract class AbstractRainSoftDevice implements RainSoftDevice {

    /**
     * The JSONObject contains the data retrieved from the RainSoft API,
     * or the data to send to the API.
     */
    protected JSONObject jsonObject;
    /**
     * The registration status.
     */
    private RainSoftDeviceRegistry.Status registrationStatus;
    /**
     * The linked RainSoft account.
     */
    private RainSoftAccount rainSoftAccount;
    /**
     * The linked RainSoftDeviceHandler.
     */
    private RainSoftDeviceHandler rainSoftDeviceHandler;

    public AbstractRainSoftDevice(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Get the device id.
     *
     * @return the device id.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getId() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_ID, "?").toString();
    }

    /**
     * Get the device device_id.
     *
     * @return the device device_id.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getDeviceId() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_DEVICE_ID, "?").toString();
    }

    /**
     * Get the device description.
     *
     * @return the device description.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getDescription() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_DESCRIPTION, "?").toString();
    }

    /**
     * Get the device firmware version.
     *
     * @return the device firmware version.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getFirmwareVersion() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_FIRMWARE_VERSION, "?").toString();
    }

    /**
     * Get the device time zone.
     *
     * @return the device time zone.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getTimeZone() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_TIME_ZONE, "?").toString();
    }

    /**
     * Get the device kind.
     *
     * @return the device kind.
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getKind() {
        return jsonObject.getOrDefault(ApiConstants.DEVICE_KIND, "?").toString();
    }

    /**
     * Get battery level
     *
     * @return battery level (%)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Integer getBattery() {
        if (jsonObject.getOrDefault(ApiConstants.DEVICE_BATTERY, "-1") != null) {
            return Integer.parseInt(jsonObject.getOrDefault(ApiConstants.DEVICE_BATTERY, "-1").toString());
        } else {
            return 0;
        }
    }

    /**
     * Get the registration status.
     *
     * @return
     */
    @Override
    public RainSoftDeviceRegistry.Status getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Set the registration status.
     *
     * @param status
     */
    @Override
    public void setRegistrationStatus(RainSoftDeviceRegistry.Status registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    /**
     * Get the linked RainSoft Device Handler.
     *
     * @return the handler.
     */
    @Override
    public RainSoftDeviceHandler getRainSoftDeviceHandler() {
        return rainSoftDeviceHandler;
    }

    /**
     * Set the linked RainSoft Device Handler.
     *
     * @param rainSoftDeviceHandler the handler.
     */
    @Override
    public void setRainSoftDeviceHandler(RainSoftDeviceHandler rainSoftDeviceHandler) {
        this.rainSoftDeviceHandler = rainSoftDeviceHandler;
    }

    /**
     * Get the linked RainSoft account.
     *
     * @return the account.
     */
    @Override
    public RainSoftAccount getRainSoftAccount() {
        return rainSoftAccount;
    }

    /**
     * Set the linked RainSoft account.
     *
     * @param rainSoftAccount
     */
    @Override
    public void setRainSoftAccount(RainSoftAccount rainSoftAccount) {
        this.rainSoftAccount = rainSoftAccount;
    }
}
