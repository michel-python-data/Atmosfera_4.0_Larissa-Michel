package org.example;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class RelatorioService {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public List<Registro> buscarRegistros(String periodo)
            throws SQLException {

        String sql;

        switch (periodo) {
            case "semana":
                sql = "SELECT * FROM registros " +
                        "WHERE time_stamp >= NOW() - INTERVAL 7 DAY " +
                        "ORDER BY time_stamp DESC";
                break;

            case "24h":
                sql = "SELECT * FROM registros " +
                        "WHERE time_stamp >= NOW() - INTERVAL 24 HOUR " +
                        "ORDER BY time_stamp DESC";
                break;

            case "20":
                sql = "SELECT * FROM registros " +
                        "ORDER BY id_registro DESC LIMIT 20";
                break;

            default:
                throw new IllegalArgumentException(
                        "Período inválido. Use semana, 24h ou 20."
                );
        }

        List<Registro> registros = new ArrayList<>();

        try (Connection conexao = Conexao.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Registro r = new Registro();

                r.setIdRegistro(rs.getLong("id_registro"));
                r.setTimeStamp(rs.getTimestamp("time_stamp"));
                r.setTemperatura(floatNulo(rs, "temperatura"));
                r.setUmidade(floatNulo(rs, "umidade"));
                r.setPressao(floatNulo(rs, "pressao"));
                r.setAltitude(floatNulo(rs, "altitude"));
                r.setLuminosidade(floatNulo(rs, "luminosidade"));
                r.setVelocidadeVento(
                        floatNulo(rs, "velocidade_vento"));
                r.setDirecaoVento(intNulo(rs, "direcao_vento"));
                r.setIntensidadeWifi(intNulo(rs, "intensidade_wifi"));
                r.setIdDispositivoFk(intNulo(rs, "id_dispositivo_fk"));

                registros.add(r);
            }
        }

        return registros;
    }

    private Float floatNulo(ResultSet rs, String coluna)
            throws SQLException {
        float valor = rs.getFloat(coluna);
        return rs.wasNull() ? null : valor;
    }

    private Integer intNulo(ResultSet rs, String coluna)
            throws SQLException {
        int valor = rs.getInt(coluna);
        return rs.wasNull() ? null : valor;
    }

    private String dataFormatada(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return timestamp.toLocalDateTime().format(FORMATO_DATA);
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String campoCsv(Object valor) {
        String texto = texto(valor);
        texto = texto.replace("\"", "\"\"");

        return "\"" + texto + "\"";
    }

    public byte[] gerarCsv(String periodo)
            throws SQLException {

        List<Registro> registros = buscarRegistros(periodo);

        StringBuilder csv = new StringBuilder();

        // BOM para facilitar a leitura de acentos no Excel.
        csv.append('\uFEFF');

        csv.append(
                "ID Registro;Data/Hora;ID Dispositivo;" +
                        "Temperatura (°C);Umidade (%);Pressão (hPa);" +
                        "Altitude (m);Luminosidade (lux);" +
                        "Velocidade do vento;Direção do vento;" +
                        "Intensidade Wi-Fi (dBm)\n"
        );

        for (Registro r : registros) {
            csv.append(campoCsv(r.getIdRegistro())).append(';');
            csv.append(campoCsv(dataFormatada(r.getTimeStamp()))).append(';');
            csv.append(campoCsv(r.getIdDispositivoFk())).append(';');
            csv.append(campoCsv(r.getTemperatura())).append(';');
            csv.append(campoCsv(r.getUmidade())).append(';');
            csv.append(campoCsv(r.getPressao())).append(';');
            csv.append(campoCsv(r.getAltitude())).append(';');
            csv.append(campoCsv(r.getLuminosidade())).append(';');
            csv.append(campoCsv(r.getVelocidadeVento())).append(';');
            csv.append(campoCsv(r.getDirecaoVento())).append(';');
            csv.append(campoCsv(r.getIntensidadeWifi())).append('\n');
        }

        return csv.toString().getBytes(
                java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] gerarPdf(String periodo)
            throws SQLException, DocumentException, IOException {

        List<Registro> registros = buscarRegistros(periodo);

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            Document documento = new Document(
                    PageSize.A4.rotate(), 20, 20, 25, 25);

            PdfWriter.getInstance(documento, saida);
            documento.open();

            Font titulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 16, Font.BOLD);

            Font normal = FontFactory.getFont(
                    FontFactory.HELVETICA, 9, Font.NORMAL);

            Font cabecalho = FontFactory.getFont(
                    FontFactory.HELVETICA, 7, Font.BOLD);

            Font conteudo = FontFactory.getFont(
                    FontFactory.HELVETICA, 7, Font.NORMAL);

            documento.add(new Paragraph(
                    "Relatório Atmosfera 4.0", titulo));

            documento.add(new Paragraph(
                    "Período: " + descricaoPeriodo(periodo), normal));

            documento.add(new Paragraph(
                    "Gerado em: " +
                            LocalDateTime.now().format(FORMATO_DATA), normal));

            documento.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(11);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{
                    0.7f, 1.5f, 1.0f, 1.0f, 0.8f,
                    0.8f, 0.8f, 1.0f, 1.1f, 1.0f, 1.1f
            });

            String[] colunas = {
                    "ID", "Data/Hora", "Dispositivo",
                    "Temperatura", "Umidade", "Pressão",
                    "Altitude", "Luminosidade", "Vel. vento",
                    "Dir. vento", "Wi-Fi"
            };

            for (String coluna : colunas) {
                PdfPCell celula = new PdfPCell(
                        new Phrase(coluna, cabecalho));
                celula.setPadding(4);
                tabela.addCell(celula);
            }

            tabela.setHeaderRows(1);

            for (Registro r : registros) {
                adicionarCelula(tabela, texto(r.getIdRegistro()), conteudo);
                adicionarCelula(tabela, dataFormatada(r.getTimeStamp()), conteudo);
                adicionarCelula(tabela, texto(r.getIdDispositivoFk()), conteudo);
                adicionarCelula(tabela, texto(r.getTemperatura()), conteudo);
                adicionarCelula(tabela, texto(r.getUmidade()), conteudo);
                adicionarCelula(tabela, texto(r.getPressao()), conteudo);
                adicionarCelula(tabela, texto(r.getAltitude()), conteudo);
                adicionarCelula(tabela, texto(r.getLuminosidade()), conteudo);
                adicionarCelula(tabela, texto(r.getVelocidadeVento()), conteudo);
                adicionarCelula(tabela, texto(r.getDirecaoVento()), conteudo);
                adicionarCelula(tabela, texto(r.getIntensidadeWifi()), conteudo);
            }

            if (registros.isEmpty()) {
                documento.add(new Paragraph(
                        "Nenhum registro encontrado para o período.",
                        normal));
            } else {
                documento.add(tabela);
            }

            documento.close();

            return saida.toByteArray();
        }
    }

    private void adicionarCelula(
            PdfPTable tabela, String valor, Font fonte) {

        PdfPCell celula = new PdfPCell(new Phrase(valor, fonte));
        celula.setPadding(3);
        tabela.addCell(celula);
    }

    private String descricaoPeriodo(String periodo) {
        switch (periodo) {
            case "semana":
                return "Últimos 7 dias";
            case "24h":
                return "Últimas 24 horas";
            case "20":
                return "Últimos 20 registros";
            default:
                return periodo;
        }
    }
}
