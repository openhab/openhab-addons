package org.openhab.binding.kermi.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.Datapoint;
import org.openhab.binding.kermi.internal.api.DatapointReadValuesResponse;
import org.openhab.binding.kermi.internal.api.DatapointValue;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.slf4j.LoggerFactory;

import tech.units.indriya.unit.Units;

public class KermiSiteInfo {

    private Map<String, DeviceInfo> deviceIdToDeviceInfo;

    private Map<String, Datapoint> dataPointConfigIdToDatapoint;
    private BidiMap<String, String> wellKnownNameToDatapointConfigId;

    // regularly updated
    private Map<String, State> wellKnownNameToCurrentState;

    // The WellKnownName datapoint values that are
    // bound to a channel, and thus included in the
    // refresh call
    private Set<String[]> wellKnownNameRefreshBinding;

    public KermiSiteInfo() {
        deviceIdToDeviceInfo = Collections.synchronizedMap(new HashMap<>());
        wellKnownNameToDatapointConfigId = new DualHashBidiMap<>();
        wellKnownNameRefreshBinding = Collections.synchronizedSet(new HashSet<>());
        wellKnownNameToCurrentState = Collections.synchronizedMap(new HashMap<>());
        dataPointConfigIdToDatapoint = Collections.synchronizedMap(new HashMap<>());
    }

    public DeviceInfo getHeatpumpManagerDeviceInfo() {
        return deviceIdToDeviceInfo.get(KermiBindingConstants.DEVICE_ID_HEATPUMP_MANAGER);
    }

    public void updateSiteInfo(Map<String, DeviceInfo> deviceInfo) {
        clearSiteInfo();
        deviceIdToDeviceInfo.putAll(deviceInfo);
    }

    public void clearSiteInfo() {
        deviceIdToDeviceInfo.clear();
    }

    public boolean isInitialized() {
        return !deviceIdToDeviceInfo.isEmpty();
    }

    /**
     * Initialize the information about this site. Iterate through all DeviceInfo elements to collect all
     * VisualizationDataPoints
     *
     * @param _deviceInfo
     * @throws KermiCommunicationException
     */
    public void initializeSiteInfo(KermiHttpUtil httpUtil, Map<@NonNull String, @NonNull DeviceInfo> deviceInfo)
            throws KermiCommunicationException {
        clearSiteInfo();

        deviceIdToDeviceInfo.putAll(deviceInfo);

        Collection<DeviceInfo> devices = deviceIdToDeviceInfo.values();
        for (DeviceInfo device : devices) {
            List<Datapoint> deviceDatapoints = KermiSiteInfoUtil.collectDeviceDatapoints(httpUtil, device);
            for (Datapoint datapoint : deviceDatapoints) {
                String wellKnownName = datapoint.getConfig().getWellKnownName();
                if (wellKnownName != null) {
                    String datapointConfigId = datapoint.getConfig().getDatapointConfigId();
                    wellKnownNameToDatapointConfigId.put(wellKnownName, datapointConfigId);
                    dataPointConfigIdToDatapoint.put(datapointConfigId, datapoint);
                }
            }
        }

    }

    public void putRefreshBinding(@NonNull String wellKnownId, @NonNull String deviceId) {
        String datapointConfigId = wellKnownNameToDatapointConfigId.get(wellKnownId);
        wellKnownNameRefreshBinding.add(new String[] { deviceId, datapointConfigId });
    }

    public void updateStateValues(@NonNull KermiHttpUtil httpUtil) throws KermiCommunicationException {
        if (wellKnownNameRefreshBinding.isEmpty()) {
            return;
        }

        DatapointReadValuesResponse datapointReadValues = httpUtil.getDatapointReadValues(wellKnownNameRefreshBinding);
        List<DatapointValue> datapointValues = datapointReadValues.getResponseData();
        datapointValues.forEach(dpv -> {
            // parallel stream?
            String wellKnownName = wellKnownNameToDatapointConfigId.getKey(dpv.getDatapointConfigId());
            State currentState = convertDatapointValueToState(dpv);
            wellKnownNameToCurrentState.put(wellKnownName, currentState);

        });
    }

    public State convertDatapointValueToState(DatapointValue datapointValue) {
        // getDatapoint as resolved in #initializeSiteInfo
        Datapoint datapoint = dataPointConfigIdToDatapoint.get(datapointValue.getDatapointConfigId());
        if (datapoint == null || datapoint.getConfig() == null) {
            // TODO log?
            return null;
        }

        int datapointType = datapoint.getConfig().getDatapointType();
        if (1 == datapointType) {
            return new QuantityType<>((double) datapointValue.getValue(), Units.CELSIUS);
        } else if (2 == datapointType) {
            // boolean
        }
        LoggerFactory.getLogger(getClass()).warn("Unknown datapointType {} in {}", datapointType,
                datapoint.getConfig().getWellKnownName());
        return null;
    }

    public State getStateByWellKnownName(String wellKnownName, String deviceId) {
        return wellKnownNameToCurrentState.get(wellKnownName);
    }

    public DeviceInfo getDeviceInfoByAddress(String busAddress) {
        return deviceIdToDeviceInfo.values().stream().filter(deviceInfo -> busAddress.equals(deviceInfo.getAddress()))
                .findFirst().orElse(null);
    }

}
