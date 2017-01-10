package powercrystals.minefactoryreloaded.world;

import static powercrystals.minefactoryreloaded.setup.MFRThings.*;

import cofh.api.world.IFeatureGenerator;
import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.setup.MFRConfig;

public class MineFactoryReloadedWorldGen implements IFeatureGenerator
{
	private static List<Integer> _blacklistedDimensions;
	private static List<String> _sludgeBiomeList, _sewageBiomeList, _rubberTreeBiomeList;
	private static boolean _sludgeLakeMode, _sewageLakeMode, _rubberTreesEnabled;
	private static boolean _lakesEnabled;
	private static boolean _regenSewage, _regenSludge, _regenTrees;
	private static int _sludgeLakeRarity, _sewageLakeRarity;

	public static boolean generateMegaRubberTree(World world, Random random, BlockPos pos, boolean safe)
	{
		return new WorldGenMassiveTree(false).setTreeScale(4 + (random.nextInt(3)), 0.8f, 0.7f).
				setLeafAttenuation(0.6f).setSloped(true).setSafe(safe).
				generate(world, random, pos);
	}

	public static boolean generateSacredSpringRubberTree(World world, Random random, BlockPos pos)
	{
		return new WorldGenMassiveTree(false).setTreeScale(6 + (random.nextInt(4)), 1f, 0.9f).
				setLeafAttenuation(0.35f).setSloped(false).setMinTrunkSize(4).
				generate(world, random, pos);
	}

	private final String name = "MFR:WorldGen";

	@Override
	public String getFeatureName() {
		return name;
	}


	@Override
	public boolean generateFeature(Random random, int chunkX, int chunkZ, World world, boolean newGen)
	{
		if(_blacklistedDimensions == null)
		{
			buildBlacklistedDimensions();
		}

		if (_blacklistedDimensions.contains(world.provider.getDimension()))
		{
			return false;
		}

		int x = chunkX * 16 + random.nextInt(16);
		int z = chunkZ * 16 + random.nextInt(16);

		BlockPos pos = new BlockPos(x, 1, z);
		Biome b = world.getBiome(pos);
		if (b == null)
			return false;

		String biomeName = b.getBiomeName();

		if (_rubberTreesEnabled & (newGen | _regenTrees))
		{
			if (_rubberTreeBiomeList.contains(biomeName))
			{
				if (random.nextInt(100) < 40)
				{
					if (random.nextInt(30) == 0)
					{
						String ln = biomeName.toLowerCase(Locale.US);
						if (ln.contains("mega"))
							generateMegaRubberTree(world, random, world.getHeight(pos), false);
						else if (ln.contains("sacred") && random.nextInt(20) == 0)
							generateSacredSpringRubberTree(world, random, world.getHeight(pos));
					}
					new WorldGenRubberTree(false).generate(world, random, new BlockPos(x, random.nextInt(3) + 4, z));
				}
			}
		}

		if (_lakesEnabled && world.provider.canRespawnHere())
		{
			int rarity = _sludgeLakeRarity;
			if (rarity > 0 & (newGen | _regenSludge) &&
					_sludgeBiomeList.contains(biomeName) == _sludgeLakeMode &&
					random.nextInt(rarity) == 0)
			{
				int lakeX = x - 8 + random.nextInt(16);
				int lakeY = random.nextInt(world.getActualHeight());
				int lakeZ = z - 8 + random.nextInt(16);
				new WorldGenLakesMeta(sludgeLiquid.getDefaultState()).generate(world, random, new BlockPos(lakeX, lakeY, lakeZ));
			}

			rarity = _sewageLakeRarity;
			if (rarity > 0 & (newGen | _regenSewage) &&
					_sewageBiomeList.contains(biomeName) == _sewageLakeMode &&
					random.nextInt(rarity) == 0)
			{
				int lakeX = x - 8 + random.nextInt(16);
				int lakeY = random.nextInt(world.getActualHeight());
				int lakeZ = z - 8 + random.nextInt(16);
				String ln = biomeName.toLowerCase(Locale.US);
				if (ln.contains("mushroom"))
				{
					new WorldGenLakesMeta(mushroomSoupLiquid.getDefaultState()).generate(world, random, new BlockPos(lakeX, lakeY, lakeZ));
				}
				else
				{
					new WorldGenLakesMeta(sewageLiquid.getDefaultState()).generate(world, random, new BlockPos(lakeX, lakeY, lakeZ));
				}
			}
		}

		return true;
	}

	private static void buildBlacklistedDimensions()
	{
		_blacklistedDimensions = Ints.asList(MFRConfig.worldGenDimensionBlacklist.getIntList());

		_rubberTreeBiomeList = MFRRegistry.getRubberTreeBiomes();
		_rubberTreesEnabled = MFRConfig.rubberTreeWorldGen.getBoolean(true);

		_lakesEnabled = MFRConfig.mfrLakeWorldGen.getBoolean(true);

		_sludgeLakeRarity = MFRConfig.mfrLakeSludgeRarity.getInt();
		_sludgeBiomeList = Arrays.asList(MFRConfig.mfrLakeSludgeBiomeList.getStringList());
		_sludgeLakeMode = MFRConfig.mfrLakeSludgeBiomeListToggle.getBoolean(false);

		_sewageLakeRarity = MFRConfig.mfrLakeSewageRarity.getInt();
		_sewageBiomeList = Arrays.asList(MFRConfig.mfrLakeSewageBiomeList.getStringList());
		_sewageLakeMode = MFRConfig.mfrLakeSewageBiomeListToggle.getBoolean(false);

		_regenSewage = MFRConfig.mfrLakeSewageRetrogen.getBoolean(false);
		_regenSludge = MFRConfig.mfrLakeSludgeRetrogen.getBoolean(false);
		_regenTrees = MFRConfig.rubberTreeRetrogen.getBoolean(false);
	}
}
