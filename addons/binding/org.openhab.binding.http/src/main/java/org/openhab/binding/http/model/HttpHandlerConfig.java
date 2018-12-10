package org.openhab.binding.http.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_COMMAND_METHOD;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_CONTENT_TYPE;
import static org.openhab.binding.http.HttpBindingConstants.DEFAULT_STATE_REFRESH_INTERVAL;

/**
 * A class describing configuration for the HTTP handler.
 *
 * @author Brian J. Tarricone
 */
@NonNullByDefault
public class HttpHandlerConfig {
    /**
     * Enumeration describing the HTTP method.
     */
    public enum Method {
        POST, GET
    }

    /**
     * A class describing configuration for the HTTP request to make when fetching {@link State}.
     *
     * @author Brian J. Tarricone
     */
    @NonNullByDefault
    public static class StateRequest {
        private final URL url;
        private final Duration refreshInterval;
        private final Optional<Transform> responseTransform;

        StateRequest(final URL url, final Duration refreshInterval, final Optional<Transform> responseTransform) {
            this.url = url;
            this.refreshInterval = refreshInterval;
            this.responseTransform = responseTransform;
        }

        public URL getUrl() {
            return url;
        }

        public Duration getRefreshInterval() {
            return refreshInterval;
        }

        public Optional<Transform> getResponseTransform() {
            return responseTransform;
        }
    }

    /**
     * A class describing configuration for the HTTP request to make when sending a {@link Command}.
     *
     * @author Brian J. Tarricone
     */
    @NonNullByDefault
    public static class CommandRequest {
        private final Method method;
        private final URL url;
        private final String contentType;
        private final Optional<Transform> requestTransform;
        private final Optional<Transform> responseTransform;

        CommandRequest(final Method method, final URL url, final String contentType, final Optional<Transform> requestTransform, final Optional<Transform> responseTransform) {
            this.method = method;
            this.url = url;
            this.contentType = contentType;
            this.requestTransform = requestTransform;
            this.responseTransform = responseTransform;
        }

        public Method getMethod() {
            return method;
        }

        public URL getUrl() {
            return url;
        }

        public String getContentType() {
            return contentType;
        }

        public Optional<Transform> getRequestTransform() {
            return requestTransform;
        }

        public Optional<Transform> getResponseTransform() {
            return responseTransform;
        }
    }

    @SuppressWarnings("unused")
    private @Nullable String stateUrl;
    private long stateRefreshInterval = DEFAULT_STATE_REFRESH_INTERVAL.toMillis();
    @SuppressWarnings("unused")
    private @Nullable String stateResponseTransform;

    private String commandMethod = DEFAULT_COMMAND_METHOD.name();
    @SuppressWarnings("unused")
    private @Nullable String commandUrl;
    private String commandContentType = DEFAULT_CONTENT_TYPE;
    @SuppressWarnings("unused")
    private @Nullable String commandRequestTransform;
    @SuppressWarnings("unused")
    private @Nullable String commandResponseTransform;

    public Optional<StateRequest> getStateRequest() throws IllegalArgumentException {
        return Optional.ofNullable(this.stateUrl).map(stateUrl -> {
            try {
                final Optional<Transform> stateResponseTransform = Optional.ofNullable(this.stateResponseTransform)
                        .map(Transform::parse);
                return new StateRequest(new URL(stateUrl), Duration.ofMillis(this.stateRefreshInterval), stateResponseTransform);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid stateUrl: " + e.getMessage());
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid stateResponseTransform: " + e.getMessage(), e);
            }
        });
    }

    public Optional<CommandRequest> getCommandRequest() throws IllegalArgumentException {
        return Optional.ofNullable(this.commandUrl).map(commandUrl -> {
            final Method commandMethod;
            try {
                commandMethod = Method.valueOf(this.commandMethod);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandMethod", e);
            }
            final Optional<Transform> commandRequestTransform;
            try {
                commandRequestTransform = Optional.ofNullable(this.commandRequestTransform)
                        .map(Transform::parse);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandRequestTransform: " + e.getMessage());
            }
            final Optional<Transform> commandResponseTransform;
            try {
                commandResponseTransform = Optional.ofNullable(this.commandResponseTransform)
                        .map(Transform::parse);
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid commandResponseTransform: " + e.getMessage());
            }
            try {
                return new CommandRequest(commandMethod, new URL(commandUrl), this.commandContentType, commandRequestTransform, commandResponseTransform);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid commandUrl: " + e.getMessage());
            }
        });
    }
}
