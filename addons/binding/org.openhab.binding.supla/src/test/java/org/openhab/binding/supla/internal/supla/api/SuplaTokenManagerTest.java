package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.JsonBody;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaToken;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.internal.http.CommonHeaders.CONTENT_TYPE_JSON;

@RunWith(MockitoJUnitRunner.class)
public class SuplaTokenManagerTest extends SuplaTest {

    private SuplaTokenManager manager;

    @Mock
    private HttpExecutor httpExecutor;

    @Mock
    private JsonMapper jsonMapper;

    @Before
    public void init() {
        manager = new SuplaTokenManager(jsonMapper, httpExecutor, server);
    }

    @Test
    public void shouldDoHttpRequestForOathTokenWhenObtainingToken() {

        // given
        final JsonBody body = createJsonBody();
        given(httpExecutor.post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body))
                .willReturn(new Response(200, ""));

        // when
        manager.obtainToken();

        // then
        verify(httpExecutor).post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body);
    }

    @Test
    public void shouldMapResponseWithJsonMapper() {

        // given
        final JsonBody body = createJsonBody();
        final String response = "resp";
        given(httpExecutor.post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body))
                .willReturn(new Response(200, response));

        // when
        manager.obtainToken();

        // then
        verify(jsonMapper).to(SuplaToken.class, response);
    }


    private JsonBody createJsonBody() {
        return new JsonBody(ImmutableMap.<String, String>builder()
                .put("client_id", server.getClientId())
                .put("client_secret", server.getSecretAsString()).put("grant_type", "password")
                .put("username", server.getUsername())
                .put("password", server.getPasswordAsString())
                .build(),
                jsonMapper);
    }

}
