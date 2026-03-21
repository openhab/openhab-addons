package org.openhab.binding.smartthings.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(scope = ServiceScope.PROTOTYPE, service = SmartThingsActions.class)
@ThingActionsScope(name = "smartthings") // Your bindings id is usually the scope
@NonNullByDefault
public class SmartThingsActions implements ThingActions {
    private @Nullable SmartThingsThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (SmartThingsThingHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @ActionOutput(name = "topic", type = "String", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc")
    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public String setFade(
            @ActionInput(name = "state", label = "state", description = "State of the fade effect : Run/Stop") @Nullable String state,
            @ActionInput(name = "fadeType", label = "fadeType", description = "The type of fade: WakeUp/WakeDown") @Nullable String fadeType,
            @ActionInput(name = "duration", label = "duration", description = "The duration of the fade effect") int duration,
            @ActionInput(name = "colorTemperature", label = "colorTemperature", description = "The colorTemperature") int colorTemperature) {

        Map<String, String> properties = handler.getThing().getProperties();
        String deviceId = properties.get("deviceId");
        if (deviceId == null) {
            return "Missing device id";
        }

        String jsonMsg = "";
        jsonMsg += "{";
        jsonMsg += "    commands:";
        jsonMsg += "        [";
        jsonMsg += "            {";
        jsonMsg += "                component: \"main\",";
        jsonMsg += "                capability: \"synthetic.lightingEffectFade\",";
        jsonMsg += "                command:\"setFade\",";
        jsonMsg += "                arguments:";
        jsonMsg += "                [";
        jsonMsg += "                    {";
        jsonMsg += "                        \"duration\":" + duration + ",";
        jsonMsg += "                        \"fadeType\":\"" + fadeType + "\",";
        jsonMsg += "                        \"state\":\"" + state + "\",";
        jsonMsg += "                        \"effects\":";
        jsonMsg += "                        [";
        jsonMsg += "                            {";
        jsonMsg += "                                \"capability\":\"switchLevel\",";
        jsonMsg += "                                \"start\":95,";
        jsonMsg += "                                \"end\":10";
        jsonMsg += "                            },";
        jsonMsg += "                            {";
        jsonMsg += "                                \"capability\":\"colorTemperature\",";
        jsonMsg += "                                \"start\":" + colorTemperature + ",";
        jsonMsg += "                                \"end\":2000";
        jsonMsg += "                            }";
        jsonMsg += "                        ]";
        jsonMsg += "                    }";
        jsonMsg += "                ]";
        jsonMsg += "            }";
        jsonMsg += "        ]";
        jsonMsg += "}";

        SmartThingsApi api = handler.getApi();
        try {
            api.sendCommand(deviceId, jsonMsg);
        } catch (SmartThingsException e) {

        }
        return "test";
    }

    public static String setFade(@Nullable ThingActions actions, @Nullable String state, @Nullable String fadeType,
            int duration, int colorTemperature) {
        if (actions instanceof SmartThingsActions) {
            return ((SmartThingsActions) actions).setFade(state, fadeType, duration, colorTemperature);
        } else {
            throw new IllegalArgumentException("Instance is not an MQTTActions class.");
        }
    }
}
