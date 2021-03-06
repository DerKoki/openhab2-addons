/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi smart plug device
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt -Refactor
 */
public class XiaomiActorPlugHandler extends XiaomiActorBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(XiaomiActorPlugHandler.class);

    public XiaomiActorPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (CHANNEL_POWER_ON.equals(channelUID.getId())) {
            String status = command.toString().toLowerCase();
            getXiaomiBridgeHandler().writeToDevice(getItemId(), new String[] { "status" }, new Object[] { status });
            return;
        }
        // Only gets here, if no condition was met
        logger.warn("Can't handle command {} on channel {}", command, channelUID);
    }

    @Override
    void parseHeartbeat(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReadAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReport(JsonObject data) {
        getStatusFromData(data);
    }

    @Override
    void parseWriteAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        getStatusFromData(data);
        if (data.has("inuse")) {
            updateState(CHANNEL_IN_USE, (data.get("inuse").getAsInt() == 1) ? OnOffType.ON : OnOffType.OFF);
        }
        if (data.has("load_power")) {
            updateState(CHANNEL_LOAD_POWER, new DecimalType(data.get("load_power").getAsBigDecimal()));
        }
        if (data.has("power_consumed")) {
            updateState(CHANNEL_POWER_CONSUMED,
                    new DecimalType(data.get("power_consumed").getAsBigDecimal().scaleByPowerOfTen(-3)));
        }
    }

    private void getStatusFromData(JsonObject data) {
        if (data.has("status")) {
            boolean isOn = "on".equals(data.get("status").getAsString());
            updateState(CHANNEL_POWER_ON, isOn ? OnOffType.ON : OnOffType.OFF);
            if (!isOn) {
                updateState(CHANNEL_IN_USE, OnOffType.OFF);
                updateState(CHANNEL_LOAD_POWER, new DecimalType(0));
            }
        }
    }

}
