package de.espend.idea.license;

import org.jetbrains.annotations.Nullable;

public class LicenseResponse {
    private boolean success = false;

    @Nullable
    private String token;

    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}
