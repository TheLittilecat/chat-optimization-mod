package minecraft.chat.optimization.mod.client;

import minecraft.chat.optimization.mod.compat.PlatformImpl;
import minecraft.chat.optimization.mod.core.ChatReplacerConfig;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ChatReplacerConfig.load();
		
		PlatformImpl platform = new PlatformImpl();
		platform.registerChatHook();
		platform.registerCommands();
	}
}
