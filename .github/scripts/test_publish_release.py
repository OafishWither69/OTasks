# SPDX-License-Identifier: GPL-3.0-or-later
import unittest
from publish_release import version_code, verify_certificate


class ReleaseChecks(unittest.TestCase):
    def test_versions_are_canonical_and_increase_across_all_phases(self):
        versions = [v for phase in range(1, 10) for v in (
            f'0.{phase}.0-alpha.1', f'0.{phase}.0-alpha.98', f'0.{phase}.0',
        )] + ['1.0.0-alpha.1', '1.0.0', '1.1.0-alpha.1', '1.1.0-alpha.2',
             '1.1.0-alpha.3', '1.1.0', '1.2.0-alpha.1', '1.2.0']
        codes = list(map(version_code, versions))
        self.assertEqual(codes, sorted(set(codes)))
        self.assertLess(version_code('1.0.999'), version_code('1.1.0-alpha.1'))
        for value in ['01.0.0', '1.100.0', '1.0.1000', '1.0.0-alpha.0', '1.0.0-alpha.99']:
            with self.assertRaises(ValueError):
                version_code(value)

    def test_old_and_new_apksigner_output_and_rejection(self):
        digest = 'ab' * 32
        for label in ['Signer #1', 'V2 Signer', 'V3 Signer']:
            self.assertEqual(digest, verify_certificate(
                f'{label} certificate SHA-256 digest: {digest}', digest.upper(),
            ))
        for output in ['', 'certificate SHA-256 digest: ' + 'cd' * 32,
                       'certificate SHA-256 digest: ' + digest + '\ncertificate SHA-256 digest: ' + 'cd' * 32]:
            with self.assertRaises(AssertionError):
                verify_certificate(output, digest)


if __name__ == '__main__':
    unittest.main()
