package br.com.schedule.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ponto de inspeção dentro de uma ronda")
public record PontoDTO(
        @Schema(description = "Código do ponto, único dentro da ronda",
                example = "PT-01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String codigo,

        @Schema(description = "Descrição do ponto",
                example = "Válvula de bloqueio",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String descricao) {
}
