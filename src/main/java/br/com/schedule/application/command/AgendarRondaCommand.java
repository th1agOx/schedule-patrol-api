package br.com.schedule.application.command;

public record AgendarRondaCommand(String idRonda, String responsavel, PeriodoCommand periodoPrevisto) {
}
