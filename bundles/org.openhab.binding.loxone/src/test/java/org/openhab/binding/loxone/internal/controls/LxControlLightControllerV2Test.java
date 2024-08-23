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
package org.openhab.binding.loxone.internal.controls;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlSwitch}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlLightControllerV2Test extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("1076668f-0101-7076-ffff403fb0c34b9e", "0b734138-03ac-03f0-ffff403fb0c34b9e",
                "0b734138-033e-02d4-ffff403fb0c34b9e", "Lighting controller");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlLightControllerV2.class, 1, 5, 1, 6, 4);
        testSubControl(LxControlSwitch.class, "Overall Switch");
        testSubControl(LxControlSwitch.class, "Hall Study Lights");
        testSubControl(LxControlSwitch.class, "Roof Lights");
        testSubControl(LxControlSwitch.class, "Hall Left Lights");
        testSubControl(LxControlSwitch.class, "Hall Right Lights");
    }

    @Test
    public void testChannels() {
        testChannel("Number", Set.of("Scene"));
    }

    @Test
    public void testCommands() {
        for (int i = 0; i < 20; i++) {
            executeCommand(UpDownType.UP);
            testAction("plus");
            executeCommand(UpDownType.DOWN);
            testAction("minus");
        }
        executeCommand(new DecimalType(1));
        testAction(null);

        loadMoodList1();

        executeCommand(new DecimalType(0));
        testAction(null);
        executeCommand(new DecimalType(1));
        testAction(null);
        executeCommand(new DecimalType(2));
        testAction("changeTo/2");
        executeCommand(new DecimalType(3));
        testAction("changeTo/3");
        executeCommand(new DecimalType(4));
        testAction("changeTo/4");
        executeCommand(new DecimalType(5));
        testAction("changeTo/5");
        executeCommand(new DecimalType(6));
        testAction(null);
        executeCommand(new DecimalType(777));
        testAction("changeTo/777");
        executeCommand(new DecimalType(778));
        testAction("changeTo/778");
        executeCommand(new DecimalType(779));
        testAction(null);
        clearMoodList();
    }

    @Test
    public void testMoodListChanges() {
        for (int i = 0; i < 20; i++) {
            loadMoodList1();
            loadMoodList2();
        }
        clearMoodList();
    }

    @Test
    public void testActiveMoodChanges() {
        loadMoodList2();
        for (int i = 0; i < 10; i++) {
            changeLoxoneState("activemoods", "[4]");
            testActiveMoods(779, 4);
            changeLoxoneState("activemoods", "[4,6]");
            testActiveMoods(779, 4, 6);
            changeLoxoneState("activemoods", "[4,6,7,8,5]");
            testActiveMoods(779, 4, 6, 7, 8, 5);
            changeLoxoneState("activemoods", "[4,6,7,8,5,777,778]");
            testActiveMoods(779, 4, 6, 7, 8, 5, 777, 778);
            changeLoxoneState("activemoods", "[6,8,777]");
            testActiveMoods(779, 6, 8, 777);
            changeLoxoneState("activemoods", "[779]");
            testActiveMoods(779, 779);
            changeLoxoneState("activemoods", "[1,2,3,500,900]");
            testActiveMoods(779);
            changeLoxoneState("activemoods", "[1,2,3,6,500,900]");
            testActiveMoods(779, 6);
            changeLoxoneState("activemoods", "[5]");
            testActiveMoods(779, 5);
            changeLoxoneState("activemoods", "[778]");
            testActiveMoods(779, 778);
        }
        clearMoodList();
    }

    @Test
    public void testMoodAddRemove() {
        loadMoodList1();
        for (int i = 0; i < 10; i++) {
            handler.extraControls.values().forEach(c -> {
                LxControlMood m = (LxControlMood) c;
                if (!m.getId().equals(778)) {
                    executeCommand(m, OnOffType.ON);
                    testAction("addMood/" + m.getId());
                    executeCommand(m, OnOffType.OFF);
                    testAction("removeMood/" + m.getId());
                }
            });
        }
        clearMoodList();
    }

    private void testActiveMoods(Integer offId, Integer... moods) {
        List<Integer> ids = new ArrayList<>();
        for (Integer id : moods) {
            if (!offId.equals(id)) {
                LxControlMood m = getMood(id);
                testChannelState(m, OnOffType.ON);
            }
            ids.add(id);
        }
        handler.extraControls.values().stream()
                .filter(c -> !ids.contains(((LxControlMood) c).getId()) && !((LxControlMood) c).getId().equals(offId))
                .forEach(c -> testChannelState(c, OnOffType.OFF));

        if (ids.size() == 1) {
            testChannelState(new DecimalType(ids.get(0)));
        } else {
            testChannelState(UnDefType.UNDEF);
        }
    }

    private void loadMoodList1() {
        String list = loadMoodList("MoodList1.json");
        changeLoxoneState("moodlist", list);
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("2", "Side Lights"));
        options.add(new StateOption("3", "Play Lights"));
        options.add(new StateOption("4", "Study Only"));
        options.add(new StateOption("5", "Low Lights"));
        options.add(new StateOption("777", "Bright"));
        options.add(new StateOption("778", "Off"));
        testMoodList(options, 778);
    }

    private void loadMoodList2() {
        String list = loadMoodList("MoodList2.json");
        changeLoxoneState("moodlist", list);
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("4", "Study Only Changed Name")); // changed name
        options.add(new StateOption("5", "Low Lights")); // same as in list 1
        options.add(new StateOption("6", "Play Lights")); // changed id
        options.add(new StateOption("7", "New Mood 1"));
        options.add(new StateOption("8", "New Mood 2"));
        options.add(new StateOption("777", "Bright"));
        options.add(new StateOption("778", "Off"));
        options.add(new StateOption("779", "New Off"));
        testMoodList(options, 779);
    }

    private LxControlMood getMood(Integer id) {
        LxControl ctrl = handler.extraControls.get(new LxUuid("1076668f-0101-7076-ffff403fb0c34b9e-M" + id));
        assertNotNull(ctrl);
        assertThat(ctrl, is(instanceOf(LxControlMood.class)));
        return (LxControlMood) ctrl;
    }

    private void clearMoodList() {
        changeLoxoneState("moodlist", "[]");
        List<StateOption> options = new ArrayList<>();
        testMoodList(options, 0);
    }

    private void testMoodList(List<StateOption> options, Integer offId) {
        assertEquals(options.size(), handler.extraControls.size());
        if (options.isEmpty()) {
            return;
        }
        Integer min = null;
        Integer max = null;
        for (StateOption o : options) {
            testMood(o.getLabel(), o.getValue(), o.getValue().equals(offId.toString()));
            Integer id = Integer.parseInt(o.getValue());
            assertNotNull(id);
            if (min == null || id < min) {
                min = id;
            }
            if (max == null || id > max) {
                max = id;
            }
        }
        assertNotNull(min);
        assertNotNull(max);
        testChannel("Number", null, new BigDecimal(min), new BigDecimal(max), BigDecimal.ONE, null, false, options,
                Set.of("Scene"));
    }

    private void testMood(String name, String id, boolean isStatic) {
        LxControlMood mood = getMood(Integer.parseInt(id));
        assertEquals(new LxUuid("0b734138-03ac-03f0-ffff403fb0c34b9e"), mood.getRoom().getUuid());
        assertEquals(new LxUuid("0b734138-033e-02d4-ffff403fb0c34b9e"), mood.getCategory().getUuid());
        assertEquals(name, mood.getName());
        assertEquals(id, mood.getId().toString());
        if (isStatic) {
            assertEquals(0, mood.getChannels().size());
        } else {
            assertEquals(1, mood.getChannels().size());
            testChannel(mood, "Switch", Set.of("Lighting"));
        }
    }

    private String loadMoodList(String name) {
        InputStream stream = LxControlLightControllerV2Test.class.getResourceAsStream(name);
        assertNotNull(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        assertNotNull(reader);
        String msg = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        assertNotNull(msg);
        // mood list comes as a single line JSON from Loxone Miniserver
        msg = msg.replaceAll("[\\t+\\n+]", "");
        return msg;
    }
}
