package br.com.schedule.application.response;

import br.com.schedule.domain.model.Periodo;

import java.time.Instant;

public record PeriodoResponse(Instant inicio, Instant fim) {

    public static PeriodoResponse from(Periodo periodo) {
        return new PeriodoResponse(periodo.inicio(), periodo.fim());
    }
}
