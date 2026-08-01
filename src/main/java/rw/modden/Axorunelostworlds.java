package rw.modden;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.commands.BattleCommands;
import rw.modden.commands.CharacterCommands;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Axorunelostworlds implements ModInitializer {

	private static File getConfigFile() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		return configDir.resolve("ar:lw.json").toFile();
	}

	File file = getConfigFile();
	Path configDir = FabricLoader.getInstance().getConfigDir();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
 	private static final Map<String, UUID> uuid = new HashMap<>();
	public static final String MOD_ID = "axorunelostworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public String getUniqueItemID() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Gods Axo- worlds!");
        try {
            Files.createDirectories(configDir.resolve("arlwEvents"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

		CharacterCommands.initialize();
        BattleCommands.initialize();
	}

	public void writeToJson(String name, UUID Uuid) {
		try (FileWriter writer = new FileWriter(file)) {
			if (file.exists()) {
				Map<String, UUID> newUUID = readJson();
				if (!newUUID.containsKey(name)) {
					uuid.put(name, Uuid);
					gson.toJson(uuid, writer);
				} else
					LOGGER.error("In this file have written this user before");
			} else {
				Axorunelostworlds config = new Axorunelostworlds();
				config.createFile();
			}
		} catch (Exception e) {
			LOGGER.error("rw.modden.Axorunelostworlds.writeToJson");
		}
	}

	public UUID readFromJson(String name) {
		UUID value = null;
		try {
			Map<String, UUID> newUUID = readJson();
			if (newUUID.containsKey(name))
				value = newUUID.get(name);
			else
				LOGGER.error("In this file doesn`t have written this user before");
		} catch (Exception e) {
			LOGGER.error("rw.modden.Axorunelostworlds.readFromJson");
		}
		return value;
	}

	public boolean checkJson(String name) {
		Map<String, UUID> newUUID = readJson();
		return newUUID.containsKey(name);
	}

	private Map<String, UUID> readJson() {
		Map<String, UUID> newUUID = null;
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				newUUID = gson.fromJson(reader, Map.class);
			} catch (IOException e) {
				e.printStackTrace();
				Axorunelostworlds config = new Axorunelostworlds();
				config.createFile();
			}
		}
		else
			LOGGER.error("This file doesn`t exist");;
		return newUUID;
	}

	private void createFile() {
		File file = getConfigFile();
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(this, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}