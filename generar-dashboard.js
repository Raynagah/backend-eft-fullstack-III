const fs = require('fs');

// Tu lista de microservicios según tu estructura de carpetas
const carpetas = [
    { id: 'api-gateway', nombre: '🌐 API Gateway' },
    { id: 'bff', nombre: '🔗 BFF' },
    { id: 'eureka-server', nombre: '📡 Eureka Server' },
    { id: 'gestion-mascotas', nombre: '🐾 Gestión Mascotas' },
    { id: 'ms-geolocalizacion', nombre: '📍 MS Geolocalización' },
    { id: 'ms-motor-coincidencias', nombre: '🧩 MS Motor Coincidencias' },
    { id: 'notificaciones', nombre: '🔔 Notificaciones' },
    { id: 'usuarios', nombre: '👥 Usuarios' }
];

let itemsHtml = '';

carpetas.forEach(ms => {
    // JaCoCo siempre genera un CSV con los datos crudos junto al HTML
    const csvPath = `./${ms.id}/target/site/jacoco/jacoco.csv`;
    let porcentaje = 'N/A';
    let color = '#95a5a6'; // Gris si no hay test o no se ha compilado
    
    if (fs.existsSync(csvPath)) {
        const lines = fs.readFileSync(csvPath, 'utf-8').split('\n');
        let missed = 0;
        let covered = 0;
        
        // Sumamos las instrucciones perdidas y cubiertas (saltamos la línea 0 que es la cabecera)
        for (let i = 1; i < lines.length; i++) {
            const parts = lines[i].split(',');
            if (parts.length >= 5) {
                missed += parseInt(parts[3]) || 0; // Columna INSTRUCTION_MISSED
                covered += parseInt(parts[4]) || 0; // Columna INSTRUCTION_COVERED
            }
        }
        
        const total = missed + covered;
        if (total > 0) {
            const percent = Math.round((covered / total) * 100);
            porcentaje = `${percent}%`;
            
            // Semáforo de colores
            if (percent >= 80) color = '#2ecc71';      // Verde (Excelente)
            else if (percent >= 50) color = '#f1c40f'; // Amarillo (Regular)
            else color = '#e74c3c';                    // Rojo (Peligro)
        }
    }

    // Armamos el botón para este microservicio
    itemsHtml += `
        <li>
            <a href="./${ms.id}/target/site/jacoco/index.html" target="_blank" style="display: flex; justify-content: space-between; align-items: center;">
                <span>${ms.nombre}</span>
                <span style="background-color: ${color}; color: white; padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 14px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    ${porcentaje}
                </span>
            </a>
        </li>`;
});

// Plantilla HTML final
const html = `<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard de Cobertura - Fullstack III</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; color: #333; margin: 0; padding: 40px 20px; display: flex; justify-content: center; }
        .container { background: #ffffff; max-width: 600px; width: 100%; padding: 30px; border-radius: 12px; box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; text-align: center; margin-bottom: 5px; }
        p { text-align: center; color: #7f8c8d; margin-bottom: 25px; font-size: 14px; }
        ul { list-style: none; padding: 0; margin: 0; }
        li { margin-bottom: 12px; }
        a { display: block; text-decoration: none; background-color: #f8f9fa; color: #2980b9; font-size: 16px; font-weight: 600; padding: 15px 20px; border: 1px solid #e9ecef; border-radius: 8px; transition: all 0.3s ease; }
        a:hover { background-color: #e3f2fd; border-color: #bbdefb; color: #1565c0; transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,0.05); }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 Cobertura Global</h1>
        <p>Actualiza los tests y corre <code>node generar-dashboard.js</code></p>
        <ul>
            ${itemsHtml}
        </ul>
    </div>
</body>
</html>`;

fs.writeFileSync('index-cobertura.html', html);
console.log('✅ ¡Dashboard generado con éxito! Abre el archivo index-cobertura.html para ver tus porcentajes.');