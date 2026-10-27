package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.command.CriarRondaCommand;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;

public final class CriarRondaUseCase {

    private final RondaRepository rondaRepository;

    public CriarRondaUseCase(RondaRepository rondaRepository) {
        this.rondaRepository = rondaRepository;
    }

    public RondaResponse executar(CriarRondaCommand command) {
        Ronda ronda = Ronda.criar(command.nome(), command.descricao(), command.toPontos());
        return RondaResponse.from(rondaRepository.salvar(ronda));
    }
}
