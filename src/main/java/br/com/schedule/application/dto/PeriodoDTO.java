package br.com.schedule.application.dto;

import java.time.Instant;

public record PeriodoDTO(Instant inicio, Instant fim) {
}
