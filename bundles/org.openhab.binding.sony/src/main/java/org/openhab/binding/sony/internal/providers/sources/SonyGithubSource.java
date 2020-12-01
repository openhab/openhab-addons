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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.providers.SonyModelListener;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyServiceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyThingDefinition;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.openhab.binding.sony.internal.transports.TransportOptionHeader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * An implementation of a {@link SonySource} that will source thing types from
 * github using an OAUTH type of github application.
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyGithubSource extends AbstractSonySource {
    /** The folder base where thing types are stored */
    private static final String FOLDERBASE = ConfigConstants.getUserDataFolder() + File.separator + "sony";

    /** The date pattern for IF-MODIFIED header */
    private static final String WEBDATEPATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /** Various property keys that will be used */
    private static final String PROP_APIKEY = "SonyGithubSource.Api.Key";
    private static final String PROP_APIURL = "SonyGithubSource.Api.Url";
    private static final String PROP_SCANINTERVAL = "SonyGithubSource.Scan.Interval";
    private static final String PROP_APIREST = "SonyGithubSource.Api.RestApi";
    private static final String PROP_APITHINGTYPES = "SonyGithubSource.Api.ThingTypes";
    private static final String PROP_APIDEVISSUES = "SonyGithubSource.Api.Dev.Issues";
    private static final String PROP_APIOPENHABISSUES = "SonyGithubSource.Api.openHab.Issues";
    private static final String PROP_APIDEFTHINGTYPES = "SonyGithubSource.Api.Definition.ThingTypes";
    private static final String PROP_APIDEFCAPABILITIES = "SonyGithubSource.Api.Definition.Capabilities";
    private static final String PROP_APIMETA = "SonyGithubSource.Api.MetaInfo";
    private static final String PROP_FOLDERTYPES = "SonyGithubSource.Folder.ThingTypes";
    private static final String PROP_ISSUECAPABILITY = "SonyGithubSource.Issue.Capability";
    private static final String PROP_ISSUETHINGTYPE = "SonyGithubSource.Issue.ThingType";
    private static final String PROP_ISSUESERVICE = "SonyGithubSource.Issue.Service";
    private static final String PROP_ISSUEMETHOD = "SonyGithubSource.Issue.Method";
    private static final String PROP_LABELOPENHAB = "SonyGithubSource.Label.openHAB";
    private static final String PROP_LABELAPI = "SonyGithubSource.Label.Api";
    private static final String PROP_LABELMETHOD = "SonyGithubSource.Label.Method";
    private static final String PROP_LABELSERVICE = "SonyGithubSource.Label.Service";
    private static final String PROP_LABELCAPABILITY = "SonyGithubSource.Label.Capability";
    private static final String PROP_LABELTHINGTYPE = "SonyGithubSource.Label.ThingType";

    /** The code fence for github */
    private static final String GITHUB_CODEFENCE = "```";

    /** The GITHUB header to specify that we want a raw file */
    private static final TransportOptionHeader GITHUB_RAWHEADER = new TransportOptionHeader("Accept",
            "application/vnd.github.VERSION.raw");

    /** The various properties that we got */
    private final String apiKey;
    private final String apiRestJson;
    private final String apiMetaJson;
    private final String apiThingTypes;
    private final String apiDevIssues;
    private final String apiOpenHABIssues;
    private final String apiDefThingTypes;
    private final String apiDefCapabilities;

    private final String labelOpenHAB;
    private final String labelApi;
    private final String labelService;
    private final String labelMethod;
    private final String labelCapability;
    private final String labelThingType;

    private final String issueCapability;
    private final String issueThingType;
    private final String issueService;
    private final String issueMethod;

    /** The path to the thing type cache */
    private final Path thingTypePath;

    /** The folder watcher (null if none being watched) */
    private final AtomicReference<@Nullable Future<?>> watcher = new AtomicReference<>(null);

    /** The master service capabilities lock to manage access to capabilitiesMaster */
    private final Lock capabilitiesLock = new ReentrantLock();

    /** The master service capabilities */
    private List<SonyServiceCapability> capabilitiesMaster = Collections.emptyList();

    /** The etag of the last download of capabilities */
    private @Nullable String capabilitiesEtag;

    /** The meta information lock used to manage access ot the metaInfo/metaEtag */
    private final Lock metaLock = new ReentrantLock();

    /** The meta information (ignore or convert) for all types */
    private MetaInfo metaInfo = new MetaInfo();

    /** The etag of the last download of meta information */
    private @Nullable String metaEtag;

    /** The known things lock to managed access to knownThingTypes AND waitingModelNames AND thingTypesEtag */
    private final Lock knownThingsLock = new ReentrantLock();

    /** A map of thing type filename to it's related model name pattern */
    private final Map<String, List<ServiceModelName>> knownThingTypes = new HashMap<>();

    /** Set of service/model names waiting for a thing type */
    private final Set<ServiceModelName> waitingModelNames = new HashSet<>();

    /** The etag of the last download of thing types */
    private @Nullable String thingTypesEtag = "1";

    /** The transport to use for calling into github */
    private final SonyHttpTransport transport;

    /** The GSON that will be used for deserialization */
    private final Gson gson;

    /** Funtional interface for determining if an issue is a match */
    @FunctionalInterface
    @NonNullByDefault
    private interface IssueCallback {
        boolean isMatch(String body) throws JsonSyntaxException;
    }

    @FunctionalInterface
    @NonNullByDefault
    static interface ObjCallback<T> {
        boolean isMatch(T foundObj);
    }

    /**
     * Constructs the source and starts the various threads
     * 
     * @param scheduler a non-null scheduler to use
     * @param properties a non-null, possibly empty map of properties
     */
    public SonyGithubSource(final ScheduledExecutorService scheduler, final Map<String, String> properties) {
        Objects.requireNonNull(scheduler, "scheduler cannot be null");
        Objects.requireNonNull(properties, "properties cannot be null");

        gson = GsonUtilities.getDefaultGsonBuilder()
                .registerTypeAdapter(MetaConvert.class, new MetaConvertDeserializer())
                .registerTypeAdapter(MetaInfo.class, new MetaInfoDeserializer())
                .registerTypeAdapter(SonyDeviceCapability.class, new SonyDeviceCapabilitySerializer()).create();

        final String[] apiKeys = new String(Base64.getDecoder().decode(getProperty(properties, PROP_APIKEY)))
                .split(",");
        apiKey = apiKeys[new Random().nextInt(apiKeys.length)];

        final String apiUrl = getProperty(properties, PROP_APIURL);
        try {
            transport = SonyTransportFactory.createHttpTransport(apiUrl);
            transport.setOption(TransportOptionAutoAuth.FALSE);
            transport.setOption(new TransportOptionHeader("Authorization", "token " + apiKey));
            transport.setOption(new TransportOptionHeader("User-Agent", "Sony-openHAB"));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(
                    PROP_APIURL + " defined an invalid URI: " + apiUrl + " - " + e.getMessage());
        }

        apiRestJson = apiUrl + getProperty(properties, PROP_APIREST);
        apiMetaJson = apiUrl + getProperty(properties, PROP_APIMETA);
        apiThingTypes = apiUrl + getProperty(properties, PROP_APITHINGTYPES);
        apiDevIssues = apiUrl + getProperty(properties, PROP_APIDEVISSUES);
        apiOpenHABIssues = apiUrl + getProperty(properties, PROP_APIOPENHABISSUES);
        apiDefThingTypes = apiUrl + getProperty(properties, PROP_APIDEFTHINGTYPES);
        apiDefCapabilities = apiUrl + getProperty(properties, PROP_APIDEFCAPABILITIES);

        labelOpenHAB = getProperty(properties, PROP_LABELOPENHAB);
        labelApi = getProperty(properties, PROP_LABELAPI);
        labelService = getProperty(properties, PROP_LABELSERVICE);
        labelMethod = getProperty(properties, PROP_LABELMETHOD);
        labelCapability = getProperty(properties, PROP_LABELCAPABILITY);
        labelThingType = getProperty(properties, PROP_LABELTHINGTYPE);

        issueCapability = getProperty(properties, PROP_ISSUECAPABILITY);
        issueThingType = getProperty(properties, PROP_ISSUETHINGTYPE);
        issueService = getProperty(properties, PROP_ISSUESERVICE);
        issueMethod = getProperty(properties, PROP_ISSUEMETHOD);

        thingTypePath = Paths.get(FOLDERBASE + getProperty(properties, PROP_FOLDERTYPES));
        createFolder(thingTypePath.toString());

        final int scanInterval = getPropertyInt(properties, PROP_SCANINTERVAL);
        SonyUtil.cancel(watcher.getAndSet(scheduler.scheduleWithFixedDelay(() -> {
            updateFromGithub();
        }, 0, scanInterval, TimeUnit.SECONDS)));

        try {
            readFiles(thingTypePath.toString());
        } catch (JsonSyntaxException | IOException e) {
            logger.debug("Exception reading files from {}: {}", thingTypePath.toString(), e.getMessage(), e);
        }
    }

    @Override
    public void writeThingDefinition(final SonyThingDefinition thingTypeDefinition) {
        Objects.requireNonNull(thingTypeDefinition, "thingTypeDefinition cannot be null");

        final String ttModelName = thingTypeDefinition.getModelName();
        if (ttModelName == null || StringUtils.isEmpty(ttModelName)) {
            logger.debug("Cannot write thing definition because it has no model name: {}", thingTypeDefinition);
            return;
        }

        String modelName;
        try {
            final MetaInfo meta = getMetaInfo();
            if (!meta.isEnabled()) {
                logger.debug("Ignoring write thing definition - processing is disabled: {}", ttModelName);
                return;
            }

            if (meta.isIgnoredModelName(ttModelName)) {
                logger.debug("Ignoring write thing definition - model name was on ignore list: {}", ttModelName);
                return;
            }
            modelName = meta.getModelName(ttModelName);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Exception writing device capabilities: {}", e.getMessage(), e);
            return;
        }

        if (!StringUtils.equalsIgnoreCase(modelName, ttModelName)) {
            logger.debug("Converting model name {} to {}", ttModelName, modelName);
        }

        try {
            final boolean found = findModelName(modelName, apiDefThingTypes, SonyThingDefinition.class,
                    old -> SonyMatcher.matches(thingTypeDefinition, old, metaInfo));

            if (!found && BooleanUtils.isFalse(findIssue(apiOpenHABIssues, b -> {
                final SonyThingDefinition issueDef = gson.fromJson(b.replaceAll(GITHUB_CODEFENCE, ""),
                        SonyThingDefinition.class);
                return SonyMatcher.matches(thingTypeDefinition, issueDef, metaInfo);
            }, labelThingType))) {
                postIssue(apiOpenHABIssues, String.format(issueThingType, modelName), thingTypeDefinition,
                        labelThingType);
            }
        } catch (final JsonSyntaxException e) {
            logger.debug("Exception writing thing defintion: {}", e.getMessage(), e);
        }
    }

    @Override
    public void writeDeviceCapabilities(final SonyDeviceCapability deviceCapability) {
        Objects.requireNonNull(deviceCapability, "deviceCapability cannot be null");

        final String ttModelName = deviceCapability.getModelName();
        if (ttModelName == null || StringUtils.isEmpty(ttModelName)) {
            logger.debug("Cannot write device capabilities because it has no model name: {}", deviceCapability);
            return;
        }

        // ---- Check to see if we should ignore this model ----
        String modelName;
        try {
            final MetaInfo meta = getMetaInfo();
            if (!meta.isEnabled()) {
                logger.debug("Ignoring device capabilities - processing is disabled: {}", ttModelName);
                return;
            }

            if (meta.isIgnoredModelName(ttModelName)) {
                logger.debug("Ignoring device capabilities - model name was on ignore list: {}", ttModelName);
                return;
            }

            modelName = meta.getModelName(ttModelName);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Exception writing device capabilities: {}", e.getMessage(), e);
            return;
        }

        if (!StringUtils.equalsIgnoreCase(modelName, ttModelName)) {
            logger.debug("Converting model name {} to {}", ttModelName, modelName);
        }

        // ----
        // Check to see if the capability already exists (in sonydevices/openHAB/issues)
        // and will post a new issue if it does not (to
        // /openHAB/contents/definitions/capabilities)
        // Will check modelName.json, modelName-1.json, etc up to a max -100
        // ----
        try {
            final boolean found = findModelName(modelName, apiDefCapabilities, SonyDeviceCapability.class,
                    old -> SonyMatcher.matches(deviceCapability, old));

            if (!found && BooleanUtils.isFalse(findIssue(apiOpenHABIssues, b -> {
                final SonyDeviceCapability issueCap = gson.fromJson(b.replaceAll(GITHUB_CODEFENCE, ""),
                        SonyDeviceCapability.class);
                return SonyMatcher.matches(deviceCapability, issueCap);
            }, labelCapability))) {
                postIssue(apiOpenHABIssues, String.format(issueCapability, modelName), deviceCapability,
                        labelCapability);
            }
        } catch (final JsonSyntaxException e) {
            logger.debug("Exception writing device capabilities: {}", e.getMessage(), e);
        }

        // ----
        // Checks if any service or method doesn't exist in the master definition
        // document (sonydevices/dev/contents/apiinfo/restapi.json)
        // If it doesn't, post a new issue (to sonydevices/dev/issues)
        // ----
        try {
            final List<SonyServiceCapability> masterCapabilities = getMasterDefinitions();

            for (final SonyServiceCapability deviceService : deviceCapability.getServices()) {
                // Get all service version for the name
                final List<SonyServiceCapability> masterServices = masterCapabilities.stream()
                        .filter(s -> StringUtils.equalsIgnoreCase(deviceService.getServiceName(), s.getServiceName()))
                        .collect(Collectors.toList());

                // If we didn't find our service or we didn't match any version in the services - post an issue
                if ((masterServices.isEmpty() || masterServices.stream()
                        .noneMatch(srv -> StringUtils.equalsIgnoreCase(deviceService.getVersion(), srv.getVersion())))
                        && BooleanUtils.isFalse(findIssue(apiDevIssues, b -> {
                            final SonyServiceCapability issueSrv = gson.fromJson(b.replaceAll(GITHUB_CODEFENCE, ""),
                                    SonyServiceCapability.class);
                            return SonyMatcher.matches(deviceService, issueSrv);
                        }, labelOpenHAB, labelApi, labelService))) {
                    postIssue(apiDevIssues,
                            String.format(issueService, deviceService.getServiceName(), deviceService.getVersion()),
                            deviceService, labelOpenHAB, labelApi, labelService);
                }

                // Get all the various methods for the service name (across all service
                // versions)
                final List<ScalarWebMethod> mstrMethods = masterServices.stream()
                        .flatMap(srv -> srv.getMethods().stream())
                        .filter(m -> m.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                        .collect(Collectors.toList());

                // Find the method and if not, post an issue
                final List<ScalarWebMethod> deviceMethods = deviceService.getMethods().stream()
                        .filter(m -> m.getVariation() != ScalarWebMethod.UNKNOWN_VARIATION)
                        .collect(Collectors.toList());

                for (final ScalarWebMethod mth : deviceMethods) {
                    if ((mstrMethods.isEmpty() || mstrMethods.stream().noneMatch(m -> SonyMatcher.matches(mth, m)))
                            && BooleanUtils.isFalse(findIssue(apiDevIssues, b -> {
                                final ScalarWebMethod issueMth = gson.fromJson(b.replaceAll(GITHUB_CODEFENCE, ""),
                                        ScalarWebMethod.class);
                                return SonyMatcher.matches(mth, issueMth);
                            }, labelOpenHAB, labelApi, labelMethod))) {
                        postIssue(apiDevIssues,
                                String.format(issueMethod, deviceService.getServiceName(), deviceService.getVersion(),
                                        mth.getMethodName(), mth.getVersion()),
                                mth, labelOpenHAB, labelApi, labelMethod);
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Exception writing service/method capabilities: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to find an issue that matches. Three things can be returned from this method:
     * <ol>
     * <li>TRUE if we found a matching issue</li>
     * <li>FALSE if we did not find a matching issue</li>
     * <li>null if there was an error trying to get a matching issue</li>
     * </ol>
     * 
     * @param baseUri a non-null, non-empty baseUri
     * @param callback a non-null callback to determine if the issue matches or not
     * @param labels a set of labels to filter with
     * @return a boolean indicating success or null if an error occurs
     */
    private @Nullable Boolean findIssue(final String baseUri, final IssueCallback callback, final String... labels) {
        Validate.notEmpty(baseUri, "baseUri cannot be empty");
        Objects.requireNonNull(callback, "callback cannot be null");
        if (labels.length < 1) {
            throw new IllegalArgumentException("labels must have atleast one element");
        }

        // Initial URI
        String uri = baseUri + "?filter=created&state=open&labels=" + String.join(",", labels);

        // go up to a maximum of 100 pages!
        for (int t = 0; t < 100; t++) {
            final HttpResponse resp = transport.executeGet(uri, GITHUB_RAWHEADER);
            if (resp.getHttpCode() == HttpStatus.OK_200) {
                final String content = resp.getContent();
                final JsonArray ja = gson.fromJson(content, JsonArray.class);
                for (int i = 0; i < ja.size(); i++) {
                    final JsonElement je = ja.get(i);
                    if (je instanceof JsonObject) {
                        final JsonObject jo = je.getAsJsonObject();
                        final JsonElement jbody = jo.get("body");
                        if (jbody == null) {
                            logger.debug("There is no body element to the object: {} from {}", jo, content);
                        } else {
                            final String body = jbody.getAsString();
                            try {
                                if (callback.isMatch(body)) {
                                    logger.trace("Found match: {}", body);
                                    return true;
                                }
                            } catch (final JsonSyntaxException e) {
                                logger.trace("JsonSyntaxException on {}: {}", body, e.getMessage());
                            }
                        }
                    } else {
                        logger.debug("Element {} is not a valid JsonObject: {} from {}", i, je, content);
                    }
                }
                final URI nextUri = resp.getLink(HttpResponse.REL_NEXT);
                if (nextUri == null) {
                    logger.trace("No match found for baseURI: {}", baseUri);
                    return false;
                } else {
                    uri = nextUri.toString();
                    logger.trace("Trying next page {}: {}", t + 1, uri);
                }
            } else {
                logger.debug("Error getting issues for baseURI {}: {}", baseUri, resp.getHttpCode());
                return null;
            }
        }
        logger.debug("No match found within the first 100 pages!: {}", baseUri);
        return false;
    }

    /**
     * Helper method to update our internal state from github (and possibly alert listeners of a new thing type)
     */
    private void updateFromGithub() {
        try {
            final MetaInfo meta = getMetaInfo();
            if (!meta.isEnabled()) {
                logger.debug("Ignoring update from github  - processing is disabled");
                return;
            }

            refreshGitHubThingTypes();
            getMasterDefinitions();
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Exception updating from github: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets the master list of service capabilities. This will only reretrieve them if a newer version exists on github
     * 
     * @return a non-null, possibly empty list of service capabilities
     * @throws JsonSyntaxException if we encountered a json syntax problem
     * @throws IOException if an io exception to github occurred
     */
    private List<SonyServiceCapability> getMasterDefinitions() throws JsonSyntaxException, IOException {
        try {
            capabilitiesLock.lock();

            final HttpResponse resp = transport.executeGet(apiRestJson, GITHUB_RAWHEADER, new TransportOptionHeader(
                    HttpHeader.IF_NONE_MATCH, StringUtils.defaultIfEmpty(capabilitiesEtag, "1")));
            if (resp.getHttpCode() == HttpStatus.OK_200) {
                capabilitiesEtag = resp.getResponseHeader(HttpHeader.ETAG.asString());
                final String content = resp.getContent();
                final List<SonyServiceCapability> sdc = gson.fromJson(content,
                        new TypeToken<List<SonyServiceCapability>>() {
                        }.getType());
                capabilitiesMaster = Collections.unmodifiableList(sdc);
                logger.trace("Got new master definition for etag {}: {}", capabilitiesEtag, content);
                return capabilitiesMaster;
            } else if (resp.getHttpCode() == HttpStatus.NOT_MODIFIED_304) {
                logger.trace("Master definitions was not modified - returning last version from etag {}",
                        capabilitiesEtag);
                return capabilitiesMaster;
            } else if (resp.getHttpCode() == HttpStatus.NOT_FOUND_404) {
                logger.trace("No master definitions was found - returning last version from etag {}", capabilitiesEtag);
                return capabilitiesMaster;
            } else {
                throw resp.createException();
            }
        } finally {
            capabilitiesLock.unlock();
        }
    }

    /**
     * Gets the meta information file. This will only reretrieve them if a newer version exists on github
     * 
     * @return a non-null meta information file
     * @throws JsonSyntaxException if we encountered a json syntax problem
     * @throws IOException if an io exception to github occurred
     */
    private MetaInfo getMetaInfo() throws JsonSyntaxException, IOException {
        try {
            metaLock.lock();

            final HttpResponse resp = transport.executeGet(apiMetaJson, GITHUB_RAWHEADER,
                    new TransportOptionHeader(HttpHeader.IF_NONE_MATCH, StringUtils.defaultIfEmpty(metaEtag, "1")));

            if (resp.getHttpCode() == HttpStatus.OK_200) {
                metaEtag = resp.getResponseHeader(HttpHeader.ETAG.asString());
                final String content = resp.getContent();
                logger.trace("Got new meta info for etag {}: {}", metaEtag, content);

                metaInfo = gson.fromJson(content, MetaInfo.class);
                return metaInfo;
            } else if (resp.getHttpCode() == HttpStatus.NOT_MODIFIED_304) {
                logger.trace("Metainfo was not modified - returning last version from etag {}", metaEtag);
                return metaInfo;
            } else if (resp.getHttpCode() == HttpStatus.NOT_FOUND_404) {
                logger.trace("No Metainfo was found - returning last version from etag {}", metaEtag);
                return metaInfo;
            } else {
                throw resp.createException();
            }
        } finally {
            metaLock.unlock();
        }
    }

    /**
     * Refreshs thing types from github. This will ONLY download those things types that are either in use or we are
     * waiting on. Likewise, if a thing type is no longer available on github - we'll remove it locally and notify the
     * listeners that they should use the default thing type
     * 
     * @throws JsonSyntaxException if we encountered a json syntax problem
     * @throws IOException if an io exception to github occurred
     */
    private void refreshGitHubThingTypes() throws IOException, JsonSyntaxException {
        try {
            // save the modelnames in the file
            knownThingsLock.lock();
            final String localThingTypesEtag = thingTypesEtag;
            final HttpResponse resp = transport.executeGet(apiThingTypes, GITHUB_RAWHEADER, new TransportOptionHeader(
                    HttpHeader.IF_NONE_MATCH, StringUtils.defaultIfEmpty(localThingTypesEtag, "1")));

            // treat 200 and 404 the same (404 can be if the directory was empty)
            if (resp.getHttpCode() == HttpStatus.OK_200 || resp.getHttpCode() == HttpStatus.NOT_FOUND_404) {
                thingTypesEtag = resp.getResponseHeader(HttpHeader.ETAG.asString());

                logger.trace("New Thing types etag {}", thingTypesEtag);

                final Map<String, Long> files = Files.walk(thingTypePath).filter(p -> Files.isRegularFile(p))
                        .collect(Collectors.toMap(p -> {
                            return p.getFileName().toString();
                        }, v -> {
                            return v.toFile().lastModified();
                        }));

                if (resp.getHttpCode() == HttpStatus.OK_200) {
                    final String content = resp.getContent();
                    final JsonElement contentElm = gson.fromJson(content, JsonElement.class);

                    JsonArray ja;
                    if (contentElm.isJsonArray()) {
                        ja = contentElm.getAsJsonArray();
                    } else {
                        ja = new JsonArray();
                        ja.add(contentElm);
                    }

                    for (int i = 0; i < ja.size(); i++) {
                        final JsonElement je = ja.get(i);
                        if (je instanceof JsonObject) {
                            final JsonObject jo = je.getAsJsonObject();
                            final JsonElement nameElement = jo.get("name");
                            if (nameElement == null) {
                                logger.debug("There was no name element for {}: {} from {}", i, jo, content);
                                continue;
                            }

                            final String name = nameElement.getAsString();
                            final Long lastModified = files.remove(name);

                            final ModifiedThingDefinitions mtd = getThingDefinition(name, lastModified);

                            // If not null, then we have a new or updated file...
                            if (mtd.getModified() != null && !mtd.getDefinitions().isEmpty()) {
                                final List<ServiceModelName> modelNames = mtd.getDefinitions().stream().map(e -> {
                                    final String service = e == null ? null : e.getService();
                                    final String modelName = e == null ? null : e.getModelName();
                                    return service == null || StringUtils.isEmpty(service) || modelName == null
                                            || StringUtils.isEmpty(modelName) ? null
                                                    : new ServiceModelName(service, modelName);
                                }).filter(e -> e != null).collect(Collectors.toList());

                                logger.debug("Adding known thing types from {}: {}", name, modelNames);

                                knownThingTypes.put(name, modelNames);

                                // if the file exists (meaning we have an update)
                                // or we are waiting on one of the models in the file...
                                // the write out the thing definition
                                final File theFile = thingTypePath.resolve(name).toFile();

                                // always execute isWaiting since it (also) removes the model name waiting
                                final boolean isWaiting = waitingModelNames.removeIf(m -> modelNames.stream()
                                        .anyMatch(mn -> SonyUtil.isModelMatch(mn.getServiceName(), mn.getModelName(),
                                                m.getServiceName(), m.getModelName())));

                                if (theFile.exists() || isWaiting) {
                                    writeThingDefinition(name, mtd.getModified(), mtd.getDefinitions());
                                }
                            }
                        } else {
                            logger.debug("Element {} is not a valid JsonObject: {} from {}", i, je, content);
                        }
                    }
                }

                // If something was leftover (ie deleted from github), delete the local file and
                // reload everything
                if (!files.isEmpty()) {
                    for (final String name : files.keySet()) {
                        final File theFile = thingTypePath.resolve(name).toFile();
                        if (theFile.exists()) {
                            logger.debug("File {} was no longer on github and is being deleted", name);
                            final List<SonyThingDefinition> ttds = readThingDefinitions(theFile.getAbsolutePath());
                            theFile.delete();
                            if (!ttds.isEmpty()) {
                                final List<ServiceModelName> modelNames = ttds.stream().map(e -> {
                                    final String service = e == null ? null : e.getService();
                                    final String modelName = e == null ? null : e.getModelName();
                                    return service == null || StringUtils.isEmpty(service) || modelName == null
                                            || StringUtils.isEmpty(modelName) ? null
                                                    : new ServiceModelName(service, modelName);
                                }).filter(e -> e != null).collect(Collectors.toList());

                                for (final ServiceModelName modelName : getListeningServiceModelNames()) {
                                    modelNames.stream()
                                            .filter(e -> SonyUtil.isModelMatch(e.getServiceName(), e.getModelName(),
                                                    modelName.getServiceName(), modelName.getModelName()))
                                            .forEach(m -> {
                                                final List<SonyModelListener> listeners = getListeners(m);
                                                if (listeners != null) {
                                                    listeners.forEach(l -> l.thingTypeFound(new ThingTypeUID(
                                                            SonyBindingConstants.BINDING_ID, m.getServiceName())));
                                                }
                                            });
                                }
                            }
                        }
                    }
                    try {
                        readFiles(thingTypePath.toString());
                    } catch (JsonSyntaxException | IOException e) {
                        logger.debug("Exception re-reading files: {}", e.getMessage(), e);
                    }
                }
            } else if (resp.getHttpCode() == HttpStatus.NOT_MODIFIED_304) {
                logger.trace("Thing types has not changed for etag {}", thingTypesEtag);
            } else {
                throw resp.createException();
            }
        } finally {
            knownThingsLock.unlock();
        }
    }

    /**
     * Helper method to determine if the model name already existing in a github url
     * 
     * @param modelName a non-null, non-empty model name
     * @param apiUrl a non-null, non-empty API url
     * @param toMatch a non-null class used to deserialize objects from the apiUrl
     * @param callback a non-null callback to determine if there was a match
     * @return true if a matching model name was found, false if not
     */
    private <T> boolean findModelName(final String modelName, final String apiUrl, final Class<T> toMatch,
            final ObjCallback<T> callback) {
        Validate.notEmpty(modelName, "modelName cannot be empty");
        Validate.notEmpty(apiUrl, "apiUrl cannot be empty");
        Objects.requireNonNull(toMatch, "toMatch cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        for (int i = 0; i < 100; i++) {
            String fileName;
            try {
                fileName = URLEncoder.encode(modelName + (i == 0 ? "" : ("-" + i)) + ".json", "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                logger.debug("UnsupportedEncodingException for modelName {}", modelName, e);
                continue;
            }

            final HttpResponse defResp = transport.executeGet(apiUrl + "/" + fileName, GITHUB_RAWHEADER);

            if (defResp.getHttpCode() == HttpStatus.OK_200) {
                final T old = (T) gson.fromJson(defResp.getContent(), toMatch);
                if (callback.isMatch(old)) {
                    return true;
                }
            } else if (defResp.getHttpCode() == HttpStatus.NOT_FOUND_404) {
                break;
            }
        }
        return false;
    }

    /**
     * Helper method to post an issue to github
     * 
     * @param apiIssues a non-null, non-empty isssues API URL
     * @param title a non-null, non-empty title to use
     * @param toPost a non-null object to post
     * @param labels a list of labels to use
     */
    private void postIssue(final String apiIssues, final String title, final Object toPost, final String... labels) {
        Validate.notEmpty(apiIssues, "apiIssues cannot be empty");
        Validate.notEmpty(title, "title cannot be empty");
        Objects.requireNonNull(toPost, "toPost cannot be null");

        final String body = GITHUB_CODEFENCE + gson.toJson(toPost) + GITHUB_CODEFENCE;
        final JsonObject jo = new JsonObject();
        jo.addProperty("title", title);
        jo.addProperty("body", body);

        final JsonArray ja = new JsonArray();
        for (final String label : labels) {
            if (StringUtils.isNotEmpty(label)) {
                ja.add(label);
            }
        }
        jo.add("labels", ja);

        final HttpResponse resp = transport.executePostJson(apiIssues, gson.toJson(jo), GITHUB_RAWHEADER);
        if (resp.getHttpCode() != HttpStatus.CREATED_201) {
            logger.debug("Error posting issue to {}: {}\n{}", apiIssues, resp, body);
        }
    }

    /**
     * Helper method to get all thing definitions for a given name if they have been modified by some date
     * 
     * @param name the non-null, non-empty name
     * @param lastModified the last modified time or null to ignore
     * @return a non-null modified thing definitions
     */
    private ModifiedThingDefinitions getThingDefinition(final String name, final @Nullable Long lastModified) {
        Validate.notEmpty(name, "name cannot be empty");

        final SimpleDateFormat webDateFormat = new SimpleDateFormat(WEBDATEPATTERN);
        final String ifModifiedSince = webDateFormat
                .format(lastModified == null ? new Date(0) : new Date(lastModified));

        final HttpResponse fileResponse = transport.executeGet(apiThingTypes + "/" + name, GITHUB_RAWHEADER,
                new TransportOptionHeader(HttpHeader.IF_MODIFIED_SINCE, ifModifiedSince));

        if (fileResponse.getHttpCode() == HttpStatus.OK_200) {
            final String lastModifiedResponse = fileResponse.getResponseHeader(HttpHeader.LAST_MODIFIED.asString());
            long lastModifiedTime;
            try {
                lastModifiedTime = webDateFormat.parse(lastModifiedResponse).getTime();
            } catch (final ParseException e) {
                lastModifiedTime = System.currentTimeMillis();
                logger.debug(
                        "Cannot parse the last modified response (and thus not setting the last modified attribute on the file): {} for {}",
                        lastModifiedResponse, name);
            }

            final String fileContents = fileResponse.getContent();
            if (fileContents != null && StringUtils.isNotEmpty(fileContents)) {
                try {
                    final JsonElement def = gson.fromJson(fileContents, JsonElement.class);
                    if (def.isJsonArray()) {
                        return new ModifiedThingDefinitions(lastModifiedTime,
                                gson.fromJson(def, SonyThingDefinition.LISTTYPETOKEN));
                    } else {
                        return new ModifiedThingDefinitions(lastModifiedTime,
                                Collections.singletonList(gson.fromJson(def, SonyThingDefinition.class)));
                    }
                } catch (final JsonSyntaxException e) {
                    logger.debug("JsonSyntaxException when trying to parse filecontents for {}: {}", name,
                            e.getMessage());
                }
            } else {
                logger.debug("Definitions file was empty for {}", name);
            }
        } else if (fileResponse.getHttpCode() == HttpStatus.NOT_MODIFIED_304) {
            logger.debug("Definitions for {} were not modified from {}", name, lastModified);
        } else if (fileResponse.getHttpCode() == HttpStatus.NOT_FOUND_404) {
            logger.debug("No definitions for {} were found", name);
        } else {
            logger.debug("Error getting definitions for {} from {}: {}", name, lastModified,
                    fileResponse.getHttpCode());
        }

        // grr - can't return null here. @Nullable can't be used on Map.Entry so we
        // return this instead
        return new ModifiedThingDefinitions();
    }

    /**
     * Helper method to write a bunch of thing definitions to a file using the last modified time
     * 
     * @param name the non-null, non-empty name
     * @param lastModifiedTime the last modified time to use
     * @param ttds a non-null, non-empty list of thing definitions
     * @throws IOException if an IO exception occurred during write
     */
    private void writeThingDefinition(final String name, final long lastModifiedTime,
            final List<SonyThingDefinition> ttds) throws IOException {
        Validate.notEmpty(name, "name cannot be empty");
        Objects.requireNonNull(ttds, "ttds cannot be null");
        if (ttds.isEmpty()) {
            throw new IllegalArgumentException("ttds cannot be empty");
        }

        final File theFile = thingTypePath.resolve(name).toFile();

        if (theFile.exists()) {
            logger.trace("Deleting local file {}", theFile);
            theFile.delete();
        }

        final String fileContents = gson.toJson(ttds);
        logger.debug("Writing new file ({}) found on github: {}", name, fileContents);
        FileUtils.write(theFile, fileContents, false);

        logger.debug("Setting last modified on file ({}) to: {}", name, lastModifiedTime);
        theFile.setLastModified(lastModifiedTime);

        addThingDefinitions(name, ttds);
    }

    @Override
    public void addListener(final String modelName, final ThingTypeUID currentThingTypeUID,
            final SonyModelListener listener) {
        super.addListener(modelName, currentThingTypeUID, listener);

        final String serviceName = SonyUtil.getServiceName(currentThingTypeUID);

        try {
            knownThingsLock.lock();

            // Find out if we know this model name yet...
            final @Nullable String fileName = knownThingTypes.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(f -> new AbstractMap.SimpleEntry<>(e.getKey(), f)))
                    .filter(e -> SonyUtil.isModelMatch(e.getValue().getServiceName(), e.getValue().getModelName(),
                            serviceName, modelName))
                    .map(e -> e.getKey()).filter(e -> e != null).findAny().orElse("");

            // If we found the file name
            // ... it exists - do nothing (super.addListener should have done it)
            // ... it doesn't exist - get the definition and write it
            // If not found
            // ... add it to the waiting list
            if (fileName != null && StringUtils.isNotEmpty(fileName)) {
                final File theFile = thingTypePath.resolve(fileName).toFile();
                if (!theFile.exists()) {
                    try {
                        final ModifiedThingDefinitions mtd = getThingDefinition(fileName, null);
                        if (mtd.getModified() != null && !mtd.getDefinitions().isEmpty()) {
                            writeThingDefinition(fileName, mtd.getModified(), mtd.definitions);
                        }
                    } catch (final IOException e) {
                        logger.debug("Exception reading device capabilities from github: {}", fileName);
                    }
                }
            } else {
                waitingModelNames.add(new ServiceModelName(serviceName, modelName));
            }
        } finally {
            knownThingsLock.unlock();
        }
    }

    @Override
    public void close() {
        SonyUtil.cancel(watcher.getAndSet(null));
    }

    /**
     * Helper class representing a resource of definitions from a given modified date
     */
    @NonNullByDefault
    static class ModifiedThingDefinitions {
        /** The last modified date (or null if unknown) */
        private final @Nullable Long modified;

        /** The list of thing definitions */
        private final List<SonyThingDefinition> definitions;

        /**
         * Constructs the class with no modified date and an empty list of definitions
         */
        ModifiedThingDefinitions() {
            this.modified = null;
            this.definitions = new ArrayList<>();
        }

        /**
         * Consturcts the class with possibly modified date and list of definitions
         * 
         * @param modified a possibly null modified date
         * @param definitions a possibly null, possibly empty list of thing definitions
         */
        ModifiedThingDefinitions(final @Nullable Long modified,
                final @Nullable List<@Nullable SonyThingDefinition> definitions) {
            this.modified = modified;
            this.definitions = SonyUtil.convertNull(definitions);
        }

        /**
         * Returns the last modified date
         * 
         * @return the modified or null if none
         */
        public @Nullable Long getModified() {
            return modified;
        }

        /**
         * Returns the list of thing definitions
         * 
         * @return a non-null, possibly empty list of thing definitinos
         */
        public List<SonyThingDefinition> getDefinitions() {
            return definitions;
        }
    }
}
