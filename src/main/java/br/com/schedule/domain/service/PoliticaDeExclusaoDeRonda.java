package br.com.schedule.domain.service;

import br.com.schedule.domain.exception.RondaComAgendamentosAtivosException;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;


public class PoliticaDeExclusaoDeRonda {

    private final AgendamentoRondaRepository agendamentoRepository;

    public PoliticaDeExclusaoDeRonda(AgendamentoRondaRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public void verificarExclusao(String idRonda) {
        if (agendamentoRepository.existeAtivoParaRonda(idRonda)) {
            throw new RondaComAgendamentosAtivosException(idRonda);
        }
    }
}
