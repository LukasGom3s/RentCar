# Histórias de Usuário - Sistema de Aluguel de Carros

## 1. Autenticação e Cadastro

**US01 - Autenticação no Sistema**
* **Como** usuário do sistema (Cliente ou Agente)
* **Quero** me autenticar com usuário e senha
* **Para que** eu possa acessar de forma segura as minhas funcionalidades restritas.
* **Critérios de Aceitação:** 
  * O sistema só pode ser utilizado após cadastro prévio.
  * O acesso só é permitido após a validação das credenciais.

**US02 - Registro de Cliente**
* **Como** Cliente
* **Quero** registrar os meus dados de identificação (RG, CPF, Nome, Endereço), profissão e até três entidades empregadoras com os respectivos rendimentos auferidos
* **Para que** eu tenha um perfil válido no sistema para realizar pedidos de aluguel.
* **Critérios de Aceitação:** 
  * O sistema deve validar os dados de identificação.
  * O sistema deve limitar o registro a um máximo de três (3) rendimentos/empregadores.

## 2. Gestão de Pedidos (Cliente)

**US03 - Introduzir Pedido de Aluguel**
* **Como** Cliente
* **Quero** introduzir um pedido de aluguel de um automóvel no sistema
* **Para que** a minha solicitação seja enviada para análise financeira dos agentes.
* **Critérios de Aceitação:** 
  * O pedido deve estar associado a um automóvel registrado (matrícula, ano, marca, modelo e placa).
  * O status inicial do pedido deve ser "Em análise" (ou similar).

**US04 - Consultar Pedido de Aluguel**
* **Como** Cliente
* **Quero** consultar os pedidos de aluguel que introduzi
* **Para que** eu possa acompanhar o status e o parecer da avaliação financeira.

**US05 - Modificar Pedido de Aluguel (Cliente)**
* **Como** Cliente
* **Quero** modificar os detalhes do meu pedido de aluguel
* **Para que** eu possa corrigir informações ou alterar o veículo pretendido antes da execução do contrato.
* **Critérios de Aceitação:** 
  * Só é possível modificar pedidos que ainda não foram executados ou finalizados.

**US06 - Cancelar Pedido de Aluguel**
* **Como** Cliente
* **Quero** cancelar o meu pedido de aluguel
* **Para que** o processo seja interrompido sem gerar obrigações contratuais.
* **Critérios de Aceitação:** 
  * O status do pedido deve mudar para "Cancelado".

## 3. Análise e Contratos (Agente)

**US07 - Avaliar Pedido (Parecer Financeiro)**
* **Como** Agente (Empresa ou Banco)
* **Quero** avaliar os pedidos de aluguel introduzidos do ponto de vista financeiro
* **Para que** eu possa emitir um parecer positivo ou recusar a solicitação com base nos rendimentos do cliente.
* **Critérios de Aceitação:** 
  * O sistema deve registrar o resultado do parecer (Aprovado/Reprovado).

**US08 - Modificar Pedido (Agente)**
* **Como** Agente (Empresa ou Banco)
* **Quero** modificar os detalhes do pedido de aluguel em análise
* **Para que** eu possa ajustar condições financeiras ou associar um contrato de crédito gerido pelo banco.

**US09 - Executar Contrato**
* **Como** Agente
* **Quero** colocar o contrato em consideração para execução após um parecer financeiro positivo
* **Para que** o aluguel do automóvel seja formalizado de acordo com a propriedade registrada (cliente, empresa ou banco).
* **Critérios de Aceitação:** 
  * O contrato deve vincular o automóvel, o cliente e as condições estabelecidas (como contrato de crédito).