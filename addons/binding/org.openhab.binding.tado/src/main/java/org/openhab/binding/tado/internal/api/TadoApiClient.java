/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.api;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.terminationConditionTemplateToTerminationCondition;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.tado.internal.api.client.HomeApi;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.HomeInfo;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.User;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.openhab.binding.tado.internal.api.model.ZoneState;

import retrofit2.Response;

/**
 * API client to access tado's cloud API.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoApiClient {
    private HomeApi api;

    public TadoApiClient(HomeApi api) {
        this.api = api;
    }

    public HomeInfo getHomeDetails(long homeId) throws IOException, TadoClientException {
        Response<HomeInfo> response = api.showHome(homeId).execute();
        HomeInfo homeDetails = response.body();
        handleError(response, "Error getting details of home " + homeId);
        return homeDetails;
    }

    public User getUserDetails() throws IOException, TadoClientException {
        Response<User> response = api.showUser().execute();
        User user = response.body();
        handleError(response, "Error getting user details");
        return user;
    }

    public List<Zone> listZones(long homeId) throws IOException, TadoClientException {
        Response<List<Zone>> response = api.listZones(homeId).execute();
        List<Zone> zones = response.body();
        handleError(response, "Error listing zones of home " + homeId);
        return zones;
    }

    public Zone getZoneDetails(long homeId, long zoneId) throws IOException, TadoClientException {
        Response<Zone> response = api.showZoneDetails(homeId, zoneId).execute();
        Zone zoneDetails = response.body();
        handleError(response, "Error getting details of zone " + zoneId + " of home " + homeId);
        return zoneDetails;
    }

    public ZoneState getZoneState(long homeId, long zoneId) throws IOException, TadoClientException {
        Response<ZoneState> response = api.showZoneState(homeId, zoneId).execute();
        ZoneState zoneState = response.body();
        handleError(response, "Error getting state of zone " + zoneId + " of home " + homeId);
        return zoneState;
    }

    public GenericZoneCapabilities getZoneCapabilities(long homeId, long zoneId)
            throws IOException, TadoClientException {
        Response<GenericZoneCapabilities> response = api.showZoneCapabilities(homeId, zoneId).execute();
        GenericZoneCapabilities capabilities = response.body();
        handleError(response, "Error getting capabilities of zone " + zoneId + " of home " + homeId);
        return capabilities;
    }

    public OverlayTerminationCondition getDefaultTerminationCondition(long homeId, long zoneId)
            throws IOException, TadoClientException {
        Response<OverlayTemplate> response = api.showZoneDefaultOverlay(homeId, zoneId).execute();
        OverlayTemplate overlayTemplate = response.body();
        handleError(response, "Error getting overlay template of zone " + zoneId + " of home " + homeId);
        return terminationConditionTemplateToTerminationCondition(overlayTemplate.getTerminationCondition());
    }

    public Overlay setOverlay(long homeId, long zoneId, Overlay overlay) throws IOException, TadoClientException {
        Response<Overlay> response = api.updateZoneOverlay(homeId, zoneId, overlay).execute();
        Overlay newOverlay = response.body();
        handleError(response, "Error changing HVAC settings of zone " + zoneId + " of home " + homeId);
        return newOverlay;
    }

    public void removeOverlay(long homeId, long zoneId) throws IOException, TadoClientException {
        Response<Void> response = api.deleteZoneOverlay(homeId, zoneId).execute();
        handleError(response, "Error removing overlay of zone " + zoneId + " of home " + homeId);
        response.raw().close();
    }

    public List<MobileDevice> listMobileDevices(long homeId) throws IOException, TadoClientException {
        Response<List<MobileDevice>> response = api.listMobileDevices(homeId).execute();
        List<MobileDevice> devices = response.body();
        handleError(response, "Error getting list of mobile devices of home " + homeId);
        return devices;
    }

    public MobileDevice getMobileDeviceDetails(long homeId, int mobileDeviceId)
            throws IOException, TadoClientException {
        Response<List<MobileDevice>> mobileDeviceResponse = api.listMobileDevices(homeId).execute();
        List<MobileDevice> mobileDeviceDetails = mobileDeviceResponse.body();
        handleError(mobileDeviceResponse,
                "Error getting details of mobile of device " + mobileDeviceId + " of home " + homeId);
        return mobileDeviceDetails.stream().filter(m -> m.getId() == mobileDeviceId).findFirst().orElse(null);
    }

    private void handleError(Response<?> response, String errorMessage) throws IOException, TadoClientException {
        if (!response.isSuccessful()) {
            String errorText = response.errorBody().string();
            throw new TadoClientException(errorMessage + ": " + errorText);
        }
    }
}
