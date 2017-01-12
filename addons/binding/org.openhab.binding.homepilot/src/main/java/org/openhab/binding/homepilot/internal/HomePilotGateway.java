package org.openhab.binding.homepilot.internal;

import java.util.List;

public interface HomePilotGateway {

    public List<HomePilotDevice> loadAllDevices();

    public HomePilotDevice loadDevice(String deviceId);

    public boolean handleSetPosition(String deviceId, int position);

    public boolean handleSetOnOff(String deviceId, boolean on);

    public boolean handleStop(String deviceId);

    public String getId();

    public void initialize();

    public void cancelLoadAllDevices();
}
