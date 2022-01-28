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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ConfigSerializable
public class WorldMapConfig {
	@Comment("The default configuration used when no type matches")
	@Setting("Global")
	public String global = "overworld";

	@Comment("The default configuration by type when no name matches")
	@Setting("Types")
	public Map<ResourceKey, String> types = new HashMap<>();

	@Comment("The configurations associated to specific world names")
	@Setting("Worlds")
	public Map<ResourceKey, String> worlds = new HashMap<>();

	public <T> WorldMap<T> load(Function<String, T> loader) {
		Registry<WorldType> worldTypeRegistry = RegistryTypes.WORLD_TYPE.get();
		Map<String, T> cache = new HashMap<>();

		T global = cache.computeIfAbsent(this.global, loader);

		ImmutableMap.Builder<WorldType, T> types = ImmutableMap.builder();
		for (Map.Entry<ResourceKey, String> e : this.types.entrySet()) {
			T value = cache.computeIfAbsent(e.getValue(), loader);
			if (value != null)
				types.put(worldTypeRegistry.value(e.getKey()), value);
		}

		ImmutableMap.Builder<ResourceKey, T> worlds = ImmutableMap.builder();
		for (Map.Entry<ResourceKey, String> e : this.worlds.entrySet()) {
			T value = cache.computeIfAbsent(e.getValue(), loader);
			if (value != null)
				worlds.put(e.getKey(), value);
		}

		return new WorldMap<>(global, types.build(), worlds.build());
	}
}
