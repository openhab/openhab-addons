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
package org.openhab.binding.xmppclient.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.java7.XmppHostnameVerifier;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.util.dns.javax.JavaxResolver;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smack.util.stringencoder.Base64UrlSafeEncoder;
import org.jivesoftware.smack.util.stringencoder.java7.Java7Base64Encoder;
import org.jivesoftware.smack.util.stringencoder.java7.Java7Base64UrlSafeEncoder;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.openhab.binding.xmppclient.internal.handler.XMPPClientHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link XMPPClientHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.xmppclient", service = ThingHandlerFactory.class)
public class XMPPClientHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(XMPPClientBindingConstants.BRIDGE_TYPE_XMPP);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(XMPPClientBindingConstants.BRIDGE_TYPE_XMPP)) {
            return new XMPPClientHandler((Bridge) thing);
        }
        return null;
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Roster.setRosterLoadedAtLoginDefault(false);
        DNSUtil.setDNSResolver(JavaxResolver.getInstance());
        SmackConfiguration.setDefaultHostnameVerifier(new XmppHostnameVerifier());
        Base64.setEncoder(Java7Base64Encoder.getInstance());
        Base64UrlSafeEncoder.setEncoder(Java7Base64UrlSafeEncoder.getInstance());
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        ProviderManager.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
    }
}
