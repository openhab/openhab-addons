package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BocTopologyDesired {

    @SerializedName("boc_gateways")
    @Expose
    private List<Object> bocGateways = null;

    /**
     * @return
     */
    public List<Object> getBocGateways() {
        return bocGateways;
    }

    /**
     * @param bocGateways
     */
    public void setBocGateways(List<Object> bocGateways) {
        this.bocGateways = bocGateways;
    }

}