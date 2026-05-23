package com.oreradar;

import com.oreradar.gui.HackMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OreRadar implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("oreradar");

    // ESP
    public static boolean radarEnabled      = false;
    public static boolean xrayEnabled       = false;
    public static boolean containerEnabled  = false;
    public static boolean entityEspEnabled  = false;
    public static boolean playerEspEnabled  = false;
    public static boolean nameTagsEnabled   = false;
    public static boolean tracersEnabled    = false;

    // Combat
    public static boolean flyEnabled         = false;
    public static boolean killAuraEnabled    = false;
    public static boolean fullAuraEnabled    = false;
    public static boolean antiKbEnabled      = false;
    public static boolean autoSprintEnabled  = false;
    public static boolean reachEnabled       = false;
    public static boolean fastBowEnabled     = false;
    public static boolean autoClickerEnabled = false;
    public static boolean bhopEnabled        = false;

    // Utility
    public static boolean nightVisionEnabled  = false;
    public static boolean fullBrightEnabled   = false;
    public static boolean autoFishEnabled     = false;
    public static boolean autoMineEnabled     = false;
    public static boolean fastPlaceEnabled    = false;
    public static boolean antiAfkEnabled      = false;
    public static boolean autoEatEnabled      = false;
    public static boolean autoToolEnabled     = false;
    public static boolean scaffoldEnabled     = false;
    public static boolean autoRespawnEnabled  = false;
    public static boolean noSlowdownEnabled   = false;
    public static boolean chestStealerEnabled = false;
    public static boolean autoWalkEnabled     = false;
    public static boolean invSortEnabled      = false;
    public static boolean antiPoisonEnabled   = false;
    public static boolean safeWalkEnabled     = false;
    public static boolean antiHungerEnabled   = false;
    public static boolean autoArmorEnabled    = false;
    public static boolean clickTpEnabled      = false;
    public static boolean freecamEnabled      = false;
    public static boolean autoCraftEnabled    = false;

    private static KeyBinding menuKey;

    private static int    killAuraCooldown  = 0;
    private static int    fullAuraCooldown  = 0;
    private static int    antiAfkTimer      = 0;
    private static int    autoFishTimer     = 0;
    private static int    autoEatTimer      = 0;
    private static int    antiPoisonTimer   = 0;
    private static int    chestStealTimer   = 0;
    private static int    autoClickTimer    = 0;
    private static int    autoArmorTimer    = 0;
    private static double originalGamma     = -1;

    private static boolean isFood(ItemStack s) {
        return !s.isEmpty() && s.contains(DataComponentTypes.FOOD);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Cadens Hacks 3.0 loaded! Press M for menu.");

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.oreradar.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.oreradar"));

        OreRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (menuKey.wasPressed()) client.setScreen(new HackMenuScreen());

            // Fly
            if (flyEnabled) {
                client.player.getAbilities().allowFlying = true;
                client.player.sendAbilitiesUpdate();
            }

            // KillAura (mobs only)
            if (killAuraEnabled && client.world != null && --killAuraCooldown <= 0) {
                killAuraCooldown = 10;
                LivingEntity nearest = null; double best = 16.0;
                for (Entity e : client.world.getEntities()) {
                    if (e == client.player || !(e instanceof LivingEntity le)) continue;
                    if (e instanceof PlayerEntity) continue;
                    double d = e.squaredDistanceTo(client.player);
                    if (d < best) { best = d; nearest = le; }
                }
                if (nearest != null) {
                    client.interactionManager.attackEntity(client.player, nearest);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }

            // FullAura (hits players too)
            if (fullAuraEnabled && client.world != null && --fullAuraCooldown <= 0) {
                fullAuraCooldown = 10;
                LivingEntity nearest = null; double best = 16.0;
                for (Entity e : client.world.getEntities()) {
                    if (e == client.player || !(e instanceof LivingEntity le)) continue;
                    double d = e.squaredDistanceTo(client.player);
                    if (d < best) { best = d; nearest = le; }
                }
                if (nearest != null) {
                    client.interactionManager.attackEntity(client.player, nearest);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }

            // AutoClicker
            if (autoClickerEnabled && --autoClickTimer <= 0) {
                autoClickTimer = 4;
                if (client.crosshairTarget != null) {
                    if (client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                        var ehr = (EntityHitResult) client.crosshairTarget;
                        client.interactionManager.attackEntity(client.player, ehr.getEntity());
                        client.player.swingHand(Hand.MAIN_HAND);
                    } else {
                        client.options.attackKey.setPressed(true);
                    }
                }
            }

            // Bhop
            if (bhopEnabled && client.player.isOnGround())
                client.player.jump();

            // SafeWalk
            if (safeWalkEnabled)
                client.player.setSneaking(true);

            // Night Vision
            if (nightVisionEnabled)
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false));

            // FullBright
            if (fullBrightEnabled) {
                if (originalGamma < 0) originalGamma = client.options.getGamma().getValue();
                client.options.getGamma().setValue(16.0);
            } else if (originalGamma >= 0) {
                client.options.getGamma().setValue(originalGamma);
                originalGamma = -1;
            }

            // FastPlace
            if (fastPlaceEnabled)
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 10, false, false));

            // AntiHunger
            if (antiHungerEnabled)
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 4, false, false));

            // AntiAFK
            if (antiAfkEnabled && ++antiAfkTimer >= 200) {
                antiAfkTimer = 0;
                client.player.setSneaking(!client.player.isSneaking());
            }

            // AutoSprint
            if (autoSprintEnabled) client.player.setSprinting(true);

            // AntiKB
            if (antiKbEnabled) {
                var vel = client.player.getVelocity();
                client.player.setVelocity(vel.x * 0.0, vel.y, vel.z * 0.0);
            }

            // NoSlowdown
            if (noSlowdownEnabled)
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, false));

            // AutoFish
            if (autoFishEnabled && --autoFishTimer <= 0) {
                autoFishTimer = 5;
                if (client.player.getMainHandStack().getItem() instanceof FishingRodItem) {
                    FishingBobberEntity bobber = client.player.fishHook;
                    if (bobber != null && bobber.isOnGround()) {
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    } else if (bobber == null) {
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    }
                }
            }

            // AutoMine
            if (autoMineEnabled && client.crosshairTarget != null
                    && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                var bhr = (BlockHitResult) client.crosshairTarget;
                client.interactionManager.updateBlockBreakingProgress(bhr.getBlockPos(), bhr.getSide());
            }

            // AutoEat
            if (autoEatEnabled && --autoEatTimer <= 0) {
                autoEatTimer = 10;
                if (client.player.getHungerManager().getFoodLevel() < 18) {
                    if (isFood(client.player.getMainHandStack()))
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    else if (isFood(client.player.getOffHandStack()))
                        client.interactionManager.interactItem(client.player, Hand.OFF_HAND);
                }
            }

            // AutoTool
            if (autoToolEnabled && client.crosshairTarget != null
                    && client.crosshairTarget.getType() == HitResult.Type.BLOCK
                    && client.world != null) {
                var bhr = (BlockHitResult) client.crosshairTarget;
                var bs = client.world.getBlockState(bhr.getBlockPos());
                var inv = client.player.getInventory();
                float bestSpd = -1; int bestSlot = -1;
                for (int i = 0; i < 9; i++) {
                    float spd = inv.getStack(i).getMiningSpeedMultiplier(bs);
                    if (spd > bestSpd) { bestSpd = spd; bestSlot = i; }
                }
                if (bestSlot >= 0) inv.selectedSlot = bestSlot;
            }

            // AutoRespawn
            if (autoRespawnEnabled && client.player.isDead())
                client.player.requestRespawn();

            // AntiPoison
            if (antiPoisonEnabled && ++antiPoisonTimer >= 20) {
                antiPoisonTimer = 0;
                List<RegistryEntry<StatusEffect>> toRemove = new ArrayList<>();
                for (var eff : client.player.getStatusEffects()) {
                    var e = eff.getEffectType();
                    if (e == StatusEffects.POISON || e == StatusEffects.WITHER
                        || e == StatusEffects.SLOWNESS || e == StatusEffects.WEAKNESS
                        || e == StatusEffects.MINING_FATIGUE || e == StatusEffects.BLINDNESS
                        || e == StatusEffects.NAUSEA || e == StatusEffects.HUNGER)
                        toRemove.add(e);
                }
                for (var e : toRemove) client.player.removeStatusEffect(e);
            }

            // AutoWalk
            if (autoWalkEnabled) {
                client.player.forwardSpeed = 1.0f;
                client.player.setSprinting(true);
            }

            // ChestStealer
            if (chestStealerEnabled && --chestStealTimer <= 0) {
                chestStealTimer = 5;
                if (client.currentScreen != null && client.player.currentScreenHandler != null) {
                    var handler = client.player.currentScreenHandler;
                    for (int i = 0; i < handler.slots.size(); i++) {
                        var slot = handler.slots.get(i);
                        if (slot.hasStack() && !slot.inventory.equals(client.player.getInventory()))
                            client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, client.player);
                    }
                }
            }

            // FastBow
            if (fastBowEnabled)
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 5, false, false));

            // AutoArmor - detect slot by item type name
            if (autoArmorEnabled && ++autoArmorTimer >= 40) {
                autoArmorTimer = 0;
                var inv = client.player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;
                    String name = stack.getItem().getClass().getSimpleName().toLowerCase();
                    int slot = -1;
                    if (name.contains("helmet") || name.contains("cap") || name.contains("skull"))  slot = 3;
                    else if (name.contains("chest") || name.contains("tunic") || name.contains("vest")) slot = 2;
                    else if (name.contains("legging") || name.contains("pant")) slot = 1;
                    else if (name.contains("boot") || name.contains("shoe"))    slot = 0;
                    if (slot >= 0 && inv.armor.get(slot).isEmpty()) {
                        inv.armor.set(slot, stack.copy());
                        inv.removeStack(i, 1);
                    }
                }
            }

            // ClickTP
            if (clickTpEnabled && client.isInSingleplayer()
                    && client.crosshairTarget != null
                    && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_V)) {
                    var bhr = (BlockHitResult) client.crosshairTarget;
                    BlockPos tp = bhr.getBlockPos();
                    client.player.requestTeleport(tp.getX() + 0.5, tp.getY() + 1, tp.getZ() + 0.5);
                }
            }
        });
    }
}
