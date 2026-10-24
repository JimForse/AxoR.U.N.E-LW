package rw.modden;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rw.modden.characters.CharacterInitializer;
import rw.modden.commands.BattleCommands;
import rw.modden.commands.CharacterCommands;
import rw.modden.network.ServerNetwork;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Axorunelostworlds implements ModInitializer {
	private static final Path configDir = FabricLoader.getInstance().getConfigDir();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
 	private static final Map<String, UUID> uuid = new HashMap<>();
	public static final String MOD_ID = "axorunelostworlds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ArrayList<String> permitted_equipment = new ArrayList<>();

	public static String getUniqueItemID() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Server on initialized");
        try {
			readFromEquipmentsJson();
            Files.createDirectories(configDir.resolve("arlwEvents"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

		new CharacterInitializer().getOrCreate();
		CharacterCommands.initialize();
        BattleCommands.initialize();
		ServerNetwork.registerGlobalRecevier();
	}

	@SuppressWarnings("unchecked")
	private static void readFromEquipmentsJson() {
		ArrayList<String> list = new ArrayList<>();
		File fileW = configDir.resolve("equipments.json").toFile();
		if (fileW.exists()) {
			try (FileReader reader = new FileReader(fileW)) {
				list = gson.fromJson(reader, ArrayList.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			LOGGER.error("This file doesn`t exist");
		permitted_equipment = list;
	}
}