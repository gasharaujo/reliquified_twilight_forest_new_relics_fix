package dev.codex.rtfnrf.legacy.component;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import java.util.Objects;

public final class LegacyAbilitiesComponent {
    private final AbilitiesComponent delegate;

    public LegacyAbilitiesComponent(AbilitiesComponent delegate) {
        this.delegate = delegate == null ? AbilitiesComponent.EMPTY : delegate;
    }

    public AbilitiesComponent unwrap() { return delegate; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyAbilitiesComponent component && delegate.equals(component.delegate);
    }

    @Override
    public int hashCode() { return Objects.hash(delegate); }
}

