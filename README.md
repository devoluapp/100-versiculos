# Versículos do dia (100 Versículos)

Aplicativo Android nativo de versículos bíblicos diários, organizado por temas emocionais/espirituais, com notificações agendadas, favoritos, compartilhamento como imagem e modelo freemium (assinatura Premium via Google Play Billing).

- **Package:** `blog.robertotavares.cemversiculos`
- **Versão atual:** 1.6 (versionCode 6)
- **minSdk:** 26 (Android 8.0) · **target/compileSdk:** 36 (Android 16)
- **Idioma:** Português (Brasil)

## O que o app faz

O usuário escolhe uma categoria (ex.: Fé, Ansiedade, Gratidão) e navega por versículos em cartões deslizáveis. Pode favoritar, compartilhar como texto ou imagem, personalizar o tema visual e receber notificações em horários e frequências configuráveis. O conteúdo funciona 100% offline.

### Funcionalidades

- **Onboarding** inicial com configuração de preferências.
- **Home:** cartões de versículos com swipe, exibição inteligente (prioriza versículos nunca mostrados, depois os mostrados há mais tempo — `lastShownTimestamp`/`shownCount`).
- **11 categorias de versículos** (JSON em `assets/`, ~100 versículos cada, com referência, testamento e tags): Fé, Gratidão, Luto, Medo, Raiva, Oração, Perdão, Solidão, Tristeza, Ansiedade, Propósito — mais a categoria virtual "Favoritas".
- **Favoritos**, livre para todos com limite de 20 itens no plano gratuito. A troca de categoria/carregamento de conteúdo usa `flatMapLatest` por categoria (`HomeViewModel.kt`) para evitar que uma carga antiga (ex.: semeadura de uma categoria comum) sobrescreva a lista de Favoritas depois de uma troca rápida de categoria.
- **Compartilhamento** como imagem (captura do cartão via FileProvider) ou texto. O convite para baixar o app ("Baixe grátis na Play Store...") vai como **texto real** na mensagem de compartilhamento (link clicável), não desenhado na imagem — a imagem carrega só uma marca d'água discreta com o nome do app (`ShareUtils.kt`).
- **Notificações agendadas** via WorkManager (agendamento inexato, sem depender da permissão restrita `SCHEDULE_EXACT_ALARM`), com ações "Próximo", "Compartilhar" e "Excluir" direto na notificação (a notificação não some sozinha ao ser tocada, para dar tempo de terminar a leitura), e reagendamento após reboot (`BootReceiver`).
- **4 temas visuais** com variante clara/escura: Areia (padrão), Natureza, Oceano, Crepúsculo (exclusivo Premium).
- **Streak de dias consecutivos:** chip "🔥 X dias com a Palavra" na Home, exibido só a partir do 3º dia consecutivo de uso (`HomeViewModel.showStreakBadge`) para não aparecer trivialmente no dia 1 de uma instalação nova.
- **Tutorial** em modal na primeira utilização.
- **Widget de tela inicial** (Jetpack Glance) com o versículo do dia da categoria selecionada, cores seguindo o tema ativo (claro/escuro), fonte que se adapta ao tamanho do widget redimensionado pelo usuário, e amostra estática na tela de adicionar widget do sistema; toque abre a `MainActivity` já no versículo exibido. Atualizado 1x/dia via WorkManager.
- **Analytics, Crashlytics e avaliação in-app:** eventos de uso via Firebase Analytics (categoria selecionada, versículo compartilhado, paywall visto, assinatura iniciada, rewarded assistido, notificação exibida — `AnalyticsHelper.kt`), relatórios de falha via Firebase Crashlytics, e pedido de avaliação via Play In-App Review API a partir do 3º dia de uso ou do 5º favorito, respeitando um intervalo mínimo de 30 dias entre pedidos (`InAppReviewManager.kt`).
- **Política de Privacidade e Termos de Uso** acessíveis em Configurações → Sobre, abrindo a URL configurada em `strings.xml` (`privacy_policy_url`/`terms_of_use_url`).

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
│   ├── ads/AdManager.kt           # AdMob + UMP (consentimento, banner adaptativo, interstitial, rewarded)
│   ├── analytics/AnalyticsHelper.kt # Eventos Firebase Analytics
│   ├── billing/BillingManager.kt # Google Play Billing (assinaturas, acknowledge, restore)
│   ├── notification/              # NotificationDisplayer/Receiver/Helper/Worker, BootReceiver
│   ├── review/InAppReviewManager.kt # Play In-App Review API (elegibilidade por dias de uso/favoritos)
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
| Kotlin | 2.2.21 (KSP 2.2.21-2.0.5) |
| AGP | 8.13.2 · Gradle 8.14.5 |
| Jetpack Compose | BOM 2026.06.01, Material 3 |
| Navigation Compose | 2.9.8 |
| Room | 2.8.4 |
| Hilt | 2.58 (pinned — Dagger 2.59+ exige AGP 9/Gradle 9.1+, fora de escopo por ora) |
| Hilt Navigation Compose | 1.3.0 |
| Play Billing | 7.1.1 |
| Play Services Ads (AdMob) | 23.6.0 · banner adaptativo + interstitial + rewarded |
| User Messaging Platform (UMP) | 3.1.0 (consentimento AdMob) |
| Firebase BOM | 34.15.0 (Analytics + Crashlytics) |
| Play In-App Review | 2.0.2 (+ review-ktx) |
| Glance App Widget | 1.1.1 |
| WorkManager | 2.11.2 |
| Hilt Work | 1.3.0 |
| Gson | 2.11.0 |

## Como buildar

O app tem duas **product flavors** na dimensão `distribution` — `staging` e `production` —, cada uma com seus próprios IDs de anúncio AdMob embutidos via `BuildConfig` (ver `app/build.gradle.kts`). A flavor decide isso, não o build type: mesmo uma `productionDebug` usa os IDs de teste (o `AdManager` sempre prioriza `BuildConfig.DEBUG`), então IDs reais só saem em `productionRelease`.

| Flavor | IDs de anúncio | Uso |
|---|---|---|
| `staging` | Sempre os de teste oficiais do Google, hardcoded — nunca lê `local.properties` | Builds para as faixas de teste da Play Store (interno/fechado/aberto) e para instalar em dispositivo/emulador no dia a dia |
| `production` | Reais, de `local.properties` (preencha `admob.appId`/`admob.bannerId`/`admob.interstitialId`/`admob.rewardedId`) | **Só** a faixa de Produção da Play Store |

```bash
# Debug local (recomendado no dia a dia — nunca usa IDs reais de anúncio)
./gradlew installStagingDebug

# AAB para subir numa faixa de TESTE da Play Store (interno/fechado/aberto)
./gradlew bundleStagingRelease

# AAB para subir na faixa de PRODUÇÃO — único que deve conter os IDs reais de anúncio
./gradlew bundleProductionRelease
```

`./gradlew assembleDebug`/`bundleRelease` (sem o nome da flavor) ainda funcionam e continuam existindo como tarefas agregadoras do Gradle, mas builadas as duas flavors de uma vez — prefira sempre nomear a flavor explicitamente para não gerar (ou subir) o artefato errado por engano.

Requisitos: JDK 17, Android SDK 36. Abra no Android Studio (Ladybug+) e sincronize o Gradle.

### Assinatura de release

Para gerar um `.aab` assinado **pelo Android Studio** (Build → Generate Signed App Bundle / APK), o assistente pede o caminho da keystore e as senhas diretamente na interface — não depende de nada em `app/build.gradle.kts`. Na lista de variantes do assistente, escolha `stagingRelease` (faixas de teste) ou `productionRelease` (faixa de Produção).

`app/build.gradle.kts` também tem uma `signingConfig` opcional lendo de `local.properties` (`keystore.file`, `keystore.password`, `keystore.keyAlias`, `keystore.keyPassword`), útil só se você preferir buildar por linha de comando (`./gradlew bundleProductionRelease`) em vez do assistente. Sem preencher esses campos, o assistente do Android Studio continua funcionando normalmente — só o build por linha de comando de uma variante `*Release` falharia no passo de assinatura.

## Documentos do projeto

- `docs/GUIA-PUBLICACAO-PLAY-STORE.md` — guia passo a passo de tudo que precisa ser configurado no Google Play Console para publicar (App content, Data safety, monetização, ficha da loja, teste fechado), escrito a partir do comportamento real do app.
- `RELATORIO-MERCADO-ASO.md` — análise de mercado, SWOT, monetização e ASO (alguns números técnicos citados lá, como versão de Billing/target API, já foram superados pelo estado atual do app — ver seções acima).
- `PLANO-MELHORIAS.md` — plano de melhorias executável via Claude Code + Android Studio (fases 0–6 já concluídas).
- `versiculo_do_dia_privacy_policy.html` — política de privacidade (linkada em Configurações → Sobre; precisa estar hospedada em URL pública — ver guia de publicação).
- `termos_de_uso.html` — termos de uso, incluindo aviso de que o conteúdo é devocional/inspiracional e não substitui aconselhamento profissional (linkado em Configurações → Sobre).

## Observações conhecidas

- Nomes de categoria (ex.: "Gratidão") e de tema (ex.: "Crepúsculo") continuam como literais Kotlin em vez de string resources, pois funcionam como chaves de dados comparadas com o campo `categoria` dos JSONs, `SharedPreferences` e ViewModels/Repository — extrair para `strings.xml` quebraria esse casamento de valores caso o app seja localizado no futuro.
- Arquivos de afirmações (paz, foco, energia, gratidão, prosperidade, autoestima) foram removidos de `data/repository/categories/` por não serem referenciados em lugar nenhum; ficaram arquivados em `docs/afirmacoes-backup/` para eventual reaproveitamento futuro.
- `VersiculoWidgetWorker` chama `contentRepository.markAsShown()` no versículo exibido no widget, para manter a mesma lógica de rotação ("nunca repetir") usada na Home — abrir o app pode mostrar um versículo diferente do widget se a categoria selecionada mudar entre a última atualização do widget e a abertura do app.
- `privacy_policy_url`/`terms_of_use_url` em `strings.xml` ainda apontam para um domínio placeholder (`SEU-DOMINIO-AQUI.com`) — trocar pela URL real antes de publicar (ver `docs/GUIA-PUBLICACAO-PLAY-STORE.md`, seção 2).
