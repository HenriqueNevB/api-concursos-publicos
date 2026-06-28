package com.concursos.api_concursos.scraper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.concursos.api_concursos.dto.CargoDTO;
import com.concursos.api_concursos.dto.EditalScrapedDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component("fgvScraper")
public class FGVScraper implements BancaScraper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public List<EditalScrapedDataDTO> executarScraping(String urlAlvo) throws Exception {
      List<EditalScrapedDataDTO> editaisColetados = new ArrayList<>();
      List<Evento> eventos  = new ArrayList<>();
      // List<String> fases = List.of("&view_name=concursos_em_andamento", "&view_name=concursos_realizados");
      List<String> fases = List.of("&view_name=concursos_em_andamento");

      String urlBase = "https://conhecimento.fgv.br"; 
      String afixoListagem = "/views/ajax?view_display_id=block_1";

      for (String fase : fases){
        String url = urlBase + afixoListagem + fase + "&page=";
        int maximo_paginas = obterMaxPaginas(url+"0");
        for (int i = 0; i <= maximo_paginas; i++){
          String resp = executarRequisicao(url + Integer.toString(i));
          recuperaEventos(eventos, resp);
        }
      }


      for (Evento evento : eventos){
        String urlEvento = urlBase + evento.getUrl();
        EditalScrapedDataDTO editalDto = new EditalScrapedDataDTO();
        
        List<CargoDTO> cargos = new ArrayList<>();
        cargos.add(new CargoDTO("Consultar Edital", 0, "Consultar Edital", "Consultar Edital"));

        editalDto.setTitulo(evento.getTitulo());
        editalDto.setOrgao(evento.getSigla());
        editalDto.setCargos(cargos);
        
        List<LinkEData> LinksEDatas = recuperaLinksEDatas(urlEvento);
        if (LinksEDatas.size() != 0) {
          LinkEData infoEdital = LinksEDatas.get(LinksEDatas.size() - 1);
          editalDto.setUrlEdital(infoEdital.getUrlArquivo());
          LocalDate dataPublicacao = LocalDate.parse(infoEdital.getData(), dateFormatter);
          editalDto.setDataPublicacao(dataPublicacao);
          LinkEData infoInscricao = recuperaInscricao(LinksEDatas);
          if (infoInscricao != null){
            LocalDate dataInscricao = LocalDate.parse(infoInscricao.getData(), dateFormatter);
            editalDto.setDataInscricaoInicio(dataInscricao.atStartOfDay());
          }
        } else{
          editalDto.setUrlEdital(urlEvento);
        }

        editaisColetados.add(editalDto);
        Thread.sleep(300);
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

    private void recuperaEventos(List<Evento> eventos, String resp) throws JsonMappingException, JsonProcessingException{
      JsonNode root = objectMapper.readTree(resp);

      for (JsonNode node : root){
        if ("insert".equals(node.path("command").asText())
            && "replaceWith".equals(node.path("method").asText())) {
          String html = node.path("data").asText();

          Document doc = Jsoup.parse(html);
          for (Element link : doc.select(".views-row .field-content a")){
            String title = link.text();
            String href = link.attr("href");
            
            Evento evento = new Evento(title, href);
            eventos.add(evento);

          }
          break;
        }
      }
    }

    private int obterMaxPaginas(String url) throws Exception {
      String urlBase = url.contains("&page=") ? url.substring(0, url.indexOf("&page=")) : url;
      String resposta = executarRequisicao(urlBase + "&page=0");

      JsonNode root = objectMapper.readTree(resposta);
      int maxPage = 0;

      for (JsonNode node : root) {
        if ("insert".equals(node.path("command").asText())
            && "replaceWith".equals(node.path("method").asText())) {
          String html = node.path("data").asText();
          Document doc = Jsoup.parse(html);

          for (Element link : doc.select("a[href*=page]")) {
            String href = link.attr("href");
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("page=(\\d+)").matcher(href);
            if (matcher.find()) {
              int page = Integer.parseInt(matcher.group(1));
              if (page > maxPage) {
                maxPage = page;
              }
            }
          }
          break;
        }
      }
      return maxPage;
    }

    private List<LinkEData> recuperaLinksEDatas(String url) throws Exception {
      String html = executarRequisicao(url);
      Document doc = Jsoup.parse(html);
      List<LinkEData> linksEDatas = new ArrayList<>();

      for (Element p : doc.select(".paragraph--type--texto-data")) {
          String data = p.select(".field--name-field-td-data time").text();
          Element link = p.selectFirst(".field--name-field-td-texto a");
          if (link != null) {
              linksEDatas.add(new LinkEData(data, link.attr("href"), link.text()));
          }
      }
      return linksEDatas;
    }

    private LinkEData recuperaInscricao(List<LinkEData> LinksEDatas) {
      for (LinkEData link : LinksEDatas) {
        if ("Inscrição".equals(link.getTitulo())) {
          return link;
        }
      }
      return null;
    }

    @Getter
    @AllArgsConstructor
    private static class Evento {
      public String titulo;
      public String url;


      public String getSigla() {
        return url.substring(url.lastIndexOf('/') + 1); 
      }
      @Override
      public String toString(){
        return "Evento{Título='" + titulo + "', url='" + url + "}";
      }
    }

    @Getter
    @AllArgsConstructor
    private static class LinkEData {
      public String data;
      public String urlArquivo;
      public String titulo;

      @Override
      public String toString(){
        return "ArquivoData{data='" + data + "', url='" + urlArquivo + "', titulo='" + titulo + "'}";
      }
    }

}