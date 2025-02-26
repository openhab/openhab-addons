package org.openhab.binding.bambulab.internal.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class BambuLabApiTest {
    @Test
    @DisplayName("should ")
    void x() throws Exception {
        // given
        var sslContextFactory = new SslContextFactory.Client();
        var client = new HttpClient(sslContextFactory);
        client.start();
        var api = new BambuLabApi(client, "https://api.bambulab.com/v1");

//        api.login("email", "pass");
//        var login = api.verificationCode("email", "pass");
//        System.out.println(login);
        var myPreference = api.queryMyPreference("access token");
        System.out.println(myPreference);
    }
}
