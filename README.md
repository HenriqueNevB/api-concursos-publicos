API Concursos Públicos

Este projeto é uma API REST desenvolvida em Spring Boot com Java 21 e PostgreSQL. O objetivo principal do sistema é automatizar a coleta, estruturação e disponibilização de dados sobre concursos públicos de diversas bancas organizadoras, centralizando informações críticas como órgãos, vagas, remunerações e links oficiais de editais.

Como Rodar o Projeto com Docker
Pré-requisitos
Docker e Docker Compose instalados na máquina.

Execução
Toda a infraestrutura necessária (o banco de dados PostgreSQL e a própria aplicação Spring Boot) já está configurada para subir de maneira automatizada e isolada em contêineres.

Para iniciar o projeto, abra o terminal na raiz do diretório onde estão localizados os arquivos do código e execute o seguinte comando:

Bash
docker compose up --build
O Docker baixará as imagens necessárias, criará a estrutura de tabelas na base de dados automaticamente, executará a carga inicial (seed) do usuário administrador padrão e iniciará o servidor. A aplicação estará totalmente operacional na porta 8080.

Para acessar a documentação interativa dos endpoints, testar os fluxos de requisições e disparar manualmente os robôs de coleta de dados, abra o seu navegador de internet e acesse o endereço do Swagger UI:

http://localhost:8080/swagger-ui.html

Arquitetura e Estrutura de Arquivos
O sistema foi estruturado seguindo os padrões recomendados de desenvolvimento de software, dividindo a aplicação em camadas claras para garantir modularidade, manutenibilidade e facilidade de expansão.

Abaixo está o mapeamento dos principais componentes organizados no código fonte da aplicação:

1 Camada de Configurações (config/)
DatabaseSeedConfig.java: Executa rotinas imediatamente após a inicialização do banco. Verifica se a tabela de usuários está vazia e gera automaticamente as credenciais do primeiro administrador do sistema (admin@concursos.com / senha: Admin) utilizando a criptografia BCrypt.

ScraperScheduler.java: Gerencia o agendamento automatizado do robô. Possui um despertador interno configurado via expressão Cron que, por padrão, acorda de hora em hora para buscar novas atualizações de editais sem necessitar de interferência humana.

SecurityConfig.java e SecurityFilter.java: Configura os filtros de proteção de endpoints. Estabelece quais rotas são livres e quais exigem a validação do Token JWT anexado ao cabeçalho da requisição HTTP, gerenciando permissões baseadas nas atribuições (roles) do usuário autenticado.

SwaggerConfig.java: Customiza as informações de título, descrição e versão do projeto que aparecem na interface web interativa do Swagger.

2 Camada de Controladores (controller/)
Responsável pela exposição das rotas HTTP da API. O mapeamento divide o acesso público da navegação de cunho restrito e administrativo:

AuthController.java (/api/auth): Rota pública criada para receber as credenciais de login e, em caso de sucesso, emitir o Token de acesso JWT criptografado.

BancaController.java: Expõe rotas públicas (/api/bancas) para listagem e busca por ID das organizadoras. Protege sob a rota /api/admin/bancas os métodos de cadastro, atualização e exclusão de bancas.

EditalController.java: Fornece a listagem pública de todos os editais capturados pelos robôs em /api/editais. Bloqueia em /api/admin/editais/{id} a visualização minuciosa e estruturada de detalhes de um concurso específico para auditoria interna.

ScraperController.java (/api/admin/scrapers): Endpoint administrativo focado em forçar o acionamento sob demanda dos motores de scraping de dados, permitindo disparar a busca global de todas as bancas cadastradas ou direcionar para apenas uma de ID específico.

UsuarioController.java (/api/admin/usuarios): Permite listar os administradores cadastrados ou adicionar novas contas autorizadas ao painel interno.

3 Camada de Repositórios (repository/)
Interfaces que herdam os comportamentos padrão de CRUD do Spring Data JPA e implementam regras cruciais de integridade de dados:

BancaRepository.java: Valida a existência de siglas duplicadas no sistema antes da persistência.

EditalRepository.java: Busca editais baseando-se na URL única de publicação. É fundamental para o motor do robô identificar se um edital já foi processado anteriormente ou se trata-se de um novo registro.

UsuarioRepository.java: Localiza o perfil de acesso mapeado na base através do endereço de e-mail.

LogScraperRepository.java: Realiza a escrita do histórico de auditoria técnica das execuções do mecanismo.

4 Camada de Modelos e Dados (model/)
Banca.java: Representa a entidade da organizadora, associando seus dados institucionais ao nome do identificador do robô (scraperBean) responsável por lê-la.

Usuario.java: Representa os dados cadastrais e o nível de acesso do administrador.

LogScraper.java: Grava informações de metadados das tentativas de varredura (data de início, data de término, status do log e mensagem final de erro ou sucesso).

Edital.java: Mapeia os dados extraídos do concurso. Possui o campo jsonCargos anotado nativamente como um tipo jsonb do PostgreSQL (@JdbcTypeCode(SqlTypes.JSON)). Isso permite persistir matrizes de cargos, remunerações e distribuição de vagas dinâmicas em formato de texto estruturado dentro da própria tabela, eliminando a necessidade de mapear relacionamentos pesados.

5 Camada de Serviços (service/)
BancaService.java e UsuarioService.java: Centralizam regras de validação cadastral e conversão de dados entre requisições.

EditalService.java: Gerencia o fornecimento de editais.

TokenService.java: Codifica e decodifica os Tokens JWT da aplicação utilizando algoritmos de assinatura HMAC256 com tempo de expiração programado de duas horas.

ScraperService.java: O motor integrador da aplicação. É responsável por buscar as bancas cadastradas que possuem vinculação com robôs ativos, buscar a instância lógica do robô dinamicamente pelo contexto interno do Spring, receber a estrutura limpa de dados coletados e avaliar se o edital deve ser inserido como novo ou apenas atualizado no banco.

O Mecanismo Scraper e o Padrão Strategy

O projeto adota o padrão de projeto arquitetural Strategy para garantir que o sistema consiga escalar a sua infraestrutura e aceitar dezenas de novos robôs de leitura sem precisar reescrever as classes de negócio centrais.

A interface abstrata BancaScraper define o contrato oficial de comportamento do sistema. Qualquer nova banca que necessite ser integrada ao sistema precisa apenas criar uma nova classe Java que implemente essa interface e assinar o método executarScraping.

Regras para Criação e Substituição de Novos Scrapers
Localização Correta: O novo robô desenvolvido deve ser inserido obrigatoriamente dentro do pacote (pasta) com.concursos.api_concursos.scraper.

Registro no Motor do Spring: A nova classe precisa receber a anotação @Component("nomeDoSeuScraper"). É esta string que define o identificador que o Spring lerá dinamicamente em tempo de execução.

Gerenciamento Flexível de Implementações: Se o layout do portal de um concurso mudar ou você desejar reescrever a estratégia de captura de uma banca, a substituição é totalmente transparente:

Abordagem de Substituição de Código: Você pode manter o mesmo nome no componente do Spring (@Component) na sua nova classe e deletar a lógica antiga. O sistema absorverá o robô atualizado imediatamente sem mexer em nenhuma configuração.

Abordagem de Substituição de Banco: Se você criar um novo scraper com outro nome (ex: cebraspeScraperV2), basta ir ao Swagger UI (ou diretamente na tabela banca através de uma query SQL) e atualizar o valor da coluna scraper_bean daquela banca para o novo nome.

Exemplo Prático de Funcionamento: Cebraspe

O robô focado na leitura do portal do Cebraspe (CebraspeScraper.java) demonstra a maturidade da arquitetura da aplicação ao lidar com portais web modernos construídos em arquiteturas do tipo SPA (Single Page Applications, como Angular e React).

Em vez de inicializar instâncias pesadas de navegadores virtuais em segundo plano (como Selenium ou Puppeteer), que degradam o desempenho da hospedagem e geram consumo excessivo de memória RAM, o robô intercepta diretamente a comunicação de rede das chamadas de API REST ocultas da própria organizadora através do Jsoup, tratando as respostas JSON de forma veloz com a biblioteca Jackson.

Para colocar o robô para rodar na sua máquina local ou em ambiente simulado do zero, o fluxo completo consiste nas duas etapas práticas descritas abaixo:

Passo 1: Cadastrar a Banca através do Endpoint

Abra a página do Swagger UI no seu navegador, expanda a aba referente ao gerenciamento de Bancas e localize a rota de criação: POST /api/admin/bancas.

Clique na opção Try it out e insira exatamente o objeto estruturado JSON abaixo dentro do corpo da requisição:

JSON
{
  "nome": "Centro Brasileiro de Pesquisa em Avaliação e Seleção e de Promoção de Eventos",
  "sigla": "CEBRASPE",
  "siteOficial": "https://www.cebraspe.org.br/concursos/",
  "scraperBean": "cebraspeScraper"
}

Clique no botão Execute. A aplicação processará a requisição, validará que o identificador cebraspeScraper é uma classe válida registrada no contexto do projeto e criará o registro na tabela de banco de dados, retornando o código de status HTTP 211 Created.

Passo 2: Execução Interna do Processamento do Robô

Com a banca devidamente cadastrada, o motor de execução pode ser ativado a qualquer momento executando uma chamada na rota POST /api/admin/scrapers/disparar-todos. Ao receber o sinal, a classe ScraperService executa as seguintes ações de processamento de forma automatizada:

Mapeamento de Contexto: A classe lê o banco de dados, encontra a linha correspondente ao Cebraspe e solicita ao gerenciador de componentes do Spring que traga a instância da estratégia registrada como cebraspeScraper.

Fase 1: O robô realiza uma requisição para a rota restrita de eventos gerais do portal alvo, obtendo uma árvore de dados que classifica os concursos ativos (ex: inscrições abertas, andamento, encerrados) e extrai os códigos numéricos de identificação de cada edital na plataforma.

Fase 2: O robô executa iterações em laço, montando URLs de consulta dinâmicas com os códigos obtidos. Para cada concurso individual, ele consome a resposta de dados rica fornecida, extraindo informações cruciais sobre as áreas de atuação, remuneração máxima prevista e quantitativo total de vagas.

Tratamento de Expressões Regulares: O robô extrai strings brutas descritivas sobre cronogramas e utiliza padrões Regex para mapear formatos de data nacionais (dd/MM/yyyy), convertendo strings em objetos estruturados de tempo (LocalDateTime) manipuláveis pelo sistema.

Auditoria de Arquivos de Abertura: O scraper analisa a listagem de links de documentos anexados ao painel do concurso. Ele rastreia metadados em busca de menções a termos de "abertura", salvando o link definitivo do arquivo PDF direto do repositório da banca e usando o carimbo de data mais antigo encontrado como a data oficial de nascimento daquele certame.

Padronização e Persistência: Todas as informações são envelopadas no DTO unificado EditalScrapedDataDTO e entregues de volta ao serviço central. O motor valida se a URL do edital já se encontra salva na base. Se já houver registro, o sistema atualiza as colunas de vagas e o JSON de cargos, alterando o indicador de auditoria atualizado_em. Se for inédito, o edital é persistido imediatamente no banco com o status padrão de inscrições abertas. Todos os passos técnicos alimentam a tabela log_scraper para monitoramento.
