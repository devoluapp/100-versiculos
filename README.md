# Versículos do dia (100 Versículos)

Aplicativo Android nativo de versículos bíblicos diários, organizado por temas emocionais/espirituais, com notificações agendadas, favoritos, compartilhamento como imagem e modelo freemium (assinatura Premium via Google Play Billing).

- **Package:** `blog.robertotavares.cemversiculos`
- **Versão atual:** 1.2 (versionCode 3)
- **minSdk:** 26 (Android 8.0) · **target/compileSdk:** 35 (Android 15)
- **Idioma:** Português (Brasil)

## O que o app faz

O usuário escolhe uma categoria (ex.: Fé, Ansiedade, Gratidão) e navega por versículos em cartões deslizáveis. Pode favoritar, compartilhar como texto ou imagem, personalizar o tema visual e receber notificações em horários e frequências configuráveis. O conteúdo funciona 100% offline.

### Funcionalidades

- **Onboarding** inicial com configuração de preferências.
- **Home:** cartões de versículos com swipe, exibição inteligente (prioriza versículos nunca mostrados, depois os mostrados há mais tempo — `lastShownTimestamp`/`shownCount`).
- **11 categorias de versículos** (JSON em `assets/`, ~100 versículos cada, com referência, testamento e tags): Fé, Gratidão, Luto, Medo, Raiva, Oração, Perdão, Solidão, Tristeza, Ansiedade, Propósito.
- **Favoritos** (categoria virtual "Favoritas").
- **Compartilhamento** como imagem (captura do cartão via FileProvider) ou texto.
- **Notificações agendadas** com alarmes exatos (`SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`), ações "Próximo" e "Compartilhar" direto na notificação, e reagendamento após reboot (`BootReceiver`).
- **4 temas visuais** com variante clara/escura: Areia (padrão), Natureza, Oceano, Crepúsculo.
- **Tutorial** em modal na primeira utilização.

### Modelo freemium (estado atual)

| Recurso | Grátis | Premium |
|---|---|---|
| Categorias | Gratidão, Fé, Propósito | Todas as 11 |
| Favoritos | Bloqueado (abre paywall) | Liberado |
| Frequência de notificações | Até 5/dia | Ilimitada |
| Temas | Natureza, Oceano | + Areia, Crepúsculo |
| Anúncios | Placeholder de banner (não implementado) | Sem anúncios |

**Assinaturas (Google Play Billing 6.1):** `mensal_990` (R$ 9,90/mês) e `anual_5900` (R$ 59,00/ano), com acknowledge de compra e restauração via `queryPurchasesAsync`.

> ⚠️ O AdMob está declarado no manifest com o **APPLICATION_ID de teste** do Google e nenhum formato de anúncio foi implementado (apenas placeholder visual na Home). Ver `PLANO-MELHORIAS.md`.

## Arquitetura

MVVM + separação em camadas (inspirada em Clean Architecture), UI 100% Jetpack Compose, injeção de dependência com Hilt.

```
app/src/main/java/blog/robertotavares/cemversiculos/
├── App.kt                        # Application (@HiltAndroidApp)
├── MainActivity.kt               # Single-activity, NavHost (Onboarding/Home/Settings/Paywall)
├── di/AppModule.kt               # Módulo Hilt (Room, repositórios, billing…)
├── core/
│   ├── billing/BillingManager.kt # Google Play Billing (assinaturas, acknowledge, restore)
│   ├── notification/             # AlarmScheduler, NotificationReceiver, BootReceiver, Helper
│   └── utils/                    # ShareUtils (imagem/texto), PreferenceManager, PermissionManager
├── data/
│   ├── local/                    # Room: AppDatabase, ContentDao, ContentItemEntity
│   └── repository/               # ContentRepositoryImpl (seed via JSON), SettingsRepositoryImpl
│       └── categories/           # ⚠️ Afirmações hardcoded (paz, foco, energia…) — código legado não referenciado
├── domain/repository/            # Interfaces ContentRepository, SettingsRepository
└── presentation/
    ├── home/                     # HomeScreen (544 linhas) + HomeViewModel
    ├── settings/                 # SettingsScreen + ViewModel
    ├── paywall/                  # PaywallScreen + ViewModel
    ├── onboarding/               # OnboardingScreen + ViewModel
    ├── navigation/Screen.kt
    └── theme/Theme.kt            # 4 temas x claro/escuro
```

**Fluxo de dados:** JSONs em `assets/` → seed no Room na primeira abertura da categoria → DAO expõe `Flow` → ViewModel ordena/deduplica → Compose. Preferências (categoria, tema, premium, onboarding) em `SharedPreferences`.

## Stack

| Tecnologia | Versão |
|---|---|
| Kotlin | 2.0.21 (KSP) |
| AGP | 8.7.3 · Gradle 9 |
| Jetpack Compose | BOM 2024.11.00, Material 3 |
| Navigation Compose | 2.8.4 |
| Room | 2.6.1 |
| Hilt | 2.52 |
| Play Billing | 6.1.0 |
| Play Services Ads (AdMob) | 23.6.0 (não integrado na UI) |
| Gson | 2.11.0 |

## Como buildar

```bash
# Debug
./gradlew assembleDebug

# Release (minify + shrink habilitados; requer keystore configurada)
./gradlew bundleRelease
```

Requisitos: JDK 17, Android SDK 35. Abra no Android Studio (Ladybug+) e sincronize o Gradle.

## Documentos do projeto

- `RELATORIO-MERCADO-ASO.md` — análise de mercado, SWOT, monetização, guia de publicação na Play Store e ASO.
- `PLANO-MELHORIAS.md` — plano de melhorias executável via Claude Code + Android Studio.
- `versiculo_do_dia_privacy_policy.html` — política de privacidade.

## Observações conhecidas

- Strings de UI hardcoded em Kotlin (apenas `app_name` em `strings.xml`) — dificulta localização.
- JSONs de conteúdo duplicados na raiz do projeto e em `app/src/main/assets/`.
- Arquivos de afirmações em `data/repository/categories/` não são usados (possível expansão futura ou código morto).
- Tema "Areia" é o padrão e ao mesmo tempo exclusivo Premium (inconsistência de gating).
