package com.silkpanda.artilian.client;

import com.silkpanda.artilian.mana.ManaData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Compact, kid-readable Mana bar rendered above the right side of the hotbar using the
 * {@code DESIGN.md} arcane palette (#2B2338 / #3A2F4D / #6A5A8C / #47B8FF -> #9C5CFF).
 */
public final class ManaHudOverlay {
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 11;

    private static final int COLOR_PANEL_BG = 0xEE2B2338;
    private static final int COLOR_TRACK_BG = 0xFF3A2F4D;
    private static final int COLOR_BORDER = 0xFF6A5A8C;
    private static final int COLOR_CORNER_RIVET = 0xFFFF9F38;
    private static final int COLOR_TEXT = 0xFFFFF6D6;

    private static final int GRADIENT_START = 0xFF47B8FF;
    private static final int GRADIENT_END = 0xFF9C5CFF;
    private static final int HIGHLIGHT_TOP = 0x997DF9FF;
    private static final int SHADOW_BOTTOM = 0x55181024;

    private ManaHudOverlay() {}

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.options.hideGui || player == null || player.isSpectator()) {
            return;
        }

        ManaData data = ManaData.get(player);
        int mana = data.getMana();
        int maxMana = Math.max(1, data.getMaxMana());

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = screenWidth / 2 + 10;
        int y;
        if (player.isCreative()) {
            y = screenHeight - 36;
        } else {
            y = screenHeight - 52;
            boolean hasAirBar = player.getAirSupply() < player.getMaxAirSupply();
            boolean hasMountHealth = player.getVehicle() instanceof LivingEntity;
            if (hasAirBar || hasMountHealth) {
                y -= 10;
            }
        }

        // Outer slate backing & arcane trim border with Sunsteel gold corner rivets
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_PANEL_BG);
        guiGraphics.renderOutline(x, y, BAR_WIDTH, BAR_HEIGHT, COLOR_BORDER);
        guiGraphics.fill(x, y, x + 1, y + 1, COLOR_CORNER_RIVET);
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + 1, COLOR_CORNER_RIVET);
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + 1, y + BAR_HEIGHT, COLOR_CORNER_RIVET);
        guiGraphics.fill(x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_CORNER_RIVET);

        int innerX = x + 1;
        int innerY = y + 1;
        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = BAR_HEIGHT - 2;
        guiGraphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, COLOR_TRACK_BG);

        float ratio = Mth.clamp((float) mana / (float) maxMana, 0.0F, 1.0F);
        int fillWidth = Math.round(innerWidth * ratio);
        if (fillWidth > 0) {
            for (int px = 0; px < fillWidth; px++) {
                float t = innerWidth <= 1 ? 0.0F : (float) px / (float) (innerWidth - 1);
                int colColor = lerpColor(GRADIENT_START, GRADIENT_END, t);
                guiGraphics.fill(innerX + px, innerY, innerX + px + 1, innerY + innerHeight, colColor);
            }
            guiGraphics.fill(innerX, innerY, innerX + fillWidth, innerY + 1, HIGHLIGHT_TOP);
            guiGraphics.fill(innerX, innerY + innerHeight - 1, innerX + fillWidth, innerY + innerHeight, SHADOW_BOTTOM);
        }

        Font font = minecraft.font;
        Component label = Component.translatable("hud.artilian.mana", mana, maxMana);
        guiGraphics.drawCenteredString(font, label, x + BAR_WIDTH / 2, y + 2, COLOR_TEXT);
    }

    private static int lerpColor(int start, int end, float t) {
        int r1 = (start >> 16) & 0xFF;
        int g1 = (start >> 8) & 0xFF;
        int b1 = start & 0xFF;
        int r2 = (end >> 16) & 0xFF;
        int g2 = (end >> 8) & 0xFF;
        int b2 = end & 0xFF;
        int r = Math.round(Mth.lerp(t, r1, r2));
        int g = Math.round(Mth.lerp(t, g1, g2));
        int b = Math.round(Mth.lerp(t, b1, b2));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
