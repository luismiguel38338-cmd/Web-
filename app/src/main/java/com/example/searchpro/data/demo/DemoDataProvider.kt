package com.example.searchpro.data.demo

import com.example.searchpro.domain.model.SearchItem
import java.time.LocalDate

/**
 * Provides representative demo items across multiple categories, statuses and dates.
 * These can be easily replaced by a real REST API endpoint or remote database.
 */
object DemoDataProvider {

    fun getSampleItems(): List<SearchItem> {
        val now = LocalDate.now()

        return listOf(
            SearchItem(
                id = 1L,
                title = "Arquitectura de Software Móvil",
                description = "Guía exhaustiva para implementar Clean Architecture y MVVM en aplicaciones Android modernas con Jetpack Compose y Coroutines.",
                category = "Tecnología",
                status = "Activo",
                date = now.minusDays(2),
                tags = listOf("android", "kotlin", "arquitectura", "clean-code"),
                relevance = 0.98
            ),
            SearchItem(
                id = 2L,
                title = "Especificación de API REST v2",
                description = "Documentación técnica detallada de endpoints, esquemas JSON y autenticación mediante JWT para servicios backend de búsqueda.",
                category = "Documentos",
                status = "Activo",
                date = now.minusDays(4),
                tags = listOf("api", "rest", "backend", "seguridad"),
                relevance = 0.92
            ),
            SearchItem(
                id = 3L,
                title = "Servicio de Búsqueda Inteligente",
                description = "Microservicio de indexación semántica en tiempo real con tolerancia a errores tipográficos y soporte multilingüe.",
                category = "Servicios",
                status = "Activo",
                date = now.minusDays(6),
                tags = listOf("motor", "indexing", "cloud", "algoritmos"),
                relevance = 0.95
            ),
            SearchItem(
                id = 4L,
                title = "Sistema de Diseño Material 3",
                description = "Librería de componentes, tokens de diseño de color dinámico, tipografías escalables y accesibilidad WCAG 2.1 AA.",
                category = "Diseño",
                status = "Activo",
                date = now.minusDays(10),
                tags = listOf("m3", "ui", "ux", "accesibilidad"),
                relevance = 0.90
            ),
            SearchItem(
                id = 5L,
                title = "Framework de Pruebas Automatizadas",
                description = "Módulo de pruebas locales con Robolectric, pruebas de regresión visual con Roborazzi y cobertura de flujos de navegación.",
                category = "Desarrollo",
                status = "Activo",
                date = now.minusDays(14),
                tags = listOf("testing", "robolectric", "calidad", "ci-cd"),
                relevance = 0.88
            ),
            SearchItem(
                id = 6L,
                title = "Manual de Seguridad y Cifrado",
                description = "Protocolos de almacenamiento cifrado local con Android Keystore, SQLCipher y gestión segura de tokens mediante DataStore.",
                category = "Documentos",
                status = "Activo",
                date = now.minusDays(18),
                tags = listOf("seguridad", "cifrado", "keystore", "privacidad"),
                relevance = 0.94
            ),
            SearchItem(
                id = 7L,
                title = "Motor de Reconocimiento de Voz",
                description = "Integración de Android SpeechRecognizer para dictado por voz y comandos de búsqueda por audio en tiempo real.",
                category = "Servicios",
                status = "Pendiente",
                date = now.minusDays(22),
                tags = listOf("audio", "voz", "speech", "ia"),
                relevance = 0.87
            ),
            SearchItem(
                id = 8L,
                title = "Guía de Optimización de Batería y Rendimiento",
                description = "Mejores prácticas para optimizar consumo de red, gestión eficiente de coroutines en segundo plano y renderizado Compose a 120 FPS.",
                category = "Tecnología",
                status = "Activo",
                date = now.minusDays(28),
                tags = listOf("rendimiento", "bateria", "profiler", "optimizacion"),
                relevance = 0.91
            ),
            SearchItem(
                id = 9L,
                title = "Prototipo de Interfaz de Búsqueda Adaptativa",
                description = "Diseños en Figma y pantallas interactivas para teléfonos plegables, tabletas y modo horizontal con Navigation Rail.",
                category = "Diseño",
                status = "Borrador",
                date = now.minusDays(35),
                tags = listOf("figma", "responsive", "tablets", "ux"),
                relevance = 0.85
            ),
            SearchItem(
                id = 10L,
                title = "Pipeline de Migración a Cloud SQL",
                description = "Estrategia para sincronizar caché local Room con base de datos en la nube manteniendo soporte completo sin conexión.",
                category = "Desarrollo",
                status = "Borrador",
                date = now.minusDays(45),
                tags = listOf("database", "cloud", "sync", "offline"),
                relevance = 0.89
            ),
            SearchItem(
                id = 11L,
                title = "Contrato de Nivel de Servicio (SLA)",
                description = "Definición formal de métricas de disponibilidad 99.9%, latencia menor a 200ms y tiempos de respuesta de soporte técnico.",
                category = "Documentos",
                status = "Archivado",
                date = now.minusDays(60),
                tags = listOf("legal", "contrato", "sla", "soporte"),
                relevance = 0.75
            ),
            SearchItem(
                id = 12L,
                title = "Servicio de Notificaciones Push Segmentadas",
                description = "Infraestructura para envío de alertas personalizadas según temas de interés y categorías de búsqueda del usuario.",
                category = "Servicios",
                status = "Archivado",
                date = now.minusDays(75),
                tags = listOf("push", "fcm", "notificaciones", "engagement"),
                relevance = 0.78
            ),
            SearchItem(
                id = 13L,
                title = "Kit de Internacionalización Multi-idioma",
                description = "Estructura modular para soportar español, inglés, francés y añadir nuevos idiomas sin recompilar el código fuente.",
                category = "Tecnología",
                status = "Activo",
                date = now.minusDays(3),
                tags = listOf("i18n", "localizacion", "idiomas", "traduccion"),
                relevance = 0.96
            ),
            SearchItem(
                id = 14L,
                title = "Catálogo de Iconos y Recursos Gráficos",
                description = "Colección de vectores optimizados para resolución multi-pantalla, modo claro/oscuro y contrastes visuales dinámicos.",
                category = "Diseño",
                status = "Activo",
                date = now.minusDays(8),
                tags = listOf("vectores", "iconos", "branding", "graficos"),
                relevance = 0.86
            ),
            SearchItem(
                id = 15L,
                title = "Módulo de Diagnóstico y Telemetría Segura",
                description = "Herramienta interna para medir tiempos de latencia en consultas y registrar errores no fatales respetando la privacidad del usuario.",
                category = "Desarrollo",
                status = "Pendiente",
                date = now.minusDays(12),
                tags = listOf("metricas", "logging", "analitica", "monitoreo"),
                relevance = 0.84
            ),
            SearchItem(
                id = 16L,
                title = "Informe Trimestral de Calidad y Usabilidad",
                description = "Resumen de pruebas de campo con usuarios, mapas de calor en la barra de búsqueda y porcentaje de búsquedas exitosas.",
                category = "Documentos",
                status = "Activo",
                date = now.minusDays(20),
                tags = listOf("reporte", "usabilidad", "kpi", "analisis"),
                relevance = 0.82
            ),
            SearchItem(
                id = 17L,
                title = "Microservicio de Resumen y Categorización",
                description = "Procesador en segundo plano que clasifica automáticamente elementos entrantes mediante algoritmos de texto.",
                category = "Servicios",
                status = "Activo",
                date = now.minusDays(1),
                tags = listOf("nlp", "clasificacion", "inteligencia", "texto"),
                relevance = 0.97
            ),
            SearchItem(
                id = 18L,
                title = "Biblioteca de Componentes UI Reactivos",
                description = "Colección de campos de búsqueda con debounce, chips interactivos de filtros y tarjetas animadas con Material Design.",
                category = "Desarrollo",
                status = "Activo",
                date = now.minusDays(5),
                tags = listOf("compose", "componentes", "ui", "widgets"),
                relevance = 0.93
            )
        )
    }
}
