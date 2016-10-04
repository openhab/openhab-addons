/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.myopenhab.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.openhab.core.OpenHAB;
import org.openhab.io.myopenhab.MyOpenHABAction;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts my.openHAB connection service and implements interface to communicate with my.openHAB.
 * It also acts as a persistence service to send commands and updates for selected items to my.openHAB
 * and processes commands for items received from my.openHAB.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to new Jetty client and ESH APIs
 */

public class MyOpenHABService implements PersistenceService, ActionService, MyOpenHABClientListener, EventSubscriber {

    private Logger logger = LoggerFactory.getLogger(MyOpenHABService.class);

    private static final String SECRET_FILE_NAME = "myopenhab" + File.separator + "secret";

    public static String myohVersion = null;
    private MyOpenHABClient myOHClient;
    private boolean persistenceEnabled = false;
    protected ItemRegistry itemRegistry = null;
    protected EventPublisher eventPublisher = null;

    public MyOpenHABService() {
    }

    /**
     * This method sends notification message to mobile app through my.openHAB service
     *
     * @param userId the {@link String} containing my.openHAB user id to send message to
     * @param message the {@link String} containing a message to send to specified user id
     * @param icon the {@link String} containing a name of the icon to be used with this notification
     * @param severity the {@link String} containing severity (good, info, warning, error) of notification
     */
    public void sendNotification(String userId, String message, String icon, String severity) {
        logger.debug("Sending message '{}' to user id {}", message, userId);
        myOHClient.sendNotification(userId, message, icon, severity);
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
        myOHClient.sendLogNotification(message, icon, severity);
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
        myOHClient.sendBroadcastNotification(message, icon, severity);
    }

    /**
     * This method sends SMS message to mobile phones through my.openHAB service
     *
     * @param userId the {@link String} containing my.openHAB user id to send message to
     * @param message the {@link String} containing a message to send to specified user id
     */
    public void sendSMS(String phone, String message) {
        logger.debug("Sending SMS '" + message + "' to phone # " + phone);
        myOHClient.sendSMS(phone, message);
    }

    protected void activate(BundleContext context, Map<String, ?> config) {
        myohVersion = StringUtils.substringBefore(context.getBundle().getVersion().toString(), ".qualifier");
        logger.debug("my.openHAB service activated");
        modified(config);
    }

    protected void deactivate() {
        logger.debug("my.openHAB service deactivated");
        myOHClient.shutdown();
    }

    protected void modified(Map<String, ?> config) {
        if (config != null) {
            persistenceEnabled = "persistence".equals(config.get("mode"));
        } else {
            logger.debug("config is null");
        }
        logger.debug("UUID = " + InstanceUUID.get() + ", secret = " + getSecret());
        myOHClient = new MyOpenHABClient(InstanceUUID.get(), getSecret());
        myOHClient.setOpenHABVersion(OpenHAB.getVersion());
        myOHClient.connect();
        myOHClient.setListener(this);
        MyOpenHABAction.myOpenHABService = this;
    }

    @Override
    public String getActionClassName() {
        return MyOpenHABAction.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return MyOpenHABAction.class;
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
            logger.error("Couldn't create file '" + file.getPath() + "'.", e);
        } catch (IOException e) {
            logger.error("Couldn't write to file '" + file.getPath() + "'.", e);
        }
    }

    /**
     * Creates a random secret and writes it to the <code>userdata/myopenhab</code>
     * directory. An existing <code>secret</code> file won't be overwritten.
     * Returns either existing secret from the file or newly created secret.
     */

    private String getSecret() {
        File file = new File(ConfigConstants.getUserDataFolder() + File.separator + SECRET_FILE_NAME);
        String newSecretString = "";

        if (!file.exists()) {
            newSecretString = RandomStringUtils.randomAlphanumeric(20);
            logger.debug("New secret = " + newSecretString);
            writeFile(file, newSecretString);
        } else {
            newSecretString = readFirstLine(file);
            logger.debug("Using secret at '{}' with content '{}'", file.getAbsolutePath(), newSecretString);
        }

        return newSecretString;
    }

    /*
     * @see org.openhab.io.myopenhab.internal.MyOHClientListener#sendCommand(java.lang.String, java.lang.String)
     */

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
            logger.warn("Received my.openHAB command for a non-existent item '{}'", itemName);
        }
    }

    @Override
    public void store(Item item) {
        persistenceEnabled = true;
        myOHClient.sendItemUpdate(item.getName(), item.getState().toString());
    }

    @Override
    public void store(Item item, String alias) {
        persistenceEnabled = true;
        myOHClient.sendItemUpdate(item.getName(), item.getState().toString());
    }

    @Override
    public String getId() {
        return "myopenhab";
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
        if (!persistenceEnabled) {
            ItemStateEvent ise = (ItemStateEvent) event;
            myOHClient.sendItemUpdate(ise.getItemName(), ise.getItemState().toString());
        }
    }

    @Override
    public String getLabel(Locale locale) {
        return "my.openHAB";
    }
}
