package dev.codex.rtfnrf.legacy.component;

import it.hurts.sskirillss.relics.api.relics.LevelingComponent;
import java.util.Objects;

public final class LegacyLevelingComponent {
    private final LevelingComponent delegate;

    public LegacyLevelingComponent(LevelingComponent delegate) {
        this.delegate = delegate == null ? LevelingComponent.EMPTY : delegate;
    }

    public LevelingComponent unwrap() { return delegate; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyLevelingComponent component && delegate.equals(component.delegate);
    }

    @Override
    public int hashCode() { return Objects.hash(delegate); }
}

