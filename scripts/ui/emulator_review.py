"""Explicit, local debug-build review. Uses synthetic inputs; never adds identities."""
import argparse
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path


class Emulator:
    def __init__(self, adb, output, serial="emulator-5554"):
        self.adb = adb
        self.command = [adb, "-s", serial]
        self.output = Path(output)
        self.output.mkdir(parents=True, exist_ok=True)

    def run(self, *args, binary=False):
        result = subprocess.run([*self.command, *args], capture_output=True, timeout=30, check=True)
        return result.stdout if binary else result.stdout.decode("utf-8", errors="replace")

    def nodes(self):
        self.run("shell", "uiautomator", "dump", "/sdcard/selvard-review.xml")
        return ET.fromstring(self.run("exec-out", "cat", "/sdcard/selvard-review.xml")).iter("node")

    def tap(self, text=None, description=None, class_name=None):
        for node in self.nodes():
            matches = (
                (text is not None and node.get("text") == text)
                or (description is not None and node.get("content-desc", "").startswith(description))
                or (class_name is not None and node.get("class") == class_name)
            )
            if matches:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
                self.run("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
                time.sleep(1)
                return True
        return False

    def capture(self, name):
        data = self.run("exec-out", "screencap", "-p", binary=True)
        if not data.startswith(b"\x89PNG"):
            raise RuntimeError("Invalid screenshot response")
        (self.output / (name + ".png")).write_bytes(data)
        print("Captured", name, flush=True)

    def logs(self):
        (self.output / "crash-log.txt").write_text(self.run("logcat", "-b", "crash", "-d"), encoding="utf-8")
        (self.output / "runtime-log.txt").write_text(
            self.run("logcat", "-d", "-t", "3000", "-v", "threadtime"), encoding="utf-8",
        )
        (self.output / "exit-info.txt").write_text(
            self.run("shell", "dumpsys", "activity", "exit-info", "app.selvard"), encoding="utf-8",
        )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--adb", required=True)
    parser.add_argument("--serial", default="emulator-5554")
    parser.add_argument("--output", default="review/emulator")
    parser.add_argument("--idle-seconds", type=int, default=0)
    args = parser.parse_args()
    device = Emulator(args.adb, args.output, args.serial)
    if device.run("shell", "getprop", "ro.kernel.qemu").strip() != "1":
        raise RuntimeError("This review script only runs on an Android emulator")
    device.tap(text="Skip intro")
    for page in range(3):
        device.capture("onboarding-" + str(page + 1))
        if not device.tap(text="Continue"):
            break
    device.tap(text="Enter Selvard")
    device.capture("home")
    destinations = [
        ("Shield", "links"), ("Timeline", "timeline"),
        ("Identity", "identity"), ("Settings", "settings"),
    ]
    for title, name in destinations:
        if not device.tap(text=title):
            raise RuntimeError("Destination not found: " + title)
        device.capture(name)
    device.tap(text="Shield")
    for title, name in [("Apps", "apps"), ("Privacy", "privacy"), ("Network", "network")]:
        if not device.tap(text=title):
            raise RuntimeError("Pane not found: " + title)
        device.capture(name)
    device.tap(text="Home")
    initial_pid = device.run("shell", "pidof", "app.selvard").strip()
    started = time.monotonic()
    while time.monotonic() - started < args.idle_seconds:
        time.sleep(min(20, args.idle_seconds - (time.monotonic() - started)))
        pid = device.run("shell", "pidof", "app.selvard").strip()
        print("Idle seconds", round(time.monotonic() - started), "PID", pid, flush=True)
        if pid != initial_pid:
            raise RuntimeError("App process changed during idle run")
    device.capture("home-after-idle")
    device.logs()
    print("Crash buffer:", device.run("logcat", "-b", "crash", "-d"), flush=True)


if __name__ == "__main__":
    main()
