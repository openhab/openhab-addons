/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

/**
 * Converts the device-specific data from ioBroker.tuya to a binding compatible JSON
 *
 * @author Jan N. Klug - Initial contribution
 */
const http = require('https');
const fs = require('fs');

const schemaJson = fs.createWriteStream("../../../target/in-schema.json");
http.get("https://raw.githubusercontent.com/Apollon77/ioBroker.tuya/master/lib/schema.json", function(response) {
    response.setEncoding('utf8');
    response.pipe(schemaJson);
    schemaJson.on('finish', () => {
        schemaJson.close();

        const knownSchemas = require('../../../target/in-schema.json');

        let productKey, value;
        let convertedSchemas = {};

        for (productKey in knownSchemas) {
            try {
                let schema = JSON.parse(knownSchemas[productKey].schema);
                let convertedSchema = {};
                for (value in schema) {
                    let entry = schema[value];
                    let convertedEntry;
                    if (entry.type === 'raw') {
                        convertedEntry = {id: entry.id, type: entry.type};
                    } else {
                        convertedEntry = {id: entry.id, type: entry.property.type};
                        if (convertedEntry.type === 'enum') {
                            convertedEntry['range'] = entry.property.range;
                        }
                        if (convertedEntry.type === 'value' && entry.property.min !== null && entry.property.max !== null) {
                            convertedEntry['min'] = entry.property.min;
                            convertedEntry['max'] = entry.property.max;
                        }
                    }
                    convertedSchema[entry.code] = convertedEntry;
                }
                if (Object.keys(convertedSchema).length > 0) {
                    convertedSchemas[productKey] = convertedSchema;
                }
            } catch (err) {
                console.log('Parse Error in Schema for ' + productKey + ': ' + err);
            }
        }

        fs.writeFile('../resources/schema.json', JSON.stringify(convertedSchemas, null, '\t'), (err) => {
            if (err) throw err;
        });
    });
});
