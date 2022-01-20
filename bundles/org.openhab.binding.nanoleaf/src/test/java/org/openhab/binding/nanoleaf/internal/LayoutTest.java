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
package org.openhab.binding.nanoleaf.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
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
    String layout1Json = "";
    String layoutInconsistentPanelNoJson = "";

    @BeforeEach
    public void setup() {
        layout1Json = "{\n" + "      \"numPanels\": 14,\n" + "      \"sideLength\": 0,\n"
                + "      \"positionData\": [\n" + "        {\n" + "          \"panelId\": 60147,\n"
                + "          \"x\": 199,\n" + "          \"y\": 99,\n" + "          \"o\": 0,\n"
                + "          \"shapeType\": 3\n" + "        },\n" + "        {\n" + "          \"panelId\": 61141,\n"
                + "          \"x\": 200,\n" + "          \"y\": 199,\n" + "          \"o\": 90,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 42064,\n"
                + "          \"x\": 100,\n" + "          \"y\": 200,\n" + "          \"o\": 180,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 186,\n"
                + "          \"x\": 0,\n" + "          \"y\": 200,\n" + "          \"o\": 180,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 19209,\n"
                + "          \"x\": 0,\n" + "          \"y\": 100,\n" + "          \"o\": 270,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 36604,\n"
                + "          \"x\": 300,\n" + "          \"y\": 99,\n" + "          \"o\": 0,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 37121,\n"
                + "          \"x\": 400,\n" + "          \"y\": 99,\n" + "          \"o\": 270,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 45187,\n"
                + "          \"x\": 400,\n" + "          \"y\": 199,\n" + "          \"o\": 270,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 33626,\n"
                + "          \"x\": 500,\n" + "          \"y\": 199,\n" + "          \"o\": 270,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 10523,\n"
                + "          \"x\": 600,\n" + "          \"y\": 199,\n" + "          \"o\": 270,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 54086,\n"
                + "          \"x\": 599,\n" + "          \"y\": 99,\n" + "          \"o\": 540,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 3512,\n"
                + "          \"x\": 699,\n" + "          \"y\": 99,\n" + "          \"o\": 540,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 16398,\n"
                + "          \"x\": 799,\n" + "          \"y\": 99,\n" + "          \"o\": 540,\n"
                + "          \"shapeType\": 2\n" + "        },\n" + "        {\n" + "          \"panelId\": 39163,\n"
                + "          \"x\": 800,\n" + "          \"y\": 199,\n" + "          \"o\": 630,\n"
                + "          \"shapeType\": 2\n" + "        }\n" + "      ]\n" + "    }";
        layoutInconsistentPanelNoJson = "{\"numPanels\":15,\"sideLength\":100,\"positionData\":[{\"panelId\":41451,\"x\":350,\"y\":0,\"o\":0,\"shapeType\":3},{\"panelId\":8134,\"x\":350,\"y\":150,\"o\":0,\"shapeType\":2},{\"panelId\":58086,\"x\":200,\"y\":100,\"o\":270,\"shapeType\":2},{\"panelId\":38724,\"x\":300,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":48111,\"x\":200,\"y\":200,\"o\":270,\"shapeType\":2},{\"panelId\":56093,\"x\":100,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":55836,\"x\":0,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":31413,\"x\":100,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":9162,\"x\":300,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":13276,\"x\":400,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":17870,\"x\":400,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":5164,\"x\":500,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":64279,\"x\":600,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":39755,\"x\":500,\"y\":100,\"o\":90,\"shapeType\":2}]}";
    }

    @Test
    public void testTheRightLayoutView() {
        @Nullable
        Layout layout = gson.fromJson(layout1Json, Layout.class);
        if (layout == null) {
            layout = new Layout();
        }
        String layoutView = layout.getLayoutView();
        assertThat(layoutView, is(equalTo(
                "  186       42064       61141                   45187       33626       10523                   39163 \n"
                        + "                                                                                                      \n"
                        + "19209                   60147       36604       37121                   54086        3512       16398 \n")));
    }

    @Test
    public void testTheInconsistentLayoutView() {
        @Nullable
        Layout layout = gson.fromJson(layoutInconsistentPanelNoJson, Layout.class);
        if (layout == null) {
            layout = new Layout();
        }
        String layoutView = layout.getLayoutView();
        assertThat(layoutView,
                is(equalTo("            31413                    9162       13276                         \n"
                        + "                                                                              \n"
                        + "55836       56093       48111       38724       17870        5164       64279 \n"
                        + "                                           8134                               \n"
                        + "                        58086                               39755             \n"
                        + "                                                                              \n"
                        + "                                          41451                               \n")));
    }

    @Test
    public void testEffects() {
        Write write = new Write();
        write.setCommand("display");
        write.setAnimType("static");
        write.setLoop(false);
        int panelID = 123;
        int quotient = Integer.divideUnsigned(Integer.valueOf(panelID), 256);
        int remainder = Integer.remainderUnsigned(Integer.valueOf(panelID), 256);
        write.setAnimData(String.format("0 1 %d %d %d %d %d 0 0 10", quotient, remainder, 20, 40, 60));
        String content = gson.toJson(write);
        assertThat(content, containsStringIgnoringCase("palette"));
        assertThat(content, is(not(containsStringIgnoringCase("colorType"))));
    }
}
