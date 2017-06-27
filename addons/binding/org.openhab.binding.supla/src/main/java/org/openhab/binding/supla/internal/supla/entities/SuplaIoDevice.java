package org.openhab.binding.supla.internal.supla.entities;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Set;
import java.util.SortedSet;

import static java.util.Collections.emptySet;

public final class SuplaIoDevice {
    private final long id;
    private final long locationId;
    private final boolean enabled;
    private final String name;
    private final String comment;
    private final SuplaDate registration;
    private final SuplaDate lastConnected;
    private final String guid;
    private final String softwareVersion;
    private final int protocolVersion;
    private final SortedSet<SuplaChannel> channels;

    public SuplaIoDevice(long id, long locationId, boolean enabled, String name, String comment, SuplaDate registration,
                         SuplaDate lastConnected, String guid, String softwareVersion, int protocolVersion, Set<SuplaChannel> channels) {
        this.id = id;
        this.locationId = locationId;
        this.enabled = enabled;
        this.name = name;
        this.comment = comment;
        this.registration = registration;
        this.lastConnected = lastConnected;
        this.guid = guid;
        this.softwareVersion = softwareVersion;
        this.protocolVersion = protocolVersion;
        if (channels == null) {
            this.channels = ImmutableSortedSet.of();
        } else {
            this.channels = ImmutableSortedSet.copyOf(channels);
        }
    }

    public long getId() {
        return id;
    }

    public long getLocationId() {
        return locationId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public SuplaDate getRegistration() {
        return registration;
    }

    public SuplaDate getLastConnected() {
        return lastConnected;
    }

    public String getGuid() {
        return guid;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Set<SuplaChannel> getChannels() {
        return channels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaIoDevice)) return false;

        SuplaIoDevice suplaIoDevice = (SuplaIoDevice) o;

        if (id != suplaIoDevice.id) return false;
        if (locationId != suplaIoDevice.locationId) return false;
        if (enabled != suplaIoDevice.enabled) return false;
        if (protocolVersion != suplaIoDevice.protocolVersion) return false;
        if (name != null ? !name.equals(suplaIoDevice.name) : suplaIoDevice.name != null) return false;
        if (comment != null ? !comment.equals(suplaIoDevice.comment) : suplaIoDevice.comment != null) return false;
        if (registration != null ? !registration.equals(suplaIoDevice.registration) : suplaIoDevice.registration != null)
            return false;
        if (lastConnected != null ? !lastConnected.equals(suplaIoDevice.lastConnected) : suplaIoDevice.lastConnected != null)
            return false;
        if (guid != null ? !guid.equals(suplaIoDevice.guid) : suplaIoDevice.guid != null) return false;
        if (softwareVersion != null ? !softwareVersion.equals(suplaIoDevice.softwareVersion) : suplaIoDevice.softwareVersion != null)
            return false;
        return channels != null ? channels.equals(suplaIoDevice.channels) : suplaIoDevice.channels == null;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "SuplaIoDevice{" +
                "id=" + id +
                ", locationId=" + locationId +
                ", enabled=" + enabled +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", registration=" + registration +
                ", lastConnected=" + lastConnected +
                ", guid='" + guid + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", protocolVersion=" + protocolVersion +
                ", channels=" + Joiner.on(", ").join(channels) +
                '}';
    }
}
