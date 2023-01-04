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
package org.openhab.io.neeo.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.io.neeo.NeeoService;
import org.openhab.io.neeo.internal.models.ItemSubType;
import org.openhab.io.neeo.internal.models.ListUiAction;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;
import org.openhab.io.neeo.internal.models.NeeoDeviceType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.serialization.ChannelUIDSerializer;
import org.openhab.io.neeo.internal.serialization.ItemSubTypeSerializer;
import org.openhab.io.neeo.internal.serialization.ListUiActionSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoBrainDeviceSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoCapabilityTypeSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoDeviceChannelKindSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoDeviceChannelSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoDeviceSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoDeviceTypeSerializer;
import org.openhab.io.neeo.internal.serialization.NeeoThingUIDSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Various utility functions used by the NEEO Integration
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoUtil {

    /**
     * Static variable representing something is not available
     */
    public static final String NOTAVAILABLE = "N/A";

    /**
     * Builds and returns a {@link GsonBuilder}. The GsonBuilder has adapters registered for {@link ThingUID},
     * {@link ChannelUID} and for atomic string references
     *
     * @return a non-null {@link GsonBuilder} to use
     */
    public static GsonBuilder createGsonBuilder() {
        final GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ThingUID.class, new NeeoThingUIDSerializer());
        gsonBuilder.registerTypeAdapter(ChannelUID.class, new ChannelUIDSerializer());
        return gsonBuilder;
    }

    /**
     * Builds and returns a {@link GsonBuilder} suitable to serialize a {@link NeeoDevice}. Will call
     * {@link #createGsonBuilder()} and then add a number of NEEO specific serializers. This will use the
     * {@link NeeoDeviceSerializer} - do not use this call if you need a {@link NeeoBrainDeviceSerializer}. This call
     * will simply call {@link #createNeeoDeviceGsonBuilder(NeeoService, ServiceContext)} with a null service and
     * context
     *
     * @return a non-null {@link GsonBuilder} to use
     */
    static GsonBuilder createNeeoDeviceGsonBuilder() {
        return createNeeoDeviceGsonBuilder(null, null);
    }

    /**
     * Builds and returns a {@link GsonBuilder} suitable to serialize a {@link NeeoDevice} using the specified
     * {@link NeeoService} and {@link ServiceContext}. Will call {@link #createGsonBuilder()} and then add a number of
     * NEEO specific serializers. This will use the {@link NeeoDeviceSerializer} - do not use this call if you need a
     * {@link NeeoBrainDeviceSerializer}
     *
     * @param service a possibly null {@link NeeoService}
     * @param context a possibly null {@link ServiceContext}
     * @return a non-null {@link GsonBuilder} to use
     */
    public static GsonBuilder createNeeoDeviceGsonBuilder(@Nullable NeeoService service,
            @Nullable ServiceContext context) {
        final GsonBuilder gsonBuilder = createGsonBuilder();
        gsonBuilder.registerTypeAdapter(NeeoThingUID.class, new NeeoThingUIDSerializer());
        gsonBuilder.registerTypeAdapter(NeeoDeviceChannelKind.class, new NeeoDeviceChannelKindSerializer());
        gsonBuilder.registerTypeAdapter(NeeoCapabilityType.class, new NeeoCapabilityTypeSerializer());
        gsonBuilder.registerTypeAdapter(ItemSubType.class, new ItemSubTypeSerializer());
        gsonBuilder.registerTypeAdapter(ListUiAction.class, new ListUiActionSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(NeeoDeviceChannel.class, new NeeoDeviceChannelSerializer(context));
        gsonBuilder.registerTypeAdapter(NeeoDeviceType.class, new NeeoDeviceTypeSerializer());
        gsonBuilder.registerTypeAdapter(NeeoDevice.class, new NeeoDeviceSerializer(service, context));
        return gsonBuilder;
    }

    /**
     * Builds and returns a {@link Gson} from {@link #createGsonBuilder()}
     *
     * @return the non-null {@link Gson} to use
     */
    public static Gson createGson() {
        return createGsonBuilder().create();
    }

    /**
     * Gets the servlet url for the given brain id.
     *
     * @param brainId the non-empty brain id
     * @return the servlet url
     */
    public static String getServletUrl(String brainId) {
        requireNotEmpty(brainId, "brainId cannot be empty");
        return NeeoConstants.WEBAPP_PREFIX + "/" + brainId;
    }

    /**
     * Decodes the passed UTF-8 String using an algorithm that's compatible with
     * JavaScript's <code>decodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The UTF-8 encoded String to be decoded
     * @return the decoded String
     */
    public static String decodeURIComponent(@Nullable String s) {
        if (s == null) {
            return "";
        }
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    /**
     * Encodes the passed String as UTF-8 using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s) {
        requireNotEmpty(s, "s cannot be null or empty");
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replaceAll("\\+", "%20").replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
    }

    /**
     * Write a response out to the {@link HttpServletResponse}
     *
     * @param resp the non-null {@link HttpServletResponse}
     * @param str the possibly null, possibly empty string content to write
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void write(HttpServletResponse resp, String str) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");

        final Logger logger = LoggerFactory.getLogger(NeeoUtil.class);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        final PrintWriter pw = resp.getWriter();
        if (str.isEmpty()) {
            pw.print("{}");
        } else {
            pw.print(str);
        }

        logger.trace("Sending: {}", str);
        pw.flush();
    }

    /**
     * Utility function to close an {@link AutoCloseable} and log any exception thrown.
     *
     * @param closeable a possibly null {@link AutoCloseable}. If null, no action is done.
     */
    public static void close(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LoggerFactory.getLogger(NeeoUtil.class).debug("Exception closing: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Checks whether the current thread has been interrupted and throws {@link InterruptedException} if it's been
     * interrupted
     *
     * @throws InterruptedException the interrupted exception
     */
    static void checkInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("thread interrupted");
        }
    }

    /**
     * Cancels the specified {@link Future}
     *
     * @param future a possibly null future. If null, no action is done
     */
    static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Require the specified value to be a non-null, non-empty string
     *
     * @param value the value to check
     * @param msg the msg to use when throwing an {@link IllegalArgumentException}
     * @throws IllegalArgumentException if value is null or an empty string
     */
    public static void requireNotEmpty(String value, String msg) {
        Objects.requireNonNull(value, msg);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Converts a JSON property to a string
     *
     * @param jo the non-null {@link JsonObject} to use
     * @param propertyName the non-empty property name
     * @return the possibly null string representation
     */
    @Nullable
    public static String getString(JsonObject jo, String propertyName) {
        Objects.requireNonNull(jo, "jo cannot be null");
        requireNotEmpty(propertyName, "propertyName cannot be empty");

        final JsonPrimitive jp = jo.getAsJsonPrimitive(propertyName);
        return jp == null || jp.isJsonNull() ? null : jp.getAsString();
    }

    /**
     * Converts a JSON property to an integer
     *
     * @param jo the non-null {@link JsonObject} to use
     * @param propertyName the non-empty property name
     * @return the possibly null integer
     */
    @Nullable
    public static Integer getInt(JsonObject jo, String propertyName) {
        Objects.requireNonNull(jo, "jo cannot be null");
        requireNotEmpty(propertyName, "propertyName cannot be empty");

        final JsonPrimitive jp = jo.getAsJsonPrimitive(propertyName);
        return jp == null || jp.isJsonNull() ? null : jp.getAsInt();
    }

    /**
     * Gets the {@link Command} for the specified enum name - ignoring case
     *
     * @param cmd the non-null {@link Command}
     * @param enumName the non-empty enum name to search for
     * @return the {@link Command} or null if not found (or null if cmd's class is not an enum)
     */
    @Nullable
    static Command getEnum(Class<? extends Command> cmd, String enumName) {
        Objects.requireNonNull(cmd, "cmd cannot be null");
        requireNotEmpty(enumName, "enumName cannot be null");

        if (cmd.isEnum()) {
            for (Command cmdEnum : cmd.getEnumConstants()) {
                if (((Enum<?>) cmdEnum).name().equalsIgnoreCase(enumName)) {
                    return cmdEnum;
                }
            }
        }
        return null;
    }

    /**
     * Guess the {@link NeeoDeviceType} for the given {@link Thing}
     *
     * @param thing the non-null thing
     * @return always {@link NeeoDeviceType#ACCESSOIRE}
     */
    static NeeoDeviceType guessType(Thing thing) {
        Objects.requireNonNull(thing, "thing cannot be null");
        return NeeoDeviceType.ACCESSOIRE;
    }

    /**
     * Gets the label to use from the item or channelType
     *
     * @param item the possibly null item
     * @param channelType the possibly null channel type
     * @return the label to use (or null if no label)
     */
    public static String getLabel(@Nullable Item item, @Nullable ChannelType channelType) {
        if (item != null) {
            final String label = item.getLabel();
            if (label != null && !label.isEmpty()) {
                return label;
            }
        }

        if (channelType != null) {
            final String label = channelType.getLabel();
            if (!label.isEmpty()) {
                return label;
            }
        }

        return NOTAVAILABLE;
    }

    /**
     * Gets the pattern to use from the item or channelType
     *
     * @param item the possibly null item
     * @param channelType the possibly null channel type
     * @return the pattern to use (or null if no pattern to use)
     */
    @Nullable
    public static String getPattern(@Nullable Item item, @Nullable ChannelType channelType) {
        if (item != null) {
            final StateDescription sd = item.getStateDescription();
            final String format = sd == null ? null : sd.getPattern();
            if (format == null || format.isEmpty()) {
                if ("datetime".equalsIgnoreCase(item.getType())) {
                    return "%tF %<tT";
                }
            } else {
                return format;
            }
        }

        if (channelType != null) {
            final String format = channelType.getState() == null ? null : channelType.getState().getPattern();
            if (format == null || format.isEmpty()) {
                if ("datetime".equalsIgnoreCase(channelType.getItemType())) {
                    return "%tF %<tT";
                }
            } else {
                return format;
            }
        }
        return null;
    }

    /**
     * Returns the unique label name given a set of labels. The unique label will be added to the set of labels.
     *
     * @param labels the non-null, possibly empty set of labels
     * @param itemLabel the possibly null, possibly empty item label to get a unique name for
     * @return the unique label
     */
    public static String getUniqueLabel(Set<String> labels, String itemLabel) {
        Objects.requireNonNull(labels, "labels cannot be null");

        String label = itemLabel.isEmpty() ? "NA" : itemLabel;
        int idx = 0;
        if (labels.contains(label)) {
            do {
                idx++;
            } while (labels.contains(label + "." + idx));
            label = label + "." + idx;
        }

        labels.add(label);
        return label;
    }

    /**
     * Returns the group label for the given {@link ThingType} and groupId
     *
     * @param thingType a non null thingType
     * @param groupId a possibly empty, possibly null group ID
     * @return the group label or null if none
     */
    @Nullable
    public static String getGroupLabel(ThingType thingType, @Nullable String groupId) {
        Objects.requireNonNull(thingType, "thingType cannot be null");

        if (groupId != null) {
            for (ChannelGroupDefinition cgd : thingType.getChannelGroupDefinitions()) {
                if (Objects.equals(groupId, cgd.getId())) {
                    return cgd.getLabel();
                }
            }
        }

        return null;
    }
}
