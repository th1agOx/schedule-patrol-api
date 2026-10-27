package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;

import java.util.Optional;

public final class BuscarAgendamentoPorIdUseCase {

    private final AgendamentoRondaRepository agendamentoRepository;

    public BuscarAgendamentoPorIdUseCase(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public AgendamentoRondaResponse executar(String id) {
        return AgendamentoRondaResponse.from(buscar(id));
    }

    private AgendamentoRonda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(agendamentoRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.agendamento(id));
    }
}
