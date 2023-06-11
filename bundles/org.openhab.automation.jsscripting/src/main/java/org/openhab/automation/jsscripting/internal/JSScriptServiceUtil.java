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
package org.openhab.automation.jsscripting.internal;

import java.util.concurrent.locks.Lock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.module.script.action.ScriptExecution;
import org.openhab.core.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * OSGi utility service for providing easy access to script services.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(immediate = true, service = JSScriptServiceUtil.class)
@NonNullByDefault
public class JSScriptServiceUtil {
    private final Scheduler scheduler;
    private final ScriptExecution scriptExecution;

    @Activate
    public JSScriptServiceUtil(final @Reference Scheduler scheduler, final @Reference ScriptExecution scriptExecution) {
        this.scheduler = scheduler;
        this.scriptExecution = scriptExecution;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public ScriptExecution getScriptExecution() {
        return scriptExecution;
    }

    public JSRuntimeFeatures getJSRuntimeFeatures(Lock lock) {
        return new JSRuntimeFeatures(lock, this);
    }
}
