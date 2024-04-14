/*
 * Copyright (c) 2021-2022 Hugo Dupanloup (Yeregorix)
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

import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
		try {
			Files.createDirectories(this.configsDir);
		} catch (IOException e) {
			this.logger.error("Failed to create directories: " + this.configsDir, e);
			return globalFallback();
		}

		try {
			ConfigurationNode root = this.mapLoader.load();
			WorldMapConfig map = root.get(WorldMapConfig.class);

			boolean init = map == null;
			if (init) {
				map = new WorldMapConfig();
				initMap(map);
			}

			root.set(map);
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
}
