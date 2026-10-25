# d2-port

An Android port of **Diablo II: Lord of Destruction**, built on the original game logic as
reconstructed by [D2MOO](https://github.com/ThePhrozenKeep/D2MOO). The goal is a 1:1 port in the
spirit of DevilutionX: the same server code, the same random rolls, the same 25 game frames per
second, the same animations. It is not a remake.

**Target:** unmodified Diablo II: Lord of Destruction at the last official patch, **1.14d**.
D2MOO reproduces patch 1.10f. The build turns on D2MOO's code for later patches, and the 1.14d data
brings the balance changes; what is still missing is listed in
[VERSION-DIFFERENCES](docs/VERSION-DIFFERENCES.md). Mod support comes later.

> **No game files are included and none will be.** You need your own copy of Diablo II: Lord of
> Destruction; the app reads your MPQ archives (`patch_d2.mpq`, `d2exp.mpq`, `d2char.mpq`,
> `d2data.mpq`). Diablo II is a trademark of Blizzard Entertainment; this project is not
> affiliated with or endorsed by Blizzard.

## Status (October 2026)

Early but playable in Act I:

- **Data:** D2Common tables (the game's compiled `.bin` files), Act I map generation (DRLG), collision.
- **Server:** D2Game, the real game server, runs inside the app. A single-player game is created
  the way the original client did it, through an in-memory replacement of D2Net (no network).
- **Movement:** the hero walks and runs by sending the original client commands (packets
  0x01/0x03); speed, pathing and animation come from the game code. Levels are crossed seamlessly
  (town to Blood Moor on foot).
- **Monsters and combat:** monsters are spawned and driven by D2Game's AI. The hero attacks
  (packet 0x06), kills Fallen packs, picks up gold and items (0x13), and dies and revives in town (0x41).
- **Graphics:** DT1 tiles, COF/DCC units and DC6 images are decoded by our own code and drawn in
  depth order, with client-side walk/idle animation of monsters and objects.
- **UI:** automap minimap, life/mana globes, ground items, an inventory panel (move, equip, drop
  items), the first belt row (drink potions) and a virtual stick. The buttons are labelled in Polish.
- **Vendors:** the "Rozmowa" (Talk) button walks to the nearest NPC and talks to her (0x13); the
  store opens next to the inventory (0x38), a tap buys (0x32) or sells (0x33).
- **Saves:** the hero is saved as `<name>.d2s` in the app's files when the app goes to the background and loaded on the next start (D2Game's own save code).
- **1.13c respec:** the Token of Absolution works (ported from the 1.14d binary).

Not done yet: NPC dialogue and quests, hirelings, experience and levelling, skill bar,
sound, Acts II-V. See [docs/ROADMAP.md](docs/ROADMAP.md).

## Quick start

1. Build and install the APK ([docs/BUILDING.md](docs/BUILDING.md)).
2. Copy your four LoD MPQs to `/sdcard/Android/data/pl.d2port/files/import/` and start the app;
   they are imported automatically.

## Documentation

| File | Contents |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | How the pieces fit: D2MOO, our platform layers, the loopback, the app |
| [docs/BUILDING.md](docs/BUILDING.md) | Toolchain, building the APK, getting game data onto a device |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | Test loop on a phone, debugging native crashes, conventions, pitfalls |
| [docs/D2MOO-CHANGES.md](docs/D2MOO-CHANGES.md) | Every change made to D2MOO and squall, and why |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Done, next steps, known issues and unverified parts |
| [docs/VERSION-DIFFERENCES.md](docs/VERSION-DIFFERENCES.md) | Every 1.10f to 1.14d change and how it reaches the port |

## Licence

Our code is MIT (see [LICENSE](LICENSE)). D2MOO (`third_party/D2MOO`) is MIT, squall
(`third_party/D2MOO/external/squall`) is public domain; their licence files are kept with them.

## Po polsku (skrót)

Port Diablo II: Lord of Destruction (patch 1.14d, bez modów) na Androida, oparty na oryginalnej
logice gry z D2MOO (serwer D2Game działa w aplikacji). Repozytorium nie zawiera plików gry —
potrzebna jest własna kopia Diablo II LoD. Dokumentacja techniczna jest po angielsku w `docs/`.
