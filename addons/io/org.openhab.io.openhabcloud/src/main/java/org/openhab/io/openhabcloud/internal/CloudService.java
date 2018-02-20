/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.openhabcloud.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.id.InstanceUUID;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.openhab.core.OpenHAB;
import org.openhab.io.openhabcloud.NotificationAction;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the cloud connection service and implements interface to communicate with the cloud.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to new Jetty client and ESH APIs
 */

public class CloudService implements ActionService, CloudClientListener, EventSubscriber {

    private static final String CFG_EXPOSE = "expose";
    private static final String CFG_BASE_URL = "baseURL";
    private static final String CFG_MODE = "mode";
    private static final String SECRET_FILE_NAME = "openhabcloud" + File.separator + "secret";
    private static final String DEFAULT_URL = "https://myopenhab.org/";

    private Logger logger = LoggerFactory.getLogger(CloudService.class);

    public static String clientVersion = null;
    private CloudClient cloudClient;
    private String cloudBaseUrl = null;
    protected ItemRegistry itemRegistry = null;
    protected EventPublisher eventPublisher = null;

    private boolean remoteAccessEnabled = true;
    private Set<String> exposedItems = null;
    private int localPort;

    public CloudService() {
    }

    /**
     * This method sends notification message to mobile app through the openHAB Cloud service
     *
     * @param userId the {@link String} containing the openHAB Cloud user id to send message to
     * @param message the {@link String} containing a message to send to specified user id
     * @param icon the {@link String} containing a name of the icon to be used with this notification
     * @param severity the {@link String} containing severity (good, info, warning, error) of notification
     */
    public void sendNotification(String userId, String message, String icon, String severity) {
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
    public void sendLogNotification(String message, String icon, String severity) {
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
    public void sendBroadcastNotification(String message, String icon, String severity) {
        logger.debug("Sending broadcast message '{}' to all users", message);
        cloudClient.sendBroadcastNotification(message, icon, severity);
    }

    protected void activate(BundleContext context, Map<String, ?> config) {
        clientVersion = StringUtils.substringBefore(context.getBundle().getVersion().toString(), ".qualifier");
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

    protected void deactivate() {
        logger.debug("openHAB Cloud connector deactivated");
        cloudClient.shutdown();
    }

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
        if (expCfg instanceof String) {
            String value = (String) expCfg;
            while (value.startsWith("[")) {
                value = value.substring(1);
            }
            while (value.endsWith("]")) {
                value = value.substring(0, value.length() - 1);
            }
            for (String itemName : Arrays.asList((value).split(","))) {
                exposedItems.add(itemName.trim());
            }
        } else if (expCfg instanceof Iterable) {
            for (Object entry : ((Iterable<?>) expCfg)) {
                exposedItems.add(entry.toString());
            }
        }

        logger.debug("UUID = {}, secret = {}", InstanceUUID.get(), getSecret());

        if (cloudClient != null) {
            cloudClient.shutdown();
        }

        String localBaseUrl = "http://localhost:" + localPort;
        cloudClient = new CloudClient(InstanceUUID.get(), getSecret(), cloudBaseUrl, localBaseUrl, remoteAccessEnabled,
                exposedItems);
        cloudClient.setOpenHABVersion(OpenHAB.getVersion());
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
            lines = IOUtils.readLines(new FileInputStream(file));
        } catch (IOException ioe) {
            // no exception handling - we just return the empty String
        }
        return lines != null && lines.size() > 0 ? lines.get(0) : "";
    }

    /**
     * Writes a String to a specified file
     */

    private void writeFile(File file, String content) {
        // create intermediary directories
        file.getParentFile().mkdirs();
        try {
            IOUtils.write(content, new FileOutputStream(file));
            logger.debug("Created file '{}' with content '{}'", file.getAbsolutePath(), content);
        } catch (FileNotFoundException e) {
            logger.error("Couldn't create file '{}'.", file.getPath(), e);
        } catch (IOException e) {
            logger.error("Couldn't write to file '{}'.", file.getPath(), e);
        }
    }

    /**
     * Creates a random secret and writes it to the <code>userdata/openhabcloud</code>
     * directory. An existing <code>secret</code> file won't be overwritten.
     * Returns either existing secret from the file or newly created secret.
     */
    private String getSecret() {
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + SECRET_FILE_NAME);
        String newSecretString = "";

        if (!file.exists()) {
            newSecretString = RandomStringUtils.randomAlphanumeric(20);
            logger.debug("New secret = {}", newSecretString);
            writeFile(file, newSecretString);
        } else {
            newSecretString = readFirstLine(file);
            logger.debug("Using secret at '{}' with content '{}'", file.getAbsolutePath(), newSecretString);
        }

        return newSecretString;
    }

    @Override
    public void sendCommand(String itemName, String commandString) {
        try {
            if (itemRegistry != null) {
                Item item = itemRegistry.getItem(itemName);
                Command command = null;
                if (item != null) {
                    if (this.eventPublisher != null) {
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
                            this.eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
                        } else {
                            logger.warn("Received invalid command '{}' for item '{}'", commandString, itemName);
                        }
                    }
                } else {
                    logger.warn("Received command '{}' for non-existent item '{}'", commandString, itemName);
                }
            } else {
                return;
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Received command for a non-existent item '{}'", itemName);
        }
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Collections.singleton(ItemStateEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        ItemStateEvent ise = (ItemStateEvent) event;
        if (exposedItems != null && exposedItems.contains(ise.getItemName())) {
            cloudClient.sendItemUpdate(ise.getItemName(), ise.getItemState().toString());
        }
    }

}
