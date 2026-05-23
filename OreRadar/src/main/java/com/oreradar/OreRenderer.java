package com.oreradar;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class OreRenderer {

    record OreEntry(BlockPos pos, float r, float g, float b) {}
    record EntityBox(Vec3d pos, float hw, float h, float r, float g, float b, boolean isPlayer, String name, int dist) {}

    private static final List<OreEntry> cachedOres       = new ArrayList<>();
    private static final List<OreEntry> cachedContainers = new ArrayList<>();
    private static final List<OreEntry> cachedXray       = new ArrayList<>();
    private static final List<EntityBox> entityBoxes     = new ArrayList<>();

    private static long lastOreScan = 0, lastContainerScan = 0, lastXrayScan = 0;
    private static BlockPos lastOrePos = null, lastContainerPos = null, lastXrayPos = null;

    private static final long SCAN_INTERVAL   = 2000;
    private static final int  ORE_RANGE       = 48;
    private static final int  CONTAINER_RANGE = 64;
    private static final int  XRAY_RANGE      = 32;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(OreRenderer::render);
    }

    private static boolean stale(long last, BlockPos lp, BlockPos now, int thresh) {
        return System.currentTimeMillis() - last > SCAN_INTERVAL || lp == null || lp.getManhattanDistance(now) > thresh;
    }

    private static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        BlockPos pp  = mc.player.getBlockPos();
        Vec3d    cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();

        // Ore ESP
        if (OreRadar.radarEnabled && stale(lastOreScan, lastOrePos, pp, 8)) {
            cachedOres.clear();
            for (BlockPos p : BlockPos.iterate(pp.add(-ORE_RANGE,-ORE_RANGE,-ORE_RANGE), pp.add(ORE_RANGE,ORE_RANGE,ORE_RANGE))) {
                float[] c = OreColors.getColor(mc.world.getBlockState(p).getBlock());
                if (c != null) cachedOres.add(new OreEntry(p.toImmutable(), c[0], c[1], c[2]));
            }
            lastOreScan = System.currentTimeMillis(); lastOrePos = pp;
        }
        if (!OreRadar.radarEnabled) cachedOres.clear();

        // XRay
        if (OreRadar.xrayEnabled && stale(lastXrayScan, lastXrayPos, pp, 8)) {
            cachedXray.clear();
            for (BlockPos p : BlockPos.iterate(pp.add(-XRAY_RANGE,-XRAY_RANGE,-XRAY_RANGE), pp.add(XRAY_RANGE,XRAY_RANGE,XRAY_RANGE))) {
                float[] c = OreColors.getColor(mc.world.getBlockState(p).getBlock());
                if (c != null) cachedXray.add(new OreEntry(p.toImmutable(), c[0], c[1], c[2]));
            }
            lastXrayScan = System.currentTimeMillis(); lastXrayPos = pp;
        }
        if (!OreRadar.xrayEnabled) cachedXray.clear();

        // Container ESP
        if (OreRadar.containerEnabled && stale(lastContainerScan, lastContainerPos, pp, 16)) {
            cachedContainers.clear();
            for (BlockPos p : BlockPos.iterate(pp.add(-CONTAINER_RANGE,-60,-CONTAINER_RANGE), pp.add(CONTAINER_RANGE,60,CONTAINER_RANGE))) {
                if (!mc.world.isChunkLoaded(p.getX()>>4, p.getZ()>>4)) continue;
                if (OreColors.isContainer(mc.world.getBlockState(p).getBlock()))
                    cachedContainers.add(new OreEntry(p.toImmutable(), 1f, 0.15f, 0.15f));
            }
            lastContainerScan = System.currentTimeMillis(); lastContainerPos = pp;
        }
        if (!OreRadar.containerEnabled) cachedContainers.clear();

        // Entity ESP (live)
        entityBoxes.clear();
        boolean needEntities = OreRadar.entityEspEnabled || OreRadar.playerEspEnabled
                            || OreRadar.nameTagsEnabled  || OreRadar.tracersEnabled;
        if (needEntities) {
            for (Entity e : mc.world.getEntities()) {
                if (e == mc.player) continue;
                boolean isPlayer = e instanceof PlayerEntity;
                boolean isMob    = e instanceof LivingEntity && !isPlayer;
                int dist = (int) Math.sqrt(e.squaredDistanceTo(mc.player));
                if (isPlayer)
                    entityBoxes.add(new EntityBox(e.getPos(), 0.4f, 1.9f, 1f,1f,0f, true, e.getName().getString(), dist));
                else if (isMob && OreRadar.entityEspEnabled)
                    entityBoxes.add(new EntityBox(e.getPos(), 0.35f, 1.5f, 1f,0.45f,0.1f, false, e.getName().getString(), dist));
            }
        }

        // Draw
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (!cachedOres.isEmpty() && OreRadar.radarEnabled)
            drawBlockLines(ms, cam, cachedOres, 1.5f);

        if (!cachedXray.isEmpty() && OreRadar.xrayEnabled) {
            drawBlockFill(ms, cam, cachedXray);
            drawBlockLines(ms, cam, cachedXray, 2f);
        }

        if (!cachedContainers.isEmpty() && OreRadar.containerEnabled)
            drawBlockLines(ms, cam, cachedContainers, 2f);

        if (!entityBoxes.isEmpty())
            drawEntityLines(ms, cam, entityBoxes);

        if (OreRadar.tracersEnabled && !entityBoxes.isEmpty())
            drawTracers(ms, cam, entityBoxes);

        RenderSystem.disableBlend();

        if ((OreRadar.nameTagsEnabled || OreRadar.playerEspEnabled) && !entityBoxes.isEmpty())
            drawNameTags(ctx, cam, entityBoxes, mc);
    }

    private static void drawBlockLines(MatrixStack ms, Vec3d cam, List<OreEntry> list, float lw) {
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(lw);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Matrix4f m = ms.peek().getPositionMatrix();
        for (OreEntry e : list) {
            float x=(float)(e.pos().getX()-cam.x), y=(float)(e.pos().getY()-cam.y), z=(float)(e.pos().getZ()-cam.z);
            wireBox(buf, m, x, y, z, x+1, y+1, z+1, e.r(), e.g(), e.b(), 1f);
        }
        tryDraw(buf);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void drawBlockFill(MatrixStack ms, Vec3d cam, List<OreEntry> list) {
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f m = ms.peek().getPositionMatrix();
        for (OreEntry e : list) {
            float x=(float)(e.pos().getX()-cam.x), y=(float)(e.pos().getY()-cam.y), z=(float)(e.pos().getZ()-cam.z);
            fillBox(buf, m, x, y, z, x+1, y+1, z+1, e.r(), e.g(), e.b(), 0.28f);
        }
        tryDraw(buf);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void drawEntityLines(MatrixStack ms, Vec3d cam, List<EntityBox> list) {
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(1.5f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Matrix4f m = ms.peek().getPositionMatrix();
        for (EntityBox e : list) {
            if (e.isPlayer() && !OreRadar.playerEspEnabled && !OreRadar.entityEspEnabled) continue;
            if (!e.isPlayer() && !OreRadar.entityEspEnabled) continue;
            float x=(float)(e.pos().x-cam.x), y=(float)(e.pos().y-cam.y), z=(float)(e.pos().z-cam.z);
            wireBox(buf, m, x-e.hw(), y, z-e.hw(), x+e.hw(), y+e.h(), z+e.hw(), e.r(), e.g(), e.b(), 1f);
        }
        tryDraw(buf);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void drawTracers(MatrixStack ms, Vec3d cam, List<EntityBox> list) {
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(1f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Matrix4f m = ms.peek().getPositionMatrix();
        for (EntityBox e : list) {
            if (!e.isPlayer()) continue;
            float tx=(float)(e.pos().x-cam.x), ty=(float)(e.pos().y+e.h()/2-cam.y), tz=(float)(e.pos().z-cam.z);
            buf.vertex(m,0,0,0).color(e.r(),e.g(),e.b(),0.8f).normal(1,0,0);
            buf.vertex(m,tx,ty,tz).color(e.r(),e.g(),e.b(),0.8f).normal(1,0,0);
        }
        tryDraw(buf);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static void drawNameTags(WorldRenderContext ctx, Vec3d cam, List<EntityBox> list, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        MatrixStack ms = ctx.matrixStack();
        for (EntityBox e : list) {
            if (!e.isPlayer()) continue;
            if (!OreRadar.nameTagsEnabled && !OreRadar.playerEspEnabled) continue;
            ms.push();
            ms.translate(e.pos().x-cam.x, e.pos().y+e.h()+0.3-cam.y, e.pos().z-cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-0.025f,-0.025f,0.025f);
            Matrix4f m = ms.peek().getPositionMatrix();
            String tag = "§e"+e.name()+" §7"+e.dist()+"m";
            int tw = tr.getWidth(tag);
            Tessellator tess = Tessellator.getInstance();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buf.vertex(m,-tw/2f-2,-1,0).color(0f,0f,0f,0.6f);
            buf.vertex(m,-tw/2f-2, 9,0).color(0f,0f,0f,0.6f);
            buf.vertex(m, tw/2f+2, 9,0).color(0f,0f,0f,0.6f);
            buf.vertex(m, tw/2f+2,-1,0).color(0f,0f,0f,0.6f);
            tryDraw(buf);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            var vcp = mc.getBufferBuilders().getEntityVertexConsumers();
            tr.draw(tag,-tw/2f,0,0xFFFFFF,false,m,vcp,TextRenderer.TextLayerType.SEE_THROUGH,0,0xF000F0);
            vcp.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            ms.pop();
        }
    }

    private static void wireBox(BufferBuilder buf, Matrix4f m,
                                 float x,float y,float z,float x2,float y2,float z2,
                                 float r,float g,float b,float a) {
        ln(buf,m,x,y,z,  x2,y,z,  r,g,b,a); ln(buf,m,x2,y,z, x2,y,z2, r,g,b,a);
        ln(buf,m,x2,y,z2,x,y,z2,  r,g,b,a); ln(buf,m,x,y,z2, x,y,z,   r,g,b,a);
        ln(buf,m,x,y2,z, x2,y2,z, r,g,b,a); ln(buf,m,x2,y2,z,x2,y2,z2,r,g,b,a);
        ln(buf,m,x2,y2,z2,x,y2,z2,r,g,b,a); ln(buf,m,x,y2,z2,x,y2,z,  r,g,b,a);
        ln(buf,m,x,y,z,  x,y2,z,  r,g,b,a); ln(buf,m,x2,y,z, x2,y2,z, r,g,b,a);
        ln(buf,m,x2,y,z2,x2,y2,z2,r,g,b,a); ln(buf,m,x,y,z2, x,y2,z2, r,g,b,a);
    }

    private static void fillBox(BufferBuilder buf, Matrix4f m,
                                 float x,float y,float z,float x2,float y2,float z2,
                                 float r,float g,float b,float a) {
        quad(buf,m,x,y,z,   x2,y,z,   x2,y2,z,  x,y2,z,   r,g,b,a);
        quad(buf,m,x,y,z2,  x,y2,z2,  x2,y2,z2, x2,y,z2,  r,g,b,a);
        quad(buf,m,x,y,z,   x,y,z2,   x,y2,z2,  x,y2,z,   r,g,b,a);
        quad(buf,m,x2,y,z,  x2,y2,z,  x2,y2,z2, x2,y,z2,  r,g,b,a);
        quad(buf,m,x,y,z,   x2,y,z,   x2,y,z2,  x,y,z2,   r,g,b,a);
        quad(buf,m,x,y2,z,  x,y2,z2,  x2,y2,z2, x2,y2,z,  r,g,b,a);
    }

    private static void quad(BufferBuilder buf, Matrix4f m,
                              float x1,float y1,float z1,float x2,float y2,float z2,
                              float x3,float y3,float z3,float x4,float y4,float z4,
                              float r,float g,float b,float a) {
        buf.vertex(m,x1,y1,z1).color(r,g,b,a);
        buf.vertex(m,x2,y2,z2).color(r,g,b,a);
        buf.vertex(m,x3,y3,z3).color(r,g,b,a);
        buf.vertex(m,x4,y4,z4).color(r,g,b,a);
    }

    private static void ln(BufferBuilder buf, Matrix4f m,
                            float x1,float y1,float z1,float x2,float y2,float z2,
                            float r,float g,float b,float a) {
        buf.vertex(m,x1,y1,z1).color(r,g,b,a).normal(1,0,0);
        buf.vertex(m,x2,y2,z2).color(r,g,b,a).normal(1,0,0);
    }

    private static void tryDraw(BufferBuilder buf) {
        try { BufferRenderer.drawWithGlobalProgram(buf.end()); } catch (Exception ignored) {}
    }
}
