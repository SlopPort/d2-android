# Roadmap and status

Target: vanilla Diablo II: Lord of Destruction 1.14d. Mod support comes after the vanilla game.

## Done

- **Build:** native D2Common + D2Game for Android (`armeabi-v7a`); own Fog/D2Lang/D2CMP/Storm layers.
- **World:** game tables, Act I DRLG, collision, tile rendering; all active rooms are drawn, so
  level changes are seamless (town to Blood Moor on foot).
- **Server game:** D2Game single-player game through the D2Net loopback; hero movement by client
  packets 0x01/0x03.
- **Units:** COF/DCC rendering of the hero (with held weapons), objects and monsters/NPCs,
  depth-sorted, at precise positions with client-side walk/idle animation.
- **Monsters:** spawning implemented from decompilation; camp NPCs and Blood Moor Fallen packs
  run their D2Game AI and hit the hero (verified in d2test, seed 777).
- **Combat:** attack with packet 0x06 (left skill on unit); monsters die, corpses stay, loot drops.
- **Items:** ground items drawn from the `items.txt` flippy DC6 (last frame); pick-up with packet
  0x13. In the inventory panel a tap takes an item to the cursor (0x19, 0x1C) and a second tap
  places it (0x18), equips or swaps it (0x1A, 0x1D) or drops it outside the panel (0x17). The
  belt's first row is shown at the bottom; a tap drinks the potion (0x26).
- **Vendors:** "Rozmowa" (Talk) walks to the nearest NPC with the monstats interact flag and
  clicks it (0x13; the server only starts a talk within 9 subtiles); the trade action (0x38)
  shows the store (`buysell.dc6`, `inventory.txt` 'Monster' row) next to the inventory. A tap on
  a store item buys it (0x32, D2MOO's stub implemented from decompilation), on an own item sells
  it (0x33), on the top of the store turns the page; closing the panel ends the talk (0x30).
- **Respec:** Token of Absolution from the 1.14d binary (d2test `D2RESPEC=1`).
- **Death:** hero death and revival in town with packet 0x41 (d2test with `D2DEATH=1`).
- **HUD:** life/mana globes from `hlthmana.dc6`; inventory panel ("Ekwipunek", Inventory) with the layout
  from `inventory.txt` (800x600 rows), the panel from `invchar6.dc6` frames 4-7, items from their
  invfile DC6 at grid cells or centred in body boxes.
- **Automap minimap:** cells from D2Common (`DATATBLS_GetAutomapCellId`) for rooms the hero has
  been near, drawn with `maximap.dc6` frames; revealed rooms are remembered.
- **Developer helpers:** route helper `route.cpp` and the "→ poziom 2 (dev)" / "→ potwór (dev)"
  buttons ("to level 2", "to monster": walk to the next level, to the nearest hostile monster, or explore).

## Next

1. **1.10f → 1.14d.** Catalogued in [VERSION-DIFFERENCES](VERSION-DIFFERENCES.md); the build
   already uses D2MOO's 1.14d switches. Next: check and port the "To do" rows (hirelings, aura and
   skill fixes), then test the Uber Tristram event.
2. **NPCs** — dialogue menu and texts, gambling, repair, hirelings, quests (Den of Evil first,
   then Akara's respec, see VERSION-DIFFERENCES).
3. **Character progression** — experience and levels; UI for stat and skill points (the server
   side already works, d2test `D2RESPEC=1` spends them).
4. **Saving** — `.d2s` writing (D2Game does it; point `FOG_GetSavePath` at app storage).
5. **HUD** — skill bar and the rest of the control panel; full belt, item tooltips.
6. **Combat** — other skills and attack packets (0x05-0x11), hit recovery visuals, missiles.
7. **Sound** and the rest of the client side (server→client packets: overhead text, state updates).
8. **Automap polish** — line-of-sight reveal, level names, exits and unit markers.
9. **Acts II-V.**
10. **Mod support** — loading extra MPQs before the game's (`DataImport` already orders unknown
    archives first).

## Known issues / unverified

- The app is 32-bit only (`armeabi-v7a`, because D2 structs are 32-bit), so Android shows a
  compatibility warning on install/start. Expected.
- Tile order returned by `D2CMP_10088_GetTiles` (`cmp_android.cpp`) is assumed (affects which
  variant a rarity roll picks, not the number of rolls).
- `FOG_ComputeStringCRC16`, `FOG_ComputeChecksum`, `FOG_Encode14BitsToString` are our own
  algorithms (names/checksums, not gameplay).
- Monster SpawnCol=1 placement offsets (see D2MOO-CHANGES.md).
- Ground item centring (by half the DC6 width) is not verified against D2Client.
- No lighting/palette shifts, no unit shadows, no overlays (auras, missiles).
- Virtual stick: the target distance for a given tilt is our own choice.
