package com.itmecasey.skyblockresources.devtest;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

/** Dev-only JEI plugin that keeps the runtime, so the tour can open JEI's recipe screen. */
@JeiPlugin
public class JeiCapture implements IModPlugin {
	static volatile IJeiRuntime runtime;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath("skyblock_resources", "devtest_capture");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
	}
}
