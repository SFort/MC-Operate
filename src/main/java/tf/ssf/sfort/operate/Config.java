package tf.ssf.sfort.operate;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;


public class Config {
	public static Logger LOGGER = LogManager.getLogger();
	public static Boolean jolt = true;
	public static Boolean gunpowder = true;
	public static Boolean obsDispenser = true;
	public static Boolean dispenseGunpowder = true;
	public static boolean litSpoon = true;
	public static Boolean fancyInv = false;

	public static void load() {
		File confFile = new File(
				FabricLoader.getInstance().getConfigDir().toString(),
				"Operate.conf"
		);
		try {
			confFile.createNewFile();
			List<String> la = Files.readAllLines(confFile.toPath());
			List<String> defaultDesc = Arrays.asList(
					"^-Dispensers use gunpowder  [on] on | obsidian | none",
					"^-Obsidian Dispenser  [on] on | off | unregistered",
					"^-Block Placer /Jolt  [on] on | off | unregistered",
					"^-Placeable Gunpowder [on] on | off | unregistered",
					"^-Spoon can turn on Lamps (blocks with LIT property) [true] true | false",
					//TODO maybe not send sync packet when disabled only on client
					"^-Fancy Block inventory render  [on] on | off | examine"
					);
			String[] ls = la.toArray(new String[Math.max(la.size(), defaultDesc.size() * 2) | 1]);
			final int hash = Arrays.hashCode(ls);
			for (int i = 0; i<defaultDesc.size();++i)
				ls[i*2+1]= defaultDesc.get(i);

			try{dispenseGunpowder=ls[0].contains("none")? null : !ls[0].contains("obsidian");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#0\n" + e);}
			ls[0]=dispenseGunpowder==null?"none": dispenseGunpowder?"on":"obsidian";
			try{obsDispenser=ls[2].contains("unregistered")? null : !ls[2].contains("off");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#2\n" + e);}
			ls[2]=obsDispenser==null?"unregistered": obsDispenser?"on":"off";
			try{jolt=ls[4].contains("unregistered")? null : !ls[4].contains("off");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#4\n" + e);}
			ls[4]=jolt==null?"unregistered": jolt?"on":"off";
			try{gunpowder=ls[6].contains("unregistered")? null : !ls[6].contains("off");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#6\n" + e);}
			ls[6]=gunpowder==null?"unregistered": gunpowder?"on":"off";
			try{litSpoon=!ls[8].contains("false");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#8\n" + e);}
			ls[8]=litSpoon?"true":"false";
			try{fancyInv=ls[10].contains("off")? null : !ls[10].contains("examine");}catch (Exception e){LOGGER.log(Level.DEBUG, "tf.ssf.sfort.operate config#10\n" + e);}
			ls[10]=fancyInv==null?"off": fancyInv?"on":"examine";

			if (hash != Arrays.hashCode(ls))
				Files.write(confFile.toPath(), Arrays.asList(ls));
			LOGGER.log(Level.INFO, "tf.ssf.sfort.operate successfully loaded config file");
		} catch (Exception e) {
			LOGGER.log(Level.ERROR, "tf.ssf.sfort.operate failed to load config file, using defaults\n" + e);
		}
	}
}