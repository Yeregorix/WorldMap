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

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.world.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ConfigSerializable
public class WorldMapConfig {
	public static final TypeToken<WorldMapConfig> TOKEN = TypeToken.of(WorldMapConfig.class);

	@Setting(value = "Global", comment = "The default configuration used when no dimension matches")
	public String global = "overworld";
	@Setting(value = "Dimensions", comment = "The default configuration by dimension when no world name matches")
	public Map<DimensionType, String> dimensions = new HashMap<>();
	@Setting(value = "Worlds", comment = "The configurations associated to specific world names")
	public Map<String, String> worlds = new HashMap<>();

	public <T> WorldMap<T> load(Function<String, T> loader) {
		Map<String, T> cache = new HashMap<>();

		T global = cache.computeIfAbsent(this.global, loader);

		ImmutableMap.Builder<DimensionType, T> dimensions = ImmutableMap.builder();
		for (Map.Entry<DimensionType, String> e : this.dimensions.entrySet()) {
			T value = cache.computeIfAbsent(e.getValue(), loader);
			if (value != null)
				dimensions.put(e.getKey(), value);
		}

		ImmutableMap.Builder<String, T> worlds = ImmutableMap.builder();
		for (Map.Entry<String, String> e : this.worlds.entrySet()) {
			T value = cache.computeIfAbsent(e.getValue(), loader);
			if (value != null)
				worlds.put(e.getKey(), value);
		}

		return new WorldMap<>(global, dimensions.build(), worlds.build());
	}
}
