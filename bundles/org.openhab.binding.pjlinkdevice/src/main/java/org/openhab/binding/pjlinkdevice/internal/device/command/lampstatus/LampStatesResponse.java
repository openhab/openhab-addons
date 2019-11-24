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
package org.openhab.binding.pjlinkdevice.internal.device.command.lampstatus;

import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The response part of {@link LampStatesCommand}
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class LampStatesResponse extends PrefixedResponse<List<LampStatesResponse.LampState>> {
  @NonNullByDefault
  public class LampState {
    private boolean active;
    private int lampHours;

    public LampState(boolean active, int lampHours) {
      this.active = active;
      this.lampHours = lampHours;
    }

    public int getLampHours() {
      return lampHours;
    }

    public boolean isActive() {
      return active;
    }
  }

  public LampStatesResponse(String response) throws ResponseException {
    super("LAMP=", response);
  }

  @Override
  protected List<LampStatesResponse.LampState> parseResponseWithoutPrefix(String responseWithoutPrefix)
      throws ResponseException {
    List<LampStatesResponse.LampState> result = new ArrayList<LampStatesResponse.LampState>();
    LinkedList<String> queue = new LinkedList<String>(Arrays.asList(responseWithoutPrefix.split(" ")));
    while (!queue.isEmpty()) {
      try {
        int lampHours = Integer.parseInt(queue.remove());
        boolean active = Integer.parseInt(queue.remove()) == 1;
        result.add(new LampState(active, lampHours));
      } catch (NoSuchElementException | NumberFormatException e) {
        throw new ResponseException("Lamp status response could not be parsed", e);
      }
    }
    return result;
  }

}
