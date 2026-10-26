package br.com.schedule.infrastructure.persistence.adapter;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.mapper.AgendamentoRondaMapper;
import br.com.schedule.infrastructure.persistence.repository.AgendamentoRondaMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class AgendamentoRondaRepositoryAdapter implements AgendamentoRondaRepository {

    private static final List<String> STATUS_ATIVOS = Arrays.stream(StatusAgendamento.values())
            .filter(StatusAgendamento::estaAtivo)
            .map(StatusAgendamento::name)
            .toList();

    private final AgendamentoRondaMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public AgendamentoRondaRepositoryAdapter(AgendamentoRondaMongoRepository mongoRepository,
            MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AgendamentoRonda salvar(AgendamentoRonda agendamento) {
        return AgendamentoRondaMapper.toDomain(mongoRepository.save(AgendamentoRondaMapper.toDocument(agendamento)));
    }

    @Override
    public Optional<AgendamentoRonda> buscarPorId(String id) {
        return mongoRepository.findById(id).map(AgendamentoRondaMapper::toDomain);
    }

    @Override
    public List<AgendamentoRonda> listar(StatusAgendamento status, String responsavel, String idRonda) {
        Query query = new Query();
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status.name()));
        }
        if (responsavel != null) {
            query.addCriteria(Criteria.where("responsavel").is(responsavel));
        }
        if (idRonda != null) {
            query.addCriteria(Criteria.where("idRonda").is(idRonda));
        }

        return mongoTemplate.find(query, AgendamentoRondaDocument.class).stream()
                .map(AgendamentoRondaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AgendamentoRonda> buscarAtivosPorResponsavel(String responsavel) {
        return mongoRepository.findByResponsavelAndStatusIn(responsavel, STATUS_ATIVOS).stream()
                .map(AgendamentoRondaMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existeAtivoParaRonda(String idRonda) {
        return mongoRepository.existsByIdRondaAndStatusIn(idRonda, STATUS_ATIVOS);
    }
}
