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
package org.openhab.binding.sony.internal.providers.sources;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.AbstractUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelDefinitionBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.providers.SonyModelListener;
import org.openhab.binding.sony.internal.providers.models.SonyThingChannelDefinition;
import org.openhab.binding.sony.internal.providers.models.SonyThingDefinition;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * An implementation of a {@link SonySource} that will source thing types from
 * json files within the user data folder
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSonySource implements SonySource {
    /** The logger */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** The json file extension we are looking for */
    protected static final String JSONEXT = "json";

    /** The GSON that will be used for deserialization */
    protected final Gson gson = GsonUtilities.getDefaultGson();

    /** THe lock protecting the state (thingTypeDefinitions, thingTypes and groupTypes - not listeners) */
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();

    /** Our reference of thing type uids to thing type definitions */
    private final Map<ThingTypeUID, SonyThingDefinition> thingTypeDefinitions = new HashMap<>();

    /** Our reference of thing type uids to thing types */
    private final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<>();

    /** Our reference of thing type uids to thing types */
    private final Map<ChannelGroupTypeUID, ChannelGroupType> groupTypes = new HashMap<>();

    /** The lock used to manage listeners */
    private final ReadWriteLock listenerLock = new ReentrantReadWriteLock();

    /** The list of listeners */
    private final Map<ServiceModelName, List<SonyModelListener>> listeners = new HashMap<>();

    @Override
    public Collection<ThingType> getThingTypes() {
        final Lock readLock = stateLock.readLock();
        readLock.lock();
        try {
            return thingTypes.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @Nullable ThingType getThingType(final ThingTypeUID thingTypeUID) {
        Objects.requireNonNull(thingTypeUID, "thingTypeUID cannot be null");

        final Lock readLock = stateLock.readLock();
        readLock.lock();
        try {
            return thingTypes.get(thingTypeUID);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(final ChannelGroupTypeUID channelGroupTypeUID) {
        Objects.requireNonNull(channelGroupTypeUID, "channelGroupTypeUID cannot be null");
        final Lock readLock = stateLock.readLock();
        readLock.lock();
        try {
            return groupTypes.get(channelGroupTypeUID);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @Nullable Collection<ChannelGroupType> getChannelGroupTypes() {
        final Lock readLock = stateLock.readLock();
        readLock.lock();
        try {
            return groupTypes.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @Nullable SonyThingDefinition getSonyThingTypeDefinition(final ThingTypeUID thingTypeUID) {
        Objects.requireNonNull(thingTypeUID, "thingTypeUID cannot be null");
        final Lock readLock = stateLock.readLock();
        readLock.lock();
        try {
            return thingTypeDefinitions.get(thingTypeUID);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Will read all files in the specified folder and store the related thing types
     *
     * @param folder a non-null, non-empty folder (within userdata)
     * @throws IOException if an IO exception occurs reading the files
     * @throws JsonSyntaxException if a json syntax error occurs
     */
    protected void readFiles(final String folder) throws IOException, JsonSyntaxException {
        Validate.notEmpty(folder, "folder cannot be empty");

        logger.debug("Reading all files in {}", folder);

        final Lock writeLock = stateLock.writeLock();
        writeLock.lock();
        try {
            // clear out prior entries
            groupTypes.clear();
            thingTypes.clear();
            thingTypeDefinitions.clear();

            for (final File file : new File(folder).listFiles()) {
                if (file.isFile()) {
                    readFile(file.getAbsolutePath());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Reads the specified file path, validates the syntax and stores the new thing
     * type
     *
     * @param filePath a possibly null, possibly empty file path to read
     * @return a non-null, potentially empty list of thing definitions to their thing type
     * @throws IOException if an IO Exception occurs reading the file
     * @throws JsonSyntaxException if a json syntax error occurs
     */
    protected List<Map.Entry<ThingType, SonyThingDefinition>> readFile(final @Nullable String filePath)
            throws IOException, JsonSyntaxException {
        final List<SonyThingDefinition> ttds = readThingDefinitions(filePath);
        if (ttds.isEmpty()) {
            return Collections.emptyList();
        }

        final String fileName = FilenameUtils.getName(filePath);
        return addThingDefinitions(fileName, ttds);
    }

    /**
     * Reads the specified file path and returns the thing definitions within it
     *
     * @param filePath a possibly null, possibly empty file path to read
     * @return a non-null, possibly empty list of sony thing defintions found in the file
     * @throws IOException if an IO Exception occurs reading the file
     * @throws JsonSyntaxException if a json syntax error occurs
     */
    protected List<SonyThingDefinition> readThingDefinitions(final @Nullable String filePath)
            throws IOException, JsonSyntaxException {
        if (filePath != null && StringUtils.isEmpty(filePath)) {
            logger.debug("Unknown file: {}", filePath);
            return Collections.emptyList();
        }

        final String fileName = FilenameUtils.getName(filePath);

        final String ext = FilenameUtils.getExtension(filePath);
        if (!StringUtils.equalsIgnoreCase(JSONEXT, ext)) {
            logger.debug("Ignoring {} since it's not a .{} file", fileName, JSONEXT);
            return Collections.emptyList();
        }

        logger.debug("Reading file {} as a SonyThingDefinition[]", filePath);
        final String contents = FileUtils.readFileToString(new File(filePath));
        if (contents == null || StringUtils.isEmpty(contents)) {
            logger.debug("Ignoring {} since it was an empty file", JSONEXT);
            return Collections.emptyList();
        }

        final JsonElement def = gson.fromJson(contents, JsonElement.class);
        if (def.isJsonArray()) {
            return gson.fromJson(def, SonyThingDefinition.LISTTYPETOKEN);
        } else {
            final SonyThingDefinition ttd = gson.fromJson(def, SonyThingDefinition.class);
            return Collections.singletonList(ttd);
        }
    }

    /**
     * Adds thing definition(s) for the reference name
     * 
     * @param referenceName a non-null, non-empty reference name
     * @param ttds a non-null, possibly empty list of thing definitions
     * @return a non-null, possibly empty list of thingtypes to thing definitions
     */
    protected List<Map.Entry<ThingType, SonyThingDefinition>> addThingDefinitions(final String referenceName,
            final List<SonyThingDefinition> ttds) {
        Validate.notEmpty(referenceName, "referenceName cannot be empty");
        Objects.requireNonNull(ttds, "ttds cannot be null");

        logger.debug("Processing {}", referenceName);
        int idx = 0;

        final List<Map.Entry<ThingType, SonyThingDefinition>> results = new ArrayList<>();

        final Lock writeLock = stateLock.writeLock();
        writeLock.lock();
        try {
            for (final SonyThingDefinition ttd : ttds) {
                idx++;

                final List<String> validationMessage = new ArrayList<>();
                final Map<String, String> channelGroups = ttd.getChannelGroups();

                final String service = ttd.getService();
                if (service == null || StringUtils.isEmpty(service)) {
                    validationMessage.add("Invalid/missing service element");
                } else if (!service.matches(AbstractUID.SEGMENT_PATTERN)) {
                    validationMessage.add("Invalid service element (must be a valid UID): " + service);
                }

                final String modelName = ttd.getModelName();
                if (modelName == null || StringUtils.isEmpty(modelName)) {
                    validationMessage.add("Invalid/missing modelName element");
                } else if (!modelName.matches(AbstractUID.SEGMENT_PATTERN)) {
                    validationMessage.add("Invalid modelName element (must be a valid UID): " + modelName);
                }

                final String label = ttd.getLabel();
                final String desc = ttd.getDescription();

                final List<SonyThingChannelDefinition> chls = ttd.getChannels();

                final Map<String, List<ChannelDefinition>> cds = new HashMap<>();
                for (final SonyThingChannelDefinition chl : chls) {

                    final List<String> channelValidationMessage = new ArrayList<>();

                    final String channelId = chl.getChannelId();
                    if (channelId == null || StringUtils.isEmpty(channelId)) {
                        channelValidationMessage.add("Missing channelID element");
                        continue;
                    }

                    final String groupId = StringUtils.substringBefore(channelId, "#");
                    if (groupId == null || StringUtils.isEmpty(groupId)) {
                        channelValidationMessage.add("Missing groupID from channelId: " + channelId);
                        continue;
                    }

                    final String idWithoutGroup = StringUtils.substringAfter(channelId, "#");

                    final String channelType = chl.getChannelType();
                    if (channelType == null || StringUtils.isEmpty(channelType)) {
                        channelValidationMessage.add("Missing channelType element");
                        continue;
                    } else if (!channelType.matches(AbstractUID.SEGMENT_PATTERN)) {
                        channelValidationMessage
                                .add("Invalid channelType element (must be a valid UID): " + channelType);
                        continue;
                    }

                    final Map<String, String> props = new HashMap<>();
                    for (final Entry<@Nullable String, @Nullable String> entry : chl.getProperties().entrySet()) {
                        final @Nullable String propKey = entry.getKey();
                        final @Nullable String propValue = entry.getValue();
                        if (propKey == null || StringUtils.isEmpty(propKey)) {
                            channelValidationMessage.add("Missing property key value");
                        } else {
                            props.put(propKey, propValue == null ? "" : propValue);
                        }
                    }

                    props.put(ScalarWebChannel.CNL_BASECHANNELID, channelId);

                    if (channelValidationMessage.isEmpty()) {
                        List<ChannelDefinition> chlDefs = cds.get(groupId);
                        if (chlDefs == null) {
                            chlDefs = new ArrayList<>();
                            cds.put(groupId, chlDefs);
                        }

                        chlDefs.add(new ChannelDefinitionBuilder(idWithoutGroup,
                                new ChannelTypeUID(SonyBindingConstants.BINDING_ID, channelType)).withProperties(props)
                                        .build());
                    } else {
                        validationMessage.addAll(channelValidationMessage);
                    }
                }

                if (chls.isEmpty()) {
                    validationMessage.add("Has no valid channels");
                    continue;
                }

                final String configUriStr = ttd.getConfigUri();
                if (configUriStr == null || StringUtils.isEmpty(configUriStr)) {
                    validationMessage.add("Invalid thing definition - missing configUri string");
                }

                final String thingTypeId = service + "-" + modelName;
                if (validationMessage.isEmpty()) {
                    try {
                        final Map<String, ChannelGroupType> cgd = cds.entrySet().stream()
                                .collect(Collectors.toMap(k -> k.getKey(), e -> {
                                    final String groupId = e.getKey();
                                    final List<ChannelDefinition> channels = e.getValue();
                                    final String groupLabel = channelGroups.getOrDefault(groupId, groupId);
                                    final String groupTypeId = thingTypeId + "-" + groupId;

                                    return ChannelGroupTypeBuilder.instance(
                                            new ChannelGroupTypeUID(SonyBindingConstants.BINDING_ID, groupTypeId),
                                            groupLabel).withChannelDefinitions(channels).build();
                                }));

                        final List<ChannelGroupDefinition> gDefs = cgd.entrySet().stream()
                                .map(gt -> new ChannelGroupDefinition(gt.getKey(), gt.getValue().getUID()))
                                .collect(Collectors.toList());

                        final URI configUri = new URI(configUriStr);
                        final ThingType thingType = ThingTypeBuilder
                                .instance(SonyBindingConstants.BINDING_ID, thingTypeId, label)
                                .withConfigDescriptionURI(configUri).withDescription(desc)
                                .withChannelGroupDefinitions(gDefs).build();

                        final ThingTypeUID uid = thingType.getUID();

                        groupTypes.putAll(cgd.values().stream().collect(Collectors.toMap(k -> k.getUID(), v -> v)));
                        thingTypes.put(uid, thingType);
                        thingTypeDefinitions.put(uid, ttd);

                        results.add(new AbstractMap.SimpleEntry<>(thingType, ttd));

                        fireThingTypeFound(uid);

                        logger.debug("Successfully created a thing type {} from {}", thingType.getUID(), referenceName);

                    } catch (final URISyntaxException e) {
                        validationMessage.add("Configuration URI (" + configUriStr + ") was not a valid URI");
                    }
                }

                if (!validationMessage.isEmpty()) {
                    logger.debug("Error creating a thing type from element #{} ({}) in {}:", idx, modelName,
                            referenceName);
                    for (final String msg : validationMessage) {
                        logger.debug("   {}", msg);
                    }
                }
            }
            return results;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addListener(final String modelName, final ThingTypeUID currentThingTypeUID,
            final SonyModelListener listener) {
        Validate.notEmpty(modelName, "modelName cannot be empty");
        Objects.requireNonNull(currentThingTypeUID, "currentThingTypeUID cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        final String serviceName = SonyUtil.getServiceName(currentThingTypeUID);
        final ServiceModelName srvModelName = new ServiceModelName(serviceName, modelName);

        final Lock writeLock = listenerLock.writeLock();
        try {
            writeLock.lock();

            List<SonyModelListener> list = listeners.get(srvModelName);
            if (list == null) {
                list = new ArrayList<>();
                listeners.put(srvModelName, list);
            }
            if (!list.contains(listener)) {
                list.add(listener);
            }
        } finally {
            writeLock.unlock();
        }

        final ThingTypeUID uidForModel = findLatestThingTypeUID(srvModelName);
        if (uidForModel != null && !Objects.equals(uidForModel, currentThingTypeUID)) {
            listener.thingTypeFound(uidForModel);
        }
    }

    @Override
    public boolean removeListener(final SonyModelListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        final Lock writeLock = listenerLock.writeLock();
        try {
            writeLock.lock();
            return this.listeners.values().stream().map(e -> {
                return e.remove(listener);
            }).anyMatch(e -> e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Helper method to get all {@link ServiceModelName} that are being listened for
     * 
     * @return a non-null, possibly empty set
     */
    protected Set<ServiceModelName> getListeningServiceModelNames() {
        final Lock readLock = listenerLock.readLock();
        try {
            readLock.lock();
            return listeners.keySet();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get's all the listeners for a given {@link ServiceModelName}
     * 
     * Developer note: if a subclass overrides this method, you'll need to modify the addListener (which has
     * listener.get to call this instead)
     * 
     * @param srvModelname a non-null service model name
     * @return a list of listeners or null if no listeners found for service/model name
     */
    protected @Nullable List<SonyModelListener> getListeners(final ServiceModelName srvModelname) {
        Objects.requireNonNull(srvModelname, "srvModelname cannot be empty");

        final Lock readLock = listenerLock.readLock();
        try {
            readLock.lock();
            return listeners.get(srvModelname);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Finds the latest thing type uid for a given service/model
     * 
     * @param srvModelName a non-null service model name
     * @return the latest ThingTypeUID or null if none found
     */
    protected @Nullable ThingTypeUID findLatestThingTypeUID(final ServiceModelName srvModelName) {
        Objects.requireNonNull(srvModelName, "srvModelName cannot be empty");

        final Lock readLock = stateLock.readLock();
        try {
            readLock.lock();

            ThingTypeUID max = null;
            Integer maxVers = null;
            for (final ThingTypeUID uid : thingTypes.keySet()) {
                if (SonyUtil.isModelMatch(uid, srvModelName.getServiceName(), srvModelName.getModelName())) {
                    final Integer vers = SonyUtil.getModelVersion(uid);
                    if (maxVers == null || vers > maxVers) {
                        max = uid;
                        maxVers = vers;
                    }
                }
            }

            return max;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Fires a thing type found message to all listeners for that model
     * 
     * @param uid a non-null thing type UID
     */
    private void fireThingTypeFound(final ThingTypeUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final Integer vers = SonyUtil.getModelVersion(uid);

        final Lock readLock = listenerLock.readLock();
        try {
            readLock.lock();
            this.listeners.entrySet().forEach(e -> {
                final ServiceModelName srvModelName = e.getKey();
                if (SonyUtil.isModelMatch(uid, srvModelName.getServiceName(), srvModelName.getModelName())) {
                    final ThingTypeUID maxUid = findLatestThingTypeUID(srvModelName);
                    final Integer maxVers = maxUid == null ? null : SonyUtil.getModelVersion(maxUid);

                    if (maxVers == null || vers >= maxVers) {
                        e.getValue().stream().forEach(f -> f.thingTypeFound(uid));
                    }
                }
            });
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Helper method to simply get a property as an integer from a property map for a given key
     * 
     * @param properties a non-null properties map
     * @param key a non-null, non-empty key
     * @return the property value as an integer
     * @throws IllegalArgumentException if the property wasn't found or cannot be converted to an integer
     */
    protected static int getPropertyInt(final Map<String, String> properties, final String key) {
        Objects.requireNonNull(properties, "properties cannot be null");
        Validate.notEmpty(key, "key cannot be empty");

        final String prop = getProperty(properties, key);
        try {
            return Integer.parseInt(prop);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Property key " + key + " was not a valid number: " + prop);
        }
    }

    /**
     * Helper method to simply get a property from a property map for a given key
     * 
     * @param properties a non-null properties map
     * @param key a non-null, non-empty key
     * @return the property value
     * @throws IllegalArgumentException if the property wasn't found or is empty
     */
    protected static String getProperty(final Map<String, String> properties, final String key) {
        Objects.requireNonNull(properties, "properties cannot be null");
        Validate.notEmpty(key, "key cannot be empty");

        String prop = null;
        if (properties.containsKey(key)) {
            prop = properties.get(key);
        }

        if (prop == null || StringUtils.isEmpty(prop)) {
            throw new IllegalArgumentException("Property key " + key + " was not found");

        }
        return prop;
    }

    /**
     * Creates a folder if it doesn't already exist
     *
     * @param path a non-null, non-empty path
     * @return true if created, false if not (which means it already existed)
     */
    protected static boolean createFolder(final String path) {
        Validate.notEmpty(path, "path cannot be empty");
        final File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
            return true;
        }
        return false;
    }

    /**
     * Helper class that represents a service name/model pair and provide equals/hashcode services for them
     */
    @NonNullByDefault
    protected class ServiceModelName {
        /** The service name */
        private final String serviceName;

        /** The model name */
        private final String modelName;

        /**
         * Constructs the model from the two attributes
         * 
         * @param serviceName a non-null, non-empty service name
         * @param modelName a non-null, non-empty model name
         */
        public ServiceModelName(final String serviceName, final String modelName) {
            Validate.notEmpty(serviceName, "serviceName cannot be empty");
            Validate.notEmpty(modelName, "modelName cannot be empty");

            this.serviceName = serviceName;
            this.modelName = modelName;
        }

        /**
         * Returns the service name
         * 
         * @return a non-null, non-empty service name
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Returns the model name
         * 
         * @return a non-null non-empty model name
         */
        public String getModelName() {
            return modelName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
            result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
            return result;
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceModelName other = (ServiceModelName) obj;
            return StringUtils.equals(modelName, other.modelName) && StringUtils.equals(serviceName, other.serviceName);
        }
    }
}
