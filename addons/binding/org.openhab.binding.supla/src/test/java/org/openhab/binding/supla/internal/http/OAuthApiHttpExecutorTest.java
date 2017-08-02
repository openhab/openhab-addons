package org.openhab.binding.supla.internal.http;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaToken;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.internal.http.CommonHeaders.authorizationHeader;

@RunWith(MockitoJUnitRunner.class)
public class OAuthApiHttpExecutorTest extends SuplaTest {
    @InjectMocks private OAuthApiHttpExecutor oAuthApiHttpExecutor;
    @Mock private HttpExecutor httpExecutor;
    @Mock private TokenManager tokenManager;

    @Test
    public void shouldPassGetRequestToHttpExecutor() {

        // given
        final Header h1 = new Header("k", "v");
        final Header h2 = new Header("k1", "v1");
        final Request request = new Request("/path", ImmutableList.of(h1, h2));
        final SuplaToken token = randomToken();
        given(tokenManager.obtainToken()).willReturn(token);

        // when
        oAuthApiHttpExecutor.get(request);

        // then
        verify(httpExecutor).get(new Request("/api/path", ImmutableList.of(h1, h2, authorizationHeader(token))));
    }

    @Test
    public void shouldPassGetRequestToHttpExecutorWithoutHeaders() {

        // given
        final Request request = new Request("/path");
        final SuplaToken token = randomToken();
        given(tokenManager.obtainToken()).willReturn(token);

        // when
        oAuthApiHttpExecutor.get(request);

        // then
        verify(httpExecutor).get(new Request("/api/path", ImmutableList.of(authorizationHeader(token))));
    }

    private SuplaToken randomToken() {
        return new SuplaToken("acc", 100, "token type", "sc", "re");
    }
}
