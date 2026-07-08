# Guia de publicação do "Versículos do dia" na Google Play Store

> Documento vivo, escrito a partir do estado real do código em 08/07/2026 (versionCode 6, versionName 1.6). Sempre que o app mudar de forma relevante para a loja (novo SDK, nova permissão, novo produto de assinatura, nova categoria de anúncio), atualize este guia junto.

Este guia cobre **tudo que precisa ser configurado no Google Play Console** para publicar o app com o menor risco possível de rejeição, na ordem em que você deve fazer. Cada seção diz exatamente o que preencher, com base no que o app realmente faz — não em suposições genéricas.

**Antes de começar, resolva os 3 pendentes de código/config abaixo — sem eles, alguns passos deste guia não podem ser concluídos:**

| Pendente | Onde | Por quê é bloqueante |
|---|---|---|
| IDs reais do AdMob (`admob.appId`, `admob.bannerId`, `admob.interstitialId`, `admob.rewardedId`) | `local.properties` (não versionado) | Sem eles, o build de release usa os IDs de teste do Google — a Play Store rejeita/suspende apps publicados com anúncios de teste em produção. Ver `app/build.gradle.kts:19-30` e a seção 5 deste guia. |
| URL pública da Política de Privacidade e dos Termos de Uso | `app/src/main/res/values/strings.xml` → `privacy_policy_url` e `terms_of_use_url` (hoje com placeholder `SEU-DOMINIO-AQUI.com`) | O Play Console exige uma URL real e funcional no campo "Política de privacidade"; o app também abre essa mesma URL em Configurações → Sobre. Ver seção 2. |
| Keystore de upload | Fora do repo (nunca commitar) | Sem uma keystore, não é possível gerar o AAB assinado para subir ao Console. Ver seção 6. |

---

## 1. Conta de desenvolvedor Google Play

1. Acesse [play.google.com/console](https://play.google.com/console/) e crie a conta (taxa única de US$ 25).
2. Complete a **verificação de identidade** (documento + selfie ou verificação de organização com D-U-N-S, conforme o tipo de conta). Contas não verificadas não conseguem publicar em produção.
3. Se a conta for **pessoal e foi criada depois de novembro de 2023**, o Google exige que você **complete um teste fechado com pelo menos 12 testadores por 14 dias corridos** antes de liberar o acesso à produção pela primeira vez (ver seção 7). Providencie isso com antecedência — é o item que mais atrasa publicações de contas novas.

---

## 2. Antes da ficha: hospedar Política de Privacidade e Termos de Uso

Dois arquivos já prontos na raiz do repositório:
- `versiculo_do_dia_privacy_policy.html`
- `termos_de_uso.html`

Eles precisam estar acessíveis por uma **URL pública HTTPS** (o Play Console não aceita link para um arquivo local ou repositório privado). Opções, da mais simples à mais robusta:

- **GitHub Pages** de um repositório público (pode ser um repo só para esses dois HTMLs, se você preferir manter o código-fonte privado).
- Uma página no seu blog (`robertotavares.blog`), publicando o HTML como está ou convertendo o conteúdo para o CMS do blog.
- Qualquer hospedagem estática (Netlify, Vercel, Firebase Hosting — já que o projeto já usa Firebase).

Depois de publicar, **atualize as duas URLs em `app/src/main/res/values/strings.xml`** (`privacy_policy_url` e `terms_of_use_url`) para os endereços reais, e rode `./gradlew assembleDebug` para confirmar que compila. Sem isso, o link "Política de Privacidade" em Configurações → Sobre abre um domínio inexistente — e o Play Console vai rejeitar a URL de política de privacidade no passo 4.4 abaixo.

Guarde as duas URLs finais à mão: você vai colá-las em pelo menos três lugares (Play Console → App content → Privacy policy; dentro do app; e opcionalmente no rodapé da ficha da loja).

---

## 3. Criar o app no Play Console

1. **Todos os apps → Criar app**.
2. **Nome do app:** `Versículos do dia` (até 30 caracteres — ver sugestões de título na seção 8 se quiser incluir palavras-chave).
3. **Idioma padrão:** Português (Brasil).
4. **Tipo:** App (não jogo).
5. **Gratuito ou pago:** **Gratuito** (a monetização é via assinatura/compra dentro do app, não via preço de listagem — pagar para baixar e ainda ter assinatura seria incomum e prejudicaria a conversão).
6. Aceite as declarações padrão (Play Console Developer Program Policies e leis de exportação dos EUA).

---

## 4. App content (Conteúdo do app) — todas as declarações obrigatórias

Menu **Play Console → [seu app] → Política → App content**. Preencha cada questionário nesta ordem:

### 4.1 Privacy policy
Cole a URL pública da política de privacidade (seção 2). Deve corresponder ao que está publicado — o Google efetivamente abre o link e compara o conteúdo com o comportamento real do app durante a revisão.

### 4.2 Ads (Anúncios)
**Declare "Sim, meu app contém anúncios."** O app usa Google AdMob (banner adaptativo, intersticial a cada 10 versículos, e um anúncio recompensado opcional para desbloqueio de categoria) — ver `AdManager.kt`. Declarar "Não" quando há anúncios é uma das causas mais comuns de suspensão por violação de política.

### 4.3 App access (Acesso ao app)
Não há login nem restrição de acesso — mas os recursos Premium (todas as categorias, temas exclusivos, notificações ilimitadas, sem anúncios) ficam atrás de uma compra. Selecione **"Todas as funcionalidades são acessíveis sem restrições especiais"** só se você não precisar que o revisor teste o fluxo de compra; caso contrário (recomendado, para o revisor validar a tela de paywall de verdade), selecione **"Algumas funcionalidades são restritas"** e forneça instruções em **inglês** para o revisor, por exemplo:

```
This app does not require login. All Bible content is free.
Premium features (all categories, extra themes, unlimited notifications,
no ads) are unlocked via a real Google Play Billing purchase — there is
no promo/demo code.

To test the purchase flow without being charged:
1. Add the reviewer's Google account as a License Tester in
   Play Console → Setup → License testing.
2. Open the app → tap "SEJA PREMIUM" (top-left button on the home
   screen) → choose any plan (monthly, annual or lifetime) → complete
   the purchase. Accounts registered as license testers complete the
   real Billing flow but are never charged.
```

> Isso corresponde exatamente ao mecanismo já documentado em `README.md` — o app **não tem** nenhum código de bypass/backdoor para Premium, de propósito (ver observação no próprio README).

### 4.4 Content ratings (Classificação de conteúdo — questionário IARC)
Preencha o questionário com base no conteúdo real do app:

| Pergunta do questionário | Resposta | Justificativa |
|---|---|---|
| Violência | Não | Nenhum conteúdo violento |
| Conteúdo sexual/nudez | Não | Nenhum |
| Linguagem imprópria/palavrões | Não | Texto bíblico e UI padrão |
| Substâncias controladas (drogas/álcool/tabaco) | Não | Nenhuma referência |
| Jogos de azar/apostas simuladas | Não | Nenhum |
| Conteúdo gerado pelo usuário compartilhado publicamente | Não | Favoritos são privados; não há chat, comentário ou postagem pública |
| Interação entre usuários | Não | Não há chat, multiplayer ou perfis públicos |
| Compartilha localização | Não | App não usa nenhuma permissão de localização |
| Compras dentro do app | **Sim** | Assinaturas mensal/anual + compra vitalícia via Google Play Billing |
| Anúncios | **Sim** | AdMob (ver 4.2) |

Resultado esperado: classificação livre/baixa em todas as classificações regionais (ESRB "Everyone", PEGI 3, ClassInd Livre etc.). Não existe uma pergunta específica sobre "conteúdo religioso" que eleve a classificação — apps devocionais/bíblicos são rotineiramente classificados como livres para todas as idades.

### 4.5 Target audience and content (Público-alvo)
**Recomendação: selecione apenas faixas etárias a partir de 18 anos, e responda "Não" para "este app é direcionado principalmente a crianças".**

Motivo: o app tem anúncios personalizados (AdMob), compras dentro do app e um campo de texto livre (nome do usuário) — combinar isso com qualquer faixa etária infantil (abaixo de 13) aciona automaticamente as políticas de **Famílias** do Google Play (regras muito mais restritas de anúncios, obrigatoriedade de rótulo "Projetado para famílias", proibição de certos SDKs). Como o conteúdo (versículos sobre luto, ansiedade, raiva) também não é pensado para crianças, não há motivo para incluir essas faixas.

### 4.6 News app, COVID-19 contact tracing, Data safety, Government app
- **News app:** Não.
- **COVID-19:** Não se aplica.
- **Government app:** Não.
- **Data safety:** ver seção 5 completa abaixo (é a mais importante e mais sujeita a erro).

### 4.7 Ads content rating / Advertising ID
Confirme que o app declara uso do **Advertising ID** (obrigatório desde que o app usa AdMob) quando o Play Console perguntar sobre o uso desse identificador — normalmente isso é auto-preenchido ao declarar "Ads = Sim" e detectar a dependência do Google Mobile Ads SDK no seu AAB.

---

## 5. Data safety (Segurança dos dados) — mapeamento completo

Esta seção do Play Console (**Política → App content → Data safety**) pergunta, para cada tipo de dado, se é **coletado**, se é **compartilhado com terceiros** e a **finalidade**. Abaixo está o mapeamento real, com base no código (`PreferenceManager.kt`, `AnalyticsHelper.kt`, `AdManager.kt`, `BillingManager.kt`, `ShareUtils.kt`):

| Categoria Play Console | Tipo de dado | Coletado? | Compartilhado? | Finalidade | Por quê |
|---|---|---|---|---|---|
| Informações pessoais | Nome | **Não** | — | — | Fica só em `SharedPreferences` local (`user_name`); nenhuma chamada de rede o transmite. "Coletado", na definição do Google, significa transmitido para fora do aparelho — isso nunca acontece aqui. |
| Informações financeiras | Histórico de compras | **Sim** | Não | Analytics | O evento `assinatura_iniciada` (Firebase Analytics) envia o `productId` do plano tocado — ver `AnalyticsHelper.kt:23-27` e `PaywallViewModel.kt:27`. Não envia valor pago nem dado de cartão. |
| Identificadores do dispositivo | ID de publicidade, ID de instalação | **Sim** | **Sim** (com o AdMob, para anúncios) | Publicidade, Analytics | AdMob usa o Advertising ID para exibir/medir anúncios (finalidade própria do Google/anunciantes = "compartilhado", não só "coletado"). Firebase usa um ID de instalação para analytics/crash (não compartilhado com terceiros além do próprio Google como processador). |
| Atividade no app | Interações no app | **Sim** | Não | Analytics | Eventos `categoria_selecionada`, `versiculo_compartilhado`, `paywall_visto`, `rewarded_assistido`, `notificacao_exibida` — ver `AnalyticsHelper.kt` completo. |
| Relatórios de falhas | Logs de erro | **Sim** | Não | Diagnóstico de app (Crashlytics) | Padrão do Firebase Crashlytics, sempre ativo (`app/build.gradle.kts:10,147`). |
| Fotos e vídeos | — | **Não** | — | — | A imagem gerada para compartilhar fica em cache privado (`cacheDir/images/`) e só é lida pelo app de destino que o próprio usuário escolhe no share sheet — nunca é enviada a um servidor do desenvolvedor (`ShareUtils.kt`, `file_paths.xml`). |
| Localização | — | **Não** | — | — | Nenhuma permissão de localização declarada no `AndroidManifest.xml`. |
| Contatos, mensagens, calendário, arquivos, áudio | — | **Não** | — | — | Nenhuma API relacionada é usada em nenhum lugar do código. |

Passos práticos no formulário:
1. Marque **"Sim, os dados são coletados e/ou compartilhados"**.
2. Para cada tipo marcado "Sim" na tabela acima, escolha as opções correspondentes de finalidade (Analytics / Publicidade ou marketing / Funcionalidade do app) e se é **opcional** (o nome de usuário é opcional para preencher, mas como não é "coletado" no sentido do Google, nem precisa entrar aqui) ou **obrigatório**.
3. Marque **"Os dados são criptografados em trânsito"** — é verdade para todo tráfego HTTPS do Firebase/AdMob/Billing.
4. **"Você oferece um meio para que os usuários solicitem que os dados deles sejam excluídos?" (opcional, mas responda Sim):** o Google exige que o link cumpra 3 requisitos — mencionar o nome do app/desenvolvedor, descrever os passos para solicitar a exclusão, e especificar quais dados são excluídos vs. retidos (e por quanto tempo). Isso já está escrito na política de privacidade, na seção **"7.1 Como solicitar a exclusão dos seus dados"** (`versiculo_do_dia_privacy_policy.html#exclusao-de-dados`). Cole no campo **URL para exclusão de dados** a URL pública final + esse fragmento, ex.: `https://SEU-DOMINIO-AQUI.com/versiculos-do-dia/privacidade#exclusao-de-dados`. Não é preciso nenhum formulário/dashboard separado — como o app não tem conta de usuário, uma página que explica "desinstalar apaga os dados locais; para analytics/crash, peça por e-mail" satisfaz o requisito.
5. Use o **assistente automático do Play Console para SDKs conhecidos** (aparece um atalho "Ver orientação do SDK" para AdMob/Firebase/Billing) para conferir se as respostas batem com o que esses SDKs declaram oficialmente — ele pode sugerir campos adicionais que o Google atualizar depois da escrita deste guia.

---

## 6. Assinatura do app e build de release

1. **Gerar keystore de upload** (se ainda não existir): 
   ```bash
   keytool -genkeypair -v -keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
   Guarde o arquivo `.jks` e a senha em local seguro **fora do repositório** (já coberto por `*.jks` no `.gitignore`) — se perdê-lo depois de já ter enviado a primeira versão, você precisa abrir um processo de suporte com o Google para recuperar o app.
2. Configure `signingConfigs` no `app/build.gradle.kts` (ou use o assistente do Android Studio: **Build → Generate Signed App Bundle**) apontando para essa keystore.
3. **Ative o Play App Signing** (é o padrão para apps novos): o Google gerencia a chave final de assinatura, e você só precisa da chave de upload acima.
4. Preencha os IDs reais de AdMob em `local.properties` antes de gerar o release (ver tabela de pendentes no topo deste guia) — confira lendo `app/build.gradle.kts:19-30,67-71`: sem isso, o release usa IDs de teste do Google.
5. Gere o Android App Bundle:
   ```bash
   ./gradlew bundleRelease
   ```
   O arquivo sai em `app/build/outputs/bundle/release/app-release.aab`.
6. Rode um teste rápido local antes de subir: instale o `.aab` via `bundletool` ou gere um APK de teste (`./gradlew assembleRelease`) e confirme no dispositivo físico que: anúncios reais aparecem (não os de teste), a compra abre a tela de billing corretamente, e as notificações continuam funcionando.

---

## 7. Faixas de teste e lançamento gradual

1. **Play Console → Testar e lançar → Testes → Teste fechado.**
2. Crie uma lista de e-mails com pelo menos **12 testadores** (familiares, amigos, comunidade da igreja, grupos de WhatsApp — o nicho facilita recrutar rápido) e mantenha o teste ativo por **14 dias corridos ininterruptos** (obrigatório para contas pessoais criadas após novembro de 2023, antes de liberar produção pela primeira vez).
3. Durante esse período, monitore **Android Vitals** (Play Console → Qualidade) — taxa de crash e ANR baixas são pré-requisito informal para não ter a visibilidade da ficha reduzida.
4. Ao final dos 14 dias, promova a build para **Produção**, com **lançamento gradual** (ex.: 10% → 50% → 100% ao longo de alguns dias), para limitar o impacto de qualquer problema encontrado tardiamente.
5. Aproveite o teste fechado para também gerar o **Relatório pré-lançamento** (Pre-launch report, automático), que roda o app em vários aparelhos/versões de Android e aponta crashes, problemas de acessibilidade e de performance antes da revisão humana.

---

## 8. Ficha da loja (Store listing)

### 8.1 Textos prontos para colar

**Título do app** (máx. 30 caracteres):
```
Versículos do dia — Bíblia
```

**Descrição curta** (máx. 80 caracteres):
```
Versículos bíblicos por emoção: ansiedade, fé, gratidão. Palavra do dia!
```

**Descrição completa** (máx. 4000 caracteres — o texto abaixo é um ponto de partida; ajuste à vontade, ele já reflete o que o app realmente faz hoje):
```
Encontre o versículo certo para o seu momento — todos os dias, offline, sem precisar de conta.

O Versículos do dia organiza a Palavra por sentimento, não só por livro da Bíblia. Escolha como você está e receba versículos bíblicos escolhidos para aquele momento:

🙏 Gratidão · ✝️ Fé · 💔 Luto · 😨 Medo · 😠 Raiva · 🕊️ Oração · 🤝 Perdão · 🫂 Solidão · 😢 Tristeza · 😰 Ansiedade · 🎯 Propósito

PRINCIPAIS RECURSOS
• Versículos com referência bíblica, deslizando como cartões
• Favoritos ilimitados* — salve os versículos que mais tocam seu coração
• Compartilhe como imagem bonita ou texto direto no WhatsApp, Instagram e mais
• Notificações diárias no horário que você escolher
• Widget de tela inicial com o versículo do dia
• 4 temas visuais (Natureza, Oceano, Crepúsculo, Areia), com modo claro e escuro
• Funciona 100% offline — sem depender de internet para ler os versículos

VERSÍCULOS DO DIA PREMIUM
Assine para desbloquear todas as categorias, notificações ilimitadas, tema exclusivo Crepúsculo, compartilhamento sem marca d'água e uma experiência 100% sem anúncios.

*No plano gratuito, favoritos ficam limitados a 20 versículos.

Este app tem caráter devocional e inspiracional; não substitui aconselhamento profissional de saúde mental.
```

### 8.2 Categoria e tags
- **Categoria:** Estilo de vida (Lifestyle) — alternativa válida: Livros e referência, se você preferir competir nesse grupo. Apps de "versículo do dia" concorrentes usam ambas; Estilo de vida tende a combinar melhor com o ângulo emocional/categorias deste app.
- **Tags:** escolha entre as sugeridas pelo próprio Play Console no momento (a lista de tags é fechada e muda com o tempo) as que mais se aproximarem de: Bíblia, Religião, Devocional, Espiritualidade.

### 8.3 E-mail e site de contato
- **E-mail:** o mesmo usado na política de privacidade e nos termos (`devoluapp@gmail.com`, ou outro endereço de suporte dedicado, se preferir criar um).
- **Site (opcional):** `robertotavares.blog`, se for usado para hospedar a política de privacidade (seção 2).

### 8.4 Assets gráficos
| Asset | Status | Observação |
|---|---|---|
| Ícone 512×512 | Existe (`app/src/main/ic_launcher-playstore.png`) | Confirme que é a versão de alta resolução mais recente antes de subir. |
| Gráfico de recurso (feature graphic) 1024×500 | **Falta criar** | É obrigatório para publicar. Recomendo uma peça simples: fundo do tema "Crepúsculo" ou "Natureza", nome do app + um versículo curto de exemplo. |
| Screenshots de telefone | Prontos em `store-assets/screenshots/phone/` (4 imagens, 1080×2400) | Já cobre o mínimo de 2 exigido pela loja. |
| Screenshots de tablet 7" | Prontos em `store-assets/screenshots/tablet7/` (3 imagens, 1200×1920) | |
| Screenshots de tablet 10" | Prontos em `store-assets/screenshots/tablet10/` (3 imagens, 1600×2560) | |
| Vídeo promocional | Pronto localmente em `store-assets/video/demo_versiculos_do_dia.mp4` (~34s) | O campo do Play Console aceita só **link do YouTube**, não upload direto — suba esse vídeo (privado ou não listado, se preferir) e cole o link. |

---

## 9. Monetização — configurar AdMob e os produtos de compra

### 9.1 Vincular o AdMob
1. Crie/acesse uma conta em [admob.google.com](https://admob.google.com).
2. Cadastre o app (mesmo pacote `blog.robertotavares.cemversiculos`) e crie os 3 blocos de anúncio: **banner adaptativo**, **intersticial** e **recompensado** — os tipos exatamente usados em `AdManager.kt`.
3. Copie o **App ID** e os 3 **IDs de bloco de anúncio** gerados para `local.properties` (`admob.appId`, `admob.bannerId`, `admob.interstitialId`, `admob.rewardedId`) — nunca no código-fonte versionado.
4. Em **Play Console → Vincular contas do AdMob**, conecte a mesma conta para habilitar relatórios integrados.

### 9.2 Criar os produtos de assinatura e compra
Em **Play Console → Monetização → Produtos**, crie exatamente os IDs usados em `BillingManager.kt:46-58` — os IDs precisam ser **idênticos**, ou o app não encontrará os produtos:

| Onde criar | Product ID | Tipo | Preço sugerido | Observação |
|---|---|---|---|---|
| Assinaturas | `mensal_990` | Assinatura recorrente | R$ 9,90/mês | |
| Assinaturas | `anual_5900` | Assinatura recorrente | R$ 59,00/ano | Se quiser manter o selo "7 dias grátis" do paywall (`badge_free_trial` em `strings.xml`), configure uma **oferta com fase de teste gratuito de 7 dias** no base plan deste produto — sem essa oferta configurada, o selo fica incorreto e pode ser motivo de rejeição por informação enganosa sobre preço. |
| Produtos no app (não consumível) | `vitalicio_14900` | Compra única | R$ 149,00 | Marque como "não consumível" (o usuário compra uma vez e mantém para sempre). |

Depois de criados, ative todos e aguarde a propagação (pode levar algumas horas) antes de testar a compra.

### 9.3 License testing (testadores de licença)
Em **Play Console → Configuração → Testes de licença**, adicione os e-mails do Google que vão testar a compra (o seu, e o(s) do revisor se você preencher instruções específicas na seção 4.3). Contas nessa lista completam o fluxo real de billing sem cobrança de verdade.

---

## 10. Revisão de conteúdo específica deste app (itens que passam despercebidos)

- **Categorias sensíveis sem disclaimer no app:** o app tem categorias "Luto", "Medo", "Ansiedade", "Raiva", "Solidão", "Tristeza" sem nenhum aviso dentro da UI de que o conteúdo não substitui aconselhamento profissional. Isso **não bloqueia a publicação**, mas os Termos de Uso criados neste trabalho (`termos_de_uso.html`) já cobrem esse aviso — linkado em Configurações → Sobre. Se quiser reforçar ainda mais, considere um texto curto na tela de onboarding ao selecionar essas categorias.
- **Selo "7 dias grátis":** só é honesto se a oferta correspondente existir de fato no Play Console (seção 9.2) — revisar antes de publicar evita rejeição por "informação de preço enganosa".
- **Permissões:** o app só declara `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` — todas justificáveis e nenhuma exige formulário de permissão sensível separado (não há `SCHEDULE_EXACT_ALARM`, câmera, localização, contatos, SMS, ou acessibilidade). Nada a fazer aqui além de manter assim.
- **Notificações:** o app pede `POST_NOTIFICATIONS` em tempo de execução (Android 13+) — certifique-se de que o pedido só acontece depois de alguma interação do usuário (ex.: ao configurar horários em Configurações), não imediatamente ao abrir o app pela primeira vez, para não ser visto como fricção agressiva. Confirme o comportamento atual em `PermissionManager.kt`/`SettingsScreen.kt` antes de publicar.
- **Link de política de privacidade dentro do app:** implementado nesta atualização (Configurações → Sobre → "Política de Privacidade" / "Termos de Uso"), abrindo a URL configurada em `strings.xml`. Sem esse link dentro do app, apps que coletam qualquer dado (mesmo via SDKs de terceiros) podem ser reprovados mesmo com a URL certa só no Console.

---

## 11. Checklist final antes de enviar para revisão

- [ ] IDs reais de AdMob preenchidos em `local.properties` (não os de teste).
- [ ] `privacy_policy_url` e `terms_of_use_url` em `strings.xml` apontando para as URLs públicas reais, e `./gradlew assembleDebug` rodado com sucesso depois da troca.
- [ ] Política de Privacidade e Termos de Uso publicados nessas URLs e acessíveis por qualquer pessoa (teste em aba anônima).
- [ ] Produtos `mensal_990`, `anual_5900` (com oferta de 7 dias grátis, se mantiver o selo) e `vitalicio_14900` criados e ativos no Play Console, com os mesmos IDs.
- [ ] Teste de compra ponta a ponta feito com uma conta de License Tester.
- [ ] App content: Privacy policy, Ads (Sim), App access (com instruções em inglês), Content ratings, Target audience (18+), Data safety — todos preenchidos conforme seções 4 e 5.
- [ ] AAB assinado gerado (`./gradlew bundleRelease`) com a keystore de upload e Play App Signing habilitado.
- [ ] Ficha da loja completa: título, descrições, categoria, ícone, feature graphic (criar), screenshots (já prontos), vídeo no YouTube (subir e linkar).
- [ ] Teste fechado com 12+ testadores rodando (se conta pessoal pós-nov/2023) por 14 dias antes de pedir produção.
- [ ] Relatório pré-lançamento revisado, sem crashes críticos.

---

## 12. Depois de publicado

- Responda avaliações (reviews) nas primeiras semanas — impacta conversão e ranking.
- Acompanhe **Android Vitals** (crash rate, ANR) semanalmente.
- Peça avaliação in-app no momento certo — já implementado (`InAppReviewManager.kt`: 3º dia de uso ou 5º favorito).
- Releases futuras: sempre subir primeiro em teste interno/fechado antes de produção, mesmo depois de estabelecido — reduz risco de regressão em produção.

---

## Referências úteis
- [Central de Ajuda do Play Console — Data safety](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Política de Assinaturas do Google Play](https://support.google.com/googleplay/android-developer/answer/140504)
- [Requisitos de target API level](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Programa de testes fechados obrigatório para contas novas](https://support.google.com/googleplay/android-developer/answer/14151465)
- `RELATORIO-MERCADO-ASO.md` — contexto de mercado, estratégia de monetização e ASO mais aprofundado.
- `README.md` — arquitetura e stack técnica atual do app.
