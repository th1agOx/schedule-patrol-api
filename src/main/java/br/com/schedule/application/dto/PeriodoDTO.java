package br.com.schedule.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Intervalo de tempo em ISO-8601. Aceita offset (ex.: -03:00) ou Z (UTC). Z = UTC: 13:00 em Brasília é 16:00Z.")
public record PeriodoDTO(
        @Schema(description = "Início do período. Precisa estar estritamente no futuro.",
                example = "2026-12-01T08:00:00-03:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Instant inicio,

        @Schema(description = "Fim do período. Precisa ser depois do início.",
                example = "2026-12-01T12:00:00-03:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Instant fim) {
}
