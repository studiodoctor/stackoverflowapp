# CI/CD Setup Guide

## GitHub Actions Workflow Overview

```
Push / PR
    │
    ├─ lint          → lintDebug → uploads lint-report.html
    ├─ unit-tests    → testDebugUnitTest + jacocoTestReport → uploads HTML + XML coverage
    │
    ├─ build-debug   (needs lint + unit-tests) → uploads debug APK (every branch)
    └─ build-release (needs lint + unit-tests) → uploads signed APK + creates GitHub Release
                         (only on push to main OR manual trigger)
```

---

## Required GitHub Secrets

Go to **GitHub repo → Settings → Secrets and variables → Actions → New repository secret**
and add the following four secrets.

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` file (see below) |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |

### How to generate the keystore (if you don't have one)

```bash
keytool -genkeypair \
  -keystore stackoverflow-search.jks \
  -alias stackoverflow-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=Manpreet Singh, O=ABSA, C=ZA"
```

### How to encode the keystore to base64

```bash
# macOS / Linux
base64 -i stackoverflow-search.jks | pbcopy   # copies to clipboard

# or save to file
base64 -i stackoverflow-search.jks > keystore.b64
cat keystore.b64   # paste this value into the KEYSTORE_BASE64 secret
```

---

## Local Signing Setup (for running release builds locally)

Create `keystore.properties` in the **project root** (never commit this file):

```properties
storeFile=/absolute/path/to/stackoverflow-search.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=stackoverflow-key
keyPassword=YOUR_KEY_PASSWORD
```

Then run:
```bash
./gradlew assembleRelease
```

Make sure `keystore.properties` is in your `.gitignore`:
```
keystore.properties
*.jks
*.keystore
release.keystore
```

---

## Workflow Triggers

| Event | Lint | Unit Tests | Debug APK | Release APK |
|---|:---:|:---:|:---:|:---:|
| Push to `main` | ✅ | ✅ | ✅ | ✅ |
| Push to `develop` | ✅ | ✅ | ✅ | ❌ |
| Pull Request to `main` / `develop` | ✅ | ✅ | ✅ | ❌ |
| Manual dispatch (build_release=true) | ✅ | ✅ | ✅ | ✅ |

---

## Artifacts Produced

| Artifact | Retention | Available on |
|---|---|---|
| `lint-report` | 14 days | All runs |
| `unit-test-results` | 14 days | All runs |
| `coverage-report-html` | 14 days | All runs |
| `coverage-report-xml` | 14 days | All runs |
| `debug-apk` | 7 days | All runs |
| `release-apk` | 30 days | main + manual |

Coverage report is browsable HTML at:
`Actions → Run → coverage-report-html → index.html`

---

## Branch Protection (Recommended)

Go to **Settings → Branches → Add rule** for `main`:

- [x] Require status checks to pass before merging
    - `Lint`
    - `Unit Tests & Coverage`
    - `Build Debug APK`
- [x] Require branches to be up to date before merging
- [x] Require pull request reviews before merging (1 reviewer)
- [x] Do not allow bypassing the above settings