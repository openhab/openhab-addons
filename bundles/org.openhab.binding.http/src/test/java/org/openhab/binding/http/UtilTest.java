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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals("https://xn--foo-tna.bar/zhu.html?str=zin&tzz=678", Util.uriFromString(s).toString());
    }

    @Test
    public void uriUTF8InPathEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zül.html?str=zin";
        Assertions.assertEquals("https://foo.bar/z%C3%BCl.html?str=zin", Util.uriFromString(s).toString());
    }

    @Test
    public void uriUTF8InQueryEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zil.html?str=zän";
        Assertions.assertEquals("https://foo.bar/zil.html?str=z%C3%A4n", Util.uriFromString(s).toString());
    }

    @Test
    public void uriSpaceInPathEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/z l.html?str=zun";
        Assertions.assertEquals("https://foo.bar/z%20l.html?str=zun", Util.uriFromString(s).toString());
    }

    @Test
    public void uriSpaceInQueryEncodeTest() throws MalformedURLException, URISyntaxException {
        String s = "https://foo.bar/zzl.html?str=z n";
        Assertions.assertEquals("https://foo.bar/zzl.html?str=z%20n", Util.uriFromString(s).toString());
    }
}
