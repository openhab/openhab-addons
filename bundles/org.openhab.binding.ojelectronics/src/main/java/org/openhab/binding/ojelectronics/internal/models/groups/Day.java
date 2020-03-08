
package org.openhab.binding.ojelectronics.internal.models.groups;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Day {

    @SerializedName("WeekDayGrpNo")
    @Expose
    public Integer weekDayGrpNo;
    @SerializedName("Events")
    @Expose
    public List<Event> events = new ArrayList<Event>();

}
