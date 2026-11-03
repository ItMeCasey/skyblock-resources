package com.itmecasey.skyblockresources.procedures;

import net.minecraft.core.Direction;

public class TextToDirectionProcedure {
	public static Direction execute(String text) {
		if (text == null)
			return Direction.NORTH;
		return switch (text) {
			case "DOWN" -> Direction.DOWN;
			case "NORTH" -> Direction.NORTH;
			case "SOUTH" -> Direction.SOUTH;
			case "EAST" -> Direction.EAST;
			case "WEST" -> Direction.WEST;
			default -> Direction.UP;
		};
	}
}
