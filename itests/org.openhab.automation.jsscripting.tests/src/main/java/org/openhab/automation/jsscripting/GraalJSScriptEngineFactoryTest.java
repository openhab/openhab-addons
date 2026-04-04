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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;
import org.openhab.automation.jsscripting.internal.JSScriptServiceUtil;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.automation.jsscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.openhab.core.service.WatchService;
import org.openhab.core.test.java.JavaOSGiTest;

/**
 * @author Florian Hotze - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class GraalJSScriptEngineFactoryTest extends JavaOSGiTest {

    private final Map<String, Object> config = Map.of();

    private @TempDir @NonNullByDefault({}) Path tempDir;

    private @Mock @NonNullByDefault({}) WatchService watchService;
    private @Mock @NonNullByDefault({}) JSScriptServiceUtil jsScriptServiceUtil;

    private @NonNullByDefault({}) JSDependencyTracker jsDependencyTracker;
    private @NonNullByDefault({}) GraalJSScriptEngineFactory scriptEngineFactory;

    @BeforeEach
    public void beforeEach() {
        when(watchService.getWatchPath()).thenReturn(tempDir);

        jsDependencyTracker = new JSDependencyTracker(watchService);

        scriptEngineFactory = new GraalJSScriptEngineFactory(jsScriptServiceUtil, jsDependencyTracker, config);
    }

    @AfterEach
    public void afterEach() {
        scriptEngineFactory.dispose();
        scriptEngineFactory = null;

        jsDependencyTracker.deactivate();
        jsDependencyTracker = null;

        clearInvocations(watchService);
    }

    @Test
    public void registersScriptTypeForJs() {
        final List<String> scriptTypes = scriptEngineFactory.getScriptTypes();

        assertTrue(scriptTypes.contains("application/javascript"));
        assertTrue(scriptTypes.contains("js"));

        assertFalse(scriptTypes.contains("application/x-python3"));
        assertFalse(scriptTypes.contains("application/javascript;version=ECMAScript-5.1"));
    }

    @Test
    public void createsScriptEngineForJs() {
        final @Nullable ScriptEngine scriptEngine = scriptEngineFactory.createScriptEngine("application/javascript");
        assertNotNull(scriptEngine);
        assertInstanceOf(InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable.class,
                scriptEngine);
    }

    @Test
    public void doesNotCreateScriptEngineForOtherLanguages() {
        final @Nullable ScriptEngine scriptEngine = scriptEngineFactory
                .createScriptEngine("application/javascript;version=ECMAScript-5.1");
        assertNull(scriptEngine);
    }
}
