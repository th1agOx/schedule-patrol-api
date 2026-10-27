# schedule-patrol-api
API de agendamento de rondas para gasodutos desenvolvida como Prova de Conceito (POC). O sistema utiliza exclusivamente dados fictícios e anonimizados, respeitando as leis de sigilo asseguradas pela LGPD.

## Regras de negócio

### Ronda desativada

Desativar uma ronda impede apenas a criação de **novos agendamentos**. Agendamentos já existentes continuam válidos e podem ser iniciados, concluídos, cancelados ou reagendados normalmente.

A verificação de ronda ativa ocorre exclusivamente no caso de uso de agendamento (`AgendarRondaUseCase`), por meio de `Ronda.podeSerAgendada()`.
