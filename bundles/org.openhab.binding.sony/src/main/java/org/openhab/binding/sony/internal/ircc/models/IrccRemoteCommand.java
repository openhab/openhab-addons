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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This class represents the deserialized results of an IRCC remote command. The following is an example of the results
 * that will be deserialized.
 *
 *
 * <pre>
 * {@code
    <command name="Confirm" type="ircc" value="AAAAAQAAAAEAAABlAw==" />
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccRemoteCommand {
    /** The representing an IRCC remote command type */
    public static final String IRCC = "ircc";

    /** The representing an URL remote command type */
    public static final String URL = "url";

    /** The name of the remote command */
    private final String name;

    /** The type of command (url, ircc) */
    private final String type;

    /** The value for the command */
    private final String cmd;

    /**
     * Instantiates a new ircc remote command
     *
     * @param name the non-null, non-empty remote command name
     * @param type the non-null, non-empty remote command type
     * @param cmd the non-null, non-empty remote command value
     */
    IrccRemoteCommand(final String name, final String type, final String cmd) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notEmpty(type, "type cannot be empty");
        Validate.notEmpty(cmd, "cmd cannot be empty");

        this.name = name;
        this.type = type;
        // fix a bug in some IRCC systems
        this.cmd = cmd.replace(":80:80", ":80");
    }

    /**
     * Gets the remote command name
     *
     * @return the non-null, non-empty command name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the remote command type
     *
     * @return the non-null, non-empty command type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the remote command value
     *
     * @return the non-null, non-empty command value
     */
    public String getCmd() {
        return cmd;
    }

    @Override
    public String toString() {
        return name + " (" + type + "): " + cmd;
    }

    /**
     * The converter used to unmarshal the {@link IrccRemoteCommandConverter}. Please note this should only be used to
     * unmarshal XML (marshaling will throw a {@link NotImplementedException})
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    static class IrccRemoteCommandConverter implements Converter {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final @Nullable Class clazz) {
            return IrccRemoteCommand.class.equals(clazz);
        }

        @Override
        public void marshal(final @Nullable Object obj, final @Nullable HierarchicalStreamWriter writer,
                final @Nullable MarshallingContext context) {
            throw new NotImplementedException();
        }

        @Override
        @Nullable
        public Object unmarshal(final @Nullable HierarchicalStreamReader reader,
                final @Nullable UnmarshallingContext context) {
            Objects.requireNonNull(reader, "reader cannot be null");
            Objects.requireNonNull(context, "context cannot be null");

            final String name = reader.getAttribute("name");
            if (StringUtils.isEmpty(name)) {
                return null;
            }

            final String type = reader.getAttribute("type");
            if (StringUtils.isEmpty(type)) {
                return null;
            }

            final String value = reader.getAttribute("value");
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            return new IrccRemoteCommand(name, type, value);
        }
    }
}
