package org.openhab.binding.nest.internal.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * Default properties shared across all nest devices.
 *
 * @author David Bennett
 */
public class BaseNestDevice {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getNameLong() {
        return nameLong;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getStructureId() {
        return structureId;
    }

    public String getWhereId() {
        return whereId;
    }

    public String getWhere_name() {
        return where_name;
    }

    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("name")
    private String name;
    @SerializedName("name_long")
    private String nameLong;
    @SerializedName("last_connection")
    private Date lastConnection;
    @SerializedName("is_online")
    private boolean isOnline;
    @SerializedName("software_version")
    private String softwareVersion;
    @SerializedName("structure_id")
    private String structureId;
    @SerializedName("where_id")
    private String whereId;
    @SerializedName("where_name")
    private String where_name;
}
