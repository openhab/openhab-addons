package org.openhab.binding.restify.internal.config;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigWatcher implements AutoCloseable, Serializable {
    private static final Duration RELOAD_DEBOUNCE = Duration.ofMillis(300);
    @Serial
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(ConfigWatcher.class);
    private final AtomicReference<Config> config;
    private final Map<WatchKey, @Nullable Path> keyToDir = new ConcurrentHashMap<>();

    private final Path configDir;
    private final ConfigLoader loader;
    private final ConfigParser parser;
    private final WatchService watchService;
    private final ScheduledExecutorService scheduler;

    private volatile ScheduledFuture<?> pendingReload;

    public ConfigWatcher(ScheduledExecutorService scheduler) throws ConfigException, IOException {
        this(new ConfigLoader(new JsonSchemaValidator()), new ConfigParser(), scheduler);
    }

    ConfigWatcher(ConfigLoader loader, ConfigParser parser, ScheduledExecutorService scheduler)
            throws ConfigException, IOException {
        this.configDir = Path.of(OpenHAB.getConfigFolder()).resolve(BINDING_ID);
        validateConfigDir(this.configDir);

        this.loader = loader;
        this.parser = parser;

        config = new AtomicReference<>(loadConfig());

        this.scheduler = scheduler;
        watchService = FileSystems.getDefault().newWatchService();
        configDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        registerAll(configDir);
        scheduler.submit(this::watchLoop);
    }

    private static void validateConfigDir(Path configDir) {
        if (!exists(configDir)) {
            throw new IllegalArgumentException("Config dir does not exist: " + configDir.toAbsolutePath());
        }
        if (!isDirectory(configDir)) {
            throw new IllegalArgumentException(format("%s is not a directory", configDir.toAbsolutePath()));
        }
    }

    private Config loadConfig() throws ConfigException {
        logger.info("Loading config...");
        var load = loader.load(configDir);
        return parser.parse(load);
    }

    private void watchLoop() {
        var done = false;
        while (!Thread.currentThread().isInterrupted() && !done) {
            try {
                var key = watchService.take(); // blocking
                var dir = keyToDir.get(key);

                if (dir == null) {
                    key.reset();
                    continue;
                }

                for (var event : key.pollEvents()) {
                    var kind = event.kind();

                    if (kind == OVERFLOW) {
                        logger.debug("Overflow event detected");
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    var pathEvent = (WatchEvent<Path>) event;
                    var child = dir.resolve(pathEvent.context());

                    logger.debug("Config change detected: {} {}", kind.name(), child);

                    // If new directory created → register recursively
                    if (kind == ENTRY_CREATE) {
                        try {
                            if (isDirectory(child)) {
                                registerAll(child);
                            }
                        } catch (IOException e) {
                            logger.warn("Failed to register new directory: {}", child, e);
                        }
                    }

                    // Only react to JSON changes
                    if (!isDirectory(child) && child.toString().endsWith(".json")) {
                        scheduleReload();
                    }
                }

                var valid = key.reset();
                if (!valid) {
                    keyToDir.remove(key);
                    if (keyToDir.isEmpty()) {
                        logger.warn("All config directories became inaccessible");
                        done = true;
                    }
                }
            } catch (InterruptedException ignored) {
                logger.debug("Interrupted while watching config");
                Thread.currentThread().interrupt();
            } catch (ClosedWatchServiceException ignored) {
                // normal on shutdown
                logger.debug("WatchService closed");
                done = true;
            } catch (Exception e) {
                logger.error("ConfigWatcher loop failed", e);
            }
        }
        logger.info("ConfigWatcher loop terminated");
    }

    private synchronized void scheduleReload() {
        if (pendingReload != null) {
            pendingReload.cancel(false);
        }

        pendingReload = scheduler.schedule(() -> {
            try {
                logger.debug("Reloading Restify config after change...");
                var newConfig = loadConfig();
                config.set(newConfig);
                logger.info("New config loaded successfully. Config={}", newConfig);
            } catch (Exception e) {
                logger.error("Config reload failed – keeping previous config", e);
            }
        }, RELOAD_DEBOUNCE.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void registerAll(Path start) throws IOException {
        walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keyToDir.put(key, dir);
                logger.debug("Watching directory: {}", dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void close() throws Exception {
        logger.info("Closing ConfigWatcher for dir {}...", configDir.toAbsolutePath());
        watchService.close();
    }

    public Config currentConfig() {
        return config.get();
    }
}
