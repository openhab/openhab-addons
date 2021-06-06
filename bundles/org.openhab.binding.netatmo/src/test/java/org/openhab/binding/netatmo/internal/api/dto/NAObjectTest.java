package org.openhab.binding.netatmo.internal.api.dto;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.ComponentContext;

public class NAObjectTest {
    private static ApiBridge apiBridge;

    @BeforeAll
    public static void init() {
        ComponentContext componentContext = mock(ComponentContext.class);
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        apiBridge = new ApiBridge(oAuthFactory, httpClientFactory, timeZoneProvider, componentContext);
    }

    @Test
    public void testNAObject() {
        String naObject = "{id:\"5954e7f249c75f97428b7b23\",name:\"Your House\"}";
        try {
            NAObject object = apiBridge.deserialize(NAObject.class, naObject);
            assertEquals(object.getName(), "Your House");
            assertEquals(object.getId(), "5954e7f249c75f97428b7b23");
        } catch (NetatmoException e) {
            e.printStackTrace();
        }
    }
}
