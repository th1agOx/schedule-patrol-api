package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.AgendarRondaCommand;
import br.com.schedule.application.command.PeriodoCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;

import java.time.Clock;
import java.util.Optional;

public final class AgendarRondaUseCase {

    private final RondaRepository rondaRepository;
    private final AgendamentoRondaRepository agendamentoRepository;
    private final VerificadorDeConflitoDeAgenda verificadorDeConflito;
    private final Clock clock;

    public AgendarRondaUseCase(RondaRepository rondaRepository,
            AgendamentoRondaRepository agendamentoRepository,
            VerificadorDeConflitoDeAgenda verificadorDeConflito,
            Clock clock) {
        this.rondaRepository = rondaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.verificadorDeConflito = verificadorDeConflito;
        this.clock = clock;
    }

    public AgendamentoRondaResponse executar(AgendarRondaCommand command) {
        Ronda ronda = Optional.ofNullable(command.idRonda())
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(command.idRonda()));

        Periodo periodo = toPeriodo(command.periodoPrevisto());
        AgendamentoRonda agendamento = AgendamentoRonda.agendar(
                ronda, command.responsavel(), periodo, clock.instant());

        verificadorDeConflito.verificar(agendamento.getResponsavel(), agendamento.getPeriodoPrevisto());

        return AgendamentoRondaResponse.from(agendamentoRepository.salvar(agendamento));
    }

    private static Periodo toPeriodo(PeriodoCommand command) {
        return command == null ? null : command.toDomain();
    }
}
