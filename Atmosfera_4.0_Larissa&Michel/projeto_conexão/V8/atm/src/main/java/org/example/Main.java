
package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        // Inicializa o servidor na porta 8080
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        RegistroDAO registroDAO = new RegistroDAO();
        RelatorioService relatorioService = new RelatorioService();

        // 1. API REST para os registros do Atmosfera 4.0
        server.createContext("/api/registros", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                exchange.getResponseHeaders().set(
                        "Access-Control-Allow-Origin", "*");

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json; charset=UTF-8");

                // Recebe os dados enviados pelo simulador
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                    try (InputStream is = exchange.getRequestBody()) {

                        String body = new String(
                                is.readAllBytes(),
                                StandardCharsets.UTF_8);

                        Registro reg = extrairRegistroJson(body);
                        registroDAO.salvar(reg);

                        byte[] resposta = "{\"status\":\"sucesso\"}"
                                .getBytes(StandardCharsets.UTF_8);

                        exchange.sendResponseHeaders(201, resposta.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(resposta);
                        }
                    }

                    // Consulta os registros para o dashboard
                } else if ("GET".equalsIgnoreCase(
                        exchange.getRequestMethod())) {

                    List<Registro> lista = registroDAO.listarUltimos();
                    String json = converterListaParaJson(lista);

                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(200, bytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }

                } else {
                    byte[] resposta = "Método não permitido."
                            .getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(405, resposta.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(resposta);
                    }
                }
            }
        });

        // 2. API de relatórios CSV e PDF
        server.createContext("/api/relatorios/csv",
                exchange -> tratarRelatorio(
                        exchange, relatorioService, "csv"));

        server.createContext("/api/relatorios/pdf",
                exchange -> tratarRelatorio(
                        exchange, relatorioService, "pdf"));

        // 3. Servidor de arquivos HTML, CSS e JavaScript
        server.createContext("/", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String path = exchange.getRequestURI().getPath();

                // A página inicial continua sendo o dashboard
                if (path.equals("/")) {
                    path = "/dashboard.html";
                }

                // Busca os arquivos em resources/public/
                String resourcePath = "/public" + path;

                try (InputStream is =
                             Main.class.getResourceAsStream(resourcePath)) {

                    if (is != null) {

                        byte[] bytes = is.readAllBytes();

                        if (path.endsWith(".html")) {
                            exchange.getResponseHeaders().set(
                                    "Content-Type",
                                    "text/html; charset=UTF-8");

                        } else if (path.endsWith(".css")) {
                            exchange.getResponseHeaders().set(
                                    "Content-Type",
                                    "text/css; charset=UTF-8");

                        } else if (path.endsWith(".js")) {
                            exchange.getResponseHeaders().set(
                                    "Content-Type",
                                    "application/javascript; charset=UTF-8");
                        }

                        exchange.sendResponseHeaders(200, bytes.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }

                    } else {
                        byte[] resposta = "Página não encontrada."
                                .getBytes(StandardCharsets.UTF_8);

                        exchange.getResponseHeaders().set(
                                "Content-Type",
                                "text/plain; charset=UTF-8");

                        exchange.sendResponseHeaders(404, resposta.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(resposta);
                        }
                    }
                }
            }
        });

        // Inicia o servidor
        server.setExecutor(null);
        server.start();

        System.out.println("==================================================");
        System.out.println("Servidor Atmosfera 4.0 em execução!");
        System.out.println("Simulador: http://localhost:8080/robo.html");
        System.out.println("Dashboard: http://localhost:8080/dashboard.html");
        System.out.println("Relatórios: http://localhost:8080/relatorios.html");
        System.out.println("==================================================");
    }

    // Extrai os dados recebidos pelo simulador
    private static Registro extrairRegistroJson(String json) {

        Registro r = new Registro();

        r.setIdDispositivoFk(extrairInt(
                json, "idDispositivoFk", 1));

        r.setTemperatura(extrairFloat(
                json, "temperatura", 25.0f));

        r.setUmidade(extrairFloat(
                json, "umidade", 60.0f));

        r.setPressao(extrairFloat(
                json, "pressao", 1013.2f));

        r.setAltitude(extrairFloat(
                json, "altitude", 760.0f));

        r.setLuminosidade(extrairFloat(
                json, "luminosidade", 800.0f));

        r.setVelocidadeVento(extrairFloat(
                json, "velocidadeVento", 10.0f));

        r.setDirecaoVento(extrairInt(
                json, "direcaoVento", 180));

        r.setIntensidadeWifi(extrairInt(
                json, "intensidadeWifi", -65));

        return r;
    }

    private static float extrairFloat(
            String json, String chave, float padrao) {

        try {
            String busca = "\"" + chave + "\":";
            int posicao = json.indexOf(busca);

            if (posicao < 0) {
                return padrao;
            }

            int inicio = posicao + busca.length();
            int fim = json.indexOf(",", inicio);

            if (fim == -1) {
                fim = json.indexOf("}", inicio);
            }

            return Float.parseFloat(
                    json.substring(inicio, fim).trim());

        } catch (Exception e) {
            return padrao;
        }
    }

    private static int extrairInt(
            String json, String chave, int padrao) {

        try {
            String busca = "\"" + chave + "\":";
            int posicao = json.indexOf(busca);

            if (posicao < 0) {
                return padrao;
            }

            int inicio = posicao + busca.length();
            int fim = json.indexOf(",", inicio);

            if (fim == -1) {
                fim = json.indexOf("}", inicio);
            }

            return Integer.parseInt(
                    json.substring(inicio, fim).trim());

        } catch (Exception e) {
            return padrao;
        }
    }

    // Converte a lista de registros para JSON
    private static String converterListaParaJson(
            List<Registro> lista) {

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < lista.size(); i++) {

            Registro r = lista.get(i);

            sb.append("{")
                    .append("\"idRegistro\":")
                    .append(r.getIdRegistro()).append(",")

                    .append("\"idDispositivoFk\":")
                    .append(r.getIdDispositivoFk()).append(",")

                    .append("\"temperatura\":")
                    .append(r.getTemperatura()).append(",")

                    .append("\"umidade\":")
                    .append(r.getUmidade()).append(",")

                    .append("\"pressao\":")
                    .append(r.getPressao()).append(",")

                    .append("\"intensidadeWifi\":")
                    .append(r.getIntensidadeWifi()).append(",")

                    .append("\"timeStamp\":")
                    .append(r.getTimeStamp() != null
                            ? r.getTimeStamp().getTime()
                            : System.currentTimeMillis())

                    .append("}");

            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    // Processa as solicitações de CSV e PDF
    private static void tratarRelatorio(
            HttpExchange exchange,
            RelatorioService service,
            String formato) throws IOException {

        try {
            if (!"GET".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                responderRelatorio(
                        exchange,
                        405,
                        "Método não permitido.".getBytes(
                                StandardCharsets.UTF_8),
                        "text/plain; charset=UTF-8",
                        null);

                return;
            }

            String periodo = obterParametro(
                    exchange.getRequestURI().getRawQuery(),
                    "periodo");

            if (periodo == null || periodo.trim().isEmpty()) {
                periodo = "semana";
            }

            byte[] arquivo;
            String tipoConteudo;
            String nomeArquivo;

            if ("csv".equals(formato)) {

                arquivo = service.gerarCsv(periodo);
                tipoConteudo = "text/csv; charset=UTF-8";
                nomeArquivo = "relatorio_atmosfera_4_0.csv";

            } else {

                arquivo = service.gerarPdf(periodo);
                tipoConteudo = "application/pdf";
                nomeArquivo = "relatorio_atmosfera_4_0.pdf";
            }

            responderRelatorio(
                    exchange,
                    200,
                    arquivo,
                    tipoConteudo,
                    nomeArquivo);

        } catch (IllegalArgumentException e) {

            responderRelatorio(
                    exchange,
                    400,
                    e.getMessage().getBytes(StandardCharsets.UTF_8),
                    "text/plain; charset=UTF-8",
                    null);

        } catch (Exception e) {

            e.printStackTrace();

            responderRelatorio(
                    exchange,
                    500,
                    "Erro ao gerar o relatório. Verifique o servidor e o banco de dados."
                            .getBytes(StandardCharsets.UTF_8),
                    "text/plain; charset=UTF-8",
                    null);
        }
    }

    // Obtém o período informado na URL
    private static String obterParametro(
            String query, String nome) throws IOException {

        if (query == null || query.isEmpty()) {
            return null;
        }

        for (String parte : query.split("&")) {

            String[] campo = parte.split("=", 2);

            String chave = URLDecoder.decode(
                    campo[0], StandardCharsets.UTF_8.name());

            if (nome.equals(chave)) {

                if (campo.length < 2) {
                    return "";
                }

                return URLDecoder.decode(
                        campo[1], StandardCharsets.UTF_8.name());
            }
        }

        return null;
    }

    // Envia o arquivo ou a mensagem de erro ao navegador
    private static void responderRelatorio(
            HttpExchange exchange,
            int status,
            byte[] conteudo,
            String tipoConteudo,
            String nomeArquivo) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type", tipoConteudo);

        if (nomeArquivo != null) {
            exchange.getResponseHeaders().set(
                    "Content-Disposition",
                    "attachment; filename=\"" + nomeArquivo + "\"");
        }

        exchange.sendResponseHeaders(status, conteudo.length);

        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(conteudo);
        }
    }
}
