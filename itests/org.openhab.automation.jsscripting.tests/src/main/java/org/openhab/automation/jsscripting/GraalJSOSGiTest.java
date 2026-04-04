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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.automation.jsscripting.internal.GraalJSScriptEngineFactory;
import org.openhab.automation.jsscripting.internal.JSScriptServiceUtil;
import org.openhab.automation.jsscripting.internal.fs.watch.JSDependencyTracker;
import org.openhab.core.automation.module.script.action.ScriptExecution;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.service.WatchService;
import org.openhab.core.test.java.JavaOSGiTest;

/**
 * Base class for GraalJS-related tests.
 *
 * @author Florian Hotze - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public abstract class GraalJSOSGiTest extends JavaOSGiTest {
    static final String SCRIPT_TYPE = "application/javascript";

    final Map<String, Object> config;

    @TempDir
    @NonNullByDefault({})
    Path tempDir;

    @Mock
    @NonNullByDefault({})
    WatchService watchService;
    @Mock
    @NonNullByDefault({})
    Scheduler scheduler;
    @Mock
    @NonNullByDefault({})
    ScriptExecution scriptExecution;

    @NonNullByDefault({})
    JSScriptServiceUtil jsScriptServiceUtil;
    @NonNullByDefault({})
    JSDependencyTracker jsDependencyTracker;
    @NonNullByDefault({})
    GraalJSScriptEngineFactory scriptEngineFactory;

    GraalJSOSGiTest() {
        config = Map.of();
    }

    GraalJSOSGiTest(Map<String, Object> config) {
        this.config = config;
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        when(watchService.getWatchPath()).thenReturn(tempDir);

        jsScriptServiceUtil = new JSScriptServiceUtil(scheduler, scriptExecution);
        jsDependencyTracker = new JSDependencyTracker(watchService);

        scriptEngineFactory = new GraalJSScriptEngineFactory(jsScriptServiceUtil, jsDependencyTracker, config);
    }

    @AfterEach
    public void afterEach() throws Exception {
        scriptEngineFactory.dispose();
        scriptEngineFactory = null;

        jsDependencyTracker.deactivate();
        jsDependencyTracker = null;

        clearInvocations(watchService, scheduler, scriptExecution);
    }
}
