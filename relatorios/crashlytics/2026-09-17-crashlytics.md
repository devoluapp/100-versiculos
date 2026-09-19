# Crashlytics: Bíblia - Versículos do Dia (blog.robertotavares.cemversiculos)

Extraído do console do Firebase (projeto versiculos-do-dia-c7b47) em 2026-09-17.
Filtro: problemas **abertos**, tipo **Falhas (fatais)**, **últimos 90 dias** (20/06 a 17/09/2026). Não fatais e ANRs não foram incluídos.
Stack traces com frames puramente do sistema (android.app, android.os, dalvik, zygote) resumidos como "... N frames do sistema". Cada issue mostra um evento de exemplo (o mais recente exibido pelo console).

## Visão geral

| Janela | Usuários sem falhas | Sessões sem falhas |
|---|---|---|
| 90 dias | 76,21% | 92,87% |
| 7 dias | 89,47% (-3,12%) | 98,06% (+0,08%) |

Lançamento mais recente: 1.11 (11).

| # | Problema | Local | Eventos | Usuários | Versões |
|---|---|---|---|---|---|
| 1 | RuntimeException: Window couldn't find content container view | MainActivity.kt:43 | 72 | 32 | 1.6 a 1.11 |
| 2 | NPE EditText.hasFocus() no TimePicker (spinner) | TimePickerSpinnerDelegate.java:480 | 17 | 11 | 1.10 a 1.11 |
| 3 | IllegalArgumentException: trampoline sem target intent (widget Glance) | ActionTrampoline.kt:93 | 7 | 7 | 1.9 |
| 4 | NPE ViewGroup.getChildAt(int) em setContent | MainActivity.kt:43 | 4 | 3 | 1.10 a 1.11 |
| 5 | IllegalArgumentException: All products should be of the same product type | BillingManager.kt:143 | 4 | 3 | 1.9 |
| 6 | NPE PendingIntent.getIntentSender() | ProxyBillingActivity (billing 7.1.1) | 4 | 4 | 1.9 |
| 7 | IllegalArgumentException: DecorView not attached to window manager | WindowManagerGlobal.java:756 | 1 | 1 | 1.11 |

## Prioridade sugerida para correção
1. **MainActivity.kt:43** (issues 1 e 4, 76 eventos, cerca de 35 usuários, ainda ativo na 1.11). Revisar o que acontece antes de `setContent` no `onCreate` (splash screen, `enableEdgeToEdge`, tema da Activity, acesso a `window`/`decorView`, Intent vindo da notificação ou do widget, Activity sendo finalizada ou recriada). Breadcrumb do exemplo: notificação exibida cerca de 1 minuto antes da falha.
2. **TimePicker em modo spinner** (issue 2, ativo na 1.10 e 1.11, concentrado em Motorola). Trocar o seletor de horário nativo por um TimePicker do Material 3 em Compose (ou TimePickerDialog em modo relógio), evitando o `TimePickerSpinnerDelegate`.
3. **BillingManager.kt:143** (issue 5) e fluxo de compra: consultar INAPP e SUBS separadamente e tratar timeout de conexão. Considerar atualizar a Play Billing Library (issue 6).
4. **Widget Glance** (issue 3): garantir Intent/ação válida nos itens de lista do widget (visto só na 1.9; confirmar se já foi corrigido).

---

## 1. MainActivity.onCreate: RuntimeException "Window couldn't find content container view"
ID: 356f8edf0ac5ce6fe52cca1a0cd13cb1 · arquivo: MainActivity.kt:43

### Números (90 dias)
- 72 eventos, 32 usuários
- Variante 3cb1: 45 eventos / 22 usuários · Variante eaf1: 27 eventos / 15 usuários
- Por versão: 1.11 (11): 61 · 1.10 (10): 7 · 1.6 (6): 2 · 1.9 (9): 2
- Fabricantes: Samsung 36% · Motorola 29% · Realme 24% · Google 4% · Xiaomi 4,2% · OnePlus 1,4% · Multilaser 1,4%
- SO: Android 15 35% · Android 12 6,9% · Android 13 6,9% · Android 9 1,4% (demais não exibidos)
- Estado: 0% em segundo plano (todas as falhas com o app em primeiro plano)

### Evento de exemplo
Versão 1.11 (11) · Android 12 · Moto E22 · 14/09/2026 19:20:07

### Stack trace (variante 3cb1)
```
Fatal Exception: java.lang.RuntimeException
Unable to start activity ComponentInfo{blog.robertotavares.cemversiculos/blog.robertotavares.cemversiculos.MainActivity}: java.lang.RuntimeException: Window couldn't find content container view
  at android.app.ActivityThread.performLaunchActivity (ActivityThread.java:3675)
  at android.app.ActivityThread.handleLaunchActivity (ActivityThread.java:3832)
  at android.app.servertransaction.LaunchActivityItem.execute (LaunchActivityItem.java:103)
  ... frames do sistema
Caused by java.lang.RuntimeException: Window couldn't find content container view
  at com.android.internal.policy.PhoneWindow.generateLayout (PhoneWindow.java:2668)
  at com.android.internal.policy.PhoneWindow.installDecor (PhoneWindow.java:2727)
  at com.android.internal.policy.PhoneWindow.getDecorView (PhoneWindow.java:2135)
  at androidx.activity.compose.ComponentActivityKt.setContent (ComponentActivity.kt:55)
  at androidx.activity.compose.ComponentActivityKt.setContent$default (ComponentActivity.kt:50)
  at blog.robertotavares.cemversiculos.MainActivity.onCreate (MainActivity.kt:43)
  at android.app.Activity.performCreate (Activity.java:8050)
  ... 16 frames do sistema
```

### Breadcrumbs do evento de exemplo
1. 19:19:14 [analytics] `notificacao_exibida` (referencia: Salmos 135:14)
2. 19:20:07 falha ao abrir a MainActivity

> Pista: o único evento antes da falha é a notificação exibida, cerca de 1 minuto antes. A abertura parece vir do toque na notificação (ou do widget), com a Activity sendo criada e `setContent` falhando ao montar a DecorView.

---

## 2. TimePickerSpinnerDelegate.updateInputState: NullPointerException em EditText.hasFocus()
ID: d64e2963112d7dcfebc018e24248040e · arquivo: TimePickerSpinnerDelegate.java:480 (framework, android.widget)

### Números (90 dias)
- 17 eventos, 11 usuários
- Variantes: 040e 10/7 · f8ab 3/1 · d305 2/1 · b3a0 1/1 · b995 1/1
- Por versão: 1.11 (11): 14 · 1.10 (10): 3
- Fabricantes: Motorola 59% · Samsung 23% · Realme 18%
- Estado: 0% em segundo plano

### Evento de exemplo
Versão 1.11 (11) · Android 15 · SM-A146M (Galaxy A14 5G) · 15/09/2026 01:05:24

### Stack trace (variante 040e)
```
Fatal Exception: java.lang.NullPointerException
Attempt to invoke virtual method 'boolean android.widget.EditText.hasFocus()' on a null object reference
  at android.widget.TimePickerSpinnerDelegate.updateInputState (TimePickerSpinnerDelegate.java:480)
  at android.widget.TimePickerSpinnerDelegate.-$$Nest$mupdateInputState (TimePickerSpinnerDelegate.java)
  at android.widget.TimePickerSpinnerDelegate$2.onValueChange (TimePickerSpinnerDelegate.java:119)
  at android.widget.NumberPicker.notifyChange (NumberPicker.java:2080)
  at android.widget.NumberPicker.setValueInternal (NumberPicker.java:1850)
  at android.widget.NumberPicker.validateInputTextView (NumberPicker.java:2030)
  at android.widget.NumberPicker.-$$Nest$mvalidateInputTextView (NumberPicker.java)
  at android.widget.NumberPicker$3.onFocusChange (NumberPicker.java:769)
  at android.view.View.onFocusChanged (View.java:9161)
  at android.widget.TextView.onFocusChanged (TextView.java:13779)
  at android.view.View.clearFocusInternal (View.java:8986)
  at android.view.View.clearFocus (View.java:8964)
  at android.view.ViewGroup.clearFocus (ViewGroup.java:1203)  (x4)
  ... 1 frame do sistema
  at android.view.View.performClick (View.java:8464)
  at android.widget.TextView.performClick (TextView.java:18389)
  at android.view.View.performClickInternal (View.java:8441)
  at android.view.View$PerformClick.run (View.java:32966)
  ... 8 frames do sistema
```
Nenhum frame do app aparece: a falha ocorre dentro do TimePicker nativo em modo spinner (provavelmente um `TimePickerDialog`/`TimePicker` via AndroidView na tela de horário da notificação), quando o foco é limpo após um clique.

### Breadcrumbs do evento de exemplo
1. 01:04:25 [analytics] `session_start`
2. 01:04:25 [analytics] `screen_view` (firebase_screen_class: MainActivity)
3. 01:05:24 falha (cerca de 1 minuto depois, ainda na MainActivity)

---

## 3. ActionTrampolineKt.launchTrampolineAction: IllegalArgumentException (widget Glance)
ID: babbf348be7e1a6e02bf8b613922247b · arquivo: ActionTrampoline.kt:93 (androidx.glance.appwidget) · marcado como "falha no início da sessão"

### Números (90 dias)
- 7 eventos, 7 usuários · Variantes: b425 4/4 · e8a2 3/3
- Por versão: só 1.9 (9) (não reapareceu nas versões 1.10 e 1.11)
- Fabricante: OnePlus 100% · Estado: 0% em segundo plano

### Evento de exemplo
Versão 1.9 (9) · Android 11 · OnePlus8Pro · 20/07/2026 08:13:34

### Stack trace
```
Fatal Exception: java.lang.RuntimeException
Unable to start activity ComponentInfo{blog.robertotavares.cemversiculos/androidx.glance.appwidget.action.InvisibleActionTrampolineActivity}: java.lang.IllegalArgumentException: List adapter activity trampoline invoked without specifying target intent.
  ... 13 frames do sistema
Caused by java.lang.IllegalArgumentException: List adapter activity trampoline invoked without specifying target intent.
  at androidx.glance.appwidget.action.ActionTrampolineKt.launchTrampolineAction (ActionTrampoline.kt:93)
  at androidx.glance.appwidget.action.InvisibleActionTrampolineActivity.onCreate (InvisibleActionTrampolineActivity.kt:31)
  ... 16 frames do sistema
```
Sem breadcrumbs. Ocorre ao tocar em item de lista (LazyColumn) do widget Glance cuja ação não tem Intent de destino.
Observação: mesmo aparelho e mesmo minuto da falha 6 (OnePlus8Pro, 20/07 08:13); pode ser um dispositivo de teste (ex.: relatório de pré-lançamento).

---

## 4. MainActivity.onCreate: NullPointerException em ViewGroup.getChildAt(int)
ID: 846fb9b0973edf51d0776db0f707d38f · arquivo: MainActivity.kt:43 (mesma linha da falha 1)

### Números (90 dias)
- 4 eventos, 3 usuários · Variantes: d38f 3/2 · 7b93 1/1
- Por versão: 1.11 (11): 3 · 1.10 (10): 1
- Fabricantes: Samsung 50% · Realme 25% · Google 25% · Estado: 0% em segundo plano

### Evento de exemplo
Versão 1.11 (11) · Android 15 · RMX3830 (Realme) · 16/08/2026 14:02:30

### Stack trace
```
Fatal Exception: java.lang.RuntimeException
Unable to start activity ComponentInfo{blog.robertotavares.cemversiculos/blog.robertotavares.cemversiculos.MainActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.View android.view.ViewGroup.getChildAt(int)' on a null object reference
  ... 14 frames do sistema
Caused by java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.View android.view.ViewGroup.getChildAt(int)' on a null object reference
  at androidx.activity.compose.ComponentActivityKt.setContent (ComponentActivity.kt:55)
  at androidx.activity.compose.ComponentActivityKt.setContent$default (ComponentActivity.kt:50)
  at blog.robertotavares.cemversiculos.MainActivity.onCreate (MainActivity.kt:43)
  ... 17 frames do sistema
```
Sem breadcrumbs. Mesma causa provável da falha 1: `setContent` em MainActivity.kt:43 sem o container de conteúdo da janela (`android.R.id.content`) disponível.

---

## 5. BillingManager.queryProducts: IllegalArgumentException "All products should be of the same product type."
ID: 92355cdae5a785ea1e0adcee696a0834 · arquivo: BillingManager.kt:143

### Números (90 dias)
- 4 eventos, 3 usuários · Variantes: dbbb 3/2 · f181 1/1
- Por versão: só 1.9 (9)
- Fabricantes: OnePlus 75% · Samsung 25%

### Evento de exemplo
Versão 1.9 (9) · Android 11 · OnePlus8Pro · 15/07/2026 08:52:53

### Stack trace (frames do app e do Compose; toque/dispatch do sistema omitidos)
```
Fatal Exception: java.lang.IllegalArgumentException
All products should be of the same product type.
  at com.android.billingclient.api.QueryProductDetailsParams$Builder.setProductList (QueryProductDetailsParams.java:8)
  at blog.robertotavares.cemversiculos.core.billing.BillingManager.queryProducts (BillingManager.kt:143)
  at blog.robertotavares.cemversiculos.core.billing.BillingManager.retry (BillingManager.kt:120)
  at blog.robertotavares.cemversiculos.presentation.paywall.PaywallViewModel.retry (PaywallViewModel.java:33)
  at blog.robertotavares.cemversiculos.presentation.paywall.PaywallScreenKt.PaywallScreen$lambda$4$0$1$0 (PaywallScreen.kt:105)
  at androidx.compose.foundation.ClickableNode.handleUpEvent (Clickable.kt:958)
  at androidx.compose.foundation.ClickableNode.onPointerEvent-H0pRuoY (Clickable.kt:895)
  at androidx.compose.ui.input.pointer.Node.dispatchMainEventPass (HitPathTracker.kt:446)
  at androidx.compose.ui.input.pointer.HitPathTracker.dispatchChanges (HitPathTracker.kt:181)
  at androidx.compose.ui.input.pointer.PointerInputEventProcessor.process-BIzXfog (PointerInputEventProcessor.java:118)
  at androidx.compose.ui.platform.AndroidComposeView.dispatchTouchEvent (AndroidComposeView.android.kt:2650)
  ... frames de dispatch de toque do sistema (ViewGroup/DecorView/ViewRootImpl)
```

### Breadcrumbs do evento de exemplo (mais antigo primeiro)
1. 08:50:57 `session_start`
2. 08:50:57 `screen_view` (MainActivity)
3. 08:51:54 `notificacao_exibida` (referencia: Salmos 135:3)
4. 08:52:00 `categoria_selecionada` (categoria: Fé)
5. 08:52:10 `paywall_visto`
6. 08:52:21 `billing_erro` (estagio: **timeout**)
7. 08:52:53 falha ao tocar em "tentar novamente" no paywall

> Pista: `QueryProductDetailsParams` recebe numa única lista produtos INAPP e SUBS. É preciso consultar cada tipo separadamente (duas chamadas). O `billing_erro` com estágio `timeout` antes da falha indica que a conexão com o Play Billing também falhou antes do retry.

---

## 6. com.android.billingclient.api.ProxyBillingActivity.onCreate: NullPointerException em PendingIntent.getIntentSender()
ID: a0b58a19c21e10b4b97c91d815bb58ec · biblioteca: com.android.billingclient:billing 7.1.1

### Números (90 dias)
- 4 eventos, 4 usuários (lista de problemas)
- Por versão: só 1.9 (9)
- Fabricante: OnePlus 100%

### Evento de exemplo
Versão 1.9 (9) · Android 11 · OnePlus8Pro · 20/07/2026 08:13:44

### Stack trace
```
Fatal Exception: java.lang.RuntimeException
Unable to start activity ComponentInfo{blog.robertotavares.cemversiculos/com.android.billingclient.api.ProxyBillingActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'android.content.IntentSender android.app.PendingIntent.getIntentSender()' on a null object reference
  ... 13 frames do sistema
Caused by java.lang.NullPointerException
  at com.android.billingclient.api.ProxyBillingActivity.onCreate (com.android.billingclient:billing@@7.1.1:14)
  ... 16 frames do sistema
```
Sem breadcrumbs. Típico de `ProxyBillingActivity` sendo iniciada/recriada sem os extras do fluxo de compra (ex.: aberta por teste automatizado ou restaurada pelo sistema). Considerar atualizar a Play Billing Library.

---

## 7. WindowManagerGlobal.findViewLocked: IllegalArgumentException "View ... not attached to window manager"
ID: 20559a5ef58e9ce04e300822d47e75b7

### Números (90 dias)
- 1 evento, 1 usuário · Por versão: 1.11 (11): 1 · Samsung 100%

### Evento de exemplo
Versão 1.11 (11) · Android 16 · SM-A155M (Galaxy A15) · 24/07/2026 09:52:47

### Stack trace
```
Fatal Exception: java.lang.IllegalArgumentException
View=com.android.internal.policy.DecorView{ede3ecd V.E...... R.....I. 0,0-0,0}[MainActivity] not attached to window manager
  at android.view.WindowManagerGlobal.findViewLocked (WindowManagerGlobal.java:756)
  at android.view.WindowManagerGlobal.updateViewLayout (WindowManagerGlobal.java:637)
  at android.view.WindowManagerImpl.updateViewLayout (WindowManagerImpl.java:165)
  ... 14 frames do sistema
```
Breadcrumbs: `session_start` e `screen_view` (MainActivity). Sem frames do app; algo atualiza o layout da janela da MainActivity depois que ela foi desanexada (possivelmente ligado à mesma instabilidade da falha 1).
