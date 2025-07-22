package drive_only.drive_only_server.security;

import drive_only.drive_only_server.domain.ProviderType;

public class CustomUserPrincipal {
    private final String email;
    private final ProviderType provider;

    public CustomUserPrincipal(String email, ProviderType provider) {
        this.email = email;
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public ProviderType getProvider() {
        return provider;
    }
}
