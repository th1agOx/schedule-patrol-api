package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.infrastructure.persistence.document.PontoDocument;
import br.com.schedule.infrastructure.persistence.document.RondaDocument;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository 
public class RondaMongoRepository implements RondaRepository {

    private final MongoTemplate mongoTemplate;

    public RondaMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

// Contrato do domain

    @Override
    public Ronda salvar(Ronda ronda) {

        RondaDocument salvo = mongoTemplate.save(paraDocumento(ronda));
        return paraDominio(salvo);
    } 

        @Override
    public Optional<Ronda> buscarPorId(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, RondaDocument.class))
                .map(this::paraDominio);
    }
 
    @Override
    public List<Ronda> listarTodas() {
        return mongoTemplate.findAll(RondaDocument.class).stream()
                .map(this::paraDominio)
                .toList();
    }
 
    @Override
    public List<Ronda> listarPorAtiva(boolean ativa) {
        Query query = Query.query(Criteria.where("ativa").is(ativa));
 
        return mongoTemplate.find(query, RondaDocument.class).stream()
                .map(this::paraDominio)
                .toList();
    }
 
    @Override
    public void excluir(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, RondaDocument.class);
    }

// Traductions domain <-> document ( Antigamente no mapper )

    private RondaDocument paraDocumento(Ronda ronda) {
        Objects.requireNonNull(ronda, "A ronda é obrigatória para o mapeamento.");
 
        List<PontoDocument> pontos = ronda.getPontos().stream()
                .map(ponto -> new PontoDocument(ponto.codigo(), ponto.descricao()))
                .toList();
 
        return new RondaDocument(ronda.getId(), ronda.getNome(), ronda.getDescricao(), ronda.isAtiva(), pontos);
    }
 
    private Ronda paraDominio(RondaDocument document) {
        Objects.requireNonNull(document, "O documento da ronda é obrigatório para o mapeamento.");
 
        List<Ponto> pontos = document.getPontos() == null
                ? List.of()
                : document.getPontos().stream().map(this::paraDominio).toList();
 
               return Ronda.reconstituir(document.getId(), document.getNome(), document.getDescricao(),
                document.isAtiva(), pontos);
    }
 
    private Ponto paraDominio(PontoDocument document) {
        if (document == null) {
            throw new IllegalStateException("A lista de pontos do documento contém um ponto nulo.");
        }

        return new Ponto(document.getCodigo(), document.getDescricao());
    }
}