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
package org.openhab.binding.argoclima.internal.device.api;

import java.net.InetAddress;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.device.passthrough.requests.DeviceSidePostRtUpdateDTO;
import org.openhab.binding.argoclima.internal.device.passthrough.requests.DeviceSideUpdateDTO;
import org.openhab.binding.argoclima.internal.exception.ArgoApiCommunicationException;
import org.openhab.binding.argoclima.internal.exception.ArgoApiProtocolViolationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Argo protocol implementation for a LOCAL connection to the device
 * <p>
 * IMPORTANT: Local doesn't necessarily mean "directly reachable". This class is also used for devices behind NAT, where
 * all the communication is happening indirect (through intercepted device-side polls, and modifying responses through
 * stub/proxy server)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaLocalDevice extends ArgoClimaDeviceApiBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InetAddress ipAddress; // The direct IP address
    private final Optional<InetAddress> localIpAddress; // The indirect IP address (local subnet) - possibly not
                                                        // reachable if behind NAT (optional)
    private final Optional<String> cpuId; // The configured CPU id (if any) - for matching intercepted responses
    private final String id;
    private final Consumer<Map<ArgoDeviceSettingType, State>> onStateUpdate;
    private final Consumer<ThingStatus> onReachableStatusChange;
    private final int port;
    private final boolean matchAnyIncomingDeviceIp;

    /**
     * C-tor
     *
     * @param config The Thing configuration
     * @param targetDeviceIpAddress The IP address of the directly-connected device (for indirect mode, the device does
     *            NOT need to be reachable through this address!)
     * @param port The port to talk to the directly-connected device
     * @param localDeviceIpAddress Optional, local subnet IP of the device (ex. if behind NAT). Used to match
     *            intercepted responses (in indirect mode) to this thing. This may be
     *            {@link ArgoClimaConfigurationLocal#getMatchAnyIncomingDeviceIp() bypassed}
     * @param cpuId Optional, CPUID of the Wi-Fi chip of the device. If provided, will be used to match intercepted
     *            responses (in indirect mode) to this thing
     * @param client The common HTTP client used for issuing direct requests
     * @param timeZoneProvider System-wide TZ provider, for parsing/displaying local dates
     * @param i18nProvider Framework's translation provider
     * @param onStateUpdate Callback to be invoked when device status gets updated(device-side channel updates)
     * @param onReachableStatusChange Callback to be invoked when device's reachability status (online) changes
     * @param onDevicePropertiesUpdate Callback to invoke when device properties get refreshed
     * @param thingUid The UID of the Thing owning this server (used for logging)
     */
    public ArgoClimaLocalDevice(ArgoClimaConfigurationLocal config, InetAddress targetDeviceIpAddress, int port,
            Optional<InetAddress> localDeviceIpAddress, Optional<String> cpuId, HttpClient client,
            TimeZoneProvider timeZoneProvider, ArgoClimaTranslationProvider i18nProvider,
            Consumer<Map<ArgoDeviceSettingType, State>> onStateUpdate, Consumer<ThingStatus> onReachableStatusChange,
            Consumer<SortedMap<String, String>> onDevicePropertiesUpdate, String thingUid) {
        super(config, client, timeZoneProvider, i18nProvider, onDevicePropertiesUpdate, "");
        this.ipAddress = targetDeviceIpAddress;
        this.port = port;
        this.localIpAddress = localDeviceIpAddress;
        this.cpuId = cpuId;
        this.matchAnyIncomingDeviceIp = config.getMatchAnyIncomingDeviceIp();
        this.onStateUpdate = onStateUpdate;
        this.onReachableStatusChange = onReachableStatusChange;
        this.id = thingUid;
    }

    @Override
    protected URL getDeviceStateQueryUrl() {
        // Hard-coded values are part of ARGO protocol
        return newUrl(Objects.requireNonNull(this.ipAddress.getHostName()), this.port, "/", "HMI=&UPD=0");
    }

    @Override
    protected URL getDeviceStateUpdateUrl() {
        // Hard-coded values are part of ARGO protocol
        return newUrl(Objects.requireNonNull(this.ipAddress.getHostName()), this.port, "/",
                String.format("HMI=%s&UPD=1", this.deviceStatus.getDeviceCommandStatus()));
    }

    @Override
    public final ReachabilityStatus isReachable() {
        try {
            var status = extractDeviceStatusFromResponse(pollForCurrentStatusFromDeviceSync(getDeviceStateQueryUrl()));

            try {
                this.deviceStatus.fromDeviceString(status.getCommandString());
            } catch (ArgoApiProtocolViolationException e) {
                throw new ArgoApiCommunicationException("Unrecognized API response",
                        "thing-status.cause.argoclima.exception.unrecognized-response", i18nProvider, e);
            }
            this.updateDevicePropertiesFromDeviceResponse(status.getProperties(), this.deviceStatus);

            return new ReachabilityStatus(true, "");
        } catch (ArgoApiCommunicationException e) {
            logger.debug("Device not reachable: {}", e.getMessage());
            return new ReachabilityStatus(false,
                    Objects.requireNonNull(i18nProvider.getText("thing-status.argoclima.local-unreachable",
                            "Failed to communicate with Argo HVAC device at [http://{0}:{1,number,#}{2}]. {3}",
                            this.getDeviceStateQueryUrl().getHost(),
                            this.getDeviceStateQueryUrl().getPort() != -1 ? this.getDeviceStateQueryUrl().getPort()
                                    : this.getDeviceStateQueryUrl().getDefaultPort(),
                            this.getDeviceStateQueryUrl().getPath(), e.getLocalizedMessage())));
        }
    }

    @Override
    protected DeviceStatus extractDeviceStatusFromResponse(String apiResponse) {
        // local device response does not have all properties, but is always fresh
        return new DeviceStatus(apiResponse, OffsetDateTime.now(), i18nProvider);
    }

    /**
     * Update device state from intercepted message from device to remote server (device's own send of command)
     * This is sent in response to cloud-side command (likely a form of acknowledgement)
     *
     * @implNote This function is a WORK IN PROGRESS (and not doing anything useful at the present!)
     * @param fromDevice the POST message sent by the device, in acknowledgement of fulfilling remote-side command
     */
    public void updateDeviceStateFromPostRtRequest(DeviceSidePostRtUpdateDTO fromDevice) {
        if (this.cpuId.isEmpty()) {
            logger.trace(
                    "Got post update confirmation from device {}, but was not able to match it to this device b/c no CPUID is configured. Configure {} setting to allow this mode...",
                    fromDevice.cpuId, ArgoClimaBindingConstants.PARAMETER_DEVICE_CPU_ID);
            return;
        }
        if (!this.cpuId.get().equalsIgnoreCase(fromDevice.cpuId)) {
            logger.trace("Got post update from device [ID={}], but this entity belongs to device [ID={}]. Ignoring...",
                    fromDevice.cpuId, this.cpuId.orElse("???"));
            return;
        }

        // NOTICE (on possible future extension): The values from 'data' param of the response are NOT following the HMI
        // syntax in the GET requests (much more data is available in this requests - and while actual responses seem
        // empty... perhaps a response to this can provide iFeel temperatures?)
        // There are some similarities -> ex. target/actual temperatures are at offset 112 & 113 of the array,
        // so at the very least, could get the known values (but not as trivial as:
        // # fromDevice.dataParam.split(ArgoDeviceStatus.HMI_ELEMENT_SEPARATOR).Arrays.stream(paramArray).skip(111)
        // # .limit(ArgoDeviceStatus.HMI_UPDATE_ELEMENT_COUNT).toList()
        // Overall, this needs more reverse-engineering (but works w/o this information, so not implementing for now)
    }

    /**
     * Update device state from intercepted message from device to remote server (device's own polling)
     * <p>
     * Important: The device-sent message will only be used for update if it matches to configured value
     * (this is to avoid updating status of a completely different device)
     * <p>
     * Most robust match is by CPUID, though if n/a, localIP is used as heuristic alternative as well
     *
     * @param deviceUpdate The device-side update request
     */
    public void updateDeviceStateFromPushRequest(DeviceSideUpdateDTO deviceUpdate)
            throws ArgoApiCommunicationException {
        String hmiStringFromDevice = deviceUpdate.currentValues;
        String deviceIP = deviceUpdate.deviceIp;
        String deviceCpuId = deviceUpdate.cpuId;

        if (this.cpuId.isPresent() && !this.cpuId.get().equalsIgnoreCase(deviceCpuId)) {
            logger.trace(
                    "Got poll update from device [ID={} | IP={}], but this entity belongs to device [ID={}]. Ignoring...",
                    deviceCpuId, deviceIP, this.cpuId.get());
            return; // direct mismatch
        }

        if (!this.localIpAddress.orElse(this.ipAddress).getHostAddress().equalsIgnoreCase(deviceIP)) {
            if (this.matchAnyIncomingDeviceIp) {
                logger.debug(
                        "Got poll update from device {}[IP={}], which is not a match to this device [{}={}]. Ignoring the mismatch due to matchAnyIncomingDeviceIp==true...",
                        deviceCpuId, deviceIP, this.localIpAddress.isPresent() ? "localIP" : "hostname",
                        this.localIpAddress.orElse(this.ipAddress).getHostAddress());
            } else {
                if (this.cpuId.isEmpty() && this.localIpAddress.isEmpty()) {
                    logger.info(
                            "[{}] Got poll update from device {}[IP={}], but was not able to match it to this device with IP={}. Configure {} and/or {} settings to allow detection...",
                            id, deviceCpuId, deviceIP, this.ipAddress.getHostAddress(),
                            ArgoClimaBindingConstants.PARAMETER_DEVICE_CPU_ID,
                            ArgoClimaBindingConstants.PARAMETER_LOCAL_DEVICE_IP);
                } else {
                    logger.trace(
                            "Got poll update from device [ID={} | IP={}], but this entity belongs to device [ID={} | IP={}]. Ignoring...",
                            deviceCpuId, deviceIP, this.cpuId.orElse("???"),
                            this.localIpAddress.orElse(this.ipAddress).getHostAddress());
                }
                return; // IP address heuristic mismatch
            }
        }

        this.onReachableStatusChange.accept(ThingStatus.ONLINE); // Device communicated with us, so we consider it
                                                                 // ONLINE
        try {
            this.deviceStatus.fromDeviceString(hmiStringFromDevice);
        } catch (ArgoApiProtocolViolationException e) {
            throw new ArgoApiCommunicationException("Unrecognized API response",
                    "thing-status.cause.argoclima.exception.unrecognized-response", i18nProvider, e);
        }
        this.onStateUpdate.accept(this.deviceStatus.getCurrentStateMap()); // Update channels from device's state

        var properties = new DeviceStatus.DeviceProperties(OffsetDateTime.now(), deviceUpdate);
        synchronized (this) {
            // update shared properties (which may be updated using direct method as well)
            this.deviceProperties.putAll(properties.asPropertiesRaw(this.timeZoneProvider));
        }
        this.onDevicePropertiesUpdate.accept(getCurrentDeviceProperties());
    }

    /**
     * Get latest "command" string to be sent back to the device in response to its own poll
     * If there are no updates pending, this string will be similar to a canned "nothing to do" response
     *
     * @return Command string to send back to device
     */
    public String getCurrentCommandString() {
        return this.deviceStatus.getDeviceCommandStatus();
    }
}
