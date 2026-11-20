package com.silkpanda.artilian.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.silkpanda.artilian.ArtilianMod;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Custom twilight skybox for Alex's Eye Realm featuring painted twinkling star-eyes
 * and a giant Great Sky Eye overhead that periodically blinks.
 */
public class EyeRealmSkyEffects extends DimensionSpecialEffects {

    private static final ResourceLocation GREAT_SKY_EYE_TEX =
            ArtilianMod.id("textures/environment/great_sky_eye.png");
    private static final ResourceLocation SKY_STAR_EYE_TEX =
            ArtilianMod.id("textures/environment/sky_star_eye.png");

    private static final float[] STAR_ANGLES_X = { -42f, -18f, 24f, 52f, -60f, 12f, 38f, -30f, 65f, -8f };
    private static final float[] STAR_ANGLES_Y = { 28f, -45f, 35f, -22f, 10f, 58f, -55f, 64f, 18f, -72f };

    public EyeRealmSkyEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return new Vec3(0.24, 0.12, 0.36);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(
            ClientLevel level,
            int ticks,
            float partialTick,
            Matrix4f modelViewMatrix,
            Camera camera,
            Matrix4f projectionMatrix,
            boolean isFoggy,
            Runnable setupFog) {
        setupFog.run();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();

        // 0. Twilight skybox dome (deep Artilian slate zenith -> twilight violet horizon)
        drawTwilightSkybox(tesselator, poseStack.last().pose());

        // 1. Twinkling painted star-eyes around the twilight dome
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, SKY_STAR_EYE_TEX);
        float time = ticks + partialTick;
        for (int i = 0; i < STAR_ANGLES_X.length; i++) {
            float twinkle = 0.45F + 0.55F * Mth.sin(time * 0.08F + i * 1.3F);
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_ANGLES_X[i]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_ANGLES_Y[i]));
            Matrix4f mat = poseStack.last().pose();

            float size = 3.2F;
            float yDist = 92.0F;
            int a = (int) (235 * twinkle);

            BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buf.addVertex(mat, -size, yDist, -size).setUv(0.0F, 0.0F).setColor(255, 255, 255, a);
            buf.addVertex(mat, size, yDist, -size).setUv(1.0F, 0.0F).setColor(255, 255, 255, a);
            buf.addVertex(mat, size, yDist, size).setUv(1.0F, 1.0F).setColor(255, 255, 255, a);
            buf.addVertex(mat, -size, yDist, size).setUv(0.0F, 1.0F).setColor(255, 255, 255, a);
            BufferUploader.drawWithShader(buf.buildOrThrow());
            poseStack.popPose();
        }

        // 2. Giant painted Great Sky Eye high overhead with 4-frame periodic blink (160-tick cycle)
        float cycle = (time % 160.0F) / 160.0F;
        int frame;
        if (cycle < 0.80F) {
            frame = 0; // Wide open
        } else if (cycle < 0.86F) {
            frame = 1; // Half-closing
        } else if (cycle < 0.93F) {
            frame = 2; // Blink shut
        } else {
            frame = 3; // Opening
        }
        float v0 = frame * 0.25F;
        float v1 = v0 + 0.25F;

        RenderSystem.setShaderTexture(0, GREAT_SKY_EYE_TEX);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-52.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0F));
        Matrix4f eyeMat = poseStack.last().pose();
        float dist = 85.0F;
        float halfW = 26.0F;
        float halfH = 13.0F;

        BufferBuilder eyeBuf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        eyeBuf.addVertex(eyeMat, -halfW, dist, -halfH).setUv(0.0F, v0).setColor(255, 255, 255, 245);
        eyeBuf.addVertex(eyeMat, halfW, dist, -halfH).setUv(1.0F, v0).setColor(255, 255, 255, 245);
        eyeBuf.addVertex(eyeMat, halfW, dist, halfH).setUv(1.0F, v1).setColor(255, 255, 255, 245);
        eyeBuf.addVertex(eyeMat, -halfW, dist, halfH).setUv(0.0F, v1).setColor(255, 255, 255, 245);
        BufferUploader.drawWithShader(eyeBuf.buildOrThrow());

        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        return true;
    }

    private static void drawTwilightSkybox(Tesselator tesselator, Matrix4f mat) {
        float s = 100.0F;
        // Top (+Y): Deep Artilian Slate (#2B2338)
        BufferBuilder top = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        top.addVertex(mat, -s, s, -s).setColor(43, 35, 56, 255);
        top.addVertex(mat, -s, s, s).setColor(43, 35, 56, 255);
        top.addVertex(mat, s, s, s).setColor(43, 35, 56, 255);
        top.addVertex(mat, s, s, -s).setColor(43, 35, 56, 255);
        BufferUploader.drawWithShader(top.buildOrThrow());

        // Bottom (-Y): Deep void (#221830)
        BufferBuilder bot = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bot.addVertex(mat, -s, -s, s).setColor(34, 24, 48, 255);
        bot.addVertex(mat, -s, -s, -s).setColor(34, 24, 48, 255);
        bot.addVertex(mat, s, -s, -s).setColor(34, 24, 48, 255);
        bot.addVertex(mat, s, -s, s).setColor(34, 24, 48, 255);
        BufferUploader.drawWithShader(bot.buildOrThrow());

        // 4 horizon walls: zenith (#2B2338) -> twilight magenta-violet horizon (#5B2178)
        drawWall(tesselator, mat, -s, s, -s, s, s, -s, -s, -s, -s, s, -s, -s);
        drawWall(tesselator, mat, s, s, s, -s, s, s, -s, -s, s, s, -s, s);
        drawWall(tesselator, mat, -s, s, s, -s, s, -s, -s, -s, -s, -s, -s, s);
        drawWall(tesselator, mat, s, s, -s, s, s, s, s, -s, s, s, -s, -s);
    }

    private static void drawWall(
            Tesselator tesselator,
            Matrix4f mat,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4) {
        // Order: (x1,y1,z1)=top-left, (x2,y2,z2)=top-right, (x4,y4,z4)=bot-right, (x3,y3,z3)=bot-left
        BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(mat, x1, y1, z1).setColor(43, 35, 56, 255);
        buf.addVertex(mat, x2, y2, z2).setColor(43, 35, 56, 255);
        buf.addVertex(mat, x4, y4, z4).setColor(91, 33, 120, 255);
        buf.addVertex(mat, x3, y3, z3).setColor(91, 33, 120, 255);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}
