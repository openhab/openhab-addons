/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.folder;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.openhab.binding.knx.ets.KNXProjectProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is able to observe multiple folders for changes and notifies KNXProjectProviders to parse any knxproj
 * file.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@Component(immediate = true)
public class KNXFolderObserver extends AbstractWatchService {

    private final Logger logger = LoggerFactory.getLogger(KNXFolderObserver.class);

    private ArrayList<KNXProjectProvider> knxProjectProviders = new ArrayList<KNXProjectProvider>();

    private static final String FILE_DIRECTORY = "knx";
    private static final String FILE_EXTENSION = "knxproj";

    public KNXFolderObserver() {
        super(ConfigConstants.getConfigFolder() + File.separator + FILE_DIRECTORY);
        logger.trace("KNXFolderObserver::KNXFolderObserver()");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addKNXProjectProvider(KNXProjectProvider provider) {
        logger.trace("KNXFolderObserver::addKNXProjectProvider");
        knxProjectProviders.add(provider);
        importProjects(provider, new File(pathToWatch));
    }

    protected void removeKNXProjectProvider(KNXProjectProvider provider) {
        logger.trace("KNXFolderObserver::removeKNXProjectProvider");
        knxProjectProviders.remove(provider);
    }

    @Override
    protected boolean watchSubDirectories() {
        logger.trace("KNXFolderObserver::watchSubDirectories");
        return true;
    }

    @Override
    protected Kind<?>[] getWatchEventKinds(Path subDir) {
        logger.trace("KNXFolderObserver::getWatchEventKinds");
        return new Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
        logger.trace("KNXFolderObserver::processWatchEvent");
        File file = path.toFile();
        if (!file.isHidden()) {
            for (KNXProjectProvider aProvider : knxProjectProviders) {
                checkProject(aProvider, file, kind);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkProject(KNXProjectProvider knxProjectProvider, final File file, Kind kind) {
        logger.trace("KNXFolderObserver::checkProject");
        if (knxProjectProvider != null && file != null) {
            try {
                synchronized (KNXFolderObserver.class) {
                    if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
                            && FILE_EXTENSION.equals(getExtension(file.getName()))) {
                        knxProjectProvider.addOrRefreshProject(file);
                    } else if (kind == ENTRY_DELETE) {
                        knxProjectProvider.removeProject(file);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(KNXFolderObserver.class).warn("Cannot open file '{}' for reading. '{}'",
                        file.getAbsolutePath(), e.getMessage());
            }
        }
    }

    private void importProjects(KNXProjectProvider knxProjectProvider, File file) {
        logger.trace("KNXFolderObserver::importProjects");
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isHidden()) {
                        importProjects(knxProjectProvider, f);
                    }
                }
            } else {
                checkProject(knxProjectProvider, file, ENTRY_CREATE);
            }
        }
    }

    public String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
