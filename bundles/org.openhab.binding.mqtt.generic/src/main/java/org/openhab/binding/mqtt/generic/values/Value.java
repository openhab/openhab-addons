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
package org.openhab.binding.mqtt.generic.values;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;

/**
 * MQTT topics are not inherently typed.
 *
 * <p>
 * With this class users are able to map MQTT topic values to framework types,
 * for example for numbers {@link NumberValue}, boolean values {@link OnOffValue}, percentage values
 * {@link PercentageValue}, string values {@link TextValue} and more.
 * </p>
 *
 * <p>
 * This class and the encapsulated (cached) state are necessary, because MQTT can't be queried,
 * but we still need to be able to respond to framework requests for a value.
 * </p>
 *
 * <p>
 * {@link #getCache()} is used to retrieve a topic state and a call to {@link #update(Command)} sets the value.
 * </p>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class Value {
    protected State state = UnDefType.UNDEF;
    protected final List<Class<? extends Command>> commandTypes;
    private final String itemType;

    protected Value(String itemType, List<Class<? extends Command>> commandTypes) {
        this.itemType = itemType;
        this.commandTypes = commandTypes;
    }

    /**
     * Return a list of supported command types. The order of the list is important.
     * <p>
     * The framework will try to parse an incoming string into one of those command types,
     * starting with the first and continue until it succeeds.
     * </p>
     * <p>
     * Your {@link #update(Command)} method must accept all command types of this list.
     * You may accept more command types. This allows you to restrict the parsing of incoming
     * MQTT values to the listed types, but handle more user commands.
     * </p>
     * A prominent example is the {@link NumberValue}, which does not return {@link PercentType},
     * because that would interfere with {@link DecimalType} while parsing the MQTT value.
     * It does however accept a {@link PercentType} for {@link #update(Command)}, because a
     * linked Item could send that type of command.
     */
    public final List<Class<? extends Command>> getSupportedCommandTypes() {
        return commandTypes;
    }

    /**
     * Returns the item-type (one of {@link CoreItemFactory}).
     */
    public final String getItemType() {
        return itemType;
    }

    /**
     * Returns the current value state.
     */
    public final State getChannelState() {
        return state;
    }

    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        if (pattern == null) {
            return command.format("%s");
        }
        return command.format(pattern);
    }

    /**
     * Returns true if this is a binary type.
     */
    public boolean isBinary() {
        return false;
    }

    /**
     * If the MQTT connection is not yet initialised or no values have
     * been received yet, the default value is {@link UnDefType#UNDEF}. To restore to the
     * default value after a connection got lost etc, this method will be called.
     */
    public final void resetState() {
        state = UnDefType.UNDEF;
    }

    /**
     * Updates the internal value state with the given state.
     *
     * @param newState The new state to update the internal value.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public void update(State newState) throws IllegalArgumentException {
        state = newState;
    }

    /**
     * Parses a given command into the proper type for this Value type. This will usually be a State,
     * but can be a Command.
     *
     * @param command The command to parse.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public abstract Command parseCommand(Command command) throws IllegalArgumentException;

    /**
     * Parses a given command from MQTT into the proper type for this Value type. This will usually
     * be a State, but can be a non-State Command, in which case the channel will be commanded instead
     * of updated, regardless of postCommand setting. The default implementation just calls
     * parseCommand, so that both directions have the same logic.
     *
     * @param command The command to parse.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public Command parseMessage(Command command) throws IllegalArgumentException {
        return parseCommand(command);
    }

    /**
     * Updates the internal value state with the given binary payload.
     *
     * @param data The binary payload to update the internal value.
     * @exception IllegalArgumentException Thrown if for example a text is assigned to a number type.
     */
    public void update(byte data[]) throws IllegalArgumentException {
        String mimeType = null;

        // URLConnection.guessContentTypeFromStream(input) is not sufficient to detect all JPEG files
        if (data.length >= 2 && data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[data.length - 2] == (byte) 0xFF
                && data[data.length - 1] == (byte) 0xD9) {
            mimeType = "image/jpeg";
        } else {
            try (final ByteArrayInputStream input = new ByteArrayInputStream(data)) {
                try {
                    mimeType = URLConnection.guessContentTypeFromStream(input);
                } catch (final IOException ignored) {
                }
            } catch (final IOException ignored) {
            }
        }
        state = new RawType(data, mimeType == null ? RawType.DEFAULT_MIME_TYPE : mimeType);
    }

    /**
     * Return the state description fragment builder for this value state.
     *
     * @param readOnly True if this is a read-only value.
     * @return A state description fragment builder
     */
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        return StateDescriptionFragmentBuilder.create().withReadOnly(readOnly).withPattern("%s");
    }

    /**
     * Return the command description builder for this value state.
     *
     * @return A command description builder
     */
    public CommandDescriptionBuilder createCommandDescription() {
        return CommandDescriptionBuilder.create();
    }
}
