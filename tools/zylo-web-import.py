"""Apply targeted ZYLO branding to a pinned, unpacked web source tree."""
from pathlib import Path
import json
import re
import shutil
import subprocess

root = Path(__file__).resolve().parents[1]
web = root / "website"
native = root / "composeApp/src/commonMain/composeResources/drawable"
brand = web / "assets/brand"
assert (web / "LICENSE").is_file()
assert (web / "package.json").is_file()
# Preserve upstream documentation/credit and legal text verbatim.
shutil.copyfile(web / "README.md", web / "UPSTREAM_README.md")
# Use Android's real logo assets; preserve upstream filenames referenced by runtime code.
shutil.copyfile(native / "app_logo_wordmark.png", brand / "app_logo_wordmark.png")
icon = native / "app_icon_original.png"
shutil.copyfile(icon, brand / "app_logo_mark.png")
shutil.copyfile(icon, brand / "nuvio-favicon.png")
for name, size in [
    ("pwa-apple-touch-icon-180.png", 180), ("pwa-icon-192.png", 192),
    ("pwa-icon-512.png", 512), ("pwa-icon-maskable-192.png", 192),
    ("pwa-icon-maskable-512.png", 512),
]:
    cmd = ["convert", str(icon), "-resize", f"{size}x{size}", "-background", "#0d0d0d", "-gravity", "center", "-extent", f"{size}x{size}", str(brand / name)]
    if "maskable" in name:
        inset = round(size * 0.72)
        cmd[3] = f"{inset}x{inset}"
    subprocess.run(cmd, check=True)

# Preserve internal names, URL paths, and protocol identifiers for compatibility.
def brand_text(s):
    return re.sub(r"(?<![\w/.-])Nuvio(?:Web| TV)?(?![\w/.-])", "ZYLO", s)

for file in [web/"index.html", web/"manifest.webmanifest", web/"boot-guard.js", web/"sw.js", *web.glob("res/values*/strings.xml")]:
    file.write_text(brand_text(file.read_text(encoding="utf-8")), encoding="utf-8")

# Target visible JS copy in UI files; leave tests, URLs, internal IDs and imports intact.
# Only replace standalone names in HTML text and explicitly labeled strings.
for file in (web/"js/ui").rglob("*.js"):
    if ".test." in file.name:
        continue
    content = file.read_text(encoding="utf-8")
    changed = re.sub(r'(?<=>)Nuvio(?:Web| TV)?(?=<)', "ZYLO", content)
    changed = re.sub(r'(?<=alt=")Nuvio(?:Web| TV)?(?=")', "ZYLO", changed)
    changed = re.sub(r'(?<=aria-label=")Nuvio(?:Web| TV)?(?=")', "ZYLO", changed)
    if changed != content:
        file.write_text(changed, encoding="utf-8")

repo = "https://github.com/anshdeepofficial1/ZYLO"
p = web/"package.json"
package = json.loads(p.read_text())
package["name"] = "zylo-web"
package["repository"]["url"] = "git+" + repo + ".git"
package["bugs"]["url"] = repo + "/issues"
package["homepage"] = repo + "/tree/main/website"
package["nuvioFork"]["maintainer"] = "anshdeepofficial1"
p.write_text(json.dumps(package, indent=2)+"\n")
p = web/"package-lock.json"
lock = json.loads(p.read_text())
lock["name"] = "zylo-web"
if "" in lock.get("packages", {}):
    lock["packages"][""]["name"] = "zylo-web"
p.write_text(json.dumps(lock, indent=2)+"\n")
for p in [web/"scripts/appMetadata.mjs", web/"js/core/app/appIdentity.js"]:
    content = p.read_text()
    content = content.replace('name: "NuvioWeb"', 'name: "ZYLO"')
    content = content.replace('maintainer: "alphasquare"', 'maintainer: "anshdeepofficial1"')
    content = content.replace("https://github.com/alphasquare404/NuvioWeb", repo)
    content = content.replace("https://api.github.com/repos/alphasquare404/NuvioWeb", "https://api.github.com/repos/anshdeepofficial1/ZYLO")
    p.write_text(content)
# No upstream hosted sign-in credentials or remote relay defaults in the fork's example.
(web/"local.example.properties").write_text("PROBE_MODE=off\nREMUX_MODE=off\nTRANSCODE_MODE=none\nSIMKL_APP_NAME=ZYLO\n")
(web/"README.md").write_text("# ZYLO Web\n\nBrowser and PWA media client. See [setup and deployment](ZYLO_WEB.md).\n\n[Upstream credits and documentation](UPSTREAM_README.md) · [GPL-3.0 license](LICENSE).\n")
