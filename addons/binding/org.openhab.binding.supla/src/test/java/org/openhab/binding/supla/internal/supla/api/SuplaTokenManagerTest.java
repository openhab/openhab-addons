package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.internal.http.CommonHeaders.CONTENT_TYPE_JSON;

@RunWith(MockitoJUnitRunner.class)
public class SuplaTokenManagerTest extends SuplaTest {

    @Mock
    private HttpExecutor httpExecutor;

    @Mock
    private JsonMapper jsonMapper;

    @Test
    public void shouldDoHttpRequestForOathTokenWhenObtainingToken() {

        // given
        final SuplaTokenManager manager = new SuplaTokenManager(jsonMapper, httpExecutor, server);
        final JsonBody body = createJsonBody();
        given(httpExecutor.post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body))
                .willReturn(new Response(200, ""));

        // when
        manager.obtainToken();

        // then
        verify(httpExecutor).post(new Request("/oauth/v2/token", CONTENT_TYPE_JSON), body);
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
