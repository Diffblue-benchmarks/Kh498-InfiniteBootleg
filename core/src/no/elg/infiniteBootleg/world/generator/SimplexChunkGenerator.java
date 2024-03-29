package no.elg.infiniteBootleg.world.generator;

import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.util.CoordUtil;
import no.elg.infiniteBootleg.world.Chunk;
import no.elg.infiniteBootleg.world.Location;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.generator.biome.Biome;
import no.elg.infiniteBootleg.world.generator.simplex.PerlinNoise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static no.elg.infiniteBootleg.world.Chunk.CHUNK_HEIGHT;
import static no.elg.infiniteBootleg.world.Chunk.CHUNK_WIDTH;

/**
 * @author Elg
 */
public class SimplexChunkGenerator implements ChunkGenerator {

    private PerlinNoise noise;

    public SimplexChunkGenerator(int seed) {
        noise = new PerlinNoise(seed);
    }

    private double calcHeightMap(int chunkX, int x) {
        final int a = 1;
        return (noise.noise(chunkX * CHUNK_WIDTH + x, 0.5, 0.5, a, 0.001) + a) / 2;
    }

    private Biome getBiome(double height) {
        if (height > 0.5) {
            return Biome.ANCIENT_MOUNTAINS;
        }
        else {
            return Biome.PLAINS;
        }
    }

    @Override
    public @NotNull Chunk generate(@Nullable World world, @NotNull Location chunkPos, @NotNull Random random) {
        Chunk chunk = new Chunk(world, chunkPos);
        Main.SCHEDULER.executeAsync(() -> {
            for (int x = 0; x < CHUNK_WIDTH; x++) {
                double biomeWeight = calcHeightMap(chunkPos.x, x);
                Biome biome = getBiome(biomeWeight);
                double y;

                y = biome.heightAt(noise, chunkPos.x, x) * biomeWeight;

                int height = (int) y;
                int elevationChunk = CoordUtil.worldToChunk(height);
                if (chunkPos.y == elevationChunk) {
                    biome.fillUpTo(noise, chunk, x, (int) (y - elevationChunk * CHUNK_WIDTH), height);
                }
                else if (chunkPos.y < elevationChunk) {
                    biome.fillUpTo(noise, chunk, x, CHUNK_HEIGHT, height);
                }
            }
            chunk.updateTexture(false);
        });
        return chunk;
    }
}
