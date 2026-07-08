# Plano de Melhorias — Versículos do dia

Plano concreto, sequencial e executável via **Claude Code + Android Studio**. Cada fase é independente, termina com build verde e um commit em português. Prompts prontos para o Claude Code em cada etapa.

**Como usar:** abra o terminal na raiz do projeto, rode `claude` e cole o prompt da etapa. Valide no Android Studio (build + teste no emulador) antes de commitar.

> **Status em 08/07/2026:** Fases 0 a 6 concluídas (app na versão 1.6/versionCode 6, targetSdk 36, Billing 7.1.1, AdMob+UMP, Analytics/Crashlytics/In-App Review, widget, streak e marca d'água implementados). O guia detalhado de publicação no Play Console, com os pendentes reais que faltam (IDs de AdMob de produção, URL da política de privacidade), está em `docs/GUIA-PUBLICACAO-PLAY-STORE.md` — use-o em vez da Fase 6 abaixo, que foi o rascunho inicial dessa etapa. A Fase 7 (futuro) segue como backlog de ideias.

---

## Fase 0 — Higiene do projeto (30 min)

Objetivo: destravar as fases seguintes e limpar débitos.

1. Remover permissão `FOREGROUND_SERVICE` não utilizada do manifest.
2. Excluir os arquivos não usados em `data/repository/categories/` (mover as afirmações para `docs/afirmacoes-backup/` — serão úteis no futuro app de afirmações).
3. Remover os JSONs duplicados da raiz (manter só `app/src/main/assets/`).
4. Corrigir gating do tema: "Areia" (padrão) sempre grátis; premium = "Crepúsculo" apenas.
5. Extrair strings hardcoded para `res/values/strings.xml`.

> **Prompt Claude Code:** "Neste projeto Android: (1) remova a permissão FOREGROUND_SERVICE do AndroidManifest.xml; (2) mova os arquivos de data/repository/categories/ para docs/afirmacoes-backup/ e remova referências; (3) delete os arquivos .json da raiz do projeto que estão duplicados em app/src/main/assets; (4) em SettingsScreen.kt, torne o tema 'Areia' gratuito (exclusivo premium só 'Crepúsculo'); (5) extraia todas as strings de UI hardcoded dos arquivos em presentation/ para res/values/strings.xml usando stringResource(). Rode ./gradlew assembleDebug ao final."

**Commit:** `Limpeza: remove código morto, permissão não usada, JSONs duplicados e extrai strings para resources`

---

## Fase 1 — Monetização por anúncios (prioridade nº 1) (1 dia)

1. Criar app no [AdMob](https://admob.google.com), obter APPLICATION_ID real e criar 3 blocos: banner adaptativo, interstitial, rewarded.
2. Substituir o ID de teste no manifest (usar `manifestPlaceholders` com o ID em `local.properties`/BuildConfig para não commitar em texto puro, ou manter no manifest — o APPLICATION_ID não é segredo).
3. Integrar **UMP (User Messaging Platform)** para consentimento antes de carregar anúncios.
4. Banner adaptativo no lugar do placeholder da Home (`if (!isPremium)`).
5. Interstitial a cada 10 versículos navegados (contador no HomeViewModel), nunca no primeiro minuto de sessão.
6. Classe `AdManager` injetada via Hilt, com IDs de teste em build debug e reais em release.

> **Prompt Claude Code:** "Implemente AdMob neste app Compose: crie core/ads/AdManager.kt (singleton Hilt) com inicialização do MobileAds após consentimento via UMP SDK (adicione a dependência com.google.android.ump); use IDs de teste do Google em debug e IDs reais via BuildConfig em release. Substitua o placeholder de anúncio em HomeScreen.kt (bloco 'Ad Placeholder for Free Users') por um banner adaptativo AndroidView. Adicione interstitial exibido a cada 10 trocas de versículo para usuários free (contador no HomeViewModel, resetado por sessão). Anúncios nunca devem carregar se isPremium == true. Rode ./gradlew assembleDebug."

**Commit:** `Implementa monetização AdMob: banner adaptativo, interstitial e consentimento UMP`

---

## Fase 2 — Billing 7.x + novo freemium (meio dia)

1. Atualizar `billing-ktx` 6.1.0 → **7.1.1+** (obrigatório para updates na Play Store; ajustar API de `queryProductDetailsAsync` que mudou).
2. Criar no Play Console: teste grátis de 7 dias no plano anual + produto INAPP `vitalicio_14900` (lifetime R$ 149).
3. `BillingManager`: suportar INAPP + SUBS na restauração e no paywall (3 opções: mensal, anual c/ teste grátis em destaque, vitalício).
4. **Favoritos grátis** (limite 20 no free; ilimitado premium) — remover paywall do botão de favoritar.
5. Rewarded unlock: botão "Assistir anúncio para liberar esta categoria por 24h" nas categorias bloqueadas (salvar timestamp de expiração em SharedPreferences).

> **Prompt Claude Code:** "Atualize com.android.billingclient:billing-ktx para 7.1.1 e corrija breaking changes em BillingManager.kt. Adicione produto INAPP 'vitalicio_14900' ao query e à restauração (queryPurchasesAsync para INAPP e SUBS). No PaywallScreen, exiba 3 planos com o anual em destaque ('7 dias grátis'). Torne favoritos gratuitos com limite de 20 itens para não-premium (mostrar diálogo sugerindo premium ao atingir o limite). Implemente desbloqueio temporário de categoria por rewarded ad: ao tocar numa categoria bloqueada, ofereça 'Premium' ou 'Assistir anúncio (24h grátis)', salvando expiração em SharedPreferences via SettingsRepository. Rode ./gradlew assembleDebug."

**Commit:** `Atualiza Billing para 7.x, adiciona plano vitalício, teste grátis, favoritos free com limite e desbloqueio por rewarded ad`

---

## Fase 3 — Widget de tela inicial (1 dia)

Widget **Glance** (Compose para widgets) mostrando o versículo do dia; toque abre o app. Atualização diária via `WorkManager`. Versão free = categoria atual; premium = escolher categoria e tema do widget.

> **Prompt Claude Code:** "Crie um widget de home screen com Jetpack Glance (androidx.glance:glance-appwidget) que exibe o versículo do dia: texto + referência, visual limpo seguindo o tema Areia. Ao tocar, abre a MainActivity. Atualize o conteúdo 1x/dia via WorkManager usando ContentRepository.getNextContentToDisplay da categoria selecionada. Registre o receiver no manifest com android.appwidget.provider. Rode ./gradlew assembleDebug."

**Commit:** `Adiciona widget de tela inicial com versículo do dia (Glance + WorkManager)`

---

## Fase 4 — Analytics, Crashlytics e In-App Review (meio dia)

1. Firebase: Analytics + Crashlytics (google-services.json).
2. Eventos-chave: `paywall_visto`, `assinatura_iniciada`, `categoria_selecionada`, `versiculo_compartilhado`, `rewarded_assistido`, `widget_adicionado`.
3. **In-App Review API** disparada após: 3º dia de uso OU 5º favorito (nunca mais de 1x/mês).

> **Prompt Claude Code:** "Adicione Firebase Analytics e Crashlytics (plugins google-services e firebase-crashlytics; vou colocar o google-services.json em app/). Crie core/analytics/AnalyticsHelper.kt (Hilt) e registre eventos: paywall_visto, assinatura_iniciada, categoria_selecionada, versiculo_compartilhado, rewarded_assistido. Implemente In-App Review (com.google.android.play:review-ktx) disparada no 3º dia de uso ou 5º favorito, com controle de frequência em SharedPreferences. Rode ./gradlew assembleDebug."

**Commit:** `Adiciona Firebase Analytics, Crashlytics e avaliação in-app`

---

## Fase 5 — Crescimento orgânico (meio dia)

1. **Marca d'água** na imagem compartilhada: "Versículos do dia — baixe grátis" + ícone (free); premium sem marca.
2. **Streak:** contador de dias seguidos na Home ("🔥 5 dias com a Palavra"), persistido no Room/Prefs.
3. Deep link do texto compartilhado com link da loja (`https://play.google.com/store/apps/details?id=blog.robertotavares.cemversiculos`).

> **Prompt Claude Code:** "Em ShareUtils.kt: adicione marca d'água discreta na parte inferior da imagem compartilhada (nome do app + 'Baixe grátis na Play Store') apenas para usuários free; no compartilhamento de texto, acrescente o link da Play Store ao final. Implemente streak de dias consecutivos de uso (salvar última data de abertura e contador em SharedPreferences via SettingsRepository) e exiba na HomeScreen um chip '🔥 X dias com a Palavra'. Rode ./gradlew assembleDebug."

**Commit:** `Adiciona marca d'água de aquisição no compartilhamento e streak de dias consecutivos`

---

## Fase 6 — Preparação para a loja (1 dia, majoritariamente no Play Console)

1. Migrar targetSdk 35 → **36** (obrigatório para updates a partir de 31/08/2026): `./gradlew assembleDebug` + testar notificações/alarmes exatos no Android 16.
2. Gerar keystore de upload (se ainda não existir) e `bundleRelease`.
3. Play Console: ficha ASO completa (título/descrições do `RELATORIO-MERCADO-ASO.md` §5.2), screenshots (gerar mockups: emulador + molduras), Data Safety declarando AdMob, URL da política de privacidade, questionários de conteúdo.
4. Teste fechado com 12+ testadores por 14 dias (se conta pessoal pós-nov/2023) → produção com rollout gradual.
5. Configurar preços regionais e oferta de lançamento.

> **Prompt Claude Code:** "Atualize compileSdk e targetSdk para 36 neste projeto, ajuste dependências AndroidX/Compose BOM para versões compatíveis e corrija breaking changes de comportamento (especialmente alarmes exatos e notificações no Android 16). Rode ./gradlew bundleRelease e corrija erros."

**Commit:** `Migra para targetSdk 36 e prepara release para publicação na Play Store`

---

## Fase 7 (futuro) — Diferenciação premium

- **TTS**: ouvir o versículo (TextToSpeech nativo, premium).
- Pacotes sazonais (Quaresma, Advento) via JSON remoto (Firebase Remote Config) — conteúdo novo sem release.
- Custom store listing pt-PT; depois es-419 (mercado hispânico é 3x o Brasil).
- App irmão "Afirmações do dia" reutilizando o código + afirmações de `docs/afirmacoes-backup/`.

---

## Regras de commit (todas as fases)

- Um commit por fase concluída com build verde, mensagem em português no imperativo descrevendo o valor ("Implementa X", "Corrige Y").
- Nunca commitar: keystore, `local.properties`, `google-services.json` (adicionar ao `.gitignore`), IDs reais de AdMob se preferir mantê-los fora do repositório.

## Estimativa total

~5 dias úteis de trabalho até a versão pronta para a loja (Fases 0–6), com as maiores alavancas de receita (Fases 1–2) concluídas nos 2 primeiros dias.
