package org.openhab.binding.evohome.internal.api;

import org.openhab.binding.evohome.internal.api.models.DataModelResponse;

public interface EvoHomeApiClient {

    boolean login();

    void logout();

    DataModelResponse[] getData();

}