package br.com.schedule.application.dto.request;

import br.com.schedule.application.dto.PeriodoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para agendar a execução de uma ronda")
public record AgendamentoRequestDTO(
        @Schema(description = "Id da ronda. É o UUID devolvido por POST /rondas (ou listado em GET /rondas); não é um id novo.",
                example = "891ec468-649c-4d2a-9752-de32e904a7ba",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idRonda,

        @Schema(description = "Responsável pela execução da ronda",
                example = "operador-01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String responsavel,

        @Schema(description = "Período previsto da execução",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PeriodoDTO periodoPrevisto) {
}
