/**
 * This script works around an issue with the openapi generator
 * not parsing allOf types correctly.
 *
 * Originally part of the jellyfin-client-axios project:
 * https://github.com/jellyfin/jellyfin-client-axios/blob/dfadff9b0b8749a04fb21d298b86624b20db1e5a/scripts/modifySchema.mjs
 */
import fs from 'fs/promises';

(async (file) => {
	let txt = await fs.readFile(file);
	const json = JSON.parse(txt, (_, value) => {
		// Remove all "allOf" instances, this removes nullability/descriptions
		if (!!value && typeof value === 'object' && 'allOf' in value && value.allOf.length === 1) {
			return value.allOf[0];
		}

		return value;
	});

	txt = JSON.stringify(json);
	await fs.writeFile(`${file}`, txt);
})(process.argv[2]);
