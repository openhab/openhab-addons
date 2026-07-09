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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

/**
 * The {@link OpenTelemetryService} class manages the OpenTelemetry SDK and forwards logs to an OTLP endpoint.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(configurationPid = "org.openhab.opentelemetry", immediate = true, service = OpenTelemetryService.class)
@NonNullByDefault
public class OpenTelemetryService {
    private final Logger logger = LoggerFactory.getLogger(OpenTelemetryService.class);

    private @Nullable OpenTelemetrySdk openTelemetrySdk;
    private @Nullable OpenTelemetryLogListener logListener;
    private @Nullable LogReaderService logReaderService;

    @Reference
    protected void setLogReaderService(LogReaderService logReaderService) {
        this.logReaderService = logReaderService;
    }

    protected void unsetLogReaderService(LogReaderService logReaderService) {
        this.logReaderService = null;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        updateConfig(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        updateConfig(config);
    }

    @Deactivate
    protected void deactivate() {
        shutdownSdk();
    }

    private void updateConfig(Map<String, Object> configMap) {
        OpenTelemetryConfiguration config = ConfigUtil.resolveVariables(new Configuration(configMap))
                .as(OpenTelemetryConfiguration.class);
        logger.debug("Updating OpenTelemetry configuration: {}", config);

        shutdownSdk();

        if (config.otlpURL.isBlank()) {
            logger.debug("OpenTelemetry is disabled: No URL configured.");
            return;
        }

        initializeSdk(config);
    }

    private Resource getOtlpResource() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // ignore
        }

        ResourceBuilder resourceBuilder = Resource.getDefault().toBuilder() //
                // Application Identity
                .put("service.name", "openHAB") //
                .put("service.namespace", "org.openhab") //
                .put("service.version", OpenHAB.getVersion());
        resourceBuilder
                // Host/Infrastructure Metadata
                .put("os.name", System.getProperty("os.name")) //
                .put("os.version", System.getProperty("os.version"));
        if (hostname != null) {
            resourceBuilder.put("host.name", hostname);
        }

        return resourceBuilder.build();
    }

    private Map<String, String> parseOtlpHeaders(@Nullable String rawHeaders) {
        if (rawHeaders == null || rawHeaders.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();

        String[] pairs = rawHeaders.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                headers.put(kv[0].trim(), kv[1].trim());
            }
        }

        return headers;
    }

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

        Map<String, String> headers = parseOtlpHeaders(config.otlpHeaders);
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                logExporterBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        return SdkLoggerProvider.builder() //
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporterBuilder.build()).build());
    }

    private synchronized void initializeSdk(OpenTelemetryConfiguration config) {
        if (openTelemetrySdk != null) {
            logger.debug("OpenTelemetry SDK already initialized.");
            return;
        }

        Resource resource = getOtlpResource();

        SdkLoggerProviderBuilder loggerProviderBuilder = createOtlpLoggerProvider(config);
        SdkLoggerProvider loggerProvider = null;
        if (loggerProviderBuilder != null) {
            loggerProviderBuilder.setResource(resource);
            loggerProvider = loggerProviderBuilder.build();
        }

        if (loggerProvider == null) {
            return;
        }

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setLoggerProvider(loggerProvider).build();
        this.openTelemetrySdk = sdk;

        LogReaderService lrs = this.logReaderService;
        if (config.logsEnabled && lrs != null) {
            io.opentelemetry.api.logs.Logger otelLogger = sdk.getLogsBridge().get("org.openhab.io.opentelemetry");
            OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);
            this.logListener = listener;
            lrs.addLogListener(listener);
            logger.debug("OpenTelemetry LogListener registered.");
        }

        logger.info("OpenTelemetry service started.");
    }

    private synchronized void shutdownSdk() {
        LogReaderService lrs = this.logReaderService;
        OpenTelemetryLogListener listener = this.logListener;
        if (lrs != null && listener != null) {
            lrs.removeLogListener(listener);
            logger.debug("OpenTelemetry LogListener unregistered.");
        }
        this.logListener = null;

        OpenTelemetrySdk sdk = this.openTelemetrySdk;
        if (sdk != null) {
            sdk.close();
            this.openTelemetrySdk = null;
            logger.info("OpenTelemetry service shut down.");
        }
    }
}
