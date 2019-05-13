package no.elg.infiniteBootleg.world;

import com.badlogic.gdx.utils.Disposable;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.input.WorldInputHandler;
import no.elg.infiniteBootleg.world.generator.ChunkGenerator;
import no.elg.infiniteBootleg.world.render.WorldRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Elg
 */
public class World implements Disposable {

    public final static int BLOCK_SIZE = 16;

    private final ChunkGenerator generator;
    private final long seed;
    private final Random random;
    private final Map<Location, Chunk> chunks;

    //only exists when graphics exits
    private WorldInputHandler input;
    private WorldRender render;

    private String name = "World";
    private final UUID uuid;


    /**
     * Generate a world with a random seed
     *
     * @param generator
     */
    public World(@NotNull ChunkGenerator generator) {
        this(generator, new Random().nextLong());
    }

    public World(@NotNull ChunkGenerator generator, long seed) {
        this.generator = generator;
        this.seed = seed;
        random = new Random(seed);
        chunks = new WeakHashMap<>();

        if (Main.RENDER_GRAPHIC) {
            render = new WorldRender(this);
            input = new WorldInputHandler(render);
        }
        uuid = UUID.randomUUID();
    }

    @NotNull
    public Chunk getChunk(int chunkX, int chunkY) {
        return getChunk(new Location(chunkX, chunkY));
    }

    @NotNull
    public Chunk getChunk(@NotNull Location chunkLoc) {
        return chunks.computeIfAbsent(chunkLoc, loc -> generator.generate(this, loc, random));
    }

    @NotNull
    public Chunk getChunkFromWorld(int worldX, int worldY) {
        int chunkX = (int) Math.floor((float) worldX / Chunk.CHUNK_WIDTH);
        int chunkY = (int) Math.floor((float) worldY / Chunk.CHUNK_HEIGHT);

        return getChunk(chunkX, chunkY);
    }

    public boolean isLoadedAt(@NotNull Location chunkLoc) {
        return chunks.containsKey(chunkLoc);
    }

    /**
     * @param chunk
     *
     * @return If the chunk was unloaded
     */
    public boolean unload(@Nullable Chunk chunk) {
        if (chunk == null || !chunk.isLoaded() || !isLoadedAt(chunk.getLocation())) {
            return false;
        }
        chunks.remove(chunk.getLocation());
        chunk.unload();
        return true;
    }

    @NotNull
    public Chunk getChunkFromWorld(@NotNull Location location) {
        return getChunkFromWorld(location.x, location.y);
    }

    public long getSeed() {
        return seed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorldRender getRender() {
        return render;
    }

    @Override
    public String toString() {
        return "World{" + "name='" + name + '\'' + ", uuid=" + uuid + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        World world = (World) o;
        return Objects.equals(uuid, world.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public void dispose() {
        render.dispose();
        input.dispose();
    }
}
