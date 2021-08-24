package org.openhab.binding.airquality.internal.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airquality.internal.AirQualityException;
import org.openhab.binding.airquality.internal.api.dto.AirQualityData;
import org.openhab.binding.airquality.internal.api.dto.AirQualityResponse;
import org.openhab.binding.airquality.internal.api.dto.AirQualityResponse.ResponseStatus;
import org.openhab.binding.airquality.internal.config.AirQualityBindingConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@NonNullByDefault
public class ApiBridge {
    private static final Gson GSON = new Gson();
    private static final String URL = "http://api.waqi.info/feed/%query%/?token=%apiKey%";
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);
    private final AirQualityBindingConfiguration config;

    public ApiBridge(AirQualityBindingConfiguration configuration) {
        this.config = configuration;
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the aqicn.org service
     * @throws AirQualityException
     */
    private String buildRequestURL(Integer stationId, String location) throws AirQualityException {
        config.checkValid();
        String geoStr = stationId == 0
                ? String.format("geo:%s",
                        location.replace(" ", "").replace(",", ";").replace("\"", "").replace("'", "").trim())
                : String.format("@%d", stationId);

        return URL.replace("%apiKey%", config.apiKey).replace("%query%", geoStr);
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @return an air quality data object mapping the JSON response
     * @throws AirQualityException
     */
    public AirQualityData getData(Integer stationId, String location, int retryCounter) throws AirQualityException {
        String urlStr = buildRequestURL(stationId, location);
        logger.debug("URL = {}", urlStr);

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, null, null, null, REQUEST_TIMEOUT_MS);
            logger.debug("aqiResponse = {}", response);
            AirQualityResponse result = GSON.fromJson(response, AirQualityResponse.class);
            if (result != null && result.getStatus() == ResponseStatus.OK) {
                return result.getData();
            } else {
                if (retryCounter == 0) {
                    logger.warn("Error in aqicn.org, retrying once");
                    return getData(stationId, location, retryCounter + 1);
                }
                throw new AirQualityException("Error in aqicn.org response: Missing data sub-object");
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new AirQualityException("Communication error", e);
        }
    }
}
