# Vaktim

Vaktim, namaz vakitlerini gosteren, sonraki vakti hesaplayan ve bildirim cubugunda surekli gorunur servis ile vakit takibi yapan Android uygulamasidir.

## Ozellikler

- Sehir/ulke/ilce bazli namaz vakti sorgulama
- Sonraki vakit ve kalan sure hesaplama
- Foreground service ile bildirim panelinde vakit gosterimi
- Gunluk ayet/meal cekme
- SOLID odakli katmanli mimari (UI / Domain / Data / Service / DI)

## Proje Gorselleri

### Ana Ekran (Temsili)

<img src="docs/images/preview-home.svg" alt="Vaktim Ana Ekran" width="320" />

### Bildirim Gorunumu (Temsili)

<img src="docs/images/preview-notification.svg" alt="Vaktim Bildirim" width="760" />

### Mimari Diyagram

<img src="docs/images/architecture.svg" alt="Vaktim Mimari" width="760" />

## APK Indirme

APK dosyalari repository icine eklenmez. Indirme icin GitHub Releases kullanilir.

- Releases sayfasi: `https://github.com/<owner>/<repo>/releases`
- Tek APK link formati: `https://github.com/<owner>/<repo>/releases/latest/download/vaktim-1.0.apk`

## Teknoloji Yigini

- Kotlin
- Jetpack Compose + Material 3
- ViewModel + StateFlow
- Retrofit + OkHttp + Gson
- Foreground Service + Notification RemoteViews

## Mimari Klasor Yapisi

```text
app/src/main/java/com/project/vaktim
|-- core
|-- data
|   |-- api
|   |-- local
|   |-- model
|   `-- repository
|-- di
|-- domain
|-- service
`-- ui
```

## Lokalde Derleme

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

## APK Yayin Akisi

Yeni APK olusturduktan sonra tek dosya olarak `vaktim-1.0.apk` adiyla GitHub Release asset'e yukleyin:

```powershell
.\gradlew.bat assembleDebug
Copy-Item app/build/outputs/apk/debug/app-debug.apk vaktim-1.0.apk -Force
```
