package org.openhab.binding.myuplink.internal.discovery;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MyUplinkDiscoveryServiceTest {

    private static final String EMPTY_RESPONSE_STRING = "{\"page\":1,\"itemsPerPage\":100,\"numItems\":0,\"systems\":[]}";
    private static JsonObject EMPTY_RESPONSE;

    private static final String TEST_RESPONSE_STRING = """
            {
                \"page\": 0,
                \"itemsPerPage\": 0,
                \"numItems\": 0,
                \"systems\": [
                    {
                        \"systemId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",
                        \"name\": \"string\",
                        \"securityLevel\": \"admin\",
                        \"hasAlarm\": true,
                        \"country\": \"string\",
                        \"devices\": [
                            {
                                \"id\": \"Device ID\",
                                \"connectionState\": \"Disconnected\",
                                \"currentFwVersion\": \"string\",
                                \"product\": {
                                    \"serialNumber\": \"4711\",
                                    \"name\": \"My Device\"
                                }
                            }
                        ]
                    }
                ]
            }
            """;
    private static JsonObject TEST_RESPONSE;

    @BeforeAll
    public static void prepareTestData() {

        EMPTY_RESPONSE = JsonParser.parseString(EMPTY_RESPONSE_STRING).getAsJsonObject();
        TEST_RESPONSE = JsonParser.parseString(TEST_RESPONSE_STRING).getAsJsonObject();
    }

    @Test
    public void testProcessMyUplinkDiscoveryResult() {
        MyUplinkDiscoveryService discoveryService = new MyUplinkDiscoveryService();

        discoveryService.processMyUplinkDiscoveryResult(new CommunicationStatus(), TEST_RESPONSE);
    }
}
