package org.example;

import java.sql.Timestamp;

public class Registro {
    private Long idRegistro;
    private Timestamp timeStamp;
    private Float temperatura;
    private Float umidade;
    private Float pressao;
    private Float altitude;
    private Float luminosidade;
    private Float velocidadeVento;
    private Integer direcaoVento;
    private Integer intensidadeWifi;
    private Integer idDispositivoFk;

    public Registro() {}

    // Getters e Setters
    public Long getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Long idRegistro) { this.idRegistro = idRegistro; }

    public Timestamp getTimeStamp() { return timeStamp; }
    public void setTimeStamp(Timestamp timeStamp) { this.timeStamp = timeStamp; }

    public Float getTemperatura() { return temperatura; }
    public void setTemperatura(Float temperatura) { this.temperatura = temperatura; }

    public Float getUmidade() { return umidade; }
    public void setUmidade(Float umidade) { this.umidade = umidade; }

    public Float getPressao() { return pressao; }
    public void setPressao(Float pressao) { this.pressao = pressao; }

    public Float getAltitude() { return altitude; }
    public void setAltitude(Float altitude) { this.altitude = altitude; }

    public Float getLuminosidade() { return luminosidade; }
    public void setLuminosidade(Float luminosidade) { this.luminosidade = luminosidade; }

    public Float getVelocidadeVento() { return velocidadeVento; }
    public void setVelocidadeVento(Float velocidadeVento) { this.velocidadeVento = velocidadeVento; }

    public Integer getDirecaoVento() { return direcaoVento; }
    public void setDirecaoVento(Integer direcaoVento) { this.direcaoVento = direcaoVento; }

    public Integer getIntensidadeWifi() { return intensidadeWifi; }
    public void setIntensidadeWifi(Integer intensidadeWifi) { this.intensidadeWifi = intensidadeWifi; }

    public Integer getIdDispositivoFk() { return idDispositivoFk; }
    public void setIdDispositivoFk(Integer idDispositivoFk) { this.idDispositivoFk = idDispositivoFk; }
}