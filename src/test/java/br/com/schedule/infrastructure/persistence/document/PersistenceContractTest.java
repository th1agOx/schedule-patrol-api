package br.com.schedule.infrastructure.persistence.document;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceContractTest {

    @Test
    void shouldDeclareExpectedCollections() {
        Document rondas = RondaDocument.class.getAnnotation(Document.class);
        Document agendamentos = AgendamentoRondaDocument.class.getAnnotation(Document.class);

        assertThat(rondas).isNotNull();
        assertThat(rondas.collection()).isEqualTo("rondas");
        assertThat(agendamentos).isNotNull();
        assertThat(agendamentos.collection()).isEqualTo("agendamentos");
    }

    @Test
    void shouldDeclareExactRondaFields() throws NoSuchFieldException {
        assertThat(declaredFieldNames(RondaDocument.class))
                .containsExactlyInAnyOrder("id", "nome", "descricao", "ativa", "pontos");
        assertThat(RondaDocument.class.getDeclaredField("id").isAnnotationPresent(Id.class)).isTrue();
        assertThat(RondaDocument.class.getDeclaredField("id").isAnnotationPresent(Field.class)).isFalse();

        assertFieldName(RondaDocument.class, "nome", "nome");
        assertFieldName(RondaDocument.class, "descricao", "descricao");
        assertFieldName(RondaDocument.class, "ativa", "ativa");
        assertFieldName(RondaDocument.class, "pontos", "pontos");
    }

    @Test
    void shouldDeclareExactAgendamentoFields() throws NoSuchFieldException {
        assertThat(declaredFieldNames(AgendamentoRondaDocument.class))
                .containsExactlyInAnyOrder("id", "idRonda", "responsavel", "periodoPrevisto", "status",
                        "motivoCancelamento");
        assertThat(AgendamentoRondaDocument.class.getDeclaredField("id").isAnnotationPresent(Id.class)).isTrue();
        assertThat(AgendamentoRondaDocument.class.getDeclaredField("id").isAnnotationPresent(Field.class)).isFalse();

        assertFieldName(AgendamentoRondaDocument.class, "idRonda", "idRonda");
        assertFieldName(AgendamentoRondaDocument.class, "responsavel", "responsavel");
        assertFieldName(AgendamentoRondaDocument.class, "periodoPrevisto", "periodoPrevisto");
        assertFieldName(AgendamentoRondaDocument.class, "status", "status");
        assertFieldName(AgendamentoRondaDocument.class, "motivoCancelamento", "motivoCancelamento");
        assertThat(AgendamentoRondaDocument.class.getDeclaredField("status").getType()).isEqualTo(String.class);
    }

    @Test
    void shouldDeclareEmbeddedDocumentFields() throws NoSuchFieldException {
        assertThat(declaredFieldNames(PontoDocument.class)).containsExactlyInAnyOrder("codigo", "descricao");
        assertFieldName(PontoDocument.class, "codigo", "codigo");
        assertFieldName(PontoDocument.class, "descricao", "descricao");

        assertThat(declaredFieldNames(PeriodoDocument.class)).containsExactlyInAnyOrder("inicio", "fim");
        assertFieldName(PeriodoDocument.class, "inicio", "inicio");
        assertFieldName(PeriodoDocument.class, "fim", "fim");
        assertThat(PeriodoDocument.class.getDeclaredField("inicio").getType()).isEqualTo(Instant.class);
        assertThat(PeriodoDocument.class.getDeclaredField("fim").getType()).isEqualTo(Instant.class);
    }

    @Test
    void shouldNotUseIndexAnnotations() {
        List<Class<?>> documents = List.of(RondaDocument.class, PontoDocument.class,
                AgendamentoRondaDocument.class, PeriodoDocument.class);

        for (Class<?> document : documents) {
            assertThat(document.isAnnotationPresent(Indexed.class))
                    .as("@Indexed não deve ser usado em %s", document.getSimpleName()).isFalse();
            assertThat(document.isAnnotationPresent(CompoundIndex.class))
                    .as("@CompoundIndex não deve ser usado em %s", document.getSimpleName()).isFalse();

            for (java.lang.reflect.Field field : document.getDeclaredFields()) {
                assertThat(field.isAnnotationPresent(Indexed.class))
                        .as("@Indexed não deve ser usado em %s.%s", document.getSimpleName(), field.getName())
                        .isFalse();
                assertThat(field.isAnnotationPresent(CompoundIndex.class))
                        .as("@CompoundIndex não deve ser usado em %s.%s", document.getSimpleName(), field.getName())
                        .isFalse();
            }
        }
    }

    @Test
    void shouldMapIdToUnderscoreIdAndKeepIdRondaAsRegularField() {
        MongoMappingContext context = new MongoMappingContext();
        context.setSimpleTypeHolder(new MongoCustomConversions(List.of()).getSimpleTypeHolder());

        MongoPersistentEntity<?> ronda = context.getRequiredPersistentEntity(RondaDocument.class);
        assertThat(ronda.getCollection()).isEqualTo("rondas");
        assertThat(ronda.getPersistentProperty("id").getFieldName()).isEqualTo("_id");
        assertThat(ronda.getPersistentProperty("id").isIdProperty()).isTrue();

        MongoPersistentEntity<?> agendamento = context.getRequiredPersistentEntity(AgendamentoRondaDocument.class);
        assertThat(agendamento.getCollection()).isEqualTo("agendamentos");
        assertThat(agendamento.getPersistentProperty("id").getFieldName()).isEqualTo("_id");
        assertThat(agendamento.getPersistentProperty("idRonda").getFieldName()).isEqualTo("idRonda");
        assertThat(agendamento.getPersistentProperty("idRonda").isIdProperty()).isFalse();
    }

    private static List<String> declaredFieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(java.lang.reflect.Field::getName).toList();
    }

    private static void assertFieldName(Class<?> type, String property, String expectedFieldName)
            throws NoSuchFieldException {
        Field annotation = type.getDeclaredField(property).getAnnotation(Field.class);
        assertThat(annotation).as("@Field ausente em %s.%s", type.getSimpleName(), property).isNotNull();
        assertThat(annotation.value()).isEqualTo(expectedFieldName);
    }
}
