package geni.witherutils.client.render.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class SatisfactoryLightRenderer {

    // RGB-Farbdefinitionen angelehnt an Satisfactory Machine Lights
    public static final int COLOR_OFF = 0x404040;      // Aus / Kein Strom (Dunkelgrau)
    public static final int COLOR_POWER_TRIP = 0xFF0000; // Rot (Überlastet / Kein Strom)
    public static final int COLOR_STANDBY = 0xFFCC00;    // Gelb (Wartet auf Input / Ausgang voll)
    public static final int COLOR_RUNNING = 0x00FF66;    // Grün (Normalbetrieb)
    public static final int COLOR_OVERCLOCKED = 0x00FFFF;// Cyan/Blau (Übertaktet)

    /**
     * Rendert ein kleines Satisfactory-Indicator-Light-Panel an einer bestimmten Position relativ zum Block.
     */
    public static void renderIndicatorLight(PoseStack poseStack, MultiBufferSource bufferSource,
                                          Supplier<Long> gameTimeSupplier, float partialTick,
                                          int rgbColor, boolean blinking,
                                          float x, float y, float z, 
                                          float sizeX, float sizeY) {
        if (gameTimeSupplier.get() == null) {
            return;
        }

        // Wenn blinkend (z.B. bei Fehler / Power Trip), Alpha-Wert pulsieren lassen
        float alpha = 1.0f;
        if (blinking) {
            long time = gameTimeSupplier.get();
            alpha = 0.3f + 0.7f * (float) Math.sin((time + partialTick) * 0.3f);
        }

        // Emissiver RenderType, damit das Licht unabhängig vom Umgebungslicht leuchtet
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix4f = poseStack.last().pose();

        int red = (rgbColor >> 16) & 0xFF;
        int green = (rgbColor >> 8) & 0xFF;
        int blue = rgbColor & 0xFF;
        int intAlpha = (int) (Math.clamp(alpha, 0.0f, 1.0f) * 255);

        // Kleine Plane / Leuchtanzeige zeichnen
        float x2 = x + sizeX;
        float y2 = y + sizeY;

        // Front-Facing Quad (Beispiel auf der Z-Achse, anpassbar je nach Ausrichtung)
        builder.addVertex(matrix4f, x, y2, z).setColor(red, green, blue, intAlpha);
        builder.addVertex(matrix4f, x2, y2, z).setColor(red, green, blue, intAlpha);
        builder.addVertex(matrix4f, x2, y, z).setColor(red, green, blue, intAlpha);
        builder.addVertex(matrix4f, x, y, z).setColor(red, green, blue, intAlpha);
    }

    /**
     * Hilfsmethode: Ermittelt den Zustand und die Farbe flexibel aus BlockState-Properties oder der BlockEntity.
     */
    public static int resolveSatisfactoryColor(BlockState state, BooleanProperty litProperty, 
                                               Supplier<Boolean> hasPower, Supplier<Boolean> isWorking) {
        // 1. Prüfen ob Strom vorhanden ist (falls nicht -> Rot)
        if (hasPower != null && !hasPower.get()) {
            return COLOR_POWER_TRIP;
        }

        // 2. Prüfen ob BlockProperty 'lit' oder 'working' aktiv ist
        boolean working = (litProperty != null && state.hasProperty(litProperty)) 
                ? state.getValue(litProperty) 
                : (isWorking != null && isWorking.get());

        if (!working) {
            return COLOR_STANDBY; // Gelb (Standby / Keine Items)
        }

        return COLOR_RUNNING; // Grün (Läuft normal)
    }
}