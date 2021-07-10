package org.openhab.binding.carnet.internal.api.brand;

import static org.openhab.binding.carnet.internal.api.carnet.CarNetApiConstants.CNAPI_VW_TOKEN_URL;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApi;

public class BrandSkodaEnyaq extends CarNetApi {
    public BrandSkodaEnyaq(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        properties.brand = "skoda"; // it's "VW", not "Skoda"
        properties.xcountry = "CZ";
        properties.clientId = "f9a2359a-b776-46d9-bd0c-db1904343117@apps_vw-dilab_com";
        properties.xClientId = "28cd30c6-dee7-4529-a0e6-b1e07ff90b79";
        properties.xrequest = "cz.skodaauto.connect";
        properties.redirect_uri = "skodaconnect://oidc.login/";
        properties.responseType = "code token id_token";
        properties.authScope = "openid profile mbb";
        properties.tokenUrl = CNAPI_VW_TOKEN_URL;
        properties.tokenRefreshUrl = "https://tokenrefreshservice.apps.emea.vwapps.io";
        properties.xappVersion = "3.2.6";
        properties.xappName = "cz.skodaauto.connect";
        return properties;
    }
}
