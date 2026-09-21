# 🌐 SearchPro Browser — Página Web y Descarga de APK con Dominio Personalizado

Este directorio contiene la página web de presentación y distribución lista para desplegar con tu **propio dominio personalizado** (ej. `www.tudominio.com`, `searchpro.app`) junto al archivo **APK descargable para Android** (`searchpro.apk`).

---

## 📁 Archivos incluidos

- `index.html`: Landing page moderna y adaptable para móviles y computadoras.
- `styles.css`: Estilos visuales con gradientes, soporte responsivo y tipografía moderna.
- `app.js`: Lógica interactiva para generar los registros DNS en vivo según el dominio ingresado y código QR para escanear y descargar directo al teléfono.
- `searchpro.apk`: Archivo instalable APK para Android de la aplicación.
- `icon.jpg`: Logotipo de SearchPro.
- `CNAME`: Archivo para vincular dominio personalizado en GitHub Pages / Cloudflare.
- `vercel.json`: Configuración optimizada para Vercel con cabeceras MIME de APK.
- `netlify.toml`: Configuración optimizada para Netlify.
- `firebase.json`: Configuración para Firebase Hosting.

---

## 🚀 Opciones de Despliegue con Dominio Personalizado (Gratis)

### Opción 1: Cloudflare Pages (La más recomendada, 100% gratuita y ultrarrápida)
1. Inicia sesión en [Cloudflare Dashboard](https://dash.cloudflare.com/).
2. Ve a **Compute (Workers) > Pages > Create a project**.
3. Selecciona **Upload assets** y arrastra todos los archivos de esta carpeta (`website/`).
4. Haz clic en **Deploy site**.
5. Ve a **Custom domains**, añade tu dominio (ej. `midominio.com`). Cloudflare configurará automáticamente el certificado SSL HTTPS sin costo.

---

### Opción 2: Vercel
1. Instala el CLI de Vercel (`npm i -g vercel`) o sube este repositorio a GitHub.
2. En [Vercel.com](https://vercel.com/), haz clic en **Add New > Project** y selecciona la carpeta del proyecto.
3. Una vez desplegado, ve a **Settings > Domains** y añade tu dominio.
4. En tu proveedor de dominios (GoDaddy, Namecheap, DonDominio, etc.), añade el registro:
   - **Tipo**: `CNAME`
   - **Nombre**: `www`
   - **Valor**: `cname.vercel-dns.com`
   - **Tipo**: `A`
   - **Nombre**: `@`
   - **Valor**: `76.76.21.21`

---

### Opción 3: GitHub Pages
1. Sube estos archivos a una rama llamada `gh-pages` o a la carpeta `docs/` de tu repositorio.
2. En los ajustes del repositorio en GitHub: **Settings > Pages**.
3. En **Custom domain**, ingresa tu dominio.
4. Edita el archivo `CNAME` con tu dominio exacto.

---

### Opción 4: Firebase Hosting
1. Ejecuta:
   ```bash
   firebase login
   firebase init hosting
   firebase deploy
   ```
2. En la consola de Firebase, ve a **Hosting > Connect domain** y sigue las instrucciones para añadir los registros DNS provistos.

---

## 📱 Descarga del APK

Cualquier usuario que visite tu sitio podrá descargar directamente el archivo APK pulsando el botón principal o escaneando el código QR en pantalla:
```
https://tudominio.com/searchpro.apk
```
