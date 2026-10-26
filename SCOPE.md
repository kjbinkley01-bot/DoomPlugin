# Doom of Mokhaiotl Plugin — Scope

Working name: **Delve Companion**. RuneLite external plugin (Plugin Hub target) that helps players
complete the Doom of Mokhaiotl delves (levels 1–8 and deep delves 9+).

Status: F2 (risk/reward), F4 (debrief) and hotkey-started shockwave/rock timers are implemented; not yet
checked in-game. Built for personal use (not for Plugin Hub submission). See README.md.

---

## 1. The hard constraint: Jagex third-party client rules

The boss-fight rules in Jagex's Third Party Client Guidelines apply to "any boss fights", so Doom is
covered even though it isn't named. They prohibit features that aid a boss fight by:

- Predicting the next attack (timing or style)
- Showing projectile targets or impact locations
- Indicating prayer switches / which prayer to use
- Attack counters
- Automatically indicating where to stand or not stand (manually marked tiles are fine)
- Indicating when a boss mechanic starts or ends
- Adding extra visual or audio indicators of a boss mechanic (unless manually triggered)

The plugin also must not add menu entries that send actions to the server, or resize click zones.

Plugin Hub reviewers check against these rules, so any feature that breaks them won't be accepted.
Most of the obvious "helper" ideas for this boss fall under these bans. **Delve Companion does not
assist live combat decisions.** It works around the fight: planning, tracking, risk and reward,
combat-achievement attempts, and review after a delve.

### Explicit non-goals (prohibited — do not build)

| Idea | Doom mechanic | Rule it breaks |
|---|---|---|
| Prayer indicator from orb colour (blue/green/red) | Standard attack | Prayer switching indicators |
| Prayer re-enable cue after rock shatters | Rock Throw | Prayer indicators / mechanic timing |
| Highlight rock-throw shadows / debris tiles | Rock Throw | Impact locations; where not to stand |
| Shockwave countdown (20 ticks after volatile earth) | Shockwave | Mechanic start/end timing |
| Earthen shield destination/path preview | Volatile earth | Automatic where-to-stand |
| Burrow "eye" zoom path / trample lane | Car phase | Impact locations; where not to stand |
| Rockblock auto tile | Car phase | Automatic where-to-stand (manual tile markers are fine) |
| Acid blood tile highlight | Acid | Automatic where-not-to-stand |
| Melee-charge / beam-charge alert or bar | Special Beam Cannon | Added mechanic indicator; timing |
| Larva arrival ETA at the Doom | Larvae | Prediction / timing |
| Attack rotation tracker ("next: rock throw") | Rotation | Next-attack prediction; attack counter |

### Grey zone (decide before building; default off, flagged in config)

| Idea | Why it's grey |
|---|---|
| Demonic Charge stack count (larva heal/damage stacks, persists across delves) | It's state, not a prediction, but a reviewer could read it as a counter |
| Numeric shield HP (500) during the shield phase | It's similar to an HP bar (the boss health bar already turns blue), but it's extra info about a mechanic |
| Colour-blind recolour of existing projectiles and larvae | It recolours an existing cue rather than adding one; Projectile Override and colour-blind plugins exist on the Hub, but the rules don't settle it |

Recommendation: leave all three out of v1. Revisit only with a Plugin Hub reviewer's opinion.

---

## 2. What already exists (don't duplicate)

| Plugin | Covers |
|---|---|
| Larva Tracker (~108k installs) | Larva highlight and hide |
| Deep Delve Pacer | Deep-delve (9+) average and best times, plus an estimate of the level you'll reach before the 6-hour logout |
| Doom Loot Tracker / Doom Loot Value Fix | Loot claimed and lost; value adjustments |
| Doom Unique Colors | Recolours the special-loot hole |
| Delve Calculator (~59k installs) | Not reviewed yet; **check for overlap with F2 before building it** |
| Automatic Low Detail | Turns on Low Detail / Hide Lower Planes in the arena |
| Better NPC Highlight, Custom Menu Swaps, Visual Metronome, Radius Markers, Ground Object Hider | General-purpose plugins the wiki recommends configuring for Doom |

Gaps none of these cover: **split times for delves 1–8, combat-achievement attempt tracking, a debrief
after each delve, supply use per delve, and pre-trip gear checks.**

---

## 3. Proposed features

### F1 — Run splits and personal bests (MVP)
- Per-delve split times plus a running total for delves 1–8, using the game's own
  `Delve level: N duration:` message where possible and otherwise counting ticks.
- PBs per delve and for the full 1–8 run; shows the delta against your PB *between* delves.
- Pace vs combat-achievement targets: Doom Crawler (delve 1 < 0:30), Doom Chaser (1–8 < 10:00),
  Doom Racer (1–8 < 7:15). Shown only once a delve is finished, never mid-fight.
- Complements Deep Delve Pacer, which only times 9+.

### F2 — Burrow-hole risk/reward panel (MVP)
Shown when the player is at the burrow hole (not during combat):
- GE value of the unclaimed rewards so far, which a death would forfeit.
- Chance of a unique on the next delve, and the cumulative chance over the remaining planned delves
  (from the wiki table: 1/2,500 at delve 2, 1/1,000 at 3, 1/450 at 4 … 1/180 at 9+), split by item
  (cloth, Eye of Ayak, Avernic treads, Dom).
- Demon tears so far and next (50 at delve 3, +10 per delve, max 100).
- Optional user-set rule ("warn me if risked value > X gp"). The game already warns when a unique is
  pending.

### F3 — Combat-achievement attempt tracker
Arm a CA from the side panel, and the plugin reports when the attempt has failed (after the event,
not as a warning):

| CA | Failure condition detected |
|---|---|
| Grub Patrol | A larva was absorbed (heal hitsplat on the Doom / larva despawn at the Doom) |
| Perfect Doom | Damage from the boss, a larva absorbed, or stepping on acid |
| The Praying Mantis | Prayer points went up (any restore) |
| Mine's Better | Shield slot empty during delves 1–8 |
| Darkness Is Your Ally? | A demonbane weapon was equipped |
| Duel of Mokhaiotl | Boss damaged by a non-one-handed or non-melee attack |
| Doom Chaser / Racer / Crawler | Time limit passed (from F1) |
| Mopping up | Acid splats cleared in the last volatile earth (reported after it ends) |

### F4 — Delve debrief (after each delve and after the run)
Shown between delves and saved to the side panel:
- Damage taken by source: standard attacks (by style), rock debris, shockwave, trample/slam, beam,
  larva explosions, acid, melee tongue-lash.
- Hits taken while praying the wrong style (reported afterwards only).
- Larvae killed and absorbed, peak Demonic Charge stack, acid steps.
- Time spent in each phase (normal, shield, burrow).
- Supplies used per delve: brews, restores, food, special energy.

**Compliance note:** all data is buffered and only shown after the boss dies or between delves.
The plugin draws nothing during combat.

### F5 — Pre-trip loadout check
In the lobby, check inventory and equipment against a configurable Doom loadout:
- A demonbane option for each larva style (e.g. Emberlight / Arclight, scorching bow, demonbane
  autocast staff), or the Eye of Ayak plus its charges.
- Prayer level vs requirements (43 for the protection prayers, 74 for Rigour); a melee-punish weapon.
- Warns about thralls (they create acid). Also notes the lower death fee while under 20 delves.

### Later / maybe
- Export runs as CSV or JSON.
- Consumption estimates for deep-delve trips (combined with F4 data).

---

## 4. Technical approach

Standard RuneLite external plugin: Java 11, Gradle, `runelite/example-plugin` template, `net.runelite`
API, gameval constants where available.

### Detection signals (from existing open-source plugins and the wiki; verify in-game)

| Signal | Source |
|---|---|
| In a delve: instanced and map region ∈ {5269 (delve 1), 13668 (2–7), 14180 (8+)} | Deep Delve Pacer |
| Current delve: `VarPlayerID.DOM_CURRENT_LEVEL_TEMP` + 1 | Deep Delve Pacer |
| Delve complete: game message `^Delve level: 8(?:\+ \((\d+)\))? duration:` (generalise to all levels) | Deep Delve Pacer (verify the 1–7 wording) |
| Descend: MESBOX `You jump further into the burrow...` | Deep Delve Pacer |
| Doom NPCs: 14707 (normal), 14708 (shielded), 14709 (burrowed) | Wiki infobox |
| Larvae: 14711, 14712, 14713; giant larvae 14788, 14789; earthen shield 14715 | Wiki (Better NPC Highlight config) |
| Acid blood object 57283; rock object 57286 | Blert source |
| Orb projectiles: 3384 ranged, 3385 magic | Blert source (used only for F4 attribution, after the fact) |
| Shockwave graphics: 3405, 3406, 3407 | Blert source (F4 only) |
| Shield start: `The demonic shield resists your attack!` / NPC change to 14708 | Wiki |

### Components
- `DelveCompanionPlugin`: event wiring (GameTick, ChatMessage, VarbitChanged, NpcSpawned/Despawned/Changed,
  HitsplatApplied, GameObjectSpawned/Despawned, ItemContainerChanged, StatChanged).
- `DelveSession`: state machine — Lobby → Delving(n, phase) → BurrowHole(n) → Lobby / Dead.
- `SplitTimer` (F1), `LootRisk` (F2), `CaAttempt` strategies (F3), `DebriefRecorder` (F4), `LoadoutCheck` (F5).
- UI: a side `PluginPanel` (history, PBs, CA arming), an infobox/overlay shown only between delves,
  and chat summaries.
- Persistence: `ConfigManager` per RuneScape profile (PBs, history); a JSON file in the RuneLite directory for runs.
- Prices: `ItemManager.getItemPrice`.

### Technical risks / spikes
1. **Reading reward contents** at the burrow hole: find the loot interface widget group, or work it
   out from the per-delve loot rolls.
2. **Detecting larva absorption**: a heal hitsplat on the Doom vs a larva despawn. Needs in-game testing.
3. **Damage attribution** (F4): hitsplats carry no source; attribute using projectile/graphic/object
   activity on the same tick, buffered internally. Accept "unknown" as a category.
4. **Chat message wording** for delves 1–7 (only the delve 8+ pattern is confirmed).
5. IDs above come from third-party sources (wiki, Blert, Deep Delve Pacer); check each one in a dev
   client before relying on it.

---

## 5. Milestones

| # | Deliverable | Est. effort |
|---|---|---|
| M0 | Plugin skeleton, delve/session state machine, debug overlay (dev only), ID check in-game | 2–3 days |
| M1 | F1 splits and PBs + side panel | 2–3 days |
| M2 | F2 burrow-hole risk/reward | 3–4 days (widget spike) |
| M3 | F3 CA attempt tracker | 3–4 days |
| M4 | F4 debrief + supply tracking | 4–6 days |
| M5 | F5 loadout check, polish, tests, Plugin Hub submission | 2–3 days |

MVP for the Hub = M0–M2 (+ M3 if it goes smoothly).

---

## 6. Open questions

1. Which features matter most? (Suggested MVP: F1 + F2.)
2. Submit to the Plugin Hub, or personal / sideload only?
3. Include any of the grey-zone items (§1) as opt-in, or leave them out?
4. Check Delve Calculator's features before committing to F2?
5. Name: "Delve Companion" or something else?

## Sources
- OSRS Wiki: [Doom of Mokhaiotl](https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl),
  [Strategies](https://oldschool.runescape.wiki/w/Doom_of_Mokhaiotl/Strategies)
- [Third Party Client Guidelines (wiki copy)](https://oldschool.runescape.wiki/w/Update:Third_Party_Client_Guidelines),
  [Jagex: macro and client features not permitted](https://legal.jagex.com/docs/rules/macro-and-client-features-not-permitted)
- [Deep Delve Pacer source](https://github.com/DustinKieler/deep-delve-pacer),
  [Blert PR #77](https://github.com/blert-io/plugin/pull/77), [Plugin Hub mirror](https://runelite.phyce.dev)
