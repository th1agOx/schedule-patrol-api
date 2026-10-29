package br.com.schedule.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Motivo do cancelamento do agendamento")
public record CancelamentoRequestDTO(
        @Schema(description = "Motivo do cancelamento; é obrigatório e fica registrado no histórico",
                example = "Ronda reagendada a pedido do cliente",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String motivo) {
}
