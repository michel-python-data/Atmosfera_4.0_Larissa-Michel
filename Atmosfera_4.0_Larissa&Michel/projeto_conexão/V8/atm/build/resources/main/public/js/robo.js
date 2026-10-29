let intervaloSimulacao = null;

function gerarValorAleatorio(min, max, decimais = 1) {
    const valor = Math.random() * (max - min) + min;
    return parseFloat(valor.toFixed(decimais));
}

function enviarLeitura() {
    // 1. Captura os valores configurados na tela
    const idDispositivo = parseInt(document.getElementById('dispositivoId').value) || 1;
    const tempMin = parseFloat(document.getElementById('tempMin').value) || 18;
    const tempMax = parseFloat(document.getElementById('tempMax').value) || 32;
    const umidMin = parseFloat(document.getElementById('umidMin').value) || 40;
    const umidMax = parseFloat(document.getElementById('umidMax').value) || 85;

    // 2. Gera os dados simulados dos sensores
    const temperatura = gerarValorAleatorio(tempMin, tempMax, 1);
    const umidade = gerarValorAleatorio(umidMin, umidMax, 1);
    const pressao = gerarValorAleatorio(1005.0, 1020.0, 1);
    const intensidadeWifi = Math.floor(gerarValorAleatorio(-90, -40, 0)); // Sinal Wi-Fi em dBm

    // 3. Monta o payload JSON para o Java
    const payload = {
        idDispositivoFk: idDispositivo,
        temperatura: temperatura,
        umidade: umidade,
        pressao: pressao,
        intensidadeWifi: intensidadeWifi
    };

    // 4. Envia o POST para a API Java (`/api/registros`)
    fetch('/api/registros', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (!res.ok) throw new Error("Erro ao gravar registro");
        return res.json();
    })
    .then(data => {
        // 5. Atualiza a tela com o registro enviado
        const agora = new Date();
        document.getElementById('lblData').innerText = agora.toLocaleDateString('pt-BR');
        document.getElementById('lblHora').innerText = agora.toLocaleTimeString('pt-BR');

        document.getElementById('lblTemp').innerText = `${temperatura} °C`;
        document.getElementById('lblUmid').innerText = `${umidade} %`;
        document.getElementById('lblPressao').innerText = `${pressao} hPa`;
        document.getElementById('lblWifi').innerText = `${intensidadeWifi} dBm`;
    })
    .catch(err => console.error("Erro na simulação do robô:", err));
}

function iniciarSimulacao() {
    const frequenciaSegundos = parseInt(document.getElementById('frequencia').value) || 5;

    // Desabilita o botão Iniciar e habilita o botão Parar
    document.getElementById('btnIniciar').disabled = true;
    document.getElementById('btnParar').disabled = false;

    // Executa a primeira vez de imediato
    enviarLeitura();

    // Inicia o envio periódico
    intervaloSimulacao = setInterval(enviarLeitura, frequenciaSegundos * 1000);
}

function pararSimulacao() {
    if (intervaloSimulacao) {
        clearInterval(intervaloSimulacao);
        intervaloSimulacao = null;
    }

    // Reverte o estado dos botões
    document.getElementById('btnIniciar').disabled = false;
    document.getElementById('btnParar').disabled = true;
}