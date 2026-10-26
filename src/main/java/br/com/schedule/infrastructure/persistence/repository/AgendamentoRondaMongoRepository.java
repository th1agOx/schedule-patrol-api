package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface AgendamentoRondaMongoRepository extends MongoRepository<AgendamentoRondaDocument, String> {

    List<AgendamentoRondaDocument> findByResponsavelAndStatusIn(String responsavel, Collection<String> status);

    boolean existsByIdRondaAndStatusIn(String idRonda, Collection<String> status);
}
