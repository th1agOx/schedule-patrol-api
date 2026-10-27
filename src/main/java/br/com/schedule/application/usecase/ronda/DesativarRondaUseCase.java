package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;

import java.util.Optional;

public final class DesativarRondaUseCase {

    private final RondaRepository rondaRepository;

    public DesativarRondaUseCase(RondaRepository rondaRepository) {
        this.rondaRepository = rondaRepository;
    }

    public RondaResponse executar(String id) {
        Ronda ronda = buscar(id);
        ronda.desativar();
        return RondaResponse.from(rondaRepository.salvar(ronda));
    }

    private Ronda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(id));
    }
}
