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
package org.openhab.automation.jsscripting.internal.scope;

import java.util.Map;

/**
 * Base class to offer support for script extension providers
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Florian Hotze - Refactor to inherit from {@link AbstractScriptExtensionProvider}
 */
public abstract class ScriptDisposalAwareScriptExtensionProvider extends AbstractScriptExtensionProvider
        implements ScriptDisposalAware {

    @Override
    public void unload(String scriptIdentifier) {
        Map<String, Object> forScript = idToTypes.remove(scriptIdentifier);

        if (forScript != null) {
            for (Object o : forScript.values()) {
                if (o instanceof ScriptDisposalAware script) {
                    script.unload(scriptIdentifier);
                }
            }
        }
    }
}
