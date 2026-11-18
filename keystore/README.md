# Verification-only debug keystore

This is a copy of the standard Android debug keystore used to sign the released APK.
It exists so a clean checkout can reproduce the same signed artifact without writing
to `~/.android` (which is read-only in some build sandboxes).

- Alias: `androiddebugkey`
- Store password: `android`
- Key password: `android`
- Certificate SHA-256: `88e927c08e54abc623d15e7d8a3b455cb489f78271f4cc1f8fa97cd275b41d61`

**Do not use this key for a production release.** It is the public Android debug key.
