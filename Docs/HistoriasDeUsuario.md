<h1 style="color: #0946B5;">
  <img src="images/LogoRentCar.png" alt="Logo Rentcar" width="80" style="vertical-align: middle; margin-right: 15px;">Histórias de Usuário
</h1>

<h2 style="color: #0946B5;">1. Autenticação e Cadastro</h2>

<h3 style="color: #26D0FA;">US01 - Autenticação no Sistema</h3>

> **Como** usuário do sistema (Cliente ou Agente)
>
> **Quero** me autenticar com usuário e senha
>
> **Para que** eu possa acessar de forma segura as minhas funcionalidades restritas.

**Critérios de Aceitação:**
* O sistema só pode ser utilizado após cadastro prévio.
* O acesso só é permitido após a validação das credenciais.
* Clientes e agentes cadastrados podem autenticar-se pelo mesmo formulário; o sistema identifica o tipo de conta e encaminha cada um à sua área.
* Contas de agente não são criadas pelo cadastro público de clientes e devem ser provisionadas pela administração do sistema.
* Administradores autenticados podem cadastrar agentes do tipo Empresa ou Banco pela área administrativa, informando também o nome da instituição a que pertencem.
* Administradores podem consultar os agentes cadastrados e abrir uma página própria para editar nome, e-mail, tipo, instituição e, opcionalmente, senha.
* A senha inicial ou nova senha de agentes deve ter no mínimo oito caracteres.
* A autenticação deve atribuir permissões distintas para clientes e agentes, restringindo operações de cliente a contas de cliente.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US02 - Registro de Cliente</h3>

> **Como** Cliente
>
> **Quero** registrar os meus dados de identificação (RG, CPF, Nome, Endereço), profissão e até três entidades empregadoras com os respectivos rendimentos auferidos
>
> **Para que** eu tenha um perfil válido no sistema para realizar pedidos de aluguel.

**Critérios de Aceitação:**
* O sistema deve validar os dados de identificação.
* O sistema deve limitar o registro a um máximo de três (3) rendimentos/empregadores.
* Para cada entidade informada, devem ser cadastrados o nome e um rendimento maior que zero.
* O cadastro de entidades empregadoras é opcional; os dados podem ser consultados e alterados no perfil do cliente.

<br>

<h2 style="color: #0946B5;">2. Gestão de Pedidos (Cliente)</h2>

<h3 style="color: #26D0FA;">US03 - Introduzir Pedido de Aluguel</h3>

> **Como** Cliente
>
> **Quero** introduzir um pedido de aluguel de um automóvel no sistema
>
> **Para que** a minha solicitação seja enviada para análise financeira dos agentes.

**Critérios de Aceitação:**
* O pedido deve estar associado a um automóvel registrado (matrícula, ano, marca, modelo e placa).
* O status inicial do pedido deve ser "Em análise" (ou similar).
* O cliente informa as datas de início e fim, e a data final deve ser posterior à data inicial.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US04 - Consultar Pedido de Aluguel</h3>

> **Como** Cliente
>
> **Quero** consultar os pedidos de aluguel que introduzi
>
> **Para que** eu possa acompanhar o status e o parecer da avaliação financeira.

**Critérios de Aceitação:**
* Os pedidos devem ser apresentados em uma lista com opção de filtrar pelo status.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US05 - Modificar Pedido de Aluguel (Cliente)</h3>

> **Como** Cliente
>
> **Quero** modificar os detalhes do meu pedido de aluguel
>
> **Para que** eu possa corrigir informações ou alterar o veículo pretendido antes da execução do contrato.

**Critérios de Aceitação:**
* Só é possível modificar pedidos que ainda não foram executados ou finalizados.
* Pedidos pendentes, em análise ou aprovados podem ser alterados; ao alterar um pedido aprovado, ele volta para "Em análise" para nova avaliação.
* Pedidos rejeitados ou cancelados não podem ser alterados.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US06 - Cancelar Pedido de Aluguel</h3>

> **Como** Cliente
>
> **Quero** cancelar o meu pedido de aluguel
>
> **Para que** o processo seja interrompido sem gerar obrigações contratuais.

**Critérios de Aceitação:**
* O status do pedido deve mudar para "Cancelado".
* O cliente só pode cancelar pedidos pendentes ou em análise.

<br>

<h2 style="color: #0946B5;">3. Análise e Contratos (Agente)</h2>

<h3 style="color: #26D0FA;">US07 - Avaliar Pedido (Parecer Financeiro)</h3>

> **Como** Agente (Empresa ou Banco)
>
> **Quero** avaliar os pedidos de aluguel introduzidos do ponto de vista financeiro
>
> **Para que** eu possa emitir um parecer positivo ou recusar a solicitação com base nos rendimentos do cliente.

**Critérios de Aceitação:**
* O agente deve consultar pedidos em análise e os rendimentos declarados pelo cliente para fundamentar sua decisão.
* Ao escolher aprovar ou reprovar, o agente deve informar o texto do parecer financeiro, que fica registrado no pedido e visível ao cliente.
* O resultado do parecer atualiza o status do pedido para aprovado ou reprovado.
* Somente pedidos em análise podem receber parecer.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US08 - Modificar Pedido (Agente)</h3>

> **Como** Agente (Empresa ou Banco)
>
> **Quero** modificar os detalhes do pedido de aluguel em análise
>
> **Para que** eu possa ajustar condições financeiras ou associar um contrato de crédito gerido pelo banco.

**Critérios de Aceitação:**
* Enquanto o pedido estiver em análise, o agente pode alterar o automóvel e o período de aluguel.
* O automóvel deve estar cadastrado e a data final deve ser posterior à data inicial; a data inicial não pode estar no passado.
* Após a alteração, o pedido continua em análise para que o agente possa emitir o parecer financeiro.

<hr style="border: 1px solid #E0E0E0;">

<h3 style="color: #26D0FA;">US09 - Executar Contrato</h3>

> **Como** Agente
>
> **Quero** colocar o contrato em consideração para execução após um parecer financeiro positivo
>
> **Para que** o aluguel do automóvel seja formalizado de acordo com a propriedade registrada (cliente, empresa ou banco).

**Critérios de Aceitação:**
* O agente só pode encaminhar pedidos com parecer financeiro positivo.
* O agente pode colocar o pedido aprovado à consideração para execução do contrato.
* O sistema registra o novo estado como "Aguardando execução do contrato" e apresenta o pedido ao cliente.
* O agente consegue consultar os pedidos que já encaminhou para execução.
* O agente formaliza o contrato somente para pedidos encaminhados à execução; o contrato vincula o pedido, cliente, automóvel e período de aluguel.
* Um agente do tipo Banco pode registrar detalhes do crédito bancário no contrato; essa informação é opcional.
* Após formalizado, o pedido passa a "Contrato formalizado", e o cliente pode consultar os dados do contrato.