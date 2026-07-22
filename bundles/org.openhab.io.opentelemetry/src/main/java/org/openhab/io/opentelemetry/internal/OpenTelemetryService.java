/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.opentelemetry.internal;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.monitor.MeterRegistryProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * The {@link OpenTelemetryService} class manages the OpenTelemetry SDK and exports logs, metrics
 * and traces via OTLP.
 *
 * @author Florian Hotze - Initial contribution
 * @author Florian Lettner - Add OTLP metrics and traces
 */
@Component(configurationPid = "org.openhab.opentelemetry", immediate = true, service = OpenTelemetryService.class)
@ConfigurableService(category = "io", label = "OpenTelemetry Service", description_uri = "io:opentelemetry")
@NonNullByDefault
public class OpenTelemetryService {
    private final Logger logger = LoggerFactory.getLogger(OpenTelemetryService.class);

    /** Stable within the JVM lifetime; does not change across {@code modified()} reconfigurations. */
    static final String SERVICE_INSTANCE_ID = UUID.randomUUID().toString();

    private @Nullable BundleContext bundleContext;

    // Logs + traces pipeline (OTel SDK)
    private @Nullable OpenTelemetrySdk openTelemetrySdk;
    private @Nullable OpenTelemetryLogListener logListener;
    private @Nullable ServiceRegistration<EventSubscriber> eventListenerRegistration;

    // References
    private @Nullable LogReaderService logReaderService;
    private @Nullable MeterRegistryProvider meterRegistryProvider;

    // Metrics pipeline (Micrometer OTLP registry)
    private @Nullable OtlpMeterRegistry otlpMeterRegistry;

    @Reference
    protected void setLogReaderService(LogReaderService logReaderService) {
        this.logReaderService = logReaderService;
    }

    protected void unsetLogReaderService(LogReaderService logReaderService) {
        this.logReaderService = null;
    }

    @Reference
    protected void setMeterRegistryProvider(MeterRegistryProvider meterRegistryProvider) {
        this.meterRegistryProvider = meterRegistryProvider;
    }

    protected void unsetMeterRegistryProvider(MeterRegistryProvider meterRegistryProvider) {
        this.meterRegistryProvider = null;
    }

    @Activate
    protected void activate(BundleContext bundleContext, Map<String, Object> config) {
        this.bundleContext = bundleContext;
        updateConfig(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        updateConfig(config);
    }

    @Deactivate
    protected void deactivate() {
        shutdownAll();
        this.bundleContext = null;
    }

    private void updateConfig(Map<String, Object> configMap) {
        OpenTelemetryConfiguration config = ConfigUtil.resolveVariables(new Configuration(configMap))
                .as(OpenTelemetryConfiguration.class);
        logger.debug("Updating OpenTelemetry configuration: {}", config);

        shutdownAll();

        if (config.otlpURL.isBlank()) {
            logger.debug("OpenTelemetry is disabled: No URL configured.");
            return;
        }

        if (config.otlpURL.startsWith("http://")) {
            logger.warn(
                    "OpenTelemetry OTLP endpoint '{}' uses cleartext HTTP. Use HTTPS in production to protect credentials in transit.",
                    config.otlpURL);
        }

        initializeSdk(config);
        initializeMetrics(config);
    }

    // -------------------------------------------------------------------------
    // Shared resource / attribute helpers
    // -------------------------------------------------------------------------

    /** Builds service attributes used by both the OTel SDK resource and micrometer OtlpConfig. */
    private Map<String, String> buildServiceAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("service.name", "openHAB");
        attrs.put("service.namespace", "org.openhab");
        attrs.put("service.version", OpenHAB.getVersion());
        attrs.put("service.instance.id", SERVICE_INSTANCE_ID);
        try {
            attrs.put("host.name", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            // hostname not resolvable — omit
        }
        String osName = System.getProperty("os.name");
        if (osName != null) {
            attrs.put("os.name", osName);
        }
        String osVersion = System.getProperty("os.version");
        if (osVersion != null) {
            attrs.put("os.version", osVersion);
        }
        return attrs;
    }

    private Resource getOtlpResource() {
        ResourceBuilder builder = Resource.getDefault().toBuilder();
        buildServiceAttributes().forEach(builder::put);
        return builder.build();
    }

    Map<String, String> parseOtlpHeaders(@Nullable String rawHeaders) {
        if (rawHeaders == null || rawHeaders.isBlank()) {
            return Collections.emptyMap();
        }

        // Reject control characters to prevent HTTP header injection attacks
        for (int i = 0; i < rawHeaders.length(); i++) {
            char c = rawHeaders.charAt(i);
            if (c == '\n' || c == '\r' || c == '\0') {
                throw new IllegalArgumentException(
                        "otlpHeaders must not contain newline, carriage-return, or null characters");
            }
        }

        Map<String, String> headers = new HashMap<>();

        String[] pairs = rawHeaders.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                if (!key.isEmpty()) {
                    headers.put(key, kv[1].trim());
                }
            }
        }

        return headers;
    }

    // -------------------------------------------------------------------------
    // Logs + traces pipeline (OTel SDK)
    // -------------------------------------------------------------------------

    private @Nullable SdkLoggerProviderBuilder createOtlpLoggerProvider(OpenTelemetryConfiguration config) {
        if (!config.logsEnabled) {
            logger.debug("OpenTelemetry logging is disabled.");
            return null;
        } else if (config.logsEndpoint.isBlank()) {
            logger.warn("OpenTelemetry logging is enabled, but no endpoint is configured.");
            return null;
        }

        OtlpHttpLogRecordExporterBuilder logExporterBuilder = OtlpHttpLogRecordExporter.builder();

        try {
            logExporterBuilder.setEndpoint(config.getLogsURL());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP log endpoint: {}", e.getMessage());
            return null;
        }

        Map<String, String> headers;
        try {
            headers = parseOtlpHeaders(config.otlpHeaders);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP headers: {}", e.getMessage());
            return null;
        }
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                logExporterBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        return SdkLoggerProvider.builder() //
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporterBuilder.build()).build());
    }

    private @Nullable SdkTracerProvider createSdkTracerProvider(OpenTelemetryConfiguration config, Resource resource) {
        if (!config.tracesEnabled) {
            return null;
        }
        if (config.tracesEndpoint.isBlank()) {
            logger.warn("OpenTelemetry traces is enabled, but no endpoint is configured.");
            return null;
        }

        String tracesUrl;
        try {
            tracesUrl = config.getTracesURL();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP traces endpoint: {}", e.getMessage());
            return null;
        }

        OtlpHttpSpanExporterBuilder spanExporterBuilder = OtlpHttpSpanExporter.builder().setEndpoint(tracesUrl);
        Map<String, String> headers;
        try {
            headers = parseOtlpHeaders(config.otlpHeaders);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP headers: {}", e.getMessage());
            return null;
        }
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                spanExporterBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        double raw = config.tracesSamplingRatio;
        double ratio = Double.isNaN(raw) ? 1.0 : Math.max(0.0, Math.min(1.0, raw));
        Sampler sampler = Sampler.parentBased(Sampler.traceIdRatioBased(ratio));

        return SdkTracerProvider.builder() //
                .setSampler(sampler) //
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporterBuilder.build()).build()) //
                .setResource(resource) //
                .build();
    }

    /** Returns true when the OTel Java agent is running (detected via JVM arguments). */
    static boolean isAgentPresent() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(arg -> arg.contains("opentelemetry-javaagent"));
    }

    private synchronized void initializeSdk(OpenTelemetryConfiguration config) {
        if (openTelemetrySdk != null) {
            logger.debug("OpenTelemetry SDK already initialized.");
            return;
        }

        Resource resource = getOtlpResource();

        // Build logger provider (nullable if logs disabled)
        SdkLoggerProviderBuilder loggerProviderBuilder = createOtlpLoggerProvider(config);
        SdkLoggerProvider loggerProvider = null;
        if (loggerProviderBuilder != null) {
            loggerProviderBuilder.setResource(resource);
            loggerProvider = loggerProviderBuilder.build();
        }

        // Build tracer provider (nullable if traces disabled or agent present)
        SdkTracerProvider tracerProvider = null;
        Tracer tracer = null;
        if (config.tracesEnabled) {
            if (isAgentPresent()) {
                logger.info(
                        "OTel Java agent detected — event-bus spans will join the agent's pipeline (read-only GlobalOpenTelemetry).");
                tracer = GlobalOpenTelemetry.get().getTracer("org.openhab.io.opentelemetry");
            } else {
                tracerProvider = createSdkTracerProvider(config, resource);
            }
        }

        if (loggerProvider == null && tracerProvider == null) {
            // No SDK providers to configure; if a tracer came from the agent, wire it directly
            if (tracer != null) {
                registerEventListener(tracer);
            }
            return;
        }

        OpenTelemetrySdkBuilder sdkBuilder = OpenTelemetrySdk.builder();
        if (loggerProvider != null) {
            sdkBuilder.setLoggerProvider(loggerProvider);
        }
        if (tracerProvider != null) {
            sdkBuilder.setTracerProvider(tracerProvider);
        }

        OpenTelemetrySdk sdk = sdkBuilder.build();
        this.openTelemetrySdk = sdk;

        // Register log listener
        if (loggerProvider != null) {
            io.opentelemetry.api.logs.Logger otelLogger = sdk.getLogsBridge().get("org.openhab.io.opentelemetry");
            OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);
            this.logListener = listener;
            LogReaderService lrs = this.logReaderService;
            if (lrs != null) {
                lrs.addLogListener(listener);
                logger.debug("OpenTelemetry LogListener registered.");
            }
        }

        // Register event listener for traces
        if (config.tracesEnabled) {
            if (tracer == null && tracerProvider != null) {
                tracer = sdk.getTracer("org.openhab.io.opentelemetry");
            }
            if (tracer != null) {
                registerEventListener(tracer);
            }
        }

        logger.info("OpenTelemetry SDK started (logs={}, traces={}).", config.logsEnabled, config.tracesEnabled);
    }

    private void registerEventListener(Tracer tracer) {
        BundleContext bc = this.bundleContext;
        if (bc == null) {
            logger.warn("BundleContext not available — event-bus listener not registered.");
            return;
        }
        OpenTelemetryEventListener listener = new OpenTelemetryEventListener(tracer);
        this.eventListenerRegistration = bc.registerService(EventSubscriber.class, listener, null);
        logger.debug("OpenTelemetry EventSubscriber registered.");
    }

    private synchronized void shutdownSdk() {
        // Unregister event listener first so no new spans are emitted after shutdown
        ServiceRegistration<EventSubscriber> reg = this.eventListenerRegistration;
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException e) {
                // already unregistered — ignore
            }
            this.eventListenerRegistration = null;
            logger.debug("OpenTelemetry EventSubscriber unregistered.");
        }

        // Unregister log listener
        LogReaderService lrs = this.logReaderService;
        OpenTelemetryLogListener listener = this.logListener;
        if (lrs != null && listener != null) {
            lrs.removeLogListener(listener);
            logger.debug("OpenTelemetry LogListener unregistered.");
        }
        this.logListener = null;

        // Flush in-flight batches (bounded), then shut down
        OpenTelemetrySdk sdk = this.openTelemetrySdk;
        if (sdk != null) {
            sdk.getSdkTracerProvider().forceFlush().join(3, TimeUnit.SECONDS);
            sdk.getSdkLoggerProvider().forceFlush().join(3, TimeUnit.SECONDS);
            sdk.close();
            this.openTelemetrySdk = null;
            logger.info("OpenTelemetry SDK shut down.");
        }
    }

    // -------------------------------------------------------------------------
    // Metrics pipeline (Micrometer OTLP registry)
    // -------------------------------------------------------------------------

    /**
     * MeterFilter prefixes: only openHAB's own meters and standard JVM/process metrics
     * are exported to avoid leaking unrelated add-on meters or host-monitoring data.
     */
    static final String[] ALLOWED_METER_PREFIXES = { "openhab.", "jvm.", "process.", "system.", "executor.", "logback.",
            "http." };

    static boolean isAllowedMeterName(String name) {
        for (String prefix : ALLOWED_METER_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("null") // implementing unannotated Micrometer OtlpConfig interface
    private synchronized void initializeMetrics(OpenTelemetryConfiguration config) {
        if (otlpMeterRegistry != null) {
            logger.debug("OpenTelemetry metrics already initialized.");
            return;
        }
        if (!config.metricsEnabled) {
            logger.debug("OpenTelemetry metrics is disabled.");
            return;
        }

        String metricsUrl;
        try {
            metricsUrl = config.getMetricsURL();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP metrics endpoint: {}", e.getMessage());
            return;
        }

        Duration step;
        try {
            step = Duration.parse(config.metricsInterval);
        } catch (Exception e) {
            logger.warn("Invalid metrics interval '{}', using default PT60S: {}", config.metricsInterval,
                    e.getMessage());
            step = Duration.ofSeconds(60);
        }
        if (step.isZero() || step.isNegative()) {
            logger.warn("metricsInterval '{}' must be positive, using default PT60S", config.metricsInterval);
            step = Duration.ofSeconds(60);
        }

        Map<String, String> attrs = buildServiceAttributes();
        Map<String, String> headers;
        try {
            headers = parseOtlpHeaders(config.otlpHeaders);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid OTLP headers: {}", e.getMessage());
            return;
        }
        final String finalUrl = metricsUrl;
        final Duration finalStep = step;

        OtlpConfig otlpConfig = new OtlpConfig() {
            @Override
            public @Nullable String get(@Nullable String key) {
                return null;
            }

            @Override
            public String url() {
                return finalUrl;
            }

            @Override
            public Duration step() {
                return finalStep;
            }

            @Override
            @NonNullByDefault({})
            public Map<String, String> headers() {
                return headers;
            }

            @Override
            public AggregationTemporality aggregationTemporality() {
                return "DELTA".equals(config.metricsAggregationTemporality) ? AggregationTemporality.DELTA
                        : AggregationTemporality.CUMULATIVE;
            }

            @Override
            @NonNullByDefault({})
            public Map<String, String> resourceAttributes() {
                return attrs;
            }
        };

        OtlpMeterRegistry registry = new OtlpMeterRegistry(otlpConfig, Clock.SYSTEM);
        registry.config().meterFilter(MeterFilter.denyUnless(id -> isAllowedMeterName(id.getName())));

        MeterRegistryProvider provider = this.meterRegistryProvider;
        if (provider != null) {
            CompositeMeterRegistry composite = provider.getOHMeterRegistry();
            composite.add(registry);
            this.otlpMeterRegistry = registry;
            logger.info("OpenTelemetry metrics pipeline started (pushing to {}).", finalUrl);
        } else {
            registry.close();
            logger.warn("MeterRegistryProvider not available — metrics pipeline not started.");
        }
    }

    private synchronized void shutdownMetrics() {
        OtlpMeterRegistry registry = this.otlpMeterRegistry;
        if (registry != null) {
            MeterRegistryProvider provider = this.meterRegistryProvider;
            if (provider != null) {
                provider.getOHMeterRegistry().remove(registry);
            }
            registry.close();
            this.otlpMeterRegistry = null;
            logger.info("OpenTelemetry metrics pipeline shut down.");
        }
    }

    // -------------------------------------------------------------------------
    // Combined teardown
    // -------------------------------------------------------------------------

    private void shutdownAll() {
        shutdownMetrics();
        shutdownSdk();
    }
}
