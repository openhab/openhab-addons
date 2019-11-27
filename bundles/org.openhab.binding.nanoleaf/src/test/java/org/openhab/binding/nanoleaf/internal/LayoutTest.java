/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.nanoleaf.internal.model.Layout;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for the Layout
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class LayoutTest {

    private final Gson gson = new Gson();
    String layout1Json="";

    @Before
    public void setup() {
        layout1Json = "{\"numPanels\":14,\"sideLength\":100,\"positionData\":[{\"panelId\":41451,\"x\":350,\"y\":0,\"o\":0,\"shapeType\":3},{\"panelId\":8134,\"x\":350,\"y\":150,\"o\":0,\"shapeType\":2},{\"panelId\":58086,\"x\":200,\"y\":100,\"o\":270,\"shapeType\":2},{\"panelId\":38724,\"x\":300,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":48111,\"x\":200,\"y\":200,\"o\":270,\"shapeType\":2},{\"panelId\":56093,\"x\":100,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":55836,\"x\":0,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":31413,\"x\":100,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":9162,\"x\":300,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":13276,\"x\":400,\"y\":300,\"o\":90,\"shapeType\":2},{\"panelId\":17870,\"x\":400,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":5164,\"x\":500,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":64279,\"x\":600,\"y\":200,\"o\":0,\"shapeType\":2},{\"panelId\":39755,\"x\":500,\"y\":100,\"o\":90,\"shapeType\":2}]}";
    }

    @Test
    public void testTheRightLayoutView() {
        @Nullable Layout layout = gson.fromJson(layout1Json, Layout.class);
        String layoutView = layout.getLayoutView();
        assertThat(layoutView,is(equalTo(
                "            31413                    9162       13276                         \n"+
                         "                                                                              \n" +
                         "55836       56093       48111       38724       17870        5164       64279 \n" +
                         "                                           8134                               \n" +
                         "                        58086                               39755             \n" +
                         "                                                                              \n" +
                         "                                          41451                               \n"
        )));
    }
}
