package minecraft.chat.optimization.mod;

import minecraft.chat.optimization.mod.compat.Platform;
import minecraft.chat.optimization.mod.compat.PlatformImpl;
import minecraft.chat.optimization.mod.core.ChatReplacerConfig;
import net.fabricmc.api.ClientModInitializer;


public final class ChatReplacerMod implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ChatReplacerConfig.load();

		Platform platform = new PlatformImpl();
		platform.registerCommands();
		platform.registerChatHook();
	}
}
