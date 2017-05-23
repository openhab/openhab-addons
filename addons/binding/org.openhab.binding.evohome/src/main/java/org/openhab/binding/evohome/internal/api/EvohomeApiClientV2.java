/*
package org.openhab.binding.evohome.internal.api;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.models.ControlSystem;
import org.openhab.binding.evohome.internal.api.models.Locations;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.openhab.binding.evohome.internal.api.models.v2.ApiAccess;
import org.openhab.binding.evohome.internal.api.models.v2.ControlSystemAndStatus;
import org.openhab.binding.evohome.internal.api.models.v2.ControlSystemV2;
import org.openhab.binding.evohome.internal.api.models.v2.response.Authentication;
import org.openhab.binding.evohome.internal.api.models.v2.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.LocationsStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystemStatus;
import org.openhab.binding.evohome.internal.api.models.v2.response.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class EvohomeApiClientV2 implements EvohomeApiClient {
    private final Logger logger = LoggerFactory.getLogger(EvohomeApiClientV2.class);

    private EvohomeGatewayConfiguration configuration = null;
    private ApiAccess apiAccess = null;

    private UserAccount     useraccount;
    private Locations       locations;
    private LocationsStatus locationsStatus;

    public EvohomeApiClientV2(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;

        apiAccess = new ApiAccess();
        if (configuration != null) {
            apiAccess.setApplicationId(configuration.applicationId);
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
            url = String.format(url, useraccount.UserId);

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
                url = String.format(url, location.LocationInfo.LocationId);
                LocationStatus status = new LocationStatus();
                status = apiAccess.doAuthenticatedRequest(HttpMethod.GET, url, null, null, status);
                locationsStatus.add(status);
            }
        }

        return locationsStatus;
    }

    @Override
    public boolean login() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);

        boolean success = false;
        try {
            httpClient.start();

            // Building the HTTP request discretely here, as it is the only one with a deviant content type
            Request request = httpClient.newRequest(EvohomeApiConstants.URL_V2_AUTH);
            request.method(HttpMethod.POST);
            request.header("Authorization", "Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=");
            request.header("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

            String data = "Username=" + URLEncoder.encode(configuration.username, "UTF-8") + "&"
                    + "Password=" + URLEncoder.encode(configuration.password, "UTF-8") + "&"
                    + "Host=rs.alarmnet.com%2F&"
                    + "Pragma=no-cache&"
                    + "Cache-Control=no-store+no-cache&"
                    + "scope=EMEA-V1-Basic+EMEA-V1-Anonymous+EMEA-V1-Get-Current-User-Account&"
                    + "grant_type=password&"
                    + "Content-Type=application%2Fx-www-form-urlencoded%3B+charset%3Dutf-8&"
                    + "Connection=Keep-Alive";

            request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");

            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                String reply = response.getContentAsString();
                success = true;
                apiAccess.setAuthentication(new Gson().fromJson(reply, Authentication.class));
            }
        } catch (Exception e) {
            apiAccess.setAuthentication(null);
            logger.error("Authorization failed", e);
        }

        // If the authentication succeeded, gather the basic intel as well
        if (success == true) {
            useraccount = requestUserAccount();
            locations   = requestLocations();
        }

        return success;
    }

    @Override
    public void logout() {
        apiAccess.setAuthentication(null);
        useraccount     = null;
        locations       = null;
        locationsStatus = null;
    }


    @Override
    public void update() {
        locationsStatus = requestLocationsStatus();
    }

    @Override
    public DataModelResponse[] getData() {
        return new DataModelResponse[0];
    }

    @Override
    public ControlSystem[] getControlSystems() {
        //TODO move this to after login to save time
        Map<Integer, ControlSystemAndStatus> map = new HashMap<Integer, ControlSystemAndStatus>();

        // Add metadata to the map
        if (locations!= null) {
            for (Location location : locations) {
                if (location.Gateways != null) {
                    for (Gateway gateway : location.Gateways) {
                        if (gateway.TemperatureControlSystems != null) {
                            for (TemperatureControlSystem system: gateway.TemperatureControlSystems) {
                                ControlSystemAndStatus status = new ControlSystemAndStatus();
                                status.ControlSystem = system;
                                map.put(system.SystemId, status);
                            }
                        }
                    }
                }
            }
        }

        // Add all statuses to the map
        if (locationsStatus != null) {
            for (LocationStatus location : locationsStatus) {
                if (location.Gateways != null) {
                    for (GatewayStatus gateway : location.Gateways) {
                        if (gateway.TemperatureControlSystems != null) {
                            for (TemperatureControlSystemStatus system: gateway.TemperatureControlSystems) {
                                ControlSystemAndStatus status = map.get(system.SystemId);
                                if (status != null) {
                                    status.ControlSystemStatus = system;
                                    map.put(system.SystemId, status);
                                }

                            }
                        }
                    }
                }
            }
        }

        ArrayList<ControlSystem> result = new ArrayList<ControlSystem>();
        for (ControlSystemAndStatus item : map.values()) {
            result.add(new ControlSystemV2(apiAccess, item.ControlSystem, item.ControlSystemStatus));
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

}
*/