package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.token.AuthToken;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.Instant;

@Getter
public abstract class AbstractAuthToken implements AuthToken {
    private final String tokenValue;
    private final Instant issuedAt;
    private final Instant expiresAt;

    protected AbstractAuthToken(String tokenValue, Instant expiresAt) {
        this(tokenValue, null, expiresAt);
    }

    protected AbstractAuthToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        Assert.hasText(tokenValue, "tokenValue cannot be empty");
        Assert.notNull(expiresAt, "expiresAt cannot be null");
        if(issuedAt != null) {
            Assert.isTrue(expiresAt.isAfter(issuedAt), "expiresAt must be after issuedAt");
        }
        this.tokenValue = tokenValue;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        AbstractAuthToken other = (AbstractAuthToken) obj;
        if (!this.getTokenValue().equals(other.getTokenValue())) {
            return false;
        }
        if ((this.getIssuedAt() != null) ? !this.getIssuedAt().equals(other.getIssuedAt())
                : other.getIssuedAt() != null) {
            return false;
        }
        return (this.getExpiresAt() != null) ? this.getExpiresAt().equals(other.getExpiresAt())
                : other.getExpiresAt() == null;
    }

    @Override
    public int hashCode() {
        int result = this.getTokenValue().hashCode();
        result = 31 * result + ((this.getIssuedAt() != null) ? this.getIssuedAt().hashCode() : 0);
        result = 31 * result + ((this.getExpiresAt() != null) ? this.getExpiresAt().hashCode() : 0);
        return result;
    }
}
