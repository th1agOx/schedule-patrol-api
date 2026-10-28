package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.document.PeriodoDocument;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository 
public class AgendamentoRondaMongoRepository implements AgendamentoRondaRepository {

    private static final List<String> STATUS_ATIVOS = Arrays.stream(StatusAgendamento.values())
            .filter(StatusAgendamento::estaAtivo)
            .map(StatusAgendamento::name)
            .toList();

    private final MongoTemplate mongoTemplate;

    public AgendamentoRondaMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AgendamentoRonda salvar(AgendamentoRonda agendamento) {
        AgendamentoRondaDocument salvo = mongoTemplate.save(paraDocumento(agendamento));
        return paraDominio(salvo);
    }

    @Override
    public Optional<AgendamentoRonda> buscarPorId(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, AgendamentoRondaDocument.class))
                .map(this::paraDominio);
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
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public List<AgendamentoRonda> buscarAtivosPorResponsavel(String responsavel) {

        Query query = Query.query(Criteria.where("responsavel").is(responsavel)
            .and("status").in(STATUS_ATIVOS));
 
        return mongoTemplate.find(query, AgendamentoRondaDocument.class).stream()
                .map(this::paraDominio)
                .toList();
    }
 
    @Override
    public boolean existeAtivoParaRonda(String idRonda) {

        Query query = Query.query(Criteria.where("idRonda").is(idRonda)
            .and("status").in(STATUS_ATIVOS));
 
        return mongoTemplate.exists(query, AgendamentoRondaDocument.class);
    }
 
// Tradução 

  private AgendamentoRondaDocument paraDocumento(AgendamentoRonda agendamento) {
        Objects.requireNonNull(agendamento, "O agendamento é obrigatório para o mapeamento.");
 
        Periodo periodo = agendamento.getPeriodoPrevisto();
        PeriodoDocument periodoDocument = new PeriodoDocument(periodo.inicio(), periodo.fim());
 
        return new AgendamentoRondaDocument(agendamento.getId(), agendamento.getIdRonda(),
                agendamento.getResponsavel(), periodoDocument, agendamento.getStatus().name(),
                agendamento.getMotivoCancelamento().orElse(null));
    }
 
    private AgendamentoRonda paraDominio(AgendamentoRondaDocument document) {
        Objects.requireNonNull(document, "O documento do agendamento é obrigatório para o mapeamento.");
 
        PeriodoDocument periodoDocument = document.getPeriodoPrevisto();
        Periodo periodo = periodoDocument == null
                ? null
                : new Periodo(periodoDocument.getInicio(), periodoDocument.getFim());

        return AgendamentoRonda.reconstituir(document.getId(), document.getIdRonda(), document.getResponsavel(),
                periodo, paraStatus(document.getStatus(), document.getId()), document.getMotivoCancelamento());
    }
 
    private StatusAgendamento paraStatus(String status, String idAgendamento) {
        if (status == null || status.isBlank()) {
            throw new IllegalStateException(
                    "O status do agendamento '%s' não foi informado.".formatted(idAgendamento));
        }
        try {
            return StatusAgendamento.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "O status '%s' do agendamento '%s' é inválido.".formatted(status, idAgendamento), exception);
        }
    }
    
}