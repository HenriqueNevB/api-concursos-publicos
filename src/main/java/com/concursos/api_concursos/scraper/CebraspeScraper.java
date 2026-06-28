package com.concursos.api_concursos.scraper;

import com.concursos.api_concursos.dto.CargoDTO;
import com.concursos.api_concursos.dto.EditalScrapedDataDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("cebraspeScraper")
public class CebraspeScraper implements BancaScraper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private static final Pattern DATA_PATTERN = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");

    @Override
    public List<EditalScrapedDataDTO> executarScraping(String urlAlvo) throws Exception {
        List<EditalScrapedDataDTO> editaisColetados = new ArrayList<>();
        String urlListagemGeral = "https://apis.cebraspe.org.br/cebraspe/eventos/tipo/concursos/";

        String jsonListagem = executarRequisicao(urlListagemGeral);
        List<FaseEventoWrapper> fases = objectMapper.readValue(jsonListagem, new TypeReference<List<FaseEventoWrapper>>() {});

        for (FaseEventoWrapper fase : fases) {
            if (fase.getEventos() == null) continue;

            for (ListagemEventoWrapper itemCurto : fase.getEventos()) {
                if (itemCurto.getEventoURL() == null || itemCurto.getEventoURL().trim().isEmpty()) continue;

                try {
                    String urlDetalheConcurso = "https://apis.cebraspe.org.br/cebraspe/eventos/" + itemCurto.getEventoURL();
                    String jsonDetalhe = executarRequisicao(urlDetalheConcurso);
                    
                    DetalheConcursoWrapper detalhe = objectMapper.readValue(jsonDetalhe, DetalheConcursoWrapper.class);
                    
                    EditalScrapedDataDTO editalDto = new EditalScrapedDataDTO();
                    editalDto.setTitulo("Concurso Público - " + detalhe.getEventoNomeAbreviado());
                    editalDto.setOrgao(detalhe.getEventoNomeAbreviado());
                    
                    editalDto.setUrlEdital("https://www.cebraspe.org.br/concursos/" + itemCurto.getEventoURL().trim());
                    
                    processarPeriodoInscricao(detalhe.getPeriodoInscricao(), editalDto);
                    processarDataPublicacao(detalhe.getArquivosEdital(), editalDto);

                    List<CargoDTO> listaCargosMapeados = new ArrayList<>();
                    String remuneracaoPadrao = (detalhe.getStrEventoSalarioMaximo() != null) ? detalhe.getStrEventoSalarioMaximo() : "Consultar Edital";

                    if (detalhe.getEventoCargos() != null && !detalhe.getEventoCargos().isEmpty()) {
                        for (CargoWrapper cargoJson : detalhe.getEventoCargos()) {
                            listaCargosMapeados.add(new CargoDTO(
                                    cargoJson.getArea(),
                                    0, 
                                    remuneracaoPadrao,
                                    "Consultar Edital"
                            ));
                        }
                    } else {
                        listaCargosMapeados.add(new CargoDTO("Cargos do Edital", 0, remuneracaoPadrao, "Consultar Edital"));
                    }

                    editalDto.setCargos(listaCargosMapeados);
                    editaisColetados.add(editalDto);

                    Thread.sleep(300);

                } catch (Exception e) {
                    System.err.println("Erro ao processar detalhes do concurso: " + itemCurto.getEventoURL() + " -> " + e.getMessage());
                }
            }
        }

        return editaisColetados;
    }

    private String executarRequisicao(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .timeout(15000)
                .ignoreContentType(true)
                .execute()
                .body();
    }

    private void processarPeriodoInscricao(String textoPeriodo, EditalScrapedDataDTO dto) {
        if (textoPeriodo == null || textoPeriodo.trim().isEmpty()) return;

        Matcher matcher = DATA_PATTERN.matcher(textoPeriodo);
        List<LocalDate> datasEncontradas = new ArrayList<>();

        while (matcher.find()) {
            try {
                datasEncontradas.add(LocalDate.parse(matcher.group(1), dateFormatter));
            } catch (Exception e) {
                
            }
        }

        if (datasEncontradas.size() >= 2) {
            dto.setDataInscricaoInicio(datasEncontradas.get(0).atStartOfDay());
            dto.setDataInscricaoFim(datasEncontradas.get(1).atTime(23, 59, 59));
        } else if (datasEncontradas.size() == 1) {
            dto.setDataInscricaoInicio(datasEncontradas.get(0).atStartOfDay());
        }
    }

    private void processarDataPublicacao(List<ArquivoEditalWrapper> arquivos, EditalScrapedDataDTO dto) {
        if (arquivos == null || arquivos.isEmpty()) return;

        LocalDateTime dataMaisAntiga = null;

        for (ArquivoEditalWrapper arquivo : arquivos) {
            if (arquivo.getDataArquivo() == null || arquivo.getDataArquivo().trim().isEmpty()) continue;

            try {
                LocalDateTime dataAtual = LocalDateTime.parse(arquivo.getDataArquivo(), dateTimeFormatter);
                
                if (dataMaisAntiga == null || dataAtual.isBefore(dataMaisAntiga)) {
                    dataMaisAntiga = dataAtual;
                }
            } catch (Exception e) {
                
            }
        }

        if (dataMaisAntiga != null) {
            dto.setDataPublicacao(dataMaisAntiga.toLocalDate());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FaseEventoWrapper {
        private List<ListagemEventoWrapper> eventos;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ListagemEventoWrapper {
        private String eventoURL;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DetalheConcursoWrapper {
        private String eventoNomeAbreviado;
        private String strEventoSalarioMaximo;
        private String periodoInscricao;
        private List<CargoWrapper> eventoCargos;
        private List<ArquivoEditalWrapper> arquivosEdital;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CargoWrapper {
        private String area;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ArquivoEditalWrapper {
        private String dataArquivo;
    }
}