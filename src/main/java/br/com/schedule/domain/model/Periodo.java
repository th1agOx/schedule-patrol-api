package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.PeriodoInvalidoException;

import java.time.Instant;


public record Periodo(Instant inicio, Instant fim) {

    public Periodo {
        if (inicio == null || fim == null) {
            throw new PeriodoInvalidoException("O período deve ter início e fim informados.");
        }
        if (!inicio.isBefore(fim)) {
            throw new PeriodoInvalidoException("O início do período deve ser anterior ao fim.");
        }
    }

    public boolean sobrepoe(Periodo outro) {
        if (outro == null) {
            throw new PeriodoInvalidoException("O período de comparação deve ser informado.");
        }
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
