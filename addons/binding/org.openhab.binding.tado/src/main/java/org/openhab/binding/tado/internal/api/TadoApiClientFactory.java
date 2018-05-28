/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.api;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.openhab.binding.tado.internal.api.auth.OAuth;
import org.openhab.binding.tado.internal.api.auth.OAuthFlow;
import org.openhab.binding.tado.internal.api.client.HomeApi;
import org.openhab.binding.tado.internal.api.converter.OverlayTerminationConditionTemplateConverter;
import org.openhab.binding.tado.internal.api.converter.TerminationConditionConverter;
import org.openhab.binding.tado.internal.api.converter.ZoneCapabilitiesConverter;
import org.openhab.binding.tado.internal.api.converter.ZoneSettingConverter;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Factory to create and configure {@link TadoApiClient} instances.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoApiClientFactory {
    private static final String OAUTH_SCOPE = "home.user";
    private static final String OAUTH_CLIENT_ID = "public-api-preview";
    private static final String OAUTH_CLIENT_SECRET = "4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw";
    private static final String OAUTH_TOKEN_URL = "https://auth.tado.com/oauth/token";
    private static final String API_URL = "https://my.tado.com/api/v2/";
    private static final String USER_AGENT = "openhab/tado/1.0";

    public TadoApiClient create(String username, String password) {
        HomeApi PublicApi = createHomeApi(username, password);
        return new TadoApiClient(PublicApi);
    }

    private HomeApi createHomeApi(String username, String password) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(GenericZoneSetting.class, new ZoneSettingConverter())
                .registerTypeAdapter(OverlayTerminationCondition.class, new TerminationConditionConverter())
                .registerTypeAdapter(OverlayTerminationConditionTemplate.class,
                        new OverlayTerminationConditionTemplateConverter())
                .registerTypeAdapter(GenericZoneCapabilities.class, new ZoneCapabilitiesConverter()).create();

        Builder adapterBuilder = new Retrofit.Builder().baseUrl(API_URL)
                // .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson));

        ApiClient apiClient = new ApiClient();
        apiClient.setAdapterBuilder(adapterBuilder);
        apiClient.getOkBuilder().addInterceptor(new UserAgentInterceptor(USER_AGENT));

        TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest.tokenLocation(OAUTH_TOKEN_URL)
                .setScope(OAUTH_SCOPE).setClientId(OAUTH_CLIENT_ID).setClientSecret(OAUTH_CLIENT_SECRET)
                .setUsername(username).setPassword(password);
        OkHttpClient authHttpClient = new OkHttpClient.Builder().addInterceptor(new UserAgentInterceptor(USER_AGENT))
                .build();

        OAuth oauth = new OAuth(authHttpClient, tokenRequestBuilder);
        oauth.setFlow(OAuthFlow.password);

        apiClient.addAuthorization("oauth", oauth);
        return apiClient.createService(HomeApi.class);
    }
}
