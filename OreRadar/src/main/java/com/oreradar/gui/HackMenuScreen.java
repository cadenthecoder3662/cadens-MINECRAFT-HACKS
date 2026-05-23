package com.oreradar.gui;

import com.oreradar.OreRadar;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HackMenuScreen extends Screen {

    interface BoolGet { boolean get(); }
    interface BoolSet { void set(boolean v); }
    record HackEntry(String name, String desc, String cat, BoolGet get, BoolSet set) {}

    private final List<HackEntry> hacks = new ArrayList<>();
    private int scrollOffset = 0;

    private static final int BTN_W        = 190;
    private static final int BTN_H        = 18;
    private static final int GAP          = 22;
    private static final int COLS         = 2;
    private static final int VISIBLE_ROWS = 9;
    private static final int HEADER_H     = 30;

    public HackMenuScreen() { super(Text.literal("Cadens Hacks 3.0")); }

    @Override
    protected void init() {
        hacks.clear();

        // ESP
        add("Ore ESP",           "Colored outlines on ores through walls",      "ESP",    () -> OreRadar.radarEnabled,       v -> OreRadar.radarEnabled = v);
        add("XRay",              "See all ores through stone 32 block range",   "ESP",    () -> OreRadar.xrayEnabled,        v -> OreRadar.xrayEnabled = v);
        add("Container Finder",  "Red boxes on chests barrels shulkers",        "ESP",    () -> OreRadar.containerEnabled,   v -> OreRadar.containerEnabled = v);
        add("Entity ESP",        "Orange boxes on mobs through walls",          "ESP",    () -> OreRadar.entityEspEnabled,   v -> OreRadar.entityEspEnabled = v);
        add("Player ESP",        "Yellow boxes on players through walls",       "ESP",    () -> OreRadar.playerEspEnabled,   v -> OreRadar.playerEspEnabled = v);
        add("Name Tags",         "Player names and distance through walls",     "ESP",    () -> OreRadar.nameTagsEnabled,    v -> OreRadar.nameTagsEnabled = v);
        add("Tracers",           "Lines from camera to all nearby players",     "ESP",    () -> OreRadar.tracersEnabled,     v -> OreRadar.tracersEnabled = v);

        // Combat
        add("Fly",               "Fly anywhere on any server",                  "Combat", () -> OreRadar.flyEnabled, v -> {
            OreRadar.flyEnabled = v;
            if (client != null && client.player != null) {
                client.player.getAbilities().allowFlying = v;
                if (!v) client.player.getAbilities().flying = false;
                client.player.sendAbilitiesUpdate();
            }
        });
        add("KillAura",          "Auto attacks nearby mobs within 4 blocks",    "Combat", () -> OreRadar.killAuraEnabled,    v -> OreRadar.killAuraEnabled = v);
        add("FullAura",          "KillAura but also targets players",           "Combat", () -> OreRadar.fullAuraEnabled,    v -> OreRadar.fullAuraEnabled = v);
        add("AutoClicker",       "Auto clicks at 15 CPS on your target",       "Combat", () -> OreRadar.autoClickerEnabled, v -> OreRadar.autoClickerEnabled = v);
        add("AntiKnockback",     "Removes knockback when hit",                  "Combat", () -> OreRadar.antiKbEnabled,      v -> OreRadar.antiKbEnabled = v);
        add("FastBow",           "Shoot bow faster with Haste effect",          "Combat", () -> OreRadar.fastBowEnabled,     v -> OreRadar.fastBowEnabled = v);
        add("Reach",             "Extended attack range flag",                  "Combat", () -> OreRadar.reachEnabled,       v -> OreRadar.reachEnabled = v);
        add("Bhop",              "Auto jumps to maintain speed while sprinting","Combat", () -> OreRadar.bhopEnabled,        v -> OreRadar.bhopEnabled = v);

        // Utility
        add("Night Vision",      "See perfectly in total darkness",             "Utility",() -> OreRadar.nightVisionEnabled, v -> OreRadar.nightVisionEnabled = v);
        add("FullBright",        "Max gamma everything glows bright",           "Utility",() -> OreRadar.fullBrightEnabled,  v -> OreRadar.fullBrightEnabled = v);
        add("AutoSprint",        "Always sprinting automatically",              "Utility",() -> OreRadar.autoSprintEnabled,  v -> OreRadar.autoSprintEnabled = v);
        add("NoSlowdown",        "No slowdown in cobwebs soul sand powder snow","Utility",() -> OreRadar.noSlowdownEnabled,  v -> OreRadar.noSlowdownEnabled = v);
        add("SafeWalk",          "Wont walk off edges by holding sneak",        "Utility",() -> OreRadar.safeWalkEnabled,    v -> OreRadar.safeWalkEnabled = v);
        add("AntiHunger",        "Keeps hunger and saturation completely full", "Utility",() -> OreRadar.antiHungerEnabled,  v -> OreRadar.antiHungerEnabled = v);
        add("AntiPoison",        "Removes poison wither slowness blindness",    "Utility",() -> OreRadar.antiPoisonEnabled,  v -> OreRadar.antiPoisonEnabled = v);
        add("AutoRespawn",       "Instantly respawns when you die",             "Utility",() -> OreRadar.autoRespawnEnabled, v -> OreRadar.autoRespawnEnabled = v);
        add("AutoWalk",          "Keeps walking forward automatically",         "Utility",() -> OreRadar.autoWalkEnabled,    v -> OreRadar.autoWalkEnabled = v);
        add("AntiAFK",           "Sneaks every 10s to prevent AFK kick",        "Utility",() -> OreRadar.antiAfkEnabled,     v -> OreRadar.antiAfkEnabled = v);
        add("AutoFish",          "Auto reels and recasts fishing rod",          "Utility",() -> OreRadar.autoFishEnabled,    v -> OreRadar.autoFishEnabled = v);
        add("AutoMine",          "Auto breaks block you are looking at",        "Utility",() -> OreRadar.autoMineEnabled,    v -> OreRadar.autoMineEnabled = v);
        add("FastPlace",         "Haste X for ultra fast mining and placing",   "Utility",() -> OreRadar.fastPlaceEnabled,   v -> OreRadar.fastPlaceEnabled = v);
        add("AutoEat",           "Auto eats food from hand when hungry",        "Utility",() -> OreRadar.autoEatEnabled,     v -> OreRadar.autoEatEnabled = v);
        add("AutoTool",          "Switches to best tool for block you look at", "Utility",() -> OreRadar.autoToolEnabled,    v -> OreRadar.autoToolEnabled = v);
        add("AutoArmor",         "Auto equips best armor from your inventory",  "Utility",() -> OreRadar.autoArmorEnabled,   v -> OreRadar.autoArmorEnabled = v);
        add("ChestStealer",      "Auto takes all items from open chest",        "Utility",() -> OreRadar.chestStealerEnabled,v -> OreRadar.chestStealerEnabled = v);
        add("InventorySort",     "Sorts hotbar items automatically",            "Utility",() -> OreRadar.invSortEnabled,     v -> OreRadar.invSortEnabled = v);
        add("ClickTP",           "Hold V to teleport to crosshair (SP only)",  "Utility",() -> OreRadar.clickTpEnabled,     v -> OreRadar.clickTpEnabled = v);
        add("Scaffold",          "Places blocks under you mid-air",             "Utility",() -> OreRadar.scaffoldEnabled,    v -> OreRadar.scaffoldEnabled = v);
        add("Freecam",           "Detach camera and fly it around freely",      "Utility",() -> OreRadar.freecamEnabled,     v -> OreRadar.freecamEnabled = v);

        rebuildButtons();
    }

    private void add(String n, String d, String c, BoolGet g, BoolSet s) {
        hacks.add(new HackEntry(n, d, c, g, s));
    }

    private int panelX() { return this.width / 2 - 210; }
    private int panelY() { return this.height / 2 - 140; }
    private int panelW() { return 420; }
    private int panelH() { return HEADER_H + VISIBLE_ROWS * GAP + 46; }
    private int listStartY() { return panelY() + HEADER_H + 4; }

    private void rebuildButtons() {
        clearChildren();

        int sx = this.width / 2 - (BTN_W * COLS + 10) / 2;
        int sy = listStartY();
        int totalRows = (int) Math.ceil(hacks.size() / (double) COLS);
        int first = scrollOffset * COLS;
        int last  = Math.min(first + VISIBLE_ROWS * COLS, hacks.size());
        int row = 0, col = 0;

        for (int i = first; i < last; i++) {
            HackEntry h = hacks.get(i);
            int x = sx + col * (BTN_W + 10);
            int y = sy + row * GAP;
            addDrawableChild(ButtonWidget.builder(
                lbl(h.name(), h.get().get()),
                btn -> { h.set().set(!h.get().get()); btn.setMessage(lbl(h.name(), h.get().get())); }
            ).dimensions(x, y, BTN_W, BTN_H).build());
            col++;
            if (col >= COLS) { col = 0; row++; }
        }

        int bottomY = panelY() + panelH() - 22;

        if (scrollOffset > 0)
            addDrawableChild(ButtonWidget.builder(Text.literal("▲"),
                b -> { scrollOffset--; rebuildButtons(); })
                .dimensions(this.width / 2 - 62, bottomY, 50, 16).build());

        if (scrollOffset < totalRows - VISIBLE_ROWS)
            addDrawableChild(ButtonWidget.builder(Text.literal("▼"),
                b -> { scrollOffset++; rebuildButtons(); })
                .dimensions(this.width / 2 - 8, bottomY, 50, 16).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§7Close"),
            b -> this.close())
            .dimensions(this.width / 2 + 46, bottomY, 50, 16).build());
    }

    private Text lbl(String name, boolean on) {
        return Text.literal((on ? "§a■ " : "§c■ ") + "§f" + name);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int px = panelX(), py = panelY(), pw = panelW(), ph = panelH();

        // Panel background
        ctx.fill(px, py, px+pw, py+ph, 0xEE000000);
        ctx.drawBorder(px, py, pw, ph, 0xFF666666);
        ctx.drawBorder(px+1, py+1, pw-2, ph-2, 0xFF222222);

        // Header bar
        ctx.fill(px, py, px+pw, py+HEADER_H, 0xFF0A0A0A);
        ctx.drawBorder(px, py, pw, HEADER_H, 0xFF666666);

        // Title inside header
        ctx.drawCenteredTextWithShadow(textRenderer,
            "§6§lCadens Hacks §f§l3.0", this.width/2, py + 5, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            "§7by Cadenthegoat3662  |  scroll mousewheel  |  M to close",
            this.width/2, py + 17, 0x777777);

        // Page counter
        int totalRows = (int) Math.ceil(hacks.size() / (double) COLS);
        ctx.drawCenteredTextWithShadow(textRenderer,
            "§8Page " + (scrollOffset + 1) + "/" + totalRows,
            this.width/2, panelY() + panelH() - 10, 0x444444);

        // Tooltip on hover
        for (var child : children()) {
            if (child instanceof ButtonWidget btn && btn.isHovered()) {
                String txt = btn.getMessage().getString();
                for (HackEntry h : hacks) {
                    if (txt.contains(h.name())) {
                        ctx.drawTooltip(textRenderer,
                            Text.literal("§8[" + h.cat() + "] §7" + h.desc()), mx, my);
                        break;
                    }
                }
            }
        }

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        int totalRows = (int) Math.ceil(hacks.size() / (double) COLS);
        if (vAmt < 0 && scrollOffset < totalRows - VISIBLE_ROWS) scrollOffset++;
        else if (vAmt > 0 && scrollOffset > 0) scrollOffset--;
        rebuildButtons();
        return true;
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_M) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
