package br.com.schedule.application.dto.request;

import br.com.schedule.application.dto.PontoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// criar e atualizar uma ronda os campos são os mesmos
@Schema(description = "Dados para criar ou atualizar uma ronda")
public record RondaRequestDTO(
        @Schema(description = "Nome da ronda", example = "Ronda Trecho Norte",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @Schema(description = "Descrição da ronda", example = "Inspeção semanal do trecho norte")
        String descricao,

        @Schema(description = "Pontos de inspeção, na ordem de visita (mínimo 1)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<PontoDTO> pontos) {
}
