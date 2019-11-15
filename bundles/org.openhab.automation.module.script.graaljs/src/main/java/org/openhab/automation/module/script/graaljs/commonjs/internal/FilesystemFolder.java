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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

/**
 * File folder implementation
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class FilesystemFolder extends AbstractFolder {
  private File root;
  private String encoding;

  private FilesystemFolder(File root, Optional<Folder> parent, String path, String encoding) {
    super(parent, path);
    this.root = root;
    this.encoding = encoding;
  }

  @Override
  public Optional<String> tryReadFile(String name) {
    File file = new File(root, name);

    try {
      if(file.exists() && file.isFile()) {
        try (FileInputStream stream = new FileInputStream(file)) {
          return Optional.of(IOUtils.toString(stream, encoding));
        }
      } else {
        return Optional.empty();
      }
    } catch (IOException ex) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Folder> getFolder(String name) {
    File folder = new File(root, name);

    if (folder.exists()) {
      return Optional.of(new FilesystemFolder(folder, Optional.of(this), getPath() + name + File.separator, encoding));
    } else {
      return Optional.empty();
    }
  }

  public static FilesystemFolder create(File root, String encoding) {
    File absolute = root.getAbsoluteFile();
    return new FilesystemFolder(absolute, Optional.empty(), absolute.getPath() + File.separator, encoding);
  }
}
