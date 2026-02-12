package org.openhab.binding.restify.internal.config;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;
import static org.openhab.binding.restify.internal.config.ConfigLoader.GENERAL_CONFIG_FILE_NAME;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class ConfigWatcher implements AutoCloseable, Serializable {
    private static final Duration RELOAD_DEBOUNCE = Duration.ofMillis(300);
    @Serial
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(ConfigWatcher.class);
    private final AtomicReference<Config> config;

    private final Path configDir;
    private final ConfigLoader loader;
    private final ConfigParser parser;
    private final WatchService watchService;
    private final ScheduledExecutorService scheduler;

    private @Nullable volatile ScheduledFuture<?> pendingReload;

    @Activate
    public ConfigWatcher(@Reference ConfigLoader loader, @Reference ConfigParser parser)
            throws ConfigException, IOException, ConfigParseException {
        this.configDir = Path.of(OpenHAB.getConfigFolder()).resolve(BINDING_ID);
        validateConfigDir(this.configDir);

        this.loader = loader;
        this.parser = parser;

        config = new AtomicReference<>(requireNonNull(loadConfig().orElse(Config.EMPTY)));

        this.scheduler = ThreadPoolManager.getScheduledPool(BINDING_ID);

        watchService = FileSystems.getDefault().newWatchService();
        configDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
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

    private Optional<Config> loadConfig() throws ConfigException, ConfigParseException {
        logger.info("Loading config...");
        var load = loader.load(configDir);
        if (load.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parser.parseConfig(load.get()));
    }

    private void watchLoop() {
        var done = false;
        while (!done) {
            try {
                var key = watchService.take(); // blocking
                for (var event : key.pollEvents()) {
                    var kind = event.kind();
                    if (kind == OVERFLOW) {
                        logger.debug("Overflow event detected");
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    var pathEvent = (WatchEvent<Path>) event;
                    var child = configDir.resolve(pathEvent.context());

                    logger.debug("Config change detected: {} {}", kind.name(), child);

                    // Only react to config changes
                    if (!isDirectory(child) && child.getFileName().toString().equals(GENERAL_CONFIG_FILE_NAME)) {
                        scheduleReload();
                    }
                }
            } catch (InterruptedException ignored) {
                logger.debug("Interrupted while watching config");
                Thread.currentThread().interrupt();
                done = true;
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
                loadConfig().ifPresent(newConfig -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("New config loaded successfully. Config={}", newConfig);
                    } else {
                        logger.info("New config loaded successfully.");
                    }
                    config.set(newConfig);
                });
            } catch (Exception e) {
                logger.error("Config reload failed â€“ keeping previous config", e);
            }
        }, RELOAD_DEBOUNCE.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Deactivate
    @Override
    public void close() throws Exception {
        logger.info("Closing ConfigWatcher for dir {}...", configDir.toAbsolutePath());
        watchService.close();
    }

    public Config currentConfig() {
        return config.get();
    }
}
