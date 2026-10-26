package br.com.schedule.domain.service;

import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;

public class VerificadorDeConflitoDeAgenda {

    private final AgendamentoRondaRepository agendamentoRepository;

    public VerificadorDeConflitoDeAgenda(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public void verificar(String responsavel, Periodo periodo) {
        verificar(responsavel, periodo, null);
    }

    public void verificar(String responsavel, Periodo periodo, String idAgendamentoIgnorado) {
        boolean conflito = agendamentoRepository.buscarAtivosPorResponsavel(responsavel).stream()
                .filter(agendamento -> idAgendamentoIgnorado == null || !agendamento.getId().equals(idAgendamentoIgnorado))
                .anyMatch(agendamento -> sobrepoe(agendamento, periodo));

        if (conflito) {
            throw new ConflitoDeAgendaException(responsavel, periodo);
        }
    }

    private boolean sobrepoe(AgendamentoRonda agendamento, Periodo periodo) {
        return agendamento.getPeriodoPrevisto().sobrepoe(periodo);
    }
}
