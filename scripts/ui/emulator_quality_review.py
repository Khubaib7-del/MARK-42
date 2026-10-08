"""Explicit emulator-only UI/motion checks; restores display preferences and turns the test VPN off."""
import argparse
import json
import re
import subprocess
import time
from emulator_review import Emulator


def center(node):
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.get('bounds')))
    return str((x1 + x2) // 2), str((y1 + y2) // 2)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--adb', required=True)
    parser.add_argument('--output', default='review/polish-2026-10-09')
    args = parser.parse_args()
    e = Emulator(args.adb, args.output)
    if e.run('shell', 'getprop', 'ro.kernel.qemu').strip() != '1':
        raise RuntimeError('Emulator required')
    results = []

    def tap(text=None, description=None, class_name=None):
        if not e.tap(text=text, description=description, class_name=class_name):
            raise RuntimeError('Missing control: ' + str(text or description or class_name))

    def find_scrolled(text=None, class_name=None, checked=None):
        for _ in range(5):
            if checked is not None:
                control = next((n for n in e.nodes() if n.get('checkable') == 'true'
                                and n.get('checked') == str(checked).lower()), None)
                if control is not None:
                    e.run('shell', 'input', 'tap', *center(control))
                    time.sleep(1)
                    return
            if e.tap(text=text, class_name=class_name):
                return
            e.run('shell', 'input', 'swipe', '540', '1700', '540', '600', '350')
        raise RuntimeError('Missing scrolled control: ' + str(text or class_name))

    def wait_text(text):
        for _ in range(10):
            if any(text in n.get('text', '') for n in e.nodes()):
                return
            time.sleep(0.5)
        raise RuntimeError('Expected text not visible: ' + text)

    tap(text='Home')
    tap(text='Settings')
    find_scrolled(text='Read recent app exits')
    wait_text('App version:')
    e.capture('runtime-diagnostics')
    results.append('Runtime diagnostics readable')
    find_scrolled(text='Delete all recorded data')
    wait_text('Delete all recorded data?')
    e.capture('delete-confirmation')
    tap(text='Cancel')
    results.append('Deletion cancellation checked; no data deleted')
    e.run('shell', 'input', 'keyevent', '4')
    wait_text('SECURITY POSTURE')
    results.append('Back from Settings returns to Home')

    original = {}
    keys = [('system', 'font_scale'), ('system', 'accelerometer_rotation'), ('system', 'user_rotation'),
            ('global', 'animator_duration_scale')]
    for namespace, key in keys:
        original[(namespace, key)] = e.run('shell', 'settings', 'get', namespace, key).strip()
    try:
        e.run('shell', 'settings', 'put', 'system', 'font_scale', '1.5')
        time.sleep(2)
        e.capture('home-large-font')
        e.run('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
        e.run('shell', 'settings', 'put', 'system', 'user_rotation', '1')
        time.sleep(2)
        e.capture('home-landscape')
        results.append('Home captured at 150% font scale and landscape')
    finally:
        for (namespace, key), value in original.items():
            if value == 'null':
                e.run('shell', 'settings', 'delete', namespace, key)
            else:
                e.run('shell', 'settings', 'put', namespace, key, value)
    time.sleep(2)
    tap(text='Home')
    e.run('shell', 'input', 'swipe', '540', '500', '540', '1800', '350')
    time.sleep(1)
    replay = next(n for n in e.nodes() if n.get('content-desc') == 'Replay logo introduction')
    x, y = center(replay)
    recording = subprocess.Popen([*e.command, 'shell', 'screenrecord', '--time-limit', '7',
                                  '--bit-rate', '2000000', '/sdcard/selvard-intro-review.mp4'],
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(0.7)
    e.run('shell', 'input', 'tap', x, y)
    recording.communicate(timeout=20)
    if recording.returncode:
        raise RuntimeError('Intro recording failed')
    e.run('pull', '/sdcard/selvard-intro-review.mp4', str(e.output / 'intro-replay.mp4'))
    wait_text('SECURITY POSTURE')
    results.append('Full intro replay recorded and returned to Home')
    try:
        e.run('shell', 'settings', 'put', 'global', 'animator_duration_scale', '0')
        tap(description='Replay logo introduction')
        wait_text('SECURITY POSTURE')
        results.append('Disabled-motion replay returned to Home')
    finally:
        value = original[('global', 'animator_duration_scale')]
        e.run('shell', 'settings', 'put', 'global', 'animator_duration_scale', value if value != 'null' else '1')
    tap(text='Shield')
    tap(text='Apps')
    tap(text='Scan installed apps')
    time.sleep(3)
    wait_text('Scan again')
    e.capture('apps-after-scan')
    results.append('Real emulator package scan finished')
    tap(text='Network')
    e.capture('network-final')
    find_scrolled(checked=False)
    # Android permission dialog appears on first use; approved only in this test emulator.
    e.tap(text='OK')
    try:
        wait_text('local DNS filtering')
        e.capture('network-on')
        pid = e.run('shell', 'pidof', 'app.selvard').strip()
        for elapsed in range(20, 121, 20):
            time.sleep(20)
            if e.run('shell', 'pidof', 'app.selvard').strip() != pid:
                raise RuntimeError('Process changed during active-VPN run')
            print('VPN idle seconds', elapsed, 'PID', pid, flush=True)
        wait_text('local DNS filtering')
        results.append('Active local VPN: 120 seconds, stable process and running state')
    finally:
        # Stop through the app's real UI, preserving its private service boundary.
        find_scrolled(checked=True)
        wait_text('not filtering')
    tap(text='Home')
    time.sleep(2)
    e.capture('home-final')
    e.logs()
    (e.output / 'quality-results.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    print(json.dumps(results, indent=2), flush=True)


if __name__ == '__main__':
    main()
