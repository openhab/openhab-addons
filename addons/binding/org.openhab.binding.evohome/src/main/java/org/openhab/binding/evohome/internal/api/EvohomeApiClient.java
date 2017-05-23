package org.openhab.binding.evohome.internal.api;

public interface EvohomeApiClient {

    boolean login();

    void logout();

    void update();

//    DataModelResponse[] getData();

//    ControlSystem[] getControlSystems();

//    ControlSystem getControlSystem(int id);

}