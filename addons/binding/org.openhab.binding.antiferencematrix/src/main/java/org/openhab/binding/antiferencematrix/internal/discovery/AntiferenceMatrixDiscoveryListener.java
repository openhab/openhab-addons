package org.openhab.binding.antiferencematrix.internal.discovery;

import org.openhab.binding.antiferencematrix.internal.model.PortList;

public interface AntiferenceMatrixDiscoveryListener {

    void update(PortList portList);

}
