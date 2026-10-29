let chartLinhas = null;
let chartWifi = null;

// ==========================================================================
// 1. INICIALIZAÇÃO DOS GRÁFICOS (Chart.js)
// ==========================================================================
function inicializarGraficos() {
    // --- Gráfico de Linhas (Temperatura e Umidade) ---
    const ctxLinhas = document.getElementById('chartLinhas');
    if (ctxLinhas) {
        chartLinhas = new Chart(ctxLinhas.getContext('2d'), {
            type: 'line',
            data: {
                labels: [], // Horários das leituras
                datasets: [
                    {
                        label: 'Temperatura (°C)',
                        data: [],
                        borderColor: '#d97706', // Laranja do Card 1
                        backgroundColor: 'rgba(217, 119, 6, 0.2)',
                        tension: 0.3,
                        fill: true
                    },
                    {
                        label: 'Umidade (%)',
                        data: [],
                        borderColor: '#a8ccdf', // Azul do Card 2
                        backgroundColor: 'rgba(168, 204, 223, 0.2)',
                        tension: 0.3,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { ticks: { color: '#a8ccdf' }, grid: { color: '#1e4976' } },
                    y: { ticks: { color: '#a8ccdf' }, grid: { color: '#1e4976' } }
                },
                plugins: {
                    legend: { labels: { color: '#ffffff' } }
                }
            }
        });
    }

    // --- Medidor Semicircular (Gauge) para Wi-Fi ---
    const ctxWifi = document.getElementById('chartWifi');
    if (ctxWifi) {
        chartWifi = new Chart(ctxWifi.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Sinal', 'Restante'],
                datasets: [{
                    data: [0, 100],
                    backgroundColor: ['#059669', '#112840'], // Verde do Card 4 e Azul Escuro
                    borderWidth: 0
                }]
            },
            options: {
                rotation: -90,
                circumference: 180,
                cutout: '75%',
                responsive: true,
                maintainAspectRatio: false,
                plugins: { tooltip: { enabled: false } }
            }
        });
    }
}

// ==========================================================================
// 2. BUSCA DE DADOS NA API JAVA E ATUALIZAÇÃO DA TELA
// ==========================================================================
function carregarRegistros() {
    fetch('/api/registros')
        .then(res => res.json())
        .then(data => {
            if (!data || data.length === 0) return;

            // --- A. Atualiza Cards Superiores (Última Leitura do MySQL) ---
            const ultimo = data[0];

            const lblTemp = document.getElementById('lblTemp');
            if (lblTemp) lblTemp.innerText = `${ultimo.temperatura} °C`;

            const lblUmid = document.getElementById('lblUmid');
            if (lblUmid) lblUmid.innerText = `${ultimo.umidade} %`;

            const lblPressao = document.getElementById('lblPressao');
            if (lblPressao) lblPressao.innerText = `${ultimo.pressao} hPa`;

            // --- B. Preenche a Tabela de Logs (LIMITADO AOS 7 ÚLTIMOS REGISTROS) ---
            const tbody = document.getElementById('tabelaRegistros');
            if (tbody) {
                tbody.innerHTML = '';

                // O .slice(0, 7) filtra apenas os 7 primeiros/mais recentes itens
                data.slice(0, 7).forEach(reg => {
                    const tr = document.createElement('tr');
                    const horaFormatada = new Date(reg.timeStamp).toLocaleString('pt-BR');

                    tr.innerHTML = `
                        <td>${reg.idRegistro}</td>
                        <td>${reg.idDispositivoFk}</td>
                        <td>${horaFormatada}</td>
                        <td>${reg.temperatura} °C</td>
                        <td>${reg.umidade} %</td>
                        <td>${reg.pressao} hPa</td>
                        <td>${reg.intensidadeWifi} dBm</td>
                    `;
                    tbody.appendChild(tr);
                });
            }

            // --- C. Atualiza Gráfico de Linhas (Últimos 10 registros do mais antigo para o mais recente) ---
            const registrosOrdenados = [...data].reverse();
            const tempos = [];
            const temps = [];
            const umids = [];

            registrosOrdenados.slice(-10).forEach(reg => {
                tempos.push(new Date(reg.timeStamp).toLocaleTimeString('pt-BR'));
                temps.push(reg.temperatura);
                umids.push(reg.umidade);
            });

            if (chartLinhas) {
                chartLinhas.data.labels = tempos;
                chartLinhas.data.datasets[0].data = temps;
                chartLinhas.data.datasets[1].data = umids;
                chartLinhas.update();
            }

            // --- D. Atualiza Medidor Wi-Fi (Em %) ---
            if (ultimo && chartWifi) {
                const wifiDbm = ultimo.intensidadeWifi;
                // Converte a escala típica de dBm (-100 a -30) para percentagem (0% a 100%)
                let wifiPorcentagem = Math.min(Math.max(Math.round((wifiDbm + 100) * 1.42), 0), 100);

                chartWifi.data.datasets[0].data = [wifiPorcentagem, 100 - wifiPorcentagem];
                chartWifi.update();

                const txtWifi = document.getElementById('txtWifiValor');
                if (txtWifi) txtWifi.innerText = `${wifiPorcentagem}%`;
            }
        })
        .catch(err => console.error("Erro ao carregar dados da API:", err));
}

// ==========================================================================
// 3. INICIALIZAÇÃO AUTOMÁTICA
// ==========================================================================
window.onload = () => {
    inicializarGraficos();
    carregarRegistros();
    // Atualiza a cada 3 segundos em tempo real
    setInterval(carregarRegistros, 3000);
};