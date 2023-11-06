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
package org.openhab.binding.nanoleaf.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nanoleaf.internal.model.Layout;
import org.openhab.binding.nanoleaf.internal.model.Write;

import com.google.gson.Gson;

/**
 * Test for the Layout
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class LayoutTest {

    private final Gson gson = new Gson();

    private void assertLayoutFromJson(Path jsonFile, Path viewFile) throws IOException {
        String json = Files.readString(jsonFile);
        Layout layout = Objects.requireNonNull(gson.fromJson(json, Layout.class));

        String expectedView = Files.readAllLines(viewFile).stream().collect(Collectors.joining(System.lineSeparator()));

        assertThat(layout.getLayoutView(), is(equalTo(expectedView)));
    }

    @Test
    public void testTheRightLayoutView() throws IOException {
        assertLayoutFromJson(Path.of("src/test/resources/right-layout.json"),
                Path.of("src/test/resources/right-layout-view"));
    }

    @Test
    public void testTheInconsistentLayoutView() throws IOException {
        assertLayoutFromJson(Path.of("src/test/resources/inconsistent-layout.json"),
                Path.of("src/test/resources/inconsistent-layout-view"));
    }

    @Test
    public void testEffects() {
        Write write = new Write();
        write.setCommand("display");
        write.setAnimType("static");
        write.setLoop(false);
        int panelID = 123;
        int quotient = Integer.divideUnsigned(panelID, 256);
        int remainder = Integer.remainderUnsigned(panelID, 256);
        write.setAnimData(String.format("0 1 %d %d %d %d %d 0 0 10", quotient, remainder, 20, 40, 60));
        String content = gson.toJson(write);
        assertThat(content, containsStringIgnoringCase("palette"));
        assertThat(content, is(not(containsStringIgnoringCase("colorType"))));
    }
}
