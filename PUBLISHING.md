# Publishing ATAK to Maven Central

This document explains the one-time setup and the release process.

---

## One-time setup

### 1. Create a Sonatype account

Go to **https://central.sonatype.com** and sign up (free).  
This is the new Central Portal (launched 2024) — do NOT use the legacy OSSRH Jira.

### 2. Claim the `io.atak` namespace

In the Central Portal:

1. Go to **Namespaces** → **Add Namespace**
2. Enter `io.atak`
3. Sonatype will give you a TXT record to add to your DNS:
   ```
   TXT  atak.io  "< verification code >"
   ```
4. Click **Verify** — once DNS propagates (up to 24h) the namespace is confirmed

> **Alternative:** If you don't own `atak.io`, use `io.github.dimita` as groupId.
> That namespace is auto-verified via GitHub — no DNS needed.
> You would need to change `<groupId>io.atak</groupId>` to `io.github.dimita` everywhere.

### 3. Generate a Central Portal token

In the Portal: **Account** → **Generate User Token**  
Copy the `username` and `password` values.

Add them to `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username><!-- token username --></username>
            <password><!-- token password --></password>
        </server>
    </servers>
</settings>
```

### 4. Generate a GPG key

Maven Central requires all artifacts to be signed.

```bash
# Generate a new key (use your real name + email)
gpg --gen-key

# List keys to get the KEY_ID
gpg --list-secret-keys --keyid-format=long

# Output looks like:
# sec   rsa4096/AABBCCDD11223344 2025-05-01
#                ^^^^^^^^^^^^^^^^ this is your KEY_ID

# Upload the public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys AABBCCDD11223344
gpg --keyserver keys.openpgp.org     --send-keys AABBCCDD11223344
```

Export your private key for GitHub Actions:

```bash
gpg --armor --export-secret-keys AABBCCDD11223344 > private.key
# Copy the content of private.key → GitHub Secret: GPG_PRIVATE_KEY
# Delete the file: rm private.key
```

### 5. Add GitHub Secrets

In your GitHub repository: **Settings** → **Secrets and variables** → **Actions**

| Secret name | Value |
|---|---|
| `CENTRAL_USERNAME` | Token username from step 3 |
| `CENTRAL_PASSWORD` | Token password from step 3 |
| `GPG_PRIVATE_KEY` | Armored private key from step 4 |
| `GPG_PASSPHRASE` | Passphrase you chose when generating the GPG key |

---

## Release process

### Option A — Automated (recommended)

Push a version tag:

```bash
# Make sure main is clean and tests pass
git checkout main
git pull

# Tag the release
git tag v0.1.0
git push origin v0.1.0
```

The `publish.yml` GitHub Action will automatically:
1. Set the version in all POMs
2. Build, test, sign and upload to Sonatype
3. Create a GitHub Release with auto-generated notes

After the upload, log in to **https://central.sonatype.com** → **Deployments**
and click **Publish** to promote the deployment to Maven Central.  
_(Or set `<autoPublish>true</autoPublish>` in pom.xml to skip this step.)_

### Option B — Manual (local machine)

```bash
# Set the release version
mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false

# Build, sign, and upload (skip the sample app)
mvn deploy -Prelease -pl '!atak-sample'

# Restore SNAPSHOT version for next development cycle
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false
git add -A && git commit -m "chore: bump version to 0.2.0-SNAPSHOT"
```

---

## Propagation delay

After publishing, it takes **~15–30 minutes** for the artifacts to appear on:
- https://central.sonatype.com/artifact/io.atak/atak-core
- https://search.maven.org/search?q=g:io.atak

And up to **2 hours** for the CDN (`repo1.maven.org`) to serve them.

---

## Verify the release

```bash
# In a fresh directory, test that the artifact resolves correctly
mvn dependency:get -Dartifact=io.atak:atak-core:0.1.0
```

---

## Versioning policy

ATAK follows [Semantic Versioning](https://semver.org/):

| Version pattern | Meaning |
|---|---|
| `0.x.y-SNAPSHOT` | Development build — not on Central |
| `0.x.0` | Minor release — new features, backward compatible |
| `0.x.y` (y > 0) | Patch release — bug fixes only |
| `1.0.0` | First stable API — breaking changes only in major releases from here |
