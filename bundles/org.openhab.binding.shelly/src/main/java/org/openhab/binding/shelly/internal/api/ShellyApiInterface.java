/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyOtaCheckResult;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLogin;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsUpdate;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;

/**
 * The {@link ShellyApiInterface} Defines device API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public interface ShellyApiInterface {
    boolean isInitialized();

    void initialize() throws ShellyApiException;

    void setConfig(String thingName, ShellyThingConfiguration config);

    ShellySettingsDevice getDeviceInfo() throws ShellyApiException;

    ShellyDeviceProfile getDeviceProfile(String thingType, @Nullable ShellySettingsDevice device)
            throws ShellyApiException;

    ShellySettingsStatus getStatus() throws ShellyApiException;

    void setLedStatus(String ledName, boolean value) throws ShellyApiException;

    void setSleepTime(int value) throws ShellyApiException;

    ShellyStatusRelay getRelayStatus(int relayIndex) throws ShellyApiException;

    void setRelayTurn(int id, String turnMode) throws ShellyApiException;

    void resetMeterTotal(int id) throws ShellyApiException;

    ShellyRollerStatus getRollerStatus(int rollerIndex) throws ShellyApiException;

    void setRollerTurn(int relayIndex, String turnMode) throws ShellyApiException;

    void setRollerPos(int relayIndex, int position) throws ShellyApiException;

    void setAutoTimer(int index, String timerName, double value) throws ShellyApiException;

    ShellyStatusSensor getSensorStatus() throws ShellyApiException;

    ShellyStatusLight getLightStatus() throws ShellyApiException;

    ShellyShortLightStatus getLightStatus(int index) throws ShellyApiException;

    void setLightMode(String mode) throws ShellyApiException;

    void setLightParm(int lightIndex, String parm, String value) throws ShellyApiException;

    void setLightParms(int lightIndex, Map<String, String> parameters) throws ShellyApiException;

    ShellyShortLightStatus setLightTurn(int id, String turnMode) throws ShellyApiException;

    void setBrightness(int id, int brightness, boolean autoOn) throws ShellyApiException;

    void setValveMode(int id, boolean auto) throws ShellyApiException;

    void setValveTemperature(int valveId, double value) throws ShellyApiException;

    void setValveProfile(int valveId, int value) throws ShellyApiException;

    void setValvePosition(int valveId, double value) throws ShellyApiException;

    void setValveBoostTime(int valveId, int value) throws ShellyApiException;

    void startValveBoost(int valveId, int value) throws ShellyApiException;

    void muteSmokeAlarm(int smokeId) throws ShellyApiException;

    ShellyOtaCheckResult checkForUpdate() throws ShellyApiException;

    ShellySettingsUpdate firmwareUpdate(String uri) throws ShellyApiException;

    ShellySettingsLogin getLoginSettings() throws ShellyApiException;

    ShellySettingsLogin setLoginCredentials(String user, String password) throws ShellyApiException;

    String setWiFiRecovery(boolean enable) throws ShellyApiException;

    boolean setWiFiRangeExtender(boolean enable) throws ShellyApiException;

    boolean setEthernet(boolean enable) throws ShellyApiException;

    boolean setBluetooth(boolean enable) throws ShellyApiException;

    String deviceReboot() throws ShellyApiException;

    String setDebug(boolean enabled) throws ShellyApiException;

    String getDebugLog(String id) throws ShellyApiException;

    String setCloud(boolean enabled) throws ShellyApiException;

    String setApRoaming(boolean enable) throws ShellyApiException;

    String factoryReset() throws ShellyApiException;

    String resetStaCache() throws ShellyApiException;

    int getTimeoutsRecovered();

    int getTimeoutErrors();

    String getCoIoTDescription() throws ShellyApiException;

    ShellySettingsLogin setCoIoTPeer(String peer) throws ShellyApiException;

    void setActionURLs() throws ShellyApiException;

    void sendIRKey(String keyCode) throws ShellyApiException, IllegalArgumentException;

    void postEvent(String device, String index, String event, Map<String, String> parms) throws ShellyApiException;

    void close();

    void startScan();
}
