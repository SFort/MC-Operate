package tf.ssf.sfort.operate.pipe;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.pipe.util.TransportedStack;
import tf.ssf.sfort.operate.util.SyncableLinkedList;

import static tf.ssf.sfort.operate.MainClient.mc;

public class AbstractPipeRenderer<T extends AbstractPipeEntity> {

	public void render(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		renderConnections(entity, tickDelta, matrix, vertex, light, overlay);
		renderItems(entity, tickDelta, matrix, vertex, light, overlay);
	}
	public void renderItems(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		ItemRenderer ir = mc.getItemRenderer();
		World world = entity.getWorld();
		if (world == null) return;
		final float step = 1 / (float) entity.getPipeTransferTime();
		matrix.push();
		boolean xFlip = false;
		boolean xMid =  false;
		boolean zFlip = false;
		boolean zMid = false;
		{
			Entity cam = mc.cameraEntity;
			BlockPos entityPos = entity.getPos();
			if (cam != null) {
				if (entityPos.getX()+.4 < cam.getX()){
					xFlip = true;
					if (entityPos.getX()+.6 < cam.getX()){
						xMid = true;
					}
				}
				if (entityPos.getZ()+.4 < cam.getZ()){
					zFlip = true;
					if (entityPos.getZ()+.6 < cam.getZ()){
						zMid = true;
					}
				}
			}
		}
		for (SyncableLinkedList.Node<TransportedStack> stackNode = entity.itemQueue.first; stackNode!=null; stackNode=stackNode.next) {
			int pipeTransferTime = entity.getPipeTransferTime();
			long diff = Math.min(pipeTransferTime, stackNode.item.travelTime - world.getTime());
			double progress = 0;
			if (diff > 0) {
				float divDiff = diff / (float)pipeTransferTime;
				progress = MathHelper.lerp(tickDelta, divDiff + step, divDiff);
			}
			if (progress < 0.99) {
				matrix.push();
				stackNode.item.render(progress, world, ir, tickDelta, matrix, vertex, light, overlay, xFlip, zFlip, xMid, zMid);
				matrix.pop();
			}
		}
		matrix.pop();
	}
	public void renderConnections(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		renderConnections(entity.getWorld(), entity.getPos(), entity.connectedSidesByte, tickDelta, matrix, vertex, light, overlay, 1, 1, 1, .8f, .5f, .5f, .5f, .7f);
	}
	public void renderConnections(World world, BlockPos epos, byte connectedSidesByte, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay, float red1, float green1, float blue1, float alpha1, float red2, float green2, float blue2, float alpha2) {
		BlockPos.Mutable pos = epos.mutableCopy();
		for (Direction dir : Direction.values()) {
			if ((connectedSidesByte & (1 << dir.ordinal())) != 0) {
				matrix.push();
				pos.set(epos).move(dir);
				if (world != null) {
					BlockEntity nentity = world.getBlockEntity(pos);
					MatrixStack.Entry entry = matrix.peek();
					VertexConsumer vc = vertex.getBuffer(RenderLayer.LINES);
					if (nentity instanceof AbstractPipeEntity || nentity instanceof Inventory) {
						switch (dir) {
							case UP -> {
								drawGradLine(entry, vc, .375f, .75f, .625f, .375f, 1.25f, .625f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
								drawGradLine(entry, vc, .625f, .75f, .625f, .625f, 1.25f, .625f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
							case DOWN -> {
								drawGradLine(entry, vc, .375f, .25f, .375f, .375f, -.25f, .375f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
								drawGradLine(entry, vc, .625f, .25f, .375f, .625f, -.25f, .375f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
							case NORTH -> {
								drawGradLine(entry, vc, .625f, .375f, .25f, .625f, .375f, -.25f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
							case SOUTH -> {
								drawGradLine(entry, vc, .375f, .375f, .75f, .375f, .375f, 1.25f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
							case WEST -> {
								drawGradLine(entry, vc, .25f, .375f, .625f, -.25f, .375f, .625f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
							case EAST -> {
								drawGradLine(entry, vc, .75f, .375f, .375f, 1.25f, .375f, .375f, red1, green1, blue1, alpha1, red2, green2, blue2, alpha2);
							}
						}
					} else {
						matrix.translate(.5, .5, .5);
						matrix.multiply(dir.getRotationQuaternion());
						drawSideLines(entry, vc, red1, green1, blue1, alpha1);
					}
				}
				matrix.pop();
			}
		}
	}

	public static void drawSideLines(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red1, float green1, float blue1, float alpha1) {
		drawLine(entry, vertexConsumer, -.125f, .25f, -.125f, -.125f, .5f, -.125f, red1, green1, blue1, alpha1);
		drawLine(entry, vertexConsumer, .125f, .25f, -.125f, .125f, .5f, -.125f, red1, green1, blue1, alpha1);
		drawLine(entry, vertexConsumer, -.125f, .25f, .125f, -.125f, .5f, .125f, red1, green1, blue1, alpha1);
		drawLine(entry, vertexConsumer, .125f, .25f, .125f, .125f, .5f, .125f, red1, green1, blue1, alpha1);

	}
	public static void drawGradLine(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red1, float green1, float blue1, float alpha1, float red2, float green2, float blue2, float alpha2) {
		vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red1, green1, blue1, alpha1).normal(entry.getNormalMatrix(), 1, 1, 1).next();
		vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red2, green2, blue2, alpha2).normal(entry.getNormalMatrix(), 1, 1, 1).next();
	}
	public static void drawLine(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
		vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), 1, 1, 1).next();
		vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), 1, 1, 1).next();
	}
}
