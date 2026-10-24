# 1.10f → 1.14d

D2MOO reconstructs version 1.10f. The target is the last official patch, 1.14d. This page lists
what changed between them (from the patch notes shipped with the game, `Patch.txt`) and how each
change reaches the port.

There are four ways:

- **Data:** the change is in the 1.14d table files (`.txt`/`.bin` in `patch_d2.mpq` and the game
  archives). It arrives with the user's 1.14d data; no code needed.
- **Switch:** D2MOO already has the code behind a version check. The build sets
  `D2_GAME_VERSION=114D` (default in `app/src/main/cpp/CMakeLists.txt`), which defines
  `D2_VERSION_MINOR=14` and `D2_VERSION_113C`, so these parts are compiled in.
- **To do:** game code that changed and is not in D2MOO yet, or that has not been checked.
- **Done:** ported by us from the 1.14d `Game.exe` (addresses in the code comments).

## Gameplay

| Patch | Change | How |
| --- | --- | --- |
| 1.11 | Uber Tristram and Pandemonium event (portals, Uber bosses and their AI, Hellfire Torch) | Switch (`D2_VERSION_HAS_UBERS`); not tested in game yet |
| 1.11 | 10 new runewords, new uniques from special bosses | Data |
| 1.11 | Hirelings for hire 1-5 levels below the player; faster hireling XP | To do (check code) |
| 1.11 | Fixes: Diablo's seals, "missile firing too far", set bonuses kept after death, Trang-Oul invincibility, charged skills giving synergies, ethereal stats kept by transmute | To do (check each) |
| 1.13c | Respec with the Token of Absolution | Done, from the 1.14d binary (d2test `D2RESPEC=1`) |
| 1.13c | Free respec from Akara after the Den of Evil | To do, with the NPC dialogue menu (see below) |
| 1.13c | Higher high-rune drop rates, Fire Enchanted explosion damage, Oblivion Knight without Iron Maiden, Hellfire Torch proc 5%, Fangskin Hell loot | Data |
| 1.13c | Uber minions give no experience | Switch |
| 1.13c | Uber Mephisto checks both Uber Diablo and Uber Baal | To do (check) |
| 1.13c | Gold in stash: flat cap, not bound to level | To do (check) |
| 1.13c | Skill balance (Immolation Arrow, Dragon Claw/Talon, Shadow Master, combo 15 s, Whirlwind mana, masteries, Blessed Hammer vs undead/demons, Werebear, Shockwave, Blood Golem life sharing, Corpse Explosion, Poison Nova, Firewall/Blaze synergies, Hydra) | Mostly data; Blessed Hammer, Blood Golem and combo duration to check |
| 1.13c/d | Fixes: aura stacking, mercenary multiple auras, auras after mercenary revive, Principle runeword stats, Charge and Leap lock-ups | To do (check each) |
| 1.13c | Path code: rooms re-cached during a step, extra null checks | Switch, except one step in `Step.cpp` that D2MOO cannot build (kept as 1.10f, `UNVERIFIED`) |
| 1.14b | Mercenaries no longer become "An Evil Force" | To do (check) |

### Notes: Akara's respec in 1.14d

A quest record (id 30) in the `a1q1.cpp` code, using quest flag 0x29:

- NPC menu command 0x94 is handled at `Game.0x57A1C5`.
- `Game.0x58FD20` grants the flag in Hell to characters that finished the Den of Evil earlier.
- `Game.0x58FD50` resets stats and skills with the same functions as the token, then clears the flag.
- The quest callbacks start at `Game.0x58FDD0`.

## Client and interface

These belong to the game client, which the app replaces with its own:

| Patch | Change | Plan |
| --- | --- | --- |
| 1.13c | Orange name for runes and Uber items | Do when item names are drawn |
| 1.13c | Text over the life/mana globes, toggled by tapping | Do with the HUD |
| 1.13c | Hardcore characters without finishing softcore first | Do with character creation |
| 1.13c | Difficulty keys R/N/H when creating a game | Not needed (touch UI) |
| 1.14b | Frame rate cap 200 | Not needed (the app draws at the game's 25 frames + display rate) |

Battle.net, installer, Mac, windowed-mode and crash-report changes do not apply to a single-player
Android game.
