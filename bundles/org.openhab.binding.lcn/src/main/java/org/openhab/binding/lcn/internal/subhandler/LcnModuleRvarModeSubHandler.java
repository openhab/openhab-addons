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
package org.openhab.binding.lcn.internal.subhandler;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.StringType;

/**
 * Handles the heating/cooling mode of a regulator.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleRvarModeSubHandler extends AbstractLcnModuleVariableSubHandler {
    public LcnModuleRvarModeSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    @Override
    public void handleStatusMessage(Matcher matcher) throws LcnException {
        // nothing
    }

    @Override
    public void handleCommandString(StringType command, int number) throws LcnException {
        boolean cooling;
        switch (command.toString()) {
            case "HEATING":
                cooling = false;
                break;
            case "COOLING":
                cooling = true;
                break;
            default:
                throw new LcnException();
        }

        handler.sendPck(PckGenerator.setRVarMode(number, cooling));
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Collections.emptyList();
    }
}
