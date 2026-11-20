package com.silkpanda.artilian.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Custom twilight skybox for Alex's Eye Realm featuring twinkling star-eyes
 * and a giant Great Sky Eye overhead that periodically blinks.
 */
public class EyeRealmSkyEffects extends DimensionSpecialEffects {

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
        RenderSystem.enableCull();

        // 1. Twinkling star-eyes around the twilight dome
        float time = ticks + partialTick;
        for (int i = 0; i < STAR_ANGLES_X.length; i++) {
            float twinkle = 0.45F + 0.55F * Mth.sin(time * 0.08F + i * 1.3F);
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(STAR_ANGLES_X[i]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(STAR_ANGLES_Y[i]));
            Matrix4f mat = poseStack.last().pose();

            float size = 2.2F;
            float yDist = 92.0F;
            boolean cyan = (i % 2 == 0);
            int r = cyan ? 125 : 217;
            int g = cyan ? 249 : 70;
            int b = cyan ? 255 : 239;
            int a = (int) (180 * twinkle);

            BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.addVertex(mat, -size, yDist, -size * 0.65F).setColor(r, g, b, a);
            buf.addVertex(mat, size, yDist, -size * 0.65F).setColor(r, g, b, a);
            buf.addVertex(mat, size, yDist, size * 0.65F).setColor(r, g, b, a);
            buf.addVertex(mat, -size, yDist, size * 0.65F).setColor(r, g, b, a);
            BufferUploader.drawWithShader(buf.buildOrThrow());
            poseStack.popPose();
        }

        // 2. Giant Great Sky Eye high overhead with smooth periodic blink (160-tick cycle)
        float cycle = (time % 160.0F) / 160.0F;
        float openness;
        if (cycle < 0.80F) {
            openness = 1.0F;
        } else if (cycle < 0.88F) {
            openness = 1.0F - (cycle - 0.80F) / 0.08F;
        } else if (cycle < 0.92F) {
            openness = 0.05F;
        } else {
            openness = 0.05F + 0.95F * ((cycle - 0.92F) / 0.08F);
        }
        openness = Mth.clamp(openness, 0.05F, 1.0F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-52.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0F));
        Matrix4f eyeMat = poseStack.last().pose();
        float dist = 85.0F;

        // Outer sclera (soft lavender-white)
        drawQuad(tesselator, eyeMat, dist, 20.0F, 11.5F * openness, 232, 214, 248, 225);
        // Twilight magenta iris
        drawQuad(tesselator, eyeMat, dist - 0.2F, 10.5F, 9.0F * openness, 217, 70, 239, 235);
        // Cyan pupil glow ring
        drawQuad(tesselator, eyeMat, dist - 0.4F, 6.2F, 6.2F * openness, 125, 249, 255, 245);
        // Deep void pupil slit
        drawQuad(tesselator, eyeMat, dist - 0.6F, 2.6F, 4.8F * openness, 24, 12, 38, 255);

        poseStack.popPose();

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

    private static void drawQuad(
            Tesselator tesselator,
            Matrix4f mat,
            float y,
            float halfW,
            float halfH,
            int r,
            int g,
            int b,
            int a) {
        BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(mat, -halfW, y, -halfH).setColor(r, g, b, a);
        buf.addVertex(mat, halfW, y, -halfH).setColor(r, g, b, a);
        buf.addVertex(mat, halfW, y, halfH).setColor(r, g, b, a);
        buf.addVertex(mat, -halfW, y, halfH).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}
