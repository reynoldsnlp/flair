/*
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/.
 */
package com.flair.server.resources;

import java.io.InputStream;
import java.nio.file.Paths;

import com.flair.server.utilities.ServerLogger;

/**
 * Tiny wrapper class for loading bundled resource files
 * 
 * @author shadeMe
 */
public final class ResourceLoader
{
	public static InputStream get(String fileName) {
		//update file path as necessary
		return ResourceLoader.class.getResourceAsStream("/com/flair/server/resources/" + fileName);
	}
}
