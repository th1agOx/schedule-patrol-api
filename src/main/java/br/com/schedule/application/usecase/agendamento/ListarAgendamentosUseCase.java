package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.query.ListarAgendamentosQuery;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;

import java.util.List;

public final class ListarAgendamentosUseCase {

    private final AgendamentoRondaRepository agendamentoRepository;

    public ListarAgendamentosUseCase(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<AgendamentoRondaResponse> executar(ListarAgendamentosQuery query) {
        return agendamentoRepository.listar(query.status(), query.responsavel(), query.idRonda()).stream()
                .map(AgendamentoRondaResponse::from)
                .toList();
    }
}
