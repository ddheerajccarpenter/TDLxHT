import { createClient } from '@supabase/supabase-js';
import fs from 'fs';
import path from 'path';

const supabaseUrl = process.env.SUPABASE_URL || 'https://mtlckvkyimoqbrlvirch.supabase.co';
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseKey) {
  console.error("Missing SUPABASE_SERVICE_ROLE_KEY");
  process.exit(1);
}

const supabase = createClient(supabaseUrl, supabaseKey);

async function uploadApk(fileName, remotePath) {
  const filePath = path.resolve(process.cwd(), fileName);
  if (!fs.existsSync(filePath)) {
    console.error(`File ${filePath} does not exist`);
    return;
  }
  const fileBuffer = fs.readFileSync(filePath);
  console.log(`Uploading ${fileName} (${fileBuffer.length} bytes) to apk/${remotePath}...`);

  const { data, error } = await supabase.storage
    .from('apk')
    .upload(remotePath, fileBuffer, {
      contentType: 'application/vnd.android.package-archive',
      upsert: true
    });

  if (error) {
    console.error(`Failed to upload ${remotePath}:`, error);
    process.exit(1);
  } else {
    console.log(`Successfully uploaded ${remotePath}:`, data);
  }
}

async function main() {
  await uploadApk('TDLxHT.2.5.19.apk', 'TDLxHT.2.5.19.apk');
  await uploadApk('TDlxHT.apk', 'TDlxHT.apk');
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
