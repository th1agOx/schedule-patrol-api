package br.com.schedule.application.dto.request;

import br.com.schedule.application.dto.PeriodoDTO;

public record AgendamentoRequestDTO(String idRonda, String responsavel, PeriodoDTO periodoPrevisto) {
}
