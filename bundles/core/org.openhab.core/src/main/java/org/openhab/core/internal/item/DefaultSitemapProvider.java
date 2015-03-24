/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.item;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.impl.FrameImpl;
import org.eclipse.smarthome.model.sitemap.impl.GroupImpl;
import org.eclipse.smarthome.model.sitemap.impl.SitemapImpl;

/**
 * This class dynamically provides a default sitemap which comprises
 * all group items that do not have any parent group.
 * 
 * @author Kai Kreuzer
 *
 */
public class DefaultSitemapProvider implements SitemapProvider {

	private ItemRegistry itemRegistry;

	protected void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	protected void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}

	@Override
	public Sitemap getSitemap(String sitemapName) {
		if(sitemapName.equals("default")) {
			SitemapImpl sitemap = (SitemapImpl) SitemapFactory.eINSTANCE.createSitemap();
			FrameImpl mainFrame = (FrameImpl) SitemapFactory.eINSTANCE.createFrame();

	        FrameImpl thingFrame = (FrameImpl) SitemapFactory.eINSTANCE.createFrame();
	        thingFrame.setLabel("Things");

			sitemap.setLabel("Home");
			sitemap.setName("default");

			for(Item item : itemRegistry.getAll()) {
				if(item instanceof GroupItem && (item.getTags().contains(ThingSetupManager.TAG_HOME_GROUP) || item.getTags().contains(ThingSetupManager.TAG_THING))) {
					GroupImpl group = (GroupImpl) SitemapFactory.eINSTANCE.createGroup();
					group.setItem(item.getName());
					group.setLabel(item.getLabel());
					String category = item.getCategory();
					if(category != null) {
					    group.setIcon(item.getCategory());
					}
					if(item.getTags().contains(ThingSetupManager.TAG_HOME_GROUP)) {
                        mainFrame.getChildren().add(group);
					} else {
                        thingFrame.getChildren().add(group);
					}
				}
			}
			
			if(!mainFrame.getChildren().isEmpty()) {
		         sitemap.getChildren().add(mainFrame);
			}
            if(!thingFrame.getChildren().isEmpty()) {
               sitemap.getChildren().add(thingFrame);
            }
			
			return sitemap;
		}
		return null;
	}

}
