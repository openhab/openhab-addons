/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO.ArmState;
import org.openhab.binding.verisure.internal.dto.VerisureBroadbandConnectionsDTO.Broadband;
import org.openhab.binding.verisure.internal.dto.VerisureClimatesDTO.Climate;
import org.openhab.binding.verisure.internal.dto.VerisureDoorWindowsDTO.DoorWindow;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO.EventLog;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO.CommunicationState;
import org.openhab.binding.verisure.internal.dto.VerisureInstallationsDTO.Account;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO.Mouse;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLocksDTO.Doorlock;
import org.openhab.binding.verisure.internal.dto.VerisureSmartPlugsDTO.Smartplug;
import org.openhab.binding.verisure.internal.dto.VerisureUserPresencesDTO.UserTracking;

import com.google.gson.annotations.SerializedName;

/**
 * A base JSON thing for other Verisure things to inherit.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBaseThingDTO implements VerisureThingDTO {

    protected String deviceId = "";
    protected @Nullable String name;
    protected @Nullable String location;
    protected @Nullable String status;
    protected @Nullable String siteName;
    protected BigDecimal siteId = BigDecimal.ZERO;
    protected Data data = new Data();

    /**
     *
     * @return
     *         The status
     */
    public @Nullable String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     *            The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the deviceId
     */
    @Override
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    @Override
    public void setDeviceId(String deviceId) {
        // Make sure device id is normalized, i.e. replace all non character/digits with empty string
        this.deviceId = deviceId.replaceAll("[^a-zA-Z0-9]+", "");
    }

    /**
     * @return the location
     */
    @Override
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    @Override
    public @Nullable String getSiteName() {
        return siteName;
    }

    @Override
    public void setSiteName(@Nullable String siteName) {
        this.siteName = siteName;
    }

    @Override
    public BigDecimal getSiteId() {
        return siteId;
    }

    @Override
    public void setSiteId(BigDecimal siteId) {
        this.siteId = siteId;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return getThingTypeUID();
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VerisureBaseThingDTO)) {
            return false;
        }

        VerisureBaseThingDTO other = (VerisureBaseThingDTO) obj;
        if (!deviceId.equals(other.deviceId)) {
            return false;
        }

        String localName = name;
        if (localName == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!localName.equals(other.name)) {
            return false;
        }

        String localLocation = location;
        if (localLocation == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!localLocation.equals(other.location)) {
            return false;
        }

        String localStatus = status;
        if (localStatus == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!localStatus.equals(other.status)) {
            return false;
        }

        String localSiteName = siteName;
        if (localSiteName == null) {
            if (other.siteName != null) {
                return false;
            }
        } else if (!localSiteName.equals(other.siteName)) {
            return false;
        }

        if (siteId != other.siteId) {
            return false;
        }

        if (!data.equals(other.data)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("data", data).toString();
    }

    public static class Data {
        private Installation installation = new Installation();
        private Account account = new Account();

        public Account getAccount() {
            return account;
        }

        public Installation getInstallation() {
            return installation;
        }

        public void setInstallation(Installation installation) {
            this.installation = installation;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("installation", installation).append("account", account).toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Data)) {
                return false;
            }
            Data rhs = ((Data) other);
            return new EqualsBuilder().append(installation, rhs.installation).isEquals();
        }
    }

    public static class Installation {

        private ArmState armState = new ArmState();
        private Broadband broadband = new Broadband();
        private EventLog eventLog = new EventLog();
        private List<Climate> climates = new ArrayList<>();
        private List<DoorWindow> doorWindows = new ArrayList<>();
        private List<CommunicationState> communicationState = new ArrayList<>();
        private List<Mouse> mice = new ArrayList<>();
        private List<Doorlock> doorlocks = new ArrayList<>();
        private List<Smartplug> smartplugs = new ArrayList<>();
        private List<UserTracking> userTrackings = new ArrayList<>();

        @SerializedName("__typename")
        private @Nullable String typename;

        public ArmState getArmState() {
            return armState;
        }

        public Broadband getBroadband() {
            return broadband;
        }

        public List<Climate> getClimates() {
            return climates;
        }

        public void setClimates(List<Climate> climates) {
            this.climates = climates;
        }

        public List<DoorWindow> getDoorWindows() {
            return doorWindows;
        }

        public void setDoorWindows(List<DoorWindow> doorWindows) {
            this.doorWindows = doorWindows;
        }

        public EventLog getEventLog() {
            return eventLog;
        }

        public List<CommunicationState> getCommunicationState() {
            return communicationState;
        }

        public List<Mouse> getMice() {
            return mice;
        }

        public void setMice(List<Mouse> mice) {
            this.mice = mice;
        }

        public List<Doorlock> getDoorlocks() {
            return doorlocks;
        }

        public void setDoorlocks(List<Doorlock> doorlocks) {
            this.doorlocks = doorlocks;
        }

        public List<Smartplug> getSmartplugs() {
            return smartplugs;
        }

        public void setSmartplugs(List<Smartplug> smartplugs) {
            this.smartplugs = smartplugs;
        }

        public List<UserTracking> getUserTrackings() {
            return userTrackings;
        }

        public void setUserTrackings(List<UserTracking> userTrackings) {
            this.userTrackings = userTrackings;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("typename", typename).append("armState", armState)
                    .append("broadband", broadband).append("eventLog", eventLog).append("climates", climates)
                    .append("doorWindows", doorWindows).append("communicationState", communicationState)
                    .append("mice", mice).append("doorLocks", doorlocks).append("smartplugs", smartplugs)
                    .append("userTrackings", userTrackings).toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Installation)) {
                return false;
            }
            Installation rhs = ((Installation) other);
            return new EqualsBuilder().append(typename, rhs.typename).append(armState, rhs.armState)
                    .append(broadband, rhs.broadband).append(climates, rhs.climates)
                    .append(doorWindows, rhs.doorWindows).append(communicationState, rhs.communicationState)
                    .append(eventLog, rhs.eventLog).append(mice, rhs.mice).append(doorlocks, rhs.doorlocks)
                    .append(smartplugs, rhs.smartplugs).append(userTrackings, rhs.userTrackings).isEquals();
        }
    }

    public static class Device {

        private @Nullable String deviceLabel;
        private @Nullable String area;
        private Gui gui = new Gui();
        @SerializedName("__typename")
        private @Nullable String typename;

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public @Nullable String getArea() {
            return area;
        }

        public Gui getGui() {
            return gui;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("deviceLabel", deviceLabel).append("area", area).append("gui", gui)
                    .append("typename", typename).toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Device)) {
                return false;
            }
            Device rhs = ((Device) other);
            return new EqualsBuilder().append(gui, rhs.gui).append(area, rhs.area).append(typename, rhs.typename)
                    .append(deviceLabel, rhs.deviceLabel).isEquals();
        }
    }

    public static class Gui {

        private @Nullable String label;
        private @Nullable String support;
        @SerializedName("__typename")
        private @Nullable String typename;

        public @Nullable String getLabel() {
            return label;
        }

        public @Nullable String getSupport() {
            return support;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("label", label).append("support", support)
                    .append("typename", typename).toString();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Gui)) {
                return false;
            }
            Gui rhs = ((Gui) other);
            return new EqualsBuilder().append(typename, rhs.typename).append(support, rhs.support)
                    .append(label, rhs.label).isEquals();
        }
    }
}
