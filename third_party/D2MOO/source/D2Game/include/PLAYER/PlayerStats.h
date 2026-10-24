#pragma once

#include <D2BuildInformation.h>
#include <Units/Units.h>


//D2Game.0x6FC7EA50
void __fastcall PLAYERSTATS_SetStatsForStartingAct(D2GameStrc* pGame, D2UnitStrc* pUnit, uint8_t nAct);
//D2Game.0x6FC7EC00
void __fastcall PLAYERSTATS_LevelUp(D2GameStrc* pGame, D2UnitStrc* pUnit);
//D2Game.0x6FC7EDE0
void __fastcall PLAYERSTATS_OnPlayerLeveledUp(D2GameStrc* pGame, D2UnitStrc* pUnit, void* pLeveledUpPlayer);
//D2Game.0x6FC7EDF0
int32_t __fastcall PLAYERSTATS_SpendStatPoint(D2UnitStrc* pUnit, int32_t nStatId);

#if D2_VERSION_MAJOR >= 1 && D2_VERSION_MINOR >= 13
//1.14d: Game.0x570C80
void __fastcall PLAYERSTATS_ResetStats(D2GameStrc* pGame, D2UnitStrc* pUnit);
//1.14d: Game.0x570360
void __fastcall PLAYERSTATS_ResetSkills(D2GameStrc* pGame, D2UnitStrc* pUnit);
#endif
