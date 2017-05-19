package org.openhab.binding.evohome.internal.api;

import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;

public interface EvohomeApiClient {

    boolean login();

    void logout();

    DataModelResponse[] getData();

}