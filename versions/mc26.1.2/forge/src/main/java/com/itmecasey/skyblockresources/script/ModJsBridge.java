package com.itmecasey.skyblockresources.script;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Gives ModJS scripts a global {@code SkyblockResources}. ModJS is optional, so this goes through reflection: ModJS offers
 * {@code ExtraBindings.register(BindingProvider)}, whose {@code install(type, cx, scope)} adds globals to every script scope.
 * Without ModJS nothing is loaded and nothing fails. Each install makes a fresh {@link SkyblockResourcesApi}; the one it replaces
 * for the same script type drops its listeners, so reloading scripts does not stack handlers.
 */
public final class ModJsBridge {
	private static final Logger LOG = Logger.getLogger("skyblock_resources");
	private static final Map<String, SkyblockResourcesApi> SESSIONS = new ConcurrentHashMap<>();

	private ModJsBridge() {
	}

	/** Installs the binding when ModJS is on the classpath; returns whether it did. */
	public static boolean install() {
		try {
			Class<?> provider = Class.forName("com.itmecasey.modjs.script.BindingProvider");
			Class<?> extra = Class.forName("com.itmecasey.modjs.script.ExtraBindings");
			Class<?> context = Class.forName("org.mozilla.javascript.Context");
			Class<?> scriptable = Class.forName("org.mozilla.javascript.Scriptable");
			Class<?> scriptableObject = Class.forName("org.mozilla.javascript.ScriptableObject");
			Method javaToJs = context.getMethod("javaToJS", Object.class, scriptable);
			Method put = scriptableObject.getMethod("putProperty", scriptable, String.class, Object.class);
			InvocationHandler handler = (proxy, method, args) -> {
				switch (method.getName()) {
					case "install" -> {
						if (args != null && args.length == 3) {
							SkyblockResourcesApi api = new SkyblockResourcesApi();
							SkyblockResourcesApi old = SESSIONS.put(String.valueOf(args[0]), api);
							if (old != null) {
								old.clear();
							}
							put.invoke(null, args[2], "SkyblockResources", javaToJs.invoke(null, api, args[2]));
						}
						return null;
					}
					case "toString" -> {
						return "SkyblockResources binding";
					}
					case "hashCode" -> {
						return System.identityHashCode(proxy);
					}
					case "equals" -> {
						return proxy == args[0];
					}
					default -> {
						return null;
					}
				}
			};
			Object binding = Proxy.newProxyInstance(provider.getClassLoader(), new Class<?>[] {provider}, handler);
			extra.getMethod("register", provider).invoke(null, binding);
			LOG.info("Skyblock Resources: ModJS scripts can use the SkyblockResources global");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
			LOG.warning("Skyblock Resources: could not add the SkyblockResources global to ModJS: " + e);
			return false;
		}
	}
}
