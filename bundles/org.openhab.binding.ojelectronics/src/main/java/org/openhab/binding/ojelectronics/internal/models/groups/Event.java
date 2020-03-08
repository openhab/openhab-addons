
package org.openhab.binding.ojelectronics.internal.models.groups;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("ScheduleType")
    @Expose
    public Integer scheduleType;
    @SerializedName("Clock")
    @Expose
    public String clock;
    @SerializedName("Temperature")
    @Expose
    public Integer temperature;
    @SerializedName("Active")
    @Expose
    public Boolean active;
    @SerializedName("EventIsOnNextDay")
    @Expose
    public Boolean eventIsOnNextDay;

}
