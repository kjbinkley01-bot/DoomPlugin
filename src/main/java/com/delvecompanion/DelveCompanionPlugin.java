package com.delvecompanion;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Delve Companion",
	description = "Doom of Mokhaiotl: burrow-hole risk/reward, post-delve debrief and hotkey timers",
	tags = {"doom", "mokhaiotl", "delve", "boss"}
)
public class DelveCompanionPlugin extends Plugin
{
	private static final Set<Integer> DELVE_REGIONS = ImmutableSet.of(5269, 13668, 14180);
	private static final int FIRST_DELVE_REGION = 5269;

	private static final Set<Integer> BOSS_IDS = ImmutableSet.of(
		NpcID.DOM_BOSS, NpcID.DOM_BOSS_SHIELDED, NpcID.DOM_BOSS_BURROWED);
	private static final Set<Integer> LARVA_IDS = ImmutableSet.of(
		NpcID.DOM_DEMONIC_ENERGY, NpcID.DOM_DEMONIC_ENERGY_RANGE, NpcID.DOM_DEMONIC_ENERGY_MAGE,
		NpcID.DOM_DEMONIC_ENERGY_MELEE, NpcID.DOM_DEMONIC_ENERGY_GIANT_RANGE, NpcID.DOM_DEMONIC_ENERGY_GIANT_MAGE);
	private static final Set<Integer> ACID_IDS = ImmutableSet.of(ObjectID.DOM_ACIDPOOL, ObjectID.DOM_ACIDPOOL_DIAGONAL);
	private static final Set<Integer> UNIQUE_ITEMS = ImmutableSet.of(
		ItemID.MOKHAIOTL_CLOTH, ItemID.EYE_OF_AYAK, ItemID.EYE_OF_AYAK_UNCHARGED, ItemID.AVERNIC_TREADS);
	private static final Set<Integer> SHOCKWAVE_GRAPHICS = ImmutableSet.of(
		SpotanimID.VFX_AREA_SLAM_01, SpotanimID.VFX_AREA_SLAM_02, SpotanimID.VFX_AREA_SLAM_03, SpotanimID.VFX_AREA_SLAM_04);
	private static final Set<Integer> BURROW_GRAPHICS = ImmutableSet.of(
		SpotanimID.VFX_DOOM_AREA_SLAM, SpotanimID.VFX_DOM_BURROWED_EXPLOSION, SpotanimID.VFX_DOM_BURROWED_EXPLOSION_AOE);

	/** e.g. "Delve level: 3 duration: 1:23.40" or "Delve level: 8+ (12) duration: 2:01.20". */
	static final Pattern LEVEL_END = Pattern.compile("Delve level: (\\d+)(?:\\+ \\((\\d+)\\))? duration: (\\d+:\\d{2}(?:\\.\\d+)?)");
	private static final Pattern DOSES = Pattern.compile("\\s*\\(\\d\\)$");
	/** Ticks after the boss dies to wait for the completion message before closing the record ourselves. */
	private static final int COMPLETION_GRACE_TICKS = 5;

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private DelveCompanionConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private RiskOverlay riskOverlay;
	@Inject
	private TimerOverlay timerOverlay;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ChatMessageManager chatMessageManager;
	@Inject
	private KeyManager keyManager;
	@Inject
	private Gson gson;
	@Inject
	private ScheduledExecutorService executor;

	private DelveCompanionPanel panel;
	private NavigationButton navButton;

	private final ManualTimer shockwaveTimer = new ManualTimer("Shockwave");
	private final ManualTimer rockTimer = new ManualTimer("Rock");
	@Getter
	private final List<ManualTimer> timers = Arrays.asList(shockwaveTimer, rockTimer);

	private final HotkeyListener shockwaveHotkey = new HotkeyListener(() -> config.shockwaveHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			clientThread.invoke(() -> shockwaveTimer.start(config.shockwaveTicks()));
		}
	};

	private final HotkeyListener rockHotkey = new HotkeyListener(() -> config.rockHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			clientThread.invoke(() -> rockTimer.start(config.rockTicks()));
		}
	};

	@Value
	private static class PendingHit
	{
		HitClassifier.Hit hit;
		boolean prayMagic;
		boolean prayRanged;
		boolean prayMelee;
	}

	private final HitClassifier classifier = new HitClassifier();
	private final List<PendingHit> pendingHits = new ArrayList<>();
	private final Set<WorldPoint> acidTiles = new HashSet<>();
	private final Set<Projectile> seenProjectiles = Collections.newSetFromMap(new WeakHashMap<>());
	private final Set<NPC> deadLarvae = Collections.newSetFromMap(new WeakHashMap<>());
	private final Deque<DelveRecord> history = new ArrayDeque<>();
	private final List<DelveRecord> runRecords = new ArrayList<>();

	private boolean inDelve;
	private NPC boss;
	private DelveRecord current;
	private int nextLevel = 1;
	@Getter
	private int lastCompletedLevel;
	@Getter
	private long lootValue;
	@Getter
	private boolean uniqueInPile;
	private int bossDeathTick = -1;
	private WorldPoint lastPlayerTile;
	private boolean wasOnAcid;
	private int lastSpecEnergy = -1;
	private int lastMissedOrbs;

	@Provides
	DelveCompanionConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DelveCompanionConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new DelveCompanionPanel(this::clearHistory);
		navButton = NavigationButton.builder()
			.tooltip("Delve Companion")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		overlayManager.add(riskOverlay);
		overlayManager.add(timerOverlay);
		keyManager.registerKeyListener(shockwaveHotkey);
		keyManager.registerKeyListener(rockHotkey);
		updatePanel();
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(shockwaveHotkey);
		keyManager.unregisterKeyListener(rockHotkey);
		overlayManager.remove(riskOverlay);
		overlayManager.remove(timerOverlay);
		clientToolbar.removeNavigation(navButton);
		resetArenaState();
		inDelve = false;
		current = null;
		history.clear();
		runRecords.clear();
	}

	boolean isEndLevelInterfaceOpen()
	{
		Widget w = client.getWidget(InterfaceID.DomEndLevelUi.UNIVERSE);
		return w != null && !w.isHidden();
	}

	private boolean isInDelveArea()
	{
		WorldView wv = client.getTopLevelWorldView();
		if (wv == null || !wv.isInstance())
		{
			return false;
		}
		for (int region : wv.getMapRegions())
		{
			if (DELVE_REGIONS.contains(region))
			{
				return true;
			}
		}
		return false;
	}

	private boolean inFirstDelveRegion()
	{
		WorldView wv = client.getTopLevelWorldView();
		return wv != null && Arrays.stream(wv.getMapRegions()).anyMatch(r -> r == FIRST_DELVE_REGION);
	}

	// ---- Lifecycle ----

	private void enterDelve()
	{
		resetArenaState();
		runRecords.clear();
		// Region 5269 is only used for delve 1; otherwise trust the game's level varp.
		nextLevel = inFirstDelveRegion() ? 1 : client.getVarpValue(VarPlayerID.DOM_CURRENT_LEVEL_TEMP) + 1;
		lastCompletedLevel = nextLevel - 1;
		updatePanel();
	}

	private void leaveDelve()
	{
		if (current != null)
		{
			finishDelve(DelveRecord.Outcome.LEFT, null, null);
		}
		if (!runRecords.isEmpty())
		{
			sendChat(runSummary());
		}
		runRecords.clear();
		resetArenaState();
		timers.forEach(ManualTimer::stop);
		updatePanel();
	}

	private void resetArenaState()
	{
		classifier.reset();
		pendingHits.clear();
		acidTiles.clear();
		seenProjectiles.clear();
		deadLarvae.clear();
		boss = null;
		bossDeathTick = -1;
		lootValue = 0;
		uniqueInPile = false;
		lastPlayerTile = null;
		wasOnAcid = false;
	}

	private void startDelve()
	{
		current = new DelveRecord();
		current.setLevel(nextLevel);
		current.setStartTick(client.getTickCount());
		current.setMissedOrbs(0);
		lastMissedOrbs = client.getVarbitValue(VarbitID.DOM_MISSED_ORBS);
		bossDeathTick = -1;
		updatePanel();
	}

	private void finishDelve(DelveRecord.Outcome outcome, Integer level, String gameDuration)
	{
		DelveRecord r = current;
		if (r == null)
		{
			return;
		}
		classifyPendingHits();
		current = null;

		if (level != null)
		{
			r.setLevel(level);
		}
		r.setOutcome(outcome);
		r.setGameDuration(gameDuration);
		r.setTicks(client.getTickCount() - r.getStartTick());
		r.setLootValueAfter(outcome == DelveRecord.Outcome.DIED ? 0 : lootValue);

		if (outcome == DelveRecord.Outcome.COMPLETED)
		{
			lastCompletedLevel = r.getLevel();
			nextLevel = r.getLevel() + 1;
		}

		history.addFirst(r);
		while (history.size() > config.maxDebriefs())
		{
			history.removeLast();
		}
		runRecords.add(r);

		if (config.chatDebrief())
		{
			sendChat(DebriefFormatter.chatLine(r));
		}
		if (config.saveToFile())
		{
			saveRecord(r);
		}
		updatePanel();
	}

	private void clearHistory()
	{
		clientThread.invoke(() ->
		{
			history.clear();
			updatePanel();
		});
	}

	// ---- Events ----

	@Subscribe
	public void onGameTick(GameTick event)
	{
		timers.forEach(ManualTimer::tick);

		boolean nowInDelve = isInDelveArea();
		if (nowInDelve != inDelve)
		{
			inDelve = nowInDelve;
			if (nowInDelve)
			{
				enterDelve();
			}
			else
			{
				leaveDelve();
			}
		}
		if (!inDelve)
		{
			return;
		}

		int tick = client.getTickCount();
		classifyPendingHits();
		classifier.expire(tick);

		if (current != null && bossDeathTick >= 0 && tick - bossDeathTick > COMPLETION_GRACE_TICKS)
		{
			// No completion message seen; close the record with our own timing.
			finishDelve(DelveRecord.Outcome.COMPLETED, null, null);
		}

		if (current != null && boss != null && bossDeathTick < 0)
		{
			switch (boss.getId())
			{
				case NpcID.DOM_BOSS_SHIELDED:
					current.setShieldTicks(current.getShieldTicks() + 1);
					break;
				case NpcID.DOM_BOSS_BURROWED:
					current.setBurrowTicks(current.getBurrowTicks() + 1);
					break;
				default:
					current.setNormalTicks(current.getNormalTicks() + 1);
			}
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint tile = player.getWorldLocation();
		boolean onAcid = acidTiles.contains(tile);
		if (current != null && onAcid && (!wasOnAcid || !tile.equals(lastPlayerTile)))
		{
			current.setAcidSteps(current.getAcidSteps() + 1);
		}
		wasOnAcid = onAcid;
		lastPlayerTile = tile;

		trackProjectiles(player, tick);
	}

	private void trackProjectiles(Player player, int tick)
	{
		for (Projectile projectile : client.getProjectiles())
		{
			if (!seenProjectiles.add(projectile))
			{
				continue;
			}
			DamageSource source = HitClassifier.sourceForProjectile(projectile.getId());
			if (config.debugLogging())
			{
				log.info("[delve] tick {} projectile {} target={} point={} remaining={} -> {}", tick, projectile.getId(),
					projectile.getTargetActor() == null ? null : projectile.getTargetActor().getName(),
					projectile.getTargetPoint(), projectile.getRemainingCycles(), source);
			}
			if (source == null)
			{
				continue;
			}
			Actor target = projectile.getTargetActor();
			WorldPoint point = projectile.getTargetPoint();
			boolean atPlayer = target == player
				|| (target == null && point != null && point.distanceTo(player.getWorldLocation()) <= 1);
			if (atPlayer)
			{
				// 30 client cycles per game tick.
				classifier.addLanding(source, tick + (projectile.getRemainingCycles() + 29) / 30);
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!inDelve)
		{
			return;
		}
		Actor actor = event.getActor();
		int type = event.getHitsplat().getHitsplatType();
		int tick = client.getTickCount();

		if (actor == boss && type == HitsplatID.HEAL)
		{
			classifier.lastBossHealTick = tick;
			if (current != null)
			{
				current.setLarvaeAbsorbed(current.getLarvaeAbsorbed() + 1);
			}
			return;
		}

		Player player = client.getLocalPlayer();
		if (actor != player || current == null || event.getHitsplat().getAmount() <= 0 || type == HitsplatID.HEAL)
		{
			return;
		}

		Set<Integer> spotAnims = new HashSet<>();
		for (ActorSpotAnim anim : player.getSpotAnims())
		{
			spotAnims.add(anim.getId());
		}
		HitClassifier.Hit hit = new HitClassifier.Hit(tick, event.getHitsplat().getAmount(), type,
			acidTiles.contains(player.getWorldLocation()), spotAnims,
			boss != null && boss.getId() == NpcID.DOM_BOSS_BURROWED);
		pendingHits.add(new PendingHit(hit,
			client.getVarbitValue(VarbitID.PRAYER_PROTECTFROMMAGIC) == 1,
			client.getVarbitValue(VarbitID.PRAYER_PROTECTFROMMISSILES) == 1,
			client.getVarbitValue(VarbitID.PRAYER_PROTECTFROMMELEE) == 1));
	}

	/** Hits are classified one tick late so that every signal from their tick has arrived. */
	private void classifyPendingHits()
	{
		if (current == null)
		{
			pendingHits.clear();
			return;
		}
		for (PendingHit pending : pendingHits)
		{
			HitClassifier.Hit hit = pending.getHit();
			DamageSource source = classifier.classify(hit);
			current.addDamage(source, hit.getAmount());

			boolean wrongPrayer = (source == DamageSource.ORB_MAGIC && !pending.isPrayMagic())
				|| (source == DamageSource.ORB_RANGED && !pending.isPrayRanged())
				|| (source == DamageSource.ORB_MELEE && !pending.isPrayMelee());
			if (wrongPrayer)
			{
				current.setOrbHitsWrongPrayer(current.getOrbHitsWrongPrayer() + 1);
			}
			if (config.debugLogging())
			{
				log.info("[delve] tick {} hit {} type {} spotanims {} acid {} -> {}{}", hit.getTick(), hit.getAmount(),
					hit.getHitsplatType(), hit.getPlayerSpotAnims(), hit.isOnAcid(), source, wrongPrayer ? " (wrong prayer)" : "");
			}
		}
		pendingHits.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (!inDelve)
		{
			return;
		}
		NPC npc = event.getNpc();
		if (BOSS_IDS.contains(npc.getId()))
		{
			boss = npc;
			if (current == null)
			{
				startDelve();
			}
		}
		else if (LARVA_IDS.contains(npc.getId()) && current != null)
		{
			current.setLarvaeSpawned(current.getLarvaeSpawned() + 1);
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		if (inDelve && BOSS_IDS.contains(event.getNpc().getId()))
		{
			boss = event.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc == boss)
		{
			boss = null;
		}
		else if (LARVA_IDS.contains(npc.getId()) && npc.isDead())
		{
			countLarvaKill(npc);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (!inDelve)
		{
			return;
		}
		Actor actor = event.getActor();
		if (actor == client.getLocalPlayer())
		{
			finishDelve(DelveRecord.Outcome.DIED, null, null);
			runRecords.clear();
		}
		else if (actor == boss)
		{
			bossDeathTick = client.getTickCount();
		}
		else if (actor instanceof NPC && LARVA_IDS.contains(((NPC) actor).getId()))
		{
			countLarvaKill((NPC) actor);
		}
	}

	private void countLarvaKill(NPC larva)
	{
		if (current == null || bossDeathTick >= 0 || !deadLarvae.add(larva))
		{
			return;
		}
		current.setLarvaeKilled(current.getLarvaeKilled() + 1);
		Player player = client.getLocalPlayer();
		if (player != null && larva.getWorldLocation().distanceTo(player.getWorldLocation()) <= 2)
		{
			classifier.lastLarvaDeathNearPlayerTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inDelve || event.getActor() != boss || boss == null)
		{
			return;
		}
		int anim = boss.getAnimation();
		int tick = client.getTickCount();
		switch (anim)
		{
			case AnimationID.DOM_MELEE_ATTACK:
				classifier.lastTongueLashTick = tick;
				break;
			case AnimationID.DOM_BEAM_FIRE:
				classifier.lastBeamTick = tick;
				break;
			case AnimationID.DOM_BURROWED_EXPLOSION:
				classifier.lastBurrowHitTick = tick;
				break;
			default:
				break;
		}
		if (config.debugLogging())
		{
			log.info("[delve] tick {} boss {} animation {}", tick, boss.getId(), anim);
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		if (!inDelve)
		{
			return;
		}
		int id = event.getGraphicsObject().getId();
		int tick = client.getTickCount();
		if (SHOCKWAVE_GRAPHICS.contains(id))
		{
			classifier.lastShockwaveTick = tick;
		}
		else if (BURROW_GRAPHICS.contains(id))
		{
			classifier.lastBurrowHitTick = tick;
		}
		else if (id == SpotanimID.VFX_ROCK_PROJECTILE_IMPACT && nearPlayer(event.getGraphicsObject().getLocation()))
		{
			classifier.lastRockImpactTick = tick;
		}
		if (config.debugLogging())
		{
			log.info("[delve] tick {} graphic {} at {}", tick, id, event.getGraphicsObject().getLocation());
		}
	}

	private boolean nearPlayer(LocalPoint point)
	{
		Player player = client.getLocalPlayer();
		if (player == null || point == null)
		{
			return false;
		}
		// 128 local units per tile.
		return point.distanceTo(player.getLocalLocation()) <= 128 * 1.5;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		addAcid(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		removeAcid(event.getGameObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		addAcid(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		removeAcid(event.getGroundObject());
	}

	private void addAcid(TileObject object)
	{
		if (inDelve && ACID_IDS.contains(object.getId()))
		{
			acidTiles.add(object.getWorldLocation());
		}
	}

	private void removeAcid(TileObject object)
	{
		if (ACID_IDS.contains(object.getId()))
		{
			acidTiles.remove(object.getWorldLocation());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE || !inDelve)
		{
			return;
		}
		Matcher m = LEVEL_END.matcher(Text.removeTags(event.getMessage()));
		if (!m.find())
		{
			return;
		}
		int level = Integer.parseInt(m.group(2) != null ? m.group(2) : m.group(1));
		if (current != null)
		{
			finishDelve(DelveRecord.Outcome.COMPLETED, level, m.group(3));
		}
		else
		{
			lastCompletedLevel = level;
			nextLevel = level + 1;
			updatePanel();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.DOM_LOOTPILE || id == InventoryID.DOM_LOOTPILE_DURING)
		{
			updateLoot(event.getItemContainer());
		}
	}

	private void updateLoot(ItemContainer container)
	{
		long value = 0;
		boolean unique = false;
		if (container != null)
		{
			for (Item item : container.getItems())
			{
				if (item.getId() <= 0 || item.getQuantity() <= 0)
				{
					continue;
				}
				value += itemManager.getItemPrice(item.getId()) * (long) item.getQuantity();
				unique |= UNIQUE_ITEMS.contains(item.getId());
			}
		}
		lootValue = value;
		uniqueInPile = unique;
		updatePanel();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.DOM_END_LEVEL_UI)
		{
			return;
		}
		ItemContainer pile = client.getItemContainer(InventoryID.DOM_LOOTPILE);
		if (pile == null)
		{
			pile = client.getItemContainer(InventoryID.DOM_LOOTPILE_DURING);
		}
		if (pile != null)
		{
			updateLoot(pile);
		}
		int threshold = config.riskWarningValue();
		if (threshold > 0 && lootValue >= threshold)
		{
			sendChat("Descending risks " + QuantityFormatter.quantityToStackSize(lootValue)
				+ " gp of unclaimed loot" + (uniqueInPile ? ", including a unique" : "") + ".");
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (current == null || !event.isItemOp())
		{
			return;
		}
		String option = Text.removeTags(event.getMenuOption());
		if (!option.equals("Eat") && !option.equals("Drink"))
		{
			return;
		}
		String name = itemManager.getItemComposition(event.getItemId()).getName();
		current.addSupply(DOSES.matcher(name).replaceAll(""));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayerID.SA_ENERGY)
		{
			int energy = event.getValue();
			if (current != null && lastSpecEnergy > energy)
			{
				current.setSpecEnergyUsed(current.getSpecEnergyUsed() + (lastSpecEnergy - energy) / 10);
			}
			lastSpecEnergy = energy;
		}
		else if (event.getVarbitId() == VarbitID.DOM_MISSED_ORBS)
		{
			int missed = event.getValue();
			if (current != null && missed > lastMissedOrbs)
			{
				current.setMissedOrbs(current.getMissedOrbs() + missed - lastMissedOrbs);
			}
			lastMissedOrbs = missed;
		}
	}

	// ---- Output ----

	private String runSummary()
	{
		int ticks = runRecords.stream().mapToInt(DelveRecord::getTicks).sum();
		int damage = runRecords.stream().mapToInt(DelveRecord::totalDamage).sum();
		int wrong = runRecords.stream().mapToInt(DelveRecord::getOrbHitsWrongPrayer).sum();
		int first = runRecords.get(0).getLevel();
		int last = runRecords.get(runRecords.size() - 1).getLevel();
		return "Run over: delves " + first + "-" + last + " in " + DebriefFormatter.formatTicks(ticks)
			+ " | took " + damage + " | wrong-prayer orbs " + wrong;
	}

	private void sendChat(String text)
	{
		String message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("[Delve] ")
			.append(ChatColorType.NORMAL)
			.append(text)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message)
			.build());
	}

	private void saveRecord(DelveRecord record)
	{
		String json = gson.toJson(record);
		executor.execute(() ->
		{
			File dir = new File(RuneLite.RUNELITE_DIR, "delve-companion");
			if (!dir.exists() && !dir.mkdirs())
			{
				log.warn("Could not create {}", dir);
				return;
			}
			try (Writer w = new FileWriter(new File(dir, "delves.jsonl"), StandardCharsets.UTF_8, true))
			{
				w.write(json);
				w.write('\n');
			}
			catch (IOException e)
			{
				log.warn("Could not save delve record", e);
			}
		});
	}

	/** Collects panel data on the client thread and hands it to Swing. */
	private void updatePanel()
	{
		if (panel == null)
		{
			return;
		}
		List<String[]> risk = inDelve
			? DebriefFormatter.riskLines(lastCompletedLevel, lootValue, uniqueInPile, config.targetDelve())
			: null;
		List<String> debriefs = history.stream().map(DebriefFormatter::panelHtml).collect(Collectors.toList());
		SwingUtilities.invokeLater(() ->
		{
			panel.setRisk(risk);
			panel.setDebriefs(debriefs);
		});
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(150, 40, 20));
		g.fillOval(0, 0, 16, 16);
		g.setColor(new Color(255, 200, 60));
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
		g.drawString("D", 4, 12);
		g.dispose();
		return image;
	}
}
