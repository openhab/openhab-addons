/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The {@link LoginDialogPageTest} contains tests for the {@link LoginDialogPage} record
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class LoginDialogPageTest {
    private static final List<String> HOSTS = List.of("www.amazon.com", "www.amazon.de");
    private static final ServletUri ACCOUNT = new ServletUri("account1", "");
    private static final LoginDialogPage SIGN_IN_PAGE = new LoginDialogPage("www.amazon.com", "/ap/signin");
    private static final LoginDialogPage RETAIL_PAGE = new LoginDialogPage("www.amazon.de", "/ap/cvf/verify");

    private static Stream<Arguments> testFromRequest() {
        return Stream.of( //
                Arguments.of("/FORWARD/www.amazon.com/ap/signin", new LoginDialogPage("www.amazon.com", "/ap/signin")), //
                Arguments.of("/FORWARD/www.amazon.de/ap/cvf/verify?arb=1&openid.mode=checkid_setup",
                        new LoginDialogPage("www.amazon.de", "/ap/cvf/verify?arb=1&openid.mode=checkid_setup")), //
                Arguments.of("/FORWARD/www.amazon.de", new LoginDialogPage("www.amazon.de", "/")), //
                Arguments.of("/FORWARD/www.amazon.com", new LoginDialogPage("www.amazon.com", "/")), //
                Arguments.of("/FORWARD/WWW.AMAZON.COM/ap/signin", null), //
                Arguments.of("/FORWARD/www.amazon.co.uk/ap/signin", null), //
                Arguments.of("/FORWARD/na.account.amazon.com/ap/cvf/request", null), //
                Arguments.of("/FORWARD/evil.example/ap/signin", null), //
                Arguments.of("/FORWARD/www.amazon.com@evil.example/ap/signin", null), //
                Arguments.of("/FORWARD/www.amazon.com:443/ap/signin", null), //
                Arguments.of("/FORWARD/ap/signin", null), //
                Arguments.of("/FORWARD/", null), //
                Arguments.of("/ap/signin", null), //
                Arguments.of("", null));
    }

    @ParameterizedTest
    @MethodSource
    public void testFromRequest(String request, @Nullable LoginDialogPage expected) {
        assertThat(request, LoginDialogPage.fromRequest(request, HOSTS), is(expected));
    }

    private static Stream<Arguments> testFromLocation() {
        return Stream.of( //
                Arguments.of("/ap/forgotpassword/reverification?arb=1", SIGN_IN_PAGE,
                        new LoginDialogPage("www.amazon.com", "/ap/forgotpassword/reverification?arb=1")), //
                Arguments.of("/ap/cvf/approval/poll", RETAIL_PAGE,
                        new LoginDialogPage("www.amazon.de", "/ap/cvf/approval/poll")), //
                Arguments.of("https://www.amazon.com/ap/cvf/transactionapproval?arb=2", RETAIL_PAGE,
                        new LoginDialogPage("www.amazon.com", "/ap/cvf/transactionapproval?arb=2")), //
                Arguments.of("https://www.amazon.de/ap/signin", SIGN_IN_PAGE,
                        new LoginDialogPage("www.amazon.de", "/ap/signin")), //
                Arguments.of("https://www.amazon.de/", SIGN_IN_PAGE, new LoginDialogPage("www.amazon.de", "/")), //
                Arguments.of("https://www.amazon.de", SIGN_IN_PAGE, new LoginDialogPage("www.amazon.de", "/")), //
                Arguments.of("https://www.amazon.de:443/ap/signin", SIGN_IN_PAGE,
                        new LoginDialogPage("www.amazon.de", "/ap/signin")), //
                Arguments.of("HTTPS://WWW.AMAZON.DE/ap/signin", SIGN_IN_PAGE,
                        new LoginDialogPage("www.amazon.de", "/ap/signin")), //
                Arguments.of("https://www.amazon.co.uk/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("https://na.account.amazon.com/ap/cvf/request", SIGN_IN_PAGE, null), //
                Arguments.of("https://www.amazon.de.evil.example/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("https://www.amazon.com@evil.example/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("https://www.amazon.com:8443/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("http://www.amazon.com/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("//evil.example/ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("ap/signin", SIGN_IN_PAGE, null), //
                Arguments.of("https://www.amazon.com/ap/sig nin", SIGN_IN_PAGE, null), //
                Arguments.of("", SIGN_IN_PAGE, null));
    }

    @ParameterizedTest
    @MethodSource
    public void testFromLocation(String location, LoginDialogPage current, @Nullable LoginDialogPage expected) {
        assertThat(location, LoginDialogPage.fromLocation(location, current, HOSTS), is(expected));
    }

    @Test
    public void testEchoedServletPathsAreTranslatedBackToThePageUrl() {
        String plain = "/amazonechocontrol/account1/FORWARD/www.amazon.com/ap/maplanding?openid.oa2.access_token=x";
        assertThat(LoginDialogPage.unrewrite(plain, ACCOUNT),
                is("https://www.amazon.com/ap/maplanding?openid.oa2.access_token=x"));
        String encoded = "/ap/cvf/approval?openid.return_to=%2Famazonechocontrol%2Faccount1%2FFORWARD%2Fwww.amazon.com%2Fap%2Fsignin";
        assertThat(LoginDialogPage.unrewrite(encoded, ACCOUNT),
                is("/ap/cvf/approval?openid.return_to=https%3A%2F%2Fwww.amazon.com%2Fap%2Fsignin"));
        assertThat(LoginDialogPage.unrewrite("https://www.amazon.com/ap/signin", ACCOUNT),
                is("https://www.amazon.com/ap/signin"));
        assertThat(LoginDialogPage.unrewrite("/amazonechocontrol/other/FORWARD/www.amazon.com/x", ACCOUNT),
                is("/amazonechocontrol/other/FORWARD/www.amazon.com/x"));
    }

    @Test
    public void testAddresses() {
        LoginDialogPage page = new LoginDialogPage("www.amazon.de", "/ap/cvf/verify?arb=1");
        assertThat(page.origin(), is("https://www.amazon.de"));
        assertThat(page.url(), is("https://www.amazon.de/ap/cvf/verify?arb=1"));
        assertThat(page.servletPath(ACCOUNT),
                is("/amazonechocontrol/account1/FORWARD/www.amazon.de/ap/cvf/verify?arb=1"));
        assertThat(page.linkBase(ACCOUNT), is("/amazonechocontrol/account1/FORWARD/www.amazon.de/"));
        assertThat(LoginDialogPage.signIn().host(), is("www.amazon.com"));
    }
}
