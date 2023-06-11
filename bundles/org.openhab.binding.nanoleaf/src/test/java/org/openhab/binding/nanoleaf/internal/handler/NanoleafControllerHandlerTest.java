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
package org.openhab.binding.nanoleaf.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.binding.nanoleaf.internal.model.State;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.Gson;

/**
 * Test for the Layout
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class NanoleafControllerHandlerTest {

    private final Gson gson = new Gson();

    private String controllerInfoJSON = "";

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testStateOn() {
        controllerInfoJSON = "{\r\n  \"name\":\"Nanoleaf Light Panels 12:34:56\",\r\n  \"serialNo\":\"S19082ABCDE\",\r\n  \"manufacturer\":\"Nanoleaf\",\r\n  \"firmwareVersion\":\"3.3.3\",\r\n  \"hardwareVersion\":\"1.6-2\",\r\n  \"model\":\"NL22\",\r\n  \"cloudHash\":{\r\n\r\n  },\r\n  \"discovery\":{\r\n\r\n  },\r\n  \"effects\":{\r\n    \"effectsList\":[\r\n      \"Color Burst\",\r\n      \"Fireworks\",\r\n      \"Flames\",\r\n      \"Forest\",\r\n      \"Inner Peace\",\r\n      \"Lightning\",\r\n      \"Northern Lights\",\r\n      \"Pulse Pop Beats\",\r\n      \"Vibrant Sunrise\"\r\n    ],\r\n    \"select\":\"Flames\"\r\n  },\r\n  \"firmwareUpgrade\":{\r\n\r\n  },\r\n  \"panelLayout\":{\r\n    \"globalOrientation\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n    \"layout\":{\r\n      \"numPanels\":9,\r\n      \"sideLength\":150,\r\n      \"positionData\":[\r\n        {\r\n          \"panelId\":1,\r\n          \"x\":299,\r\n          \"y\":0,\r\n          \"o\":300,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":2,\r\n          \"x\":299,\r\n          \"y\":86,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":3,\r\n          \"x\":224,\r\n          \"y\":129,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":4,\r\n          \"x\":224,\r\n          \"y\":216,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":5,\r\n          \"x\":149,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":6,\r\n          \"x\":74,\r\n          \"y\":216,\r\n          \"o\":240,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":7,\r\n          \"x\":0,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":8,\r\n          \"x\":149,\r\n          \"y\":346,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":9,\r\n          \"x\":374,\r\n          \"y\":129,\r\n          \"o\":180,\r\n          \"shapeType\":0\r\n        }\r\n      ]\r\n    }\r\n  },\r\n  \"rhythm\":{\r\n    \"auxAvailable\":false,\r\n    \"firmwareVersion\":\"2.4.3\",\r\n    \"hardwareVersion\":\"2.0\",\r\n    \"rhythmActive\":false,\r\n    \"rhythmConnected\":true,\r\n    \"rhythmId\":10,\r\n    \"rhythmMode\":0,\r\n    \"rhythmPos\":{\r\n      \"x\":449.99521692839559,\r\n      \"y\":86.60030339609753,\r\n      \"o\":0.0\r\n    }\r\n  },\r\n  \"schedules\":{\r\n\r\n  },\r\n  \"state\":{\r\n    \"brightness\":{\r\n      \"value\":29,\r\n      \"max\":100,\r\n      \"min\":0\r\n    },\r\n    \"colorMode\":\"effect\",\r\n    \"ct\":{\r\n      \"value\":3000,\r\n      \"max\":6500,\r\n      \"min\":1200\r\n    },\r\n    \"hue\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n    \"on\":{\r\n      \"value\":true\r\n    },\r\n    \"sat\":{\r\n      \"value\":0,\r\n      \"max\":100,\r\n      \"min\":0\r\n    }\r\n  }\r\n}";

        @Nullable
        ControllerInfo controllerInfo = gson.fromJson(controllerInfoJSON, ControllerInfo.class);
        assertThat(controllerInfo, is(notNullValue()));

        if (controllerInfo != null) {
            final State state = controllerInfo.getState();
            assertThat(state, is(notNullValue()));
            assertThat(state.getOnOff(), is(OnOffType.ON));
        }
    }

    @Test
    public void testStateOff() {
        controllerInfoJSON = "{\r\n  \"name\":\"Nanoleaf Light Panels 12:34:56\",\r\n  \"serialNo\":\"S19082ABCDE\",\r\n  \"manufacturer\":\"Nanoleaf\",\r\n  \"firmwareVersion\":\"3.3.3\",\r\n  \"hardwareVersion\":\"1.6-2\",\r\n  \"model\":\"NL22\",\r\n  \"cloudHash\":{\r\n\r\n  },\r\n  \"discovery\":{\r\n\r\n  },\r\n  \"effects\":{\r\n    \"effectsList\":[\r\n      \"Color Burst\",\r\n      \"Fireworks\",\r\n      \"Flames\",\r\n      \"Forest\",\r\n      \"Inner Peace\",\r\n      \"Lightning\",\r\n      \"Northern Lights\",\r\n      \"Pulse Pop Beats\",\r\n      \"Vibrant Sunrise\"\r\n    ],\r\n    \"select\":\"Flames\"\r\n  },\r\n  \"firmwareUpgrade\":{\r\n\r\n  },\r\n  \"panelLayout\":{\r\n    \"globalOrientation\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n    \"layout\":{\r\n      \"numPanels\":9,\r\n      \"sideLength\":150,\r\n      \"positionData\":[\r\n        {\r\n          \"panelId\":1,\r\n          \"x\":299,\r\n          \"y\":0,\r\n          \"o\":300,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":2,\r\n          \"x\":299,\r\n          \"y\":86,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":3,\r\n          \"x\":224,\r\n          \"y\":129,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":4,\r\n          \"x\":224,\r\n          \"y\":216,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":5,\r\n          \"x\":149,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":6,\r\n          \"x\":74,\r\n          \"y\":216,\r\n          \"o\":240,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":7,\r\n          \"x\":0,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":8,\r\n          \"x\":149,\r\n          \"y\":346,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":9,\r\n          \"x\":374,\r\n          \"y\":129,\r\n          \"o\":180,\r\n          \"shapeType\":0\r\n        }\r\n      ]\r\n    }\r\n  },\r\n  \"rhythm\":{\r\n    \"auxAvailable\":false,\r\n    \"firmwareVersion\":\"2.4.3\",\r\n    \"hardwareVersion\":\"2.0\",\r\n    \"rhythmActive\":false,\r\n    \"rhythmConnected\":true,\r\n    \"rhythmId\":10,\r\n    \"rhythmMode\":0,\r\n    \"rhythmPos\":{\r\n      \"x\":449.99521692839559,\r\n      \"y\":86.60030339609753,\r\n      \"o\":0.0\r\n    }\r\n  },\r\n  \"schedules\":{\r\n\r\n  },\r\n  \"state\":{\r\n    \"brightness\":{\r\n      \"value\":29,\r\n      \"max\":100,\r\n      \"min\":0\r\n    },\r\n    \"colorMode\":\"effect\",\r\n    \"ct\":{\r\n      \"value\":3000,\r\n      \"max\":6500,\r\n      \"min\":1200\r\n    },\r\n    \"hue\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n                                                     \"sat\":{\r\n      \"value\":0,\r\n      \"max\":100,\r\n      \"min\":0\r\n    }\r\n  }\r\n}";

        ControllerInfo controllerInfo = gson.fromJson(controllerInfoJSON, ControllerInfo.class);
        assertThat(controllerInfo, is(notNullValue()));

        if (controllerInfo != null) {
            final State state = controllerInfo.getState();
            assertThat(state, is(notNullValue()));
            assertThat(state.getOnOff(), is(OnOffType.OFF));
        }
    }

    @Test
    public void testStateOnMissing() {
        controllerInfoJSON = "{\r\n  \"name\":\"Nanoleaf Light Panels 12:34:56\",\r\n  \"serialNo\":\"S19082ABCDE\",\r\n  \"manufacturer\":\"Nanoleaf\",\r\n  \"firmwareVersion\":\"3.3.3\",\r\n  \"hardwareVersion\":\"1.6-2\",\r\n  \"model\":\"NL22\",\r\n  \"cloudHash\":{\r\n\r\n  },\r\n  \"discovery\":{\r\n\r\n  },\r\n  \"effects\":{\r\n    \"effectsList\":[\r\n      \"Color Burst\",\r\n      \"Fireworks\",\r\n      \"Flames\",\r\n      \"Forest\",\r\n      \"Inner Peace\",\r\n      \"Lightning\",\r\n      \"Northern Lights\",\r\n      \"Pulse Pop Beats\",\r\n      \"Vibrant Sunrise\"\r\n    ],\r\n    \"select\":\"Flames\"\r\n  },\r\n  \"firmwareUpgrade\":{\r\n\r\n  },\r\n  \"panelLayout\":{\r\n    \"globalOrientation\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n    \"layout\":{\r\n      \"numPanels\":9,\r\n      \"sideLength\":150,\r\n      \"positionData\":[\r\n        {\r\n          \"panelId\":1,\r\n          \"x\":299,\r\n          \"y\":0,\r\n          \"o\":300,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":2,\r\n          \"x\":299,\r\n          \"y\":86,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":3,\r\n          \"x\":224,\r\n          \"y\":129,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":4,\r\n          \"x\":224,\r\n          \"y\":216,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":5,\r\n          \"x\":149,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":6,\r\n          \"x\":74,\r\n          \"y\":216,\r\n          \"o\":240,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":7,\r\n          \"x\":0,\r\n          \"y\":259,\r\n          \"o\":60,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":8,\r\n          \"x\":149,\r\n          \"y\":346,\r\n          \"o\":120,\r\n          \"shapeType\":0\r\n        },\r\n        {\r\n          \"panelId\":9,\r\n          \"x\":374,\r\n          \"y\":129,\r\n          \"o\":180,\r\n          \"shapeType\":0\r\n        }\r\n      ]\r\n    }\r\n  },\r\n  \"rhythm\":{\r\n    \"auxAvailable\":false,\r\n    \"firmwareVersion\":\"2.4.3\",\r\n    \"hardwareVersion\":\"2.0\",\r\n    \"rhythmActive\":false,\r\n    \"rhythmConnected\":true,\r\n    \"rhythmId\":10,\r\n    \"rhythmMode\":0,\r\n    \"rhythmPos\":{\r\n      \"x\":449.99521692839559,\r\n      \"y\":86.60030339609753,\r\n      \"o\":0.0\r\n    }\r\n  },\r\n  \"schedules\":{\r\n\r\n  },\r\n  \"state\":{\r\n    \"brightness\":{\r\n      \"value\":29,\r\n      \"max\":100,\r\n      \"min\":0\r\n    },\r\n    \"colorMode\":\"effect\",\r\n    \"ct\":{\r\n      \"value\":3000,\r\n      \"max\":6500,\r\n      \"min\":1200\r\n    },\r\n    \"hue\":{\r\n      \"value\":0,\r\n      \"max\":360,\r\n      \"min\":0\r\n    },\r\n    \"on\":{\r\n      \"value\":false\r\n    },\r\n    \"sat\":{\r\n      \"value\":0,\r\n      \"max\":100,\r\n      \"min\":0\r\n    }\r\n  }\r\n}";

        ControllerInfo controllerInfo = gson.fromJson(controllerInfoJSON, ControllerInfo.class);
        assertThat(controllerInfo, is(notNullValue()));

        if (controllerInfo != null) {
            final State state = controllerInfo.getState();
            assertThat(state, is(notNullValue()));
            assertThat(state.getOnOff(), is(OnOffType.OFF));
        }
    }
}
