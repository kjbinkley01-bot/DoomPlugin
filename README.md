# Delve Companion

A personal RuneLite plugin for the Doom of Mokhaiotl. See [SCOPE.md](SCOPE.md) for the full scope and the
reasoning behind what is and isn't included.

## Features

### Burrow-hole risk / reward
Shown in the side panel during a run, and as an overlay while the end-of-level interface is open:
- GE value of your unclaimed loot (read from the game's loot-pile container), and whether a unique is in it
- Next delve: unique rate and pool, pet rate, demon tears
- Chance of at least one unique between the next delve and your **Target delve** (config)
- Optional chat warning when the unclaimed loot is worth more than a set amount

### Post-delve debrief
Recorded during the fight and shown only after each level ends (chat line + side panel, optionally saved to
`.runelite/delve-companion/delves.jsonl`):
- Damage taken by source: orbs (by style), falling rocks, shockwave, beam, burrow/trample/slam, tongue lash,
  larvae, acid/venom, unknown
- Orbs that hit while you were on the wrong prayer, plus the game's own missed-orb counter
- Larvae spawned / killed / absorbed, acid steps
- Time in normal, shield and burrow phases
- Food and potions used, special attack energy used
- A run summary when you leave

### Hotkey timers (shockwave and rock throw)
You start these yourself, the same way the wiki recommends using Visual Metronome:
- **Shockwave**: press when volatile earth spawns. It counts down 20 ticks to the first shockwave.
- **Rock**: press when the Doom throws its rock. It counts down 7 ticks to the explosion.

Bind the keys under *Manual timers* in the plugin config (they're unbound by default). Use keys you don't type
with, such as F-keys, because the key press still reaches the game.

### Recolour (grey area, off by default)
These change the colour of graphics the game already draws. They add no new markers, and nothing shows up earlier or
anywhere the game doesn't already show it. Jagex's rules neither allow nor ban this explicitly.
- **Rock shadows:** recolours the falling-rock graphics (pink by default). The default IDs (3388-3403) are the
  falling-rock projectiles. If the shadow turns out to be a separate graphic, find its ID with
  *Log Doom events* and put it in **Rock graphic IDs**. If the shadow is part of the rock model, switch
  **Rock recolour mode** to *Dark faces only* so just the shadow changes colour.
- **Acid:** recolours the acid blood pools (cyan by default).

Recolouring edits the game's cached models, so a colour can stay after you turn it off, until the area reloads
(for example, the next delve or a relog). This hasn't been checked in-game yet; if nothing changes colour, the
graphic is probably rebuilt every frame and needs a different approach.

## Running it

Requires Java 11+.

```
./gradlew run        # launches a developer-mode RuneLite client with the plugin loaded
./gradlew test       # unit tests
./gradlew shadowJar  # standalone jar in build/libs/
```

## Checking it in-game

The NPC, object, animation and graphic IDs come from RuneLite's gameval tables, but how they map to damage
sources hasn't been checked in-game yet. Turn on **Developer → Log Doom events**, do a delve, then look at
`.runelite/logs/client.log` for `[delve]` lines. They show each hit, what it was attributed to, and the boss
animations, graphics and projectiles around it. Hits marked `UNKNOWN` show where the mapping in
`HitClassifier` / `DelveCompanionPlugin` needs adjusting.

Things most likely to need adjusting:
- The level-complete chat message format for delves 1–7 (only the 8+ format is confirmed). If it doesn't match,
  the plugin closes the record 5 ticks after the boss dies and uses its own timing.
- Whether the Doom fires `ActorDeath` when it burrows down at the end of a level.
- Which graphics belong to the shockwave and which to the car-phase slam.

## Mechanics notes

**Rock throw:** the rock explodes 7 ticks after it's thrown. Falling rocks target your tile, marked by a
shadow, and your tile turns into a solid rock, so take one step off it. When the rock explodes, your
protection prayer is switched off. Turn it back on for the follow-up projectiles: 1 at delve 1, 2 at delves
2–3, 3 from delve 4. They are only ever ranged or magic, and never the same style twice in a row. From delve 8
there are two rocks per throw; pray after the second one explodes.

**Shockwave:** volatile earth spawns, and the first shockwave comes 20 ticks later (more shockwaves follow at
higher delves). Attack two volatile earth: the first one you break sets where the earthen shield ends up, and
the second sets where it spawns. Stand in the shield as it moves.

**Rockblock (car phase, delve 6+):** the wiki has a ready-made tile marker set
(`Module:Tile_markers/Doom_Rockblock.json`) that you can import into RuneLite's built-in Ground Markers plugin.
