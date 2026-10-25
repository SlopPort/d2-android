# Changes to third-party code

D2MOO upstream commit: see `third_party/D2MOO-UPSTREAM.txt`. The full diff of D2MOO is in
`third_party/D2MOO-android.patch` (regenerate it whenever D2MOO files change).

## D2MOO

| File | Change | Why |
| --- | --- | --- |
| `Fog/include/Fog.h`, `D2Common/include/Drlg/D2DrlgDrlg.h`, `D2Game/include/SKILLS/Skills.h`, `D2Game/src/SKILLS/Skills.cpp` | function-pointer fields/params use `D2AnyFnPtr` on Android | MSVC converts function pointers to `void*` implicitly in table initialisers, clang does not |
| `D2Common/src/DataTbls/LevelsTbls.cpp` | automap level types: numeric names accepted, table of 64 instead of 36 | robustness for modded data tables with more than 36 level types; the 1.10 array would overflow into `pLvlMazeTxt` |
| `D2Common/src/DataTbls/DataTbls.cpp` | `.bin` record size check; compile the `.txt` when it differs | robustness for data tables in a different record layout (e.g. an `itemstatcost.bin` with 336-byte records instead of 1.10's 324) |
| `D2Game/src/GAME/Game.cpp` | `va_list` init, two struct-size asserts off, all log levels to logcat, `gnGamesGUIDs` aliases `hGameArray` | clang; squall packs differently (internal only); diagnostics; one array at 0x6FD447F8 in the original |
| `D2Game/src/PLAYER/PlayerList.cpp` | `dword_6FD4DC40` is the variable from `SUnit.cpp` | one original variable, declared twice with different types |
| `D2Game/src/PLAYER/PlrSave.cpp` | without a save file, create a new character from its class template (expansion flag from the game) | the original client wrote a fresh `.d2s` first; we have no client-side character creation yet |
| `D2Game/src/UNIT/Party.cpp` | assert `!pPartyControl` | inverted check in D2MOO (fails on PC too) |
| `D2Game/src/OBJECTS/ObjRgn.cpp` | object regions stored at `[i]`, not `[0]` | D2MOO put every level's region in slot 0; populating the first non-town room crashed |
| `D2Game/src/MONSTER/MonsterSpawn.cpp` | implemented `D2GAME_SpawnNormalMonster_6FC68E30` and `MONSTERSPAWN_SetupNormalMods` from their decompilation comments | stubs: no monster or NPC was ever created |
| `D2Game/src/UNIT/SUnitInactive.cpp` | `SUNITINACTIVE_CompressUnitIfNeeded` reads the unit type once, before compressing | compressing a ground item frees it; D2MOO then read `dwUnitType` from freed memory (the original kept it in a register) and crashed when the hero left loot behind |
| `D2Game/src/ITEMS/ItemMode.cpp` | `sub_6FC4A9B0` equips the item at the body location `sub_6FC42F20` found | D2MOO passed the caller's 0 (meant as "skip requirement check") on as the body location, so picking up an equippable item asserted |
| `D2Game/src/PLAYER/PlayerStats.cpp`, `D2Game/src/ITEMS/ItemMode.cpp` | added `PLAYERSTATS_ResetStats`/`ResetSkills` and the Token of Absolution (`toa`) branch of the quest-item use, from 1.14d `Game.0x570C80`, `0x570360`, `0x55E52A` (1.13+ builds only) | respec is a 1.13c feature D2MOO does not have; every call was matched to its D2MOO function by its code |
| `D2Game/src/UNIT/SUnitNpc.cpp` | implemented `sub_6FCC88B0` (buying from a vendor) from its decompilation comment | stub that returned 0: nothing could be bought |
| `D2Game/src/PLAYER/PlrSave2.cpp` | `PLRSAVE2_CreateSaveFile` writes the size and checksum at the start of the file; `PLRSAVE2_ProcessSaveFile` checks the quest section's own header ('Woo!', 6) | the header writer advances `pData`, so size and checksum went into the quest section and the file was cut short; the loader compared the quest magic with the file header, so no save could be loaded |
| `D2Common/src/Path/Step.cpp` | the 1.13c call `PATH_RecacheRoomIfNeeded(path, x, y, room)` is skipped on Android | D2MOO has no such overload; the 1.14d build keeps 1.10f behaviour there (`UNVERIFIED`) |

`MonsterSpawn.cpp` was checked against 1.14d `Game.0x5B2A00`/`0x5B21B0` by two independent passes; the
SpawnCol=1 neighbour offsets come from the table at `Game.0x6E2D50`.

## squall (`third_party/D2MOO/external/squall`, upstream commit in D2MOO-UPSTREAM.txt)

| File | Change | Why |
| --- | --- | --- |
| `storm/list/TSLink.hpp`, `storm/hash/TSHashTable.hpp` | end-of-list test `SquallIsListEnd` (null or odd pointer) instead of `pointer <= 0` | the end marker is `~address`; on Android heap addresses are above 0x80000000 |
| `storm/thread/CCritSect.cpp` | recursive mutex | Win32 critical sections are re-entrant; D2Game relies on it |

## Built but not taken from D2MOO

D2MOO's `Fog.cpp`, `D2LangStubs.cpp`, `D2CMP.cpp` (all stubs), `D2Game/src/GAME/Task.cpp` (Windows
worker threads) and the `DllMain.cpp` files are excluded in `app/src/main/cpp/CMakeLists.txt`; our
`*_android.cpp` files replace them.

## Upstream

Fixes sent to ThePhrozenKeep/D2MOO: #309 (inactive item use-after-free), #310 (object region index)
and #311 (party control assert) are merged; #312 (vendor buying) and #315 (monster spawning) are in review.

Game-logic stubs left in upstream D2MOO (master 2f912ff, 2026-10-25): functions whose body is only a
`return` above commented-out decompilation, found with a scan of all `.cpp` files.

| Function | File | Effect | Here |
| --- | --- | --- | --- |
| `D2GAME_SpawnNormalMonster_6FC68E30`, `MONSTERSPAWN_SetupNormalMods` | `MONSTER/MonsterSpawn.cpp` | no monster or NPC is ever spawned | implemented, checked against 1.14d (#315) |
| `sub_6FCC88B0` | `UNIT/SUnitNpc.cpp` | nothing can be bought | implemented, checked against 1.14d (#312) |
| `D2GAME_SAVE_ProcessSaveFile_6FC8C0C0` | `PLAYER/PlrSave.cpp` | only saves of format version 91 or lower (before 1.08) cannot be loaded; 1.10-1.14d saves (version 96) go through the implemented `PLRSAVE2_ProcessSaveFile` | not needed for 1.14d |

The other stubs found (D2Win edit boxes and controls, D2Gfx tile drawing) belong to the original
client's interface and renderer, which this port does not use.
