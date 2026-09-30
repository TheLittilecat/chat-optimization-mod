package minecraft.chat.optimization.mod.compat;

import minecraft.chat.optimization.mod.core.CommandFeedback;
import minecraft.chat.optimization.mod.core.TlcCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.LiteralText;


public final class TlcCommandDispatcher {

	private static final Object SOURCE = new Object();
	private static final CommandDispatcher<Object> DISPATCHER = new CommandDispatcher<Object>();

	private TlcCommandDispatcher() {
	}

	public static void register() {
		DISPATCHER.register(TlcCommand.build(TlcCommandDispatcher::adapt));
	}

	
	public static boolean handle(String input) {
		try {
			DISPATCHER.execute(input.startsWith("/") ? input.substring(1) : input, SOURCE);
		} catch (CommandSyntaxException e) {
			feedback("[TheLittle_cat]:" + e.getMessage());
		} catch (RuntimeException e) {
			feedback("[TheLittle_cat]:" + e);
		}
		return true;
	}

	private static CommandFeedback adapt(Object source) {
		return new CommandFeedback() {
			@Override
			public void feedback(String message) {
				TlcCommandDispatcher.feedback(message);
			}

			@Override
			public void error(String message) {
				TlcCommandDispatcher.feedback(message);
			}
		};
	}

	private static void feedback(String message) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) {
			return;
		}
		ChatHud chatHud = client.inGameHud == null ? null : client.inGameHud.getChatHud();
		if (chatHud == null) {
			return;
		}
		chatHud.addMessage(new LiteralText(message));
	}
}
