package no.elg.infiniteBootleg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.VisUI;
import no.elg.infiniteBootleg.console.ConsoleHandler;
import no.elg.infiniteBootleg.console.ConsoleLogger;
import no.elg.infiniteBootleg.util.CancellableThreadScheduler;
import no.elg.infiniteBootleg.world.Chunk;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.generator.SimplexChunkGenerator;
import no.elg.infiniteBootleg.world.render.WorldRender;

import java.io.File;
import java.util.Random;

import static no.elg.infiniteBootleg.ProgramArgs.executeArgs;

public class Main extends ApplicationAdapter {

    public static final String WORLD_FOLDER = "worlds" + File.separatorChar;
    public static final String TEXTURES_FOLDER = "textures" + File.separatorChar;
    public static final String TEXTURES_BLOCK_FILE = TEXTURES_FOLDER + "blocks.pack";
    public static final String VERSION_FILE = "version";
    public static final CancellableThreadScheduler SCHEDULER = new CancellableThreadScheduler();

    private static InputMultiplexer inputMultiplexer;
    private TextureAtlas textureAtlas;

    public static boolean renderGraphic = true;
    private World world;

    private ConsoleHandler console;

    private SpriteBatch batch;
    private BitmapFont font;
    private static Main inst;

    public Main(String[] args) {
        executeArgs(args);
    }

    @Override
    public void create() {
        inst = this;
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(true);

        batch = new SpriteBatch();
        inputMultiplexer = new InputMultiplexer();
        VisUI.load();
        console = new ConsoleHandler();
        Gdx.input.setInputProcessor(inputMultiplexer);

        batch.setProjectionMatrix(camera.combined);
        textureAtlas = new TextureAtlas(TEXTURES_BLOCK_FILE);

        int worldSeed = new Random().nextInt();
        world = new World(new SimplexChunkGenerator(worldSeed), worldSeed);

//        world.getRender().getCamera().zoom = 24;

        font = new BitmapFont(true);

        Thread sleeperThread = new Thread("Sleeper Thread") {
            @Override
            public void run() {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    System.err.println("sleep thread woke!");
                    e.printStackTrace();
                }
            }
        };
        sleeperThread.setDaemon(true);
        sleeperThread.start();
    }

    @Override
    public void render() {
        if (!Main.renderGraphic) {
            return;
        }
        Gdx.gl.glClearColor(0.2f, 0.3f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.getInput().update();
        world.getRender().render();

        final Vector3 unproject = world.getRender().getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        final int blockX = (int) Math.floor(unproject.x / World.BLOCK_SIZE);
        final int blockY = (int) Math.floor(unproject.y / World.BLOCK_SIZE);

        int[] vChunks = world.getRender().getChunksInView();

        int chunksInView = Math.abs(vChunks[WorldRender.HOR_END] - vChunks[WorldRender.HOR_START]) *
                           Math.abs(vChunks[WorldRender.VERT_END] - vChunks[WorldRender.VERT_START]);
        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 10);
        font.draw(batch, "Delta time: " + Gdx.graphics.getDeltaTime(), 10, 25);
        font.draw(batch, "Pointing at block (" + blockX + ", " + blockY + ") in chunk " +
                         world.getChunkFromWorld(blockX, blockY).getLocation(), 10, 40);
        font.draw(batch,
                  "Viewing " + chunksInView + " chunks (" + chunksInView * Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH + " blocks)", 10,
                  55);
        font.draw(batch, "Zoom: " + world.getRender().getCamera().zoom, 10, 70);

        TextureRegion tr = world.getInput().getSelected().getTexture();
        if (tr != null) {
            TextureRegion wrapper = new TextureRegion(tr);
            wrapper.flip(false, true);
            batch.draw(wrapper, Gdx.graphics.getWidth() - 48, 16, 32, 32);
        }
        batch.end();
        console.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        textureAtlas.dispose();
        console.dispose();
        font.dispose();
        VisUI.dispose();
    }

    @Override
    public void resize(int width, int height) {
        world.getInput().resize(width, height);
    }

    public static InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public ConsoleLogger getConsoleLogger() {
        return console;
    }

    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public static Main inst() {
        return inst;
    }
}
