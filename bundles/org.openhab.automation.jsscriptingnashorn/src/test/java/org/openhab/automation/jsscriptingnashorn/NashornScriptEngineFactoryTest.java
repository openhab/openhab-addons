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
package org.openhab.automation.jsscriptingnashorn;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jsscriptingnashorn.internal.NashornScriptEngineFactory;

/**
 * Tests {@link NashornScriptEngineFactory}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class NashornScriptEngineFactoryTest {

    @Test
    public void scriptTypesAreNashornSpecific() {
        List<String> scriptTypes = new NashornScriptEngineFactory().getScriptTypes();

        assertThat(scriptTypes,
                contains("nashornjs", "application/javascript;version=ECMAScript-5.1",
                        "application/ecmascript;version=ECMAScript-5.1", "text/javascript;version=ECMAScript-5.1",
                        "text/ecmascript;version=ECMAScript-5.1"));
        assertThat(scriptTypes.size(), is(5));
    }
}
