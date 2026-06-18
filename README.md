API Concursos Públicos

Este projeto é uma API REST desenvolvida em Spring Boot com Java 21 e PostgreSQL. O objetivo principal do sistema é automatizar a coleta, estruturação e disponibilização de dados sobre concursos públicos de diversas bancas organizadoras, centralizando informações críticas como órgãos, vagas, remunerações e links oficiais de editais.
Como Rodar o Projeto com Docker

Pré-requisitos
Docker e Docker Compose instalados na máquina

Execução

Toda a infraestrutura (o banco de dados PostgreSQL e a própria aplicação Spring Boot) já está configurada para subir em ambiente isolado. Para iniciar o projeto, abra o terminal na raiz do diretório e execute o seguinte comando:

Bash
docker compose up --build

O Docker baixará as imagens necessárias, criará a base de dados automaticamente e iniciará o servidor. A aplicação estará disponível na porta 8080.

Para acessar a documentação interativa dos endpoints, testar as requisições e disparar os robôs, abra o navegador no endereço: http://localhost:8080/swagger-ui.html

Arquitetura e Estrutura do Projeto

O sistema foi desenhado seguindo boas práticas de desenvolvimento de software, garantindo separação de responsabilidades, extensibilidade para novos robôs de captura e persistência flexível. Abaixo está o resumo do papel de cada camada no ecossistema da aplicação:

1. Camada de Controladores (Controllers)
Responsável por expor os endpoints HTTP da API e gerenciar as rotas. A arquitetura de URLs divide claramente o acesso público do administrativo:
BancaController e EditalController: Expõem rotas públicas para listagem e consulta de dados por usuários finais através do prefixo /api/bancas e /api/editais. Também contêm os endpoints de criação, edição e exclusão de registros sob o prefixo /api/admin.
ScraperController: Rota estritamente administrativa (/api/admin/scrapers) utilizada para disparar manualmente o processo de varredura das bancas.
UsuarioController: Gerencia o cadastro e a listagem de administradores do sistema.

2. Camada de Serviços (Services)
Onde reside a lógica de negócios central do sistema:
BancaService e EditalService: Gerenciam as validações, regras de salvar, atualizar e buscar as entidades, além de fazer a ponte com os repositórios de dados.
ScraperService: O motor do robô. Ele lê as bancas cadastradas, aciona o componente de scraping correto via contexto do Spring e processa o retorno padronizado para salvar ou atualizar os registros de editais, mapeando inclusive os campos cronológicos e convertendo os cargos para persistência.

3. Camada de Modelos e Dados (Models & DTOs)
Banca e Edital: Entidades que mapeiam as tabelas do banco de dados. A entidade Edital possui um campo chamado jsonCargos, configurado como um tipo nativo jsonb do PostgreSQL. Isso permite salvar estruturas de cargos dinâmicas (com salários, vagas e escolaridades diferentes) sem a necessidade de criar tabelas de relacionamento complexas.
DTOs de Transporte Geral: Classes como EditalResponseDTO isolam as entidades do banco de dados nas respostas da API.
EditalScrapedDataDTO: DTO agnóstico e unificado criado especificamente para padronizar o retorno de qualquer scraper do sistema. Ele funciona como o contrato oficial de coleta de dados, garantindo que o motor do sistema receba títulos, órgãos, links, cargos e datas estruturadas de forma idêntica, independente de qual robô realizou a captura.

4. Camada de Segurança (Security)
SecurityConfig: Centraliza as regras de autenticação e autorização. Toda a rota que possuir o padrão /api/admin/ é interceptada e exige privilégios de administrador logado, enquanto as rotas de consulta de editais e bancas permanecem abertas ao público.

Funcionamento do Mecanismo Scraper: Exemplo Cebraspe

O sistema utiliza o padrão de projeto Strategy para que cada banca organizadora possua seu próprio algoritmo de captura isolado, mas compartilhando a mesma interface de integração (BancaScraper).

O caso do Cebraspe ilustra como o sistema lida de forma inteligente com sites modernos construídos em frameworks SPA (como React). Em vez de simular um navegador pesado para renderizar o JavaScript da página, o scraper intercepta as requisições HTTP enviadas para a API REST oculta da própria organizadora.

Configuração Obrigatória no Banco de Dados

Para que o motor do ScraperService localize e execute este robô, é necessário que a banca esteja previamente cadastrada na tabela banca do banco de dados. O campo scraper_bean deve conter exatamente o nome do componente configurado na classe Java (@Component("cebraspeScraper")).

Você pode realizar esse cadastro executando o seguinte comando SQL na sua base de dados:

SQL
INSERT INTO banca (nome, sigla, site_oficial, scraper_bean)
VALUES ('Centro Brasileiro de Pesquisa em Avaliação e Seleção e de Promoção de Eventos', 
        'CEBRASPE', 
        'https://www.cebraspe.org.br/concursos/', 
        'cebraspeScraper');


(Nota: Caso queira testar o comportamento do sistema com dados simulados e sem fazer requisições externas, você pode cadastrar uma linha utilizando o bean exemploScraper).

O Fluxo de Processamento Interno

Assim que o serviço é disparado (via endpoint administrativo ou rotina automatizada), o robô executa duas etapas consecutivas:

Etapa 1: Captura da Listagem Geral

O robô realiza uma chamada HTTP para o endpoint geral da banca. O retorno é um texto em formato JSON contendo agrupamentos por status do evento (como inscrições abertas ou em andamento). O scraper faz a leitura desse texto e extrai os códigos identificadores de cada concurso ativo na plataforma.

Etapa 2: Consulta Detalhada por Concurso

Para cada identificador encontrado na primeira etapa, o robô monta dinamicamente uma nova URL de consulta e realiza uma segunda requisição. Esse endpoint detalhado retorna dados ricos sobre o concurso específico.

Durante essa fase, o scraper realiza os seguintes procedimentos:

Extrai o nome completo do órgão e o valor da remuneração máxima.
Mapeia a listagem de cargos disponíveis.
Captura o texto corrido do período de inscrição e aplica expressões regulares (Regex) para identificar os padrões de data nacionais (dd/MM/yyyy), convertendo-os em objetos estruturados de tempo (LocalDateTime).
Varre os metadados de arquivos anexos para identificar o documento oficial de abertura, utilizando a data de publicação mais antiga encontrada como a data de publicação oficial do edital.

Com as informações tratadas, o scraper encapsula os dados no EditalScrapedDataDTO. O objeto é retornado para o ScraperService, que faz a leitura dos parâmetros, avalia se o edital já existe pelo link gerado e salva as informações cronológicas e estruturadas diretamente no banco de dados PostgreSQL.

