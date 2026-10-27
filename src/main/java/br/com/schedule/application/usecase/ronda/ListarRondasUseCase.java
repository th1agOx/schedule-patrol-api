package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.query.ListarRondasQuery;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;

import java.util.List;

public final class ListarRondasUseCase {

    private final RondaRepository rondaRepository;

    public ListarRondasUseCase(RondaRepository rondaRepository) {
        this.rondaRepository = rondaRepository;
    }

    public List<RondaResponse> executar(ListarRondasQuery query) {
        List<Ronda> rondas = query.ativa() == null
                ? rondaRepository.listarTodas()
                : rondaRepository.listarPorAtiva(query.ativa());
        return rondas.stream().map(RondaResponse::from).toList();
    }
}
