package org.openhab.binding.evohome.internal.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.models.ControlSystem;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.openhab.binding.evohome.internal.api.models.v2.ControlSystemAndStatus;
import org.openhab.binding.evohome.internal.api.models.v2.ControlSystemV2;
import org.openhab.binding.evohome.internal.api.models.v2.response.Authentication;
import org.openhab.binding.evohome.internal.api.models.v2.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationsStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvohomeApiClientV2 implements EvohomeApiClient {
    private final Logger logger = LoggerFactory.getLogger(EvohomeApiClientV2.class);

    private final SslContextFactory sslContextFactory = new SslContextFactory();
    private final HttpClient httpClient = new HttpClient(sslContextFactory);

    private EvohomeGatewayConfiguration          configuration      = null;
    private ApiAccess                            apiAccess          = null;
    private UserAccount                          useraccount        = null;
    private Locations                            locations          = null;
    private LocationsStatus                      locationsStatus    = null;
    private Map<Integer, ControlSystemAndStatus> controlSystemCache = null;

    public EvohomeApiClientV2(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Creating Evohome API client.");

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Could not start http client.", e);
        }

        apiAccess = new ApiAccess(httpClient);
        if (configuration != null) {
            apiAccess.setApplicationId(configuration.applicationId);
        }
    }

    public void close() {
        apiAccess.setAuthentication(null);
        useraccount     = null;
        locations       = null;
        locationsStatus = null;

        if (httpClient.isStarted()) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                logger.error("Could not stop http client.", e);

            }
        }
    }

    private UserAccount requestUserAccount() {
        String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_ACCOUNT;

        UserAccount userAccount =  new UserAccount();
        userAccount = apiAccess.doAuthenticatedRequest(HttpMethod.GET, url, null, null, userAccount);

        return userAccount;
    }

    private Locations requestLocations() {
        Locations locations = null;
        if (useraccount != null) {
            String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_LOCATIONS;
            url = String.format(url, useraccount.userId);

            locations = new Locations();
            locations = apiAccess.doAuthenticatedRequest(HttpMethod.GET, url, null, null, locations);
        }

        return locations;
    }

    private LocationsStatus requestLocationsStatus() {
        LocationsStatus locationsStatus = new LocationsStatus();

        if (locations != null) {
            for(Location location : locations) {
                String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_STATUS;
                url = String.format(url, location.locationInfo.locationId);
                LocationStatus status = new LocationStatus();
                status = apiAccess.doAuthenticatedRequest(HttpMethod.GET, url, null, null, status);
                locationsStatus.add(status);
            }
        }

        return locationsStatus;
    }

    @Override
    public boolean login() {
        Authentication authentication = new Authentication();

        try {
            String data = "Username=" + URLEncoder.encode(configuration.username, "UTF-8") + "&"
                        + "Password=" + URLEncoder.encode(configuration.password, "UTF-8") + "&"
                        + "Host=rs.alarmnet.com%2F&"
                        + "Pragma=no-cache&"
                        + "Cache-Control=no-store+no-cache&"
                        + "scope=EMEA-V1-Basic+EMEA-V1-Anonymous+EMEA-V1-Get-Current-User-Account&"
                        + "grant_type=password&"
                        + "Content-Type=application%2Fx-www-form-urlencoded%3B+charset%3Dutf-8&"
                        + "Connection=Keep-Alive";

        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=");
        headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

        authentication  = apiAccess.doRequest(
                HttpMethod.POST, EvohomeApiConstants.URL_V2_AUTH,
                headers, data, "application/x-www-form-urlencoded", authentication);

        apiAccess.setAuthentication(authentication);

        } catch (UnsupportedEncodingException e) {
            logger.error("Credential conversion failed", e);
        }

        boolean success = (authentication != null);

        // If the authentication succeeded, gather the basic intel as well
        if (success == true) {
            useraccount = requestUserAccount();
            locations   = requestLocations();
            controlSystemCache = populateCache();
        } else {
            apiAccess.setAuthentication(null);
            logger.error("Authorization failed");
        }

        return success;
    }

    private Map<Integer, ControlSystemAndStatus> populateCache() throws NullPointerException {
        Map<Integer, ControlSystemAndStatus> map = new HashMap<Integer, ControlSystemAndStatus>();

        // Add metadata to the map
        for (Location location : locations) {
            for (Gateway gateway : location.gateways) {
                for (TemperatureControlSystem system: gateway.temperatureControlSystems) {
                    ControlSystemAndStatus status = new ControlSystemAndStatus();
                    status.controlSystem = system;
                    map.put(system.systemId, status);
                }
            }
        }

        return map;
    }

    private void updateCache() throws NullPointerException {
        // Add all statuses to the map
        for (LocationStatus location : locationsStatus) {
            for (GatewayStatus gateway : location.gateways) {
                for (TemperatureControlSystemStatus system: gateway.temperatureControlSystems) {
                    ControlSystemAndStatus status = controlSystemCache.get(system.systemId);
                    if (status != null) {
                        status.controlSystemStatus = system;
                        controlSystemCache.put(system.systemId, status);
                    }
                }
            }
        }
    }

    @Override
    public void logout() {
        close();
    }

    @Override
    public void update() {
        locationsStatus = requestLocationsStatus();
        updateCache();
    }

    @Override
    public DataModelResponse[] getData() {
        return new DataModelResponse[0];
    }

    @Override
    public ControlSystem[] getControlSystems() {
        ArrayList<ControlSystem> result = new ArrayList<ControlSystem>();
        for (ControlSystemAndStatus item : controlSystemCache.values()) {
            result.add(new ControlSystemV2(apiAccess, item.controlSystem, item.controlSystemStatus));
        }

        return result.toArray(new ControlSystem[result.size()]);
    }

    @Override
    public ControlSystem getControlSystem(int id) {
        for (ControlSystem controlSystem : getControlSystems()) {
            if (controlSystem.getId() == id) {
                return controlSystem;
            }
        }

        return null;
    }

    @Override
    public void refresh() {
        //TODO add check in token expired, use refresh token to update access token
        login(); // crude workaround
    }
}
