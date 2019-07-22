package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

public class BocTopologyActual {

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