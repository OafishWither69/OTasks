# SPDX-License-Identifier: GPL-3.0-or-later
"""Validate the signed APK, monotonic release code, and publish the tagged main commit."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess

def run(*args):
    return subprocess.check_output(args, text=True).strip()

def version_code(version):
    match = re.fullmatch(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(?:-alpha\.([1-9][0-9]*))?", version)
    if not match:
        raise ValueError("Invalid version")
    major, minor, patch = map(int, match.groups()[:3])
    sequence = int(match[4]) if match[4] else 99
    if major > 199 or minor > 99 or patch > 999 or (match[4] and not 1 <= sequence <= 98):
        raise ValueError("Version out of range")
    return major * 10000000 + minor * 100000 + patch * 100 + sequence

def verify_certificate(signature, expected):
    expected = expected.replace(':', '').lower()
    digests = set(re.findall(r'certificate SHA-256 digest: ([a-fA-F0-9]{64})', signature))
    assert {digest.lower() for digest in digests} == {expected}, 'Unexpected or missing signing certificate'
    return expected

def main():
    version = os.environ['APP_VERSION']
    assert os.environ['GITHUB_REF_NAME'] == 'v' + version
    code = version_code(version)
    repository = os.environ['GITHUB_REPOSITORY']
    pages = run('gh', 'api', '--paginate', '--slurp', f'repos/{repository}/releases?per_page=100')
    for release in (release for page in json.loads(pages) for release in page):
        if release['draft']:
            continue
        tag = release['tag_name']
        if tag.startswith('v'):
            assert code > version_code(tag[1:]), 'Release versionCode must exceed every published build'
    apk = Path('app/build/outputs/apk/release/app-release.apk')
    sdk = Path(os.environ['ANDROID_HOME'])
    apksigner = sorted((p for p in (sdk/'build-tools').glob('*/apksigner') if re.fullmatch(r'[0-9]+\.[0-9]+\.[0-9]+', p.parent.name)), key=lambda p: tuple(map(int, p.parent.name.split('.'))))[-1]
    signature = run(str(apksigner), 'verify', '--print-certs', str(apk))
    certificate = verify_certificate(signature, os.environ['RELEASE_CERT_SHA256'])
    output = Path(f'OTasks-{version}.apk')
    shutil.copyfile(apk, output)
    checksum = hashlib.sha256(output.read_bytes()).hexdigest()
    checkfile = output.with_suffix('.apk.sha256')
    checkfile.write_text(f'{checksum}  {output.name}\n', encoding='utf-8')
    changelog = Path('CHANGELOG.md').read_text(encoding='utf-8')
    section = changelog.split(f'## [{version}]', 1)[1].split('\n## ', 1)[0].strip()
    progress = Path('PROGRESS.md').read_text(encoding='utf-8')
    checks = progress.split('### Phase 1', 1)[1].split('### Phase 2', 1)[0]
    known = progress.split('## Known issues', 1)[1].split('\n## ', 1)[0]
    notes = Path('release-notes.md')
    notes.write_text(section + '\n\nWhat to test\n' + checks + '\nKnown issues\n' + known + f'\nInstall over the existing OTasks release. Keep the same signing certificate.\n\nAPK SHA-256: {checksum}\nCertificate SHA-256: {certificate}\nVersion code: {code}\n', encoding='utf-8')
    args = ['gh', 'release', 'create', 'v'+version, str(output), str(checkfile), '--title', f'OTasks {version} — Alpha (Phase 1: Foundation and data)', '--notes-file', str(notes)]
    args += ['--prerelease'] if '-alpha.' in version else ['--latest']
    subprocess.run(args, check=True)

if __name__ == '__main__':
    assert version_code('1.0.10') < version_code('1.1.0-alpha.1')
    assert version_code('0.3.0-alpha.2') == 300002
    main()
