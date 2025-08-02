/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;

/**
 * @author Jochen Klein - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HomeAssistantChannelTransformationTests extends AbstractHomeAssistantTests {

    private @Mock @NonNullByDefault({}) AbstractComponent<?> component;

    @BeforeEach
    public void beforeEachChannelTransformationTest() {
        HaID haID = new HaID("homeassistant/light/pool/light/config");
        when(component.getHaID()).thenReturn(haID);
    }

    @Test
    public void testInvalidTemplate() {
        assertThat(transform("{{}}", ""), is(nullValue()));
    }

    @Test
    public void testIif() {
        assertThat(transform("{{ iif(true) }}", ""), is("True"));
        assertThat(transform("{{ iif(false) }}", ""), is("False"));
        assertThat(transform("{{ iif(none) }}", ""), is("False"));
        assertThat(transform("{{ iif(true, 'Yes') }}", ""), is("Yes"));
        assertThat(transform("{{ iif(false, 'Yes') }}", ""), is("False"));
        assertThat(transform("{{ iif(none, 'Yes') }}", ""), is("False"));
        assertThat(transform("{{ iif(true, 'Yes', 'No') }}", ""), is("Yes"));
        assertThat(transform("{{ iif(false, 'Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ iif(none, 'Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ iif(true, 'Yes', 'No', none) }}", ""), is("Yes"));
        assertThat(transform("{{ iif(false, 'Yes', 'No', none) }}", ""), is("No"));
        assertThat(transform("{{ iif(none, 'Yes', 'No', 'NULL') }}", ""), is("NULL"));
        assertThat(transform("{{ iif(none, 'Yes', 'No', none) }}", ""), is("None"));

        assertThat(transform("{{ true | iif('Yes') }}", ""), is("Yes"));
        assertThat(transform("{{ false | iif('Yes') }}", ""), is("False"));
        assertThat(transform("{{ none | iif('Yes') }}", ""), is("False"));
        assertThat(transform("{{ true | iif('Yes', 'No') }}", ""), is("Yes"));
        assertThat(transform("{{ false | iif('Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ none | iif('Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ true | iif('Yes', 'No', none) }}", ""), is("Yes"));
        assertThat(transform("{{ false | iif('Yes', 'No', none) }}", ""), is("No"));
        assertThat(transform("{{ none | iif('Yes', 'No', 'NULL') }}", ""), is("NULL"));
        assertThat(transform("{{ none | iif('Yes', 'No', none) }}", ""), is("None"));
    }

    @Test
    public void testIsDefined() {
        assertThat(transform("{{ value_json.val }}", "{ \"val\": \"abc\" }", "default"), is("abc"));
        assertThat(transform("{{ value_json.val }}", "{ \"val\": null }", "default"), is("None"));
        assertThat(transform("{{ value_json.something | is_defined }}", "{ \"val\": null }", "default"), is("default"));
    }

    @Test
    public void testRegexFindall() {
        assertThat(transform("{{ 'Flight from JFK to LHR' | regex_findall('([A-Z]{3})') }}", ""), is("['JFK', 'LHR']"));
        assertThat(transform(
                "{{ 'button_up_press' | regex_findall('^(?P<button>(?:button_)?[a-z0-9]+)_(?P<action>(?:press|hold)(?:_release)?)$') }}",
                ""), is("[('button_up', 'press')]"));
    }

    @Test
    public void testRegexFindallIndex() {
        assertThat(transform("{{ 'Flight from JFK to LHR' | regex_findall_index('([A-Z]{3})', 0) }}", ""), is("JFK"));
        assertThat(transform("{{ 'Flight from JFK to LHR' | regex_findall_index('([A-Z]{3})', 1) }}", ""), is("LHR"));
        assertThat(transform("{{ ['JFK', 'LHR'] | regex_findall_index('([A-Z]{3})', 1) }}", ""), is("LHR"));
        assertThat(transform(
                "{{ 'button_up_press' | regex_findall_index('^(?P<button>(?:button_)?[a-z0-9]+)_(?P<action>(?:press|hold)(?:_release)?)$') }}",
                ""), is("('button_up', 'press')"));
    }

    @Test
    public void testIntegerDictLookup() {
        assertThat(transform("{{ {0:'off', 1:'low', 2:'medium', 3:'high'}[value] | default('') }}", 0, true),
                is("off"));
    }

    protected @Nullable Object transform(String template, Object value) {
        return new HomeAssistantChannelTransformation(PYTHON, component, PYTHON.newRawTemplate(template), false)
                .transform(value);
    }

    protected @Nullable Object transform(String template, Object value, boolean command) {
        return new HomeAssistantChannelTransformation(PYTHON, component, PYTHON.newRawTemplate(template), command)
                .transform(value);
    }

    protected @Nullable Object transform(String template, Object value, String defaultValue) {
        return new HomeAssistantChannelTransformation(PYTHON, component, PYTHON.newRawTemplate(template), defaultValue)
                .transform(value);
    }
}
