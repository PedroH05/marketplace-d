const fs = require('fs');
const path = require('path');

const envPath = path.resolve(__dirname, '..', 'src', 'environments', 'environment.prod.ts');
const currentContent = fs.readFileSync(envPath, 'utf8');

const replacements = {
  apiUrl: process.env.FRONTEND_API_URL,
  wsUrl: process.env.FRONTEND_WS_URL,
  cloudinaryCloudName: process.env.CLOUDINARY_CLOUD_NAME,
  cloudinaryUploadPreset: process.env.CLOUDINARY_UPLOAD_PRESET,
};

const hasBuildVariables = Object.values(replacements).some(Boolean);

if (!hasBuildVariables) {
  console.log('No frontend production variables found. Using committed environment.prod.ts.');
  process.exit(0);
}

function valueFor(key) {
  const fromEnv = replacements[key];
  if (fromEnv) return fromEnv;

  const match = currentContent.match(new RegExp(`${key}:\\s*'([^']*)'`));
  return match?.[1] ?? '';
}

const content = `export const environment = {
  production: true,
  apiUrl: '${valueFor('apiUrl')}',
  wsUrl: '${valueFor('wsUrl')}',
  cloudinaryCloudName: '${valueFor('cloudinaryCloudName')}',
  cloudinaryUploadPreset: '${valueFor('cloudinaryUploadPreset')}',
};
`;

fs.writeFileSync(envPath, content);
console.log('Production environment generated from deployment variables.');
