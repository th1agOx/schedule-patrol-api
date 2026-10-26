package br.com.schedule.domain.repository;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRondaRepository {

    AgendamentoRonda salvar(AgendamentoRonda agendamento);

    Optional<AgendamentoRonda> buscarPorId(String id);

    List<AgendamentoRonda> listar(StatusAgendamento status, String responsavel, String idRonda);

    List<AgendamentoRonda> buscarAtivosPorResponsavel(String responsavel);

    boolean existeAtivoParaRonda(String idRonda);
}
