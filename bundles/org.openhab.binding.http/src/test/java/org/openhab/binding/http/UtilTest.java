/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.http.internal.Util;

/**
 * The {@link UtilTest} is a test class for URL encoding
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class UtilTest {

    @Test
    public void uriUTF8InHostnameEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foöo.bar/zhu.html?str=zin&tzz=678";
        assertEquals("https://xn--foo-tna.bar/zhu.html?str=zin&tzz=678", Util.uriFromString(s).toString());
    }

    @Test
    public void uriUTF8InPathEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zül.html?str=zin";
        assertEquals("https://foo.bar/z%C3%BCl.html?str=zin", Util.uriFromString(s).toString());
    }

    @Test
    public void uriUTF8InQueryEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zil.html?str=zän";
        assertEquals("https://foo.bar/zil.html?str=z%C3%A4n", Util.uriFromString(s).toString());
    }

    @Test
    public void uriSpaceInPathEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/z l.html?str=zun";
        assertEquals("https://foo.bar/z%20l.html?str=zun", Util.uriFromString(s).toString());
    }

    @Test
    public void uriSpaceInQueryEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zzl.html?str=z n";
        assertEquals("https://foo.bar/zzl.html?str=z%20n", Util.uriFromString(s).toString());
    }

    @Test
    public void uriPlusInQueryEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zzl.html?str=z+n";
        assertEquals("https://foo.bar/zzl.html?str=z%2Bn", Util.uriFromString(s).toString());
    }

    @Test
    public void uriAlreadyPartlyEscapedTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zzl.html?p=field%2Bvalue&foostatus=This is a test String&date=2024-  07-01";
        assertEquals(
                "https://foo.bar/zzl.html?p=field%252Bvalue&foostatus=This%20is%20a%20test%20String&date=2024-%20%2007-01",
                Util.uriFromString(s).toString());
    }

    @Test
    public void wrappedStringFormatDateTest() {
        String formatString = "https://foo.bar/zzl.html?p=field%2Bvalue&date=%1$tY-%1$4tm-%1$td";
        Date testDate = Date.from(Instant.parse("2024-07-01T10:00:00.000Z"));
        assertEquals("https://foo.bar/zzl.html?p=field%2Bvalue&date=2024-  07-01",
                Util.wrappedStringFormat(formatString, testDate));
    }

    @Test
    public void wrappedStringFormatDateAndCommandTest() {
        String formatString = "https://foo.bar/zzl.html?p=field%2Bvalue&foostatus=%2$s&date=%1$tY-%1$4tm-%1$td";
        Date testDate = Date.from(Instant.parse("2024-07-01T10:00:00.000Z"));
        String testCommand = "This is a test String";
        assertEquals("https://foo.bar/zzl.html?p=field%2Bvalue&foostatus=This is a test String&date=2024-  07-01",
                Util.wrappedStringFormat(formatString, testDate, testCommand));
    }
}
