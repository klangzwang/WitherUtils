package geni.witherutils.client.events;

import geni.witherutils.client.gui.console.DevConsoleOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class KeybindMappingEvents {

	private static KeyMapping devReload;
	private static KeyMapping toggleConsole;
	private static KeyMapping devKeyOne;
	private static KeyMapping devKeyTwo;
	public static boolean isConsoleActive = false;

	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
	{
		if (!FMLEnvironment.production) {
			toggleConsole = new KeyMapping("keybind.witherutils.toggle_console", GLFW.GLFW_KEY_GRAVE_ACCENT, "WitherUtils Hotkeys");
			event.register(toggleConsole);

			devReload = new KeyMapping("keybind.witherutils.dev_reload", GLFW.GLFW_KEY_F1, "WitherUtils Hotkeys");
			event.register(devReload);

			devKeyOne = new KeyMapping("keybind.witherutils.devkey_one", GLFW.GLFW_KEY_K, "WitherUtils Hotkeys");
			event.register(devKeyOne);

			devKeyTwo = new KeyMapping("keybind.witherutils.devkey_two", GLFW.GLFW_KEY_L, "WitherUtils Hotkeys");
			event.register(devKeyTwo);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.Key event)
	{
		if (FMLEnvironment.production) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null)
			return;

		if(toggleConsole.consumeClick()) {
            isConsoleActive = !isConsoleActive;
			DevConsoleOverlay.setVisible(isConsoleActive);
		}

		if(devKeyOne.consumeClick())
		{
		}

		if(devKeyTwo.consumeClick())
		{
		}
	}
}