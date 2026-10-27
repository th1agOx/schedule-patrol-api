package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;

import java.util.Optional;

public final class ExcluirRondaUseCase {

    private final RondaRepository rondaRepository;
    private final PoliticaDeExclusaoDeRonda politicaDeExclusao;

    public ExcluirRondaUseCase(RondaRepository rondaRepository, PoliticaDeExclusaoDeRonda politicaDeExclusao) {
        this.rondaRepository = rondaRepository;
        this.politicaDeExclusao = politicaDeExclusao;
    }

    public void executar(String id) {
        Ronda ronda = buscar(id);
        politicaDeExclusao.verificarExclusao(ronda.getId());
        rondaRepository.excluir(ronda.getId());
    }

    private Ronda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(id));
    }
}
