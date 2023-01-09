package tf.ssf.sfort.operate;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tf.ssf.sfort.ini.SFIni;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;


public class Config {
	public static Logger LOGGER = LogManager.getLogger();
	public static EnumOnOffUnregistered bit = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered colorTube = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered jolt = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered punch = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered gunpowder = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered obsDispenser = EnumOnOffUnregistered.ON;
	public static EnumOnOffObsidian dispenseGunpowder = EnumOnOffObsidian.ON;
	public static EnumOnOffExamine fancyInv = EnumOnOffExamine.ON;
	public static EnumOnOffUnregistered basicPipe = EnumOnOffUnregistered.ON;
	public static EnumOnOffUnregistered cylinder = EnumOnOffUnregistered.ON;
	public static Boolean advancedPipe = true;
	public static boolean litSpoon = true;
	public static boolean chunkLoadPipes = false;
	//TODO maybe don't send sync packet when disabled only on client
	public static void load() {
		SFIni defIni = new SFIni();
		defIni.load(String.join("\n", new String[]{
				"; Dispensers use gunpowder  [on] on | obsidian | off",
				"dispenseGunpowder=on",
				"; Block inventory render    [on] on | examine | off",
				"renderBlockInv=on",
				"; Obsidian Dispenser        [on] on | off | unregistered",
				"obsidianDispenser=on",
				"; Block Placer              [on] on | off | unregistered",
				"jolt=on",
				"; Placeable Gunpowder       [on] on | off | unregistered",
				"placeableGunpowder=on",
				"; Auto Crafter              [on] on | off | unregistered",
				"punch=on",
				"; ItemStack Computer        [on] on | off | unregistered",
				"bitStak=on",
				"; Color tube                [on] on | off | unregistered",
				"colorTube=on",
				"; Item pipes                [on] on | off | unregistered",
				"itemPipe=on",
				"; Item Cylinder             [on] on | off | unregistered",
				"itemCylinder=on",
				"; Spoon can turn on Lamps (blocks with LIT property) [true] true | false",
				"spoonLit=true",
				"; Chunk load active item pipes [false] true | false",
				"chunkLoadPipes=false",
		}));
		loadLegacyConfig(defIni);
		File confFile = new File(
				FabricLoader.getInstance().getConfigDir().toString(),
				"Operate.sf.ini"
		);
		if (!confFile.exists()) {
			try {
				Files.write(confFile.toPath(), defIni.toString().getBytes());
				LOGGER.log(Level.INFO,"tf.ssf.sfort.operate successfully created config file");
				loadIni(defIni);
			} catch (IOException e) {
				LOGGER.log(Level.ERROR,"tf.ssf.sfort.operate failed to create config file, using defaults", e);
			}
			return;
		}
		try {
			SFIni ini = new SFIni();
			String text = Files.readString(confFile.toPath());
			int hash = text.hashCode();
			ini.load(text);
			for (Map.Entry<String, List<SFIni.Data>> entry : defIni.data.entrySet()) {
				List<SFIni.Data> list = ini.data.get(entry.getKey());
				if (list == null || list.isEmpty()) {
					ini.data.put(entry.getKey(), entry.getValue());
				} else {
					list.get(0).comments = entry.getValue().get(0).comments;
				}
			}
			loadIni(ini);
			String iniStr = ini.toString();
			if (hash != iniStr.hashCode()) {
				Files.write(confFile.toPath(), iniStr.getBytes());
			}
		} catch (IOException e) {
			LOGGER.log(Level.ERROR,"tf.ssf.sfort.operate failed to load config file, using defaults", e);
		}
	}

	public static<E extends Enum<E>> void setOrResetEnum(SFIni ini, String key, Consumer<E> set, E en, Class<E> clazz) {
		try {
			set.accept(ini.getEnum(key, clazz));
		} catch (Exception e) {
			SFIni.Data data = ini.getLastData(key);
			if (data != null) data.val = en.name().toLowerCase(Locale.ROOT);
			LOGGER.log(Level.ERROR,"tf.ssf.sfort.operate failed to load "+key+", setting to default value", e);
		}
	}
	public static void setOrResetBool(SFIni ini, String key, Consumer<Boolean> set, boolean bool) {
		try {
			set.accept(ini.getBoolean(key));
		} catch (Exception e) {
			SFIni.Data data = ini.getLastData(key);
			if (data != null) data.val = Boolean.toString(bool);
			LOGGER.log(Level.ERROR,"tf.ssf.sfort.operate failed to load "+key+", setting to default value", e);
		}
	}
	public static void loadIni(SFIni ini) {
		setOrResetEnum(ini, "dispenseGunpowder", e -> dispenseGunpowder = e, dispenseGunpowder, EnumOnOffObsidian.class);
		setOrResetEnum(ini, "renderBlockInv", e -> fancyInv = e, fancyInv, EnumOnOffExamine.class);
		setOrResetEnum(ini, "renderBlockInv", e -> fancyInv = e, fancyInv, EnumOnOffExamine.class);
		setOrResetEnum(ini, "obsidianDispenser", e -> obsDispenser = e, obsDispenser, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "jolt", e -> jolt = e, jolt, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "placeableGunpowder", e -> gunpowder = e, gunpowder, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "punch", e -> punch = e, punch, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "bitStak", e -> bit = e, bit, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "colorTube", e -> colorTube = e, colorTube, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "itemPipe", e -> basicPipe = e, basicPipe, EnumOnOffUnregistered.class);
		setOrResetEnum(ini, "itemCylinder", e -> cylinder = e, cylinder, EnumOnOffUnregistered.class);

		setOrResetBool(ini, "spoonLit", b-> litSpoon = b, litSpoon);
		setOrResetBool(ini, "chunkLoadPipes", b-> chunkLoadPipes = b, chunkLoadPipes);

		LOGGER.log(Level.INFO,"tf.ssf.sfort.operate finished loaded config file");
	}

	public static void loadLegacyConfig(SFIni inIni) {
		Map<String, String> oldConf = new HashMap<>();
		File confFile = new File(
				FabricLoader.getInstance().getConfigDir().toString(),
				"Operate.conf"
		);
		if (!confFile.exists()) return;
		try {
			List<String> la = Files.readAllLines(confFile.toPath());
			String[] ls = la.toArray(new String[Math.max(la.size(), 24) | 1]);
			List<String> defaultDesc = Arrays.asList(
					"^-Dispensers use gunpowder  [on] on | obsidian | none",
					"^-Obsidian Dispenser  [on] on | off | unregistered",
					"^-Block Placer /Jolt  [on] on | off | unregistered",
					"^-Placeable Gunpowder [on] on | off | unregistered",
					"^-Spoon can turn on Lamps (blocks with LIT property) [true] true | false",
					//TODO maybe not send sync packet when disabled only on client
					"^-Block inventory render  [on] on | off | examine",
					"^-Crafter /Punch      [on] on | off | unregistered",
					"^-Computer /BitStak   [on] on | off | unregistered",
					"^-Color tube          [on] on | off | unregistered",
					//TODO should probably allow each pipe to be toggled separately
					"^-Item pipes          [on] on | off | unregistered",
					"^-Chunk load active item pipes [false] true | false",
					"^-Item Cylinder       [on] on | off | unregistered"
			);

			try{
				oldConf.put("dispenseGunpowder", ls[0].contains("none")? "none" : !ls[0].contains("obsidian") ? "on" : "obsidian");
			}catch (Exception ignore){}
			try{
				oldConf.put("obsidianDispenser", ls[2].contains("unregistered")? "unregistered" : !ls[2].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("jolt", ls[4].contains("unregistered")? "unregistered" : !ls[4].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("placeableGunpowder", ls[6].contains("unregistered")? "unregistered" : !ls[6].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("spoonLit", !ls[8].contains("false") ? "true" : "false");
			}catch (Exception ignore){}
			try{
				oldConf.put("renderBlockInv", ls[10].contains("off")? "off" : !ls[10].contains("examine") ? "on" : "examine");
			}catch (Exception ignore){}
			try{
				oldConf.put("punch", ls[12].contains("unregistered")? "unregistered" : !ls[12].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("bitStak", ls[14].contains("unregistered")? "unregistered" : !ls[14].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("colorTube", ls[16].contains("unregistered")? "unregistered" : !ls[16].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("itemPipe", ls[18].contains("unregistered")? "unregistered" : !ls[18].contains("off") ? "on" : "off");
			}catch (Exception ignore){}
			try{
				oldConf.put("chunkLoadPipes", ls[20].contains("true") ? "true" : "false");
			}catch (Exception ignore){}
			try{
				oldConf.put("itemCylinder", ls[22].contains("unregistered")? "unregistered" : !ls[22].contains("off") ? "on" : "off");
			}catch (Exception ignore){}

			for (Map.Entry<String, String> entry : oldConf.entrySet()) {
				SFIni.Data data = inIni.getLastData(entry.getKey());
				if (data != null) {
					data.val = entry.getValue();
				}
			}

			Files.delete(confFile.toPath());
			LOGGER.log(Level.INFO, "tf.ssf.sfort.operate successfully loaded legacy config file");
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, "tf.ssf.sfort.operate failed to load legacy config file, using defaults\n" + e);
		}
	}
	public enum EnumOnOffUnregistered {
		ON, OFF, UNREGISTERED;
	}
	public enum EnumOnOffExamine {
		ON, EXAMINE, OFF;
	}
	public enum EnumOnOffObsidian {
		ON, OBSIDIAN, OFF;
	}
}