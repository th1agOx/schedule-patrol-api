package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.infrastructure.persistence.document.RondaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RondaMongoRepository extends MongoRepository<RondaDocument, String> {

    List<RondaDocument> findByAtiva(boolean ativa);
}
