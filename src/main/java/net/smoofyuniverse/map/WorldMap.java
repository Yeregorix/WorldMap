/*
 * Copyright (c) 2021-2024 Hugo Dupanloup (Yeregorix)
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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.Collections;
import java.util.Map;

public class WorldMap<T> {
	private final T global;
	private final Map<WorldType, T> types;
	private final Map<ResourceKey, T> worlds;

	public WorldMap(T global) {
		this(global, Collections.emptyMap(), Collections.emptyMap());
	}

	public WorldMap(T global, Map<WorldType, T> types, Map<ResourceKey, T> worlds) {
		if (global == null)
			throw new IllegalArgumentException("global");
		if (types == null)
			throw new IllegalArgumentException("types");
		if (worlds == null)
			throw new IllegalArgumentException("worlds");

		this.global = global;
		this.types = types;
		this.worlds = worlds;
	}

	public T get(ServerWorldProperties properties) {
		T config = this.worlds.get(properties.key());
		if (config != null)
			return config;

		config = this.types.get(properties.worldType());
		if (config != null)
			return config;

		return this.global;
	}
}
