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

import java.util.Objects;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This class represents the deserialized results of an IRCC command (both the name and the value of the command). The
 * following is an example of the results that will be deserialized:
 *
 * <pre>
 * {@code
    <av:X_IRCCCode command="Power">AAAAAQAAAAEAAAAVAw==</av:X_IRCCCode>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("X_IRCCCode")
class IrccCode {
    /**
     * The command for this code (will be mixed case, will not be empty)
     */
    private final String command;

    /** The value for this code (will not be empty) */
    private final String value;

    /**
     * Constructs the code from the given command and value
     *
     * @param command a non-null, non-empty command
     * @param value a non-null, non-empty value
     */
    private IrccCode(final String command, final String value) {
        Validate.notEmpty(command, "command cannot be empty");
        Validate.notEmpty(value, "value cannot be empty");
        this.command = command;
        this.value = value;
    }

    /**
     * The command (name) of this code
     *
     * @return a non-null, non-empty command
     */
    public String getCommand() {
        return command;
    }

    /**
     * The command value of this code
     *
     * @return a non-null, non-empty value
     */
    public String getValue() {
        return value;
    }

    /**
     * The converter used to unmarshal the {@link IrccCode}. Please note this should only be used to unmarshal XML
     * (marshaling will throw a {@link NotImplementedException})
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    static class IrccCodeConverter implements Converter {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final @Nullable Class clazz) {
            return IrccCode.class.equals(clazz);
        }

        @Override
        public void marshal(final @Nullable Object arg0, final @Nullable HierarchicalStreamWriter arg1,
                final @Nullable MarshallingContext arg2) {
            throw new NotImplementedException();
        }

        @Override
        public @Nullable Object unmarshal(final @Nullable HierarchicalStreamReader reader,
                final @Nullable UnmarshallingContext context) {
            Objects.requireNonNull(reader, "reader cannot be null");
            Objects.requireNonNull(context, "context cannot be null");

            final String command = reader.getAttribute("command");
            if (StringUtils.isEmpty(command)) {
                return null;
            }

            final String value = reader.getValue();
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            return new IrccCode(command, value);
        }
    }
}
