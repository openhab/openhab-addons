
package org.openhab.binding.ojelectronics.internal.models.groups;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Schedule {

    @SerializedName("Days")
    @Expose
    public List<Day> days = new ArrayList<Day>();
    @SerializedName("ModifiedDueToVerification")
    @Expose
    public Boolean modifiedDueToVerification;

}
