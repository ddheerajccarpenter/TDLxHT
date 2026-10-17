// Vercel Serverless Function: Dynamic Latest APK Download Redirect
// Ensures direct links to /api/download always redirect to the GitHub Release APK.

export default function handler(req, res) {
  const githubReleaseApk = "https://github.com/ddheerajccarpenter/TDLxHT/releases/download/v2.3.9/TDLxHT.2.3.9.apk";
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  return res.redirect(302, githubReleaseApk);
}
