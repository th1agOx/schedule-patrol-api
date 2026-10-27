package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;

import java.util.Optional;

public final class ConcluirAgendamentoUseCase {

    private final AgendamentoRondaRepository agendamentoRepository;

    public ConcluirAgendamentoUseCase(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public AgendamentoRondaResponse executar(String id) {
        AgendamentoRonda agendamento = buscar(id);
        agendamento.concluir();
        return AgendamentoRondaResponse.from(agendamentoRepository.salvar(agendamento));
    }

    private AgendamentoRonda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(agendamentoRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.agendamento(id));
    }
}
