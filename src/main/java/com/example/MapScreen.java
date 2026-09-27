package com.example;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class MapScreen extends Screen {

    /*
     * MAP SETTINGS
     */

    // How far from the player the map extends.
    // 1000 = 1000 blocks in every direction.
    private static final int MAP_RADIUS = 256;

    // One map pixel represents an 8x8 Minecraft block area.
    private static final int BLOCKS_PER_PIXEL = 2;

    // Approximately 250x250 pixels.
    private static final int MAP_SIZE =
            (MAP_RADIUS * 2) / BLOCKS_PER_PIXEL;

    /*
     * MAP DATA
     */

    private int[][] mapColors;

    private int playerX;
    private int playerZ;

    private int mapLeft;
    private int mapTop;
    private int mapWidth;
    private int mapHeight;

    public MapScreen() {
        super(Text.literal("World Map"));
    }

    @Override
    protected void init() {
        super.init();

        if (this.client == null || this.client.player == null) {
            return;
        }

        playerX = this.client.player.getBlockX();
        playerZ = this.client.player.getBlockZ();

        /*
         * Generate the map ONCE.
         *
         * We don't generate it every frame.
         */
        generateMap();
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_M) {
            if (this.client != null) {
                this.client.setScreen(null);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Generates the map data.
     * <p>
     * Each pixel represents an 8x8 area.
     */
    private void generateMap() {

        mapColors = new int[MAP_SIZE][MAP_SIZE];

        ClientWorld world = this.client.world;

        if (world == null) {
            return;
        }

        int startX = playerX - MAP_RADIUS;
        int startZ = playerZ - MAP_RADIUS;

        for (int pixelX = 0; pixelX < MAP_SIZE; pixelX++) {

            for (int pixelZ = 0; pixelZ < MAP_SIZE; pixelZ++) {

                int worldX =
                        startX + pixelX * BLOCKS_PER_PIXEL;

                int worldZ =
                        startZ + pixelZ * BLOCKS_PER_PIXEL;

                mapColors[pixelX][pixelZ] =
                        getAverageColor(
                                world,
                                worldX,
                                worldZ
                        );
            }
        }
    }

    /**
     * Gets the average terrain color for an 8x8 area.
     * <p>
     * IMPORTANT:
     * We check whether the chunk is loaded before accessing it.
     * <p>
     * This means the map will NOT cause Minecraft to load
     * thousands of new chunks.
     */
    private int getAverageColor(
            ClientWorld world,
            int startX,
            int startZ
    ) {

        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;

        int samples = 0;

        for (int x = 0; x < BLOCKS_PER_PIXEL; x++) {

            for (int z = 0; z < BLOCKS_PER_PIXEL; z++) {

                int worldX = startX + x;
                int worldZ = startZ + z;

                /*
                 * Check if this chunk is already loaded.
                 *
                 * false means:
                 * "Do not create/load a chunk if it isn't loaded."
                 */
                if (!world.isChunkLoaded(
                        worldX >> 4,
                        worldZ >> 4
                )) {
                    continue;
                }

                /*
                 * Use the heightmap to find the surface.
                 *
                 * This is MUCH faster than searching from the
                 * top of the world downward.
                 */
                int y = world.getTopY(
                        net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                        worldX,
                        worldZ
                );

                if (y <= world.getBottomY()) {
                    continue;
                }

                BlockPos pos =
                        new BlockPos(worldX, y - 1, worldZ);

                BlockState state =
                        world.getBlockState(pos);

                int color =
                        getBlockColor(state);

                totalRed += (color >> 16) & 0xFF;
                totalGreen += (color >> 8) & 0xFF;
                totalBlue += color & 0xFF;

                samples++;
            }
        }

        /*
         * Nothing loaded in this 8x8 area.
         * Dark gray = unexplored/unloaded.
         */
        if (samples == 0) {
            return 0xFF303030;
        }

        /*
         * Average the colors.
         */
        int red = totalRed / samples;
        int green = totalGreen / samples;
        int blue = totalBlue / samples;

        return 0xFF000000
                | (red << 16)
                | (green << 8)
                | blue;
    }

    /**
     * Converts Minecraft blocks into map colors.
     */
    private int getBlockColor(BlockState state) {

        if (state.isOf(Blocks.GRASS_BLOCK)) {
            return 0x55AA33;
        }

        if (state.isOf(Blocks.DIRT)
                || state.isOf(Blocks.COARSE_DIRT)
                || state.isOf(Blocks.ROOTED_DIRT)) {
            return 0x8B5A2B;
        }

        if (state.isOf(Blocks.SAND)
                || state.isOf(Blocks.RED_SAND)) {
            return 0xE5D080;
        }

        if (state.isOf(Blocks.WATER)
                || state.isOf(Blocks.BUBBLE_COLUMN)) {
            return 0x3366CC;
        }

        if (state.isOf(Blocks.STONE)
                || state.isOf(Blocks.DEEPSLATE)) {
            return 0x777777;
        }

        if (state.isOf(Blocks.SNOW)
                || state.isOf(Blocks.SNOW_BLOCK)) {
            return 0xFFFFFF;
        }

        if (state.isOf(Blocks.ICE)
                || state.isOf(Blocks.PACKED_ICE)) {
            return 0x99DDFF;
        }

        if (state.isOf(Blocks.GRAVEL)) {
            return 0x888888;
        }

        if (state.isOf(Blocks.OAK_LEAVES)
                || state.isOf(Blocks.BIRCH_LEAVES)
                || state.isOf(Blocks.SPRUCE_LEAVES)
                || state.isOf(Blocks.JUNGLE_LEAVES)
                || state.isOf(Blocks.ACACIA_LEAVES)
                || state.isOf(Blocks.DARK_OAK_LEAVES)
                || state.isOf(Blocks.MANGROVE_LEAVES)) {
            return 0x228833;
        }

        if (state.isOf(Blocks.OAK_LOG)
                || state.isOf(Blocks.BIRCH_LOG)
                || state.isOf(Blocks.SPRUCE_LOG)
                || state.isOf(Blocks.JUNGLE_LOG)) {
            return 0x6B4523;
        }

        return 0x666666;
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {

        context.fill(
                0,
                0,
                this.width,
                this.height,
                0xFFFF00FF
        );

        /*
         * DON'T call renderBackground().
         *
         * This is what removes the vanilla background blur.
         */

        // Just use a solid dark background.
        context.fill(
                0,
                0,
                this.width,
                this.height,
                0xFF101010
        );

        if (mapColors == null) {
            return;
        }

        /*
         * Calculate map size.
         */
        mapWidth = this.width - 120;
        mapHeight = this.height - 120;

        mapLeft =
                (this.width - mapWidth) / 2;

        mapTop = 45;

        /*
         * Draw the map.
         */

        for (int x = 0; x < MAP_SIZE; x++) {

            for (int z = 0; z < MAP_SIZE; z++) {

                int screenX =
                        mapLeft
                                + x * mapWidth / MAP_SIZE;

                int screenY =
                        mapTop
                                + z * mapHeight / MAP_SIZE;

                int nextX =
                        mapLeft
                                + (x + 1) * mapWidth / MAP_SIZE;

                int nextY =
                        mapTop
                                + (z + 1) * mapHeight / MAP_SIZE;

                context.fill(
                        screenX,
                        screenY,
                        nextX,
                        nextY,
                        mapColors[x][z]
                );
            }
        }

        /*
         * Map border.
         */
        context.drawBorder(
                mapLeft,
                mapTop,
                mapWidth,
                mapHeight,
                0xFFFFFFFF
        );

        /*
         * Player marker.
         */
        int centerX =
                mapLeft + mapWidth / 2;

        int centerY =
                mapTop + mapHeight / 2;

        // White outline
        context.fill(
                centerX - 4,
                centerY - 4,
                centerX + 5,
                centerY + 5,
                0xFFFFFFFF
        );

        // Red center
        context.fill(
                centerX - 2,
                centerY - 2,
                centerX + 3,
                centerY + 3,
                0xFFFF0000
        );

        /*
         * Title.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("World Map"),
                this.width / 2,
                15,
                0xFFFFFF
        );

        /*
         * Coordinates.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(
                        "X: " + playerX
                                + "   Z: " + playerZ
                                + "   |   1000 block radius"
                ),
                this.width / 2,
                this.height - 20,
                0xFFFFFF
        );

        /*
         * North indicator.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("N"),
                centerX,
                mapTop + 5,
                0xFFFFFFFF
        );

        //super.render(
                //context,
                //mouseX,
                //mouseY,
                //delta
        //);
    }

    /*
     * Don't pause singleplayer when the map is open.
     */
    @Override
    public boolean shouldPause() {
        return false;
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}


