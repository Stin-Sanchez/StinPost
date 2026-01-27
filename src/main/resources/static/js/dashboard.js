document.addEventListener('DOMContentLoaded', function() {
    // Obtenemos los datos inyectados en el HTML a través de un atributo data-
    // o una variable global definida antes de cargar este script.
    // En este caso, buscaremos un elemento oculto que contenga el JSON.

    const chartDataElement = document.getElementById('chartData');
    if (!chartDataElement) return;

    try {
        const chartData = JSON.parse(chartDataElement.textContent);

        if(chartData && chartData.labels && chartData.labels.length > 0) {
            const ctx = document.getElementById('salesChart').getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: chartData.labels,
                    datasets: [{
                        label: 'Ventas ($)',
                        data: chartData.data,
                        backgroundColor: 'rgba(54, 162, 235, 0.6)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1,
                        borderRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    let label = context.dataset.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    if (context.parsed.y !== null) {
                                        label += new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(context.parsed.y);
                                    }
                                    return label;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: { borderDash: [2, 4] },
                            ticks: {
                                callback: function(value, index, values) {
                                    return '$' + value;
                                }
                            }
                        },
                        x: {
                            grid: { display: false }
                        }
                    }
                }
            });
        }
    } catch (e) {
        console.error("Error al parsear los datos del gráfico:", e);
    }
});