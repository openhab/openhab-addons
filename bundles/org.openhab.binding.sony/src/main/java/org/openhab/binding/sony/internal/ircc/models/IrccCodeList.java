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
package org.openhab.binding.sony.internal.ircc.models;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the deserialized results of an IRCC actionList command. The following is an example of the
 * results that will be deserialized:
 *
 * <pre>
 * {@code
    <av:X_IRCCCodeList xmlns:av="urn:schemas-sony-com:av">
        <av:X_IRCCCode command="Power">AAAAAQAAAAEAAAAVAw==</av:X_IRCCCode>
    </av:X_IRCCCodeList>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccCodeList {

    /**
     * The list of commands for this code list
     */
    @XStreamImplicit
    private @Nullable List<@Nullable IrccCode> cmds;

    /**
     * Gets the commands for the code list
     *
     * @return a possibly empty list of {@link IrccCode}
     */
    public List<IrccCode> getCommands() {
        final List<@Nullable IrccCode> localCmds = cmds;

        // Need to filter out nulls in case of the the IRCC was invalid
        return localCmds == null ? Collections.emptyList()
                : Collections.unmodifiableList(localCmds.stream().filter(x -> x != null).collect(Collectors.toList()));
    }
}
