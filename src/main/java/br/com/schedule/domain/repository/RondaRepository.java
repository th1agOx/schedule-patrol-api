package br.com.schedule.domain.repository;

import br.com.schedule.domain.model.Ronda;

import java.util.List;
import java.util.Optional;

public interface RondaRepository {

    Ronda salvar(Ronda ronda);

    Optional<Ronda> buscarPorId(String id);

    List<Ronda> listarTodas();

    List<Ronda> listarPorAtiva(boolean ativa);

    void excluir(String id);
}
