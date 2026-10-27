package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.PeriodoCommand;
import br.com.schedule.application.command.ReagendarRondaCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;

import java.time.Clock;
import java.util.Optional;

public final class ReagendarRondaUseCase {

    private final AgendamentoRondaRepository agendamentoRepository;
    private final VerificadorDeConflitoDeAgenda verificadorDeConflito;
    private final Clock clock;

    public ReagendarRondaUseCase(AgendamentoRondaRepository agendamentoRepository,
            VerificadorDeConflitoDeAgenda verificadorDeConflito,
            Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.verificadorDeConflito = verificadorDeConflito;
        this.clock = clock;
    }

    public AgendamentoRondaResponse executar(String id, ReagendarRondaCommand command) {
        AgendamentoRonda agendamento = buscar(id);
        Periodo periodo = toPeriodo(command.periodoPrevisto());

        agendamento.reagendar(periodo, clock.instant());
        verificadorDeConflito.verificar(agendamento.getResponsavel(), agendamento.getPeriodoPrevisto(),
                agendamento.getId());

        return AgendamentoRondaResponse.from(agendamentoRepository.salvar(agendamento));
    }

    private AgendamentoRonda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(agendamentoRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.agendamento(id));
    }

    private static Periodo toPeriodo(PeriodoCommand command) {
        return command == null ? null : command.toDomain();
    }
}
