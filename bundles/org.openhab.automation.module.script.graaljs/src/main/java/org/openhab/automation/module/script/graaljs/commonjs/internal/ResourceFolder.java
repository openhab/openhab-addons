/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Folder implementation allowing loading from Resource folders (via a classloader)
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class ResourceFolder extends AbstractFolder {
  private ClassLoader loader;
  private String resourcePath;
  private String encoding;

  @Override
  public Optional<String> tryReadFile(String name) {
    InputStream stream = loader.getResourceAsStream(resourcePath + "/" + name);
    if (stream == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(IOUtils.toString(stream, encoding));
    } catch (IOException ex) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Folder> getFolder(String name) {
    return Optional.of(new ResourceFolder(
        loader, resourcePath + "/" + name, Optional.of(this), getPath() + name + "/", encoding));
  }

  private ResourceFolder(
      ClassLoader loader, String resourcePath, Optional<Folder> parent, String displayPath, String encoding) {
    super(parent, displayPath);
    this.loader = loader;
    this.resourcePath = resourcePath;
    this.encoding = encoding;
  }

  public static ResourceFolder create(ClassLoader loader, String path, String encoding) {
    return new ResourceFolder(loader, path, Optional.empty(), "/", encoding);
  }
}
