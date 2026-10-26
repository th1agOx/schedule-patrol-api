package br.com.schedule.infrastructure.persistence.adapter;

import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.infrastructure.persistence.mapper.RondaMapper;
import br.com.schedule.infrastructure.persistence.repository.RondaMongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RondaRepositoryAdapter implements RondaRepository {

    private final RondaMongoRepository mongoRepository;

    public RondaRepositoryAdapter(RondaMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Ronda salvar(Ronda ronda) {
        return RondaMapper.toDomain(mongoRepository.save(RondaMapper.toDocument(ronda)));
    }

    @Override
    public Optional<Ronda> buscarPorId(String id) {
        return mongoRepository.findById(id).map(RondaMapper::toDomain);
    }

    @Override
    public List<Ronda> listarTodas() {
        return mongoRepository.findAll().stream().map(RondaMapper::toDomain).toList();
    }

    @Override
    public List<Ronda> listarPorAtiva(boolean ativa) {
        return mongoRepository.findByAtiva(ativa).stream().map(RondaMapper::toDomain).toList();
    }

    @Override
    public void excluir(String id) {
        mongoRepository.deleteById(id);
    }
}
