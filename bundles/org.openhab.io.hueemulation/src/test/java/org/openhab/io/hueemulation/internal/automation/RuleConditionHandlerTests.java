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
package org.openhab.io.hueemulation.internal.automation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.util.ConditionBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.GroupItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.io.hueemulation.internal.DeviceType;
import org.openhab.io.hueemulation.internal.RuleUtils;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueSensorEntry;

/**
 * Test the {@link HueRuleConditionHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RuleConditionHandlerTests {
    protected @NonNullByDefault({}) HueDataStore ds;

    private static class HueRuleConditionHandlerEx extends HueRuleConditionHandler {
        LocalDateTime dt = LocalDateTime.of(2019, 04, 12, 12, 12, 12);

        public HueRuleConditionHandlerEx(Condition module, HueDataStore ds) {
            super(module, ds);
        }

        @Override
        protected LocalDateTime getNow() {
            return dt;
        }
    }

    @BeforeEach
    public void setUp() {
        ds = new HueDataStore();

        ds.lights.put("1", new HueLightEntry(new SwitchItem("switch"), "switch", DeviceType.SwitchType));

        ds.sensors.put("2", new HueSensorEntry(new ContactItem("contact")));

        ds.groups.put("10",
                new HueGroupEntry("name", new GroupItem("white", new NumberItem("number")), DeviceType.SwitchType));
    }

    @AfterEach
    public void tearDown() {
        RuleUtils.random = new Random();
    }

    @Test
    public void itemNotExisting() {
        Configuration configuration = new Configuration();
        configuration.put("address", "/groups/9/action");
        configuration.put("operator", "dx");
        configuration.put("value", "");
        Condition c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        assertThrows(IllegalStateException.class, () -> new HueRuleConditionHandler(c, ds));
    }

    @Test
    public void itemAccept() {
        Condition c;
        Configuration configuration = new Configuration();
        configuration.put("operator", "dx");
        configuration.put("value", "");

        configuration.put("address", "/groups/10/action");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        new HueRuleConditionHandler(c, ds);

        configuration.put("address", "/lights/1/state");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        new HueRuleConditionHandler(c, ds);

        configuration.put("address", "/sensors/2/state");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        new HueRuleConditionHandler(c, ds);
    }

    @Test
    public void timeRangeAccept() {
        Condition c;
        Configuration configuration = new Configuration();
        configuration.put("address", "/config/localtime");
        configuration.put("operator", "in");
        configuration.put("value", "T12:12:10/T12:12:50");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        HueRuleConditionHandlerEx subject = new HueRuleConditionHandlerEx(c, ds);
        assertThat(subject.isSatisfied(Collections.emptyMap()), is(true));

        configuration.put("address", "/config/localtime");
        configuration.put("operator", "not_in");
        configuration.put("value", "W8/T12:12:10/T12:12:50");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandlerEx(c, ds);
        assertThat(subject.isSatisfied(Collections.emptyMap()), is(false));
    }

    @Test
    public void equalOperator() {
        Map<String, Object> context = new TreeMap<>();

        HueRuleConditionHandler subject;
        Condition c;
        Configuration configuration = new Configuration();
        configuration.put("operator", "eq");

        context.put("newState", OnOffType.ON);
        context.put("oldState", OnOffType.OFF);
        configuration.put("value", "true");
        configuration.put("address", "/lights/1/state");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandler(c, ds);
        assertThat(subject.isSatisfied(context), is(true));

        context.put("newState", OpenClosedType.OPEN);
        context.put("oldState", OpenClosedType.CLOSED);
        configuration.put("value", "true");
        configuration.put("address", "/sensors/2/state");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandler(c, ds);
        assertThat(subject.isSatisfied(context), is(true));

        context.put("newState", new DecimalType(12));
        context.put("oldState", new DecimalType(0));
        configuration.put("value", "12");
        configuration.put("address", "/groups/10/action");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandler(c, ds);
        assertThat(subject.isSatisfied(context), is(true));
    }

    @Test
    public void gtOperator() {
        Map<String, Object> context = new TreeMap<>();

        HueRuleConditionHandler subject;
        Condition c;
        Configuration configuration = new Configuration();
        configuration.put("operator", "gt");

        context.put("newState", new DecimalType(12));
        context.put("oldState", new DecimalType(0));
        configuration.put("value", "10");
        configuration.put("address", "/groups/10/action");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandler(c, ds);
        assertThat(subject.isSatisfied(context), is(true));
    }

    @Test
    public void ltOperator() {
        Map<String, Object> context = new TreeMap<>();

        HueRuleConditionHandler subject;
        Condition c;
        Configuration configuration = new Configuration();
        configuration.put("operator", "lt");

        context.put("newState", new DecimalType(12));
        context.put("oldState", new DecimalType(0));
        configuration.put("value", "15");
        configuration.put("address", "/groups/10/action");
        c = ConditionBuilder.create().withId("a").withTypeUID(HueRuleConditionHandler.MODULE_TYPE_ID)
                .withConfiguration(configuration).build();
        subject = new HueRuleConditionHandler(c, ds);
        assertThat(subject.isSatisfied(context), is(true));
    }
}
