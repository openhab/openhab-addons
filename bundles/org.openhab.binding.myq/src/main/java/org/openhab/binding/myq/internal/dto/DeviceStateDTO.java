/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.myq.internal.dto;

import java.util.List;

/**
 * The {@link DeviceStateDTO} entity from the MyQ API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceStateDTO {

    public Boolean gdoLockConnected;
    public Boolean attachedWorkLightErrorPresent;
    public String doorState;
    public String lampState;
    public String open;
    public String close;
    public String lastUpdate;
    public String passthroughInterval;
    public String doorAjarInterval;
    public String invalidCredentialWindow;
    public String invalidShutoutPeriod;
    public Boolean isUnattendedOpenAllowed;
    public Boolean isUnattendedCloseAllowed;
    public String auxRelayDelay;
    public Boolean useAuxRelay;
    public String auxRelayBehavior;
    public Boolean rexFiresDoor;
    public Boolean commandChannelReportStatus;
    public Boolean controlFromBrowser;
    public Boolean reportForced;
    public Boolean reportAjar;
    public Integer maxInvalidAttempts;
    public Boolean online;
    public String lastStatus;
    public String firmwareVersion;
    public Boolean homekitCapable;
    public Boolean homekitEnabled;
    public String learn;
    public Boolean learnMode;
    public String updatedDate;
    public List<String> physicalDevices = null;
    public Boolean pendingBootloadAbandoned;
}
