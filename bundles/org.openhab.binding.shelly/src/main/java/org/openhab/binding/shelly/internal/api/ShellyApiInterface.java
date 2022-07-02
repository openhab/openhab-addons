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
package org.openhab.binding.shelly.internal.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;

/**
 * The {@link ShellyApiInterface} Defines device API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyApiInterface {
    public boolean isInitialized();

    public void setConfig(String thingName, ShellyThingConfiguration config);

    public ShellySettingsDevice getDeviceInfo() throws ShellyApiException;

    public ShellyDeviceProfile getDeviceProfile(String thingType) throws ShellyApiException;

    public ShellySettingsStatus getStatus() throws ShellyApiException;

    public void setLedStatus(String ledName, Boolean value) throws ShellyApiException;

    public void setSleepTime(int value) throws ShellyApiException;

    public ShellyStatusRelay getRelayStatus(Integer relayIndex) throws ShellyApiException;

    public void setRelayTurn(int id, String turnMode) throws ShellyApiException;

    public ShellyControlRoller getRollerStatus(int rollerIndex) throws ShellyApiException;

    public void setRollerTurn(int relayIndex, String turnMode) throws ShellyApiException;

    public void setRollerPos(int relayIndex, int position) throws ShellyApiException;

    public void setTimer(int index, String timerName, int value) throws ShellyApiException;

    public ShellyStatusSensor getSensorStatus() throws ShellyApiException;

    public ShellyStatusLight getLightStatus() throws ShellyApiException;

    public ShellyShortLightStatus getLightStatus(int index) throws ShellyApiException;

    public void setLightMode(String mode) throws ShellyApiException;

    public void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException;

    public void setLightParms(int lightIndex, Map<String, String> parameters) throws ShellyApiException;

    public ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException;

    public void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException;

    // Valve
    public void setValveMode(int id, boolean auto) throws ShellyApiException;

    public void setValveTemperature(int valveId, int value) throws ShellyApiException;

    public void setValveProfile(int valveId, int value) throws ShellyApiException;

    public void setValvePosition(int valveId, double value) throws ShellyApiException;

    public void setValveBoostTime(int valveId, int value) throws ShellyApiException;

    public void startValveBoost(int valveId, int value) throws ShellyApiException;

    public ShellyOtaCheckResult checkForUpdate() throws ShellyApiException;

    public ShellySettingsLogin getLoginSettings() throws ShellyApiException;

    public ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException;

    public String setWiFiRecovery(boolean enable) throws ShellyApiException;

    public String deviceReboot() throws ShellyApiException;

    public String setDebug(boolean enabled) throws ShellyApiException;

    public String getDebugLog(String id) throws ShellyApiException;

    public String setCloud(boolean enabled) throws ShellyApiException;

    public String setApRoaming(boolean enable) throws ShellyApiException;

    public String factoryReset() throws ShellyApiException;

    public String resetStaCache() throws ShellyApiException;

    public int getTimeoutsRecovered();

    public int getTimeoutErrors();

    public String getCoIoTDescription() throws ShellyApiException;

    public ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException;

    public void setActionURLs() throws ShellyApiException;

    public void sendIRKey(String keyCode) throws ShellyApiException, IllegalArgumentException;
}
