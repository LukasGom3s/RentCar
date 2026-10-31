# Diagrama de Casos de Uso - Sistema de Aluguel de Carros

## 1. Diagrama Visual

![Diagrama de Casos de Uso](images/DiagramaCasosDeUso.png)

## 2. Atores

* **Cliente (Usuário Individual):** Pessoa física cadastrada no sistema. Deseja alugar veículos e gerenciar seus próprios pedidos.

* **Agente (Empresa/Banco):** Usuário corporativo com permissões gerenciais. Responsável por analisar o perfil financeiro dos clientes e gerir o andamento dos contratos de aluguel.

## 3. Descrição dos Casos de Uso

### Casos de Uso Gerais

* **Autenticar (Login):** Valida as credenciais (usuário e senha) do Cliente ou Agente. É um requisito obrigatório (`<<include>>`) para qualquer outra ação no sistema, garantindo que usuários anônimos não tenham acesso.

### Casos de Uso do Cliente

* **Introduzir pedido:** Permite que o cliente crie um novo pedido de aluguel de veículo, selecionando as opções desejadas e fornecendo os dados necessários (identificação, rendimentos, etc.).

* **Modificar pedido:** Permite ao cliente alterar os detalhes de um pedido de aluguel que ainda não foi executado (ex: trocar a data ou o modelo do carro).

* **Consultar pedido:** Permite ao cliente visualizar o status e os detalhes de seus pedidos de aluguel atuais e passados.

* **Cancelar pedido:** Permite ao cliente desistir e cancelar um pedido de aluguel pendente.

### Casos de Uso do Agente

* **Modificar pedido (Agente):** Permite ao agente alterar informações técnicas atreladas a um pedido de um cliente.

* **Avaliar pedido:** O agente analisa os dados do pedido do cliente sob o ponto de vista financeiro, verificando renda, histórico e viabilidade de crédito.

* **Executar contrato:** Após um parecer financeiro positivo na avaliação, o agente gera e formaliza o contrato de aluguel/crédito, consolidando a operação.