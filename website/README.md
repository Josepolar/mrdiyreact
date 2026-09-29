# MR.D.I.Y. Careers download site

Project creator and developer: **PolarDredd**. See [project credits](../CREDITS.md).

`index.html` is the main page and `styles.css` defines its presentation.
See [assets notes](assets/README.md) for artwork maintenance and
[download notes](downloads/README.md) for the APK replacement workflow.

Production site: https://mrdiy-careers-download.vercel.app/

This is a standalone static download page for the Android app. The APK is
available at `downloads/mrdiy-careers.apk`.

To preview it locally from the project root:

```powershell
cd website
npx serve .
```

For a public deployment, upload the contents of this folder to any static host
such as Vercel, Netlify, GitHub Pages, or Cloudflare Pages. Keep the `downloads`
folder in the deployed output so the download button continues to work.
