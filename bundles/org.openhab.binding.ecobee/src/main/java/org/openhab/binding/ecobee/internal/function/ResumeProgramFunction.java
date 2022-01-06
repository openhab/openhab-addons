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
package org.openhab.binding.ecobee.internal.function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The resume program function removes the currently running event providing the event
 * is not a mandatory demand response event. The top active event is removed from the
 * stack and the thermostat resumes its program, or enters the next event in the stack
 * if one exists. This function may need to be called multiple times in order to resume all
 * events. Sending 3 resume functions in a row will resume the thermostat to its program
 * in all instances. Note that vacation events cannot be resumed, you must delete the
 * vacation event using the DeleteVacationFunction.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class ResumeProgramFunction extends AbstractFunction {

    public ResumeProgramFunction(@Nullable Boolean resumeAll) {
        super("resumeProgram");
        if (resumeAll != null) {
            params.put("resumeAll", resumeAll);
        }
    }
}
