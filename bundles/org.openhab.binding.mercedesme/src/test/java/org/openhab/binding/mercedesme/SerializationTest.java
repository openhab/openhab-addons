/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.VehicleHandler;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;

/**
 * The {@link SerializationTest} is testing token seriial- & deserialization
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SerializationTest {

    @Test
    public void test() {
        AccessTokenResponse atr = new AccessTokenResponse();
        atr.setAccessToken("abc");
        atr.setExpiresIn(123);
        atr.setRefreshToken("xyz");
        atr.setTokenType("Bearer");

        System.out.println(atr);
        String ser = Utils.toString(atr);
        System.out.println(ser);
        Object o = Utils.fromString(ser);
        System.out.println(o);
        assertTrue(atr.equals(o));
    }

    @Test
    public void testCopy() {
        String test = "rO0ABXNyADdvcmcub3BlbmhhYi5jb3JlLmF1dGguY2xpZW50Lm9hdXRoMi5BY2Nlc3NUb2tlblJlc3BvbnNlQyEPNSC58y4CAAdKAAlleHBpcmVzSW5MAAthY2Nlc3NUb2tlbnQAEkxqYXZhL2xhbmcvU3RyaW5nO0wACWNyZWF0ZWRPbnQAGUxqYXZhL3RpbWUvTG9jYWxEYXRlVGltZTtMAAxyZWZyZXNoVG9rZW5xAH4AAUwABXNjb3BlcQB+AAFMAAVzdGF0ZXEAfgABTAAJdG9rZW5UeXBlcQB+AAF4cAAAAAAAABwfdAVaZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1qVTJRMEpETFVoVE5URXlJaXdpWTNSNUlqb2lTbGRVSWl3aWEybGtJam9pUTBsQlRWTlpUU0lzSW5CcExtRjBiU0k2SW1oNE0yVWlmUS4uZVZOM2ZtYlFNeEdNUHZ6WFJIeTZEQS5ZaGU3RDNhYk1SMTlqb21LV1VLdG5wZ25DdmZEVVlSMEU0OXE0TGNKSGk2Nkc5YUpPbUtlLTEzbktfLUY2bW11Qk9qVXNtNkY4SU1UTF9LRHlKQ0VoY0R1VzlYdDdUYzFQLS0tcFNBUkdWNGxRRjg2N1JtaV9LbjJrdEVYYkwtYlRHYTB6czRiRlhiTXczdjcyRU5SMzBNVVNZbW1NQ0JLSG8zMm5uT0JneEpyVXJTZnBVRzVnNWEwNDIzeVozTkhIdURqTi1YZTVnU0lwTEVjZ2ZnbXA1Y1M0SHpoRlA0WFIzd2V5aE43WVNlRTZ1YURBc3BFRTZ5dXJ3NklsSEZqNGxQN3VoWGhoVE0xc2w0TEVPT09ydENGdFBmRWVfX0I1OUpTUUFPbl8wWkc3RlliN2JyRGRhbDdJbHBvRDFsakNVRlNBVFBPRUJnS3dMbmotNE9QUGhKQ2RlWV9vMEY1bE1nekRtdG9xb09QdUk3bzZyeTF5blY4SlpQS0h1UmFfV05kWmZva2lXNzdJM2tXUm9qcTJUWVBBY0luV3ppZGRCZ0JrWTFEMURQUzRUbXhBRHJQYjhfOWNUVHBzTF9vanFIa252Wk5YYW92eVhEN29INGY5dWktTk5pT0xfTURQUlZTOEhGUjFFcmdfYzd5bHpRc281aG9BZmJ1cUpUMzZyeFdwdVQ5R0kzVDEwOFJRMWkydnI1NTNXS3ZzaU8zWnRvUlpYQ2JncWx3SDcybWFyNWd0NW9jMjI3QklMMlRaTmFiV1h5ZURVbS1DWm1ucXdMaHN5b2RfSjBsUkdsLUVnMk0wR0ZlY2VKM0tISlR1U0x4N0ZMTDhwZ25PQ3lIYjE3N0dOUTY4VXlsSFdkeUdJYVlBWllfMGdseGxNWExiaUxaaTYyeUJ6UHdvNWJvaF9KSEFUQ1N4aXdDM3pMZ3pyX3pGX1ZBc3ZEcGpLR0hfbzNKWWpKTlpQNFlFTzdCeWdXQVB4U1NoSHBSeEJoSWNKR2NUbGc0QzRaZzN1Q3dvRmZHY1N6M0RtZndpY2V0LXF5RzRmd3U4X3Y0T1lWOWtmNlF1VVV6dWh5cmoyaG42b2xOQi1waTZTVGp2T2ZPaUFNaVpGV2pVVk1Pc0RXc2JBU1ZfMDVqYllEV285b19yTHJLajI5N3VmS0dpUm5EeVNfMV9PLUk3T3ViMFZmc2QyOFMybTlRaktfWTVqOVdVSjRfQ2N0VDJKVEh1dUhTdmFNS3oxZVAxNS1lNmZJSVM1Rl9aY3QwbVE4Y19xOWpkM3F6THNFcUE1OTBmUEVxVXM0RTgzTG5lVHhOTjNZNHMyemJjOTJGVWRqMDdZaTR6b0VKZjN1VWx3WWE1Um9IaFg3dVB3aEZwMjJNWVBvVkJ6d0ZzQmZSNkx1d2JiTENLRFhzcklheEszNnFNc0FpWWkzMm0zWGJ6dFFfNkpnZWtqa1VKdFhMZnRFMjluZ2dkTTdIblhtWkM3a1d2aTY2OW5lemU1Q2hyNERIOTRReEFrOHBEZnZKazJkN0JuYWY2Ql9SeFV6SmRDUDNhY0FRV2JTZ2RYUXk5TklWX1JGeHRwckJoVnlQcEw4LmE0SFh3NDZ0OWl4dEdhYm1falZkQXd2X3BsTnJOVV91MU1sQl9IVHpQRG9zcgANamF2YS50aW1lLlNlcpVdhLobIkiyDAAAeHB3DgUAAAfmBhEACQQbewTYeHQAKldRMFJpVVlwRFFadE1seEV1VDltUm5qcDVMNXpzdWltUUtCcVdsd0Z2b3BwdAAGQmVhcmVy";
        Object o;
        o = Utils.fromString(test);
        System.out.println("Test 2 " + o);
    }

    @Test
    public void testReplacement() {
        String url = String.format(Constants.STATUS_URL, "W1N");
        System.out.println(url);
    }

    @Test
    public void testVehicleHandler() {
        VehicleHandler vh = new VehicleHandler(mock(Thing.class), mock(HttpClientFactory.class), Constants.BEV);
    }
}
