package org.openhab.binding.restify.internal.servlet;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointRegistry implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(EndpointRegistry.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Endpoint, Response> registry = new HashMap<>();

    public Optional<Response> find(String path, DispatcherServlet.Method method) {
        lock.readLock().lock();
        try {
            logger.debug("Finding {}:{}", method, path);
            return Optional.ofNullable(registry.get(new Endpoint(method, path)));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void register(String path, DispatcherServlet.Method method, @Nullable Response response) {
        lock.writeLock().lock();
        try {
            logger.debug("Registering {}:{}", method, path);
            var key = new Endpoint(method, path);
            if (registry.containsKey(key)) {
                throw new IllegalStateException("Duplicate key found! key: %s:%s".formatted(method, path));
            }
            registry.put(key, response);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unregister(String path, DispatcherServlet.Method method) {
        lock.writeLock().lock();
        try {
            logger.debug("Unregistering {}:{}", method, path);
            var remove = registry.remove(new Endpoint(method, path));
            if (remove == null) {
                logger.warn("Trying to unregister non existing endpoint: {}:{}", method, path);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private record Endpoint(DispatcherServlet.Method method, String path) {
    }
}
