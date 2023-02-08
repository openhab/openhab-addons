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
package org.openhab.binding.verisure.internal.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.dto.VerisureAlarmsDTO.ArmState;
import org.openhab.binding.verisure.internal.dto.VerisureBroadbandConnectionsDTO.Broadband;
import org.openhab.binding.verisure.internal.dto.VerisureClimatesDTO.Climate;
import org.openhab.binding.verisure.internal.dto.VerisureDoorWindowsDTO.DoorWindow;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO.EventLog;
import org.openhab.binding.verisure.internal.dto.VerisureGatewayDTO.CommunicationState;
import org.openhab.binding.verisure.internal.dto.VerisureMiceDetectionDTO.Mouse;
import org.openhab.binding.verisure.internal.dto.VerisureSmartLocksDTO.Doorlock;
import org.openhab.binding.verisure.internal.dto.VerisureSmartPlugsDTO.Smartplug;
import org.openhab.binding.verisure.internal.dto.VerisureUserPresencesDTO.UserTracking;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * A base JSON thing for other Verisure things to inherit from.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public abstract class VerisureBaseThingDTO implements VerisureThingDTO {

    protected String deviceId = "";
    protected @Nullable String name;
    protected @Nullable String location;
    protected @Nullable String status;
    protected @Nullable String siteName;
    protected BigDecimal siteId = BigDecimal.ZERO;
    protected Data data = new Data();

    public @Nullable String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(String deviceId) {
        // Make sure device id is normalized
        this.deviceId = VerisureThingConfiguration.normalizeDeviceId(deviceId);
    }

    @Override
    public @Nullable String getLocation() {
        return location;
    }

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
    public abstract ThingTypeUID getThingTypeUID();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + data.hashCode();
        result = prime * result + deviceId.hashCode();
        String localLocation = location;
        result = prime * result + ((localLocation == null) ? 0 : localLocation.hashCode());
        String localName = name;
        result = prime * result + ((localName == null) ? 0 : localName.hashCode());
        result = prime * result + siteId.hashCode();
        String localSiteName = siteName;
        result = prime * result + ((localSiteName == null) ? 0 : localSiteName.hashCode());
        String localStatus = status;
        result = prime * result + ((localStatus == null) ? 0 : localStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureBaseThingDTO other = (VerisureBaseThingDTO) obj;
        if (!data.equals(other.data)) {
            return false;
        }
        if (!deviceId.equals(other.deviceId)) {
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
        String localName = name;
        if (localName == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!localName.equals(other.name)) {
            return false;
        }
        if (!siteId.equals(other.siteId)) {
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
        String localStatus = status;
        if (localStatus == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!localStatus.equals(other.status)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VerisureBaseThingDTO [deviceId=" + deviceId + ", name=" + name + ", location=" + location + ", status="
                + status + ", siteName=" + siteName + ", siteId=" + siteId + ", data=" + data + "]";
    }

    public static class Data {
        private Installation installation = new Installation();

        public Installation getInstallation() {
            return installation;
        }

        public void setInstallation(Installation installation) {
            this.installation = installation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + installation.hashCode();
            return result;
        }

        @SuppressWarnings("PMD.SimplifyBooleanReturns")
        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Data other = (Data) obj;
            if (!installation.equals(other.installation)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Data [installation=" + installation + "]";
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

        public @Nullable List<Climate> getClimates() {
            return climates;
        }

        public void setClimates(List<Climate> climates) {
            this.climates = climates;
        }

        public @Nullable List<DoorWindow> getDoorWindows() {
            return doorWindows;
        }

        public void setDoorWindows(List<DoorWindow> doorWindows) {
            this.doorWindows = doorWindows;
        }

        public EventLog getEventLog() {
            return eventLog;
        }

        public @Nullable List<CommunicationState> getCommunicationState() {
            return communicationState;
        }

        public @Nullable List<Mouse> getMice() {
            return mice;
        }

        public void setMice(List<Mouse> mice) {
            this.mice = mice;
        }

        public @Nullable List<Doorlock> getDoorlocks() {
            return doorlocks;
        }

        public void setDoorlocks(List<Doorlock> doorlocks) {
            this.doorlocks = doorlocks;
        }

        public @Nullable List<Smartplug> getSmartplugs() {
            return smartplugs;
        }

        public void setSmartplugs(List<Smartplug> smartplugs) {
            this.smartplugs = smartplugs;
        }

        public @Nullable List<UserTracking> getUserTrackings() {
            return userTrackings;
        }

        public void setUserTrackings(List<UserTracking> userTrackings) {
            this.userTrackings = userTrackings;
        }

        public @Nullable String getTypename() {
            return typename;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + armState.hashCode();
            result = prime * result + broadband.hashCode();
            result = prime * result + climates.hashCode();
            result = prime * result + communicationState.hashCode();
            result = prime * result + doorWindows.hashCode();
            result = prime * result + doorlocks.hashCode();
            result = prime * result + eventLog.hashCode();
            result = prime * result + mice.hashCode();
            result = prime * result + smartplugs.hashCode();
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            result = prime * result + userTrackings.hashCode();
            return result;
        }

        @SuppressWarnings("PMD.SimplifyBooleanReturns")
        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Installation other = (Installation) obj;
            if (!armState.equals(other.armState)) {
                return false;
            }
            if (!broadband.equals(other.broadband)) {
                return false;
            }
            if (!climates.equals(other.climates)) {
                return false;
            }
            if (!communicationState.equals(other.communicationState)) {
                return false;
            }
            if (!doorWindows.equals(other.doorWindows)) {
                return false;
            }
            if (!doorlocks.equals(other.doorlocks)) {
                return false;
            }
            if (!eventLog.equals(other.eventLog)) {
                return false;
            }
            if (!mice.equals(other.mice)) {
                return false;
            }
            if (!smartplugs.equals(other.smartplugs)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            if (!userTrackings.equals(other.userTrackings)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Installation [armState=" + armState + ", broadband=" + broadband + ", eventLog=" + eventLog
                    + ", climates=" + climates + ", doorWindows=" + doorWindows + ", communicationState="
                    + communicationState + ", mice=" + mice + ", doorlocks=" + doorlocks + ", smartplugs=" + smartplugs
                    + ", userTrackings=" + userTrackings + ", typename=" + typename + "]";
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localArea = area;
            result = prime * result + ((localArea == null) ? 0 : localArea.hashCode());
            String localDeviceLabel = deviceLabel;
            result = prime * result + ((localDeviceLabel == null) ? 0 : localDeviceLabel.hashCode());
            result = prime * result + gui.hashCode();
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Device other = (Device) obj;
            String localArea = area;
            if (localArea == null) {
                if (other.area != null) {
                    return false;
                }
            } else if (!localArea.equals(other.area)) {
                return false;
            }
            String localDeviceLabel = deviceLabel;
            if (localDeviceLabel == null) {
                if (other.deviceLabel != null) {
                    return false;
                }
            } else if (!localDeviceLabel.equals(other.deviceLabel)) {
                return false;
            }
            if (!gui.equals(other.gui)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Device [deviceLabel=" + deviceLabel + ", area=" + area + ", gui=" + gui + ", typename=" + typename
                    + "]";
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localLabel = label;
            result = prime * result + ((localLabel == null) ? 0 : localLabel.hashCode());
            String localSupport = support;
            result = prime * result + ((localSupport == null) ? 0 : localSupport.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Gui other = (Gui) obj;
            String localLabel = label;
            if (localLabel == null) {
                if (other.label != null) {
                    return false;
                }
            } else if (!localLabel.equals(other.label)) {
                return false;
            }
            String localSupport = support;
            if (localSupport == null) {
                if (other.support != null) {
                    return false;
                }
            } else if (!localSupport.equals(other.support)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Gui [label=" + label + ", support=" + support + ", typename=" + typename + "]";
        }
    }
}
