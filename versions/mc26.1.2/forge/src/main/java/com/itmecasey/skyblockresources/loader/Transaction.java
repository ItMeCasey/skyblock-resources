package com.itmecasey.skyblockresources.loader;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A rollback scope for {@link ResourceHandler} calls, shaped like NeoForge's Transaction: open it, move things,
 * {@link #commit()} to keep the result, and leave the try block without committing to undo everything.
 *
 * <p>Forge's handlers have no transactions (they take a simulate flag), so the handlers in this package change the
 * real inventory straight away and each registers an undo step here the first time it is touched. Closing an
 * uncommitted transaction runs the undo steps, newest first. A nested transaction that commits hands its undo steps
 * to its parent, so the parent can still roll them back.
 */
public final class Transaction implements AutoCloseable {
	@Nullable
	private final Transaction parent;
	private final List<Runnable> undo = new ArrayList<>();
	private final Map<Object, Boolean> touched = new IdentityHashMap<>();
	private boolean committed;
	private boolean closed;

	private Transaction(@Nullable Transaction parent) {
		this.parent = parent;
	}

	public static Transaction openRoot() {
		return new Transaction(null);
	}

	public static Transaction open(@Nullable Transaction parent) {
		return new Transaction(parent);
	}

	/** Called by a handler before it changes anything: the first call per handler asks for its undo step. */
	void touch(Object handler, Supplier<Runnable> snapshot) {
		if (touched.put(handler, Boolean.TRUE) == null) {
			undo.add(snapshot.get());
		}
	}

	/** Called by a handler after a change it cannot snapshot as a whole (fluids and energy): how to take it back. */
	void onRollback(Runnable step) {
		undo.add(step);
	}

	public void commit() {
		committed = true;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		if (committed) {
			if (parent != null) {
				parent.undo.addAll(undo);
			}
		} else {
			for (int i = undo.size() - 1; i >= 0; i--) {
				undo.get(i).run();
			}
		}
		undo.clear();
	}
}
