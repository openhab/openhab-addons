
package org.openhab.binding.ojelectronics.internal.models.groups;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupContentResponseModel {

    @SerializedName("GroupContents")
    @Expose
    public List<GroupContent> groupContents = new ArrayList<GroupContent>();
    @SerializedName("ErrorCode")
    @Expose
    public Integer errorCode;

}
