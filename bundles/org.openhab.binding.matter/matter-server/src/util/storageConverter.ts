import * as fs from 'fs';
import * as path from 'path';


//convert file and return the name and directory where the files the new files are stored
export function convertJsonFile(inputFile: string, nodeNum: number) {
  const parsedPath = path.parse(inputFile);
  const outputDir = parsedPath.dir
  const id = `${parsedPath.name}-${nodeNum}`;
  const filesDir = path.join(outputDir, id)

  if (fs.existsSync(inputFile) && parsedPath.ext === '.json') {
    const jsonData = JSON.parse(fs.readFileSync(inputFile, 'utf-8'));
    if (!fs.existsSync(filesDir)) {
      fs.mkdirSync(filesDir, { recursive: true });
      console.log(`Directory created: ${filesDir}`);
    } else {
      console.log(`Directory already exists: ${filesDir}`);
    }
    splitJson(jsonData, filesDir);
    renameOriginalFile(inputFile);
  }

  return {
    outputDir: outputDir,
    name: parsedPath.name,
    id: id
  }
}

function writeToFile(outputDir: string, fileName: string, content: any) {
  const filePath = path.join(outputDir, fileName);
  fs.writeFileSync(filePath, JSON.stringify(content, null, 2));
  console.log(`Written to: ${filePath}`);
}


function splitJson(jsonData: any, outputDir: string) {
  const rootCertManager = jsonData['0.RootCertificateManager'];
  const sessionManager = jsonData['0.SessionManager'];
  const matterController = jsonData['0.MatterController'];

  if (rootCertManager) {
    writeToFile(outputDir, 'credentials.nextCertificateId', rootCertManager.nextCertificateId);
    writeToFile(outputDir, 'credentials.rootCertBytes', rootCertManager.rootCertBytes);
    writeToFile(outputDir, 'credentials.rootCertId', rootCertManager.rootCertId);
    writeToFile(outputDir, 'credentials.rootKeyIdentifier', rootCertManager.rootKeyIdentifier);
    writeToFile(outputDir, 'credentials.rootKeyPair', rootCertManager.rootKeyPair);
  }

  if (sessionManager) {
    writeToFile(outputDir, 'sessions.resumptionRecords', sessionManager.resumptionRecords);
  }

  if (matterController) {
    writeToFile(outputDir, 'nodes.commissionedNodes', matterController.commissionedNodes);
    writeToFile(outputDir, 'credentials.fabric', matterController.fabric);
  }
}

function renameOriginalFile(filePath: string) {
  const parsedPath = path.parse(filePath);
  const newFilePath = path.join(parsedPath.dir, `${parsedPath.name}.converted${parsedPath.ext}`);

  fs.renameSync(filePath, newFilePath);
  console.log(`Original file renamed to: ${newFilePath}`);
}