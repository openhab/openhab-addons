package org.openhab.binding.restify.internal;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.util.Hashtable;

import javax.servlet.ServletException;

import org.jspecify.annotations.NonNull;
import org.openhab.binding.restify.internal.config.Config;
import org.openhab.binding.restify.internal.config.ConfigContent;
import org.openhab.binding.restify.internal.config.ConfigException;
import org.openhab.binding.restify.internal.config.ConfigLoader;
import org.openhab.binding.restify.internal.config.ConfigParser;
import org.openhab.binding.restify.internal.config.JsonSchemaValidator;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

@Component(service = RestifyEndpoint.class, immediate = true)
public class RestifyEndpoint {
    private final HttpService httpService;

    @Activate
    public RestifyEndpoint(@Reference HttpService httpService, @Reference ItemRegistry itemRegistry,
            @Reference ThingRegistry thingRegistry) throws ServletException, NamespaceException {
        this.httpService = httpService;

        var processor = buildProcessor(itemRegistry, thingRegistry);

        httpService.registerServlet(BINDING_ID, new DispatcherServlet(processor, new JsonEncoder()), new Hashtable<>(),
                httpService.createDefaultHttpContext());
    }

    private static @NonNull RequestProcessor buildProcessor(ItemRegistry itemRegistry, ThingRegistry thingRegistry) {
        var engine = new Engine(itemRegistry, thingRegistry);
        var validator = new JsonSchemaValidator();
        var configLoader = new ConfigLoader(validator);
        var configParser = new ConfigParser();
        var configContent = configLoader.load();
        var config = parseConfig(configParser, configContent);
        return new RequestProcessor(config, engine);
    }

    private static Config parseConfig(ConfigParser configParser, ConfigContent configContent) {
        try {
            return configParser.parse(configContent);
        } catch (ConfigException e) {
            throw new IllegalStateException("Cannot parse RESTify config", e);
        }
    }

    @Deactivate
    public void deactivate() {
        httpService.unregister(BINDING_ID);
    }
}
