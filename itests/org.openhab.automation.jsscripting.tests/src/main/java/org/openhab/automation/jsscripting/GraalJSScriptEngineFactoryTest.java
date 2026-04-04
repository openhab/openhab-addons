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
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;
import org.openhab.automation.jsscripting.internal.JSScriptServiceUtil;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.core.test.java.JavaOSGiTest;

/**
 * @author Florian Hotze - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class GraalJSScriptEngineFactoryTest extends JavaOSGiTest {

    private final Map<String, Object> config = Map.of();

    private @Mock @NonNullByDefault({}) JSScriptServiceUtil jsScriptServiceUtil;
    private @Mock @NonNullByDefault({}) JSDependencyTracker jsDependencyTracker;

    private @NonNullByDefault({}) GraalJSScriptEngineFactory scriptEngineFactory;

    @BeforeEach
    public void beforeEach() {
        scriptEngineFactory = new GraalJSScriptEngineFactory(jsScriptServiceUtil, jsDependencyTracker, config);
    }

    @AfterEach
    public void afterEach() {
        scriptEngineFactory.dispose();
        scriptEngineFactory = null;
    }

    @Test
    public void registerScriptTypeForJs() {
        assertTrue(scriptEngineFactory.getScriptTypes().contains("application/javascript"));
    }
}
