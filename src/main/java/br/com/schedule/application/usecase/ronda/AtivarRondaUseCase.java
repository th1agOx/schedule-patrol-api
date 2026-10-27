package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;

import java.util.Optional;

public final class AtivarRondaUseCase {

    private final RondaRepository rondaRepository;

    public AtivarRondaUseCase(RondaRepository rondaRepository) {
        this.rondaRepository = rondaRepository;
    }

    public RondaResponse executar(String id) {
        Ronda ronda = buscar(id);
        ronda.ativar();
        return RondaResponse.from(rondaRepository.salvar(ronda));
    }

    private Ronda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(id));
    }
}
