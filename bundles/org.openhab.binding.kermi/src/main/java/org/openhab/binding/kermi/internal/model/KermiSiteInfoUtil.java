package org.openhab.binding.kermi.internal.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.kermi.internal.api.DatapointValue;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.VisualizationDatapoint;

public class KermiSiteInfoUtil {

    public static Optional<VisualizationDatapoint> getVisualizationDatapointByWellKnownName(String wellKnownName,
            DeviceInfo deviceInfo) {
        return deviceInfo.getVisualizationDatapoints().stream().filter(vdp -> vdp.getConfig() != null)
                .filter(vdp -> StringUtils.equalsIgnoreCase(wellKnownName, vdp.getConfig().getWellKnownName()))
                .findFirst();
    }

    public static Optional<DatapointValue> getVisualizationDatapointValueByWellKnownName(String wellKnownName,
            DeviceInfo deviceInfo) {
        Optional<VisualizationDatapoint> visualizationDatapointByWellKnownName = getVisualizationDatapointByWellKnownName(
                wellKnownName, deviceInfo);
        return visualizationDatapointByWellKnownName.map(VisualizationDatapoint::getDatapointValue);
    }

}
