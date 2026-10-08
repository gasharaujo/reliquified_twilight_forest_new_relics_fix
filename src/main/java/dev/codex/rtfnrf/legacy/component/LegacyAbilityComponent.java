package dev.codex.rtfnrf.legacy.component;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import java.util.Objects;

public final class LegacyAbilityComponent {
    private final AbilityComponent delegate;

    public LegacyAbilityComponent(AbilityComponent delegate) {
        this.delegate = delegate == null ? AbilityComponent.EMPTY : delegate;
    }

    public AbilityComponent unwrap() { return delegate; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyAbilityComponent component && delegate.equals(component.delegate);
    }

    @Override
    public int hashCode() { return Objects.hash(delegate); }
}

