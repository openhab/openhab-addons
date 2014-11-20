/**
 * Copyright (c) 2010-2014, openHAB.org and others.
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
			FrameImpl frame = (FrameImpl) SitemapFactory.eINSTANCE.createFrame();

			sitemap.setLabel("Home");
			sitemap.setName("default");
			sitemap.getChildren().add(frame);

			for(Item item : itemRegistry.getAll()) {
				if(item instanceof GroupItem && !item.getName().equals("Things") 
						&& (item.getGroupNames().isEmpty() || item.getGroupNames().contains("Things"))) {
					GroupImpl group = (GroupImpl) SitemapFactory.eINSTANCE.createGroup();
					group.setItem(item.getName());
					frame.getChildren().add(group);
				}
			}
						
			return sitemap;
		}
		return null;
	}

}
