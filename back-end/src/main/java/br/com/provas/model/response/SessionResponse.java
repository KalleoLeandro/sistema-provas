package br.com.provas.model.response;

import java.time.Instant;

public record SessionResponse(Instant expiresAt, boolean refreshAllowed) {}
