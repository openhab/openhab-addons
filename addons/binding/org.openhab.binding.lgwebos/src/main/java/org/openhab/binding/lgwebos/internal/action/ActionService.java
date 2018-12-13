package org.openhab.binding.lgwebos.internal.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.annotation.ActionInput;
import org.eclipse.smarthome.automation.annotation.RuleAction;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;

/**
 * This is the automation engine action handler service for the
 * lgwebos action.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@ThingActionsScope(name = "lgwebos")
@Component()
public class ActionService implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ActionService.class);
    private @Nullable LGWebOSHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (LGWebOSHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private enum Button {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        BACK,
        DELETE,
        ENTER,
        HOME,
        OK
    }

    @RuleAction(label = "@text/actionShowToastLabel", description = "@text/actionShowToastDesc")
    public void showToast(
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        showToast(ActionService.class.getResource("/openhab-logo-square.png").toString(), text);
    }

    @RuleAction(label = "@text/actionShowToastWithIconLabel", description = "@text/actionShowToastWithIconLabel")
    public void showToast(
            @ActionInput(name = "icon", label = "@text/actionShowToastInputIconLabel", description = "@text/actionShowToastInputIconDesc") String icon,
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        BufferedImage bi = ImageIO.read(new URL(icon));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); OutputStream b64 = Base64.getEncoder().wrap(os);) {
            ImageIO.write(bi, "png", b64);
            String string = os.toString(StandardCharsets.UTF_8.name());
            getControl(ToastControl.class)
                    .ifPresent(control -> control.showToast(text, string, "png", createResponseListener()));
        }
    }

    @RuleAction(label = "@text/actionLaunchBrowserLabel", description = "@text/actionLaunchBrowserDesc")
    public void launchBrowser(
            @ActionInput(name = "url", label = "@text/actionLaunchBrowserInputUrlLabel", description = "@text/actionLaunchBrowserInputUrlDesc") String url) {
        getControl(Launcher.class).ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    @RuleAction(label = "@text/actionLaunchApplicationLabel", description = "@text/actionLaunchApplicationDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId) {
        getControl(Launcher.class).ifPresent(control -> control.launchApp(appId, createResponseListener()));
    }

    @RuleAction(label = "@text/actionLaunchApplicationWithParamLabel", description = "@text/actionLaunchApplicationWithParamDesc")
    public void launchApplicationWithParam(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId,
            @ActionInput(name = "param", label = "@text/actionLaunchApplicationInputParamLabel", description = "@text/actionLaunchApplicationInputParamDesc") Object param) {
        getControl(Launcher.class).ifPresent(control -> control.getAppList(new Launcher.AppListListener() {
            @Override
            public void onError(ServiceCommandError error) {
                logger.warn("error requesting application list: {}.", error == null ? "" : error.getMessage());
            }

            @Override
            public void onSuccess(List<AppInfo> appInfos) {
                Optional<AppInfo> appInfo = appInfos.stream().filter(a -> a.getId().equals(appId)).findFirst();
                if (appInfo.isPresent()) {
                    control.launchAppWithInfo(appInfo.get(), param, createResponseListener());
                } else {
                    logger.warn("TV does not support any app with id: {}.", appId);
                }
            }
        }));
    }

    @RuleAction(label = "@text/actionSendTextLabel", description = "@text/actionSendTextDesc")
    public void sendText(
            @ActionInput(name = "text", label = "@text/actionSendTextInputTextLabel", description = "@text/actionSendTextInputTextDesc") String text) {
        getControl(TextInputControl.class).ifPresent(control -> control.sendText(text));
    }

    @RuleAction(label = "@text/actionSendButtonLabel", description = "@text/actionSendButtonDesc")
    public void sendButton(
            @ActionInput(name = "text", label = "@text/actionSendButtonInputButtonLabel", description = "@text/actionSendButtonInputButtonDesc") String button) {
        try {
            switch (Button.valueOf(button)) {
                case UP:
                    getControl(KeyControl.class).ifPresent(control -> control.up(createResponseListener()));
                    break;
                case DOWN:
                    getControl(KeyControl.class).ifPresent(control -> control.down(createResponseListener()));
                    break;
                case LEFT:
                    getControl(KeyControl.class).ifPresent(control -> control.left(createResponseListener()));
                    break;
                case RIGHT:
                    getControl(KeyControl.class).ifPresent(control -> control.right(createResponseListener()));
                    break;
                case BACK:
                    getControl(KeyControl.class).ifPresent(control -> control.back(createResponseListener()));
                    break;
                case DELETE:
                    getControl(TextInputControl.class).ifPresent(control -> control.sendDelete());
                    break;
                case ENTER:
                    getControl(TextInputControl.class).ifPresent(control -> control.sendEnter());
                    break;
                case HOME:
                    getControl(KeyControl.class).ifPresent(control -> control.home(createResponseListener()));
                    break;
                case OK:
                    getControl(KeyControl.class).ifPresent(control -> control.ok(createResponseListener()));
                    break;
            }
        } catch (IllegalArgumentException ex) {
            logger.warn("{} is not a valid value for button - available are: {}", button,
                    Stream.of(Button.values()).map(b -> b.name()).collect(Collectors.joining(", ")));
        }
    }

    @RuleAction(label = "@text/actionIncreaseChannelLabel", description = "@text/actionIncreaseChannelDesc")
    public void increaseChannel() {
        getControl(TVControl.class).ifPresent(control -> control.channelUp(createResponseListener()));
    }

    @RuleAction(label = "@text/actionDecreaseChannelLabel", description = "@text/actionDecreaseChannelDesc")
    public void decreaseChannel() {
        getControl(TVControl.class).ifPresent(control -> control.channelDown(createResponseListener()));
    }

    private <C extends CapabilityMethods> Optional<C> getControl(Class<C> clazz) {
        if (this.handler == null) {
            logger.warn("LGWebOS ThingHandler is null.");
            return Optional.empty();
        }
        final Optional<ConnectableDevice> connectableDevice = this.handler.getDevice();
        if (!connectableDevice.isPresent()) {
            logger.warn("Device not online.");
            return Optional.empty();
        }
        C control = connectableDevice.get().getCapability(clazz);
        if (control == null) {
            logger.warn("Device does not have the ability: {}", clazz.getName());
            return Optional.empty();
        }
        return Optional.of(control);
    }

    private <O> ResponseListener<O> createResponseListener() {
        return new ResponseListener<O>() {

            @Override
            public void onError(ServiceCommandError error) {
                logger.warn("Response: {}", error == null ? "" : error.getMessage());
            }

            @Override
            public void onSuccess(O object) {
                logger.debug("Response: {}", object == null ? "OK" : object.toString());
            }
        };
    }
}
