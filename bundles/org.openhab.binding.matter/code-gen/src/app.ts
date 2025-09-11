import { Bytes } from "@matter/general";
import {
    AnyElement,
    AnyValueElement,
    AttributeElement,
    ClusterElement,
    ClusterModel,
    CommandElement,
    DatatypeElement,
    FieldElement,
    Matter,
    MatterModel,
} from "@matter/main/model";
import * as MatterNode from "@matter/node";
import "@matter/model/resources";
import fs from "fs";
import handlebars from "handlebars";

// Convert Matter object to JSON string and parse it back, this is a workaround to avoid some typescript issues iterating over the Matter object
const matterData = JSON.parse(JSON.stringify(Matter)) as MatterModel;

matterData.children
    .filter(c => c.name == "LevelControl" || c.name == "ColorControl")
    .forEach(c => {
        c.children
            ?.filter(c => c.tag == "command")
            .forEach(c => {
                c.children
                    ?.filter(c => c.type == "Options")
                    .forEach(c => {
                        c.type = "OptionsBitmap";
                    });
            });
    });

interface ExtendedClusterElement extends ClusterElement {
    attributes: AnyValueElement[];
    commands: CommandElement[];
    datatypes: AnyValueElement[];
    enums: AnyValueElement[];
    structs: AnyValueElement[];
    maps: AnyValueElement[];
    typeMapping: Map<string, string | undefined>;
}

function toJSON(data: any, space = 2) {
    return JSON.stringify(
        data,
        (_, value) => {
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
        },
        space,
    );
}

handlebars.registerHelper("asUpperCase", function (str) {
    if (str == undefined) {
        return "UNDEFINED";
    }
    return str.toUpperCase();
});

handlebars.registerHelper("asLowerCase", function (str) {
    if (str == undefined) {
        return "undefined";
    }
    return str.toLowerCase();
});

handlebars.registerHelper("asUpperCamelCase", function (str) {
    return toUpperCamelCase(str);
});

handlebars.registerHelper("asLowerCamelCase", function (str) {
    return toLowerCamelCase(str);
});

handlebars.registerHelper("asTitleCase", function (str) {
    if (!str) {
        return "Undefined";
    }
    return str
        .replace(/([a-z])([A-Z])/g, "$1 $2") // Add a space before uppercase letters that follow lowercase letters
        .replace(/[_\s]+/g, " ") // Replace underscores or multiple spaces with a single space
        .trim()
        .split(" ")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(" ");
});

handlebars.registerHelper("asEnumField", function (str) {
    // Check if the string starts with a number and prepend "V" if it does
    if (/^\d/.test(str)) {
        str = "V" + str;
    }

    // First split camelCase words by inserting underscores
    str = str
        // Split between lowercase and uppercase letters
        .replace(/([a-z])([A-Z])/g, "$1_$2")
        // Split between uppercase letters followed by lowercase
        .replace(/([A-Z])([A-Z][a-z])/g, "$1_$2")
        // Replace any remaining spaces with underscores
        .replace(/\s+/g, "_")
        // Finally convert to uppercase
        .toUpperCase();

    return str;
});

handlebars.registerHelper("asUpperSnakeCase", function (str) {
    if (str == undefined) {
        return "UNDEFINED";
    }
    return str
        .replace(/([a-z])([A-Z])/g, "$1_$2") // Insert underscore between camelCase
        .replace(/[\s-]+/g, "_") // Replace spaces and hyphens with underscore
        .toUpperCase();
});

handlebars.registerHelper("asSpacedTitleCase", function (str) {
    if (!str) {
        return "Undefined";
    }
    return str
        .replace(/([a-z])([A-Z])/g, "$1 $2") // Add a space before uppercase letters that follow lowercase letters
        .replace(/([A-Z])([A-Z][a-z])/g, "$1 $2") // Split between capital letters when followed by capital+lowercase
        .replace(/([a-z])([A-Z][a-z])/g, "$1 $2") // Split between lowercase and camelCase word
        .replace(/([a-zA-Z])(\d)/g, "$1 $2") // Split between letters and numbers
        .replace(/(\d)([a-zA-Z])/g, "$1 $2") // Split between numbers and letters
        .replace(/[_\s]+/g, " ") // Replace underscores or multiple spaces with a single space
        .trim();
});

handlebars.registerHelper("asHex", function (decimal, length) {
    let hex = decimal.toString(16).toUpperCase();
    if (length > 0) {
        hex = hex.padStart(length, "0");
    }
    return `0x${hex}`;
});

handlebars.registerHelper("isLastElement", function (index: number, count: number) {
    return index >= count - 1;
});
handlebars.registerHelper("isFirstElement", function (index: number) {
    return index == 0;
});
handlebars.registerHelper("isEmpty", function (e: Array<any> | String | undefined) {
    return e == undefined || e.length == 0;
});
handlebars.registerHelper("isDepreciated", function (field) {
    return field.conformance == "D" || field.conformance == "X" || field.conformance == "[!LT]";
});
handlebars.registerHelper("isReadOnly", function (field) {
    return field.access.indexOf("RW") == -1;
});
handlebars.registerHelper("toBitmapType", function (constraint) {
    return constraint != undefined && constraint.indexOf(" to ") > 0 ? "short" : "boolean";
});
handlebars.registerHelper("toBitmapChildName", function (child, type) {
    return type == "FeatureMap" ? toLowerCamelCase(child.title) : toLowerCamelCase(child.name);
});
handlebars.registerHelper("isAttribute", function (field) {
    return field.tag == "attribute";
});

handlebars.registerHelper("isNonNull", function (field) {
    return field.access?.indexOf("RW") > -1 || field.isNonNull;
});

function toLowerCamelCase(str: string): string {
    if (str == undefined) {
        return "undefined";
    }
    return str.replace(/(?:^\w|[_\s]\w)/g, (match, offset) => {
        return offset === 0 ? match.toLowerCase() : match.replace(/[_\s]/, "").toUpperCase();
    });
}

function toUpperCamelCase(str: string | undefined) {
    if (str == undefined) {
        return "undefined";
    }
    return str.replace(/(^\w|[_\s]\w)/g, match => match.replace(/[_\s]/, "").toUpperCase());
}

/**
 *
 * @param field Lookup function to map matter native types to Java native types
 * @returns
 */
function matterNativeTypeToJavaNativeType(field: AnyElement) {
    switch (field.type || field.name) {
        case "bool":
            return "Boolean";
        case "uint8":
        case "uint16":
        case "uint24":
        case "uint32":
        case "int8":
        case "int16":
        case "int24":
        case "int32":
        case "status":
            return "Integer";
        case "uint40":
        case "uint48":
        case "uint56":
        case "uint64":
        case "int40":
        case "int48":
        case "int56":
        case "int64":
            return "BigInteger";
        case "single":
            return "Float";
        case "double":
            return "Double";
        case "date":
            return "date";
        case "string":
        case "locationdesc":
            return "Locationdesc";
        case "octstr":
            return "OctetString";
        // this are semantic tag fields
        case "tag":
        case "namespace":
            return "Integer";
        case "list":
        case "struct":
        case "map8":
        case "map16":
        case "map32":
        case "map64":
        case "map8":
        case "map16":
        case "map32":
        case "map64":
        //these are complex types and do not map to a Java native type
        default:
            return undefined;
    }
}

function filterDep(e: AnyValueElement) {
    //remove fields flagged as depreciated or not used
    const children = e.children?.filter(field => {
        const f = field as FieldElement;
        return f.conformance != "X" && f.conformance != "D";
    });
    e.children = children as AnyValueElement[];
    return e;
}

/**
 * Type mapper attempts to lookup the Java native type for any matter element, this include Integers, Strings, Booleans, etc...
 *
 * If there is no matching type, then the matter element is a complex type, like maps, enums and structs
 * These complex types are represented as Java classes, so the mapping will refer to that complex type which
 * will be templated out later in the code
 *
 * This code also traverses any children of the data type, applying the same logic
 *
 * @param mappings - existing set of mapping lookups for types
 * @param dt - the data type we are operating on
 * @returns the data type which now includes a new field 'mappedType' on all levels of the object
 */
function typeMapper(mappings: Map<string, string | undefined>, dt: AnyValueElement): any {
    let mappedType: string | undefined;
    if (
        (dt.tag == "attribute" && dt.type?.startsWith("enum")) ||
        dt.type?.startsWith("map") ||
        dt.type?.startsWith("struct")
    ) {
        //these types will be generated as inner classes and will be referred to by name
        mappedType = dt.name;
    } else {
        //this gets raw types
        mappedType = (dt.type && mappings.get(dt.type)) || matterNativeTypeToJavaNativeType(dt) || dt.type || "String";
    }
    if (mappedType == "list") {
        const ct = dt.children?.[0].type;
        //if the type is cluster.type then its referring to type in another cluster
        if (ct && ct.indexOf(".") > 0) {
            const [otherCluster, otherType] = ct.split(".");
            mappedType = `List<${toUpperCamelCase(otherCluster + "Cluster")}.${toUpperCamelCase(otherType)}>`;
        } else {
            mappedType = `List<${toUpperCamelCase((ct && mappings.get(ct)) || ct)}>`;
        }
    } else if (mappedType && mappedType.indexOf(".") > 0) {
        //some types reference other clusters, like MediaPlayback.CharacteristicEnum
        const [cName, dtName] = mappedType.split(".");
        mappedType = `${toUpperCamelCase(cName)}Cluster.${toUpperCamelCase(dtName)}`;
    } else if (mappings.get(mappedType)) {
        //if the type is already mapped, then use the mapped type
        mappedType = mappings.get(mappedType);
    }

    const children = dt.children?.map(child => {
        return typeMapper(mappings, child as AnyValueElement) as AnyValueElement;
    });
    return {
        ...dt,
        children: children,
        mappedType: mappedType,
    };
}

/**
 * Certain clusters have complex inheritance that we don't support yet (and don't need right now)
 */
const skipClusters = new Set(["Messages"]);

/**
 * Global types (not in a cluster)
 */
const globalDataTypes = (matterData.children as DatatypeElement[]).filter(c => c.tag === "datatype");
const globalAttributes = (matterData.children as AttributeElement[]).filter(c => c.tag === "attribute");

/**
 * Global type mapping lookup, clusters will combine this with their own mapping
 */
const globalTypeMapping = new Map();
//some types are special and need to be mapped to Java native types here
globalTypeMapping.set("FabricIndex", "Integer");
// semantic tag fields
globalTypeMapping.set("namespace", "Integer");
globalTypeMapping.set("tag", "Integer");

globalDataTypes.forEach(dt => {
    matterNativeTypeToJavaNativeType(dt) && globalTypeMapping.set(dt.name, matterNativeTypeToJavaNativeType(dt));
});
//it seems like there is a global data type that overrides the string type
globalTypeMapping.set("string", "String");

globalAttributes.forEach(
    dt => matterNativeTypeToJavaNativeType(dt) && globalTypeMapping.set(dt.name, matterNativeTypeToJavaNativeType(dt)),
);

const clusters: ExtendedClusterElement[] =
    (matterData.children as ClusterElement[])
        .filter(c => c.tag === "cluster")
        .filter(c => !skipClusters.has(c.name))
        .map(cluster => {
            // typeMapping is a map of matter types to Java types
            const typeMapping = new Map<string, string | undefined>(globalTypeMapping);
            const dataTypes = (cluster.children || []).filter(c => c.tag === "datatype") as DatatypeElement[];
            const maps = (cluster.children || []).filter(c => c.type?.startsWith("map")) as AnyValueElement[];
            const enums = (cluster.children || []).filter(c => c.type?.startsWith("enum")) as AnyValueElement[];
            const structs = (cluster.children || [])
                .filter(dt => dt.type === "struct" || dt.tag === "event")
                .map(dt => typeMapper(typeMapping, dt as AnyValueElement));
            dataTypes?.forEach(dt => {
                if (dt.type && dt.type.indexOf(".") > 0) {
                    return typeMapping.set(dt.name, dt.type);
                }
                return (
                    matterNativeTypeToJavaNativeType(dt) &&
                    typeMapping.set(dt.name, matterNativeTypeToJavaNativeType(dt))
                );
            });

            // if the cluster has a type, then the java class will extend this type (which is another cluster)
            const parent = cluster.type ? matterData.children.find(c => c.name == cluster.type) : undefined;

            const attributes = cluster.children
                ?.filter(c => c.tag == "attribute")
                ?.filter((element, index, self) => {
                    //remove duplicates, not sure why they exist in the model
                    const dupIndex = self.findIndex(e => e.name === element.name);
                    if (dupIndex != index) {
                        if (element.conformance?.toString().startsWith("[!")) {
                            return false;
                        }
                    }
                    // if the parent cluster has an attribute with the same name, then don't include it as we need to use the parent's attribute
                    if (parent) {
                        const parentAttr = parent.children
                            ?.filter(c => c.tag == "attribute")
                            ?.find(c => c.name == element.name);
                        if (parentAttr) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(attr => filterDep(attr))
                .map(dt => typeMapper(typeMapping, dt));

            //some command types reference attribute types (LevelControl Options)
            attributes?.forEach(dt => {
                if (dt.type && dt.type.indexOf(".") > 0) {
                    typeMapping.set(dt.name, dt.type);
                    return;
                }
                typeMapping.set(dt.name, matterNativeTypeToJavaNativeType(dt) || dt.type);

                //some local Attributes like FeatureMap reference the global attribute type
                if (dt.children) {
                    const ga = globalAttributes.find(a => a.name == dt.type);
                    if (ga && ga.type?.startsWith("map") && !maps?.find(e => e.name == dt.name)) {
                        maps?.push(dt as ClusterModel.Child);
                    }
                }
            });

            //clean up commands
            const commandsRaw = cluster.children?.filter(c => c.tag == "command");
            const commands = commandsRaw
                ?.map(command => {
                    //some commands reference others
                    if (command.type != undefined) {
                        command.children = commandsRaw?.find(c => c.name == command.type)?.children || [];
                    }
                    return command;
                })
                .map(command => filterDep(command))
                .filter(c => (c as CommandElement).direction == "request")
                .map(dt => {
                    if (dt.type && dt.type.indexOf(".") > 0) {
                        typeMapping.set(dt.name, dt.type);
                        return;
                    }
                    const newCommand = typeMapper(typeMapping, dt);
                    newCommand.children?.forEach((c: any) => {
                        if (c.type?.startsWith("map") && !maps?.find(e => e.name == c.name)) {
                            maps?.push(c as ClusterModel.Child);
                        }
                    });
                    return newCommand;
                });

            return {
                ...cluster,
                attributes: attributes,
                commands: commands,
                datatypes: dataTypes,
                enums: enums,
                structs: structs,
                maps: maps,
                typeMapping: typeMapping,
            } as ExtendedClusterElement;
        }) || [];

function copyClusterDatatype(sourceCluster: ExtendedClusterElement, destCluster: ExtendedClusterElement, name: string) {
    let dt =
        sourceCluster.datatypes?.find(d => d.name == name) ||
        sourceCluster.enums?.find(d => d.name == name) ||
        sourceCluster.structs?.find(d => d.name == name) ||
        sourceCluster.attributes?.find(d => d.name == name);
    if (dt) {
        destCluster.typeMapping.set(name, name);
        if (dt.type) {
            if (dt.type.startsWith("enum")) {
                if (!destCluster.enums) {
                    destCluster.enums = [];
                }
                destCluster.enums.push(dt);
            } else if (dt.type.startsWith("map")) {
                if (!destCluster.maps) {
                    destCluster.maps = [];
                }
                destCluster.maps.push(dt);
            } else if (dt.type == "struct") {
                if (!destCluster.structs) {
                    destCluster.structs = [];
                }
                destCluster.structs.push(dt);
            } else {
                if (!destCluster.datatypes) {
                    destCluster.datatypes = [];
                }
                destCluster.datatypes.push(dt);
            }
        }
        destCluster.commands = destCluster.commands.map(c => typeMapper(destCluster.typeMapping, c));
    }
}
clusters.forEach(cluster => {
    cluster.typeMapping.forEach((value, key, map) => {
        if (value && value.indexOf(".") > 0) {
            const [cName, dtName] = value.split(".");
            const otherCluster = clusters.find(c => c.name != cluster.name && c.name == cName);
            if (otherCluster) {
                copyClusterDatatype(otherCluster, cluster, dtName);
            }
            return;
        }
    });
});

// Compile Handlebars template
const clusterSource = fs.readFileSync("src/templates/cluster-class.java.hbs", "utf8");
const clusterTemplate = handlebars.compile(clusterSource);
const baseClusterSource = fs.readFileSync("src/templates/base-cluster.java.hbs", "utf8");
const baseClusterTemplate = handlebars.compile(baseClusterSource);
const deviceTypeSource = fs.readFileSync("src/templates/device-types-class.java.hbs", "utf8");
const deviceTypeTemplate = handlebars.compile(deviceTypeSource);
const clusterRegistrySource = fs.readFileSync("src/templates/cluster-registry.java.hbs", "utf8");
const clusterRegistryTemplate = handlebars.compile(clusterRegistrySource);
const clusterConstantsSource = fs.readFileSync("src/templates/cluster-constants.java.hbs", "utf8");
const clusterConstantsTemplate = handlebars.compile(clusterConstantsSource);
const semanticTagsSource = fs.readFileSync("src/templates/semantic-tags-class.java.hbs", "utf8");
const semanticTagsTemplate = handlebars.compile(semanticTagsSource);


// Generate Java code

const datatypes = {
    enums: [
        ...globalDataTypes?.filter(c => c.type?.startsWith("enum")),
        ...globalAttributes?.filter(c => c.type?.startsWith("enum")),
    ],
    structs: [
        ...globalDataTypes?.filter(c => c.type?.startsWith("struct")),
        ...globalAttributes?.filter(c => c.type?.startsWith("struct")),
    ].map(e => typeMapper(globalTypeMapping, e)),
    maps: [
        ...globalDataTypes?.filter(c => c.type?.startsWith("map")),
        ...globalAttributes?.filter(c => c.type?.startsWith("map")),
    ],
};

fs.mkdir("out", { recursive: true }, err => {});

const baseClusterClass = baseClusterTemplate(datatypes);
fs.writeFileSync(`out/BaseCluster.java`, baseClusterClass);

const deviceTypeClass = deviceTypeTemplate({
    deviceTypes: matterData.children.filter((c: AnyElement) => c.tag === "deviceType" && c.id !== undefined),
});
fs.writeFileSync(`out/DeviceTypes.java`, deviceTypeClass);

// We'll write ClusterRegistry and ClusterConstants after inheritance merging so they reflect the final definitions.
//
// After clusters array is initially built (`const clusters: ExtendedClusterElement[] = ...`) we need to merge any clusters
// that specify a `type` (i.e. they inherit from another cluster) with their parent cluster.
// The child cluster should override any definitions from the parent.  We also keep the parent
// cluster in the array so that other children can still inherit from it if necessary, but we will skip emitting any
// Java source for clusters that don't have a `CLUSTER_ID` (those are the abstract parent definitions).

// Helper to merge two lists of schema objects (attributes, commands, etc.) where the child overrides the parent on
// duplicate `name` values.
function mergeLists<T extends { name: string }>(parentList: T[] | undefined, childList: T[] | undefined): T[] {
    const merged: T[] = [];
    const pushOrReplace = (item: T) => {
        const idx = merged.findIndex(i => i.name === item.name);
        if (idx >= 0) {
            merged[idx] = item; // child overrides parent
        } else {
            merged.push(item);
        }
    };
    parentList?.forEach(pushOrReplace);
    childList?.forEach(pushOrReplace);
    return merged;
}

// Build a quick lookup map from cluster name to instance for recursive merging.
const clusterLookup = new Map<string, ExtendedClusterElement>();
clusters.forEach(c => clusterLookup.set(c.name, c));

// Recursively merge parent definitions into the child cluster.  The `type` property is cleared afterwards so the
// templating logic always extends `BaseCluster`.
function resolveInheritance(cluster: ExtendedClusterElement, seen: Set<string> = new Set()): void {
    if (!cluster.type) {
        return; // no parent
    }
    if (seen.has(cluster.name)) {
        // circular reference guard (should never happen in the Matter model)
        return;
    }
    seen.add(cluster.name);
    const parent = clusterLookup.get(cluster.type);
    if (!parent) {
        return; // parent not found – leave as-is; the template will still extend BaseCluster
    }

    // Ensure parent is resolved first (support multi-level inheritance)
    resolveInheritance(parent, seen);

    // Merge lists – parent first, then child overrides duplicates
    cluster.attributes = mergeLists(parent.attributes, cluster.attributes);
    cluster.commands = mergeLists(parent.commands, cluster.commands);
    cluster.enums = mergeLists(parent.enums, cluster.enums);
    cluster.structs = mergeLists(parent.structs, cluster.structs);
    cluster.maps = mergeLists(parent.maps, cluster.maps);
    cluster.datatypes = mergeLists(parent.datatypes, cluster.datatypes);

    // Merge low-level children array that some templates rely on
    cluster.children = mergeLists(parent.children as any, cluster.children as any);

    // Merge type mapping – child entries should override parent entries
    cluster.typeMapping = new Map([...parent.typeMapping, ...cluster.typeMapping]);

    // Clear the `type` so the template does not generate an "extends ParentCluster"
    // and instead always extends BaseCluster.
    delete (cluster as any).type;
}

// Apply the merge for every cluster that specifies a parent type.
clusters.forEach(c => resolveInheritance(c));

// Emit Java code for concrete clusters only (i.e. those that have a CLUSTER_ID).
clusters
    .filter(c => c.id !== undefined && c.id !== null)
    .forEach(cluster => {
        const javaCode = clusterTemplate(cluster);
        fs.writeFileSync(`out/${cluster.name}Cluster.java`, javaCode);
    });

// Generate ClusterRegistry and ClusterConstants using the merged cluster data. Note that they can still reference
// clusters without IDs (e.g. abstract definitions), but the templates themselves guard against missing IDs where
// appropriate.

const concreteClusters = clusters.filter(c => c.id !== undefined && c.id !== null);

const clusterRegistryClass = clusterRegistryTemplate({ clusters: concreteClusters });
fs.writeFileSync(`out/ClusterRegistry.java`, clusterRegistryClass);

const clusterConstantsClass = clusterConstantsTemplate({ clusters: concreteClusters });
fs.writeFileSync(`out/ClusterConstants.java`, clusterConstantsClass);

// Build namespace → tags structure expected by the template
const namespaces = Object.entries(MatterNode)
    .filter(([name]) => name.endsWith("Tag"))
    .map(([rawName, tagDefs]: [string, any]) => {
        // Derive a clean namespace identifier (e.g. "Area" from "AreaNamespaceTag")
        let namespace = rawName;
        if (namespace.endsWith("NamespaceTag")) {
            namespace = namespace.substring(0, namespace.length - "NamespaceTag".length);
        } else if (namespace.endsWith("Tag")) {
            namespace = namespace.substring(0, namespace.length - "Tag".length);
        }

        // Convert the tag definition object into an array understood by the template
        const tagEntries = Object.entries(tagDefs).map(([tagName, info]: [string, any]) => ({
            name: tagName,
            id: info.tag,
            label: info.label,
        }));

        // All tags in a namespace share the same namespaceId, so read it from the first tag
        const firstTagKey = Object.keys(tagDefs)[0];
        const namespaceId = firstTagKey ? tagDefs[firstTagKey].namespaceId : undefined;

        return {
            namespace,
            id: namespaceId,
            tags: tagEntries,
        };
    });

// Generate NamespaceTags.java using the prepared structure
const semanticTagsClass = semanticTagsTemplate({ namespaces });
fs.writeFileSync(`out/SemanticTags.java`, semanticTagsClass);


