package org.example;

import java.util.List;

public class TesteDAO {
    public static void main(String[] args) {
        RegistroDAO dao = new RegistroDAO();

        System.out.println("==================================================");
        System.out.println("--- TESTANDO INSERÇÃO NO BANCO DE DADOS ---");
        System.out.println("==================================================");

        Registro novo = new Registro();
        // ID do dispositivo cadastrado no banco (ex: 1, 2 ou 3)
        novo.setIdDispositivoFk(1);
        novo.setTemperatura(26.5f);
        novo.setUmidade(60.0f);
        novo.setPressao(1013.2f);
        novo.setAltitude(760.0f);
        novo.setLuminosidade(850.0f);
        novo.setVelocidadeVento(10.0f);
        novo.setDirecaoVento(180);
        novo.setIntensidadeWifi(-65);

        // Salva o novo registro no MySQL
        dao.salvar(novo);
        System.out.println("✅ Registro salvo com sucesso no banco de dados!");

        System.out.println("\n==================================================");
        System.out.println("--- TESTANDO LISTAGEM DOS ÚLTIMOS REGISTROS ---");
        System.out.println("==================================================");

        List<Registro> lista = dao.listarUltimos();
        for (Registro r : lista) {
            System.out.println("ID Reg: " + r.getIdRegistro() +
                    " | Disp: " + r.getIdDispositivoFk() +
                    " | Temp: " + r.getTemperatura() + "°C" +
                    " | Umidade: " + r.getUmidade() + "%" +
                    " | Pressão: " + r.getPressao() + " hPa" +
                    " | Wi-Fi: " + r.getIntensidadeWifi() + " dBm");
        }
        System.out.println("==================================================");
    }
}