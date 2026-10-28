package br.com.schedule.application.dto.response;

import br.com.schedule.application.dto.PeriodoDTO;

public record AgendamentoResponseDTO(String id, String idRonda, String responsavel,
        PeriodoDTO periodoPrevisto, String status, String motivoCancelamento) {
}
