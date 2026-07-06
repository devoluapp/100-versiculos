# Versículos do dia (100 Versículos)

Aplicativo Android nativo de versículos bíblicos diários, organizado por temas emocionais/espirituais, com notificações agendadas, favoritos, compartilhamento como imagem e modelo freemium (assinatura Premium via Google Play Billing).

- **Package:** `blog.robertotavares.cemversiculos`
- **Versão atual:** 1.2 (versionCode 3)
- **minSdk:** 26 (Android 8.0) · **target/compileSdk:** 36 (Android 16)
- **Idioma:** Português (Brasil)

## O que o app faz

O usuário escolhe uma categoria (ex.: Fé, Ansiedade, Gratidão) e navega por versículos em cartões deslizáveis. Pode favoritar, compartilhar como texto ou imagem, personalizar o tema visual e receber notificações em horários e frequências configuráveis. O conteúdo funciona 100% offline.

### Funcionalidades

- **Onboarding** inicial com configuração de preferências.
- **Home:** cartões de versículos com swipe, exibição inteligente (prioriza versículos nunca mostrados, depois os mostrados há mais tempo — `lastShownTimestamp`/`shownCount`).
- **11 categorias de versículos** (JSON em `assets/`, ~100 versículos cada, com referência, testamento e tags): Fé, Gratidão, Luto, Medo, Raiva, Oração, Perdão, Solidão, Tristeza, Ansiedade, Propósito.
- **Favoritos** (categoria virtual "Favoritas"), livre para todos com limite de 20 itens no plano gratuito.
- **Compartilhamento** como imagem (captura do cartão via FileProvider) ou texto.
- **Notificações agendadas** via WorkManager (agendamento inexato, sem depender da permissão restrita `SCHEDULE_EXACT_ALARM`), ações "Próximo" e "Compartilhar" direto na notificação, e reagendamento após reboot (`BootReceiver`).
- **4 temas visuais** com variante clara/escura: Areia (padrão), Natureza, Oceano, Crepúsculo.
- **Tutorial** em modal na primeira utilização.
- **Widget de tela inicial** (Jetpack Glance) com o versículo do dia da categoria selecionada, cores seguindo o tema ativo; toque abre a `MainActivity`. Atualizado 1x/dia via WorkManager.

### Modelo freemium (estado atual)

| Recurso | Grátis | Premium |
|---|---|---|
| Categorias | Gratidão, Fé, Propósito (+ qualquer categoria desbloqueada por anúncio, por 24h) | Todas as 11, permanente |
| Favoritos | Liberado, limite de 20 itens (diálogo sugere Premium ao atingir o limite) | Ilimitado |
| Frequência de notificações | Até 5/dia | Ilimitada |
| Temas | Natureza, Oceano, Areia | + Crepúsculo |
| Anúncios | Banner adaptativo + interstitial a cada 10 versículos | Sem anúncios |

**Desbloqueio temporário de categoria por rewarded ad:** ao tocar numa categoria bloqueada em Configurações, o usuário escolhe entre assinar Premium ou assistir a um anúncio premiado para liberar aquela categoria por 24h (expiração salva em `SharedPreferences` via `SettingsRepository.saveCategoryUnlockExpiration`).

**Produtos (Google Play Billing 7.1.1):**
- `mensal_990` (SUBS, R$ 9,90/mês)
- `anual_5900` (SUBS, R$ 59,00/ano — destacado no paywall com badge "7 dias grátis")
- `vitalicio_14900` (INAPP não consumível, pagamento único — acesso Premium vitalício)

Compras de qualquer um dos três produtos acionam `saveIsPremium(true)` após acknowledge; `checkActivePurchases()` consulta SUBS e INAPP separadamente e combina os resultados (uma falha ou ausência em um tipo não derruba o status obtido pelo outro).

> ⚠️ **Badge "7 dias grátis":** é apenas um rótulo de UI no card do plano anual. Para o texto corresponder à realidade, configure uma fase de teste gratuito de 7 dias no base plan de `anual_5900` no Play Console — sem isso, o rótulo ficará incorreto (a cobrança seria imediata).

> ⚠️ O `admob.appId`/`admob.bannerId`/`admob.interstitialId`/`admob.rewardedId` de produção em `local.properties` (não versionado) ainda não foram preenchidos — builds de release caem nos IDs de teste do Google até que sejam configurados. Ver `AdManager.kt` e `app/build.gradle.kts`.

### Testar recursos Premium na revisão da Play Store

O app **não usa backdoor de código** para liberar Premium — essa abordagem foi removida por dois motivos: (1) qualquer pessoa pode descompilar o APK e encontrar uma string mágica, virando um bypass de monetização em produção; (2) as políticas do Google Play desencorajam funcionalidades ocultas que alteram o comportamento do app sem divulgação. O mecanismo oficial e recomendado pela documentação do Play Console é:

1. **License Testing** (Play Console → *Settings* → *License testing*): cadastre o e-mail Google do revisor (ou o seu, para gravar prints/vídeo de demonstração) na lista de testadores licenciados. Contas nessa lista completam a compra real de qualquer um dos três produtos (`mensal_990`/`anual_5900`/`vitalicio_14900`) pelo fluxo de billing de verdade, sem cobrança.
2. **App Access** (Play Console → *App content* → *App access* → *Manage*): ao enviar o app para revisão, adicione instruções em inglês explicando que não há login, mas que os recursos Premium ficam atrás de uma assinatura/compra única, e como completar a compra de teste (ex.: "Tap 'Seja Premium' → select any plan → complete the purchase; this Google account is registered as a license tester and will not be charged").

Isso garante que o revisor valide o fluxo de compra real (preço, termos, cancelamento) em vez de pular a tela, e não deixa nenhum mecanismo de bypass exposto no app publicado.

## Arquitetura

MVVM + separação em camadas (inspirada em Clean Architecture), UI 100% Jetpack Compose, injeção de dependência com Hilt.

```
app/src/main/java/blog/robertotavares/cemversiculos/
├── App.kt                        # Application (@HiltAndroidApp)
├── MainActivity.kt               # Single-activity, NavHost (Onboarding/Home/Settings/Paywall)
├── di/AppModule.kt               # Módulo Hilt (Room, repositórios, billing…)
├── core/
│   ├── ads/AdManager.kt           # AdMob + UMP (consentimento, banner adaptativo, interstitial)
│   ├── billing/BillingManager.kt # Google Play Billing (assinaturas, acknowledge, restore)
│   ├── notification/             # AlarmScheduler, NotificationReceiver, BootReceiver, Helper
│   ├── utils/                    # ShareUtils (imagem/texto), PreferenceManager, PermissionManager
│   └── widget/                   # VersiculoWidget (Glance), Receiver, Worker (WorkManager, 1x/dia)
├── data/
│   ├── local/                    # Room: AppDatabase, ContentDao, ContentItemEntity
│   └── repository/               # ContentRepositoryImpl (seed via JSON), SettingsRepositoryImpl
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
| Play Billing | 7.1.1 |
| Play Services Ads (AdMob) | 23.6.0 · banner adaptativo + interstitial |
| User Messaging Platform (UMP) | 3.1.0 (consentimento AdMob) |
| Glance App Widget | 1.1.1 |
| WorkManager | 2.11.2 |
| Hilt Work | 1.3.0 |
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

- Nomes de categoria (ex.: "Gratidão") e de tema (ex.: "Crepúsculo") continuam como literais Kotlin em vez de string resources, pois funcionam como chaves de dados comparadas com o campo `categoria` dos JSONs, `SharedPreferences` e ViewModels/Repository — extrair para `strings.xml` quebraria esse casamento de valores caso o app seja localizado no futuro.
- Arquivos de afirmações (paz, foco, energia, gratidão, prosperidade, autoestima) foram removidos de `data/repository/categories/` por não serem referenciados em lugar nenhum; ficaram arquivados em `docs/afirmacoes-backup/` para eventual reaproveitamento futuro.
- `VersiculoWidgetWorker` chama `contentRepository.markAsShown()` no versículo exibido no widget, para manter a mesma lógica de rotação ("nunca repetir") usada na Home — abrir o app pode mostrar um versículo diferente do widget se a categoria selecionada mudar entre a última atualização do widget e a abertura do app.
