package geni.witherutils.client.gui.console;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;

public class DevConsoleOverlay {
    private static boolean visible = false;
    private static final List<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 6;

    public static void setVisible(boolean value) {
        visible = !value;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void log(String message) {
        logs.add(message);
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (!visible) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int overlayHeight = 70;
        int x1 = 0;
        int y1 = screenHeight - overlayHeight;
        int x2 = screenWidth;
        int y2 = screenHeight;

        // Schwarzer, leicht transparenter Hintergrund (0xCC000000 = ~80% Alpha)
        guiGraphics.fill(x1, y1, x2, y2, 0xCC000000);

        // Grüne Trennlinie am oberen Rand des Kastens
        guiGraphics.fill(x1, y1, x2, y1 + 1, 0xFF00FF00);

        // Header / Titel
        guiGraphics.drawString(mc.font, "--- Dev Console ---", 5, y1 + 4, 0x00FF00);

        // Textausgabe der Logs
        int textY = y1 + 16;
        for (int i = 0; i < logs.size(); i++) {
            guiGraphics.drawString(mc.font, "> " + logs.get(i), 5, textY + (i * 9), 0xFFFFFF);
        }
    }
}