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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * WebSocket resource model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record Resource(String service, String endpoint) {
    public static final String IZ = "iz";
    public static final String NI = "ni";
    public static final String CE = "ce";
    public static final String CI = "ci";
    public static final String EI = "ei";
    public static final String RO = "ro";

    public static final Resource RO_VALUES = new Resource(RO, "values");
    public static final Resource RO_DESCRIPTION_CHANGE = new Resource(RO, "descriptionChange");
    public static final Resource RO_ALL_MANDATORY_VALUES = new Resource(RO, "allMandatoryValues");
    public static final Resource RO_ALL_DESCRIPTION_CHANGES = new Resource(RO, "allDescriptionChanges");
    public static final Resource RO_ACTIVE_PROGRAM = new Resource(RO, "activeProgram");
    public static final Resource RO_SELECTED_PROGRAM = new Resource(RO, "selectedProgram");

    public static final Resource EI_INITIAL_VALUES = new Resource(EI, "initialValues");
    public static final Resource EI_DEVICE_READY = new Resource(EI, "deviceReady");

    public static final Resource CI_SERVICES = new Resource(CI, "services");
    public static final Resource CI_REGISTERED_DEVICES = new Resource(CI, "registeredDevices");
    public static final Resource CI_PAIRABLE_DEVICES = new Resource(CI, "pairableDevices");
    public static final Resource CI_DELREGISTRATION = new Resource(CI, "delregistration");
    public static final Resource CI_NETWORK_DETAILS = new Resource(CI, "networkDetails");
    public static final Resource CI_NETWORK_DETAILS2 = new Resource(CI, "networkDetails2");
    public static final Resource CI_WIFI_NETWORKS = new Resource(CI, "wifiNetworks");
    public static final Resource CI_WIFI_SETTING = new Resource(CI, "wifiSetting");
    public static final Resource CI_WIFI_SETTING2 = new Resource(CI, "wifiSetting2");
    public static final Resource CI_TZ_INFO = new Resource(CI, "tzInfo");
    public static final Resource CI_INFO = new Resource(CI, "info");
    public static final Resource CI_AUTHENTICATION = new Resource(CI, "authentication");
    public static final Resource CI_REGISTER = new Resource(CI, "register");
    public static final Resource CI_DEREGISTER = new Resource(CI, "deregister");

    public static final Resource CE_SERVER_DEVICE_TYPE = new Resource(CE, "serverDeviceType");
    public static final Resource CE_SERVER_CREDENTIAL = new Resource(CE, "serverCredential");
    public static final Resource CE_CLIENT_CREDENTIAL = new Resource(CE, "clientCredential");
    public static final Resource CE_HUB_INFORMATION = new Resource(CE, "hubInformation");
    public static final Resource CE_HUB_CONNECTED = new Resource(CE, "hubConnected");
    public static final Resource CE_STATUS = new Resource(CE, "status");

    public static final Resource NI_CONFIG = new Resource(NI, "config");
    public static final Resource NI_INFO = new Resource(NI, "info");

    public static final Resource IZ_SERVICES = new Resource(IZ, "services");
    public static final Resource IZ_INFO = new Resource(IZ, "info");
}
