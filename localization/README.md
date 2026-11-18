# Localization

This is the source of truth for all custom DeepSeek Unlimited UI strings.

- `languages.json` — list of selectable languages and their Android resource qualifiers.
- `strings.json` — every `ds_*` key with English and Simplified Chinese.
- `overrides/<tag>.json` — optional per-language overrides (not yet populated).
- `../scripts/generate_localization.py` — regenerates `app/src/main/res/values*/strings_deepseek.xml`.

## Current behavior

- Default is `system` (follow the device language).
- English + Simplified Chinese are complete.
- Every other listed language has a generated Android resource folder; missing keys
  fall back to English. This keeps the language picker useful without shipping
  unreviewed machine translations.
- `SupportedLanguages.kt` contains the picker labels (native names).

## Add or change a string

1. Add the key to `strings.json` (`en` and `zh-CN`).
2. Use `stringResource(R.string.ds_...)` in Compose, or `context.getString(...)`
   outside Compose.
3. Run `python3 scripts/generate_localization.py`.
4. Rebuild.

## Add a language

1. Add an entry to `languages.json`.
2. Add it to `SupportedLanguages.kt` if it should appear in the picker.
3. Optionally add `overrides/<tag>.json` with translated keys.
4. Regenerate resources.

Do not edit generated `strings_deepseek.xml` by hand.
