/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.components.VeluxScene;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to retrieve the list of scenes on the KLF200 unit.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdGetAllScenes extends BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KlfCmdGetAllScenes.class);

    /** Holds all of the scenes that have been discovered. */
    private List<VeluxScene> scenes;

    /**
     * Constructor.
     */
    public KlfCmdGetAllScenes() {
        super();
        this.scenes = new ArrayList<VeluxScene>();
    }

    /**
     * Gets the list of nodes that have been discovered as a result of executing
     * the command.
     *
     * @return List of nodes
     */
    public List<VeluxScene> getScenes() {
        return this.scenes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        logger.trace("Handling response: {}", KLFUtils.formatBytes(data));
        short responseCode = KLFUtils.decodeKLFCommand(data);
        switch (responseCode) {
            case KLFCommandCodes.GW_GET_SCENE_LIST_CFM:
                int scenesExpected = data[FIRSTBYTE];
                logger.info("Command executing, expecting data for {} scenes.", scenesExpected);
                if (0 == scenesExpected) {
                    this.commandStatus = CommandStatus.COMPLETE;
                }
                break;
            case KLFCommandCodes.GW_GET_SCENE_LIST_NTF:
                int scenesFound = data[FIRSTBYTE];
                logger.info("Command recieved data for {} scenes.", scenesFound);
                int framePos = FIRSTBYTE + 1;
                for (int i = 0; i < scenesFound; ++i) {
                    logger.info("Found scene Id:{} - {}", data[framePos],
                            KLFUtils.extractUTF8String(data, framePos + 1, framePos + 64));
                    this.scenes.add(new VeluxScene(data[framePos],
                            KLFUtils.extractUTF8String(data, framePos + 1, framePos + 64)));
                    framePos += 65;
                }
                this.commandStatus = CommandStatus.COMPLETE;
                break;
            default:
                // This should not happen. If it does, the most likely cause is that
                // the KLFCommandStructure has not been configured or implemented
                // correctly.
                this.commandStatus = CommandStatus.ERROR;
                logger.error("Processing requested for a KLF response code (command code) that is not supported: {}.",
                        responseCode);
                break;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#getKLFCommandStructure
     * ()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.GET_ALL_SCENES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        return new byte[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(short,
     * byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }

}
