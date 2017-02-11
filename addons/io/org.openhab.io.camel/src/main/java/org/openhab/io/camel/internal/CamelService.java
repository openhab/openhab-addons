/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.camel.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.UnmarshalException;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.util.jndi.JndiContext;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.service.AbstractWatchQueueReader;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.openhab.io.camel.CamelAction;
import org.openhab.io.camel.internal.config.OpenhabCamelConfiguration;
import org.openhab.io.camel.internal.endpoint.CamelCallback;
import org.openhab.io.camel.internal.endpoint.OpenhabComponent;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * This class starts Apache Camel service and implements interface to communicate with Camel routes.
 * It also acts as a persistence service to send stored values to defined Camel routes
 * and processes commands for items received from Camel routes.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class CamelService implements PersistenceService, ActionService, EventSubscriber, CamelCallback {

    private Logger logger = LoggerFactory.getLogger(CamelService.class);
    private ItemRegistry itemRegistry = null;
    private EventPublisher eventPublisher = null;
    private JndiContext jndiContext = null;
    private CamelContext camelContext = null;
    private OpenhabComponent openhabComponent = null;
    private WatchService watchService = null;
    private ExecutorService taskExecutor = null;
    private Map<String, RoutesDefinition> routeDefinations = new HashMap<String, RoutesDefinition>();
    private OpenhabCamelConfiguration configuration = new OpenhabCamelConfiguration();

    public CamelService() {
    }

    protected void activate(BundleContext context, Map<String, ?> config) {
        logger.debug("Apache Camel service activated with config: {}", config);

        try {
            configuration.applyConfig(config);
        } catch (NumberFormatException e) {
            logger.warn("Configuration contains invalid values, using configuration: {}", configuration.toString());
        }

        logger.debug("Using configuration: {}", configuration.toString());

        startCamel();
        startFolderWatcher();

        taskExecutor = new MyThreadPool(configuration.getCorePoolSize(), configuration.getMaxPoolSize(), 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        CamelAction.camelService = this;
        logger.debug("activate done");
    }

    protected void deactivate() {
        logger.debug("Apache Camel service deactivated");
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        stopFolderWacher();
        stopCamel();
        logger.debug("deactivate done");
    }

    protected synchronized void modified(BundleContext context, Map<String, ?> config) {
        logger.debug("Apache Camel service modified");
        deactivate();
        activate(context, config);
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
    public String getId() {
        return "camel";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Camel persistence";
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return ImmutableSet.of(ItemStateEvent.TYPE, ItemCommandEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public String getActionClassName() {
        return CamelAction.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return CamelAction.class;
    }

    private void startCamel() {
        try {
            // Create Camel context and Register openHAB Camel component
            jndiContext = new JndiContext();
            camelContext = new DefaultCamelContext(jndiContext);
            openhabComponent = new OpenhabComponent(this);
            camelContext.addComponent("openhab", openhabComponent);
            // Load routes from XML files from "camel" folder
            loadInitialCamelRoutes();
            camelContext.start();
        } catch (Exception e) {
            logger.error("Exception occured during Apache Camel activation", e);
        }
    }

    private void stopCamel() {
        try {
            camelContext.removeComponent("openhab");
            camelContext.stop();
            routeDefinations.clear();
        } catch (Exception e) {
            logger.error("Exception occured during Apache Camel deactivation", e);
        }
    }

    private void loadInitialCamelRoutes() {
        File folder = new File(getFolder());
        logger.debug("Trying to load Camel routes from folder {}", folder);
        final String[] validExtension = { "xml" };
        File[] files = folder.listFiles(new FileExtensionsFilter(validExtension));
        if (files != null) {
            if (files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        logger.info("Load Camel routes from file {} ({})", file.getName(), file.getAbsolutePath());
                        try {
                            RoutesDefinition routes = camelContext
                                    .loadRoutesDefinition(new FileInputStream(file.getAbsolutePath()));
                            // Store route information for later use. When any file on the "camel" folder is
                            // changed, we know which routes should be updated or deleted.
                            routeDefinations.put(file.getName(), routes);
                            camelContext.addRouteDefinitions(routes.getRoutes());
                        } catch (Exception e) {
                            logger.error("Exception occured during Camel route loading", e);
                        }
                    }
                }
            } else {
                logger.warn("Can't load any Camel routes as folder {} doesn't contain any xml files.", folder);
            }
        } else {
            logger.warn("Can't load any Camel routes as folder {} doesn't exists.", folder);
        }
    }

    private void startFolderWatcher() {
        String pathToWatch = getFolder();
        Path toWatch = Paths.get(pathToWatch);
        logger.debug("Start folder wacther for folder {}", pathToWatch);

        try {
            watchService = toWatch.getFileSystem().newWatchService();
        } catch (IOException e) {
            logger.error("Error occured during folder watcher initialization", e.getMessage());
        }

        final Map<WatchKey, Path> registeredWatchKeys = new HashMap<>();

        try {
            WatchKey registrationKey = toWatch.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            if (registrationKey != null) {
                registeredWatchKeys.put(registrationKey, toWatch);
            } else {
                logger.info("The directory '{}' was not registered in the watch service", toWatch);
            }
        } catch (NoSuchFileException e) {
            logger.error("Can't register folder watcher as folder '{}' doesn't exixts", pathToWatch);
        } catch (IOException e) {
            logger.error("Error occured during folder watcher register", e);
        }

        AbstractWatchQueueReader reader = new WatchQueueReader(watchService, toWatch, registeredWatchKeys);
        Thread folderWatcher = new Thread(reader, "Camel Folder Watcher");
        folderWatcher.start();
    }

    private void stopFolderWacher() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warn("Cannot deactivate folder watcher", e);
            }
        }
    }

    private String getFolder() {
        String folder = configuration.getFolderName();
        Path p = Paths.get(folder);
        if (p.isAbsolute()) {
            logger.debug("Configuration folder is absolute path, return '{}'", folder);
            return folder;
        }

        folder = ConfigConstants.getConfigFolder() + File.separator + configuration.getFolderName();
        logger.debug("Configuration folder is relative path, return full path '{}'", folder);
        return folder;
    }

    @Override
    public void sendCommand(String itemName, String commandString) {
        logger.debug("sendCommand: itemName='{}', commandString='{}'", itemName, commandString);
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
            logger.warn("Received Camel command for a non-existent item '{}'", itemName);
        }
    }

    @Override
    public void sendStatusUpdate(String itemName, String statusUpdateString) {
        logger.debug("sendStatusUpdate: itemName='{}', commandString='{}'", itemName, statusUpdateString);
        try {
            if (itemRegistry != null) {
                Item item = itemRegistry.getItem(itemName);
                State state = null;
                if (item != null) {
                    if (this.eventPublisher != null) {
                        state = TypeParser.parseState(item.getAcceptedDataTypes(), statusUpdateString);
                        if (state != null) {
                            logger.debug("Received update '{}' for item '{}'", statusUpdateString, itemName);
                            this.eventPublisher.post(ItemEventFactory.createStateEvent(itemName, state));
                        } else {
                            logger.warn("Received invalid update '{}' for item '{}'", statusUpdateString, itemName);
                        }
                    }
                } else {
                    logger.warn("Received update '{}' for non-existent item '{}'", statusUpdateString, itemName);
                }
            } else {
                return;
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Received Camel update for a non-existent item '{}'", itemName);
        }
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received event: {}", event);

        if (event instanceof ItemStateEvent) {
            logger.debug("Create item update task: event={}", event);
            OneShotUpdateTask task = new OneShotUpdateTask(event);
            taskExecutor.submit(task);
        } else if (event instanceof ItemCommandEvent) {
            logger.debug("Create item command task: event={}", event);
            OneShotCommandTask task = new OneShotCommandTask(event);
            taskExecutor.submit(task);
        }
        logger.debug("done");
    }

    @Override
    public void store(Item item) {
        logger.debug("Create item store task: item={}", item);
        OneShotStoreTask task = new OneShotStoreTask(item);
        taskExecutor.submit(task);
        logger.debug("done");
    }

    @Override
    public void store(Item item, String alias) {
        logger.debug("Create item store task: item={}, alias={}", item, alias);
        OneShotStoreTask task = new OneShotStoreTask(item);
        taskExecutor.submit(task);
        logger.debug("done");
    }

    /**
     * This method sends direct message to Camel route.
     *
     * @param actionId the {@link String} contains action ID to send message correct Camel route
     * @param headers the {@link String} contains headers to include in Camel message
     * @param message the {@link String} contains a message to send
     */
    public void sendCamelAction(String actionId, Map<String, Object> headers, String message) {
        logger.debug("Create action task: message '{}' to route '{}' with headers '{}'", message, actionId, headers);
        OneShotActionTask task = new OneShotActionTask(actionId, headers, message);
        taskExecutor.submit(task);
        logger.debug("done");
    }

    /**
     * Task for send item updates to Camel routes.
     */
    private class OneShotUpdateTask implements Runnable {
        private Event event;

        OneShotUpdateTask(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            ItemStateEvent ise = (ItemStateEvent) event;
            openhabComponent.sendItemUpdate(ise.getItemName(), ise.getItemState().toString());
        }
    }

    /**
     * Task for send item commands to Camel routes.
     */
    private class OneShotCommandTask implements Runnable {
        private Event event;

        OneShotCommandTask(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            ItemCommandEvent ise = (ItemCommandEvent) event;
            openhabComponent.sendItemCommand(ise.getItemName(), ise.getItemCommand().toString());
        }
    }

    /**
     * Task for send item persistence updates to Camel routes.
     */
    private class OneShotStoreTask implements Runnable {
        private Item item;

        OneShotStoreTask(Item item) {
            this.item = item;
        }

        @Override
        public void run() {
            openhabComponent.storeItem(item.getName(), item.getState().toString());
        }
    }

    /**
     * Task for send actions from automation rules to Camel routes.
     */
    private class OneShotActionTask implements Runnable {
        private String actionId;
        private Map<String, Object> headers;
        private String message;

        OneShotActionTask(String actionId, Map<String, Object> headers, String message) {
            this.actionId = actionId;
            this.headers = headers;
            this.message = message;
        }

        @Override
        public void run() {
            openhabComponent.sendAction(actionId, headers, message);
        }
    }

    protected class FileExtensionsFilter implements FilenameFilter {

        private String[] validExtensions;

        public FileExtensionsFilter(String[] validExtensions) {
            this.validExtensions = validExtensions;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (validExtensions != null && validExtensions.length > 0) {
                for (String extension : validExtensions) {
                    if (name.toLowerCase().endsWith("." + extension)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Class to watch camel route folder changes.
     */
    private class WatchQueueReader extends AbstractWatchQueueReader {

        public WatchQueueReader(WatchService watchService, Path dir, Map<WatchKey, Path> registeredKeys) {
            super(watchService, dir, registeredKeys);
        }

        @Override
        protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
            logger.debug("Event '{}' to File '{}' received", kind.toString(), path);

            if (kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
                RoutesDefinition currentRoutes = routeDefinations.get(path.toString());

                if (currentRoutes != null) {
                    logger.debug("Remove current Camel routes from file {}", path);
                    try {
                        camelContext.removeRouteDefinitions(currentRoutes.getRoutes());
                        routeDefinations.remove(path.toString());
                    } catch (Exception e) {
                        logger.error("Error occured during '{}' remove", path, e);
                    }
                }
            }

            if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                logger.debug("Load new Camel routes from file {}", path);

                try {
                    String fullPath = getFolder() + File.separator + path.toString();
                    RoutesDefinition routes = camelContext.loadRoutesDefinition(new FileInputStream(fullPath));
                    camelContext.addRouteDefinitions(routes.getRoutes());
                    routeDefinations.put(path.toString(), routes);
                } catch (UnmarshalException e) {
                    logger.error("File '{}' is not a valid xml file", path);
                } catch (Exception e) {
                    logger.error("Error occured during '{}' loading", path, e);
                }
            }
        }
    }

    /**
     * Special thread pool executor, which watch task execution times
     * and show some statistics about all executed tasks.
     */
    private class MyThreadPool extends ThreadPoolExecutor {

        private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
        private final AtomicLong numTasks = new AtomicLong();
        private final AtomicLong totalTime = new AtomicLong();

        public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            logger.debug(String.format("Thread %s: start task %s", t, r));
            startTime.set(System.currentTimeMillis());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            try {
                long endTime = System.currentTimeMillis();
                long taskTime = endTime - startTime.get();
                numTasks.incrementAndGet();
                totalTime.addAndGet(taskTime);
                logger.debug(String.format("Thread %s: end task %s, time = %dms", t, r, taskTime));
                logger.debug("ThreadPoolExecutor stats: {}", this.toString());
            } finally {
                super.afterExecute(r, t);
            }
        }

        @Override
        protected void terminated() {
            logger.debug("ThreadPoolExecutor terminated, stats: {}", this.toString());
        }

        @Override
        public String toString() {
            if (numTasks.get() == 0) {
                return super.toString();
            } else {
                return super.toString() + "[avg exec time = " + totalTime.get() / numTasks.get() + "ms]";
            }
        }
    }
}
