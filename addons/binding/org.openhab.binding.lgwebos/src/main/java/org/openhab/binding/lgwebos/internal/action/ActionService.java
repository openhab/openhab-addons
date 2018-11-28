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

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.annotation.ActionScope;
import org.eclipse.smarthome.core.thing.binding.AnnotatedActionThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lgwebos.LGWebOS;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
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
@ActionScope(name = "binding.lgwebos")
@Component(immediate = false, service = { AnnotatedActionThingHandlerService.class })
@NonNullByDefault
public class ActionService implements AnnotatedActionThingHandlerService {
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

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") String value) {

        if (handler == null) {
            logger.warn("LGWebOS Action service ThingHandler is null!");
            return;
        }

    }

    public enum Button {
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

    /** Sends a toast message to a WebOS device with openhab icon. */
    public void showToast(String text) {
        showToast(LGWebOS.class.getResource("/openhab-logo-square.png").toString(), text);
    }

    /** Sends a toast message to a WebOS device with custom icon. */
    public void showToast(String icon, String text) throws IOException {

        BufferedImage bi = ImageIO.read(new URL(icon));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); OutputStream b64 = Base64.getEncoder().wrap(os);) {
            ImageIO.write(bi, "png", b64);
            String string = os.toString(StandardCharsets.UTF_8.name());
            getControl(ToastControl.class)
                    .ifPresent(control -> control.showToast(text, string, "png", createResponseListener()));
        }
    }

    /** Opens the given URL in the TV's browser application. */
    public void launchBrowser(String url) {
        getControl(Launcher.class).ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    /** Opens the application with given appId. */
    public void launchApplication(String appId) {
        getControl(Launcher.class).ifPresent(control -> control.launchApp(appId, createResponseListener()));
    }

    /** Opens the application with given appId and passes additional parameters. */
    public void launchApplicationWithParam(String appId, Object param) {
        getControl(Launcher.class).ifPresent(control -> control.getAppList(new Launcher.AppListListener() {
            @Override
            public void onError(@Nullable ServiceCommandError error) {
                logger.warn("error requesting application list: {}.", error == null ? "" : error.getMessage());
            }

            @Override
            public void onSuccess(@Nullable List<AppInfo> appInfos) {
                Optional<AppInfo> appInfo = appInfos.stream().filter(a -> a.getId().equals(appId)).findFirst();
                if (appInfo.isPresent()) {
                    control.launchAppWithInfo(appInfo.get(), param, createResponseListener());
                } else {
                    logger.warn("TV does not support any app with id: {}.", appId);
                }
            }
        }));
    }

    /** Sends a text input to a WebOS device. */
    public void sendText(String thingId, String text) {
        getControl(TextInputControl.class).ifPresent(control -> control.sendText(text));
    }

    /** Sends the button press event to a WebOS device. */
    public void sendButton(String thingId, Button button) {
        switch (button) {
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
    }

    private <C extends CapabilityMethods> Optional<C> getControl(Class<C> clazz) {

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
            public void onError(@Nullable ServiceCommandError error) {
                logger.warn("Response: {}", error == null ? "" : error.getMessage());
            }

            @Override
            public void onSuccess(O object) {
                logger.debug("Response: {}", object == null ? "OK" : object.toString());
            }
        };
    }
}
