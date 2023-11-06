/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Base64;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.anel.internal.auth.AnelAuthentication;
import org.openhab.binding.anel.internal.auth.AnelAuthentication.AuthMethod;

/**
 * This class tests {@link AnelAuthentication}.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelAuthenticationTest {

    private static final String STATUS_HUT_V4 = "NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_04.0";
    private static final String STATUS_HUT_V5 = "NET-PwrCtrl:ANEL2          :192.168.0.244:255.255.255.0:192.168.0.1:0.4.163.10.9.107:Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.9*C:NET-PWRCTRL_05.0";
    private static final String STATUS_HOME_V4_6 = "NET-PwrCtrl:NET-CONTROL    :192.168.0.244:255.255.255.0:192.168.0.1:0.5.163.21.4.71:Nr. 1,0:Nr. 2,1:Nr. 3,0:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,0:Nr. 8,0:248:80:NET-PWRCTRL_04.6:H:xor:";
    private static final String STATUS_UDP_SPEC_EXAMPLE_V7 = "NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_06.1:h:p:225.9:0.0004:50.056:0.04:0.00:0.0:1.0000:s:20.61:40.7:7.0:xor";
    private static final String STATUS_PRO_EXAMPLE_V4_5 = "172.25.3.147776172NET-PwrCtrl:DT-BT14-IPL-1 :172.25.3.14:255.255.0.0:172.25.1.1:0.4.163.19.3.129:Nr. 1,0:Nr. 2,0:Nr. 3,0:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,0:Nr. 8,0:0:80:NET-PWRCTRL_04.5:xor:";
    private static final String STATUS_IO_EXAMPLE_V6_5 = "NET-PwrCtrl:NET-CONTROL :192.168.0.244:255.255.255.0:192.168.0.1:0.4.163.20.7.65:Nr.1,0:Nr.2,1:Nr.3,0:Nr.4,0:Nr.5,0:Nr.6,0:Nr.7,0:Nr.8,0:0:80:IO-1,0,1:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:23.1°C:NET-PWRCTRL_06.5:i:n:xor:";
    private static final String STATUS_EXAMPLE_V6_0 = " NET-PwrCtrl:NET-CONTROL :192.168.178.148:255.255.255.0:192.168.178.1:0.4.163.10.9.107:Nr. 1,1:Nr. 2,1:Nr. 3,1:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,1:Nr. 8,1:0:80:IO-1,0,0:IO-2,0,0:IO-3,0,0:IO-4,0,0:IO-5,0,0:IO-6,0,0:IO-7,0,0:IO-8,0,0:27.7°C:NET-PWRCTRL_06.0:o:p:225.9:0.0004:50.056:0.04:0.00:0.0:1.0000";

    @Test
    public void authenticationMethod() {
        assertThat(AuthMethod.of(""), is(AuthMethod.PLAIN));
        assertThat(AuthMethod.of(" \n"), is(AuthMethod.PLAIN));
        assertThat(AuthMethod.of(STATUS_HUT_V4), is(AuthMethod.PLAIN));
        assertThat(AuthMethod.of(STATUS_HUT_V5), is(AuthMethod.PLAIN));
        assertThat(AuthMethod.of(STATUS_HOME_V4_6), is(AuthMethod.XORBASE64));
        assertThat(AuthMethod.of(STATUS_UDP_SPEC_EXAMPLE_V7), is(AuthMethod.XORBASE64));
        assertThat(AuthMethod.of(STATUS_PRO_EXAMPLE_V4_5), is(AuthMethod.XORBASE64));
        assertThat(AuthMethod.of(STATUS_IO_EXAMPLE_V6_5), is(AuthMethod.XORBASE64));
        assertThat(AuthMethod.of(STATUS_EXAMPLE_V6_0), is(AuthMethod.BASE64));
    }

    @Test
    public void encodeUserPasswordPlain() {
        encodeUserPassword(AuthMethod.PLAIN, (u, p) -> u + p);
    }

    @Test
    public void encodeUserPasswordBase64() {
        encodeUserPassword(AuthMethod.BASE64, (u, p) -> base64(u + p));
    }

    @Test
    public void encodeUserPasswordXorBase64() {
        encodeUserPassword(AuthMethod.XORBASE64, (u, p) -> base64(xor(u + p, p)));
    }

    private void encodeUserPassword(AuthMethod authMethod, BiFunction<String, String, String> expectedEncoding) {
        assertThat(AnelAuthentication.getUserPasswordString("admin", "anel", authMethod),
                is(equalTo(expectedEncoding.apply("admin", "anel"))));
        assertThat(AnelAuthentication.getUserPasswordString("", "", authMethod),
                is(equalTo(expectedEncoding.apply("", ""))));
        assertThat(AnelAuthentication.getUserPasswordString(null, "", authMethod),
                is(equalTo(expectedEncoding.apply("", ""))));
        assertThat(AnelAuthentication.getUserPasswordString("", null, authMethod),
                is(equalTo(expectedEncoding.apply("", ""))));
        assertThat(AnelAuthentication.getUserPasswordString(null, null, authMethod),
                is(equalTo(expectedEncoding.apply("", ""))));
    }

    private static String base64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes());
    }

    private String xor(String text, String key) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return sb.toString();
    }
}
