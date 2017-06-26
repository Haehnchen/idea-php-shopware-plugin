package de.espend.idea.license.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ApplicationImplementation {
    void validatedLicense();
    void invalidedLicense();
    void persistToken(@NotNull String token);
    boolean isLicensed();
    @Nullable
    String loadToken();
}
