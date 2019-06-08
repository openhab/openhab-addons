package org.openhab.binding.amazonechocontrol.internal.statedescription;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_TYPE_LIGHT_COLOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonColors;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * Dynamic channel state description provider
 * Overrides the state description for the colors of the smart bulbs
 *
 * @author Lukas Knoeller
 *
 */

@Component(service = { DynamicStateDescriptionProvider.class, AmazonEchoDynamicStateDescriptionSmartHome.class })
@NonNullByDefault
public class AmazonEchoDynamicStateDescriptionSmartHome implements DynamicStateDescriptionProvider {

    private @Nullable ThingRegistry thingRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable ThingHandler findHandler(Channel channel) {
        ThingRegistry thingRegistry = this.thingRegistry;
        if (thingRegistry == null) {
            return null;
        }
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }
        ThingUID accountThingId = thing.getBridgeUID();
        Thing accountThing = thingRegistry.get(accountThingId);
        if (accountThing == null) {
            return null;
        }
        AccountHandler accountHandler = (AccountHandler) accountThing.getHandler();
        if (accountHandler == null) {
            return null;
        }
        Connection connection = accountHandler.findConnection();
        if (connection == null || !connection.getIsLoggedIn()) {
            return null;
        }
        return thing.getHandler();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        if (originalStateDescription == null) {
            return null;
        }
        ThingRegistry thingRegistry = this.thingRegistry;
        if (thingRegistry == null) {
            return originalStateDescription;
        }
        if (CHANNEL_TYPE_LIGHT_COLOR.equals(channel.getChannelTypeUID())) {
            EchoHandler handler = (EchoHandler) findHandler(channel);
            AccountHandler account = handler.findAccount();
            Connection connection = account.findConnection();
            List<JsonColors> colors = connection.getEchoLightColors();
            ArrayList<StateOption> options = new ArrayList<>();
            options.addAll(originalStateDescription.getOptions());

            for (JsonColors color : colors) {
                if (color != null && color.colorName != null) {
                    options.add(new StateOption(color.colorName, color.colorName));
                }
            }

            StateDescription result = new StateDescription(originalStateDescription.getMinimum(),
                    originalStateDescription.getMaximum(), originalStateDescription.getStep(),
                    originalStateDescription.getPattern(), originalStateDescription.isReadOnly(), options);
            return result;
        }
        return originalStateDescription;
    }

}
