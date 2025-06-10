import { Logger } from "@matter/general";

export function printError(logger: Logger, error: Error, functionName: string) {
    logger.error(`Error executing function ${functionName}: ${error.message}`);
    logger.error(`Stack trace: ${error.stack}`);

    // Log additional error properties if available
    if ("code" in error) {
        logger.error(`Error code: ${(error as any).code}`);
    }
    if ("name" in error) {
        logger.error(`Error name: ${(error as any).name}`);
    }
    if ("id" in error) {
        logger.error(`Error id: ${(error as any).id}`);
    }

    // Fallback: log the entire error object in case there are other useful details
    logger.error(`Full error object: ${JSON.stringify(error, Object.getOwnPropertyNames(error), 2)}`);
    logger.error("--------------------------------");
    logger.error(error);
}
