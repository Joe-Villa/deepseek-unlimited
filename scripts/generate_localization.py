#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STRINGS = json.loads((ROOT / "localization/strings.json").read_text(encoding="utf-8"))
LANGS = json.loads((ROOT / "localization/languages.json").read_text(encoding="utf-8"))

def escape(value: str) -> str:
    value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    value = value.replace("'", "\\'").replace('"', '\\"')
    # Keep already-escaped \n sequences as Android newline escapes.
    return value

def write(language, android_dir):
    # English is the source/fallback. zh-CN has a full translation.
    values = {key: item["en"] for key, item in STRINGS.items()}
    if language == "zh-CN":
        values.update({key: item["zh-CN"] for key, item in STRINGS.items()})
    # Optional per-language overrides: localization/overrides/<tag>.json
    override_file = ROOT / "localization/overrides" / f"{language}.json"
    if override_file.exists():
        values.update(json.loads(override_file.read_text(encoding="utf-8")))
    target = ROOT / "app/src/main/res" / android_dir
    target.mkdir(parents=True, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    for key in sorted(values):
        lines.append(f'    <string name="{key}">{escape(values[key])}</string>')
    lines.append('</resources>')
    (target / "strings_deepseek.xml").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{language}: {len(values)} strings -> {android_dir}/strings_deepseek.xml")

for lang in LANGS["languages"]:
    if lang["tag"] == "system":
        continue
    write(lang["tag"], lang["android"])
