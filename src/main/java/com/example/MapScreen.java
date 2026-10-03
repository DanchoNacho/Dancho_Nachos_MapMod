package com.example;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


public class MapScreen extends Screen {

    /*
     * MAP SETTINGS
     */

    // How far from the player the map extends.
    // 1000 = 1000 blocks in every direction.
    private static final int MAP_RADIUS = 256;

    // One map pixel represents an 8x8 Minecraft block area.
    private static final int BLOCKS_PER_PIXEL = 4;

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

    private double mapCenterX;
    private double mapCenterZ;

    private boolean dragging = false;
    private double lastMouseX;
    private double lastMouseY;

    private double zoom = 1.0;

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

        mapCenterX = playerX;
        mapCenterZ = playerZ;

        CreditsManager.setCredits(
                this.client.player.getUuid(),
                1234.56
        );

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
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {

            mapCenterX = playerX;
            mapCenterZ = playerZ;

            generateMap();

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

        int visibleRadius =
                (int)(MAP_RADIUS / zoom);

        int startX =
                (int)(mapCenterX - visibleRadius);

        int startZ =
                (int)(mapCenterZ - visibleRadius);

        for (int pixelX = 0; pixelX < MAP_SIZE; pixelX++) {

            for (int pixelZ = 0; pixelZ < MAP_SIZE; pixelZ++) {

                double blocksPerPixel =
                        (visibleRadius * 2.0) / MAP_SIZE;

                int worldX =
                        (int)(startX + pixelX * blocksPerPixel);

                int worldZ =
                        (int)(startZ + pixelZ * blocksPerPixel);

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
        if (this.client != null && this.client.player != null) {
            playerX = this.client.player.getBlockX();
            playerZ = this.client.player.getBlockZ();
        }
        int bounty =
                BountyManager.getBounty(
                        this.client.player.getUuid()
                );


        double credits =
                CreditsManager.getCredits(
                        this.client.player.getUuid()
                );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(
                        "Bounty: CR " + bounty
                ),
                this.width / 2,
                this.height - 70,
                0xf4fa3c

        );
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(
                        String.format(
                                "Credits: CR %.2f",
                                credits
                        )
                ),
                this.width / 2,
                this.height - 58,
                0x00FF00
        );



        /*
         * DON'T call renderBackground().
         *
         * This is what removes the vanilla background blur.
         */

        // Just use a solid dark background.

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
        double visibleRadius =
                MAP_RADIUS / zoom;

        double blocksToPixelsX =
                mapWidth / (visibleRadius * 2.0);

        double blocksToPixelsZ =
                mapHeight / (visibleRadius * 2.0);

        int centerX =
                (int)(
                        mapLeft
                                + mapWidth / 2.0
                                + (playerX - mapCenterX)
                                * blocksToPixelsX
                );

        int centerY =
                (int)(
                        mapTop
                                + mapHeight / 2.0
                                + (playerZ - mapCenterZ)
                                * blocksToPixelsZ
                );

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
                                + "   |   "
                                + MAP_RADIUS
                                + " block radius"
                ),
                this.width / 2,
                this.height - 40,
                0xFFFFFF
        );

        /*
         * North indicator.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("N"),
                mapLeft + mapWidth / 2  ,
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
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {

        if (verticalAmount > 0) {
            zoom *= 1.2;
        }

        if (verticalAmount < 0) {
            zoom /= 1.2;
        }

        zoom = Math.max(0.25, Math.min(zoom, 20.0));

        generateMap();

        return true;
    }
    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button == 0) {

            dragging = true;

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            return true;
        }



        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }
    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        dragging = false;

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }
    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {

        if (dragging) {

            double blocksPerScreenPixel =
                    (MAP_RADIUS * 2.0)
                            / mapWidth
                            / zoom;

            mapCenterX -=
                    deltaX * blocksPerScreenPixel;

            mapCenterZ -=
                    deltaY * blocksPerScreenPixel;

            generateMap();

            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                deltaX,
                deltaY
        );
    }
    @Override
    public boolean shouldPause() {
        return false;
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}


