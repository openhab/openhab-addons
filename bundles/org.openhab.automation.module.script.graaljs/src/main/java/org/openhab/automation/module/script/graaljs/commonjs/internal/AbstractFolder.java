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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Optional;

/**
 * Simple abstract folder implementation
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractFolder implements Folder {
  private Optional<Folder> parent;
  private String path;

  public Optional<Folder> getParent() {
    return parent;
  }

  public String getPath() {
    return path;
  }

  AbstractFolder(Optional<Folder> parent, String path) {
    this.parent = parent;
    this.path = path;
  }

  public Optional<Folder> resolveChild(String[] elements) {

    Optional<Folder> rv = Optional.of(this);

    for (String name : elements) {
      switch (name) {
        case "":
          throw new IllegalArgumentException();
        case ".":
          continue;
        case "..":
          rv = rv.flatMap(Folder::getParent);
          break;
        default:
          rv = rv.flatMap(x -> x.getFolder(name));
          break;
      }
    }

    return rv;
  }
}
