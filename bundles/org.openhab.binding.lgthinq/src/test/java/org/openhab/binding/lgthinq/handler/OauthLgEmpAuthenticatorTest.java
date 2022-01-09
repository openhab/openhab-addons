package org.openhab.binding.lgthinq.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OauthLgEmpAuthenticatorTest {

    @Test
    void getOauth2Sig() {
        String reqUrl = "/emp/oauth2/token/empsession?account_type=LGE&client_id=LGAO221A02&country_code=BR&username=nemer.daud%40gmail.com";
        String timestamp = "Sat, 08 Jan 2022 20:32:24 +0000";
        String secretkey = "fefe3620d403d67b4a2a2384c021a414";
        byte[] result = OauthLgEmpAuthenticator.getOauth2Sig(String.format("%s\n%s",reqUrl,timestamp),secretkey);
        assertEquals(new String(result), "eWMwwFFhGWTUCdg9g3QxAl52qnY=");
    }
}