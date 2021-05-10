/*
 * Copyright (c) 2021 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.map;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class WorldMapLoader<T> {
	protected final Logger logger;
	protected final Path configsDir;
	protected final T fallback;
	private final ConfigurationLoader<? extends ConfigurationNode> mapLoader;

	public WorldMapLoader(Logger logger, ConfigurationLoader<? extends ConfigurationNode> mapLoader, Path configsDir, T fallback) {
		this.logger = logger;
		this.mapLoader = mapLoader;
		this.configsDir = configsDir.toAbsolutePath();
		this.fallback = fallback;
	}

	public WorldMap<T> load() {
		return load(map -> {});
	}

	protected WorldMap<T> load(Consumer<WorldMapConfig> preprocessor) {
		try {
			Files.createDirectories(this.configsDir);
		} catch (IOException e) {
			this.logger.error("Failed to create directories: " + this.configsDir, e);
			return globalFallback();
		}

		try {
			ConfigurationNode root = this.mapLoader.load();
			WorldMapConfig map = root.getValue(WorldMapConfig.TOKEN);

			boolean init = map == null;
			if (init)
				map = new WorldMapConfig();
			preprocessor.accept(map);
			if (init)
				initMap(map);

			root.setValue(WorldMapConfig.TOKEN, map);
			this.mapLoader.save(root);

			return map.load(this::loadConfig);
		} catch (Exception e) {
			this.logger.error("Failed to load configurations map", e);
			return globalFallback();
		}
	}

	protected WorldMap<T> globalFallback() {
		return new WorldMap<>(this.fallback);
	}

	protected void initMap(WorldMapConfig map) {}

	protected T loadConfig(String name) {
		if (name == null || name.isEmpty()) {
			this.logger.warn("Config name cannot be empty.");
			return this.fallback;
		}

		Path file = this.configsDir.resolve(name + ".conf").toAbsolutePath();
		if (!file.startsWith(this.configsDir)) {
			this.logger.warn("Invalid config location: " + file);
			return this.fallback;
		}

		this.logger.info("Loading configuration " + name + " ...");
		try {
			return loadConfig(file);
		} catch (Exception e) {
			this.logger.error("Failed to load configuration " + name, e);
			return this.fallback;
		}
	}

	protected abstract T loadConfig(Path file) throws Exception;

	public WorldMap<T> importWorlds(Path worlds) {
		return load(map -> {
			try (DirectoryStream<Path> st = Files.newDirectoryStream(worlds)) {
				for (Path file : st) {
					String fn = file.getFileName().toString();
					if (!fn.endsWith(".conf"))
						continue;

					// Detect main worlds
					String name = fn.substring(0, fn.length() - 5);
					DimensionType dim;
					switch (name) {
						case "world":
							dim = DimensionTypes.OVERWORLD;
							break;
						case "DIM-1":
							dim = DimensionTypes.NETHER;
							break;
						case "DIM1":
							dim = DimensionTypes.THE_END;
							break;
						default:
							dim = null;
							break;
					}

					// Set default parameters when possible
					if (dim != null) {
						String dimId = getShortId(dim);
						Path dimDest = this.configsDir.resolve(dimId + ".conf");
						if (Files.notExists(dimDest)) {
							if (dimId.equals(map.global)) {
								Files.copy(file, dimDest);
								continue;
							} else if (!map.dimensions.containsKey(dim)) {
								Files.copy(file, dimDest);
								map.dimensions.put(dim, dimId);
								continue;
							}
						}
					}

					// Set world
					Path worldDest = this.configsDir.resolve(name + ".conf");
					if (Files.notExists(worldDest) && !map.worlds.containsKey(name)) {
						Files.copy(file, worldDest);
						map.worlds.put(name, name);
					}
				}
			} catch (Exception e) {
				this.logger.warn("Failed to import worlds", e);
			}
		});
	}

	public static String getShortId(DimensionType dim) {
		return dim.getId().substring(dim.getId().indexOf(':') + 1);
	}
}
