package br.com.schedule.application.command;

import br.com.schedule.domain.model.Periodo;

import java.time.Instant;

public record PeriodoCommand(Instant inicio, Instant fim) {

    public Periodo toDomain() {
        return new Periodo(inicio, fim);
    }
}
