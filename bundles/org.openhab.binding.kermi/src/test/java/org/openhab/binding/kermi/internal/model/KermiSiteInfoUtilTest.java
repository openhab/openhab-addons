package org.openhab.binding.kermi.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;
import org.openhab.binding.kermi.internal.api.GetDeviceResponse;

import com.google.gson.Gson;

public class KermiSiteInfoUtilTest {

    @Test
    public void getVisualizationDatapointByWellKnownName() throws IOException {

        GetDeviceResponse getDeviceResponse = null;
        try (InputStream resourceAsStream = getClass()
                .getResourceAsStream("/getDeviceInfoByDeviceIdDrinkingWaterHeating.json")) {
            getDeviceResponse = new Gson().fromJson(new InputStreamReader(resourceAsStream), GetDeviceResponse.class);
        }

        // VisualizationDatapoint visualizationDatapoint = KermiSiteInfoUtil.getVisualizationDatapointByWellKnownName(
        // KermiBindingConstants.WELL_KNOWN_NAME_BS_TWE_TEMP_ACT, getDeviceResponse.getResponseData()).get();
        // assertEquals("06e61673-abc2-4671-9e5a-960809d1f326",
        // visualizationDatapoint.getConfig().getDatapointConfigId());
    }

}
