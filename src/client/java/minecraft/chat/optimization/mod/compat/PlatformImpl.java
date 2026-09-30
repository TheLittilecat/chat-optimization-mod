package minecraft.chat.optimization.mod.compat;

import minecraft.chat.optimization.mod.core.ChatReplacer;
import minecraft.chat.optimization.mod.core.ChatReplacerConfig;
import minecraft.chat.optimization.mod.core.CommandFeedback;
import minecraft.chat.optimization.mod.core.TlcCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.text.Text;


public final class PlatformImpl implements Platform {

	@Override
	public void registerChatHook() {
		ClientSendMessageEvents.MODIFY_CHAT.register(
				message -> ChatReplacer.apply(ChatReplacerConfig.INSTANCE, message));
	}

	@Override
	public void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(TlcCommand.build(PlatformImpl::adapt)));
	}

	private static CommandFeedback adapt(FabricClientCommandSource source) {
		return new CommandFeedback() {
			@Override
			public void feedback(String message) {
				source.sendFeedback(Text.literal(message));
			}

			@Override
			public void error(String message) {
				source.sendError(Text.literal(message));
			}
		};
	}
}
