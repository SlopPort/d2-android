r"""Extract Diablo II game files for the native engine (lower-case paths, '/' separators).

Archives are searched in the game's load order (patch_d2, d2exp, d2data, d2char) of the Diablo II
installation two folders up. With --mods DIR every .mpq in DIR (a mod) is searched first.
Usage: python tools/d2data_extract.py [--mods DIR] OUT_DIR name [name ...]   (names like DATA\GLOBAL\AnimData.d2)
       python tools/d2data_extract.py [--mods DIR] OUT_DIR @list.txt          (one name per line)
"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from assets_mpq import MPQ

GAME = Path(__file__).resolve().parents[3]  # D:/gry/Diablo II
VANILLA = [GAME/'patch_d2.mpq', GAME/'d2exp.mpq', GAME/'d2data.mpq', GAME/'d2char.mpq']

def main():
    args = sys.argv[1:]
    order = VANILLA
    if args and args[0] == '--mods':  # every .mpq in the given folder is searched before the game's
        order = sorted(Path(args[1]).glob('*.mpq')) + VANILLA; args = args[2:]
    out = Path(args[0]); names = args[1:]
    if len(names) == 1 and names[0].startswith('@'):
        names = [l.strip() for l in Path(names[0][1:]).read_text().splitlines() if l.strip()]
    archives = [MPQ(p) for p in order if p.exists()]
    missing = 0
    for name in names:
        key = name.replace('/', '\\')
        for a in archives:
            try: data = a.read(key)
            except (FileNotFoundError, OSError): continue
            dst = out / key.replace('\\', '/').lower()
            dst.parent.mkdir(parents=True, exist_ok=True); dst.write_bytes(data)
            print('ok', name, a.path.name, len(data)); break
        else:
            print('missing', name); missing += 1
    for a in archives: a.close()
    return 1 if missing else 0

if __name__ == '__main__': sys.exit(main())
