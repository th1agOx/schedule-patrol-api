package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.CancelarAgendamentoCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;

import java.util.Optional;

public final class CancelarAgendamentoUseCase {

    private final AgendamentoRondaRepository agendamentoRepository;

    public CancelarAgendamentoUseCase(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public AgendamentoRondaResponse executar(String id, CancelarAgendamentoCommand command) {
        AgendamentoRonda agendamento = buscar(id);
        agendamento.cancelar(command.motivo());
        return AgendamentoRondaResponse.from(agendamentoRepository.salvar(agendamento));
    }

    private AgendamentoRonda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(agendamentoRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.agendamento(id));
    }
}
