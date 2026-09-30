package minecraft.chat.optimization.mod.core;

import minecraft.chat.optimization.mod.core.ChatReplacerConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class ChatReplacer {

	private ChatReplacer() {
	}

	
	public static String apply(ChatReplacerConfig config, String message) {
		if (message == null || message.isEmpty()) {
			return message;
		}
		ChatReplacerConfig.Plan plan = config.getActivePlan();
		if (plan == null) {
			return message;
		}
		String result = message;
		Map<String, List<ChatReplacerConfig.ReplaceRule>> byFrom = new LinkedHashMap<>();
		for (ChatReplacerConfig.ReplaceRule rule : plan.rules) {
			if (rule.from == null || rule.from.isEmpty()) {
				continue;
			}
			byFrom.computeIfAbsent(rule.from, k -> new ArrayList<>()).add(rule);
		}
		for (List<ChatReplacerConfig.ReplaceRule> group : byFrom.values()) {
			ChatReplacerConfig.ReplaceRule chosen = group.get(ThreadLocalRandom.current().nextInt(group.size()));
			if (chosen.to != null) {
				result = result.replace(chosen.from, chosen.to);
			}
		}
		if (plan.suffixes != null && !plan.suffixes.isEmpty()) {
			String suffix = plan.suffixes.get(ThreadLocalRandom.current().nextInt(plan.suffixes.size()));
			result = result + suffix;
		}
		return result;
	}
}
