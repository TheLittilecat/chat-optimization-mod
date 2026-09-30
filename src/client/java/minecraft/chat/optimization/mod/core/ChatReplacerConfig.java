package minecraft.chat.optimization.mod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ChatReplacerConfig {

	public static final int MAX_RULES = 10;
	public static final String DEFAULT_PLAN = "猫娘";

	public static ChatReplacerConfig INSTANCE = new ChatReplacerConfig();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("minecraft-chat-optimization-mod.json");

	public String activePlan;
	public Map<String, Plan> plans;

	public ChatReplacerConfig() {
		
		this.activePlan = DEFAULT_PLAN;
		this.plans = new LinkedHashMap<>();
		Plan catgirl = new Plan(DEFAULT_PLAN);
		catgirl.rules.add(new ReplaceRule("我", "本喵"));
		catgirl.rules.add(new ReplaceRule("你", "主人"));
		catgirl.suffixes.add("喵~");
		this.plans.put(DEFAULT_PLAN, catgirl);
	}

	
	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				JsonElement root = new JsonParser().parse(reader);
				ChatReplacerConfig parsed = GSON.fromJson(root, ChatReplacerConfig.class);
				if (parsed != null) {
					
					if (parsed.plans == null || parsed.plans.isEmpty()) {
						ChatReplacerConfig migrated = new ChatReplacerConfig();
						if (root.isJsonObject()) {
							LegacyConfig legacy = GSON.fromJson(root.getAsJsonObject(), LegacyConfig.class);
							if (legacy.replaceRules != null && !legacy.replaceRules.isEmpty()) {
								migrated.plans.get(DEFAULT_PLAN).rules = legacy.replaceRules;
							}
							if (legacy.appendSuffix != null && !legacy.appendSuffix.isEmpty()) {
								migrated.plans.get(DEFAULT_PLAN).suffixes = new ArrayList<>(Arrays.asList(legacy.appendSuffix));
							}
						}
						parsed = migrated;
					}
					INSTANCE = parsed;
				}
			} catch (Exception e) {
				
				System.err.println("[聊天替换] 配置加载失败，使用默认配置: " + e);
				INSTANCE = new ChatReplacerConfig();
			}
		} else {
			INSTANCE = new ChatReplacerConfig();
		}
		INSTANCE.sanitize();
		INSTANCE.save();
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			System.err.println("[聊天替换] 配置保存失败: " + e);
		}
	}

	private void sanitize() {
		if (plans == null) {
			plans = new LinkedHashMap<>();
		}
		plans.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null);
		for (Plan plan : plans.values()) {
			if (plan.rules == null) {
				plan.rules = new ArrayList<>();
			}
			plan.rules.removeIf(rule -> rule == null || rule.from == null || rule.from.isEmpty());
			if (plan.rules.size() > MAX_RULES) {
				plan.rules = new ArrayList<>(plan.rules.subList(0, MAX_RULES));
			}
			if (plan.suffixes == null) {
				plan.suffixes = new ArrayList<>();
			}
			plan.suffixes.removeIf(suffix -> suffix == null || suffix.isEmpty());
		}
		if (plans.isEmpty()) {
			plans = new ChatReplacerConfig().plans;
		}
		for (Map.Entry<String, Plan> entry : plans.entrySet()) {
			entry.getValue().name = entry.getKey();
		}
		if (activePlan == null || !plans.containsKey(activePlan)) {
			activePlan = plans.keySet().iterator().next();
		}
	}

	
	public Plan getActivePlan() {
		return plans == null ? null : plans.get(activePlan);
	}

	public boolean setActivePlan(String name) {
		if (name == null || !plans.containsKey(name)) {
			return false;
		}
		activePlan = name;
		return true;
	}

	public boolean addPlan(String name) {
		if (name == null || name.isEmpty() || plans.containsKey(name)) {
			return false;
		}
		plans.put(name, new Plan(name));
		return true;
	}

	
	public boolean removePlan(String name) {
		if (name == null || !plans.containsKey(name) || plans.size() <= 1) {
			return false;
		}
		plans.remove(name);
		if (name.equals(activePlan)) {
			activePlan = plans.keySet().iterator().next();
		}
		return true;
	}

	public static class Plan {
		public String name;
		public List<ReplaceRule> rules;
		public List<String> suffixes;

		public Plan() {
			this.rules = new ArrayList<>();
			this.suffixes = new ArrayList<>();
		}

		public Plan(String name) {
			this();
			this.name = name;
		}
	}

	public static class ReplaceRule {
		public String from;
		public String to;

		public ReplaceRule() {
		}

		public ReplaceRule(String from, String to) {
			this.from = from;
			this.to = to;
		}
	}

	
	private static class LegacyConfig {
		public List<ReplaceRule> replaceRules;
		public String appendSuffix;
	}
}
