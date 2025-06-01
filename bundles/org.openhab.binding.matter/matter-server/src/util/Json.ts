import { Bytes, Logger } from "@matter/general";
import { ValueModel } from "@matter/model";
import { ValidationDatatypeMismatchError } from "@matter/types";
import { camelize } from "./String";

const logger = Logger.get("Clusters");

export function convertJsonDataWithModel(model: ValueModel, data: any): any {
    const definingModel = model.definingModel ?? model;
    logger.debug("convertJsonDataWithModel: type {}", definingModel.effectiveMetatype);
    logger.debug("convertJsonDataWithModel: data {}", data);
    logger.debug("convertJsonDataWithModel: model {}", model);
    switch (definingModel.effectiveMetatype) {
        case "array":
            if (!Array.isArray(data)) {
                throw new ValidationDatatypeMismatchError(`Expected array, got ${typeof data}`);
            }
            return data.map(item => convertJsonDataWithModel(definingModel.children[0], item));
        case "object":
            if (typeof data !== "object") {
                throw new ValidationDatatypeMismatchError(`Expected object, got ${typeof data}`);
            }
            for (const child of definingModel.children) {
                const childKeyName = camelize(child.name);
                data[childKeyName] = convertJsonDataWithModel(child, data[childKeyName]);
            }
            return data;
        case "integer":
            if (typeof data === "string") {
                if (definingModel.metabase?.byteSize !== undefined && definingModel.metabase.byteSize > 6) {
                    // If we have an integer with byteSize > 6 and a string value, we need to convert the string to a
                    // BigInt also handles 0x prefixed hex strings
                    return BigInt(data);
                } else if (data.startsWith("0x")) {
                    // Else if hex string convert to number
                    return parseInt(data.substring(2), 16);
                }
            }
            break;
        case "bytes":
            if (typeof data === "string") {
                // ByteArray encoded as hex-String ... so convert to ByteArray
                return Bytes.fromHex(data);
            }
            break;
    }

    return data;
}

export function toJSON(data: any) {
    return JSON.stringify(data, (_, value) => {
        if (typeof value === "bigint") {
            return value.toString();
        }
        if (value instanceof Uint8Array) {
            return Bytes.toHex(value);
        }
        if (value === undefined) {
            return "undefined";
        }
        return value;
    });
}

export function fromJSON(data: any) {
    return JSON.parse(data, (key, value) => {
        if (typeof value === "string" && value.startsWith("0x")) {
            return Bytes.fromHex(value);
        }
        return value;
    });
}
