/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.automation.jsscripting.internal.DebuggingGraalScriptEngine;
import org.openhab.automation.jsscripting.internal.OpenhabGraalJSScriptEngine;

/**
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GraalJSScriptEngineFactoryTest extends GraalJSOSGiTest {

    @Test
    public void registersScriptTypeForJs() {
        final List<String> scriptTypes = scriptEngineFactory.getScriptTypes();

        assertTrue(scriptTypes.contains(SCRIPT_TYPE));
        assertTrue(scriptTypes.contains("js"));

        assertFalse(scriptTypes.contains("application/x-python3"));
        assertFalse(scriptTypes.contains("application/javascript;version=ECMAScript-5.1"));
    }

    @Test
    public void createsScriptEngineForJs() {
        try (DebuggingGraalScriptEngine<OpenhabGraalJSScriptEngine> scriptEngine = assertInstanceOf(
                DebuggingGraalScriptEngine.class, scriptEngineFactory.createScriptEngine(SCRIPT_TYPE))) {
            assertNotNull(scriptEngine);
        }
    }

    @Test
    public void doesNotCreateScriptEngineForOtherLanguages() {
        @Nullable
        ScriptEngine scriptEngine = scriptEngineFactory
                .createScriptEngine("application/javascript;version=ECMAScript-5.1");
        assertNull(scriptEngine);

        scriptEngine = scriptEngineFactory.createScriptEngine("application/x-python3");
        assertNull(scriptEngine);
    }
}
