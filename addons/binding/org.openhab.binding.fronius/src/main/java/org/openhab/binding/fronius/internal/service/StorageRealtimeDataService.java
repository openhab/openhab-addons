package org.openhab.binding.fronius.internal.service;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.fronius.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.configuration.ServiceConfiguration;
import org.openhab.binding.fronius.internal.model.StorageRealtimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StorageRealtimeDataService {

    private final Logger logger = LoggerFactory.getLogger(StorageRealtimeDataService.class);
    private final String API = FroniusBindingConstants.STORAGE_REALTIME_DATA_URL;
    private final ServiceConfiguration configuration;
    private final JsonParser parser = new JsonParser();

    private String url;

    public StorageRealtimeDataService(ServiceConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    public StorageRealtimeData getData() {
        final String url = getUrl();
        final HttpClient httpClient = new HttpClient();
        JsonObject json = new JsonObject();
        try {
            httpClient.start();
            ContentResponse response = httpClient.GET(url);
            logger.debug("Response data {}", response.toString());
            httpClient.stop();
            final String jsonString = response.getContentAsString();
            json = parser.parse(jsonString).getAsJsonObject();
        } catch (Exception e) {
            logger.warn("Error during HTTP request: {}", e);
        }
        return new StorageRealtimeData(json);
    }

    private String getUrl() {
        if (null == url) {
            StringBuilder sb = new StringBuilder();
            sb.append("http://");
            sb.append(configuration.getHostname());
            sb.append(API);
            sb.append("?Scope=Device");
            sb.append("&DeviceId=");
            sb.append(configuration.getDevice());
            url = sb.toString();
        }
        logger.debug("StorageRealtimeData URL: {}", url);
        return url;
    }
}
