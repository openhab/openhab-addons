/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.config;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Tr064RootConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064RootConfiguration extends Tr064BaseThingConfiguration {
    public String host = "";
    public String user = "dslf-config";
    public String password = "";

    /* following parameters only available in fritzbox thing */
    public List<String> tamIndices = Collections.emptyList();
    public List<String> callDeflectionIndices = Collections.emptyList();
    public List<String> missedCallDays = Collections.emptyList();
    public List<String> rejectedCallDays = Collections.emptyList();
    public List<String> inboundCallDays = Collections.emptyList();
    public List<String> outboundCallDays = Collections.emptyList();
    public List<String> callListDays = Collections.emptyList();
    public int phonebookInterval = 0;

    public boolean isValid() {
        return !host.isEmpty() && !user.isEmpty() && !password.isEmpty();
    }
}
