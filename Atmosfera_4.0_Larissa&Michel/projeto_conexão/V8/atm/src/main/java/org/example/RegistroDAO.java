package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistroDAO {

    public void salvar(Registro r) {
        String sql = "INSERT INTO registros (time_stamp, temperatura, umidade, pressao, altitude, " +
                "luminosidade, velocidade_vento, direcao_vento, intensidade_wifi, id_dispositivo_fk) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, r.getTimeStamp() != null ? r.getTimeStamp() : new Timestamp(System.currentTimeMillis()));
            stmt.setObject(2, r.getTemperatura());
            stmt.setObject(3, r.getUmidade());
            stmt.setObject(4, r.getPressao());
            stmt.setObject(5, r.getAltitude());
            stmt.setObject(6, r.getLuminosidade());
            stmt.setObject(7, r.getVelocidadeVento());
            stmt.setObject(8, r.getDirecaoVento());
            stmt.setObject(9, r.getIntensidadeWifi());
            stmt.setInt(10, r.getIdDispositivoFk());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Registro> listarUltimos() {
        List<Registro> lista = new ArrayList<>();
        String sql = "SELECT * FROM registros ORDER BY id_registro DESC LIMIT 20";

        try (Connection conn = Conexao.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Registro r = new Registro();
                r.setIdRegistro(rs.getLong("id_registro"));
                r.setTimeStamp(rs.getTimestamp("time_stamp"));
                r.setTemperatura(rs.getFloat("temperatura"));
                r.setUmidade(rs.getFloat("umidade"));
                r.setPressao(rs.getFloat("pressao"));
                r.setAltitude(rs.getFloat("altitude"));
                r.setLuminosidade(rs.getFloat("luminosidade"));
                r.setVelocidadeVento(rs.getFloat("velocidade_vento"));
                r.setDirecaoVento(rs.getInt("direcao_vento"));
                r.setIntensidadeWifi(rs.getInt("intensidade_wifi"));
                r.setIdDispositivoFk(rs.getInt("id_dispositivo_fk"));
                lista.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}