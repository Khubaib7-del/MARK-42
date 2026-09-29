# Installing Selvard from GitHub (sideload help)

Selvard is not on Google Play yet, so it is installed from the GitHub Releases page. Two different things can get in the way on a phone: the **browser download** and the **Play Protect install check**. This page records what we verified, what we did not, and what to do.

## 1. Download hangs at 100% ("10.33 MB / 10.33 MB", never finishes)

### What we verified (2026-09-29)

- The server side is correct. For `selvard-v0.9.0-ui-refresh.apk`, GitHub answers `content-type: application/vnd.android.package-archive`, `content-disposition: attachment`, and the full `content-length` (17,363,630 bytes) over HTTPS from `release-assets.githubusercontent.com`. Nothing in the repository or release configuration makes the file incomplete or mislabeled.
- The owner's screenshot (5:10 PM, 2026-09-26) shows Chrome's **Custom Tab** chrome (close ✕, chevron, share) rather than the normal browser toolbar. That is what Chrome shows when a link is opened *from another app* (GitHub mobile, WhatsApp, Instagram, a notes app). Downloads started inside a Custom Tab are a known way to get stuck at 100%: see [uazo/cromite#2771](https://github.com/uazo/cromite/issues/2771) ("Mostly trying .apk files from github, it shows 100% but it cannot finish") and [tiann/KernelSU#3216](https://github.com/tiann/KernelSU/issues/3216) (workaround: "Open in browser").
- Chrome for Android also runs Safe Browsing **download protection** for APK files (added to Chromium in 2025). It can delay or warn about APKs it has not seen before.

### What we did not verify

We have not reproduced the stall on a device, so the exact cause (Custom Tab handling, the Safe Browsing check, or both) is unconfirmed. The steps below are ordered from most to least likely to help.

### What to do

1. **Leave the in-app tab.** In the tab's ⋮ menu choose **Open in Chrome** (or copy the link and paste it into Chrome itself), then download again.
2. Open Chrome ⋮ → **Downloads**. If the file is listed with a warning ("this type of file can harm your device"), choose **Keep** / **Download anyway** — only for the APK from this repository's Releases page.
3. Try another browser (Firefox, Brave, Opera). Not tested by us; no guarantee.
4. Download on a computer and copy the file to the phone. If a computer's Chrome also stalls, use another browser there too.
5. Developer route, no browser involved: `adb install -r selvard-<version>.apk`.

Every release also carries a `.sha256` file. Compare it with `sha256sum selvard-<version>.apk` on a computer to confirm the download is intact.

## 2. "App blocked to protect your device" (Google Play Protect)

### Why it happens

Play Protect says *"hasn't seen an app from this developer before."* Every Selvard build so far is signed with the generic Android **debug** certificate (`CN=Android Debug`, confirmed with `apksigner verify --print-certs`), which has no reputation with Play Protect. This is the expected result for pre-release sideloaded builds; it is not a verdict that the app is harmful, and it is not evidence that it is safe.

### What to do

1. On the dialog tap **More details** → **Install anyway**.
2. If your device only offers **Got it**: Play Store → profile picture → **Play Protect** → ⚙ → turn off **Scan apps with Play Protect**, install Selvard, then turn it back on. Leaving it off is your call; we recommend restoring it.
3. Android may also ask you to allow *Install unknown apps* for the app you opened the APK from (Chrome, Files, WhatsApp). That is a separate, normal prompt.

### Updating over an older build

An update installs over the previous one only if both are signed with the same certificate. The v0.9.0 and v0.9.1 APKs carry the same certificate (matching SHA-256 from `apksigner verify --print-certs`), so 0.9.1 should update 0.9.0 in place. If Android reports *"App not installed as package conflicts with an existing package"*, uninstall the old build first (its local data is removed with it).

### The durable fix (owner action)

- Sign releases with a **stable release key that the owner keeps** (not the debug key), so the developer identity is consistent across versions. Never commit the key; `*.jks`, `*.keystore` and `keystore.properties` are already git-ignored.
- Register as an Android developer. Google's [developer verification](https://support.google.com/android/answer/17065026) starts in September 2026 for installs from participating app stores on certified devices in Brazil, Indonesia, Singapore and Thailand, and is planned to expand to all install sources in 2027. A free *limited distribution* account (up to 20 devices, no government ID) exists for hobbyists ([FAQ](https://developer.android.com/developer-verification/guides/faq)). ADB installs are not affected.

Until that is done, expect the Play Protect prompt on every fresh install.
