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
package org.openhab.io.openhabcloud.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.model.script.engine.action.ActionService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.openhab.io.openhabcloud.NotificationAction;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the cloud connection service and implements interface to communicate with the cloud.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to new Jetty client and ESH APIs
 */
@Component(service = { CloudService.class, EventSubscriber.class,
        ActionService.class }, configurationPid = "org.openhab.openhabcloud", property = Constants.SERVICE_PID
                + "=org.openhab.openhabcloud")
@ConfigurableService(category = "io", label = "openHAB Cloud", description_uri = CloudService.CONFIG_URI)
public class CloudService implements ActionService, CloudClientListener, EventSubscriber {

    protected static final String CONFIG_URI = "io:openhabcloud";

    private static final String CFG_EXPOSE = "expose";
    private static final String CFG_BASE_URL = "baseURL";
    private static final String CFG_MODE = "mode";
    private static final String SECRET_FILE_NAME = "openhabcloud" + File.separator + "secret";
    private static final String DEFAULT_URL = "https://myopenhab.org/";
    private static final int DEFAULT_LOCAL_OPENHAB_MAX_CONCURRENT_REQUESTS = 200;
    private static final int DEFAULT_LOCAL_OPENHAB_REQUEST_TIMEOUT = 30000;
    private static final String HTTPCLIENT_NAME = "openhabcloud";
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom SR = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(CloudService.class);

    public static String clientVersion = null;
    private CloudClient cloudClient;
    private String cloudBaseUrl = null;
    private final HttpClient httpClient;
    protected final ItemRegistry itemRegistry;
    protected final EventPublisher eventPublisher;

    private boolean remoteAccessEnabled = true;
    private Set<String> exposedItems = null;
    private int localPort;

    @Activate
    public CloudService(final @Reference HttpClientFactory httpClientFactory,
            final @Reference ItemRegistry itemRegistry, final @Reference EventPublisher eventPublisher) {
        this.httpClient = httpClientFactory.createHttpClient(HTTPCLIENT_NAME);
        this.httpClient.setStopTimeout(0);
        this.httpClient.setMaxConnectionsPerDestination(DEFAULT_LOCAL_OPENHAB_MAX_CONCURRENT_REQUESTS);
        this.httpClient.setConnectTimeout(DEFAULT_LOCAL_OPENHAB_REQUEST_TIMEOUT);
        this.httpClient.setFollowRedirects(false);

        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
    }

    /**
     * This method sends notification message to mobile app through the openHAB Cloud service
     *
     * @param userId the {@link String} containing the openHAB Cloud user id to send message to
     * @param message the {@link String} containing a message to send to specified user id
     * @param icon the {@link String} containing a name of the icon to be used with this notification
     * @param severity the {@link String} containing severity (good, info, warning, error) of notification
     */
    public void sendNotification(String userId, String message, @Nullable String icon, @Nullable String severity) {
        logger.debug("Sending message '{}' to user id {}", message, userId);
        cloudClient.sendNotification(userId, message, icon, severity);
    }

    /**
     * Sends an advanced notification to log. Log notifications are not pushed to user
     * devices but are shown to all account users in notifications log
     *
     * @param message the {@link String} containing a message to send to specified user id
     * @param icon the {@link String} containing a name of the icon to be used with this notification
     * @param severity the {@link String} containing severity (good, info, warning, error) of notification
     */
    public void sendLogNotification(String message, @Nullable String icon, @Nullable String severity) {
        logger.debug("Sending log message '{}'", message);
        cloudClient.sendLogNotification(message, icon, severity);
    }

    /**
     * Sends a broadcast notification. Broadcast notifications are pushed to all
     * mobile devices of all users of the account
     *
     * @param message the {@link String} containing a message to send to specified user id
     * @param icon the {@link String} containing a name of the icon to be used with this notification
     * @param severity the {@link String} containing severity (good, info, warning, error) of notification
     */
    public void sendBroadcastNotification(String message, @Nullable String icon, @Nullable String severity) {
        logger.debug("Sending broadcast message '{}' to all users", message);
        cloudClient.sendBroadcastNotification(message, icon, severity);
    }

    private String substringBefore(String str, String separator) {
        int index = str.indexOf(separator);
        return index == -1 ? str : str.substring(0, index);
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        clientVersion = substringBefore(context.getBundle().getVersion().toString(), ".qualifier");
        localPort = HttpServiceUtil.getHttpServicePort(context);
        if (localPort == -1) {
            logger.warn("openHAB Cloud connector not started, since no local HTTP port could be determined");
        } else {
            logger.debug("openHAB Cloud connector activated");
            checkJavaVersion();
            modified(config);
        }
    }

    private void checkJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.charAt(2) == '8') {
            // we are on Java 8, let's check the update
            String update = version.substring(version.indexOf('_') + 1);
            try {
                Integer uVersion = Integer.valueOf(update);
                if (uVersion < 101) {
                    logger.warn(
                            "You are running Java {} - the openhab Cloud connection requires at least Java 1.8.0_101, if your cloud server uses Let's Encrypt certificates!",
                            version);
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not determine update version of java {}", version);
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("openHAB Cloud connector deactivated");
        cloudClient.shutdown();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Could not stop Jetty http client", e);
        }
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        if (config != null && config.get(CFG_MODE) != null) {
            remoteAccessEnabled = "remote".equals(config.get(CFG_MODE));
        } else {
            logger.debug("remoteAccessEnabled is not set, keeping value '{}'", remoteAccessEnabled);
        }

        if (config.get(CFG_BASE_URL) != null) {
            cloudBaseUrl = (String) config.get(CFG_BASE_URL);
        } else {
            cloudBaseUrl = DEFAULT_URL;
        }

        exposedItems = new HashSet<>();
        Object expCfg = config.get(CFG_EXPOSE);
        if (expCfg instanceof String value) {
            while (value.startsWith("[")) {
                value = value.substring(1);
            }
            while (value.endsWith("]")) {
                value = value.substring(0, value.length() - 1);
            }
            for (String itemName : Arrays.asList((value).split(","))) {
                exposedItems.add(itemName.trim());
            }
        } else if (expCfg instanceof Iterable iterable) {
            for (Object entry : iterable) {
                exposedItems.add(entry.toString());
            }
        }

        logger.debug("UUID = {}, secret = {}", censored(InstanceUUID.get()), censored(getSecret()));

        if (cloudClient != null) {
            cloudClient.shutdown();
        }

        if (!httpClient.isRunning()) {
            try {
                httpClient.start();
                // we act as a blind proxy, don't try to auto decode content
                httpClient.getContentDecoderFactories().clear();
            } catch (Exception e) {
                logger.error("Could not start Jetty http client", e);
            }
        }

        String localBaseUrl = "http://localhost:" + localPort;
        cloudClient = new CloudClient(httpClient, InstanceUUID.get(), getSecret(), cloudBaseUrl, localBaseUrl,
                remoteAccessEnabled, exposedItems);
        cloudClient.connect();
        cloudClient.setListener(this);
        NotificationAction.cloudService = this;
    }

    @Override
    public String getActionClassName() {
        return NotificationAction.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return NotificationAction.class;
    }

    /**
     * Reads the first line from specified file
     */

    private String readFirstLine(File file) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // no exception handling - we just return the empty String
        }
        return lines == null || lines.isEmpty() ? "" : lines.get(0);
    }

    /**
     * Writes a String to a specified file
     */

    private void writeFile(File file, String content) {
        // create intermediary directories
        file.getParentFile().mkdirs();
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            logger.debug("Created file '{}' with content '{}'", file.getAbsolutePath(), censored(content));
        } catch (FileNotFoundException e) {
            logger.error("Couldn't create file '{}'.", file.getPath(), e);
        } catch (IOException e) {
            logger.error("Couldn't write to file '{}'.", file.getPath(), e);
        }
    }

    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(SR.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Creates a random secret and writes it to the <code>userdata/openhabcloud</code>
     * directory. An existing <code>secret</code> file won't be overwritten.
     * Returns either existing secret from the file or newly created secret.
     */
    private String getSecret() {
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + SECRET_FILE_NAME);
        String newSecretString = "";

        if (!file.exists()) {
            newSecretString = randomString(20);
            logger.debug("New secret = {}", censored(newSecretString));
            writeFile(file, newSecretString);
        } else {
            newSecretString = readFirstLine(file);
            logger.debug("Using secret at '{}' with content '{}'", file.getAbsolutePath(), censored(newSecretString));
        }

        return newSecretString;
    }

    private static String censored(String secret) {
        if (secret.length() < 4) {
            return "*******";
        }
        return secret.substring(0, 2) + "..." + secret.substring(secret.length() - 2, secret.length());
    }

    @Override
    public void sendCommand(String itemName, String commandString) {
        try {
            Item item = itemRegistry.getItem(itemName);
            Command command = null;
            if ("toggle".equalsIgnoreCase(commandString)
                    && (item instanceof SwitchItem || item instanceof RollershutterItem)) {
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.OFF;
                }
                if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.ON;
                }
                if (UpDownType.UP.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.DOWN;
                }
                if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.UP;
                }
            } else {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandString);
            }
            if (command != null) {
                logger.debug("Received command '{}' for item '{}'", commandString, itemName);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
            } else {
                logger.warn("Received invalid command '{}' for item '{}'", commandString, itemName);
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Received command '{}' for a non-existent item '{}'", commandString, itemName);
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(ItemStateEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        ItemStateEvent ise = (ItemStateEvent) event;
        if (supportsUpdates() && exposedItems != null && exposedItems.contains(ise.getItemName())) {
            cloudClient.sendItemUpdate(ise.getItemName(), ise.getItemState().toString());
        }
    }

    private boolean supportsUpdates() {
        return cloudBaseUrl.contains(CFG_BASE_URL);
    }
}
