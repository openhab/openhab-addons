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
package org.openhab.binding.kaleidescape.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.kaleidescape.internal.communication.KaleidescapeFormatter;

/**
 * Unit tests for {@link KaleidescapeFormatter}.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeFormatterTest {
    @Test
    public void formatEncodedActorNameList() {
        assertThat(KaleidescapeFormatter.formatString(
                "Craig T. Nelson\\rHolly Hunter\\rJason Lee\\rSamuel L. Jackson\\rBrad Bird\\rSarah Vowell\\rSpencer Fox\\rWallace Shawn\\rElizabeth Pe\\d241a"),
                is("Craig T. Nelson, Holly Hunter, Jason Lee, Samuel L. Jackson, Brad Bird, Sarah Vowell, Spencer Fox, Wallace Shawn, Elizabeth Peña"));
    }

    @Test
    public void formatEncodedAlbumName() {
        assertThat(KaleidescapeFormatter.formatString(
                "Bart\\d243k\\: Concerto for Orchestra; Music for Strings, Percussion and Celesta; Hungarian Sketches"),
                is("Bartók: Concerto for Orchestra; Music for Strings, Percussion and Celesta; Hungarian Sketches"));
    }

    @Test
    public void formatEncodedUrl() {
        assertThat(
                KaleidescapeFormatter
                        .formatString("http\\:\\/\\/10.100.12.194\\/panelcoverart\\/b9bca9a6f224fb54\\/4254312.jpg"),
                is("http://10.100.12.194/panelcoverart/b9bca9a6f224fb54/4254312.jpg"));
    }

    @Test
    public void formatEncodedProverb() {
        assertThat(KaleidescapeFormatter.formatString(
                "\\d196ll w\\d246rk \\d226nd \\d241o pl\\d226\\d255 m\\d228k\\d200s J\\d195\\d231k \\d229 d\\d249ll b\\d244\\d253"),
                is("Äll wörk ând ño plâÿ mäkÈs JÃçk å dùll bôý"));
    }

    @Test
    public void formatEncodedLatin1ExtentedAlphabetToChars() {
        assertThat(KaleidescapeFormatter.formatString(
                "\\d161\\d162\\d163\\d164\\d165\\d166\\d167\\d168\\d169\\d170\\d171\\d172\\d174\\d175\\d176\\d177\\d178\\d179\\d180\\d181\\d182\\d183\\d184\\d185\\d186\\d187\\d188\\d189\\d190\\d191\\d192\\d193\\d194\\d195\\d196\\d197\\d198\\d199\\d200\\d201\\d202\\d203\\d204\\d205\\d206\\d207\\d208\\d209\\d210\\d211\\d212\\d213\\d214\\d215\\d216\\d217\\d218\\d219\\d220\\d221\\d222\\d223\\d224\\d225\\d226\\d227\\d228\\d229\\d230\\d231\\d232\\d233\\d234\\d235\\d236\\d237\\d238\\d239\\d240\\d241\\d242\\d243\\d244\\d245\\d246\\d247\\d248\\d249\\d250\\d251\\d252\\d253\\d254\\d255"),
                is("¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"));
    }
}
