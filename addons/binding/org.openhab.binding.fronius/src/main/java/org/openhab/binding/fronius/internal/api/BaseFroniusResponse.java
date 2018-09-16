package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * base class for a response-object from the API
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class BaseFroniusResponse {
    @SerializedName("Head")
    private Head head;

    public Head getHead() {
        if (head == null) {
            head = new Head();
        }
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

}
