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
package org.openhab.automation.jsscripting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;

/**
 * Tests {@link GraalJSScriptEngineFactory}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GraalJSScriptEngineFactoryTest {

    @Test
    public void scriptTypesAreGraalJsSpecific() {
        List<String> scriptTypes = new GraalJSScriptEngineFactory().getScriptTypes();

        assertThat(scriptTypes, contains("application/javascript", "application/ecmascript", "text/javascript",
                "text/ecmascript", "js", "mjs"));
        assertThat(scriptTypes.size(), is(6));
    }
}
