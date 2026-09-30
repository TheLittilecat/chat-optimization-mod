package minecraft.chat.optimization.mod.compat;

import net.minecraft.text.LiteralText;


public final class PlatformImpl implements Platform {

	@Override
	public void registerChatHook() {
		
	}

	@Override
	public void registerCommands() {
		TlcCommandDispatcher.register();
	}

	
	static void echo(String message) {
		net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
		if (client != null && client.inGameHud != null) {
			client.inGameHud.getChatHud().addMessage(new LiteralText(message));
		}
	}
}
