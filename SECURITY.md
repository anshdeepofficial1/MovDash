# Security

## Reporting a vulnerability

Please use [GitHub private vulnerability reporting](https://github.com/anshdeepofficial1/ZYLO/security/advisories/new) if enabled. Otherwise contact the maintainer privately. Never include passwords, access tokens, signing material or private media links in a public issue.

## Android release signing

Keep the keystore outside the repository. Configure the following environment variables or equivalent entries in the untracked local.properties file:

- `NUVIO_RELEASE_STORE_FILE` — absolute path or path relative to the repository root
- `NUVIO_RELEASE_STORE_PASSWORD`
- `NUVIO_RELEASE_KEY_ALIAS`
- `NUVIO_RELEASE_KEY_PASSWORD`

Use a newly generated, securely stored key for a new production distribution. A previous development signing key was committed publicly and must be treated as compromised. Removing it from the current branch does not remove it from Git history. Existing APKs signed with it must not be regarded as trusted production releases. Rotating a key may require uninstalling development builds or following the relevant platform signing migration procedure.

Debug builds do not need release signing credentials.
