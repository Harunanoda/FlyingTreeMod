package com.flyingtree;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.*;

public class FlyingTreeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // イベントリスナーを登録する
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            // クライアント側（シングルプレイの画面側）では処理しない
            if (world.isClient) {
                return ActionResult.PASS;
            }

            BlockState state = world.getBlockState(pos);

            // 破壊されたブロックが原木かどうかを判定する
            if (!state.isIn(BlockTags.LOGS)) {
                return ActionResult.PASS;
            }

            // 木全体を探索するためのキューと訪問済みセット
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(pos);

            int count = 0;
            int maxBlocks = 200; // 吹き飛ばすブロックの最大数を制限する

            while (!queue.isEmpty() && count < maxBlocks) {
                BlockPos current = queue.poll();
                if (visited.contains(current)) {
                    continue;
                }

                BlockState bs = world.getBlockState(current);

                // 原木または葉ブロックを探索対象とする
                if (bs.isIn(BlockTags.LOGS) || bs.isIn(BlockTags.LEAVES)) {
                    visited.add(current);
                    count++;

                    // 元のブロックをワールドから消去する
                    world.removeBlock(current, false);

                    // FallingBlockEntityを生成し、速度を設定する
                    FallingBlockEntity fbe = FallingBlockEntity.spawnFromBlock(world, current, bs);
                    fbe.setVelocity(0, 0.5, 0); // 上向きの速度
                    fbe.setNoGravity(true); // 重力を無効化

                    // 隣接するブロックも探索対象に追加する
                    for (Direction dir : Direction.values()) {
                        queue.add(current.offset(dir));
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}